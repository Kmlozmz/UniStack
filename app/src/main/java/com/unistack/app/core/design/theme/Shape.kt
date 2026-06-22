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
    val LargeCard
        get() = RoundedCornerShape(AppearanceRuntime.cornerStyle.cardRadius() + 8.dp)
    val MediumCard
        get() = RoundedCornerShape(AppearanceRuntime.cornerStyle.cardRadius() + 4.dp)
    val SmallCard
        get() = RoundedCornerShape(AppearanceRuntime.cornerStyle.cardRadius())
    val Pill = RoundedCornerShape(50)
    val BottomBar
        get() = RoundedCornerShape(AppearanceRuntime.cornerStyle.cardRadius() + 10.dp)
}
