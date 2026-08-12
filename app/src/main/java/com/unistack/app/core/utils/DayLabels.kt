package com.unistack.app.core.utils

import java.time.DayOfWeek

/**
 * Nombres de los días de la semana, en un solo sitio.
 *
 * Estaban escritos a mano en diez lugares con tres formas distintas, y dos de ellas se
 * contradecían: unas pantallas usaban `M` tanto para martes como para miércoles y otras
 * reservaban `X` para miércoles. En Gastos y en Horario se veían las dos convenciones a
 * dos toques de distancia.
 *
 * Gana `X`, que es lo estándar en Colombia y además resuelve la ambigüedad: dos emes
 * seguidas obligan a contar posiciones para saber cuál es cuál.
 *
 * El orden empieza en lunes, igual que [DayOfWeek.getValue], para que indexar por
 * `day.value - 1` sea correcto.
 */
object DayLabels {
    /** Una letra: `L M X J V S D`. Para rejillas y selectores de días. */
    val short: List<String> = listOf("L", "M", "X", "J", "V", "S", "D")

    /** Tres letras: `LUN MAR MIÉ…`. Para cabeceras de calendario, donde hay sitio. */
    val medium: List<String> = listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM")

    fun short(day: DayOfWeek): String = short[day.value - 1]

    fun medium(day: DayOfWeek): String = medium[day.value - 1]

    /**
     * Etiqueta corta a partir del índice ISO del día (1 = lunes … 7 = domingo), que es
     * como se guardan en [com.unistack.app.feature_schedule.domain.ClassSession].
     */
    fun shortByIsoDay(isoDay: Int): String = short[(isoDay - 1).coerceIn(0, 6)]
}
