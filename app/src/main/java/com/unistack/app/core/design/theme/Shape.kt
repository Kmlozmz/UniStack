package com.unistack.app.core.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val UniStackShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

object AppShapes {
    val LargeCard = RoundedCornerShape(28.dp)
    val MediumCard = RoundedCornerShape(24.dp)
    val SmallCard = RoundedCornerShape(18.dp)
    val Pill = RoundedCornerShape(50)
    val BottomBar = RoundedCornerShape(32.dp)
}
