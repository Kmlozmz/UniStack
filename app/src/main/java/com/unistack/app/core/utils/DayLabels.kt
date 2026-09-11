package com.unistack.app.core.utils

import java.time.DayOfWeek
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

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
    /** Una letra. En inglés: M T W T F S S. En español: L M X J V S D. */
    val short: List<String>
        get() {
            return Textos.get(R.string.day_labels_short).split(",")
        }

    /** Tres letras. En inglés: MON TUE WED... En español: LUN MAR MIÉ... */
    val medium: List<String>
        get() {
            return Textos.get(R.string.day_labels_medium).split(",")
        }

    fun short(day: DayOfWeek): String = short[day.value - 1]

    fun medium(day: DayOfWeek): String = medium[day.value - 1]

    /**
     * Etiqueta corta a partir del índice ISO del día (1 = lunes … 7 = domingo), que es
     * como se guardan en [com.unistack.app.feature_schedule.domain.ClassSession].
     */
    fun shortByIsoDay(isoDay: Int): String = short[(isoDay - 1).coerceIn(0, 6)]
}

/**
 * Los siete dias, ordenados a partir del que empieza la semana.
 *
 * El calendario y el grafico semanal estaban clavados en lunes —el `previousOrSame(MONDAY)` de
 * la rejilla, y `DayLabels.short` tal cual en las cabeceras—, asi que el ajuste de «primer dia
 * de la semana» no tenia por donde llegar. Aqui se rota la lista una vez y de ahi salen tanto
 * los rotulos como el dia con el que arranca la primera fila.
 *
 * Rotar y no reordenar: la semana sigue siendo la misma y lo unico que cambia es por donde se
 * corta. Con domingo primero, `D L M X J V S`.
 */
fun DayLabels.desde(primerDia: DayOfWeek): List<String> {
    val corte = primerDia.value - 1
    return short.drop(corte) + short.take(corte)
}

/** Los mismos siete, en tres letras. */
fun DayLabels.mediumDesde(primerDia: DayOfWeek): List<String> {
    val corte = primerDia.value - 1
    return medium.drop(corte) + medium.take(corte)
}

/**
 * Cuantas casillas vacias van antes del dia uno del mes.
 *
 * Estaba escrito como `dayOfWeek.value - 1`, que solo vale con la semana empezando en lunes:
 * con domingo primero, un mes que empieza en domingo dejaba seis huecos en vez de ninguno.
 */
fun huecosAntesDelUno(primerDelMes: DayOfWeek, primerDia: DayOfWeek): Int =
    Math.floorMod(primerDelMes.value - primerDia.value, 7)
