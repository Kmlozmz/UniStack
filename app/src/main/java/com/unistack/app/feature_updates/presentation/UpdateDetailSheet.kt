package com.unistack.app.feature_updates.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.unistack.app.R
import com.unistack.app.feature_updates.domain.UpdateInfo
import kotlinx.coroutines.launch

/**
 * La hoja de «Actualización disponible», al abrir la app con una versión pendiente.
 *
 * Decidida el 19 sep 2026: sale donde estés, una vez por versión, y tiene dos salidas.
 * **Changelog** lleva a Actualizaciones sin descargar nada —ahí se ve qué trae y se decide—;
 * **Actualizar** lleva a Actualizaciones y arranca la descarga. Se cierra deslizando, y eso
 * también cuenta como vista: no vuelve a salir hasta que haya otra versión.
 *
 * No enseña las notas: para eso está el Changelog. Antes las traía enteras y el botón se
 * quedaba sin sitio con una lista larga.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdateDetailSheet(
    info: UpdateInfo,
    onChangelogClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    // Primero se va la hoja y luego pasa lo que pediste: al revés, la pantalla entraba con la
    // hoja todavía encima (19 sep).
    fun cerrarY(accion: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { accion() }
    }
    AEscalaDelArtifact {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 22.dp, end = 22.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Losa(icon = Icons.Rounded.NewReleases, tono = Tono.ACENTO)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.updates_sheet_title),
                        style = tituloDeTarjeta(22),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(
                            R.string.updates_sheet_meta,
                            info.versionName,
                            fechaCorta(info.releaseDate),
                            pesoEnMb(info.sizeMb)
                        ),
                        style = ApoyoDeTarjeta,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BotonDeLaReplica(
                    text = stringResource(R.string.updates_sheet_changelog),
                    onClick = { cerrarY(onChangelogClick) },
                    filled = false,
                    height = 50.dp,
                    modifier = Modifier.weight(1f)
                )
                BotonDeLaReplica(
                    text = stringResource(R.string.updates_sheet_update),
                    onClick = { cerrarY(onUpdateClick) },
                    icon = Icons.Rounded.Download,
                    height = 50.dp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
    }
}
