package com.unistack.app.core.navigation

import android.content.Context

/**
 * Dato mínimo que hace falta saber en el primer fotograma, antes de que nada esté cargado.
 *
 * El perfil vive en DataStore, que es asíncrono: cuando la pantalla de lanzamiento empieza
 * a animarse todavía no se sabe si el setup está hecho, y para entonces ya hay que haber
 * decidido cuánto va a durar. Por eso se guarda aparte, en preferencias, que sí se leen en
 * el acto.
 *
 * Es una pista, no la verdad: quien manda sobre el estado del setup sigue siendo el perfil.
 * Aquí solo se usa para elegir el ritmo de una animación, así que fallar significa como
 * mucho verla una vez al ritmo que no tocaba.
 */
object LaunchHints {

    private const val PREFS_NAME = "unistack_launch_hints"
    private const val KEY_SETUP_COMPLETED = "setup_completed"

    /**
     * Si el usuario ya pasó por el setup en algún arranque anterior.
     *
     * En quien ya lo tenía hecho antes de que esto existiera devuelve false la primera vez;
     * se corrige solo en ese mismo arranque, en cuanto carga el perfil.
     */
    fun setupCompleted(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SETUP_COMPLETED, false)

    fun setSetupCompleted(context: Context, completed: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SETUP_COMPLETED, false) == completed) return
        prefs.edit().putBoolean(KEY_SETUP_COMPLETED, completed).apply()
    }
}
