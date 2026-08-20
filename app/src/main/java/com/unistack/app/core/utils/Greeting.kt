package com.unistack.app.core.utils

import java.time.LocalTime

/**
 * El saludo, con el cielo que le toque a esa hora.
 *
 * La madrugada se lleva su propio tramo: hasta las seis, «Buenos días» era mentira y la app
 * lo decía igual. De seis a doce el sol, de doce a ocho la tarde, y el resto la luna.
 *
 * Vive aquí y no dentro de Inicio porque la vista previa de Apariencia enseña ese mismo
 * saludo: si cada una trae el suyo, la maqueta acaba prometiendo algo que la pantalla real
 * no dice.
 */
fun greetingForNow(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 6 -> "🌙 Buenas noches"
        hour < 12 -> "☀️ Buenos días"
        hour < 20 -> "🌤️ Buenas tardes"
        else -> "🌙 Buenas noches"
    }
}
