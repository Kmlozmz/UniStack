package com.unistack.app.core.design.shapes

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath

/**
 * Catálogo de polígonos redondeados propios.
 *
 * Equivalen a los presets de `MaterialShapes`, que solo existen en material3 1.5-alpha.
 * Aquí son datos, no API: se definen con [RoundedPolygon], que es estable.
 *
 * Todos se generan centrados en (0.5, 0.5) con radio 0.5 para que [polygonToPath] pueda
 * escalarlos al tamaño del componente sin cálculos adicionales.
 */
object UniStackShapesCatalog {

    private const val CENTER = 0.5f
    private const val RADIUS = 0.5f

    /** Círculo (referencia neutra para morphs). */
    val circle: RoundedPolygon
        get() = RoundedPolygon.circle(numVertices = 12, radius = RADIUS, centerX = CENTER, centerY = CENTER)

    /** Cuadrado de esquinas muy suaves, tipo "squircle". */
    val squircle: RoundedPolygon
        get() = RoundedPolygon(
            numVertices = 4,
            radius = RADIUS,
            centerX = CENTER,
            centerY = CENTER,
            rounding = CornerRounding(radius = 0.32f, smoothing = 1f)
        )

    /** Pentágono redondeado. */
    val pentagon: RoundedPolygon
        get() = RoundedPolygon(
            numVertices = 5,
            radius = RADIUS,
            centerX = CENTER,
            centerY = CENTER,
            rounding = CornerRounding(radius = 0.22f, smoothing = 0.6f)
        )

    /** Hexágono redondeado. */
    val hexagon: RoundedPolygon
        get() = RoundedPolygon(
            numVertices = 6,
            radius = RADIUS,
            centerX = CENTER,
            centerY = CENTER,
            rounding = CornerRounding(radius = 0.2f, smoothing = 0.6f)
        )

    /** Forma tipo "galleta": muchos lóbulos poco profundos. */
    val cookie: RoundedPolygon
        get() = RoundedPolygon.star(
            numVerticesPerRadius = 9,
            radius = RADIUS,
            innerRadius = RADIUS * 0.78f,
            rounding = CornerRounding(radius = 0.18f, smoothing = 1f),
            centerX = CENTER,
            centerY = CENTER
        )

    /** Ráfaga suave: lóbulos más marcados. */
    val softBurst: RoundedPolygon
        get() = RoundedPolygon.star(
            numVerticesPerRadius = 8,
            radius = RADIUS,
            innerRadius = RADIUS * 0.62f,
            rounding = CornerRounding(radius = 0.16f, smoothing = 1f),
            centerX = CENTER,
            centerY = CENTER
        )

    /** Trébol de cuatro lóbulos. */
    val clover: RoundedPolygon
        get() = RoundedPolygon.star(
            numVerticesPerRadius = 4,
            radius = RADIUS,
            innerRadius = RADIUS * 0.68f,
            rounding = CornerRounding(radius = 0.32f, smoothing = 1f),
            centerX = CENTER,
            centerY = CENTER
        )

    /**
     * Secuencia por defecto del indicador de carga: cada vuelta muta a la siguiente forma.
     * Se cierra volviendo a la primera para que el ciclo sea continuo.
     */
    val loadingSequence: List<RoundedPolygon>
        get() = listOf(softBurst, cookie, pentagon, clover, hexagon)
}

/**
 * Convierte un [RoundedPolygon] normalizado (centrado en 0.5, radio 0.5) en un [Path] de
 * Compose escalado a [size] y rotado [rotationDegrees] grados sobre su centro.
 */
fun polygonToPath(
    polygon: RoundedPolygon,
    size: Size,
    rotationDegrees: Float = 0f
): Path {
    val path = polygon.toPath().asComposePath()
    val matrix = Matrix()
    matrix.scale(size.width, size.height)
    if (rotationDegrees != 0f) {
        // El polígono vive en espacio 0..1, así que el centro de giro es (0.5, 0.5).
        matrix.translate(CENTER_NORMALIZED, CENTER_NORMALIZED)
        matrix.rotateZ(rotationDegrees)
        matrix.translate(-CENTER_NORMALIZED, -CENTER_NORMALIZED)
    }
    path.transform(matrix)
    return path
}

/**
 * Igual que [polygonToPath] pero para un [Morph]: [progress] interpola entre la forma
 * inicial y la final (0 = inicial, 1 = final).
 */
fun morphToPath(
    morph: Morph,
    progress: Float,
    size: Size,
    rotationDegrees: Float = 0f
): Path {
    val path = morph.toPath(progress.coerceIn(0f, 1f)).asComposePath()
    val matrix = Matrix()
    matrix.scale(size.width, size.height)
    if (rotationDegrees != 0f) {
        matrix.translate(CENTER_NORMALIZED, CENTER_NORMALIZED)
        matrix.rotateZ(rotationDegrees)
        matrix.translate(-CENTER_NORMALIZED, -CENTER_NORMALIZED)
    }
    path.transform(matrix)
    return path
}

private const val CENTER_NORMALIZED = 0.5f

/**
 * [Shape] de Compose respaldada por un [RoundedPolygon], para usar en `clip()` o
 * `background()` igual que cualquier otra forma.
 */
class PolygonShape(
    private val polygon: RoundedPolygon,
    private val rotationDegrees: Float = 0f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline = Outline.Generic(polygonToPath(polygon, size, rotationDegrees))
}

/**
 * [Shape] que interpola entre dos polígonos. Reconstruye el trazado en cada valor de
 * [progress], así que anímalo desde un `graphicsLayer`/estado y no en un bucle apretado.
 */
class MorphShape(
    private val morph: Morph,
    private val progress: Float,
    private val rotationDegrees: Float = 0f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline = Outline.Generic(morphToPath(morph, progress, size, rotationDegrees))
}
