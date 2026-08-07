package com.unistack.app.feature_updates.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_updates.domain.UpdateInfo
import com.unistack.app.feature_updates.domain.UpdateState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDetailSheet(
    info: UpdateInfo,
    state: UpdateState,
    onDownloadClick: () -> Unit,
    onInstallClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = UniStackColors.Background,
        contentColor = UniStackColors.TextPrimary,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Nueva actualización v${info.versionName}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = UniStackColors.TextPrimary
            )
            Text(
                "Publicado el ${info.releaseDate} · ${String.format(Locale.US, "%.1f", info.sizeMb)} MB",
                style = MaterialTheme.typography.bodySmall,
                color = UniStackColors.TextSecondary
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                info.releaseNotes.lineSequence()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .forEach { line ->
                        Text(
                            "- ${line.removePrefix("-").trim()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = UniStackColors.TextSecondary
                        )
                    }
            }

            if (state is UpdateState.Downloading) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = { state.progress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Text(
                        "${state.progress}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = UniStackColors.TextSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Text("Más tarde")
                }
                Button(
                    onClick = if (state is UpdateState.ReadyToInstall) onInstallClick else onDownloadClick,
                    enabled = state !is UpdateState.Downloading,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UniStackColors.Primary,
                        contentColor = UniStackColors.OnPrimary
                    )
                ) {
                    Text(
                        when (state) {
                            is UpdateState.ReadyToInstall -> "Instalar"
                            is UpdateState.Downloading -> "Descargando..."
                            else -> "Descargar"
                        }
                    )
                }
            }
        }
    }
}
