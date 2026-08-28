package com.unistack.app.feature_notes.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Un día del cuaderno: su fecha, cómo se llama en la cabecera y lo que se escribió ese día. */
data class NoteDay(
    val date: LocalDate,
    val label: String,
    val notes: List<QuickNote>
)

/**
 * Cómo se reparten las notas en la lista.
 *
 * Vive aquí y no en la pantalla porque el reparto es el mismo en cuaderno y en mosaico —cambia
 * cómo se dibuja, no qué va en cada montón— y porque así se puede probar sin encender un
 * teléfono.
 */
object NoteGrouping {

    private val spanish = Locale.forLanguageTag("es-ES")

    /**
     * Las fijadas, que se salen del orden del tiempo.
     *
     * Fijar una nota es decir «esta no envejece»: si se quedara en su día, al cabo de una semana
     * estaría enterrada, que es exactamente lo contrario de lo que se pidió.
     */
    fun pinned(notes: List<QuickNote>): List<QuickNote> = notes.filter { it.pinned }

    /** El resto, por días y de lo más reciente a lo más viejo. */
    fun byDay(
        notes: List<QuickNote>,
        today: LocalDate,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<NoteDay> {
        return notes.asSequence()
            .filterNot { it.pinned }
            .groupBy { dateOf(it, zone) }
            .toList()
            .sortedByDescending { (date, _) -> date }
            .map { (date, sameDay) ->
                NoteDay(
                    date = date,
                    label = dayLabel(date, today),
                    notes = sameDay.sortedByDescending { it.updatedAt }
                )
            }
    }

    fun dateOf(note: QuickNote, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(note.updatedAt).atZone(zone).toLocalDate()

    /**
     * Cómo se llama un día en la cabecera.
     *
     * «Hoy» y «Ayer» antes que la fecha, por lo mismo que en Gastos: quien abre las notas a las
     * once de la noche está mirando hoy, y leer «26 de agosto» obliga a comprobar qué día es hoy
     * para saber si eso es lo de hoy.
     */
    fun dayLabel(date: LocalDate, today: LocalDate): String = when {
        date == today -> "Hoy"
        date == today.minusDays(1) -> "Ayer"
        else -> {
            val pattern = if (date.year == today.year) "EEEE, d 'de' MMMM" else "d 'de' MMMM 'de' yyyy"
            date.format(DateTimeFormatter.ofPattern(pattern, spanish))
                .replaceFirstChar { it.uppercase(spanish) }
        }
    }

    /** La hora que se enseña al pie de cada nota. */
    fun timeLabel(note: QuickNote, use24Hour: Boolean, zone: ZoneId = ZoneId.systemDefault()): String {
        val time = Instant.ofEpochMilli(note.updatedAt).atZone(zone).toLocalTime()
        val pattern = if (use24Hour) "HH:mm" else "h:mm a"
        return time.format(DateTimeFormatter.ofPattern(pattern, spanish))
    }
}
