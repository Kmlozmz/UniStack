package com.unistack.app.feature_updates.domain

/**
 * Comparación de versiones publicadas, con etiquetas de preestreno.
 *
 * La versión anterior partía por puntos y descartaba lo que no fuera un número entero, así que
 * `1.0.0-alpha.1` se leía como `1.0.1`: el sufijo desaparecía y el número que lo acompañaba
 * pasaba a ocupar el lugar del parche. Una alpha se anunciaba como más nueva que la estable
 * que venía después.
 *
 * La regla es la de semver en lo que aquí importa: mandan los números y, a igualdad de
 * números, una versión con sufijo va **antes** que la misma sin él —`1.1.0-alpha.1` es
 * anterior a `1.1.0`—. Entre dos sufijos se comparan como texto, que basta para ordenar
 * `alpha` < `beta` < `rc`.
 */
data class ReleaseVersion(
    val numbers: List<Int>,
    val preRelease: String?
) : Comparable<ReleaseVersion> {

    override fun compareTo(other: ReleaseVersion): Int {
        val length = maxOf(numbers.size, other.numbers.size)
        for (index in 0 until length) {
            val mine = numbers.getOrElse(index) { 0 }
            val theirs = other.numbers.getOrElse(index) { 0 }
            if (mine != theirs) return mine.compareTo(theirs)
        }
        return when {
            preRelease == null && other.preRelease == null -> 0
            // Sin sufijo es la definitiva, y va después de cualquier preestreno suyo.
            preRelease == null -> 1
            other.preRelease == null -> -1
            else -> preRelease.compareTo(other.preRelease)
        }
    }

    companion object {
        fun parse(raw: String): ReleaseVersion {
            val cleaned = raw.trim().removePrefix("v").removePrefix("V")
            val separator = cleaned.indexOfFirst { it == '-' || it == '+' }
            val numeric = if (separator >= 0) cleaned.take(separator) else cleaned
            val suffix = if (separator >= 0) cleaned.substring(separator + 1) else null
            return ReleaseVersion(
                numbers = numeric.split('.').map { part ->
                    part.takeWhile(Char::isDigit).toIntOrNull() ?: 0
                },
                preRelease = suffix?.takeIf { it.isNotBlank() }
            )
        }

        /** Si [remote] es una versión posterior a [current]. */
        fun isNewer(remote: String, current: String): Boolean = parse(remote) > parse(current)

        /**
         * Si [remote] y [current] son la misma versión.
         *
         * Se comparan los números y el sufijo, no el texto: `v1.2.0-alpha.2` y `1.2.0-alpha.2`
         * son la misma publicación aunque no se escriban igual.
         */
        fun isSame(remote: String, current: String): Boolean =
            parse(remote).compareTo(parse(current)) == 0
    }
}
