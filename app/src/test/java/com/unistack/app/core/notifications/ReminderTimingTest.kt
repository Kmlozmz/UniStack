package com.unistack.app.core.notifications

import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.UserProfile
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Las reglas de cuándo sale cada aviso.
 *
 * Esta carpeta no tenía ninguna prueba, y el precio se pagó: un recordatorio de clase
 * desaparecía si el usuario tocaba la app en el minuto equivocado, y solo se supo porque le
 * ocurrió a alguien de verdad. El caso está abajo con su nombre.
 */
class ReminderTimingTest {

    private val zone = ZoneId.of("America/Bogota")
    private val ventana = 48L * 60L * 60L * 1000L
    private val minuto = 60_000L

    private fun momento(texto: String): Long =
        LocalDateTime.parse(texto).atZone(zone).toInstant().toEpochMilli()

    private fun perfil(
        quietHoursEnabled: Boolean = false,
        quietHoursStartHour: Int? = null,
        quietHoursEndHour: Int? = null
    ) = UserProfile(
        userId = "u",
        preferredName = "Estudiante",
        careerOrProgram = "Ingenieria",
        studyArea = null,
        gradingScale = GradingScale.ZERO_TO_FIVE,
        passingGrade = 3.0,
        targetAverage = 4.0,
        enabledModules = AppModule.entries.toSet(),
        setupCompleted = true,
        createdAt = 0L,
        updatedAt = 0L,
        quietHoursEnabled = quietHoursEnabled,
        quietHoursStartHour = quietHoursStartHour,
        quietHoursEndHour = quietHoursEndHour
    )

    // --- decide() ---

    @Test
    fun `un aviso futuro dentro de la ventana se arma`() {
        val ahora = momento("2026-03-02T07:00")
        assertEquals(
            ReminderAction.ARM,
            ReminderTiming.decide(
                triggerAtMillis = ahora + 50 * minuto,
                eventAtMillis = ahora + 60 * minuto,
                now = ahora,
                windowMillis = ventana
            )
        )
    }

    @Test
    fun `un aviso mas alla de la ventana se deja para el rearmado`() {
        val ahora = momento("2026-03-02T07:00")
        assertEquals(
            ReminderAction.SKIP,
            ReminderTiming.decide(
                triggerAtMillis = ahora + ventana + minuto,
                eventAtMillis = ahora + ventana + 10 * minuto,
                now = ahora,
                windowMillis = ventana
            )
        )
    }

    @Test
    fun `justo en el borde de la ventana todavia se arma`() {
        val ahora = momento("2026-03-02T07:00")
        assertEquals(
            ReminderAction.ARM,
            ReminderTiming.decide(
                triggerAtMillis = ahora + ventana,
                eventAtMillis = ahora + ventana + minuto,
                now = ahora,
                windowMillis = ventana
            )
        )
    }

    /**
     * El fallo que lo empezó todo.
     *
     * Un usuario no recibió el aviso de la clase a la que iba, pero sí el de la siguiente.
     * Causa: la alarma seguía pendiente pasada su hora, abrir la app la canceló, y al
     * reprogramar se descartaba por tener la hora vencida. La de la clase siguiente aún estaba
     * en el futuro, así que sobrevivía.
     */
    @Test
    fun `el aviso de una clase que aun no ha empezado sale aunque su hora haya pasado`() {
        val ahora = momento("2026-03-02T07:52")
        val avisoPrevisto = momento("2026-03-02T07:50")
        val comienzoDeClase = momento("2026-03-02T08:00")

        assertEquals(
            ReminderAction.SEND_NOW,
            ReminderTiming.decide(
                triggerAtMillis = avisoPrevisto,
                eventAtMillis = comienzoDeClase,
                now = ahora,
                windowMillis = ventana
            )
        )
    }

    @Test
    fun `el aviso de una clase que ya empezo se descarta`() {
        val ahora = momento("2026-03-02T08:30")
        assertEquals(
            ReminderAction.SKIP,
            ReminderTiming.decide(
                triggerAtMillis = momento("2026-03-02T07:50"),
                eventAtMillis = momento("2026-03-02T08:00"),
                now = ahora,
                windowMillis = ventana
            )
        )
    }

    @Test
    fun `sin momento del evento, un aviso vencido no se envia`() {
        val ahora = momento("2026-03-02T08:30")
        assertEquals(
            ReminderAction.SKIP,
            ReminderTiming.decide(
                triggerAtMillis = momento("2026-03-02T07:50"),
                eventAtMillis = null,
                now = ahora,
                windowMillis = ventana
            )
        )
    }

    // --- minutesUntil() ---

    @Test
    fun `los minutos que faltan se redondean hacia arriba`() {
        val ahora = momento("2026-03-02T07:52")
        assertEquals(8L, ReminderTiming.minutesUntil(ahora + 8 * minuto, ahora))
        // Siete minutos y medio son ocho, no siete: el aviso no puede quedarse corto.
        assertEquals(8L, ReminderTiming.minutesUntil(ahora + 7 * minuto + 30_000L, ahora))
        // Nunca «empieza en 0 min» mientras quede algo por delante.
        assertEquals(1L, ReminderTiming.minutesUntil(ahora + 1_000L, ahora))
        assertEquals(0L, ReminderTiming.minutesUntil(ahora, ahora))
    }

    // --- adjustForQuietHours() ---

    @Test
    fun `sin horario silencioso el aviso no se mueve`() {
        val previsto = momento("2026-03-02T03:00")
        assertEquals(
            previsto,
            ReminderTiming.adjustForQuietHours(perfil(), previsto, zone)
        )
    }

    @Test
    fun `un aviso de madrugada se corre al final del tramo que cruza medianoche`() {
        val previsto = momento("2026-03-02T03:00")
        assertEquals(
            momento("2026-03-02T07:00"),
            ReminderTiming.adjustForQuietHours(
                perfil(quietHoursEnabled = true, quietHoursStartHour = 22, quietHoursEndHour = 7),
                previsto,
                zone
            )
        )
    }

    @Test
    fun `un aviso de la noche espera al final del tramo, ya al dia siguiente`() {
        val previsto = momento("2026-03-02T23:30")
        assertEquals(
            momento("2026-03-03T07:00"),
            ReminderTiming.adjustForQuietHours(
                perfil(quietHoursEnabled = true, quietHoursStartHour = 22, quietHoursEndHour = 7),
                previsto,
                zone
            )
        )
    }

    @Test
    fun `un aviso fuera del tramo silencioso se queda donde estaba`() {
        val previsto = momento("2026-03-02T15:00")
        assertEquals(
            previsto,
            ReminderTiming.adjustForQuietHours(
                perfil(quietHoursEnabled = true, quietHoursStartHour = 22, quietHoursEndHour = 7),
                previsto,
                zone
            )
        )
    }

    @Test
    fun `un tramo silencioso que no cruza medianoche tambien corre el aviso`() {
        val previsto = momento("2026-03-02T14:30")
        assertEquals(
            momento("2026-03-02T16:00"),
            ReminderTiming.adjustForQuietHours(
                perfil(quietHoursEnabled = true, quietHoursStartHour = 13, quietHoursEndHour = 16),
                previsto,
                zone
            )
        )
    }

    @Test
    fun `un tramo con inicio y fin iguales se ignora`() {
        val previsto = momento("2026-03-02T03:00")
        assertEquals(
            previsto,
            ReminderTiming.adjustForQuietHours(
                perfil(quietHoursEnabled = true, quietHoursStartHour = 7, quietHoursEndHour = 7),
                previsto,
                zone
            )
        )
    }

    // --- isAttendanceWindowOpen() ---

    @Test
    fun `la ventana de asistencia sigue abierta con la clase en curso`() {
        // Clase 8:00–10:00, con 20 min de margen. Ventana hasta 10:20.
        val now = LocalDateTime.parse("2026-03-02T08:30")
        assertTrue(
            ReminderTiming.isAttendanceWindowOpen(
                endMinuteOfDay = 10 * 60,
                delayMinutes = 20L,
                date = now.toLocalDate(),
                now = now
            )
        )
    }

    @Test
    fun `la ventana de asistencia sigue abierta justo despues de acabar la clase`() {
        // Clase termina a las 10:00, ahora son las 10:05. Ventana hasta 10:20.
        val now = LocalDateTime.parse("2026-03-02T10:05")
        assertTrue(
            ReminderTiming.isAttendanceWindowOpen(
                endMinuteOfDay = 10 * 60,
                delayMinutes = 20L,
                date = now.toLocalDate(),
                now = now
            )
        )
    }

    @Test
    fun `la ventana de asistencia cierra pasados los minutos de margen`() {
        // Clase termina a las 10:00, ahora son las 10:25. Ventana cerró a las 10:20.
        val now = LocalDateTime.parse("2026-03-02T10:25")
        assertFalse(
            ReminderTiming.isAttendanceWindowOpen(
                endMinuteOfDay = 10 * 60,
                delayMinutes = 20L,
                date = now.toLocalDate(),
                now = now
            )
        )
    }

    @Test
    fun `la ventana de asistencia de ayer esta cerrada`() {
        val now = LocalDateTime.parse("2026-03-03T08:00")
        val ayer = now.toLocalDate().minusDays(1)
        assertFalse(
            ReminderTiming.isAttendanceWindowOpen(
                endMinuteOfDay = 10 * 60,
                delayMinutes = 20L,
                date = ayer,
                now = now
            )
        )
    }
}
