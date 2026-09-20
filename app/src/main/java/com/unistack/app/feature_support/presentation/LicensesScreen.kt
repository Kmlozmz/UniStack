@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_support.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.unistack.app.R
import com.unistack.app.core.design.components.SettingsGroup
import com.unistack.app.core.design.components.SettingsRow
import com.unistack.app.core.design.components.SettingsSoloRow
import com.unistack.app.core.design.theme.tonosDeAjustes
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.feature_support.domain.Biblioteca
import com.unistack.app.feature_support.domain.BibliotecasDelJson
import com.unistack.app.feature_support.domain.FuenteEmpaquetada
import com.unistack.app.feature_support.domain.FuentesEmpaquetadas
import com.unistack.app.feature_support.domain.TextoDeLicencia
import com.unistack.app.feature_support.domain.comoParrafos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Ajustes › Acerca de › Licencias: lo que la app lleva de otros y bajo qué condiciones.
 *
 * Dos grupos. **Tipografías**: las ocho familias de `res/font`, bajo la SIL Open Font License,
 * que exige que su texto y el aviso de cada una viajen con las fuentes. **Bibliotecas**: lo
 * que sale del JSON que genera el plugin de AboutLibraries en cada compilación (nombre,
 * versión y licencia de cada dependencia), leído por [BibliotecasDelJson] y pintado con las
 * piezas de la app. Tocar una fila abre la licencia en una hoja.
 */
@Composable
fun LicensesScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val librerias by produceState<List<Biblioteca>>(initialValue = emptyList()) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.resources.openRawResource(R.raw.aboutlibraries).bufferedReader().use { it.readText() }
            }.mapCatching(BibliotecasDelJson::leer).getOrDefault(emptyList())
        }
    }
    var abierta by remember { mutableStateOf<Licencia?>(null) }

    SupportScaffold(
        title = stringResource(R.string.licenses_title),
        subtitle = stringResource(R.string.licenses_subtitle),
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        item(key = "fuentes") {
            SettingsGroup(label = stringResource(R.string.licenses_fonts_header), rowCount = FuentesEmpaquetadas.todas.size) {
                FuentesEmpaquetadas.todas.forEach { fuente ->
                    SettingsRow(
                        icon = Icons.Rounded.TextFields,
                        title = fuente.nombre,
                        subtitle = stringResource(R.string.licenses_ofl_short),
                        iconColor = tonosDeAjustes.indigo,
                        onClick = { abierta = Licencia.DeFuente(fuente) }
                    )
                }
            }
        }
        item(key = "librerias") {
            val rotulo = if (librerias.isEmpty()) {
                stringResource(R.string.licenses_libraries_header)
            } else {
                stringResource(R.string.licenses_libraries_header_count, librerias.size)
            }
            SettingsGroup(label = rotulo, rowCount = maxOf(1, librerias.size)) {
                if (librerias.isEmpty()) {
                    SettingsSoloRow {
                        Text(
                            text = stringResource(R.string.licenses_libraries_loading),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                }
                librerias.forEach { lib ->
                    SettingsRow(
                        icon = Icons.Rounded.Extension,
                        title = lib.nombre,
                        subtitle = listOfNotNull(lib.version, lib.licencia?.nombre).joinToString(" · "),
                        iconColor = tonosDeAjustes.turquesa,
                        onClick = { abierta = Licencia.DeLibreria(lib) }
                    )
                }
            }
        }
    }

    abierta?.let { licencia ->
        HojaDeLicencia(licencia = licencia, onDismiss = { abierta = null })
    }
}

/** Lo que se abre en la hoja: una fuente o una biblioteca. */
private sealed interface Licencia {
    data class DeFuente(val fuente: FuenteEmpaquetada) : Licencia
    data class DeLibreria(val libreria: Biblioteca) : Licencia
}


/**
 * La licencia entera, en una hoja.
 *
 * El texto sale del asset si la licencia es una de las que empaquetamos (OFL, Apache 2.0),
 * o del JSON del plugin si la trajo; si no hay texto, queda el enlace a la licencia en la web.
 */
@Composable
private fun HojaDeLicencia(licencia: Licencia, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val titulo: String
    val aviso: String?
    val url: String?
    val asset: TextoDeLicencia?
    val textoDelJson: String?
    when (licencia) {
        is Licencia.DeFuente -> {
            titulo = licencia.fuente.nombre
            aviso = licencia.fuente.aviso
            url = licencia.fuente.url
            asset = TextoDeLicencia.OFL
            textoDelJson = null
        }
        is Licencia.DeLibreria -> {
            val lib = licencia.libreria
            val lic = lib.licencia
            titulo = lib.nombre
            aviso = listOfNotNull(
                lib.version?.let { "v$it" },
                lib.autores.takeIf { it.isNotEmpty() }?.joinToString(", "),
                lic?.nombre
            ).joinToString(" · ").ifBlank { null }
            url = lic?.url ?: lib.web
            asset = TextoDeLicencia.porSpdx(lic?.spdxId)
            textoDelJson = lic?.texto
        }
    }

    val texto by produceState<String?>(initialValue = null, asset, textoDelJson) {
        value = withContext(Dispatchers.IO) {
            when {
                asset != null -> runCatching {
                    context.assets.open(asset.asset).bufferedReader().use { it.readText() }
                }.getOrNull()
                else -> textoDelJson
            }?.comoParrafos()
        }
    }

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
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 22.dp, end = 22.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = titulo, style = MaterialTheme.typography.titleLargeEmphasized)
                if (aviso != null) {
                    Text(
                        text = aviso,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (url != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UniStackButton(
                        text = stringResource(R.string.licenses_open_site),
                        onClick = { abrirEnElNavegador(context, url) },
                        variant = UniStackButtonVariant.Tonal,
                        leadingIcon = Icons.Rounded.OpenInNew
                    )
                }
            }
            when {
                texto != null -> Text(
                    text = texto!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                asset == null && textoDelJson == null -> Text(
                    text = stringResource(R.string.licenses_only_online),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Abre una dirección en el navegador del teléfono, o avisa si no hay con qué. */
internal fun abrirEnElNavegador(context: android.content.Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
        android.widget.Toast.makeText(
            context,
            com.unistack.app.core.utils.Textos.get(R.string.notes_error_no_app_to_open),
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}
