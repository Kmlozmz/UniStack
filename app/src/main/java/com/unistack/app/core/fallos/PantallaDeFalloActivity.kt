package com.unistack.app.core.fallos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import com.unistack.app.MainActivity
import com.unistack.app.core.design.theme.UniStackTheme
import com.unistack.app.core.utils.LocaleHelper
import com.unistack.app.feature_support.presentation.FlujoDeFallo

/**
 * La pantalla del fallo cazado al vuelo, en su propio proceso.
 *
 * **Vive en `:fallo` a propósito** (ver el manifiesto). El proceso de la app se está muriendo
 * cuando esto arranca: si la actividad compartiera proceso con él, moriría con él y no se
 * vería nada. Con un proceso aparte, el que se cae se cae y esta pantalla sigue en pie.
 *
 * **No usa Hilt ni lee preferencias.** Este proceso no tiene los repositorios levantados y
 * tampoco conviene levantarlos: pedirle a una app que acaba de romperse que abra su base de
 * datos para saber qué tema tiene puesto es buscar un segundo fallo encima del primero. El
 * tema sale de sus valores por defecto siguiendo al del sistema, que acierta casi siempre y
 * nunca revienta. El idioma sí se respeta, porque ese está en una preferencia suelta.
 */
class PantallaDeFalloActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val conIdioma = runCatching { LocaleHelper.applyLocale(newBase) }.getOrDefault(newBase)
        super.attachBaseContext(conIdioma)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val informe = AlmacenDeFallos.pendiente(this)
        if (informe == null) {
            // Sin informe no hay nada que enseñar: mejor devolver al usuario a la app que
            // dejarle mirando una pantalla vacía.
            volverALaApp()
            return
        }
        setContent {
            UniStackTheme(darkTheme = isSystemInDarkTheme()) {
                FlujoDeFallo(
                    informe = informe,
                    enElActo = true,
                    onTerminar = { volverALaApp() }
                )
            }
        }
    }

    /**
     * Reiniciar de verdad: tarea nueva y pila limpia.
     *
     * Volver atrás no sirve —la actividad que había detrás se fue con el proceso muerto— así
     * que la app arranca otra vez desde su pantalla de entrada, con su animación y todo.
     */
    private fun volverALaApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }
}
