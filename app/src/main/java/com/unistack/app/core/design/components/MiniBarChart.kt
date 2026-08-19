package com.unistack.app.core.design.components

import com.unistack.app.core.utils.DayLabels

import com.unistack.app.core.design.theme.AppShapes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
@Composable
fun MiniBarChart(
    values: List<Int>,
    modifier: Modifier = Modifier
) {
    val labels = DayLabels.short
    val max = values.maxOrNull()?.coerceAtLeast(1) ?: 1

    Row(
        modifier = modifier.height(60.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom
    ) {
        values.take(7).forEachIndexed { index, value ->
            val normalized = value / max.toFloat()
            val barHeight = (16 + normalized * 28).dp
            Column(
                modifier = Modifier.width(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(44.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .width(9.dp)
                            .height(barHeight)
                            .clip(AppShapes.Small)
                            .background(
                                SolidColor(MaterialTheme.colorScheme.primary)
                            )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = labels.getOrElse(index) { "" },
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 9.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
