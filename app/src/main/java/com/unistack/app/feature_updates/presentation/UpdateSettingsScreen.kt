@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_updates.presentation

import com.unistack.app.core.design.theme.contentColorOn
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.components.SettingsHeader
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.UniStackButtonDefaults
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_updates.domain.ReleaseVersion
import com.unistack.app.feature_updates.domain.UpdateInfo
import com.unistack.app.feature_updates.domain.UpdateState
import kotlin.math.roundToInt
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import com.unistack.app.core.design.components.UniCard
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.ui.text.font.FontWeight

/**
 * Actualizaciones: qué hay publicado y qué trae.
 *
 * **Ya no hay canales.** La app toma la última publicación de GitHub, sea preestreno o
 * definitiva, y esta pantalla enseña las recientes con sus notas. Antes había que elegir un
 * canal, canjear un código para abrirlo y entender por qué «vas por delante» no era lo mismo
 * que «al día»; todo eso repartía las mismas descargas públicas que cualquiera podía bajar
 * desde el enlace.
 *
 * Lo que se lee de arriba abajo: en qué estado estás, qué versiones hay y qué cambió en cada
 * una. La acción va anclada abajo, porque es una sola y siempre la misma.
 */
@Composable
fun UpdateSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UpdateViewModel = hiltViewModel()
) {
    BackHandler(onBack = onBackClick)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val releases by viewModel.releases.collectAsStateWithLifecycle()
    val pendingApks by viewModel.pendingApks.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.refreshPendingApks()
        if (state is UpdateState.Idle) viewModel.checkForUpdates()
    }

    // Solo lo que aún no tienes puesto. Las que ya instalaste no son novedad aquí; para eso
    // está Novedades, que cuenta lo que trae la versión en la que estás.
    val installed = viewModel.currentVersionName
    val pending = releases.filter { ReleaseVersion.isNewer(it.versionName, installed) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                bottom = scrollBottomRoom + 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
                item("cabecera") {
                    SettingsHeader(
                        title = "Actualizaciones",
                        subtitle = "Comprueba y descarga",
                        onBackClick = onBackClick,
                        action = {
                            IconButton(onClick = viewModel::checkForUpdates) {
                                Icon(Icons.Rounded.Refresh, contentDescription = "Volver a comprobar")
                            }
                        }
                    )
                }
                item("titular") { UpdateHeadline(state = state, installed = installed) }

                if (state is UpdateState.Downloading) {
                    item("progreso") {
                        val downloading = state as UpdateState.Downloading
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (downloading.progress == UpdateState.UNKNOWN_PROGRESS) {
                                LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
                            } else {
                                LinearWavyProgressIndicator(
                                    progress = { downloading.progress / 100f },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Text(
                                text = "Descargando la v${downloading.info.versionName}…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(pending, key = { it.versionName }) { release ->
                    ReleaseEntry(release)
                }

                if (pending.isEmpty() && state !is UpdateState.Checking) {
                    item("instalada") { InstalledCard(installed, viewModel.currentVersionCode) }
                }

                item("limpieza") {
                    CleanupCard(
                        visible = pendingApks > 0,
                        apkCount = pendingApks,
                        onClick = viewModel::clearDownload
                    )
                }

                // El pie dice qué versión llevas puesta y cada cuánto se mira: sin eso, una
                // pantalla que dice «estás al día» no aclara si eso se comprobó hace un
                // minuto o hace una semana.
                item("pie") {
                    Text(
                        text = "Tienes la $installed. Se comprueba solo una vez al día.",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
        }

        UpdateActions(
            state = state,
            canInstall = viewModel.canInstallPackages(),
            onDownload = viewModel::downloadUpdate,
            onInstall = viewModel::installUpdate,
            onAllowInstall = viewModel::openInstallPermissionSettings,
            onCheck = viewModel::checkForUpdates,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * El estado, como una tarjeta con su propio color.
 *
 * Era un icono centrado y dos líneas de texto gris: los nueve estados se veían igual, y «no se
 * pudo comprobar» tenía exactamente el mismo aspecto que «estás al día». Ahora el color de la
 * tarjeta lo dice antes de leer nada.
 */
@Composable
private fun UpdateHeadline(state: UpdateState, installed: String) {
    val sections = LocalSectionColors.current
    val (icon, title, support) = when (state) {
        is UpdateState.Available -> Triple(
            Icons.Rounded.NewReleases,
            "Hay una versión disponible",
            "Tienes la v$installed. La última publicada es la v${state.info.versionName}."
        )
        is UpdateState.ReadyToInstall -> Triple(
            Icons.Rounded.Download,
            "Lista para instalar",
            "La v${state.info.versionName} ya está descargada."
        )
        is UpdateState.Downloading -> Triple(
            Icons.Rounded.Download,
            "Descargando",
            "No cierres la app hasta que termine."
        )
        UpdateState.Checking -> Triple(Icons.Rounded.Refresh, "Comprobando…", "Consultando lo último publicado.")
        UpdateState.UpToDate -> Triple(Icons.Rounded.CheckCircle, "Estás al día", "Tienes la última versión publicada.")
        is UpdateState.Ahead -> Triple(
            Icons.Rounded.CheckCircle,
            "Vas por delante",
            "Tu v$installed es más nueva que la última publicada, la v${state.info.versionName}."
        )
        UpdateState.NoReleases -> Triple(Icons.Rounded.CheckCircle, "Nada que instalar", "Todavía no hay ninguna versión publicada.")
        is UpdateState.Error -> Triple(Icons.Rounded.CloudOff, "No se pudo comprobar", state.message)
        UpdateState.Idle -> Triple(Icons.Rounded.Refresh, "Actualizaciones", "Comprueba si hay algo más nuevo.")
    }
    val tones: Pair<Color, Color> = when (state) {
        is UpdateState.Available, is UpdateState.ReadyToInstall ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        is UpdateState.Downloading ->
            sections.scheduleContainer to sections.schedule
        UpdateState.UpToDate, is UpdateState.Ahead, UpdateState.NoReleases ->
            sections.onTrackContainer to sections.onTrack
        is UpdateState.Error ->
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
        else ->
            MaterialTheme.colorScheme.surfaceContainerLow to MaterialTheme.colorScheme.onSurfaceVariant
    }
    val container = tones.first
    val accent = tones.second

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = container,
        contentColor = contentColorOn(container)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = accent,
                contentColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = support,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/** Una versión publicada: su etiqueta, su fecha y qué trae. */
@Composable
private fun ReleaseEntry(release: UpdateInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.NewReleases, contentDescription = null, modifier = Modifier.size(22.dp))
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "v${release.versionName}",
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = release.releaseDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (release.sizeMb > 0) {
                            Text(
                                text = "· ${"%.1f".format(release.sizeMb)} MB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (release.releaseNotes.isNotBlank()) {
            ReleaseNotes(markdown = release.releaseNotes, modifier = Modifier.padding(horizontal = 4.dp))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

/** Cuando no hay nada pendiente: qué versión llevas puesta. */
@Composable
private fun InstalledCard(versionName: String, versionCode: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("VERSIÓN INSTALADA", style = SectionLabelStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = "v$versionName",
                style = MaterialTheme.typography.titleLargeEmphasized,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Compilación $versionCode",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * La acción, anclada abajo.
 *
 * Es una sola y depende del estado: descargar, instalar, autorizar la instalación o volver a
 * comprobar. Antes había botones repartidos por la pantalla y no quedaba claro cuál era el que
 * hacía avanzar la cosa.
 */
@Composable
private fun UpdateActions(
    state: UpdateState,
    canInstall: Boolean,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onAllowInstall: () -> Unit,
    onCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    val action: Triple<String, ImageVector, () -> Unit>? = when {
        state is UpdateState.Available -> Triple("Descargar", Icons.Rounded.Download, onDownload)
        state is UpdateState.ReadyToInstall && canInstall -> Triple("Instalar", Icons.Rounded.Download, onInstall)
        state is UpdateState.ReadyToInstall -> Triple("Permitir instalar", Icons.Rounded.Download, onAllowInstall)
        state is UpdateState.Error -> Triple("Volver a comprobar", Icons.Rounded.Refresh, onCheck)
        else -> null
    }
    if (action == null) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Button(
            shapes = UniStackButtonDefaults.shapes,
            onClick = action.third,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .heightIn(min = UniStackButtonDefaults.PrimaryHeight)
        ) {
            Icon(action.second, contentDescription = null, modifier = Modifier.size(ButtonDefaults.MediumIconSize))
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(action.first, style = MaterialTheme.typography.titleMediumEmphasized)
        }
    }
}

/**
 * La tarjeta de limpieza, que se va como se tira algo a la basura.
 *
 * Se inclina, cae y se apaga; el hueco se cierra después, para que la lista no dé el tirón
 * antes de que la tarjeta haya terminado de irse. La duración sale del escalado de movimiento
 * del sistema: con las animaciones apagadas desaparece sin más.
 */
@Composable
private fun CleanupCard(
    visible: Boolean,
    apkCount: Int,
    onClick: () -> Unit
) {
    val motionScale = LocalMotionDurationScale.current
    val toss = (TOSS_MILLIS * motionScale).roundToInt().coerceAtLeast(1)
    val collapse = (COLLAPSE_MILLIS * motionScale).roundToInt().coerceAtLeast(1)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(toss)) + expandVertically(tween(collapse)),
        exit = shrinkVertically(
            animationSpec = tween(collapse, delayMillis = toss),
            shrinkTowards = Alignment.Top
        )
    ) {
        val gone by transition.animateFloat(
            // Acelerando, que es como cae algo que se suelta.
            transitionSpec = { tween(toss, easing = FastOutLinearInEasing) },
            label = "basura"
        ) { estado -> if (estado == EnterExitState.Visible) 0f else 1f }

        UniCard(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0.12f, 1f)
                    rotationZ = -18f * gone
                    translationY = 56.dp.toPx() * gone
                    scaleX = 1f - 0.22f * gone
                    scaleY = scaleX
                }
                .alpha(1f - gone),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            onClick = onClick
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UpdateIconTile(icon = Icons.Rounded.DeleteOutline, accent = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (apkCount > 1) "Limpiar descargas" else "Limpiar descarga",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (apkCount > 1) {
                            "$apkCount APK ocupando espacio en el móvil"
                        } else {
                            "1 APK ocupando espacio en el móvil"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/** Lo que tarda la tarjeta en caer, y lo que tarda el hueco en cerrarse detrás. */
private const val TOSS_MILLIS = 260
private const val COLLAPSE_MILLIS = 200

@Composable
private fun UpdateIconTile(
    icon: ImageVector,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(accent.copy(alpha = 0.13f), MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = accent)
    }
}
