package com.unistack.app.feature_support.domain

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val QUICK_NOTES_PREFS = "unistack_quick_notes"
private const val QUICK_NOTES_TEXT = "text"

/**
 * El contenido del bloc, y si ya se leyó del disco.
 *
 * [loaded] existe para que la pantalla no confunda «todavía no he leído» con «está vacío»: sin
 * esa distinción, el campo arranca en blanco y el primer guardado automático borra lo que había.
 */
data class QuickNotes(
    val text: String = "",
    val loaded: Boolean = false
)

/**
 * El bloc de notas rápidas, guardado en este teléfono.
 *
 * No pasa por la base de datos ni por el respaldo: es un papel de usar y tirar, y meterlo en el
 * modelo obligaría a migraciones y a decidir a qué materia pertenece, que es justo lo que hace
 * que no se apunte nada.
 */
object QuickNotesStore {
    private val notesFlow = MutableStateFlow(QuickNotes())
    private var loaded = false

    fun observe(context: Context): StateFlow<QuickNotes> {
        ensureLoaded(context.applicationContext)
        return notesFlow
    }

    fun save(context: Context, text: String) {
        val app = context.applicationContext
        ensureLoaded(app)
        app.getSharedPreferences(QUICK_NOTES_PREFS, Context.MODE_PRIVATE).edit {
            putString(QUICK_NOTES_TEXT, text)
        }
        notesFlow.value = QuickNotes(text = text, loaded = true)
    }

    private fun ensureLoaded(app: Context) {
        if (loaded) return
        val stored = app.getSharedPreferences(QUICK_NOTES_PREFS, Context.MODE_PRIVATE)
            .getString(QUICK_NOTES_TEXT, "")
            .orEmpty()
        notesFlow.value = QuickNotes(text = stored, loaded = true)
        loaded = true
    }
}
