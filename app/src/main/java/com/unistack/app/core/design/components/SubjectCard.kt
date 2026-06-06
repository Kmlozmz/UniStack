package com.unistack.app.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.core.utils.GradingScaleUtils

@Composable
fun SubjectCard(
    name: String,
    average: Double?,
    progress: Float,
    icon: ImageVector,
    accentColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    gradingScale: GradingScale = GradingScale.ZERO_TO_FIVE,
    onClick: () -> Unit = {}
) {
    UniCard(
        modifier = modifier
            .height(102.dp)
            .bounceClick(onClick),
        brush = Brush.linearGradient(
            listOf(
                backgroundColor,
                UniStackColors.GradientEnd
            )
        ),
        shape = AppShapes.MediumCard,
        tonalElevation = 4.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 11.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(29.dp)
                    .background(accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
            Text(
                text = name,
                color = UniStackColors.TextPrimary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                fontSize = 9.sp,
                lineHeight = 10.sp
            )
            Text(
                text = GradingScaleUtils.formatGrade(average, gradingScale),
                color = UniStackColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                lineHeight = 22.sp
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.12f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
