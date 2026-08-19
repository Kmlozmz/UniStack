package com.unistack.app.feature_setup.presentation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.unistack.app.core.design.components.AnimatedCheckmark
import com.unistack.app.core.design.components.floatingOffset
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import androidx.compose.material3.MaterialTheme
import com.unistack.app.core.design.theme.LocalIsDarkTheme
/**
 * ¿Existe el permiso de notificaciones como permiso de ejecución en este dispositivo?
 *
 * Antes de Android 13 se concedía al instalar, así que el paso no tiene nada que pedir y
 * se omite del flujo (y del recuento de pasos).
 */
internal fun notificationPermissionRequired(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

internal fun hasNotificationPermission(context: android.content.Context): Boolean {
    if (!notificationPermissionRequired()) return true
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Paso de permisos.
 *
 * Existe para que el diálogo del sistema no aparezca a bocajarro nada más instalar, que es
 * lo que ocurría al pedirlo desde `MainActivity.onCreate`. Explicar antes para qué sirve
 * mejora de forma apreciable la tasa de concesión, y sobre todo permite al usuario decidir
 * con criterio.
 *
 * Nunca bloquea: se puede continuar sin conceder nada.
 */
@Composable
fun SetupPermissionsScreen(
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
    step: Int = 6,
    totalSteps: Int = 7
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var granted by remember { mutableStateOf(hasNotificationPermission(context)) }
    var askedOnce by remember { mutableStateOf(false) }

    // Si el usuario se va a Ajustes a concederlo, al volver hay que reflejarlo.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = hasNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { result ->
        granted = result
        askedOnce = true
    }

    // Tras una denegación, Android deja de mostrar el diálogo y `shouldShowRationale` pasa
    // a false. En ese punto el único camino es Ajustes, y ofrecer otra vez el botón sería
    // ofrecer algo que ya no hace nada.
    val mustUseSettings = askedOnce && !granted && activity != null &&
        !activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)

    BackHandler(onBack = onBackClick)
    SetupScaffold(
        onBackClick = onBackClick,
        step = step,
        totalSteps = totalSteps,
        modifier = modifier,
        actions = {
            when {
                granted -> UniStackButton(
                    text = "Continuar",
                    onClick = onContinueClick,
                    trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
                )
                mustUseSettings -> {
                    UniStackButton(
                        text = "Abrir ajustes",
                        onClick = { context.openAppSettings() }
                    )
                    UniStackButton(
                        text = "Continuar sin notificaciones",
                        onClick = onContinueClick,
                        variant = UniStackButtonVariant.Outlined
                    )
                }
                else -> {
                    UniStackButton(
                        text = "Activar notificaciones",
                        onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                        leadingIcon = Icons.Rounded.NotificationsActive
                    )
                    UniStackButton(
                        text = "Ahora no",
                        onClick = onContinueClick,
                        variant = UniStackButtonVariant.Outlined
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PermissionsHero(granted = granted)
            Text(
                text = buildAnnotatedString {
                    append("¿Te avisamos de\n")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append("lo importante?")
                    }
                },
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Actívalas para no perderte fechas, clases ni cambios en tu promedio.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            UniCard(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 0.dp,
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.84f),
                borderWidth = 1.dp,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Ejemplos concretos, no nombres de permisos: lo que se decide aquí es
                    // si el usuario quiere estos avisos, no si acepta POST_NOTIFICATIONS.
                    PermissionExampleRow(
                        icon = Icons.Rounded.TaskAlt,
                        title = "Entregas que vencen",
                        detail = "Antes de que se te pase la fecha."
                    )
                    PermissionExampleRow(
                        icon = Icons.Rounded.School,
                        title = "Tu próxima clase",
                        detail = "Un recordatorio antes de empezar."
                    )
                    PermissionExampleRow(
                        icon = Icons.Rounded.TrendingUp,
                        title = "Cambios en tu promedio",
                        detail = "Cuando una nota mueve tu meta."
                    )
                }
            }

            AnimatedVisibility(visible = mustUseSettings, enter = fadeIn(), exit = fadeOut()) {
                UniCard(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 0.dp,
                    borderColor = Color.Transparent,
                    borderWidth = 0.dp,
                    contentPadding = PaddingValues(horizontal = 13.dp, vertical = 11.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Android ya no volverá a preguntar. Puedes activarlas desde los ajustes del sistema.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionsHero(granted: Boolean) {
    val float = floatingOffset(travel = 5f, durationMillis = 3000, label = "permissions-hero-float")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = if (LocalIsDarkTheme.current) 0.28f else 0.16f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .graphicsLayer { translationY = float.dp.toPx() }
                .size(76.dp)
                .clip(CircleShape)
                .background(if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (granted) {
                AnimatedCheckmark(size = 34.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Icon(
                    imageVector = Icons.Rounded.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
private fun PermissionExampleRow(
    icon: ImageVector,
    title: String,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = detail,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 15.sp
            )
        }
    }
}

private fun android.content.Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    startActivity(intent)
}
