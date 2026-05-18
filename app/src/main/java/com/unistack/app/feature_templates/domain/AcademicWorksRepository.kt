package com.unistack.app.feature_templates.domain

import kotlinx.coroutines.flow.StateFlow

interface AcademicWorksRepository {
    val works: StateFlow<List<AcademicWork>>

    fun addWork(work: AcademicWork)
    fun updateWork(work: AcademicWork)
    fun deleteWork(workId: String)
    fun setChecklistItem(workId: String, checklistItemId: String, completed: Boolean)
    fun setStatus(workId: String, status: AcademicWorkStatus)
}
