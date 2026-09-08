package com.unistack.app.core.notifications

import com.unistack.app.feature_user.domain.UserProfile
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** Qué hacer con un aviso concreto en este momento. */
internal enum class ReminderAction {
    /** Queda tiempo: se deja puesta la alarma. */
    ARM,

    /** Se pasó la hora pero lo que anunciaba aún no ha ocurrido: sale ya. */
    SEND_NOW,

    /** Ni se arma ni se manda: o cae fuera de la ventana, o ya no sirve de nada. */
    SKIP
}

/**
 * Cuándo sale cada aviso, decidido sin tocar Android.
 *
 * Esto vivía dentro de [LocalReminderScheduler], enredado con `AlarmManager`, `Context` y
 * `SharedPreferences`, y por eso no había forma de comprobarlo: para saber si un recordatorio
 * se perdía había que instalar la app y esperar a la hora de una clase. Aquí no hay nada de
 * eso —solo números y un perfil—, así que las reglas se pueden probar en un test de JVM.
 *
 * La separación no es de adorno: el fallo de «no me avisó de esa clase pero sí de la
 * siguiente» era una decisión equivocada en [decide], y con la lógica escondida detrás del
 * sistema de alarmas no había manera de verlo hasta que le pasó a alguien.
 */
internal object ReminderTiming {

    /**
     * Qué hacer con un aviso previsto para [triggerAtMillis].
     *
     * [eventAtMillis] es cuándo ocurre lo anunciado —el comienzo de la clase, el vencimiento
     * de la tarea—, y es lo que distingue un aviso que llega tarde pero sirve de uno que ya
     * no. Sin él, cualquier aviso pasado de hora se descarta: es lo correcto para los que no
     * anuncian un instante concreto.
     */
    fun decide(
        triggerAtMillis: Long,
        eventAtMillis: Long?,
        now: Long,
        windowMillis: Long
    ): ReminderAction = when {
        // Demasiado lejos: ya lo recogerá el rearmado cuando la ventana avance hasta él.
        triggerAtMillis - now > windowMillis -> ReminderAction.SKIP
        triggerAtMillis > now -> ReminderAction.ARM
        eventAtMillis != null && eventAtMillis > now -> ReminderAction.SEND_NOW
        else -> ReminderAction.SKIP
    }

    /**
     * Corre el aviso al final del tramo silencioso, si cae dentro.
     *
     * El tramo puede cruzar la medianoche —de 22 a 7—, y entonces «dentro» significa desde la
     * hora de inicio hasta el final del día o desde el principio del día hasta la de fin.
     */
    fun adjustForQuietHours(
        profile: UserProfile,
        triggerAtMillis: Long,
        zone: ZoneId = ZoneId.systemDefault()
    ): Long {
        if (!profile.quietHoursEnabled) return triggerAtMillis
        val start = profile.quietHoursStartHour ?: return triggerAtMillis
        val end = profile.quietHoursEndHour ?: return triggerAtMillis
        if (start == end) return triggerAtMillis

        val trigger = Instant.ofEpochMilli(triggerAtMillis).atZone(zone)
        val hour = trigger.hour
        val isQuiet = if (start < end) {
            hour in start until end
        } else {
            hour >= start || hour < end
        }
        if (!isQuiet) return triggerAtMillis

        val endDate = when {
            start < end -> trigger.toLocalDate()
            hour >= start -> trigger.toLocalDate().plusDays(1)
            else -> trigger.toLocalDate()
        }
        return endDate.atTime(end, 0).atZone(zone).toInstant().toEpochMilli()
    }

    /**
     * Los minutos que faltan, redondeados hacia arriba.
     *
     * Hacia arriba y no hacia abajo porque el número acompaña a «empieza en»: con 90 segundos
     * por delante, «en 2 min» se queda corto por poco y «en 1 min» ya va tarde. Nunca baja de
     * uno: «empieza en 0 min» no es una frase.
     */
    fun minutesUntil(eventAtMillis: Long, now: Long): Long {
        val restante = eventAtMillis - now
        if (restante <= 0L) return 0L
        return ((restante + 59_999L) / 60_000L).coerceAtLeast(1L)
    }

    /**
     * ¿La ventana de asistencia del día [date] sigue abierta?
     *
     * La ventana cierra [delayMinutes] después de que la clase termina ([endMinuteOfDay]).
     * Esto decide si la notificación de asistencia apunta a hoy o al próximo día: a
     * diferencia del recordatorio previo (atado al inicio de la clase), la asistencia se
     * ata al final.
     */
    fun isAttendanceWindowOpen(
        endMinuteOfDay: Int,
        delayMinutes: Long,
        date: LocalDate,
        now: LocalDateTime
    ): Boolean {
        val deadline = date.atStartOfDay().plusMinutes(endMinuteOfDay.toLong() + delayMinutes)
        return deadline.isAfter(now)
    }
}
