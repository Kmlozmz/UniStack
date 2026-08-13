package com.unistack.app.core.design.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.AccessibilityPreferences
import com.unistack.app.feature_user.domain.CornerStyle
import com.unistack.app.feature_user.domain.InterfaceDensity
import com.unistack.app.feature_user.domain.MotionPreference

val LocalAppearancePreferences = staticCompositionLocalOf { AppearancePreferences.defaults() }
val LocalAccessibilityPreferences = staticCompositionLocalOf { AccessibilityPreferences() }

val LocalMotionDurationScale = staticCompositionLocalOf { 1f }

val LocalInterfaceSpacing = staticCompositionLocalOf { InterfaceSpacing() }

/**
 * Altura que la barra de navegación tapa del contenido, para que cada pantalla la sume al
 * final de su lista y su último elemento pueda subir por encima al desplazarse.
 *
 * Es cero con la barra acoplada, porque ahí el Scaffold ya reserva su hueco y el contenido
 * termina por encima. Solo tiene valor con la barra flotante, que se dibuja *sobre* el
 * contenido: sin este margen, lo último de cada pantalla quedaría debajo para siempre.
 *
 * Existe para que las pantallas dejen de llevar el número a mano. Había 126dp en Inicio,
 * 118dp en dos más y entre 6dp y 20dp en las otras cuatro, que es justo donde se rompía.
 */
val LocalBottomBarOverlay = staticCompositionLocalOf { 0.dp }

/**
 * Margen inferior mínimo para cualquier lista o columna desplazable.
 *
 * Hay aire propio —para que lo último no acabe pegado al borde— más lo que tape la barra
 * flotante si está. Se suma a lo que la pantalla ya reserve por su cuenta, como el hueco de
 * un botón anclado; no lo sustituye.
 *
 * Existe porque la mitad de las pantallas no reservaban nada o se quedaban en 18-40dp, y su
 * último elemento no llegaba a subir lo suficiente para verse entero. La cifra es la misma
 * en todas partes precisamente para que dejen de improvisarla.
 */
val scrollBottomRoom: Dp
    @Composable get() = 28.dp + LocalBottomBarOverlay.current

object AppearanceRuntime {
    var cornerStyle: CornerStyle = CornerStyle.BALANCED
        internal set
}

data class InterfaceSpacing(
    val screenHorizontal: Dp = 20.dp,
    val section: Dp = 16.dp,
    val cardPadding: Dp = 16.dp,
    val controlHeight: Dp = 52.dp
)

internal fun AccessibilityPreferences.motionScale(): Float = when (motionPreference) {
    MotionPreference.FULL -> 1f
    MotionPreference.REDUCED -> 0.55f
    MotionPreference.NONE -> 0f
}

internal fun AppearancePreferences.interfaceSpacing(): InterfaceSpacing = when (interfaceDensity) {
    InterfaceDensity.COMPACT -> InterfaceSpacing(
        screenHorizontal = 16.dp,
        section = 12.dp,
        cardPadding = 12.dp,
        controlHeight = 46.dp
    )
    InterfaceDensity.BALANCED -> InterfaceSpacing()
    InterfaceDensity.COMFORTABLE -> InterfaceSpacing(
        screenHorizontal = 22.dp,
        section = 20.dp,
        cardPadding = 18.dp,
        controlHeight = 56.dp
    )
}

internal fun CornerStyle.cardRadius(): Dp = when (this) {
    CornerStyle.COMPACT -> 8.dp
    CornerStyle.BALANCED -> 16.dp
    CornerStyle.SOFT -> 24.dp
}
