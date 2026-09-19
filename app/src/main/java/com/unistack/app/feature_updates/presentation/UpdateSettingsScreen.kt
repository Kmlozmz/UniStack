@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_updates.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.R
import com.unistack.app.core.design.components.SystemProgress
import com.unistack.app.core.design.components.UniLoadingIndicator
import com.unistack.app.core.design.components.UniSwitch
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.notifications.UpdateDeepLink
import com.unistack.app.feature_updates.domain.CheckInterval
import com.unistack.app.feature_updates.domain.UpdateInfo
import com.unistack.app.feature_updates.domain.UpdateSettings
import com.unistack.app.feature_updates.domain.UpdateState
import kotlinx.coroutines.delay
import java.io.File

/**
 * Actualizaciones, como la eligió el 19 sep 2026 (artifact «Flujo de actualizaciones», F).
 *
 * De arriba abajo: la tarjeta de estado —que se toca para volver a comprobar—, la ficha de la
 * versión nueva con sus notas agrupadas, y la que llevas puesta. La acción va anclada abajo,
 * porque es una sola y siempre la misma.
 *
 * **Ya no hay botón de recargar.** Se comprueba tirando de la lista o tocando la tarjeta de
 * estado, con el indicador de carga de Movimiento. En su sitio va el menú ⋮ con lo que se toca
 * pocas veces: descargar sola con Wi-Fi, avisar al estar lista, cada cuánto mirar, limpiar
 * descargas y el historial de versiones.
 */
@Composable
fun UpdateSettingsScreen(
    onBackClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UpdateViewModel = hiltViewModel()
) {
    BackHandler(onBack = onBackClick)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val releases by viewModel.releases.collectAsStateWithLifecycle()
    val pendingApks by viewModel.pendingApks.collectAsStateWithLifecycle()
    val lastCheckedAt by viewModel.lastCheckedAt.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val installRequested by UpdateDeepLink.installRequested.collectAsStateWithLifecycle()
    val installed by viewModel.installedVersion.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshPendingApks()
        if (state is UpdateState.Idle) viewModel.checkForUpdates()
    }
    // El «Instalar» del aviso: se recoge en cuanto la descarga esté lista.
    LaunchedEffect(installRequested, state) {
        if (installRequested && state is UpdateState.ReadyToInstall) {
            UpdateDeepLink.consume()
            viewModel.installUpdate()
        }
    }

    var menuOpen by remember { mutableStateOf(false) }
    val checking = state is UpdateState.Checking || state is UpdateState.Idle
    val pending = (state as? UpdateState.Available)?.info
        ?: (state as? UpdateState.Downloading)?.info
        ?: (state as? UpdateState.ReadyToInstall)?.info

    AEscalaDelArtifact {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BarraDeLaReplica(
            title = stringResource(R.string.updates_title),
            subtitle = stringResource(R.string.updates_subtitle),
            onBackClick = onBackClick
        ) {
            Box {
                BotonCircular(
                    icon = Icons.Rounded.MoreVert,
                    contentDescription = stringResource(R.string.updates_menu),
                    onClick = { menuOpen = true }
                )
                MenuDeActualizaciones(
                    expanded = menuOpen,
                    settings = settings,
                    pendingApks = pendingApks,
                    onDismiss = { menuOpen = false },
                    onSettingsChange = viewModel::updateSettings,
                    onHistoryClick = {
                        menuOpen = false
                        onHistoryClick()
                    },
                    onCleanClick = {
                        menuOpen = false
                        viewModel.clearDownload()
                    }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val listState = rememberLazyListState()
            val pullState = rememberPullToRefreshState()
            /*
             * Tirar solo cuenta si la lista **ya estaba arriba** cuando empezó el gesto. Sin
             * esto, un desplazamiento hacia arriba desde media lista llegaba al tope y, en
             * el mismo gesto, se convertía en una comprobación que nadie había pedido.
             */
            var pullAllowed by remember { mutableStateOf(true) }
            LaunchedEffect(listState) {
                listState.interactionSource.interactions.collect { interaction ->
                    if (interaction is DragInteraction.Start) {
                        pullAllowed = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .pullToRefresh(
                        isRefreshing = checking,
                        state = pullState,
                        enabled = pullAllowed,
                        onRefresh = viewModel::checkForUpdates
                    ),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 4.dp,
                    bottom = scrollBottomRoom + 90.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item("tirar") {
                    val fraction = if (checking) 1f else pullState.distanceFraction.coerceIn(0f, 1f)
                    if (fraction > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp * fraction),
                            contentAlignment = Alignment.Center
                        ) {
                            UniLoadingIndicator(
                                modifier = Modifier
                                    .size(34.dp)
                                    .graphicsLayer {
                                        scaleX = fraction
                                        scaleY = fraction
                                        alpha = fraction
                                    }
                            )
                        }
                    }
                }

                item("estado") {
                    TarjetaDeEstado(
                        state = state,
                        lastCheckedAt = lastCheckedAt,
                        onClick = viewModel::checkForUpdates
                    )
                }

                val downloading = state as? UpdateState.Downloading
                if (downloading != null) {
                    item("progreso") { TarjetaDeProgreso(downloading) }
                }

                if (pending != null) {
                    item("ficha") {
                        val (sello, tono) = when (state) {
                            is UpdateState.ReadyToInstall -> stringResource(R.string.updates_chip_downloaded) to Tono.BIEN
                            is UpdateState.Downloading -> stringResource(R.string.updates_chip_downloading) to Tono.ACENTO
                            else -> stringResource(R.string.updates_chip_new) to Tono.ACENTO
                        }
                        TarjetaDeVersion(
                            versionName = pending.versionName,
                            meta = "${fechaCorta(pending.releaseDate)} · ${pesoEnMb(pending.sizeMb)}",
                            sello = sello,
                            tonoSello = tono,
                            tonoLosa = Tono.SUAVE
                        )
                    }
                    if (pending.releaseNotes.isNotBlank()) {
                        item("notas") { NotasAgrupadas(markdown = pending.releaseNotes) }
                    }
                }

                item("instalada") {
                    val ahead = state is UpdateState.Ahead
                    TarjetaDeVersion(
                        versionName = installed,
                        meta = metaDeLaInstalada(installed, releases),
                        sello = stringResource(if (ahead) R.string.updates_chip_ahead else R.string.updates_chip_installed),
                        tonoSello = Tono.BIEN,
                        tonoLosa = Tono.SUAVE
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
    }
}

/**
 * La tarjeta de estado: el color y el título lo dicen antes de leer nada.
 *
 * Las líneas de apoyo se fueron el 19 sep 2026 por redundantes con el título o con la ficha;
 * quedan dos: la del tester («vas por delante») y la del fallo. Debajo, la pista de que se
 * toca para volver a comprobar, con cuándo fue la última vez.
 */
@Composable
private fun TarjetaDeEstado(
    state: UpdateState,
    lastCheckedAt: Long,
    onClick: () -> Unit
) {
    val sections = LocalSectionColors.current
    val checking = state is UpdateState.Checking || state is UpdateState.Idle
    val (icon, tono) = when (state) {
        is UpdateState.Available -> Icons.Rounded.NewReleases to Tono.ACENTO
        is UpdateState.ReadyToInstall -> Icons.Rounded.SystemUpdate to Tono.ACENTO
        is UpdateState.Downloading -> Icons.Rounded.Download to Tono.INFO
        UpdateState.UpToDate, is UpdateState.Ahead, UpdateState.NoReleases -> Icons.Rounded.CheckCircle to Tono.BIEN
        is UpdateState.Error -> Icons.Rounded.CloudOff to Tono.MAL
        else -> Icons.Rounded.Refresh to Tono.SUAVE
    }
    val title = stringResource(
        when (state) {
            is UpdateState.Available -> R.string.updates_version_available
            is UpdateState.ReadyToInstall -> R.string.updates_ready_to_install
            is UpdateState.Downloading -> R.string.updates_downloading
            UpdateState.UpToDate -> R.string.updates_up_to_date
            is UpdateState.Ahead -> R.string.updates_ahead
            UpdateState.NoReleases -> R.string.updates_nothing_to_install
            is UpdateState.Error -> R.string.updates_could_not_check
            else -> R.string.updates_checking
        }
    )
    val support = when (state) {
        is UpdateState.Ahead -> stringResource(R.string.updates_ahead_desc)
        UpdateState.NoReleases -> stringResource(R.string.updates_nothing_to_install_desc)
        is UpdateState.Error -> stringResource(R.string.updates_could_not_check_desc)
        else -> null
    }
    val tinted = state == UpdateState.UpToDate || state is UpdateState.Ahead || state == UpdateState.NoReleases
    val container = if (tinted) sections.onTrackContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val content = if (tinted) sections.onOnTrackContainer else MaterialTheme.colorScheme.onSurface
    val muted = if (tinted) content.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
    val withHint = !checking && state !is UpdateState.Downloading && state !is UpdateState.Error

    // «hace 2 min» se queda atrás si no se recalcula: el minuto se mueve aunque nada cambie.
    val ahora by produceState(initialValue = System.currentTimeMillis(), key1 = lastCheckedAt) {
        while (true) {
            delay(30_000)
            value = System.currentTimeMillis()
        }
    }
    val hace = haceCuanto(lastCheckedAt, ahora)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = container,
        contentColor = content,
        onClick = onClick,
        enabled = !checking && state !is UpdateState.Downloading
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Losa(icon = icon, tono = tono)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = tituloDeTarjeta(20), color = content)
                if (support != null) {
                    Text(text = support, style = ApoyoDeTarjeta, color = muted)
                }
                if (withHint) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Refresh,
                            contentDescription = null,
                            tint = muted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (hace.isBlank()) {
                                stringResource(R.string.updates_hint_recheck)
                            } else {
                                "${stringResource(R.string.updates_hint_recheck)} · $hace"
                            },
                            style = PistaDeTarjeta,
                            color = muted
                        )
                    }
                }
            }
        }
    }
}

/** «v1.0.1 · 24,1 MB» a la izquierda, «45 % · 6 s» a la derecha, y la barra ondulada debajo. */
@Composable
private fun TarjetaDeProgreso(downloading: UpdateState.Downloading) {
    val unknown = downloading.progress == UpdateState.UNKNOWN_PROGRESS
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "v${downloading.info.versionName} · ${pesoEnMb(downloading.info.sizeMb)}",
                    style = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!unknown) {
                    val eta = downloading.secondsLeft
                    Text(
                        text = if (eta != null) {
                            stringResource(R.string.updates_progress_eta, downloading.progress, eta)
                        } else {
                            stringResource(R.string.updates_progress_percent, downloading.progress)
                        },
                        style = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            SystemProgress(percent = downloading.progress.takeIf { !unknown })
        }
    }
}

/**
 * Lo de debajo de «v1.0.0»: la fecha y el peso.
 *
 * Si la versión instalada está entre las publicadas, salen de ahí; si no —una alpha del bot,
 * una compilación de trabajo—, la fecha es la de instalación y el peso el del APK puesto.
 */
@Composable
private fun metaDeLaInstalada(installed: String, releases: List<UpdateInfo>): String {
    val release = releases.firstOrNull { it.versionName == installed }
    if (release != null) return "${fechaCorta(release.releaseDate)} · ${pesoEnMb(release.sizeMb)}"
    val context = LocalContext.current
    val info = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0) }.getOrNull()
    }
    val bytes = remember(context) { runCatching { File(context.applicationInfo.sourceDir).length() }.getOrDefault(0L) }
    val fecha = info?.lastUpdateTime?.let { fechaCorta(it) }.orEmpty()
    val peso = if (bytes > 0L) " · ${pesoEnMb(bytes / 1024.0 / 1024.0)}" else ""
    return fecha + peso
}

/**
 * El menú ⋮, a lo M3E (19 sep 2026, segunda vuelta): 300 de ancho, esquinas de 28, filas de
 * 56 con el icono, dos líneas y el mando a la derecha, y un separador que se vea antes del
 * historial. Los interruptores van dentro porque así venía su captura.
 */
@Composable
private fun MenuDeActualizaciones(
    expanded: Boolean,
    settings: UpdateSettings,
    pendingApks: Int,
    onDismiss: () -> Unit,
    onSettingsChange: ((UpdateSettings) -> UpdateSettings) -> Unit,
    onHistoryClick: () -> Unit,
    onCleanClick: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 8.dp,
        modifier = Modifier.width(300.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            FilaDeMenu(
                icon = Icons.Rounded.Wifi,
                title = stringResource(R.string.updates_menu_wifi_title),
                subtitle = stringResource(R.string.updates_menu_wifi_desc),
                onClick = { onSettingsChange { it.copy(autoDownloadOnWifi = !it.autoDownloadOnWifi) } }
            ) {
                InterruptorDeMenu(settings.autoDownloadOnWifi) { on -> onSettingsChange { it.copy(autoDownloadOnWifi = on) } }
            }
            FilaDeMenu(
                icon = Icons.Rounded.Notifications,
                title = stringResource(R.string.updates_menu_notify_title),
                subtitle = stringResource(R.string.updates_menu_notify_desc),
                onClick = { onSettingsChange { it.copy(notifyWhenReady = !it.notifyWhenReady) } }
            ) {
                InterruptorDeMenu(settings.notifyWhenReady) { on -> onSettingsChange { it.copy(notifyWhenReady = on) } }
            }
            FilaDeMenu(
                icon = Icons.Rounded.Schedule,
                title = stringResource(R.string.updates_menu_check_title),
                subtitle = stringResource(
                    when (settings.checkInterval) {
                        CheckInterval.EVERY_2H -> R.string.updates_menu_check_2h
                        CheckInterval.DAILY -> R.string.updates_menu_check_daily
                        CheckInterval.MANUAL -> R.string.updates_menu_check_manual
                    }
                ),
                onClick = { onSettingsChange { it.copy(checkInterval = it.checkInterval.next()) } }
            ) {
                Sello(
                    text = stringResource(
                        when (settings.checkInterval) {
                            CheckInterval.EVERY_2H -> R.string.updates_interval_2h
                            CheckInterval.DAILY -> R.string.updates_interval_daily
                            CheckInterval.MANUAL -> R.string.updates_interval_manual
                        }
                    ),
                    tono = Tono.SUAVE
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
            FilaDeMenu(
                icon = Icons.Rounded.History,
                title = stringResource(R.string.updates_menu_history),
                subtitle = null,
                onClick = onHistoryClick
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            if (pendingApks > 0) {
                FilaDeMenu(
                    icon = Icons.Rounded.DeleteOutline,
                    title = stringResource(R.string.updates_menu_clean_title),
                    subtitle = if (pendingApks > 1) {
                        stringResource(R.string.updates_apks_space, pendingApks)
                    } else {
                        stringResource(R.string.updates_one_apk_space)
                    },
                    onClick = onCleanClick
                ) {}
            }
        }
    }
}

@Composable
private fun FilaDeMenu(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(fontSize = 14.sp, lineHeight = 17.sp, fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = TextStyle(fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        trailing()
    }
}

/** El interruptor de la app, un poco encogido para la fila del menú (44 × 24 en la réplica). */
@Composable
private fun InterruptorDeMenu(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Box(modifier = Modifier.width(46.dp).height(28.dp), contentAlignment = Alignment.Center) {
        UniSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.85f)
        )
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
    val dlText = stringResource(R.string.updates_btn_download)
    val installText = stringResource(R.string.updates_btn_install)
    val allowInstallText = stringResource(R.string.updates_btn_allow_install)
    val recheckText = stringResource(R.string.updates_check_again)
    val action: Triple<String, ImageVector, () -> Unit>? = when {
        state is UpdateState.Available -> Triple(dlText, Icons.Rounded.Download, onDownload)
        state is UpdateState.ReadyToInstall && canInstall -> Triple(installText, Icons.Rounded.SystemUpdate, onInstall)
        state is UpdateState.ReadyToInstall -> Triple(allowInstallText, Icons.Rounded.SystemUpdate, onAllowInstall)
        state is UpdateState.Error -> Triple(recheckText, Icons.Rounded.Refresh, onCheck)
        else -> null
    }
    if (action == null) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        BotonDeLaReplica(
            text = action.first,
            onClick = action.third,
            icon = action.second,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 18.dp)
        )
    }
}
