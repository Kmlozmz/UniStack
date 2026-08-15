package com.unistack.app.feature_home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
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
/**
 * El color del hero, que ya no es el acento.
 *
 * Se pintaba del acento a toda saturación, así que su aspecto cambiaba con cada tema: con uno
 * claro el bloque gritaba más que el resto de la pantalla, y con uno oscuro el texto encima
 * dejaba de leerse. Y no hay un acento que quede bien de las dos maneras.
 *
 * Ahora tiene su propia superficie —un grafito con una gota del acento, que lo emparenta con
 * el tema sin depender de lo saturado que sea— y el acento aparece solo en el botón y en el
 * rótulo, que es donde de verdad hace falta que resalte.
 */
internal val HeroSurface: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) {
        // A oscuras, algo por encima de la tarjeta: la separa del fondo sin encenderse.
        UniStackColors.Primary.copy(alpha = 0.14f).compositeOver(UniStackColors.Card)
    } else {
        // En claro se tiñe, no se oscurece. Un bloque de grafito sobre una pantalla blanca es
        // un agujero en mitad de la página, y era lo que pasaba antes.
        UniStackColors.Primary.copy(alpha = 0.13f).compositeOver(UniStackColors.Card)
    }

/** Lo que va encima: se calcula, para que el texto se lea sobre la superficie que salga. */
internal val HeroContent: Color
    @Composable get() = UniStackColors.contentColorOn(HeroSurface)

internal val HeroBrush: Brush
    @Composable get() = SolidColor(HeroSurface)

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
/*
 * Todo lo que va encima del hero se calcula sobre el hero.
 *
 * Salía de `OnPrimary`, que es el color que contrasta con el **acento**: mientras el hero se
 * pintaba del acento eso era correcto, y dejó de serlo en cuanto el hero tuvo superficie propia.
 * Con un acento claro, `OnPrimary` es tinta oscura, así que quedaba texto oscuro sobre un
 * grafito oscuro: ilegible en los dos temas.
 */
internal val HomeHeroTitle: Color
    @Composable get() = HeroContent
internal val HomeHeroSecondary: Color
    @Composable get() = HeroContent.copy(alpha = 0.76f)
internal val HomeHeroLabel: Color
    @Composable get() = HeroContent.copy(alpha = 0.84f)
internal val HomeHeroStar: Color
    @Composable get() = HeroContent
internal val HomeHeroStarSoft: Color
    @Composable get() = HeroContent.copy(alpha = 0.66f)

/** Los círculos decorativos: la misma tinta del contenido, apenas insinuada. */
internal val HomeHeroOrnament: Color
    @Composable get() = HeroContent.copy(alpha = 0.07f)

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
