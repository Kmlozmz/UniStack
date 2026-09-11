package com.unistack.app.feature_notes.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

/**
 * Los recordatorios de una nota.
 *
 * Es lo que convierte un apunte en algo que vuelve solo. «Traer la calculadora» no sirve de nada
 * guardado si nadie lo lee la mañana del parcial, y esa es exactamente la nota que más se escribe.
 *
 * Vive en `domain` y recibe el «ahora» en vez de mirarlo: una función que consulta el reloj por
 * dentro solo se puede probar a la hora que sea en ese momento.
 */
object NoteReminders {


    /**
     * Cómo se lee un recordatorio: «Hoy 8:00», «Mañana 8:00», «14 de septiembre, 8:00».
     *
     * Los dos días de al lado se dicen por su nombre porque son los que se miran de verdad; para
     * el resto hace falta la fecha, y el año solo si no es este.
     */
    fun label(
        at: Long,
        now: LocalDateTime = LocalDateTime.now(),
        zone: ZoneId = ZoneId.systemDefault()
    ): String {
        val cuando = Instant.ofEpochMilli(at).atZone(zone).toLocalDateTime()
        val hoy = now.toLocalDate()
        val dia = cuando.toLocalDate()
        val hora = cuando.toLocalTime().format(DateTimeFormatter.ofPattern("H:mm", Locale.getDefault()))

        return when {
            dia == hoy -> Textos.get(R.string.reminder_today_at, hora)
            dia == hoy.plusDays(1) -> Textos.get(R.string.reminder_tomorrow_at, hora)
            dia == hoy.minusDays(1) -> Textos.get(R.string.reminder_yesterday_at, hora)
            else -> {
                val en = Locale.getDefault().language == "en"
                val patron = when {
                    en && dia.year == hoy.year -> "MMMM d"
                    en -> "MMMM d, yyyy"
                    dia.year == hoy.year -> "d 'de' MMMM"
                    else -> "d 'de' MMMM 'de' yyyy"
                }
                dia.format(DateTimeFormatter.ofPattern(patron, Locale.getDefault())) + ", " + hora
            }
        }
    }

    /** Si el momento ya pasó. Un recordatorio vencido se enseña distinto, no se esconde. */
    fun isDue(at: Long, now: Long = System.currentTimeMillis()): Boolean = at <= now

    /** Las notas que avisan, de la más próxima a la más lejana. */
    fun upcoming(notes: List<QuickNote>): List<QuickNote> =
        notes.filter { it.reminderAt != null }.sortedBy { it.reminderAt }

    /**
     * Un momento razonable de partida cuando todavía no hay ninguno.
     *
     * Mañana a las ocho: poner la hora actual haría que el aviso saltara mientras se escribe la
     * nota, y ponerlo dentro de una hora obliga a hacer la cuenta para saber qué hora es esa.
     */
    fun defaultMoment(now: LocalDateTime = LocalDateTime.now()): LocalDateTime =
        LocalDate.from(now).plusDays(1).atTime(8, 0)
}
