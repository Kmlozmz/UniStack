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
        val partes = listOfNotNull(
            cuenta(subjects, "materia", "materias"),
            cuenta(grades, "nota", "notas"),
            cuenta(tasks, "tarea", "tareas"),
            cuenta(expenses, "gasto", "gastos"),
            cuenta(academicWorks, "trabajo", "trabajos"),
            cuenta(agendaEvents, "evento", "eventos"),
            // «Apunte» y no «nota»: en esta app una nota es una calificación, y decir
            // «12 notas» al lado de «8 notas» sería contar dos cosas distintas igual.
            cuenta(notes, "apunte", "apuntes")
        )
        return if (partes.isEmpty()) "Todavía no has registrado nada" else partes.joinToString(" · ")
    }

    private fun cuenta(total: Int, singular: String, plural: String): String? =
        if (total <= 0) null else "$total ${if (total == 1) singular else plural}"
}
