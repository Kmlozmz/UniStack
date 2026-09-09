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
    val agendaEvents: Int = 0,
    val notes: Int = 0
) {
    /**
     * Lo que hay dentro, contado en cristiano.
     *
     * Empezaba por «v10» —el número de esquema de la base—, que no significa nada fuera del
     * código y encima ocupaba el primer sitio, que es el que se lee. Y luego encadenaba seis
     * cifras aunque cinco fueran cero, así que una app recién instalada decía «0 materias · 0
     * notas · 0 tareas · 0 gastos · 0 trabajos · 0 eventos»: dos líneas para decir que no hay
     * nada.
     *
     * Ahora solo salen las cosas de las que hay alguna, con el singular donde toca, y cuando no
     * hay ninguna se dice una vez.
     */
    fun summary(): String {
        val isEn = java.util.Locale.getDefault().language == "en"
        val partes = listOfNotNull(
            cuenta(subjects, if (isEn) "subject" else "materia", if (isEn) "subjects" else "materias"),
            cuenta(grades, if (isEn) "grade" else "nota", if (isEn) "grades" else "notas"),
            cuenta(tasks, if (isEn) "task" else "tarea", if (isEn) "tasks" else "tareas"),
            cuenta(expenses, if (isEn) "expense" else "gasto", if (isEn) "expenses" else "gastos"),
            cuenta(academicWorks, if (isEn) "work" else "trabajo", if (isEn) "works" else "trabajos"),
            cuenta(agendaEvents, if (isEn) "event" else "evento", if (isEn) "events" else "eventos"),
            cuenta(notes, if (isEn) "note" else "apunte", if (isEn) "notes" else "apuntes")
        )
        return if (partes.isEmpty()) {
            if (isEn) "You haven't recorded anything yet" else "Todavía no has registrado nada"
        } else partes.joinToString(" · ")
    }

    private fun cuenta(total: Int, singular: String, plural: String): String? =
        if (total <= 0) null else "$total ${if (total == 1) singular else plural}"
}
