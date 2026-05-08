package com.unistack.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyEdgeToEdge(darkTheme = false)
        setContent {
            UniStackApp(
                onDarkThemeChanged = { darkTheme ->
                    applyEdgeToEdge(darkTheme = darkTheme)
                }
            )
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
    }
}
