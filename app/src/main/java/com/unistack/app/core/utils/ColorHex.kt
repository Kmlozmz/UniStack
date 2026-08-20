package com.unistack.app.core.utils

/**
 * «#5645D6» a color, o null si no lo es.
 *
 * Estaba escrito dos veces, en el formulario de materia y en Apariencia, que son los dos
 * sitios donde se teclea un color a mano. Y no eran iguales: la de Apariencia recorta los
 * espacios y comprueba que los seis caracteres sean hexadecimales; la otra le pasaba a
 * `toLongOrNull` cualquier cosa. Se queda la estricta.
 *
 * Devuelve null en lugar de lanzar porque lo que llega es texto que el usuario está
 * escribiendo: a medio escribir no es un error, es que todavía no ha terminado.
 */
fun String.toColorIntOrNull(): Int? {
    val normalized = trim().removePrefix("#")
    if (normalized.length != 6 || normalized.any { it !in "0123456789abcdefABCDEF" }) return null
    return runCatching {
        (0xFF000000L or normalized.toLong(16)).toInt()
    }.getOrNull()
}
