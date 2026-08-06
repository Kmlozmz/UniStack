package com.unistack.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import dagger.hilt.android.AndroidEntryPoint

private const val RUNTIME_PERMISSION_PREFS = "unistack_runtime_permissions"
private const val NOTIFICATION_PERMISSION_ASKED = "notification_permission_asked"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val pendingLaunchRoute = mutableStateOf<String?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        markNotificationPermissionAsked()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingLaunchRoute.value = intent.launchRoute()
        applyEdgeToEdge(darkTheme = isSystemInDarkMode())
        requestNotificationPermissionOnFirstOpen()
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
        pendingLaunchRoute.value = intent.launchRoute()
    }

    private fun requestNotificationPermissionOnFirstOpen() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return

        val prefs = getSharedPreferences(RUNTIME_PERMISSION_PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(NOTIFICATION_PERMISSION_ASKED, false)) return

        prefs.edit {
            putBoolean(NOTIFICATION_PERMISSION_ASKED, true)
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun markNotificationPermissionAsked() {
        getSharedPreferences(RUNTIME_PERMISSION_PREFS, Context.MODE_PRIVATE)
            .edit {
                putBoolean(NOTIFICATION_PERMISSION_ASKED, true)
            }
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

    companion object {
        const val EXTRA_LAUNCH_ROUTE = "com.unistack.app.extra.LAUNCH_ROUTE"
    }
}
