@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.RoundedCorner
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.SettingsGroup
import com.unistack.app.core.design.components.SettingsHeader
import com.unistack.app.core.design.components.SettingsRow
import com.unistack.app.core.design.theme.AppThemes
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.greetingForNow
import com.unistack.app.feature_user.domain.AccentStyle
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.BottomBarStyle
import com.unistack.app.feature_user.domain.CornerStyle
import com.unistack.app.feature_user.domain.HomeSection
import com.unistack.app.feature_user.domain.InterfaceDensity
import com.unistack.app.feature_user.domain.MotionCatalog
import com.unistack.app.feature_user.domain.ProgressShape
import com.unistack.app.feature_user.domain.SurfaceStyle
import com.unistack.app.feature_user.domain.SwitchIconStyle
import com.unistack.app.feature_user.domain.TypographyStyle
import com.unistack.app.feature_user.domain.VisualPreference
import com.unistack.app.feature_user.domain.portraitUrl

/**
 * Apariencia: un hub de cinco puertas, con la maqueta arriba.
 *
 * **Era una sola pantalla con seis grupos apilados** —modo, color, densidad y letra, detalles
 * de la interfaz, tarjetas de inicio y lo que enseña el hero— que se recorría a base de scroll
 * y donde el color se elegía en dos sitios a la vez: arriba «Tema y color» abría la pantalla de
 * los veintiocho temas, y tres dedos más abajo un grupo «COLOR» ofrecía otra vez las mismas dos
 * opciones. Elegir en uno dejaba al otro contando algo distinto.
 *
 * Ahora es el mismo arreglo que se hizo en **Configuración académica**: cada cosa en su puerta,
 * y la fila diciendo qué hay elegido ahora mismo sin tener que entrar. Lo único que se queda
 * aquí es la maqueta, porque es la razón de estar en esta pantalla: se toca algo detrás de
 * cualquier puerta y al volver se ve el resultado sin salir de ajustes.
 */
@Composable
fun AppearanceSettingsScreen(
    onBackClick: () -> Unit,
    onThemeClick: () -> Unit,
    onSurfaceClick: () -> Unit,
    onTypographyClick: () -> Unit,
    onComponentsClick: () -> Unit,
    onHomeClick: () -> Unit,
    onMotionClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val sections = LocalSectionColors.current
    val current = profile

    LazyColumn(
        modifier = modifier
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
            SettingsHeader(
                title = "Apariencia",
                subtitle = "Tema, forma, letra y tu inicio",
                onBackClick = onBackClick
            )
        }
        if (current == null) {
            item {
                Text(
                    "Cargando preferencias...",
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@LazyColumn
        }

        val appearance = current.appearancePreferences
        item {
            HomePreviewCard(
                name = current.preferredName,
                photoUrl = current.portraitUrl,
                appearance = appearance
            )
        }
        item {
            SettingsGroup(label = "SECCIONES", rowCount = 6) {
                SettingsRow(
                    icon = Icons.Rounded.Palette,
                    title = "Tema y color",
                    // El resumen dice las dos cosas que decide esta puerta —el modo y el
                    // tema— porque son las que más se cambian y las que más se olvida cuál
                    // quedó puesta.
                    subtitle = current.visualPreference.orSystem().label() + " · " +
                        if (appearance.accentStyle == AccentStyle.DYNAMIC) {
                            "Del fondo de pantalla"
                        } else {
                            AppThemes.byId(appearance.themeId).name
                        },
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = onThemeClick
                )
                SettingsRow(
                    icon = Icons.Rounded.RoundedCorner,
                    title = "Forma y superficie",
                    subtitle = appearance.surfaceStyle.label() + " · esquinas " +
                        appearance.cornerStyle.label().lowercase() + " · " +
                        appearance.interfaceDensity.label().lowercase(),
                    iconColor = sections.schedule,
                    onClick = onSurfaceClick
                )
                SettingsRow(
                    icon = Icons.Rounded.TextFields,
                    title = "Tipografía",
                    subtitle = appearance.typographyStyle.label() + " · " +
                        appearance.decimalPlaces.ejemploDeNota(),
                    iconColor = sections.onTrack,
                    onClick = onTypographyClick
                )
                SettingsRow(
                    icon = Icons.Rounded.Tune,
                    title = "Componentes",
                    subtitle = "Barra " + appearance.bottomBarStyle.label().lowercase() +
                        " · barras " + appearance.academicProgressShape.label().lowercase(),
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    onClick = onComponentsClick
                )
                SettingsRow(
                    icon = Icons.Rounded.Animation,
                    title = "Movimiento",
                    subtitle = "${MotionCatalog.gestures.size} gestos · " +
                        "${MotionCatalog.variantCount} variantes",
                    iconColor = MaterialTheme.colorScheme.secondary,
                    onClick = onMotionClick
                )
                SettingsRow(
                    icon = Icons.Rounded.Home,
                    title = "Tu inicio",
                    subtitle = appearance.resumenDeInicio(),
                    iconColor = sections.expenses,
                    onClick = onHomeClick
                )
            }
        }
        item {
            TextButton(
                onClick = viewModel::resetAppearance,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.RestartAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Restablecer apariencia")
            }
        }
    }
}

/** «3 de 4 bloques» y no la lista entera: en una fila no caben cuatro nombres. */
private fun AppearancePreferences.resumenDeInicio(): String {
    val encendidos = listOf(showHomeGreeting, showHomeHero, showHomeAgenda, showHomeSnapshot).count { it }
    return when (encendidos) {
        4 -> "Los 4 bloques encendidos"
        0 -> "Sin bloques: solo el logo"
        else -> "$encendidos de 4 bloques"
    }
}

private fun Int.ejemploDeNota(): String = when (this) {
    0 -> "notas como 3"
    1 -> "notas como 3,5"
    else -> "notas como 3,50"
}

/** Los cuatro modos que se pueden elegir. */
internal val VisiblePreferences = listOf(
    VisualPreference.SYSTEM,
    VisualPreference.LIGHT,
    VisualPreference.DARK,
    VisualPreference.OLED
)

internal fun VisualPreference.orSystem(): VisualPreference =
    if (this in VisiblePreferences) this else VisualPreference.SYSTEM

/**
 * Tu inicio, en pequeño y de verdad.
 *
 * Lo que había antes eran tres rectángulos grises y una línea de texto que resumía los ajustes
 * con palabras. No enseñaba nada: para saber cómo iba a quedar el inicio había que salir de
 * aquí e ir a mirarlo. Esta maqueta enciende y apaga las mismas piezas que los interruptores
 * de «Tu inicio», y el color y la letra son los del tema que esté puesto en ese momento.
 */
@Composable
private fun HomePreviewCard(
    name: String,
    photoUrl: String?,
    appearance: AppearancePreferences
) {
    val shown = name.takeIf { it.isNotBlank() } ?: "Estudiante"

    Column {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Surface(
                modifier = Modifier.padding(10.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(13.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // La maqueta enseña tu retrato, no una inicial genérica: es una vista
                        // previa de tu Inicio, y el avatar es lo primero que se ve en él.
                        AccountAvatar(
                            photoUrl = photoUrl,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            initial = shown.first().uppercase()
                        )
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Uni",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Stack",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(Modifier.width(22.dp))
                    }
                    if (appearance.showHomeGreeting) {
                        Text(
                            text = greetingForNow().uppercase(),
                            modifier = Modifier.padding(top = 10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            style = SectionLabelStyle
                        )
                        Text(
                            text = shown,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLargeEmphasized,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                    if (appearance.showHomeHero) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 9.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Text("LO SIGUIENTE", style = SectionLabelStyle)
                                Text(
                                    "Cálculo III a las 10:00",
                                    modifier = Modifier.padding(top = 2.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        Text(
            text = "Así queda tu inicio con lo que elijas.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

internal fun AppearancePreferences.showsSection(section: HomeSection): Boolean = when (section) {
    HomeSection.HERO -> showHomeHero
    HomeSection.AGENDA -> showHomeAgenda
    HomeSection.SNAPSHOT -> showHomeSnapshot
}

internal fun VisualPreference.label() = when (this) {
    VisualPreference.SYSTEM -> "Sistema"
    VisualPreference.LIGHT -> "Claro"
    VisualPreference.DARK -> "Oscuro"
    VisualPreference.OLED -> "OLED"
    VisualPreference.CUSTOM -> "Personalizado"
}

internal fun VisualPreference.themeDescription() = when (this) {
    VisualPreference.SYSTEM -> "Sigue el tema de Android."
    VisualPreference.LIGHT -> "Siempre claro, aunque Android esté oscuro."
    VisualPreference.DARK -> "Siempre oscuro, aunque Android esté claro."
    VisualPreference.OLED -> "Negro puro: en pantallas OLED gasta menos batería."
    VisualPreference.CUSTOM -> ""
}

internal fun InterfaceDensity.label() = when (this) {
    InterfaceDensity.COMPACT -> "Compacta"
    InterfaceDensity.BALANCED -> "Equilibrada"
    InterfaceDensity.COMFORTABLE -> "Cómoda"
}

internal fun TypographyStyle.label() = when (this) {
    TypographyStyle.UNISTACK -> "UniStack"
    TypographyStyle.SYSTEM -> "Sistema"
    TypographyStyle.SERIF -> "Serif"
    TypographyStyle.MONO -> "Mono"
}

internal fun HomeSection.label() = when (this) {
    HomeSection.HERO -> "Lo siguiente"
    HomeSection.AGENDA -> "Hoy"
    HomeSection.SNAPSHOT -> "Cifras"
}

internal fun HomeSection.detail() = when (this) {
    HomeSection.HERO -> "La tarjeta con lo más urgente"
    HomeSection.AGENDA -> "Clases y entregas del día"
    HomeSection.SNAPSHOT -> "Promedio, pendientes y gasto"
}

internal fun BottomBarStyle.label() = when (this) {
    BottomBarStyle.LABELED -> "Con texto"
    BottomBarStyle.ICONS_ONLY -> "Solo iconos"
}

internal fun ProgressShape.label() = when (this) {
    ProgressShape.FLAT -> "Rectas"
    ProgressShape.WAVY -> "Onduladas"
}

internal fun SwitchIconStyle.label() = when (this) {
    SwitchIconStyle.BOTH -> "Siempre"
    SwitchIconStyle.CHECKED_ONLY -> "Al encender"
    SwitchIconStyle.NONE -> "Nunca"
}

internal fun SurfaceStyle.label() = when (this) {
    SurfaceStyle.FLAT -> "Plana"
    SurfaceStyle.OUTLINED -> "Filete"
    SurfaceStyle.ELEVATED -> "Sombra"
    SurfaceStyle.TRANSLUCENT -> "Cristal"
}

internal fun SurfaceStyle.explicacion() = when (this) {
    SurfaceStyle.FLAT -> "Sin bordes ni sombra: la tarjeta se distingue solo por su tono."
    SurfaceStyle.OUTLINED -> "Un filete fino marca dónde acaba cada tarjeta."
    SurfaceStyle.ELEVATED -> "Las tarjetas proyectan sombra y se leen como capas."
    SurfaceStyle.TRANSLUCENT -> "Semitransparentes, dejando ver el fondo por debajo."
}

internal fun CornerStyle.label() = when (this) {
    CornerStyle.COMPACT -> "Rectas"
    CornerStyle.BALANCED -> "Medias"
    CornerStyle.SOFT -> "Suaves"
}
