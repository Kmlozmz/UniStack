// design-tokens-exempt: colores categóricos de Trabajos (tipos de material, estados y caras del grupo), iguales a los del artifact aprobado.
package com.unistack.app.feature_rooms.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.unistack.app.core.design.theme.LocalIsDarkTheme
import com.unistack.app.feature_rooms.domain.MaterialType

/**
 * Los tonos del artifact (`--t-*`), cada uno con su cara clara y oscura: el oscuro se lee sobre
 * `#0A0C11` y el claro sobre blanco. Nunca se usa el oscuro en claro —no da el contraste—.
 */
enum class RoomTone(private val dark: Long, private val light: Long) {
    ROJO(0xFFFF6B5E, 0xFFC8321F),
    NARANJA(0xFFFF9A4D, 0xFFB85A00),
    AMBAR(0xFFF2B632, 0xFF8C6800),
    VERDE(0xFF3FD17A, 0xFF0A8A31),
    TURQUESA(0xFF2FD1BE, 0xFF00897B),
    CIAN(0xFF3CC8F0, 0xFF00799C),
    AZUL(0xFF5B9DFF, 0xFF0A63D6),
    INDIGO(0xFF8A93FF, 0xFF4553C8),
    VIOLETA(0xFFB18CFF, 0xFF7446D6),
    ROSA(0xFFF27CC0, 0xFFB8307A),
    GRIS(0xFFA3AEC2, 0xFF5B6678);

    val color: Color
        @Composable @ReadOnlyComposable get() = Color(if (LocalIsDarkTheme.current) dark else light)
}

/** Las caras del grupo: el color de cada integrante, el mismo en toda la sala. */
object MemberColors {
    private val palette = listOf(
        Color(0xFF8B5CF6), Color(0xFFF57C00), Color(0xFF00A6D6), Color(0xFF10B8AC),
        Color(0xFFE84A8A), Color(0xFF7CB342), Color(0xFFF5C542), Color(0xFF4D5BD7),
        Color(0xFFFF6B7A), Color(0xFF58A6FF), Color(0xFF22C55E), Color(0xFF607D8B)
    )
    fun of(index: Int): Color = palette[Math.floorMod(index, palette.size)]
    val onFace: Color = Color(0xFFFFFFFF)
}

/** Cada tipo de material con su color propio (decidido el 23 sep). */
val MaterialType.tone: RoomTone
    get() = when (this) {
        MaterialType.LINK -> RoomTone.AZUL
        MaterialType.DOC -> RoomTone.INDIGO
        MaterialType.PHOTO -> RoomTone.ROSA
        MaterialType.VIDEO -> RoomTone.ROJO
        MaterialType.AUDIO -> RoomTone.NARANJA
        MaterialType.SOURCE -> RoomTone.VERDE
        MaterialType.NOTE -> RoomTone.AMBAR
    }

/** Los logos de formato en «Crear trabajo». */
object FormatColors {
    val word = Color(0xFF2B6CD9)
    val pdf = Color(0xFFD93B30)
    val ppt = Color(0xFFD9652B)
    val gdoc = Color(0xFF3D7BEA)
    val gslides = Color(0xFFE8A800)
    val text = Color(0xFF6B7486)
    val onLogo = Color(0xFFFFFFFF)
    val paper = Color(0xFFFFFFFF)
    val ink = Color(0xFF222222)
    val inkSoft = Color(0xFF777777)
    val qrDark = Color(0xFF111111)
    val rule = Color(0xFFDDDDDD)
    val whatsapp = Color(0xFF25D366)
    val telegram = Color(0xFF2AABEE)
    val mail = Color(0xFFEA4335)
}
