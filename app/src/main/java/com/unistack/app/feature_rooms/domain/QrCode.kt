package com.unistack.app.feature_rooms.domain

/**
 * Un código QR de verdad (modo byte, corrección M, versiones 1 a 10) para compartir el código
 * de la sala. Sigue el algoritmo de referencia de la norma: patrones fijos, Reed-Solomon por
 * bloques intercalados, la máscara con menos penalización y los bits de formato.
 */
class QrCode private constructor(val size: Int, private val modules: Array<BooleanArray>) {

    fun isDark(x: Int, y: Int): Boolean = modules[y][x]

    companion object {
        private val ECC_PER_BLOCK = intArrayOf(-1, 10, 16, 26, 18, 24, 16, 18, 22, 22, 26)
        private val NUM_BLOCKS = intArrayOf(-1, 1, 1, 1, 2, 2, 4, 4, 4, 5, 5)

        fun encode(text: String): QrCode {
            val data = text.toByteArray(Charsets.UTF_8)
            var version = 1
            while (true) {
                val capacityBits = numDataCodewords(version) * 8
                val needed = 4 + (if (version < 10) 8 else 16) + data.size * 8
                if (needed <= capacityBits) break
                version++
                require(version <= 10) { "Texto demasiado largo para el QR" }
            }
            val bits = ArrayList<Boolean>()
            fun put(value: Int, len: Int) { for (i in len - 1 downTo 0) bits.add((value ushr i) and 1 != 0) }
            put(0b0100, 4)
            put(data.size, if (version < 10) 8 else 16)
            data.forEach { put(it.toInt() and 0xFF, 8) }
            val capacity = numDataCodewords(version) * 8
            put(0, minOf(4, capacity - bits.size))
            put(0, (8 - bits.size % 8) % 8)
            var pad = 0xEC
            while (bits.size < capacity) { put(pad, 8); pad = pad xor 0xEC xor 0x11 }
            val codewords = ByteArray(bits.size / 8)
            bits.forEachIndexed { i, b -> if (b) codewords[i ushr 3] = (codewords[i ushr 3].toInt() or (1 shl (7 - (i and 7)))).toByte() }
            return build(version, addEcc(codewords, version))
        }

        private fun numRawDataModules(ver: Int): Int {
            var result = (16 * ver + 128) * ver + 64
            if (ver >= 2) {
                val numAlign = ver / 7 + 2
                result -= (25 * numAlign - 10) * numAlign - 55
                if (ver >= 7) result -= 36
            }
            return result
        }

        private fun numDataCodewords(ver: Int) = numRawDataModules(ver) / 8 - ECC_PER_BLOCK[ver] * NUM_BLOCKS[ver]

        private fun addEcc(data: ByteArray, ver: Int): ByteArray {
            val numBlocks = NUM_BLOCKS[ver]
            val blockEccLen = ECC_PER_BLOCK[ver]
            val rawCodewords = numRawDataModules(ver) / 8
            val numShortBlocks = numBlocks - rawCodewords % numBlocks
            val shortBlockLen = rawCodewords / numBlocks
            val divisor = rsDivisor(blockEccLen)
            val blocks = ArrayList<ByteArray>()
            var k = 0
            for (i in 0 until numBlocks) {
                val datLen = shortBlockLen - blockEccLen + (if (i < numShortBlocks) 0 else 1)
                val dat = data.copyOfRange(k, k + datLen)
                k += datLen
                val block = ByteArray(shortBlockLen + 1)
                System.arraycopy(dat, 0, block, 0, dat.size)
                val ecc = rsRemainder(dat, divisor)
                System.arraycopy(ecc, 0, block, block.size - blockEccLen, ecc.size)
                blocks.add(block)
            }
            val result = ByteArray(rawCodewords)
            var r = 0
            for (i in 0 until blocks[0].size) {
                for (j in blocks.indices) {
                    if (i != shortBlockLen - blockEccLen || j >= numShortBlocks) { result[r] = blocks[j][i]; r++ }
                }
            }
            return result
        }

        private fun rsDivisor(degree: Int): ByteArray {
            val result = ByteArray(degree)
            result[degree - 1] = 1
            var root = 1
            for (i in 0 until degree) {
                for (j in result.indices) {
                    result[j] = gfMul(result[j].toInt() and 0xFF, root).toByte()
                    if (j + 1 < result.size) result[j] = (result[j].toInt() xor result[j + 1].toInt()).toByte()
                }
                root = gfMul(root, 0x02)
            }
            return result
        }

        private fun rsRemainder(data: ByteArray, divisor: ByteArray): ByteArray {
            val result = ByteArray(divisor.size)
            for (b in data) {
                val factor = (b.toInt() xor result[0].toInt()) and 0xFF
                System.arraycopy(result, 1, result, 0, result.size - 1)
                result[result.size - 1] = 0
                for (i in result.indices) result[i] = (result[i].toInt() xor gfMul(divisor[i].toInt() and 0xFF, factor)).toByte()
            }
            return result
        }

        private fun gfMul(x: Int, y: Int): Int {
            var z = 0
            for (i in 7 downTo 0) {
                z = (z shl 1) xor ((z ushr 7) * 0x11D)
                z = z xor (((y ushr i) and 1) * x)
            }
            return z and 0xFF
        }

        private fun build(ver: Int, codewords: ByteArray): QrCode {
            val size = ver * 4 + 17
            val m = Array(size) { BooleanArray(size) }
            val fn = Array(size) { BooleanArray(size) }
            fun set(x: Int, y: Int, dark: Boolean) { m[y][x] = dark; fn[y][x] = true }

            // Patrones de sincronía, localizadores, alineación, formato y versión.
            for (i in 0 until size) { set(6, i, i % 2 == 0); set(i, 6, i % 2 == 0) }
            fun finder(cx: Int, cy: Int) {
                for (dy in -4..4) for (dx in -4..4) {
                    val d = maxOf(kotlin.math.abs(dx), kotlin.math.abs(dy))
                    val x = cx + dx; val y = cy + dy
                    if (x in 0 until size && y in 0 until size) set(x, y, d != 2 && d != 4)
                }
            }
            finder(3, 3); finder(size - 4, 3); finder(3, size - 4)
            val align = alignmentPositions(ver, size)
            for (i in align.indices) for (j in align.indices) {
                if ((i == 0 && j == 0) || (i == 0 && j == align.size - 1) || (i == align.size - 1 && j == 0)) continue
                for (dy in -2..2) for (dx in -2..2) set(align[i] + dx, align[j] + dy, maxOf(kotlin.math.abs(dx), kotlin.math.abs(dy)) != 1)
            }
            fun formatBits(mask: Int) {
                val data = (0 shl 3) or mask // ECC M = 0b00
                var rem = data
                repeat(10) { rem = (rem shl 1) xor ((rem ushr 9) * 0x537) }
                val bits = ((data shl 10) or rem) xor 0x5412
                fun bit(i: Int) = (bits ushr i) and 1 != 0
                for (i in 0..5) set(8, i, bit(i))
                set(8, 7, bit(6)); set(8, 8, bit(7)); set(7, 8, bit(8))
                for (i in 9 until 15) set(14 - i, 8, bit(i))
                for (i in 0 until 8) set(size - 1 - i, 8, bit(i))
                for (i in 8 until 15) set(8, size - 15 + i, bit(i))
                set(8, size - 8, true)
            }
            formatBits(0)
            if (ver >= 7) {
                var rem = ver
                repeat(12) { rem = (rem shl 1) xor ((rem ushr 11) * 0x1F25) }
                val bits = (ver shl 12) or rem
                for (i in 0 until 18) {
                    val b = (bits ushr i) and 1 != 0
                    val a = size - 11 + i % 3; val c = i / 3
                    set(a, c, b); set(c, a, b)
                }
            }

            // Los datos en zigzag, de dos en dos columnas desde la derecha.
            var i = 0
            var right = size - 1
            while (right >= 1) {
                if (right == 6) right = 5
                for (vert in 0 until size) for (j in 0..1) {
                    val x = right - j
                    val upward = ((right + 1) and 2) == 0
                    val y = if (upward) size - 1 - vert else vert
                    if (!fn[y][x] && i < codewords.size * 8) {
                        m[y][x] = ((codewords[i ushr 3].toInt() ushr (7 - (i and 7))) and 1) != 0
                        i++
                    }
                }
                right -= 2
            }

            fun masked(mask: Int): Array<BooleanArray> = Array(size) { y ->
                BooleanArray(size) { x ->
                    val inv = when (mask) {
                        0 -> (x + y) % 2 == 0
                        1 -> y % 2 == 0
                        2 -> x % 3 == 0
                        3 -> (x + y) % 3 == 0
                        4 -> (x / 3 + y / 2) % 2 == 0
                        5 -> x * y % 2 + x * y % 3 == 0
                        6 -> (x * y % 2 + x * y % 3) % 2 == 0
                        else -> ((x + y) % 2 + x * y % 3) % 2 == 0
                    }
                    m[y][x] xor (inv && !fn[y][x])
                }
            }
            var best = 0
            var bestScore = Int.MAX_VALUE
            for (mask in 0..7) {
                val g = masked(mask)
                val s = penalty(g, size)
                if (s < bestScore) { bestScore = s; best = mask }
            }
            val out = masked(best)
            for (y in 0 until size) for (x in 0 until size) m[y][x] = out[y][x]
            formatBits(best)
            return QrCode(size, m)
        }

        private fun alignmentPositions(ver: Int, size: Int): IntArray {
            if (ver == 1) return IntArray(0)
            val numAlign = ver / 7 + 2
            val step = (ver * 4 + numAlign * 2 + 1) / (numAlign * 2 - 2) * 2
            val result = IntArray(numAlign)
            result[0] = 6
            var pos = size - 7
            for (i in numAlign - 1 downTo 1) { result[i] = pos; pos -= step }
            return result
        }

        /** Penalización simplificada (tramos largos y bloques 2×2): basta para elegir una máscara legible. */
        private fun penalty(g: Array<BooleanArray>, size: Int): Int {
            var score = 0
            for (y in 0 until size) {
                var run = 1
                for (x in 1 until size) { if (g[y][x] == g[y][x - 1]) { run++; if (run == 5) score += 3 else if (run > 5) score++ } else run = 1 }
            }
            for (x in 0 until size) {
                var run = 1
                for (y in 1 until size) { if (g[y][x] == g[y - 1][x]) { run++; if (run == 5) score += 3 else if (run > 5) score++ } else run = 1 }
            }
            for (y in 0 until size - 1) for (x in 0 until size - 1) {
                val c = g[y][x]
                if (c == g[y][x + 1] && c == g[y + 1][x] && c == g[y + 1][x + 1]) score += 3
            }
            var dark = 0
            for (row in g) for (v in row) if (v) dark++
            val total = size * size
            score += (kotlin.math.abs(dark * 20 - total * 10) + total - 1) / total * 10
            return score
        }
    }
}
