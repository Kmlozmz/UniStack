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
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.SettingsGroup
import com.unistack.app.core.design.components.SettingsRow
import com.unistack.app.core.design.theme.AppThemes
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.greetingForNow
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
import com.unistack.app.core.utils.Textos

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

    LargeTitleScaffold(
        title = stringResource(R.string.settings_appearance_title),
        subtitle = stringResource(R.string.settings_appearance_subtitle),
        onBackClick = onBackClick,
        modifier = modifier,
        horizontalPadding = spacing.screenHorizontal,
        topPadding = 8.dp,
        bottomPadding = scrollBottomRoom,
        itemSpacing = 12.dp
    ) {
        if (current == null) {
            item {
                Text(
                    stringResource(R.string.settings_appearance_loading),
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@LargeTitleScaffold
        }

        val appearance = current.appearancePreferences
        item {
            SettingsGroup(label = stringResource(R.string.settings_appearance_sections), rowCount = 6) {
                SettingsRow(
                    icon = Icons.Rounded.Palette,
                    title = stringResource(R.string.settings_appearance_theme_title),
                    // El resumen dice las dos cosas que decide esta puerta —el modo y el
                    // tema— porque son las que más se cambian y las que más se olvida cuál
                    // quedó puesta.
                    subtitle = current.visualPreference.orSystem().label() + " · " +
                        AppThemes.byId(appearance.themeId).name,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = onThemeClick
                )
                SettingsRow(
                    icon = Icons.Rounded.RoundedCorner,
                    title = stringResource(R.string.settings_appearance_surface_title),
                    subtitle = stringResource(
                        R.string.settings_appearance_surface_sub,
                        appearance.surfaceStyle.label(),
                        appearance.cornerStyle.label().lowercase(),
                        appearance.interfaceDensity.label().lowercase()
                    ),
                    iconColor = sections.schedule,
                    onClick = onSurfaceClick
                )
                SettingsRow(
                    icon = Icons.Rounded.TextFields,
                    title = stringResource(R.string.settings_appearance_typo_title),
                    subtitle = appearance.typographyStyle.label() + " · " +
                        appearance.decimalPlaces.ejemploDeNota(),
                    iconColor = sections.onTrack,
                    onClick = onTypographyClick
                )
                SettingsRow(
                    icon = Icons.Rounded.Tune,
                    title = stringResource(R.string.settings_appearance_components_title),
                    subtitle = stringResource(
                        R.string.settings_appearance_components_sub,
                        appearance.bottomBarStyle.label().lowercase(),
                        appearance.academicProgressShape.label().lowercase()
                    ),
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    onClick = onComponentsClick
                )
                SettingsRow(
                    icon = Icons.Rounded.Animation,
                    title = stringResource(R.string.settings_appearance_motion_title),
                    subtitle = stringResource(
                        R.string.settings_appearance_motion_sub,
                        MotionCatalog.gestures.size,
                        MotionCatalog.variantCount
                    ),
                    iconColor = MaterialTheme.colorScheme.secondary,
                    onClick = onMotionClick
                )
                SettingsRow(
                    icon = Icons.Rounded.Home,
                    title = stringResource(R.string.settings_appearance_home_title),
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
                Text(stringResource(R.string.settings_appearance_reset))
            }
        }
    }
}

/** «3 de 4 bloques» y no la lista entera: en una fila no caben cuatro nombres. */
private fun AppearancePreferences.resumenDeInicio(): String {
    val isEn = java.util.Locale.getDefault().language == "en"
    val encendidos = listOf(showHomeGreeting, showHomeHero, showHomeAgenda, showHomeSnapshot).count { it }
    return when (encendidos) {
        4 -> Textos.get(R.string.settings_appearance_home_4_on)
        0 -> Textos.get(R.string.settings_appearance_home_0_on)
        else -> Textos.get(R.string.appearance_de_4_bloques, encendidos)
    }
}

private fun Int.ejemploDeNota(): String {
    val isEn = java.util.Locale.getDefault().language == "en"
    return when (this) {
        0 -> Textos.get(R.string.settings_appearance_grade_sample_0)
        1 -> Textos.get(R.string.settings_appearance_grade_sample_1)
        else -> Textos.get(R.string.settings_appearance_grade_sample_2)
    }
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
internal fun HomePreviewCard(
    name: String,
    photoUrl: String?,
    appearance: AppearancePreferences
) {
    val shown = name.takeIf { it.isNotBlank() } ?: stringResource(R.string.settings_profile_student)

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
                                Text(stringResource(R.string.home_hero_later), style = SectionLabelStyle)
                                Text(
                                    stringResource(R.string.preview_sample_class_at_ten),
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
            text = stringResource(R.string.appearance_home_preview_caption),
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

internal fun VisualPreference.label(): String {
    val isEn = java.util.Locale.getDefault().language == "en"
    return when (this) {
        VisualPreference.SYSTEM -> Textos.get(R.string.appearance_sistema)
        VisualPreference.LIGHT -> Textos.get(R.string.appearance_claro)
        VisualPreference.DARK -> Textos.get(R.string.appearance_oscuro)
        VisualPreference.OLED -> "OLED"
        VisualPreference.CUSTOM -> Textos.get(R.string.appearance_personalizado)
    }
}

internal fun VisualPreference.themeDescription(): String {
    val isEn = java.util.Locale.getDefault().language == "en"
    return when (this) {
        VisualPreference.SYSTEM -> Textos.get(R.string.appearance_sigue_el_tema_de_android)
        VisualPreference.LIGHT -> Textos.get(R.string.appearance_siempre_claro_aunque_android_este_oscuro)
        VisualPreference.DARK -> Textos.get(R.string.appearance_siempre_oscuro_aunque_android_este_claro)
        VisualPreference.OLED -> Textos.get(R.string.appearance_negro_puro_en_pantallas_oled_gasta)
        VisualPreference.CUSTOM -> ""
    }
}

internal fun InterfaceDensity.label(): String {
    val isEn = java.util.Locale.getDefault().language == "en"
    return when (this) {
        InterfaceDensity.COMPACT -> Textos.get(R.string.appearance_compacta)
        InterfaceDensity.BALANCED -> Textos.get(R.string.appearance_equilibrada)
        InterfaceDensity.COMFORTABLE -> Textos.get(R.string.appearance_comoda)
    }
}

internal fun TypographyStyle.label(): String {
    val isEn = java.util.Locale.getDefault().language == "en"
    return when (this) {
        TypographyStyle.SANS -> "Sans"
        TypographyStyle.SYSTEM -> Textos.get(R.string.appearance_sistema)
        TypographyStyle.SERIF -> "Serif"
        TypographyStyle.MONO -> "Mono"
        TypographyStyle.ESTRECHA -> Textos.get(R.string.appearance_estrecha)
        TypographyStyle.REDONDEADA -> Textos.get(R.string.appearance_redondeada)
    }
}

internal fun TypographyStyle.explicacion(): String {
    val isEn = java.util.Locale.getDefault().language == "en"
    return when (this) {
        TypographyStyle.SANS -> Textos.get(R.string.appearance_la_sans_serif_del_sistema_es)
        TypographyStyle.SYSTEM -> Textos.get(R.string.appearance_la_que_traiga_tu_telefono_como)
        TypographyStyle.SERIF -> Textos.get(R.string.appearance_con_remates_se_lee_mejor_en)
        TypographyStyle.MONO -> Textos.get(R.string.appearance_ancho_fijo_las_cifras_quedan_alineadas)
        TypographyStyle.ESTRECHA -> Textos.get(R.string.appearance_condensada_cabe_mas_nombre_de_materia)
        TypographyStyle.REDONDEADA -> Textos.get(R.string.appearance_de_trazo_mas_blando_si_tu)
    }
}

internal fun HomeSection.label(): String {
    val isEn = java.util.Locale.getDefault().language == "en"
    return when (this) {
        HomeSection.HERO -> Textos.get(R.string.appearance_lo_siguiente)
        HomeSection.AGENDA -> Textos.get(R.string.notif_time_today)
        HomeSection.SNAPSHOT -> Textos.get(R.string.appearance_cifras)
    }
}

internal fun HomeSection.detail(): String {
    val isEn = java.util.Locale.getDefault().language == "en"
    return when (this) {
        HomeSection.HERO -> Textos.get(R.string.appearance_la_tarjeta_con_lo_mas_urgente)
        HomeSection.AGENDA -> Textos.get(R.string.appearance_clases_y_entregas_del_dia)
        HomeSection.SNAPSHOT -> Textos.get(R.string.appearance_promedio_pendientes_y_gasto)
    }
}

internal fun BottomBarStyle.label(): String {
    val isEn = java.util.Locale.getDefault().language == "en"
    return when (this) {
        BottomBarStyle.LABELED -> Textos.get(R.string.appearance_con_texto)
        BottomBarStyle.ICONS_ONLY -> Textos.get(R.string.appearance_solo_iconos)
    }
}

internal fun ProgressShape.label(): String {
    val isEn = java.util.Locale.getDefault().language == "en"
    return when (this) {
        ProgressShape.FLAT -> Textos.get(R.string.appearance_rectas)
        ProgressShape.WAVY -> Textos.get(R.string.appearance_onduladas)
    }
}

internal fun SwitchIconStyle.label(): String {
    val isEn = java.util.Locale.getDefault().language == "en"
    return when (this) {
        SwitchIconStyle.BOTH -> Textos.get(R.string.appearance_siempre)
        SwitchIconStyle.CHECKED_ONLY -> Textos.get(R.string.appearance_al_encender)
        SwitchIconStyle.NONE -> Textos.get(R.string.appearance_nunca)
    }
}

internal fun SurfaceStyle.label(): String {
    val isEn = java.util.Locale.getDefault().language == "en"
    return when (this) {
        SurfaceStyle.FLAT -> Textos.get(R.string.appearance_plana)
        SurfaceStyle.OUTLINED -> Textos.get(R.string.appearance_filete)
        SurfaceStyle.ELEVATED -> Textos.get(R.string.appearance_sombra)
        SurfaceStyle.TRANSLUCENT -> Textos.get(R.string.appearance_cristal)
    }
}

internal fun SurfaceStyle.explicacion(): String {
    val isEn = java.util.Locale.getDefault().language == "en"
    return when (this) {
        SurfaceStyle.FLAT -> Textos.get(R.string.appearance_sin_bordes_ni_sombra_la_tarjeta)
        SurfaceStyle.OUTLINED -> Textos.get(R.string.appearance_un_filete_fino_marca_donde_acaba)
        SurfaceStyle.ELEVATED -> Textos.get(R.string.appearance_las_tarjetas_proyectan_sombra_y_se)
        SurfaceStyle.TRANSLUCENT -> Textos.get(R.string.appearance_semitransparentes_dejando_ver_el_fondo_por)
    }
}

internal fun CornerStyle.label(): String {
    val isEn = java.util.Locale.getDefault().language == "en"
    return when (this) {
        CornerStyle.COMPACT -> Textos.get(R.string.appearance_rectas_2)
        CornerStyle.BALANCED -> Textos.get(R.string.appearance_medias)
        CornerStyle.SOFT -> Textos.get(R.string.appearance_suaves)
    }
}
