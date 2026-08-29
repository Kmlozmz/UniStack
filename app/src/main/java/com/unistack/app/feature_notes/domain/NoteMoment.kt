package com.unistack.app.feature_notes.domain

import com.unistack.app.feature_schedule.domain.ClassSession
import java.time.LocalDateTime

/**
 * En qué clase estás ahora mismo, según tu propio horario.
 *
 * Es lo único que hace falta para la sugerencia del editor. Y es una **sugerencia**: la app sabe
 * a qué hora tienes Cálculo, no sabe si hoy fuiste. Por eso el resultado no se aplica solo, se
 * ofrece con un atajo y con un aspa para quitarlo de en medio.
 *
 * Vive en `domain` y recibe la hora en vez de mirarla: una función que consulta el reloj por
 * dentro solo se puede probar a las ocho de la mañana de un martes.
 */
object NoteMoment {

    /**
     * La materia de la clase que está ocurriendo, o nulo.
     *
     * Con dos clases solapadas gana la que empezó más tarde: si acabas de entrar a la segunda,
     * es de esa de la que estás tomando apuntes.
     */
    fun subjectInClassNow(sessions: List<ClassSession>, now: LocalDateTime): String? {
        val epochDay = now.toLocalDate().toEpochDay()
        val diaSemana = now.dayOfWeek.value
        val minuto = now.hour * 60 + now.minute

        return sessions
            .filter { it.occursOn(epochDay, diaSemana) }
            .filter { minuto >= it.startMinute && minuto < it.endMinute }
            .maxByOrNull { it.startMinute }
            ?.subjectId
    }

    /**
     * Si merece la pena sugerir algo.
     *
     * No se sugiere lo que ya está puesto, ni lo que ya se rechazó en esta nota. Insistir con
     * algo que acaban de quitar es la manera más rápida de que la sugerencia estorbe.
     */
    fun shouldSuggest(
        detectedSubjectId: String?,
        currentSubjectId: String?,
        dismissedSubjectId: String?
    ): Boolean {
        if (detectedSubjectId == null) return false
        if (detectedSubjectId == currentSubjectId) return false
        if (detectedSubjectId == dismissedSubjectId) return false
        return true
    }
}
