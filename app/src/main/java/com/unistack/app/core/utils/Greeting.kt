package com.unistack.app.core.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import java.time.LocalTime

/**
 * El saludo, con el cielo que le toque a esa hora y localizado según el idioma del usuario.
 */
@StringRes
fun greetingResForNow(): Int {
    val hour = LocalTime.now().hour
    return when {
        hour < 6 -> R.string.greeting_night
        hour < 12 -> R.string.greeting_morning
        hour < 20 -> R.string.greeting_afternoon
        else -> R.string.greeting_night
    }
}

@Composable
fun greetingForNow(): String = stringResource(greetingResForNow())
