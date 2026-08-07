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

/**
 * Radios de la app, todos derivados de la preferencia `cornerStyle` del usuario.
 *
 * Usa siempre estos tokens en lugar de `RoundedCornerShape(n.dp)`: un radio fijo ignora
 * el ajuste de esquinas de Ajustes, igual que un color fijo ignora el tema.
 *
 * Valores resultantes (compacto / equilibrado / suave):
 *  - [Small]      4 / 12 / 20   controles pequeños, campos, chips
 *  - [SmallCard]  8 / 16 / 24   tarjetas de lista
 *  - [MediumCard] 12 / 20 / 28  tarjetas destacadas
 *  - [LargeCard]  16 / 24 / 32  contenedores grandes y diálogos
 */
object AppShapes {
    val LargeCard
        get() = RoundedCornerShape(AppearanceRuntime.cornerStyle.cardRadius() + 8.dp)
    val MediumCard
        get() = RoundedCornerShape(AppearanceRuntime.cornerStyle.cardRadius() + 4.dp)
    val SmallCard
        get() = RoundedCornerShape(AppearanceRuntime.cornerStyle.cardRadius())

    /** El radio más usado de la app; no tenía token y se escribía suelto como 12.dp. */
    val Small
        get() = RoundedCornerShape(
            (AppearanceRuntime.cornerStyle.cardRadius().value - 4f).coerceAtLeast(4f).dp
        )

    val Pill = RoundedCornerShape(50)
    val BottomBar
        get() = RoundedCornerShape(AppearanceRuntime.cornerStyle.cardRadius() + 10.dp)
}
