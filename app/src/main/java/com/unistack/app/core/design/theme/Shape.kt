package com.unistack.app.core.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.Dp
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
        get() = RoundedCornerShape(smallRadius)

    /**
     * Radio de [Small] como medida, no como forma.
     *
     * Sirve para anidar formas concéntricas: un elemento dentro de un contenedor debe usar
     * el radio del contenedor menos el relleno que los separa, o los arcos no encajan y se
     * ve un borde redondo alrededor de otro más cuadrado.
     */
    val smallRadius: Dp
        get() = (AppearanceRuntime.cornerStyle.cardRadius().value - 4f).coerceAtLeast(4f).dp

    /** Radio concéntrico para un hijo separado del contenedor [Small] por [inset]. */
    fun insetFromSmall(inset: Dp): RoundedCornerShape =
        RoundedCornerShape((smallRadius.value - inset.value).coerceAtLeast(2f).dp)

    val Pill = RoundedCornerShape(50)
    val BottomBar
        get() = RoundedCornerShape(AppearanceRuntime.cornerStyle.cardRadius() + 10.dp)
}
