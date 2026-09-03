@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import com.unistack.app.feature_user.domain.BadgeShape

/**
 * La forma del punto de color de cada materia.
 *
 * Son las formas de Material 3 Expressive, que es lo que las distingue de un círculo con
 * muescas: cada una es un polígono de lóbulos con su propio redondeo, y por eso una galleta de
 * nueve se reconoce al lado de un trébol de cuatro aunque las dos midan veintiséis píxeles.
 *
 * **«Aleatorio» no es una sexta forma.** Reparte las cinco entre las materias sacando la que
 * toca del identificador de cada una: la misma materia se queda siempre con la suya —al
 * reabrir la app, tras un reinicio, en otro teléfono con la copia de seguridad puesta— y dos
 * materias del mismo color dejan de confundirse. Con un azar de verdad, la forma cambiaría en
 * cada recomposición y no serviría para reconocer nada.
 */
private class FormaLobulada(
    private val lobulos: Int,
    private val redondeo: Float
) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val poligono = RoundedPolygon.star(
            numVerticesPerRadius = lobulos,
            radius = 1f,
            innerRadius = 1f - redondeo,
            rounding = CornerRounding(redondeo),
            innerRounding = CornerRounding(redondeo)
        )
        val camino: Path = poligono.toPath().asComposePath()
        val lado = size.minDimension / 2f
        val matriz = androidx.compose.ui.graphics.Matrix().apply {
            translate(size.width / 2f, size.height / 2f)
            scale(lado, lado)
        }
        camino.transform(matriz)
        return Outline.Generic(camino)
    }
}

private val Galleta = FormaLobulada(lobulos = 9, redondeo = 0.16f)
private val Trebol = FormaLobulada(lobulos = 4, redondeo = 0.26f)
private val Sol = FormaLobulada(lobulos = 12, redondeo = 0.14f)
private val Rombo = CutCornerShape(percent = 50)

/** Las cinco que se reparten con «Aleatorio», en orden fijo. */
private val Reparto = listOf(BadgeShape.CIRCULO, BadgeShape.GALLETA, BadgeShape.TREBOL, BadgeShape.SOL, BadgeShape.ROMBO)

/**
 * La forma que toca a esta materia.
 *
 * @param id el identificador de la materia. Solo cuenta con [BadgeShape.ALEATORIO], y de él
 *   sale —por su `hashCode`— cuál de las cinco le corresponde, siempre la misma.
 */
fun formaDeDistintivo(estilo: BadgeShape, id: String): Shape {
    val elegida = if (estilo != BadgeShape.ALEATORIO) {
        estilo
    } else {
        Reparto[Math.floorMod(id.hashCode(), Reparto.size)]
    }
    return when (elegida) {
        BadgeShape.CIRCULO -> CircleShape
        BadgeShape.GALLETA -> Galleta
        BadgeShape.TREBOL -> Trebol
        BadgeShape.SOL -> Sol
        BadgeShape.ROMBO -> Rombo
        // Inalcanzable: el reparto solo devuelve las cinco de arriba.
        BadgeShape.ALEATORIO -> CircleShape
    }
}

/** La misma forma, leyendo el ajuste del tema en vez de recibirlo. */
@Composable
@ReadOnlyComposable
fun formaDeDistintivo(id: String): Shape =
    formaDeDistintivo(LocalAppearancePreferences.current.badgeShape, id)
