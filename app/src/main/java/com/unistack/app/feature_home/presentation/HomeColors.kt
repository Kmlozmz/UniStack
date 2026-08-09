package com.unistack.app.feature_home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import com.unistack.app.core.design.theme.UniStackColors

/**
 * Tokens de color de la pantalla de inicio.
 *
 * Todos delegan en [UniStackColors]: antes este archivo era una paleta paralela con medio
 * centenar de hex fijos, y por eso Inicio era la única zona que no reaccionaba al tema del
 * usuario ni al color dinámico del sistema.
 *
 * Se conservan los nombres para no tocar los puntos de uso, y todos son `@Composable get()`
 * para que se lean dentro de la composición y recompongan al cambiar el tema.
 */

internal val HomeBackgroundBrush: Brush
    @Composable get() = SolidColor(UniStackColors.Background)

/**
 * El hero es una superficie rellena con el acento, no su contenedor claro.
 *
 * Pasa de `PrimaryLight` a `Primary` para que la tarjeta sea el elemento con más peso de la
 * pantalla, que es lo que pide ser: contiene la única acción sugerida. El contenido va en
 * `OnPrimary`, que se calcula por contraste, así que en un tema donde el acento sea claro la
 * tinta saldrá oscura sola en vez de quedarse en blanco ilegible.
 */
internal val HeroBrush: Brush
    @Composable get() = SolidColor(UniStackColors.Primary)

internal val HomeCard: Color
    @Composable get() = UniStackColors.Card

/**
 * Fondo de las casillas del tablero: una superficie neutra, no teñida del acento.
 *
 * Antes cada casilla se pintaba con su propio color al 7%, así que la rejilla salía a
 * cuatro tintes distintos y ninguna cifra destacaba. El color se queda solo en el chip del
 * icono, que es donde distingue de un vistazo, y el fondo pasa a ser el mismo en las cuatro.
 */
internal val HomeSnapshotTile: Color
    @Composable get() = UniStackColors.SurfaceVariant
internal val HomeText: Color
    @Composable get() = UniStackColors.TextPrimary
internal val HomeSoftText: Color
    @Composable get() = UniStackColors.TextSecondary
internal val HomeMuted: Color
    @Composable get() = UniStackColors.TextSecondary
internal val HomeBorder: Color
    @Composable get() = UniStackColors.SoftOutline
internal val HomePurple: Color
    @Composable get() = UniStackColors.Primary
internal val HomeTeal: Color
    @Composable get() = UniStackColors.Teal
internal val HomeCoral: Color
    @Composable get() = UniStackColors.Coral
internal val HomeYellow: Color
    @Composable get() = UniStackColors.Yellow

internal val HomeBgTop: Color
    @Composable get() = UniStackColors.Background
internal val HomeBgMid: Color
    @Composable get() = UniStackColors.Background
internal val HomeBgBottom: Color
    @Composable get() = UniStackColors.Background
internal val HomeCardDark: Color
    @Composable get() = UniStackColors.Card

internal val HomeHeroStart: Color
    @Composable get() = UniStackColors.PrimaryLight
internal val HomeHeroMid: Color
    @Composable get() = UniStackColors.PrimaryLight
internal val HomeHeroEnd: Color
    @Composable get() = UniStackColors.PrimaryLight
internal val HomeHeroTransition: Color
    @Composable get() = UniStackColors.PrimaryLight
internal val HomeHeroVioletDepth: Color
    @Composable get() = UniStackColors.Primary
internal val HomeHeroVioletWash: Color
    @Composable get() = UniStackColors.Primary
internal val HomeHeroLightViolet: Color
    @Composable get() = UniStackColors.Primary

// Todo lo que va encima del hero se deriva de OnPrimary, nunca de los textos de la pantalla:
// el fondo ya no es el de la pantalla, así que TextPrimary podía quedar ilegible encima.
internal val HomeHeroTitle: Color
    @Composable get() = UniStackColors.OnPrimary
internal val HomeHeroSecondary: Color
    @Composable get() = UniStackColors.OnPrimary.copy(alpha = 0.78f)
internal val HomeHeroLabel: Color
    @Composable get() = UniStackColors.OnPrimary.copy(alpha = 0.86f)
internal val HomeHeroStar: Color
    @Composable get() = UniStackColors.OnPrimary
internal val HomeHeroStarSoft: Color
    @Composable get() = UniStackColors.OnPrimary.copy(alpha = 0.68f)

/** Los círculos decorativos: la misma tinta del contenido, apenas insinuada. */
internal val HomeHeroOrnament: Color
    @Composable get() = UniStackColors.OnPrimary.copy(alpha = 0.10f)

/** Sin borde: una superficie rellena no necesita contorno para separarse del fondo. */
internal val HomeHeroStroke: Color
    get() = Color.Transparent
internal val HomeHeroAssetShadow: Color
    @Composable get() = UniStackColors.Primary.copy(alpha = 0.32f)
internal val HomeHeroButtonStart: Color
    @Composable get() = UniStackColors.Primary
internal val HomeHeroButtonEnd: Color
    @Composable get() = UniStackColors.Primary

internal val HomeAccentPurple: Color
    @Composable get() = UniStackColors.Primary
internal val HomeAvatarPurpleTop: Color
    @Composable get() = UniStackColors.Primary
internal val HomeAvatarPurpleBottom: Color
    @Composable get() = UniStackColors.Primary
internal val HomeAccentTeal: Color
    @Composable get() = UniStackColors.Teal
internal val HomeAccentCoral: Color
    @Composable get() = UniStackColors.Coral
internal val HomeAccentYellow: Color
    @Composable get() = UniStackColors.Yellow

internal val HomeTextPrimary: Color
    @Composable get() = UniStackColors.TextPrimary
internal val HomeTextSoft: Color
    @Composable get() = UniStackColors.TextSecondary
internal val HomeTextMuted: Color
    @Composable get() = UniStackColors.TextSecondary
internal val HomeStroke: Color
    @Composable get() = UniStackColors.SoftOutline

/** Contenido sobre el acento: se calcula, nunca se asume blanco. */
internal val HomeHeroLight: Color
    @Composable get() = UniStackColors.OnPrimary

/** design-tokens-ok: las sombras son negras por física, no por marca. */
internal val HomeShadow = Color.Black

internal val HomeHeroLightModeStart: Color
    @Composable get() = UniStackColors.PrimaryLight
internal val HomeHeroLightModeMid: Color
    @Composable get() = UniStackColors.PrimaryLight
internal val HomeHeroLightModeTransition: Color
    @Composable get() = UniStackColors.PrimaryLight
internal val HomeHeroLightModeEnd: Color
    @Composable get() = UniStackColors.PrimaryLight
internal val HomeHeroLightModeGlow: Color
    @Composable get() = UniStackColors.Primary.copy(alpha = 0.42f)
internal val HomeHeroLightModeAccent: Color
    @Composable get() = UniStackColors.Primary.copy(alpha = 0.62f)
internal val HomeHeroLightModeDepth: Color
    @Composable get() = UniStackColors.Primary
internal val HomeCompanionHeart: Color
    @Composable get() = UniStackColors.Primary

internal val HomePrioritySheetSurface: Color
    @Composable get() = UniStackColors.Background
internal val HomePrioritySheetSuggestion: Color
    @Composable get() = UniStackColors.SurfaceVariant
internal val HomePrioritySheetText: Color
    @Composable get() = UniStackColors.TextPrimary
internal val HomePrioritySheetBody: Color
    @Composable get() = UniStackColors.TextSecondary
internal val HomePrioritySheetMuted: Color
    @Composable get() = UniStackColors.TextSecondary
internal val HomePrioritySheetSecondaryButton: Color
    @Composable get() = UniStackColors.SurfaceVariant
internal val HomePrioritySheetIconCircle: Color
    @Composable get() = UniStackColors.PrimaryLight
internal val HomePrioritySheetCardBorder: Color
    @Composable get() = UniStackColors.SoftOutline
internal val HomePrioritySheetSun: Color
    @Composable get() = UniStackColors.Yellow
internal val HomePrioritySheetAccentSoft: Color
    @Composable get() = UniStackColors.Primary
