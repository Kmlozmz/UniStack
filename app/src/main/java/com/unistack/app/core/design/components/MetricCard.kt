package com.unistack.app.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors

@Composable
fun MetricCard(
    value: String,
    label: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier.height(62.dp),
        shape = AppShapes.SmallCard,
        tonalElevation = 4.dp,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(iconColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    color = UniStackColors.TextPrimary,
                    fontSize = 23.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = label,
                    color = UniStackColors.TextPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    lineHeight = 9.sp,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}
