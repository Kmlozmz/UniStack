@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import coil.compose.AsyncImage
import com.unistack.app.feature_user.domain.AppUser
import com.unistack.app.feature_user.domain.AuthProvider
import com.unistack.app.feature_user.domain.UserProfile
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi

/*
 * Lo que queda del antiguo ProfileScreen.
 *
 * Aquel fichero era una sola pantalla con un interruptor de cinco modos —perfil, académico,
 * módulos, notificaciones y datos— y un bloque de estado compartido del que cada modo usaba
 * un trozo: mil cuatrocientas líneas donde tocar el reparto de cortes obligaba a leer también
 * la lógica del permiso de notificaciones. Cada modo es ahora su propia pantalla.
 *
 * Aquí solo sobreviven las piezas que de verdad comparten varias: el retrato, cómo se nombra
 * el perfil y la cuenta, y las tres preguntas al sistema sobre el permiso de avisos.
 */

@Composable
internal fun AccountAvatar(
    photoUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    initial: String? = null
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                SolidColor(MaterialTheme.colorScheme.primaryContainer)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl.isNullOrBlank()) {
            // Sin foto, la inicial antes que el monigote: dice de quién es la ficha, y el
            // icono genérico es el mismo para todo el mundo.
            if (initial.isNullOrBlank()) {
                Icon(Icons.Rounded.Person, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            } else {
                Text(
                    text = initial,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        } else {
            AsyncImage(
                model = photoUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * La linea bajo el nombre en el perfil.
 *
 * Antes empezaba por el nivel de estudios —«Universidad · Ingenieria»—, que dejo de existir
 * cuando la app se centro en educacion superior: repetirle a todo el mundo la unica opcion que
 * queda no informa de nada. Ahora manda la carrera, y la institucion la acompana cuando esta.
 */
internal fun UserProfile.educationSummary(): String {
    val programa = careerOrProgram?.takeIf { it.isNotBlank() }
    val centro = institutionName?.takeIf { it.isNotBlank() }
    return when {
        programa != null && centro != null -> "$programa · $centro"
        programa != null -> programa
        centro != null -> centro
        else -> "Estudiante"
    }
}

internal fun AppUser.accountLabel(): String {
    return when (authProvider) {
        AuthProvider.LOCAL -> "Cuenta local"
        AuthProvider.GOOGLE -> "Google conectado"
    }
}

/**
 * Si el sistema deja que la app avise.
 *
 * Antes solo miraba el permiso de Android 13, así que en un teléfono más antiguo respondía
 * siempre que sí —aunque el usuario hubiera apagado las notificaciones de la app en ajustes—.
 * `areNotificationsEnabled` responde lo que de verdad importa en todas las versiones.
 */
internal fun Context.hasNotificationPermission(): Boolean =
    NotificationManagerCompat.from(this).areNotificationsEnabled()

/**
 * Si todavía tiene sentido pedirle el permiso al sistema.
 *
 * Android deja de enseñar el diálogo cuando ya se ha denegado, y `launch` vuelve al instante
 * con un «no» sin que se vea nada: el botón parecía roto. Cuando se llega a ese punto, el único
 * camino son los ajustes del sistema.
 */
internal fun Context.canAskForNotificationPermission(alreadyAsked: Boolean): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
    if (!alreadyAsked) return true
    val activity = this as? android.app.Activity ?: return false
    return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
}

/** Abre los ajustes de notificaciones de la app, con el detalle de la app como respaldo. */
internal fun Context.openNotificationSettings() {
    val direct = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (runCatching { startActivity(direct) }.isSuccess) return
    val fallback = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { startActivity(fallback) }
}
