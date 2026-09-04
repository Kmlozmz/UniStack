package com.unistack.app.feature_grades.presentation

import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import com.unistack.app.feature_user.domain.BadgeShape
import org.junit.Test

/**
 * Las seis formas de distintivo se pueden construir, **todas**.
 *
 * Existe por un fallo concreto: Académico se cerraba de golpe al abrirse, sin registro ni
 * diálogo. La causa era una de estas formas lanzando al construirse, y como se construye
 * mientras se compone una fila de materia, la excepción se llevaba la pantalla entera antes de
 * pintar nada — de ahí que no hubiera rastro.
 *
 * La prueba no mira cómo se ven: mira que **ninguna lance**. Es lo único que hace falta para
 * que el fallo no vuelva, y lo único que se puede comprobar sin un dispositivo.
 */
class SubjectMarkShapeTest {

    /** La misma construcción que usa la fila de materia, para probar lo que se ejecuta. */
    private fun poligono(vertices: Int, innerRatio: Float, rounding: Float): RoundedPolygon =
        RoundedPolygon.star(
            numVerticesPerRadius = vertices,
            radius = 0.5f,
            innerRadius = 0.5f * innerRatio,
            rounding = CornerRounding(rounding),
            centerX = 0.5f,
            centerY = 0.5f
        )

    @Test
    fun `las formas del reparto se construyen`() {
        listOf(
            Triple(9, 0.84f, 0.22f),
            Triple(4, 0.62f, 0.35f),
            Triple(12, 0.80f, 0.12f),
            Triple(8, 0.88f, 0.28f),
            Triple(6, 0.66f, 0.32f),
            Triple(7, 0.78f, 0.20f),
            Triple(5, 0.72f, 0.30f),
            Triple(10, 0.90f, 0.18f)
        ).forEach { (vertices, ratio, rounding) ->
            poligono(vertices, ratio, rounding)
        }
    }

    /**
     * Las seis del ajuste, incluidas las dos que rompían.
     *
     * «Círculo» y «Rombo» pedían un radio interior **igual** al exterior, y ese es el caso que
     * la librería no acepta: una estrella cuyas puntas y valles están a la misma distancia no
     * es una estrella, es un polígono, y hay que pedirlo de otra forma.
     */
    @Test
    fun `las seis formas del ajuste se construyen`() {
        BadgeShape.entries.forEach { forma ->
            formaDeMateria(forma, seed = "calculo-iii")
        }
    }
}
