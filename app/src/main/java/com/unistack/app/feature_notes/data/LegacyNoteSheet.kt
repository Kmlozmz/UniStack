package com.unistack.app.feature_notes.data

import android.content.Context
import androidx.core.content.edit

/**
 * La hoja única de antes, para no perderla al pasar a notas sueltas.
 *
 * Hasta ahora las notas rápidas eran un solo texto de cuatro mil caracteres guardado en las
 * preferencias del teléfono, con este mismo nombre de archivo y esta misma clave. Se leen de ahí
 * tal cual: cambiar el sitio donde estaban sería empezar por borrar lo que se viene a rescatar.
 *
 * Se entrega **una vez**. La marca de entregado se escribe antes de que nadie pueda volver a
 * pedirla, para que dos arranques seguidos no acaben en dos notas iguales, y el texto original se
 * conserva en su clave por si algo saliera mal en el camino.
 */
object LegacyNoteSheet {
    private const val PREFS = "unistack_quick_notes"
    private const val TEXT = "text"
    private const val IMPORTED = "imported_into_notes"

    /** El texto de la hoja vieja si queda algo por rescatar, o nulo si no hay nada o ya se hizo. */
    fun take(context: Context): String? {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(IMPORTED, false)) return null
        val text = prefs.getString(TEXT, "").orEmpty()
        prefs.edit { putBoolean(IMPORTED, true) }
        return text.takeIf { it.isNotBlank() }
    }
}
