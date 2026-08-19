@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_updates.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unistack.app.feature_updates.domain.UpdateInfo
import com.unistack.app.feature_updates.domain.UpdateState
import java.util.Locale
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.unistack.app.core.design.components.UniStackButtonDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDetailSheet(
    info: UpdateInfo,
    state: UpdateState,
    onDownloadClick: () -> Unit,
    onInstallClick: () -> Unit,
    onDismiss: () -> Unit,
    canInstall: Boolean = true
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface,
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
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Publicado el ${info.releaseDate} · ${String.format(Locale.US, "%.1f", info.sizeMb)} MB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Las notas se desplazan dentro de su propio hueco y con tope de alto. Sin eso,
            // una lista larga empujaba los botones fuera de la hoja: se veían recortados por
            // abajo y su texto se quedaba sin ancho, reducido a puntos suspensivos.
            ReleaseNotes(
                markdown = info.releaseNotes,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 340.dp)
                    .verticalScroll(rememberScrollState())
            )

            if (state is UpdateState.Downloading) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val unknown = state.progress == UpdateState.UNKNOWN_PROGRESS
                    if (unknown) {
                        LinearWavyProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    } else {
                        LinearWavyProgressIndicator(
                            progress = { state.progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                    Text(
                        if (unknown) "Descargando..." else "${state.progress}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedButton(
                    shapes = UniStackButtonDefaults.shapes,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    // Sin este relleno, Material reserva 24dp a cada lado y en media pantalla
                    // el texto se queda sin sitio y se recorta a puntos.
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Más tarde", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Button(
                    shapes = UniStackButtonDefaults.shapes,
                    onClick = if (state is UpdateState.ReadyToInstall) onInstallClick else onDownloadClick,
                    enabled = state !is UpdateState.Downloading,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        when (state) {
                            // Si falta autorizar el origen, el botón lleva a Ajustes y no
                            // al instalador. Decirlo evita que el desvío parezca un fallo.
                            is UpdateState.ReadyToInstall ->
                                if (canInstall) "Instalar" else "Autorizar"
                            is UpdateState.Downloading -> "Descargando..."
                            else -> "Descargar"
                        },
                        // El botón mide la mitad del ancho y tiene alto fijo: «Permitir
                        // instalación» partía en dos líneas y la segunda se salía por abajo.
                        // El texto se encoge antes que cortarse.
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
