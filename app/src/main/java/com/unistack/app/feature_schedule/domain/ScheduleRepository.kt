package com.unistack.app.feature_schedule.domain

import kotlinx.coroutines.flow.StateFlow

interface ScheduleRepository {
    val sessions: StateFlow<List<ClassSession>>
    val occurrences: StateFlow<List<ClassOccurrence>>
    val agendaEvents: StateFlow<List<AgendaEvent>>

    fun saveSession(session: ClassSession)
    fun deleteSession(sessionId: String)
    fun saveOccurrence(occurrence: ClassOccurrence)
    fun deleteOccurrence(occurrenceId: String)
    fun saveAgendaEvent(event: AgendaEvent)
    fun deleteAgendaEvent(eventId: String)
}
