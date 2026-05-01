package com.unistack.app.core.design.components

import androidx.compose.foundation.background
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
import com.unistack.app.core.design.theme.UniStackColors

@Composable
fun UniCard(
    modifier: Modifier = Modifier,
    color: Color = UniStackColors.Card,
    brush: Brush? = null,
    shape: Shape = AppShapes.MediumCard,
    tonalElevation: Dp = 6.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    val backgroundModifier = if (brush != null) {
        Modifier.background(brush)
    } else {
        Modifier.background(color)
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = tonalElevation,
                shape = shape,
                ambientColor = Color(0x14000000),
                spotColor = Color(0x10000000)
            )
            .clip(shape)
            .then(backgroundModifier)
            .padding(contentPadding)
    ) {
        content()
    }
}
