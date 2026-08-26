package com.unistack.app

import java.util.Locale
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val pendingLaunchRoute = mutableStateOf<String?>(null)

    /*
     * La app va en español, también la parte que no escribimos nosotros.
     *
     * Todo el texto de UniStack está escrito a mano en español, pero los componentes de Material
     * traen los suyos y esos siguen el idioma del teléfono: en un móvil en inglés, el selector de
     * fecha decía «Select dates», «Start date» y ponía los días como S M T W T F S en mitad de
     * una pantalla que en la línea de arriba dice «Elige el periodo». Fijando el idioma del
     * contexto, los textos de la libreria salen en el mismo idioma que los nuestros.
     *
     * Va aquí y no en un ajuste porque hoy no hay traducción que elegir: la app existe en un
     * solo idioma. Cuando `AppLanguage` sirva para algo, este es el sitio donde leerlo.
     */
    override fun attachBaseContext(newBase: Context) {
        val spanish = Locale.forLanguageTag("es")
        Locale.setDefault(spanish)
        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(spanish)
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingLaunchRoute.value = intent.resolveLaunchRoute()
        applyEdgeToEdge(darkTheme = isSystemInDarkMode())
        // El permiso de notificaciones ya no se pide aquí: saltaba nada más instalar, sin
        // que el usuario supiera para qué. Ahora se pide en su paso del onboarding, después
        // de explicar qué avisos va a recibir.
        setContent {
            UniStackApp(
                launchRoute = pendingLaunchRoute.value,
                onLaunchRouteConsumed = { pendingLaunchRoute.value = null },
                onDarkThemeChanged = { darkTheme ->
                    applyEdgeToEdge(darkTheme = darkTheme)
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingLaunchRoute.value = intent.resolveLaunchRoute()
    }

    private fun applyEdgeToEdge(darkTheme: Boolean) {
        val systemBarStyle = if (darkTheme) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(
            statusBarStyle = systemBarStyle,
            navigationBarStyle = systemBarStyle
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    private fun isSystemInDarkMode(): Boolean {
        val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return uiMode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun Intent.launchRoute(): String? = getStringExtra(EXTRA_LAUNCH_ROUTE)

    private fun Intent.resolveLaunchRoute(): String? {
        val explicit = launchRoute()
        if (explicit != null) return explicit

        val navigateTo = getStringExtra("navigate_to")
        return when (navigateTo) {
            "updates" -> "settings/updates"
            else -> null
        }
    }

    companion object {
        const val EXTRA_LAUNCH_ROUTE = "com.unistack.app.extra.LAUNCH_ROUTE"
    }
}
