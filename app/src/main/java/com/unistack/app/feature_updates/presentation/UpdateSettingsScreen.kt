package com.unistack.app.feature_updates.presentation

import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.feature_updates.domain.UpdateChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.unistack.app.core.design.theme.scrollBottomRoom

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniStackLoadingIndicator
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import com.unistack.app.feature_updates.domain.UpdateState

@Composable
fun UpdateSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UpdateViewModel = hiltViewModel()
) {
    BackHandler(onBack = onBackClick)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val channel by viewModel.channel.collectAsStateWithLifecycle()
    val unlockedChannels by viewModel.unlockedChannels.collectAsStateWithLifecycle()
    var codeChannel by remember { mutableStateOf<UpdateChannel?>(null) }
    var codeInput by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf<String?>(null) }
    var checkingCode by remember { mutableStateOf(false) }
    val spacing = LocalInterfaceSpacing.current
    var showSheet by remember { mutableStateOf(false) }
    var confirmClearDownload by remember { mutableStateOf(false) }
    var pendingChannel by remember { mutableStateOf<UpdateChannel?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val pendingApks by viewModel.pendingApks.collectAsStateWithLifecycle()
    val motionScale = LocalMotionDurationScale.current
    /*
     * La tarjeta de limpieza sigue en la lista mientras cae.
     *
     * Si se quitara en el momento de borrar no habría nada que animar: desaparecería de golpe,
     * que es justo lo que no se quería. Y el número se recuerda porque durante la caída ya
     * vale cero, y la tarjeta no puede ponerse a decir «0 APK» mientras se va.
     */
    var cleanupVisible by remember { mutableStateOf(false) }
    var cleanupCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(pendingApks) {
        if (pendingApks > 0) {
            cleanupCount = pendingApks
            cleanupVisible = true
        } else if (cleanupVisible) {
            delay(trashMillis(motionScale).toLong())
            cleanupVisible = false
        }
    }
    // Se puede haber descargado algo desde la otra pantalla mientras esta no estaba montada.
    LaunchedEffect(Unit) { viewModel.refreshPendingApks() }

    LaunchedEffect(state) {
        showSheet = state is UpdateState.Available ||
            state is UpdateState.Downloading ||
            state is UpdateState.ReadyToInstall
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = 8.dp,
            bottom = scrollBottomRoom
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            UpdateSettingsHeader(onBackClick = onBackClick)
        }
        item {
            UpdateCheckCard(
                state = state,
                onCheckClick = viewModel::checkForUpdates
            )
        }
        item {
            UpdateChannelCard(
                selected = channel,
                unlocked = unlockedChannels,
                onRequestCode = { canal ->
                    codeInput = ""
                    codeError = null
                    codeChannel = canal
                },
                onSelect = { elegido ->
                    // Bajar a estable no necesita aviso: es el canal seguro. Y el canal de la
                    // versión instalada tampoco: lo que ese aviso pide aceptar —recibir
                    // versiones a medio hacer— es justo lo que ya se tiene puesto.
                    val propio = UpdateChannel.ofInstalled(viewModel.currentVersionName)
                    if (elegido == UpdateChannel.STABLE || elegido == propio) {
                        viewModel.setChannel(elegido)
                    } else {
                        pendingChannel = elegido
                    }
                }
            )
        }
        item {
            UniCard(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UpdateIconTile(icon = Icons.Rounded.Info)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Versión ${viewModel.currentVersionName}",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Build ${viewModel.currentVersionCode}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        if (cleanupVisible) {
            item(key = "limpieza") {
                CleanupCard(
                    visible = pendingApks > 0,
                    apkCount = cleanupCount,
                    onClick = { confirmClearDownload = true }
                )
            }
        }
    }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = scrollBottomRoom)
        )
    }

    if (confirmClearDownload) {
        AlertDialog(
            onDismissRequest = { confirmClearDownload = false },
            title = {
                Text(
                    if (cleanupCount > 1) "¿Eliminar los APK descargados?" else "¿Eliminar el APK descargado?",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Libera espacio. Si luego quieres instalar esa versión, habrá que descargarla otra vez.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClearDownload = false
                        val borrados = cleanupCount
                        viewModel.clearDownload()
                        // Borrar un archivo no se ve por ninguna parte, y la tarjeta que cae
                        // dice que algo se fue pero no cuánto. El aviso lo remata.
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (borrados > 1) "$borrados APK eliminados" else "APK eliminado"
                            )
                        }
                    }
                ) { Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearDownload = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    }

    pendingChannel?.let { elegido ->
        AlertDialog(
            onDismissRequest = { pendingChannel = null },
            title = { Text("Recibir versiones ${elegido.label.lowercase()}", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Text(
                    buildString {
                        append(elegido.description)
                        appendLine()
                        appendLine()
                        append(
                            if (elegido == UpdateChannel.ALPHA) {
                                "Una alpha puede tener funciones a medias, fallar al abrir o " +
                                    "corromper lo que tengas guardado. Haz una copia de seguridad antes."
                            } else {
                                "Una beta está casi lista, pero todavía puede traer fallos. " +
                                    "Conviene tener una copia de seguridad al día."
                            }
                        )
                        appendLine()
                        appendLine()
                        append(
                            "Y no se puede volver atrás sin desinstalar: una versión anterior no " +
                                "se instala encima de una posterior, y desinstalar borra tus datos."
                        )
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setChannel(elegido)
                        pendingChannel = null
                    }
                ) { Text("Entiendo, activar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { pendingChannel = null }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    }

    codeChannel?.let { canal ->
        AlertDialog(
            onDismissRequest = { if (!checkingCode) codeChannel = null },
            title = { Text("Acceso a ${canal.label}", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Este canal se abre con su propio código. Si no tienes uno, pídeselo a " +
                            "quien publica la app.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = {
                            codeInput = it.take(40)
                            codeError = null
                        },
                        label = { Text("Código") },
                        singleLine = true,
                        enabled = !checkingCode,
                        isError = codeError != null,
                        shape = AppShapes.Small,
                        modifier = Modifier.fillMaxWidth()
                    )
                    codeError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = codeInput.isNotBlank() && !checkingCode,
                    onClick = {
                        checkingCode = true
                        codeError = null
                        scope.launch {
                            val granted = viewModel.redeemAccessCode(codeInput, canal)
                            checkingCode = false
                            if (granted == null) {
                                // Mismo mensaje si el código no existe, si no se pudo comprobar
                                // o si es el de otro canal: decir «ese es el de alpha» sería
                                // contar de quién es un código que alguien está probando.
                                codeError = "Ese código no vale para ${canal.label}."
                            } else {
                                codeChannel = null
                                snackbarHostState.showSnackbar("Canal ${granted.label} disponible")
                            }
                        }
                    }
                ) { Text(if (checkingCode) "Comprobando…" else "Acceder", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(enabled = !checkingCode, onClick = { codeChannel = null }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    }

    if (showSheet) {
        val info = when (val current = state) {
            is UpdateState.Available -> current.info
            is UpdateState.Downloading -> current.info
            is UpdateState.ReadyToInstall -> current.info
            else -> null
        }
        if (info != null) {
            UpdateDetailSheet(
                info = info,
                state = state,
                onDownloadClick = viewModel::downloadUpdate,
                onInstallClick = viewModel::installUpdate,
                canInstall = viewModel.canInstallPackages(),
                onDismiss = {
                    showSheet = false
                    viewModel.dismiss()
                }
            )
        }
    }
}

/**
 * Hasta dónde se aceptan actualizaciones.
 *
 * No es un candado: los APK están en un repositorio público y quien quiera puede descargar el
 * que le apetezca. Decide qué te ofrece la app, para que nadie acabe en una alpha sin pedirlo.
 */
@Composable
private fun UpdateChannelCard(
    selected: UpdateChannel,
    unlocked: Set<UpdateChannel>,
    onRequestCode: (UpdateChannel) -> Unit,
    onSelect: (UpdateChannel) -> Unit
) {
    UniCard(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UpdateIconTile(icon = Icons.Rounded.Layers)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Qué versiones recibes",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        selected.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UpdateChannel.entries.forEach { option ->
                    val isSelected = option == selected
                    // Los canales por encima de lo desbloqueado se ven, pero piden el
                    // codigo. Ocultarlos dejaria sin explicar por que no estan.
                    val isLocked = option !in unlocked
                    Surface(
                        onClick = { if (isLocked) onRequestCode(option) else onSelect(option) },
                        modifier = Modifier.weight(1f),
                        shape = AppShapes.Pill,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isLocked) {
                                Icon(
                                    Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(
                                option.label,
                                textAlign = TextAlign.Center,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateSettingsHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.width(4.dp))
        Column {
            Text(
                "Actualizaciones de UniStack",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Mantén UniStack al día",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UpdateCheckCard(
    state: UpdateState,
    onCheckClick: () -> Unit
) {
    val isChecking = state is UpdateState.Checking
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        onClick = if (isChecking) null else onCheckClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.13f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isChecking) {
                    UniStackLoadingIndicator(
                        size = 20.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Verificar sistema",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = state.statusLabel(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                // Ir por delante no se arregla instalando: Android no pone una versión encima
                // de otra posterior. Decirlo aquí evita la búsqueda del botón que no existe.
                if (state is UpdateState.Ahead) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Para volver a esa habría que desinstalar la app, y eso borra tus datos.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            // También con el canal vacío: no hay nada que instalar, que es lo que el visto
            // significa. Dejarlo sin él hacía que un estado correcto pareciera a medias.
            if (state is UpdateState.UpToDate || state is UpdateState.NoReleases) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun UpdateIconTile(
    icon: ImageVector,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(accent.copy(alpha = 0.13f), AppShapes.Small),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = accent)
    }
}

private fun UpdateState.statusLabel(): String = when (this) {
    UpdateState.Idle -> "Toca para buscar actualizaciones"
    UpdateState.Checking -> "Verificando..."
    is UpdateState.Available -> "Nueva versión disponible: v${info.versionName}"
    is UpdateState.Downloading -> "Descargando v${info.versionName}... ${progress}%"
    is UpdateState.ReadyToInstall -> "Lista para instalar: v${info.versionName}"
    UpdateState.UpToDate -> "Al día"
    is UpdateState.Ahead -> "Vas por delante: lo último de este canal es la v${info.versionName}"
    UpdateState.NoReleases -> "Estás en la última versión disponible para este canal"
    is UpdateState.Error -> message
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

/** Lo que dura la salida entera, para no quitar la tarjeta de la lista antes de tiempo. */
private fun trashMillis(motionScale: Float): Int =
    ((TOSS_MILLIS + COLLAPSE_MILLIS) * motionScale).roundToInt().coerceAtLeast(1)
