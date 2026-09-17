package com.unistack.app.core.utils

/**
 * Los 44 bytes de cabecera de un WAV mono de 16 bits.
 *
 * La usan las grabaciones de muestra de notas y de tareas del banco de pruebas, que escriben el
 * sonido a mano. Estaba copiada, igual, en los dos generadores de muestras.
 */
internal fun cabeceraWav(datos: Int, muestreo: Int): ByteArray {
    val bytesPorSegundo = muestreo * 2
    val cabecera = ByteArray(44)
    fun texto(pos: Int, valor: String) {
        valor.forEachIndexed { i, c -> cabecera[pos + i] = c.code.toByte() }
    }
    fun entero(pos: Int, valor: Int) {
        cabecera[pos] = (valor and 0xFF).toByte()
        cabecera[pos + 1] = ((valor shr 8) and 0xFF).toByte()
        cabecera[pos + 2] = ((valor shr 16) and 0xFF).toByte()
        cabecera[pos + 3] = ((valor shr 24) and 0xFF).toByte()
    }
    fun corto(pos: Int, valor: Int) {
        cabecera[pos] = (valor and 0xFF).toByte()
        cabecera[pos + 1] = ((valor shr 8) and 0xFF).toByte()
    }

    texto(0, "RIFF")
    entero(4, 36 + datos)
    texto(8, "WAVE")
    texto(12, "fmt ")
    entero(16, 16)
    corto(20, 1)
    corto(22, 1)
    entero(24, muestreo)
    entero(28, bytesPorSegundo)
    corto(32, 2)
    corto(34, 16)
    texto(36, "data")
    entero(40, datos)
    return cabecera
}
