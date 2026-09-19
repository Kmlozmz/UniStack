package com.unistack.app.feature_updates.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.theme.LocalSectionColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
 * Las piezas de la réplica aprobada el 19 sep 2026 («F · Tu elección»), a 390 dp, sin escalar:
 * la ficha de una versión, el sello, la losa del icono y las notas agrupadas. Las medidas y los
 * cuerpos de letra son los del artifact en píxeles, que allí son dp.
 */

/** Título de una tarjeta: 20 px / 800 en la réplica; 18 en la ficha de versión. */
internal fun tituloDeTarjeta(size: Int = 20): TextStyle = TextStyle(
    fontSize = size.sp,
    lineHeight = (size * 1.15f).sp,
    fontWeight = FontWeight.ExtraBold
)

internal val ApoyoDeTarjeta: TextStyle = TextStyle(fontSize = 12.5.sp, lineHeight = 17.sp)
internal val PistaDeTarjeta: TextStyle = TextStyle(fontSize = 11.5.sp, lineHeight = 15.sp)
internal val TextoDeNota: TextStyle = TextStyle(fontSize = 13.5.sp, lineHeight = 18.sp)
internal val RotuloDeGrupo: TextStyle = TextStyle(
    fontSize = 11.sp,
    lineHeight = 14.sp,
    fontWeight = FontWeight.ExtraBold,
    letterSpacing = 0.9.sp
)

/** El tono de una losa o un sello: de qué color va y qué color lleva encima. */
internal enum class Tono { ACENTO, BIEN, INFO, MAL, SUAVE }

@Composable
internal fun Tono.fondo(): Color {
    val sections = LocalSectionColors.current
    return when (this) {
        Tono.ACENTO -> MaterialTheme.colorScheme.primary
        Tono.BIEN -> sections.onTrack
        Tono.INFO -> sections.schedule
        Tono.MAL -> MaterialTheme.colorScheme.error
        Tono.SUAVE -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
}

@Composable
internal fun Tono.encima(): Color {
    val sections = LocalSectionColors.current
    return when (this) {
        Tono.ACENTO -> MaterialTheme.colorScheme.onPrimary
        Tono.BIEN -> sections.onTrackContainer
        Tono.INFO -> sections.scheduleContainer
        Tono.MAL -> MaterialTheme.colorScheme.errorContainer
        Tono.SUAVE -> MaterialTheme.colorScheme.primary
    }
}

/** La losa de 44 dp con el icono, a la izquierda de cada tarjeta. */
@Composable
internal fun Losa(icon: ImageVector, tono: Tono, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = tono.fondo(),
        contentColor = tono.encima(),
        modifier = modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
        }
    }
}

/** El sello de la ficha: «Nueva», «Descargada», «Tienes esta». Pastilla de 11 sp. */
@Composable
internal fun Sello(text: String, tono: Tono) {
    val sections = LocalSectionColors.current
    val (fondo, tinta) = when (tono) {
        Tono.ACENTO -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        Tono.BIEN -> sections.onTrackContainer to sections.onTrack
        else -> MaterialTheme.colorScheme.surfaceContainerHigh to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .background(fondo, CircleShape)
            .padding(horizontal = 9.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = tinta,
            style = TextStyle(fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.ExtraBold),
            maxLines = 1
        )
    }
}

/**
 * La ficha de una versión: losa, «v1.0.1» con su sello, y debajo la fecha y el peso.
 *
 * @param meta lo de debajo, ya escrito: «19 sep 2026 · 24,1 MB», o con «instalada 3 días».
 */
@Composable
internal fun TarjetaDeVersion(
    versionName: String,
    meta: String,
    sello: String?,
    tonoSello: Tono,
    tonoLosa: Tono,
    icon: ImageVector = Icons.Rounded.NewReleases,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        onClick = onClick ?: {},
        enabled = onClick != null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Losa(icon = icon, tono = tonoLosa)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "v$versionName",
                        style = tituloDeTarjeta(18),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (sello != null) Sello(text = sello, tono = tonoSello)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(
                        Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = meta,
                        style = ApoyoDeTarjeta,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Las notas de una versión, agrupadas: NUEVO / MEJORADO / ARREGLADO con su icono y sus puntos.
 *
 * Los grupos son los encabezados `###` del registro de cambios; el icono sale de la palabra.
 * Sin encabezados —como las notas de la 1.0.0, que van en párrafos— se pintan como párrafos.
 */
@Composable
internal fun NotasAgrupadas(markdown: String, modifier: Modifier = Modifier) {
    val blocks = remember(markdown) { parseReleaseNotes(markdown) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            blocks.forEach { block ->
                when (block) {
                    is NotesBlock.Heading -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Icon(
                            iconoDeGrupo(block.text),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = block.text.uppercase(),
                            style = RotuloDeGrupo,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    is NotesBlock.Bullet -> Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Text(
                            text = block.text,
                            style = TextoDeNota,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    is NotesBlock.Paragraph -> Text(
                        text = block.text,
                        style = TextoDeNota,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    NotesBlock.Divider -> Spacer(Modifier.width(1.dp))
                }
            }
        }
    }
}

private fun iconoDeGrupo(heading: String): ImageVector {
    val h = heading.lowercase()
    return when {
        "nuevo" in h || "new" in h || "añad" in h || "added" in h -> Icons.Rounded.AutoAwesome
        "arregl" in h || "correg" in h || "fix" in h -> Icons.Rounded.BugReport
        "mejor" in h || "cambi" in h || "improv" in h || "changed" in h -> Icons.Rounded.Build
        else -> Icons.Rounded.NewReleases
    }
}

// ------------------------------------------------------------------ fechas y pesos

/** «19 sep 2026» a partir de la fecha ISO que da GitHub («2026-09-19»). */
@Composable
internal fun fechaCorta(iso: String): String {
    val locale = localeDePantalla()
    return remember(iso, locale) {
        runCatching { LocalDate.parse(iso.take(10)).fechaCorta(locale) }.getOrDefault(iso)
    }
}

/** «17 sep 2026» a partir de milisegundos. */
@Composable
internal fun fechaCorta(millis: Long): String {
    val locale = localeDePantalla()
    return remember(millis, locale) {
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().fechaCorta(locale)
    }
}

private fun LocalDate.fechaCorta(locale: Locale): String =
    // El nombre corto del mes lleva punto en español («sept.»); en la réplica no lo lleva.
    format(DateTimeFormatter.ofPattern("d MMM yyyy", locale)).replace(".", "")

@Composable
private fun localeDePantalla(): Locale {
    val locales = LocalConfiguration.current.locales
    return if (locales.isEmpty) Locale.getDefault() else locales[0]
}

/** «24,1 MB», con la coma o el punto del idioma. */
@Composable
internal fun pesoEnMb(sizeMb: Double): String {
    val locale = localeDePantalla()
    return remember(sizeMb, locale) { String.format(locale, "%.1f MB", sizeMb) }
}

/** «hace 2 min», «hace 3 h», «hace 2 días» o «ahora mismo»; vacío si nunca se ha mirado. */
@Composable
internal fun haceCuanto(millis: Long, ahora: Long): String {
    if (millis <= 0L) return ""
    val minutos = ((ahora - millis) / 60_000L).coerceAtLeast(0L)
    return when {
        minutos < 1L -> stringResource(R.string.updates_ago_now)
        minutos < 60L -> stringResource(R.string.updates_ago_minutes, minutos)
        minutos < 24L * 60L -> stringResource(R.string.updates_ago_hours, minutos / 60L)
        else -> stringResource(R.string.updates_ago_days, minutos / (24L * 60L))
    }
}
