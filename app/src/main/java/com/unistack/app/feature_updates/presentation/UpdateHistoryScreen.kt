package com.unistack.app.feature_updates.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.R
import com.unistack.app.core.design.components.LargeTitleScaffoldLayout
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_support.domain.changelogFor
import com.unistack.app.feature_updates.domain.HistorialDeVersiones
import com.unistack.app.feature_updates.domain.InstalledVersion
import com.unistack.app.feature_updates.domain.UpdateInfo
import java.io.File

/**
 * El historial de versiones: solo la que llevas y las anteriores.
 *
 * «Al ser historial», dijo el 19 sep 2026: lo nuevo no va aquí, se ve en Actualizaciones. Cada
 * fila dice cuándo se instaló y cuántos días estuvo puesta; la primera lleva «Tienes esta».
 */
@Composable
fun UpdateHistoryScreen(
    onBackClick: () -> Unit,
    onVersionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UpdateViewModel = hiltViewModel()
) {
    BackHandler(onBack = onBackClick)
    val history by viewModel.installHistory.collectAsStateWithLifecycle()
    val releases by viewModel.releases.collectAsStateWithLifecycle()

    LargeTitleScaffoldLayout(
        title = stringResource(R.string.updates_history_title),
        subtitle = stringResource(R.string.updates_history_subtitle),
        onBackClick = onBackClick,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = scrollBottomRoom),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item("lista") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column {
                        history.forEachIndexed { index, entry ->
                            if (index > 0) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                            val dias = HistorialDeVersiones.diasInstalada(history, index)
                            FilaDelHistorial(
                                entry = entry,
                                actual = index == 0,
                                meta = metaDelHistorial(entry, dias, releases, actual = index == 0),
                                onClick = { onVersionClick(entry.versionName) }
                            )
                        }
                    }
                }
            }
            item("pie") {
                Text(
                    text = stringResource(R.string.updates_history_footer),
                    style = ApoyoDeTarjeta,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/** Una fila del historial: losa de 38, «v1.0.0» con su sello, la fecha debajo y la flecha. */
@Composable
private fun FilaDelHistorial(
    entry: InstalledVersion,
    actual: Boolean,
    meta: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LosaPequena(
            icon = if (actual) Icons.Rounded.Check else Icons.Rounded.History,
            tono = if (actual) Tono.BIEN else Tono.SUAVE
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "v${entry.versionName}",
                    style = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (actual) Sello(text = stringResource(R.string.updates_chip_installed), tono = Tono.BIEN)
            }
            Text(
                text = meta,
                style = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun LosaPequena(icon: ImageVector, tono: Tono) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = tono.fondo(),
        contentColor = tono.encima(),
        modifier = Modifier.size(38.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

/**
 * «17 sep 2026 · 23,8 MB» para la actual; «12 sep 2026 · instalada 3 días · 23,1 MB» para las
 * de antes. El peso sale de las publicaciones si esa versión está entre ellas; si no, se omite.
 */
@Composable
private fun metaDelHistorial(
    entry: InstalledVersion,
    dias: Int?,
    releases: List<UpdateInfo>,
    actual: Boolean
): String {
    val partes = mutableListOf(fechaCorta(entry.installedAtMillis))
    if (!actual && dias != null) {
        partes += if (dias == 1) {
            stringResource(R.string.updates_history_installed_one_day)
        } else {
            stringResource(R.string.updates_history_installed_days, dias)
        }
    }
    val peso = pesoDeLaVersion(entry.versionName, releases, actual)
    if (peso != null) partes += peso
    return partes.joinToString(" · ")
}

@Composable
private fun pesoDeLaVersion(version: String, releases: List<UpdateInfo>, actual: Boolean): String? {
    val release = releases.firstOrNull { it.versionName == version }
    if (release != null) return pesoEnMb(release.sizeMb)
    if (!actual) return null
    val context = LocalContext.current
    val bytes = remember(context) { runCatching { File(context.applicationInfo.sourceDir).length() }.getOrDefault(0L) }
    return if (bytes > 0L) pesoEnMb(bytes / 1024.0 / 1024.0) else null
}

/**
 * Una versión del historial: su ficha y sus notas. Sin botón de descargar: ya pasó.
 *
 * Las notas salen de la publicación de GitHub si está entre las recientes; para la instalada,
 * del registro de cambios que va dentro del APK; para las demás, no hay.
 */
@Composable
fun UpdateVersionScreen(
    versionName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UpdateViewModel = hiltViewModel()
) {
    BackHandler(onBack = onBackClick)
    val history by viewModel.installHistory.collectAsStateWithLifecycle()
    val releases by viewModel.releases.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val actual = versionName == viewModel.currentVersionName
    val entry = history.firstOrNull { it.versionName == versionName }
    val release = releases.firstOrNull { it.versionName == versionName }

    val notas = remember(versionName, release) {
        release?.releaseNotes?.takeIf { it.isNotBlank() }
            ?: if (actual) {
                runCatching { context.assets.open("changelog.md").bufferedReader().use { it.readText() } }
                    .getOrNull()
                    ?.let { markdown -> changelogFor(markdown, versionName).joinToString("\n\n") { it.body } }
                    ?.takeIf { it.isNotBlank() }
            } else {
                null
            }
    }
    val fecha = when {
        release != null -> fechaCorta(release.releaseDate)
        entry != null -> fechaCorta(entry.installedAtMillis)
        else -> ""
    }
    val peso = pesoDeLaVersion(versionName, releases, actual)
    val meta = listOfNotNull(fecha.takeIf { it.isNotBlank() }, peso).joinToString(" · ")

    LargeTitleScaffoldLayout(
        title = "v$versionName",
        subtitle = if (actual) {
            stringResource(R.string.updates_history_detail_meta_installed, meta)
        } else {
            meta
        },
        onBackClick = onBackClick,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = scrollBottomRoom),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item("ficha") {
                TarjetaDeVersion(
                    versionName = versionName,
                    meta = meta,
                    sello = if (actual) stringResource(R.string.updates_chip_installed) else null,
                    tonoSello = Tono.BIEN,
                    tonoLosa = Tono.SUAVE
                )
            }
            item("notas") {
                NotasAgrupadas(markdown = notas ?: stringResource(R.string.update_no_notes))
            }
        }
    }
}
