package com.unistack.app.feature_notes.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

/**
 * Cómo se reparten las notas en la lista.
 *
 * Vive aquí y no en la pantalla porque el reparto es el mismo en cuaderno y en mosaico —cambia
 * cómo se dibuja, no qué va en cada montón— y porque así se puede probar sin encender un
 * teléfono.
 */
object NoteGrouping {
    /**
     * Las fijadas, que se salen del orden del tiempo.
     *
     * Fijar una nota es decir «esta no envejece»: si se quedara en su día, al cabo de una semana
     * estaría enterrada, que es exactamente lo contrario de lo que se pidió.
     */
    fun pinned(notes: List<QuickNote>, sort: NotesSort = NotesSort.MODIFICADA): List<QuickNote> =
        ordenar(notes.filter { it.pinned }, sort)

    /** Todo lo demás, que en Keep es «Otras». */
    fun others(notes: List<QuickNote>, sort: NotesSort = NotesSort.MODIFICADA): List<QuickNote> =
        ordenar(notes.filterNot { it.pinned }, sort)

    private fun ordenar(notes: List<QuickNote>, sort: NotesSort): List<QuickNote> = when (sort) {
        NotesSort.MODIFICADA -> notes.sortedByDescending { it.updatedAt }
        NotesSort.CREADA -> notes.sortedByDescending { it.createdAt }
    }

    /**
     * Cómo se llama un día en la cabecera.
     *
     * «Hoy» y «Ayer» antes que la fecha, por lo mismo que en Gastos: quien abre las notas a las
     * once de la noche está mirando hoy, y leer «26 de agosto» obliga a comprobar qué día es hoy
     * para saber si eso es lo de hoy.
     */
    fun dayLabel(date: LocalDate, today: LocalDate): String = when {
        date == today -> Textos.get(R.string.date_today)
        date == today.minusDays(1) -> Textos.get(R.string.date_yesterday)
        else -> {
            val locale = Locale.getDefault()
            val pattern = Textos.get(if (date.year == today.year) R.string.notes_day_pattern_same_year else R.string.notes_day_pattern_other_year)
            date.format(DateTimeFormatter.ofPattern(pattern, locale))
                .replaceFirstChar { it.uppercase(locale) }
        }
    }

    /** La hora que se enseña al pie de cada nota. */
    fun timeLabel(note: QuickNote, use24Hour: Boolean, zone: ZoneId = ZoneId.systemDefault()): String {
        val time = Instant.ofEpochMilli(note.updatedAt).atZone(zone).toLocalTime()
        val pattern = if (use24Hour) "HH:mm" else "h:mm a"
        return time.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }
}
