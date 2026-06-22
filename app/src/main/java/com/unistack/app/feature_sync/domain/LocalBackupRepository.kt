package com.unistack.app.feature_sync.domain

import android.content.Context

interface LocalBackupRepository {
    fun exportBackupJson(): String
    fun previewBackupJson(json: String): Result<LocalBackupPreview>
    fun restoreBackupJson(json: String): Result<LocalBackupPreview>
    fun exportAcademicReport(): String
    fun exportAcademicPdf(context: Context): Result<String>
    fun exportTasksCsv(): String
    fun exportExpensesCsv(): String
}

data class LocalBackupPreview(
    val schemaVersion: Int,
    val subjects: Int,
    val grades: Int,
    val tasks: Int,
    val expenses: Int,
    val academicWorks: Int,
    val agendaEvents: Int = 0
) {
    fun summary(): String {
        return "v$schemaVersion · $subjects materias · $grades notas · $tasks tareas · $expenses gastos · $academicWorks trabajos · $agendaEvents eventos"
    }
}
