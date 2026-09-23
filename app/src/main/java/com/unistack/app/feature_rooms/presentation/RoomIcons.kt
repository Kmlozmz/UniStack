// design-tokens-exempt: el trazo de los iconos se pinta en negro y el Icon lo tiñe; no es un color de la interfaz.

package com.unistack.app.feature_rooms.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Los iconos de línea de la barra de la sala (juego «E», elegido el 23 sep): chat, novedades y
 * grupo con trazo de 2 y puntas redondas, como en el artifact.
 */
object RoomIcons {
    private fun line(name: String, vararg paths: String): ImageVector {
        val b = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        paths.forEach { d ->
            b.addPath(
                pathData = addPathNodes(d),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            )
        }
        return b.build()
    }

    val Chat: ImageVector by lazy {
        line("rooms.chat", "M7.9 20A9 9 0 1 0 4 16.1L2 22Z", "M8 12h.01M12 12h.01M16 12h.01")
    }
    val Bell: ImageVector by lazy {
        line("rooms.bell", "M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9", "M10.3 21a1.94 1.94 0 0 0 3.4 0")
    }
    val People: ImageVector by lazy {
        line(
            "rooms.people",
            "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2",
            "M13 7a4 4 0 1 1-8 0a4 4 0 1 1 8 0",
            "M22 21v-2a4 4 0 0 0-3-3.87",
            "M16 3.13a4 4 0 0 1 0 7.75"
        )
    }
}
