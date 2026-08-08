package com.unistack.app

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
