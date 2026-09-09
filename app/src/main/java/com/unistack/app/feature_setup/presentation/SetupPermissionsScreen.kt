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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.components.UniStackBrandMark
import com.unistack.app.core.design.components.UniStackBrandPill
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.Surface
import androidx.compose.material.icons.rounded.Check
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.unistack.app.core.utils.performSafely
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
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
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    var exampleKind by rememberSaveable { mutableStateOf(PermissionExampleKind.CLASS) }
    val haptics = LocalHapticFeedback.current
    SetupScaffold(
        step = step,
        totalSteps = totalSteps,
        modifier = modifier,
        actions = {
            when {
                granted -> UniStackButton(
                    text = stringResource(R.string.setup_btn_continue),
                    onClick = onContinueClick,
                    trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
                )
                mustUseSettings -> {
                    UniStackButton(
                        text = stringResource(R.string.setup_perm_btn_open_settings),
                        onClick = { context.openAppSettings() }
                    )
                    UniStackButton(
                        text = stringResource(R.string.setup_perm_btn_continue_without),
                        onClick = onContinueClick,
                        variant = UniStackButtonVariant.Outlined
                    )
                }
                else -> {
                    UniStackButton(
                        text = stringResource(R.string.setup_perm_btn_enable),
                        onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                        leadingIcon = Icons.Rounded.NotificationsActive
                    )
                    UniStackButton(
                        text = stringResource(R.string.setup_perm_btn_not_now),
                        onClick = onContinueClick,
                        variant = UniStackButtonVariant.Outlined
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            /*
             * Se enseña, no se promete.
             *
             * Aquí había tres filas de texto diciendo de qué avisaría la app. Decirlo no
             * cuesta nada y por eso no convence a nadie: lo que despeja la duda es ver el
             * aviso donde va a salir. Y el ejemplo lo elige el usuario con los tres chips,
             * que es la única forma honesta de ser concreto en este punto del flujo: aquí
             * todavía no hay ni una clase creada, así que cualquier hora sería inventada.
             * Por eso la maqueta va marcada como ejemplo.
             */
            val isEn = java.util.Locale.getDefault().language == "en"
            Text(
                text = buildAnnotatedString {
                    if (isEn) {
                        append("Should I notify you\n")
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("of upcoming events?")
                        }
                    } else {
                        append("¿Te aviso de\n")
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("lo que se aproxima?")
                        }
                    }
                },
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMediumEmphasized
            )
            Text(
                text = stringResource(R.string.setup_perm_desc),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            PermissionLockPreview(kind = exampleKind)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PermissionExampleKind.entries.forEach { kind ->
                    val selected = kind == exampleKind
                    Surface(
                        // El mismo tacto que el resto de selectores de la app.
                        onClick = {
                            if (!selected) haptics.performSafely(HapticFeedbackType.SegmentTick)
                            exampleKind = kind
                        },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = CircleShape,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = kind.chip,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // «Un par al dia como mucho» dejo de ser verdad al quitar el tope por tipo:
                // ahora avisa de todo, y quien decide cuanto es el usuario en Ajustes.
                PermissionPerkRow(stringResource(R.string.setup_perm_perk_classes))
                PermissionPerkRow(stringResource(R.string.setup_perm_offline_info))
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
                            text = stringResource(R.string.setup_perm_settings_hint),
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



/** Los tres avisos que la app sabe dar, con la pinta que tienen en la pantalla de bloqueo. */
private enum class PermissionExampleKind(
    private val spanishChip: String,
    val clock: String,
    private val spanishAgo: String,
    private val spanishTitle: String,
    private val spanishBody: String
) {
    CLASS("Clases", "9:41", "ahora", "Cálculo III empieza en 15 minutos", "Aula 302 · hasta las 11:40"),
    TASK("Entregas", "8:00", "8:00", "Hoy vence el ensayo de Ética", "Antes de las 18:00 · te quedan 10 h"),
    GRADE("Notas", "19:20", "19:20", "Tu promedio de Redes subió a 4.1", "Con el quiz que acabas de registrar");

    val chip: String
        get() = if (java.util.Locale.getDefault().language == "en") {
            when (this) {
                CLASS -> "Classes"
                TASK -> "Deadlines"
                GRADE -> "Grades"
            }
        } else spanishChip

    val ago: String
        get() = if (java.util.Locale.getDefault().language == "en") {
            when (this) {
                CLASS -> "now"
                else -> spanishAgo
            }
        } else spanishAgo

    val title: String
        get() = if (java.util.Locale.getDefault().language == "en") {
            when (this) {
                CLASS -> "Calculus III starts in 15 minutes"
                TASK -> "Ethics essay due today"
                GRADE -> "Your Networks GPA increased to 4.1"
            }
        } else spanishTitle

    val body: String
        get() = if (java.util.Locale.getDefault().language == "en") {
            when (this) {
                CLASS -> "Room 302 · until 11:40"
                TASK -> "Before 18:00 · 10 h remaining"
                GRADE -> "From the quiz you just recorded"
            }
        } else spanishBody
}

@Composable
private fun PermissionLockPreview(kind: PermissionExampleKind) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = stringResource(R.string.setup_perm_example_header),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = SectionLabelStyle.copy(fontSize = 9.sp, lineHeight = 12.sp)
                            )
                        }
                    }
                    Text(
                        text = kind.clock,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 46.sp,
                        lineHeight = 52.sp,
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        text = if (java.util.Locale.getDefault().language == "en") "Thursday, August 20" else "jueves, 20 de agosto",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 0.dp
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // La marca de verdad, con sus degradados. La silueta blanca
                                // sobre un cuadrado del acento no es el logo: los colores
                                // son justo lo que lo identifica.
                                Box(
                                    modifier = Modifier.size(
                                        width = 16.dp,
                                        height = 16.dp * UniStackBrandMark.HeightRatio
                                    )
                                ) {
                                    UniStackBrandMark.Pills.forEach { pill ->
                                        UniStackBrandPill(pill = pill, markWidth = 16.dp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "UniStack",
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = kind.ago,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = kind.title,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = kind.body,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionPerkRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(11.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun android.content.Context.openAppSettings() {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    startActivity(intent)
}
