package com.unistack.app.feature_sync.domain

import android.content.Context
import com.unistack.app.core.utils.Textos
import com.unistack.app.R
import com.unistack.app.feature_user.domain.AppLanguage

interface LocalBackupRepository {
    fun exportBackupJson(): String
    fun previewBackupJson(json: String): Result<LocalBackupPreview>

    /**
     * Deja la app como estaba en la copia.
     *
     * Reemplaza, no mezcla, que es lo que promete el diálogo antes de restaurar: de cada cosa
     * que trae la copia se borra lo que no estaba en ella y se escribe lo que sí. Lo que una
     * copia antigua no trae, porque su versión no lo guardaba, se deja como está.
     *
     * @param localPhotoUri el retrato ya sacado del archivo de la copia, si traía uno.
     */
    suspend fun restoreBackupJson(json: String, localPhotoUri: String? = null): Result<LocalBackupPreview>
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
    val notes: Int = 0,
    val terms: Int = 0,
    /** El idioma que trae, que además de guardarse hay que aplicarlo; nulo si no trae ninguno. */
    val appLanguage: AppLanguage? = null
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
            cuenta(terms, Textos.get(R.string.backup_periodo), Textos.get(R.string.backup_periodos)),
            cuenta(subjects, Textos.get(R.string.backup_materia), Textos.get(R.string.backup_materias)),
            cuenta(grades, Textos.get(R.string.backup_nota), Textos.get(R.string.backup_notas)),
            cuenta(tasks, Textos.get(R.string.backup_tarea), Textos.get(R.string.backup_tareas)),
            cuenta(expenses, Textos.get(R.string.backup_gasto), Textos.get(R.string.backup_gastos)),
            cuenta(academicWorks, Textos.get(R.string.backup_trabajo), Textos.get(R.string.backup_trabajos)),
            cuenta(agendaEvents, Textos.get(R.string.backup_evento), Textos.get(R.string.backup_eventos)),
            cuenta(notes, Textos.get(R.string.backup_apunte), Textos.get(R.string.backup_apuntes))
        )
        return if (partes.isEmpty()) {
            Textos.get(R.string.backup_todavia_no_has_registrado_nada)
        } else partes.joinToString(" · ")
    }

    private fun cuenta(total: Int, singular: String, plural: String): String? =
        if (total <= 0) null else "$total ${if (total == 1) singular else plural}"
}
