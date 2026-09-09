package com.unistack.app.feature_schedule.presentation

import androidx.compose.ui.graphics.Color
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus

/**
 * Los colores de la asistencia, que no siguen al tema.
 *
 * El resto de la app toma su color del acento que cada uno elige, y eso está bien para lo que
 * es decoración. Aquí no: estos tres **significan algo concreto** —fuiste, no fuiste, no la
 * hubo— y el verde y el rojo son lo que los hace legibles de un vistazo sin leer nada.
 *
 * Seguían al tema, así que con el acento lila puesto una asistencia se pintaba lila y la tira
 * del historial dejaba de contar lo que tenía que contar: se veía un patrón de colores bonito
 * en lugar de faltas y asistencias.
 *
 * Están fijos también entre claro y oscuro a propósito. Son la misma información en los dos, y
 * un verde que cambia de tono según el fondo obligaría a reaprenderlo.
 *
 * design-tokens-ok-begin: la asistencia marca hechos y no puede seguir al acento del usuario
 */
internal val AttendanceAttended = Color(0xFF58D68D)
internal val AttendanceAbsent = Color(0xFFF1706F)
internal val AttendanceCancelled = Color(0xFFF0B429)
internal val AttendanceRescheduled = Color(0xFF8AA6F2)
// design-tokens-ok-end

/**
 * El color de un estado.
 *
 * `PENDING` no está aquí porque no es un hecho sino su ausencia: se pinta con el gris de la
 * superficie que le toque, y por eso lo resuelve quien lo dibuja.
 */
internal fun ClassAttendanceStatus.attendanceColor(): Color? = when (this) {
    ClassAttendanceStatus.ATTENDED -> AttendanceAttended
    ClassAttendanceStatus.ABSENT -> AttendanceAbsent
    ClassAttendanceStatus.CANCELLED -> AttendanceCancelled
    ClassAttendanceStatus.RESCHEDULED -> AttendanceRescheduled
    ClassAttendanceStatus.PENDING -> null
}

/** Cómo se llama cada estado en minúsculas, para la leyenda. */
internal fun ClassAttendanceStatus.legendName(): String = when (this) {
    ClassAttendanceStatus.ATTENDED -> if (java.util.Locale.getDefault().language == "en") "attended" else "asistí"
    ClassAttendanceStatus.ABSENT -> if (java.util.Locale.getDefault().language == "en") "absent" else "falta"
    ClassAttendanceStatus.CANCELLED -> if (java.util.Locale.getDefault().language == "en") "canceled" else "cancelada"
    ClassAttendanceStatus.RESCHEDULED -> if (java.util.Locale.getDefault().language == "en") "rescheduled" else "reprogramada"
    ClassAttendanceStatus.PENDING -> if (java.util.Locale.getDefault().language == "en") "unmarked" else "sin marcar"
}
