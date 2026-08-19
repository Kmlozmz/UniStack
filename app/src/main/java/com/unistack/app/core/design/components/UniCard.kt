package com.unistack.app.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_user.domain.SurfaceStyle

import androidx.compose.material3.MaterialTheme
@Composable
fun UniCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    brush: Brush? = null,
    shape: Shape? = null,
    tonalElevation: Dp = 6.dp,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues? = null,
    content: @Composable () -> Unit
) {
    val appearance = LocalAppearancePreferences.current
    val resolvedContentPadding = contentPadding ?: PaddingValues(LocalInterfaceSpacing.current.cardPadding)
    val resolvedShape = shape ?: androidx.compose.foundation.shape.RoundedCornerShape(
        when (appearance.cornerStyle) {
            com.unistack.app.feature_user.domain.CornerStyle.COMPACT -> 8.dp
            com.unistack.app.feature_user.domain.CornerStyle.BALANCED -> 16.dp
            com.unistack.app.feature_user.domain.CornerStyle.SOFT -> 24.dp
        }
    )
    val resolvedElevation = when (appearance.surfaceStyle) {
        SurfaceStyle.FLAT, SurfaceStyle.OUTLINED -> if (tonalElevation == 6.dp) 0.dp else tonalElevation
        SurfaceStyle.ELEVATED -> tonalElevation
        SurfaceStyle.TRANSLUCENT -> if (tonalElevation == 6.dp) 2.dp else tonalElevation
    }
    val resolvedBorderWidth = when {
        borderWidth > 0.dp -> borderWidth
        appearance.surfaceStyle == SurfaceStyle.OUTLINED -> 0.7.dp
        else -> 0.dp
    }
    val resolvedBorderColor = if (borderColor != Color.Transparent) {
        borderColor
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    }
    val backgroundModifier = if (brush != null) {
        Modifier.background(brush)
    } else {
        Modifier.background(
            if (appearance.surfaceStyle == SurfaceStyle.TRANSLUCENT) color.copy(alpha = 0.90f) else color
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = resolvedElevation,
                shape = resolvedShape,
                // design-tokens-ok: las sombras son negro translúcido por física, no por marca
                ambientColor = Color(0x14000000),
                // design-tokens-ok: idem
                spotColor = Color(0x10000000)
            )
            .clip(resolvedShape)
            .then(backgroundModifier)
            .then(
                if (onClick != null) {
                    Modifier.clickable(enabled = enabled, onClick = onClick)
                } else {
                    Modifier
                }
            )
            .then(
                if (resolvedBorderWidth > 0.dp) {
                    Modifier.border(resolvedBorderWidth, resolvedBorderColor, resolvedShape)
                } else {
                    Modifier
                }
            )
            .padding(resolvedContentPadding)
    ) {
        content()
    }
}
