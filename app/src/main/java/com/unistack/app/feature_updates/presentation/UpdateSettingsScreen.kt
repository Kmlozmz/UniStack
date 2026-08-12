package com.unistack.app.feature_updates.presentation

import com.unistack.app.core.design.theme.AppShapes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniStackLoadingIndicator
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_updates.domain.UpdateState

@Composable
fun UpdateSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UpdateViewModel = hiltViewModel()
) {
    BackHandler(onBack = onBackClick)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        showSheet = state is UpdateState.Available ||
            state is UpdateState.Downloading ||
            state is UpdateState.ReadyToInstall
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = 8.dp,
            bottom = 36.dp
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
            UniCard(modifier = Modifier.fillMaxWidth(), color = UniStackColors.Card) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UpdateIconTile(icon = Icons.Rounded.Info)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Versión ${viewModel.currentVersionName}",
                            color = UniStackColors.TextPrimary,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Build ${viewModel.currentVersionCode}",
                            color = UniStackColors.TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        if (viewModel.hasPendingDownload) {
            item {
                UniCard(
                    modifier = Modifier.fillMaxWidth(),
                    color = UniStackColors.Card,
                    onClick = viewModel::clearDownload
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UpdateIconTile(icon = Icons.Rounded.DeleteOutline, accent = UniStackColors.Coral)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Limpiar descarga",
                                color = UniStackColors.TextPrimary,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "1 APK encontrado en el almacenamiento",
                                color = UniStackColors.TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
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
                tint = UniStackColors.TextPrimary
            )
        }
        Spacer(Modifier.width(4.dp))
        Column {
            Text(
                "Actualizaciones de UniStack",
                style = MaterialTheme.typography.headlineSmall,
                color = UniStackColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Mantén UniStack al día",
                style = MaterialTheme.typography.bodySmall,
                color = UniStackColors.TextSecondary
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
        color = UniStackColors.Card,
        onClick = if (isChecking) null else onCheckClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(UniStackColors.Primary.copy(alpha = 0.13f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isChecking) {
                    UniStackLoadingIndicator(
                        size = 20.dp,
                        color = UniStackColors.Primary
                    )
                } else {
                    Icon(Icons.Rounded.Refresh, contentDescription = null, tint = UniStackColors.Primary)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Verificar sistema",
                    color = UniStackColors.TextPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = state.statusLabel(),
                    color = UniStackColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (state is UpdateState.UpToDate) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = UniStackColors.Primary)
            }
        }
    }
}

@Composable
private fun UpdateIconTile(
    icon: ImageVector,
    accent: Color = UniStackColors.Primary
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
    is UpdateState.Error -> message
}
