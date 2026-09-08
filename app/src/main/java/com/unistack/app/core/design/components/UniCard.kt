package com.unistack.app.core.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.feature_user.domain.SurfaceStyle
import com.unistack.app.feature_user.domain.ShadowIntensity
import com.unistack.app.feature_user.domain.OutlineWeight
import com.unistack.app.core.design.theme.LocalAccessibilityPreferences
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import androidx.compose.ui.draw.shadow

/**
 * La superficie sobre la que se apoya casi todo en la app.
 *
 * Ahora es un [Surface] de Material y nada más. Antes era una caja que reimplementaba a mano
 * la sombra, el recorte, el borde y el fondo, y que además decidía los cuatro a partir de dos
 * preferencias del usuario —estilo de superficie y estilo de esquinas—, así que cada pantalla
 * nueva había que mirarla en doce combinaciones.
 *
 * Eso se fue con ellas: la forma sale de la escala de formas del tema y la elevación, del
 * propio [Surface], que además tiñe el fondo según la altura como pide Material en lugar de
 * pintar una sombra por debajo.
 */
@Composable
fun UniCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    brush: Brush? = null,
    shape: Shape? = null,
    tonalElevation: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues? = null,
    content: @Composable () -> Unit
) {
    val resolvedShape = shape ?: MaterialTheme.shapes.large
    val resolvedContentPadding = contentPadding ?: PaddingValues(LocalInterfaceSpacing.current.cardPadding)
    val apariencia = LocalAppearancePreferences.current
    /*
     * «Reducir transparencias» gana al estilo de superficie.
     *
     * Es un ajuste de accesibilidad y el otro es de gusto: quien pide que no haya cristal lo
     * pide porque el texto sobre un fondo que se ve por debajo no se le lee, y eso no lo
     * arregla elegir otra superficie en otra pantalla.
     */
    val superficie = if (
        LocalAccessibilityPreferences.current.reduceTransparency &&
        apariencia.surfaceStyle == SurfaceStyle.TRANSLUCENT
    ) {
        SurfaceStyle.OUTLINED
    } else {
        apariencia.surfaceStyle
    }

    /*
     * El estilo de superficie decide aqui, no en cada pantalla.
     *
     * `surfaceStyle` se elegia en Apariencia, se guardaba y viajaba en la copia de seguridad
     * sin cambiar un pixel: «plana» y «con sombra» daban el mismo resultado porque nadie
     * miraba el ajuste. La tarjeta es el sitio donde tiene que mirarse, porque es la pieza que
     * se repite en las once pantallas.
     *
     * Un borde pedido a mano —`borderWidth` puesto por quien llama— manda sobre el estilo: es
     * un borde con intencion, como el de una materia en riesgo, y no la decoracion general.
     */
    val filete = when (apariencia.outlineWeight) {
        OutlineWeight.FINO -> 1.dp
        OutlineWeight.MEDIO -> 1.5.dp
        OutlineWeight.GRUESO -> 2.5.dp
    }
    val anchoDeBorde = when {
        borderWidth > 0.dp -> borderWidth
        superficie == SurfaceStyle.OUTLINED -> filete
        // Cristal: un filete tenue es lo que da el borde del vidrio; sin el, la tarjeta
        // semitransparente se pierde contra el fondo.
        superficie == SurfaceStyle.TRANSLUCENT -> 1.dp
        else -> 0.dp
    }
    val border = if (anchoDeBorde > 0.dp) {
        BorderStroke(
            width = anchoDeBorde,
            color = if (borderColor != Color.Transparent) {
                borderColor
            } else if (superficie == SurfaceStyle.TRANSLUCENT) {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    } else {
        null
    }
    val sombra = if (superficie != SurfaceStyle.ELEVATED) {
        0.dp
    } else {
        when (apariencia.shadowIntensity) {
            ShadowIntensity.SUAVE -> 2.dp
            ShadowIntensity.MEDIA -> 6.dp
            ShadowIntensity.FUERTE -> 12.dp
        }
    }
    val conSombra = if (sombra > 0.dp) modifier.shadow(sombra, resolvedShape) else modifier

    // Con degradado, el Surface va transparente y el pincel se pinta dentro: Surface solo
    // acepta un color liso, y perder el degradado cambiaría lo que dibujan las pantallas
    // que lo piden.
    val painted: @Composable () -> Unit = {
        Box(
            modifier = if (brush != null) Modifier.background(brush) else Modifier
        ) {
            Box(modifier = Modifier.padding(resolvedContentPadding)) { content() }
        }
    }

    val effectiveColor = if (brush != null) {
        Color.Transparent
    } else if (superficie == SurfaceStyle.TRANSLUCENT) {
        color.copy(alpha = 0.72f)
    } else {
        color
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = conSombra,
            enabled = enabled,
            shape = resolvedShape,
            color = effectiveColor,
            tonalElevation = tonalElevation,
            border = border,
            content = painted
        )
    } else {
        Surface(
            modifier = conSombra,
            shape = resolvedShape,
            color = effectiveColor,
            tonalElevation = tonalElevation,
            border = border,
            content = painted
        )
    }
}
