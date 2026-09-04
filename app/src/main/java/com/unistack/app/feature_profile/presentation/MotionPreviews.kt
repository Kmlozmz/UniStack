@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.unistack.app.core.design.theme.LocalSectionColors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Las ciento once variantes, animadas y a la vez.
 *
 * **Esto no es adorno de la pantalla de ajustes: es la pantalla.** Elegir entre «Trazo» y
 * «Barrido» leyendo dos palabras es adivinar; el nombre no dice qué va a pasar cuando marques
 * asistencia. Cada variante se pinta corriendo en bucle, así que se comparan de un vistazo y
 * se elige la que se ve, no la que suena mejor.
 *
 * Todas comparten el mismo lienzo normalizado —cien de ancho por sesenta y cuatro de alto— y
 * de ahí sale que se puedan poner en rejilla sin que unas se vean grandes y otras diminutas.
 * Dentro se dibuja con [DrawScope] y nada más: sin composables anidados, que en una rejilla de
 * noventa cajas animando son noventa recomposiciones por fotograma.
 */

private const val ANCHO = 100f
private const val ALTO = 64f

/**
 * Un reloj de 0 a 1 que vuelve a empezar.
 *
 * Todas las vistas previas cuelgan de él y no de un `animate*AsState` por variante: son
 * ciento once cajas, y ciento once animaciones con su propio arranque no llegarían nunca a
 * verse en fase.
 */
@Composable
internal fun bucle(duracionMs: Int, etiqueta: String = "bucle", desfase: Float = 0f): Float {
    val transicion = rememberInfiniteTransition(label = etiqueta)
    val valor by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(duracionMs, easing = LinearEasing)),
        label = etiqueta
    )
    /*
     * **El desfase es lo que evita que las cuatro cajas salgan vacias a la vez.**
     *
     * Todas se componen en el mismo fotograma y con la misma duracion, asi que sin esto van en
     * fase perfecta: en el instante en que la animacion esta en su tramo vacio —el resumen del
     * semestre antes de armarse, la vibracion entre golpe y golpe— **las cuatro** se ven en
     * blanco a la vez, y parece que el ajuste esta roto.
     *
     * El desfase es pequeno a proposito: lo justo para que nunca coincidan todas, sin llegar a
     * romper la comparacion, que es para lo que estan una al lado de la otra.
     */
    return (valor + desfase) % 1f
}

/** El mismo reloj, de ida y vuelta: sirve para lo que respira o late. */
@Composable
internal fun vaiven(duracionMs: Int, etiqueta: String = "vaiven"): Float {
    val transicion = rememberInfiniteTransition(label = etiqueta)
    val valor by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(duracionMs, easing = LinearEasing), RepeatMode.Reverse),
        label = etiqueta
    )
    return valor
}

/** Suaviza el arranque y la llegada, que es lo que separa un movimiento de un salto. */
private fun suave(t: Float): Float = t * t * (3f - 2f * t)

/** Un muelle que se pasa de largo y vuelve. */
private fun muelle(t: Float, rebote: Float = 0.35f): Float {
    if (t >= 1f) return 1f
    return 1f - exp(-6f * t) * cos(t * PI.toFloat() * (2f + rebote * 4f))
}

/** El tramo [desde, hasta] del reloj, devuelto otra vez como 0..1. */
private fun tramo(t: Float, desde: Float, hasta: Float): Float =
    ((t - desde) / (hasta - desde)).coerceIn(0f, 1f)

/** Colores del lienzo: los del tema, para que la vista previa no mienta. */
internal data class TintaDemo(
    val fondo: Color,
    val pieza: Color,
    val acento: Color,
    val tinta: Color,
    /**
     * Con que escribir dentro de un dibujo.
     *
     * **Es lo que separa un dibujo ilustrativo de una mancha que se mueve.** Sin texto, «Nota
     * que sube» son dos circulos y un numero imaginario; con el, es un 3,8 que pasa a 4,2 y se
     * entiende sin leer el nombre de la variante. `DrawScope` no sabe escribir por su cuenta:
     * hace falta un [TextMeasurer], y por eso viaja aqui junto a los colores.
     */
    val medidor: TextMeasurer,
    val verde: Color,
    /** El de peligro: algo esta mal y hay que arreglarlo. */
    val rojo: Color,
    /** El de aviso: vas justo, mira esto. */
    val ambar: Color,
    /** El de Gastos, que es identidad de seccion y no alarma. */
    val gasto: Color
)

@Composable
internal fun tintaDemo(): TintaDemo {
    val esquema = MaterialTheme.colorScheme
    val secciones = LocalSectionColors.current
    val medidor = rememberTextMeasurer()
    return TintaDemo(
        medidor = medidor,
        // El fondo sobre el que se dibuja es el de la caja de variante, no otro: si no
        // coinciden, lo que se pinta «en blanco» dentro del dibujo sale de un tono distinto al
        // de la caja y se ve un recorte.
        fondo = esquema.surfaceContainerHigh,
        pieza = esquema.onSurface.copy(alpha = 0.16f),
        acento = esquema.primary,
        tinta = esquema.onSurface,
        verde = secciones.onTrack,
        /*
         * **Estaban cambiados**, y por eso todo lo de peligro salia amarillo.
         *
         * En esta app `atRisk` es el ambar de «vas justo» y `expenses` es el rojo de Gastos,
         * que es identidad de seccion y no alarma. Yo los habia mapeado al reves: el aviso de
         * error y el de presupuesto pintaban en ambar, que es exactamente el color que dice
         * «cuidado» en vez de «esto esta mal».
         *
         * El rojo de peligro es el `error` del esquema, que es el unico que significa eso.
         */
        rojo = esquema.error,
        ambar = secciones.atRisk,
        gasto = secciones.expenses
    )
}

/**
 * La caja de una variante: el lienzo normalizado y el dibujo dentro.
 *
 * @param dibujo recibe el reloj ya en 0..1 y pinta en coordenadas de cien por sesenta y cuatro.
 */
@Composable
internal fun LienzoDemo(
    t: Float,
    modifier: Modifier = Modifier,
    dibujo: DrawScope.(Float) -> Unit
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val escalaX = size.width / ANCHO
        val escalaY = size.height / ALTO
        scale(scaleX = escalaX, scaleY = escalaY, pivot = Offset.Zero) {
            dibujo(t)
        }
    }
}

// ---------------------------------------------------------------------- piezas comunes

/**
 * Escribe dentro del dibujo, en coordenadas del lienzo de cien por sesenta y cuatro.
 *
 * El tamano llega en unidades del lienzo y se convierte a `sp` con la escala que ya lleva el
 * `DrawScope`, asi que un texto de ocho mide lo mismo en una caja de variante que en un demo
 * grande. Sin esa conversion, el mismo numero saldria enorme en una y microscopico en la otra.
 *
 * @param centrado mide el texto y lo desplaza media anchura: es lo que hace falta para poner
 *   una cifra en el centro de un circulo sin ir ajustando la `x` a ojo.
 */
internal fun DrawScope.texto(
    valor: String,
    x: Float,
    y: Float,
    tinta: TintaDemo,
    color: Color,
    tamano: Float = 9f,
    negrita: Boolean = true,
    centrado: Boolean = false,
    alfa: Float = 1f
) {
    if (alfa <= 0.02f) return
    val estilo = TextStyle(
        color = color.copy(alpha = color.alpha * alfa),
        fontSize = (tamano / density).sp,
        fontWeight = if (negrita) FontWeight.ExtraBold else FontWeight.Normal
    )
    val medida = tinta.medidor.measure(valor, estilo)
    val ancho = medida.size.width / density
    val alto = medida.size.height / density
    drawText(
        textLayoutResult = medida,
        topLeft = Offset(if (centrado) x - ancho / 2f else x, y - alto / 2f)
    )
}

/**
 * Una fila de la app: su marca de color, el título y la línea de apoyo.
 *
 * **No es un rectángulo.** Estuvo siéndolo, y con eso las ciento treinta cajas se veían como
 * barras grises moviéndose: no se entendía que lo que entra escalonado es *una tarea*, ni que
 * lo que se tacha es *una materia*. Con la marca a la izquierda y dos renglones de distinto
 * largo dentro, la caja de dos centímetros ya se lee como una fila de Tareas.
 */
private fun DrawScope.fila(
    y: Float,
    color: Color,
    x: Float = 12f,
    ancho: Float = 76f,
    alto: Float = 11f,
    alfa: Float = 1f
) {
    val a = alfa
    // El fondo de la fila: el tono de tarjeta, no el color del gesto.
    drawRoundRect(
        color = color.copy(alpha = color.alpha * a * 0.18f),
        topLeft = Offset(x, y),
        size = Size(ancho, alto),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.5f, 3.5f)
    )
    // La marca de la materia, a la izquierda, como en la lista de verdad.
    val marca = alto * 0.62f
    drawCircle(
        color = color.copy(alpha = color.alpha * a),
        radius = marca / 2f,
        center = Offset(x + 3f + marca / 2f, y + alto / 2f)
    )
    val textoX = x + 4f + marca + 2f
    val libre = (x + ancho) - textoX - 3f
    if (libre <= 2f) return
    // Título y línea de apoyo: dos largos distintos es lo que la hace leerse como texto.
    drawRoundRect(
        color = color.copy(alpha = color.alpha * a * 0.95f),
        topLeft = Offset(textoX, y + alto * 0.24f),
        size = Size(libre * 0.72f, alto * 0.20f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f, 1.5f)
    )
    drawRoundRect(
        color = color.copy(alpha = color.alpha * a * 0.45f),
        topLeft = Offset(textoX, y + alto * 0.58f),
        size = Size(libre * 0.44f, alto * 0.18f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f, 1.5f)
    )
}

/**
 * Una pantalla de la app en miniatura, **con su nombre escrito**.
 *
 * Como rectangulo liso, «eje» y «zoom» eran dos bloques de color deslizandose y no se sabia
 * cual era la que salia y cual la que entraba. Con «Materias» y «Cálculo III» escritos en la
 * cabecera se lee de un vistazo que una pantalla deja paso a otra, que es de lo que va el
 * ajuste.
 */
private fun DrawScope.panel(
    x: Float,
    color: Color,
    titulo: String,
    tinta: TintaDemo,
    alfa: Float = 1f,
    escala: Float = 1f
) {
    val ancho = 74f * escala
    val alto = 46f * escala
    val izq = x + (74f - ancho) / 2f
    val arriba = 9f + (46f - alto) / 2f
    val a = alfa
    drawRoundRect(
        color = color.copy(alpha = color.alpha * a * 0.20f),
        topLeft = Offset(izq, arriba),
        size = Size(ancho, alto),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f, 7f)
    )
    /*
     * El titulo se apaga **antes** que su panel.
     *
     * Con las dos pantallas encima —fundido, contenedor, zoom— los dos titulos quedaban
     * legibles a la vez y se leia «MateriasCálculo III» encima de si mismo. Elevando la
     * opacidad al cubo, el que se va desaparece pronto y el que llega tarda en aparecer: en
     * ningun momento hay dos textos compitiendo por el mismo sitio.
     */
    texto(
        titulo,
        izq + ancho * 0.10f,
        arriba + alto * 0.17f,
        tinta,
        color.copy(alpha = a * a * a),
        tamano = 7.5f * escala
    )
    // Dos filas dentro, como cualquier lista de la app.
    repeat(2) { indice ->
        val y = arriba + alto * (0.40f + indice * 0.26f)
        drawRoundRect(
            color = color.copy(alpha = color.alpha * a * 0.35f),
            topLeft = Offset(izq + ancho * 0.10f, y),
            size = Size(ancho * 0.80f, alto * 0.17f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f, 2.5f)
        )
        drawCircle(
            color = color.copy(alpha = color.alpha * a * 0.9f),
            radius = alto * 0.055f,
            center = Offset(izq + ancho * 0.16f, y + alto * 0.085f)
        )
    }
}

/** El visto de asistencia, dibujado por tramos para poder pintarlo a medias. */
private fun DrawScope.visto(centro: Offset, radio: Float, avance: Float, color: Color, grosor: Float = 3f) {
    val puntos = listOf(
        centro + Offset(-radio * 0.5f, 0f),
        centro + Offset(-radio * 0.12f, radio * 0.38f),
        centro + Offset(radio * 0.55f, -radio * 0.42f)
    )
    val largoA = (puntos[1] - puntos[0]).getDistance()
    val largoB = (puntos[2] - puntos[1]).getDistance()
    val total = largoA + largoB
    val recorrido = total * avance.coerceIn(0f, 1f)
    if (recorrido <= 0f) return
    val primero = min(recorrido, largoA)
    drawLine(
        color = color,
        start = puntos[0],
        end = puntos[0] + (puntos[1] - puntos[0]) * (primero / largoA),
        strokeWidth = grosor,
        cap = androidx.compose.ui.graphics.StrokeCap.Round
    )
    if (recorrido > largoA) {
        drawLine(
            color = color,
            start = puntos[1],
            end = puntos[1] + (puntos[2] - puntos[1]) * ((recorrido - largoA) / largoB),
            strokeWidth = grosor,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

/**
 * La onda circular de M3E: un arco cuyo **radio** ondula.
 *
 * No es una línea recta doblada, y por eso hay que calcular el radio punto a punto: doblar una
 * onda ya hecha deja las crestas mirando en la dirección equivocada según por dónde vaya el
 * arco.
 */
private fun ondaCircular(
    centro: Offset,
    radio: Float,
    amplitud: Float,
    lobulos: Int,
    desde: Float,
    hasta: Float,
    fase: Float
): Path {
    val camino = Path()
    val pasos = 80
    for (paso in 0..pasos) {
        val fraccion = paso / pasos.toFloat()
        val angulo = (desde + (hasta - desde) * fraccion) * 2f * PI.toFloat()
        val r = radio + amplitud * sin(lobulos * angulo + fase)
        val punto = Offset(centro.x + r * cos(angulo), centro.y + r * sin(angulo))
        if (paso == 0) camino.moveTo(punto.x, punto.y) else camino.lineTo(punto.x, punto.y)
    }
    return camino
}

/** El polígono de N lóbulos del `LoadingIndicator`, que es lo que le da su forma cambiante. */
private fun formaLobulada(centro: Offset, radio: Float, lobulos: Int, redondez: Float, giro: Float): Path {
    val camino = Path()
    val pasos = 120
    for (paso in 0..pasos) {
        val angulo = paso / pasos.toFloat() * 2f * PI.toFloat() + giro
        val r = radio * (1f - redondez + redondez * abs(cos(lobulos * angulo / 2f)))
        val punto = Offset(centro.x + r * cos(angulo), centro.y + r * sin(angulo))
        if (paso == 0) camino.moveTo(punto.x, punto.y) else camino.lineTo(punto.x, punto.y)
    }
    camino.close()
    return camino
}

/**
 * Qué dibuja cada variante.
 *
 * Es un `when` largo a propósito y no veinte funciones sueltas: leído de arriba abajo, cada
 * gesto queda con todas sus variantes seguidas, que es la única forma de asegurarse de que dos
 * no acaban pareciéndose. Ese fue el problema de la primera versión —«escalonada», «escala» y
 * «resorte» hacían lo mismo— y se arregló mirándolas juntas, no una a una.
 */
internal fun DrawScope.pintarVariante(gesto: String, variante: String, t: Float, tinta: TintaDemo) {
    when (gesto) {
        "velocidad" -> velocidad(variante, t, tinta)
        "rebote" -> rebote(variante, t, tinta)
        "pulsacion" -> pulsacion(variante, t, tinta)
        "carga" -> carga(variante, t, tinta)
        "transicion" -> transicion(variante, t, tinta)
        "listas" -> listas(variante, t, tinta)
        "refresco" -> refresco(variante, t, tinta)
        "asistencia" -> asistencia(variante, t, tinta)
        "notaNueva" -> notaNueva(variante, t, tinta)
        "subeNota" -> subeNota(variante, t, tinta)
        "recupera" -> recupera(variante, t, tinta)
        "sello" -> sello(variante, t, tinta)
        "cierreSem" -> cierreSemestre(variante, t, tinta)
        "celebracion" -> celebracion(variante, t, tinta)
        "tachar" -> tachar(variante, t, tinta)
        "latido" -> latido(variante, t, tinta)
        "deshacer" -> deshacer(variante, t, tinta)
        "guardado" -> guardado(variante, t, tinta)
        "fijar" -> fijar(variante, t, tinta)
        "presupuesto" -> presupuesto(variante, t, tinta)
        "claseAhora" -> claseAhora(variante, t, tinta)
        "errorShake" -> errorAviso(variante, t, tinta)
        "saludo" -> saludo(variante, t, tinta)
        "fabScroll" -> fabScroll(variante, t, tinta)
        "haptica" -> haptica(variante, t, tinta)
    }
}

// ---------------------------------------------------------------------- base

private fun DrawScope.velocidad(v: String, t: Float, c: TintaDemo) {
    val recorrido = 68f
    val avance = when (v) {
        // «Nada» no es lento: es que no hay recorrido. El punto está aquí o está allí.
        "instant" -> if (t < 0.5f) 0f else 1f
        "rapida" -> suave(tramo(t, 0f, 0.3f))
        "lenta" -> suave(tramo(t, 0f, 0.95f))
        else -> suave(tramo(t, 0f, 0.6f))
    }
    drawLine(c.pieza, Offset(16f, 32f), Offset(84f, 32f), strokeWidth = 2f)
    drawCircle(c.acento, radius = 6f, center = Offset(16f + recorrido * avance, 32f))
}

private fun DrawScope.rebote(v: String, t: Float, c: TintaDemo) {
    val fuerza = when (v) {
        "suave" -> 0f
        "vivo" -> 0.9f
        else -> 0.4f
    }
    val avance = if (fuerza == 0f) suave(tramo(t, 0f, 0.55f)) else muelle(tramo(t, 0f, 0.75f), fuerza)
    val y = 14f + 34f * avance
    drawLine(c.pieza, Offset(30f, 48f), Offset(70f, 48f), strokeWidth = 2f)
    drawRoundRect(
        color = c.acento,
        topLeft = Offset(38f, y - 10f),
        size = Size(24f, 20f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
}

/**
 * Al pulsar, **con el dedo a la vista**.
 *
 * No se veia nada, y con razon: el boton medía la mitad del lienzo y se encogia un doce por
 * ciento en el fotograma en que el dedo tocaba. En una caja de dos centimetros, eso es nada.
 * Ahora el boton ocupa casi todo el ancho, el hundido baja al ochenta y dos por ciento, y hay
 * un circulo haciendo de dedo que baja, toca y se va — que es lo que deja entender **cuando**
 * pasa lo que pasa.
 */
private fun DrawScope.pulsacion(v: String, t: Float, c: TintaDemo) {
    // El dedo baja, toca en el 0,35 y se retira. El golpe arranca justo al tocar.
    val bajada = suave(tramo(t, 0.05f, 0.35f))
    val subida = suave(tramo(t, 0.55f, 0.85f))
    val tocando = bajada >= 1f && subida <= 0f
    val golpe = tramo(t, 0.35f, 0.75f)

    val escala = when (v) {
        "hundir" -> if (tocando) 0.82f else 1f - 0.18f * (1f - subida) * (1f - golpe)
        "rebote" -> if (golpe <= 0f) 1f else 0.82f + 0.18f * muelle(golpe, 0.9f)
        else -> 1f
    }
    scale(escala.coerceIn(0.5f, 1.3f), pivot = Offset(50f, 34f)) {
        drawRoundRect(
            color = c.acento,
            topLeft = Offset(12f, 22f),
            size = Size(76f, 24f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
        )
        // La onda nace donde toca el dedo y se abre hasta salirse del boton.
        if (v == "onda" && golpe > 0f && golpe < 1f) {
            clipRect(12f, 22f, 88f, 46f) {
                drawCircle(
                    color = c.fondo.copy(alpha = 0.75f * (1f - golpe)),
                    radius = 48f * golpe,
                    center = Offset(50f, 34f)
                )
            }
        }
    }
    // El dedo: un circulo con su sombra, que es lo que da la sensacion de que baja y no de que
    // aparece.
    val y = 6f + 22f * bajada - 22f * subida
    drawCircle(color = c.tinta.copy(alpha = 0.18f), radius = 9f, center = Offset(50f, y + 3f))
    drawCircle(color = c.tinta.copy(alpha = 0.8f), radius = 8f, center = Offset(50f, y))
}

private fun DrawScope.carga(v: String, t: Float, c: TintaDemo) {
    val centro = Offset(50f, 32f)
    when (v) {
        "circulo" -> {
            drawCircle(c.pieza, radius = 16f, center = centro, style = Stroke(4f))
            drawArc(
                color = c.acento,
                startAngle = t * 360f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(centro.x - 16f, centro.y - 16f),
                size = Size(32f, 32f),
                style = Stroke(4f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        // La gracia del indicador de M3E es que **cambia de forma mientras gira**. Sin la
        // rotación continua no se distingue de un círculo que late.
        "formas" -> {
            val formas = listOf(9 to 0.10f, 4 to 0.18f, 12 to 0.13f, 6 to 0.20f, 3 to 0.26f)
            val paso = t * formas.size
            val indice = paso.toInt().coerceIn(0, formas.size - 1)
            val siguiente = (indice + 1) % formas.size
            val mezcla = suave(paso - indice)
            val lobulos = if (mezcla < 0.5f) formas[indice].first else formas[siguiente].first
            val redondez = formas[indice].second + (formas[siguiente].second - formas[indice].second) * mezcla
            drawPath(
                path = formaLobulada(centro, 17f, lobulos, redondez, t * 2f * PI.toFloat()),
                color = c.acento
            )
        }
        "onda" -> {
            drawPath(
                path = ondaCircular(centro, 15f, 2.6f, 8, 0f, 0.75f, t * 2f * PI.toFloat()),
                color = c.acento,
                style = Stroke(3.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        "puntos" -> {
            repeat(3) { indice ->
                val fase = ((t + indice * 0.18f) % 1f)
                val alto = abs(sin(fase * PI.toFloat()))
                drawCircle(
                    color = c.acento,
                    radius = 5f,
                    center = Offset(34f + indice * 16f, 36f - 10f * alto)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------- transiciones

private fun DrawScope.transicion(v: String, t: Float, c: TintaDemo) {
    val avance = suave(tramo(t, 0.15f, 0.7f))
    // La que sale es «Materias» y la que entra «Cálculo III»: con los nombres puestos se sabe
    // cual es cual sin tener que deducirlo del color.
    val sale = "Materias"
    val entra = "Cálculo III"
    when (v) {
        "ninguna" -> if (avance < 0.5f) {
            panel(13f, c.pieza, sale, c)
        } else {
            panel(13f, c.acento, entra, c)
        }
        "fundido" -> {
            panel(13f, c.pieza, sale, c, alfa = 1f - avance)
            panel(13f, c.acento, entra, c, alfa = avance)
        }
        "eje" -> {
            panel(13f - 90f * avance, c.pieza, sale, c)
            panel(13f + 90f * (1f - avance), c.acento, entra, c)
        }
        "contenedor" -> {
            panel(13f, c.pieza, sale, c, alfa = 1f - avance)
            panel(13f, c.acento, entra, c, alfa = avance, escala = 0.55f + 0.45f * avance)
        }
        "abajo" -> {
            panel(13f, c.pieza, sale, c)
            translate(top = 62f * (1f - avance)) { panel(13f, c.acento, entra, c) }
        }
        "zoom" -> {
            panel(13f, c.pieza, sale, c, alfa = 1f - avance, escala = 1f + 0.35f * avance)
            panel(13f, c.acento, entra, c, alfa = avance, escala = 0.7f + 0.3f * avance)
        }
    }
}

private fun DrawScope.listas(v: String, t: Float, c: TintaDemo) {
    /*
     * **Tres tareas con su nombre, no tres barras.**
     *
     * Con barras, «escalonada» y «cascada» se distinguen por la direccion y poco mas. Con los
     * nombres puestos se ve **cual llega primero**, que es lo que de verdad las separa: la
     * escalonada trae la de arriba antes, la cascada tambien pero cayendo, y el abanico las
     * abre desde el borde como cartas.
     */
    val nombres = listOf("Taller 2", "Parcial", "Quiz 3")
    repeat(3) { indice ->
        val y = 12f + indice * 16f
        val retardo = when (v) {
            "escalonada" -> indice * 0.10f
            "cascada" -> indice * 0.20f
            "abanico" -> indice * 0.12f
            else -> 0f
        }
        val bruto = tramo(t, 0.08f + retardo, 0.52f + retardo)

        fun tarea(alfa: Float = 1f, giro: Float = 0f, ancho: Float = 76f, x: Float = 12f) {
            rotate(degrees = giro, pivot = Offset(12f, y + 5f)) {
                drawRoundRect(
                    color = c.acento.copy(alpha = c.acento.alpha * alfa * 0.22f),
                    topLeft = Offset(x, y),
                    size = Size(ancho, 11f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.5f, 3.5f)
                )
                drawCircle(
                    color = c.acento.copy(alpha = alfa),
                    radius = 3.4f,
                    center = Offset(x + 7f, y + 5.5f)
                )
                texto(nombres[indice], x + 14f, y + 5.5f, c, c.tinta.copy(alpha = 0.85f), tamano = 7f, negrita = false, alfa = alfa)
            }
        }

        when (v) {
            "ninguna" -> tarea()
            "fundido" -> tarea(alfa = suave(tramo(t, 0.1f, 0.6f)))
            "escalonada" -> {
                val p = suave(bruto)
                translate(top = 14f * (1f - p)) { tarea(alfa = p) }
            }
            "cascada" -> {
                val p = suave(bruto)
                translate(top = -18f * (1f - p)) { tarea(alfa = p) }
            }
            // Escala no se desplaza: crece en su sitio, y las tres a la vez.
            "escala" -> {
                val p = suave(tramo(t, 0.1f, 0.55f))
                val ancho = 76f * (0.55f + 0.45f * p)
                tarea(alfa = p, ancho = ancho, x = 12f + (76f - ancho) / 2f)
            }
            // Resorte llega desde muy abajo y se pasa de largo antes de asentarse.
            "resorte" -> {
                val p = muelle(tramo(t, 0.08f, 0.75f), 0.85f)
                translate(top = 30f * (1f - p)) { tarea() }
            }
            "abanico" -> {
                val p = suave(bruto)
                tarea(alfa = p, giro = -22f * (1f - p))
            }
        }
    }
}

private fun DrawScope.refresco(v: String, t: Float, c: TintaDemo) {
    /*
     * **La lista bajando, y el indicador asomando por encima.**
     *
     * Los indicadores flotaban en el vacio: no se entendia que aquello era un gesto de tirar,
     * porque no habia nada que se estuviera tirando. Ahora la lista baja, el indicador aparece
     * en el hueco que deja, y al soltar todo vuelve a su sitio — que es el gesto entero.
     */
    val tirando = suave(tramo(t, 0.06f, 0.34f))
    val soltando = suave(tramo(t, 0.7f, 0.94f))
    val abierto = tirando - soltando
    val hueco = 22f * abierto

    // El indicador vive en el hueco, y se apaga con el.
    val cy = 4f + hueco * 0.55f
    when (v) {
        "circulo" -> drawArc(
            color = c.acento.copy(alpha = abierto),
            startAngle = t * 720f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(42f, cy - 8f),
            size = Size(16f, 16f),
            style = Stroke(3f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        // La onda **viaja** por el arco y respira: dibujada una vez y rotada, lo que gira es
        // una onda congelada, que es justo lo que no hace el indicador de M3E.
        "ondacirc" -> {
            val amplitud = 1.6f + 1.2f * sin(t * 6f * PI.toFloat())
            drawPath(
                path = ondaCircular(Offset(50f, cy), 8f, amplitud, 9, 0f, 1f, -t * 8f * PI.toFloat()),
                color = c.pieza.copy(alpha = abierto),
                style = Stroke(2.4f)
            )
            drawPath(
                path = ondaCircular(Offset(50f, cy), 8f, amplitud, 9, t, t + 0.35f, -t * 8f * PI.toFloat()),
                color = c.acento.copy(alpha = abierto),
                style = Stroke(3f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        "formas" -> {
            val formas = listOf(9 to 0.10f, 4 to 0.18f, 12 to 0.13f)
            val indice = (t * formas.size).toInt().coerceIn(0, formas.size - 1)
            drawPath(
                path = formaLobulada(Offset(50f, cy), 8f, formas[indice].first, formas[indice].second, t * 3f * PI.toFloat()),
                color = c.acento.copy(alpha = abierto)
            )
        }
        // Elastico: se estira mientras se tira, que es lo que le da el nombre.
        "elastico" -> {
            val estiron = 1f + 0.9f * abierto
            drawRoundRect(
                color = c.acento.copy(alpha = abierto),
                topLeft = Offset(50f - 9f / estiron, cy - 4f * estiron),
                size = Size(18f / estiron, 8f * estiron),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
            )
        }
        "barra" -> {
            drawRoundRect(
                color = c.pieza.copy(alpha = abierto),
                topLeft = Offset(28f, cy - 2f),
                size = Size(44f, 4f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
            clipRect(28f, cy - 4f, 72f, cy + 4f) {
                drawRoundRect(
                    color = c.acento.copy(alpha = abierto),
                    topLeft = Offset(28f + 60f * ((t * 1.6f) % 1f) - 16f, cy - 2f),
                    size = Size(16f, 4f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
            }
        }
        // La gota se descuelga del borde mientras se tira: cuanto mas abajo, mas alargada.
        "gota" -> {
            val alargue = 1f + 0.5f * abierto
            drawOval(
                color = c.acento.copy(alpha = abierto),
                topLeft = Offset(50f - 6f / alargue, cy - 6f * alargue),
                size = Size(12f / alargue, 12f * alargue)
            )
        }
    }

    // La lista, bajando con el gesto. Es lo que convierte el indicador en «tirar para
    // refrescar» en vez de en «algo dando vueltas».
    translate(top = hueco) {
        repeat(3) { indice ->
            fila(12f + indice * 16f, c.acento)
        }
    }
}

// ---------------------------------------------------------------------- académico

private fun DrawScope.asistencia(v: String, t: Float, c: TintaDemo) {
    /*
     * **La clase con su fecha, y el estado que cambia a «Asistí».**
     *
     * Un circulo verde con un visto no dice a que se asistio. Con la clase escrita arriba y el
     * estado abajo se entiende el gesto entero: se marca una casilla y **la fila cambia de
     * estado**, que es lo que pasa de verdad en Horario.
     */
    val avance = tramo(t, 0.12f, 0.6f)
    val p = suave(avance)

    texto("Cálculo III", 12f, 14f, c, c.tinta.copy(alpha = 0.8f), tamano = 8.5f, negrita = false)
    texto("Lunes 10:00", 12f, 25f, c, c.tinta.copy(alpha = 0.45f), tamano = 7f, negrita = false)

    val centro = Offset(74f, 20f)
    when (v) {
        "ninguna" -> {
            drawCircle(c.verde, radius = 13f, center = centro)
            visto(centro, 13f, 1f, c.fondo, grosor = 2.6f)
        }
        "trazo" -> {
            drawCircle(c.verde.copy(alpha = 0.22f), radius = 13f, center = centro)
            visto(centro, 13f, p, c.verde, grosor = 2.8f)
        }
        // El relleno sube por dentro, como un vaso que se llena.
        "relleno" -> {
            drawCircle(c.pieza, radius = 13f, center = centro)
            clipRect(centro.x - 13f, centro.y + 13f - 26f * p, centro.x + 13f, centro.y + 13f) {
                drawCircle(c.verde, radius = 13f, center = centro)
            }
            visto(centro, 13f, 1f, c.fondo.copy(alpha = p), grosor = 2.6f)
        }
        "rebote" -> {
            val escala = if (avance < 1f) muelle(avance, 0.9f) else 1f
            scale(escala.coerceAtLeast(0f), pivot = centro) {
                drawCircle(c.verde, radius = 13f, center = centro)
                visto(centro, 13f, 1f, c.fondo, grosor = 2.6f)
            }
        }
        // Una franja verde cruza la fila entera de izquierda a derecha.
        "barrido" -> {
            clipRect(8f, 6f, 8f + 84f * p, 34f) {
                drawRoundRect(
                    color = c.verde.copy(alpha = 0.20f),
                    topLeft = Offset(8f, 6f),
                    size = Size(84f, 28f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
            }
            drawCircle(c.verde.copy(alpha = if (p > 0.75f) 1f else 0.2f), radius = 13f, center = centro)
            visto(centro, 13f, if (p > 0.75f) 1f else 0f, c.fondo, grosor = 2.6f)
        }
    }

    // El estado, que es lo que cambia al marcar.
    val marcado = p > 0.6f
    texto(
        if (marcado) "Asistí" else "Sin marcar",
        12f, 46f, c,
        if (marcado) c.verde else c.tinta.copy(alpha = 0.4f),
        tamano = 8f
    )
}

private fun DrawScope.notaNueva(v: String, t: Float, c: TintaDemo) {
    /*
     * **Rediseno entero: la nota nueva llega con su nombre y su cifra, y el promedio la acusa.**
     *
     * Antes eran tres barras de las que una cambiaba de color, y no se entendia ni que fuera
     * una nota ni de que corte. Registrar una nota tiene dos partes que hay que ver a la vez:
     * **la fila que entra** —«Taller 2 · 4,5»— y **el promedio de arriba que se mueve por su
     * causa**. Sin la segunda, cualquiera de las seis variantes es «una fila que aparece».
     */
    val avance = suave(tramo(t, 0.14f, 0.62f))

    // La cabecera con el promedio: es la que dice que la nota nueva cambio algo.
    // «Promedio» a secas: con «Promedio del corte» el rotulo llegaba hasta donde empieza la
    // cifra y las dos se pisaban. En una caja de dos centimetros no caben las dos cosas.
    texto("Promedio", 12f, 11f, c, c.tinta.copy(alpha = 0.55f), tamano = 7f, negrita = false)
    val promedio = if (v == "contar") 3.9f + 0.35f * avance else if (avance > 0.5f) 4.25f else 3.9f
    texto("%.2f".format(promedio).replace('.', ','), 88f, 11f, c, c.verde, tamano = 11f, centrado = true)

    // Las notas que ya estaban.
    fun vieja(y: Float, nombre: String, valor: String, alfa: Float = 1f) {
        drawRoundRect(
            color = c.pieza.copy(alpha = c.pieza.alpha * alfa),
            topLeft = Offset(12f, y),
            size = Size(76f, 13f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        texto(nombre, 17f, y + 6.5f, c, c.tinta.copy(alpha = 0.7f * alfa), tamano = 7.5f, negrita = false)
        texto(valor, 82f, y + 6.5f, c, c.tinta.copy(alpha = 0.7f * alfa), tamano = 8.5f, centrado = true)
    }

    // La nueva, en acento, con su nombre y su cifra.
    fun nueva(y: Float, alfa: Float = 1f, x: Float = 12f) {
        drawRoundRect(
            color = c.acento.copy(alpha = alfa),
            topLeft = Offset(x, y),
            size = Size(76f, 13f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        texto("Taller 2", x + 5f, y + 6.5f, c, c.fondo, tamano = 7.5f, alfa = alfa)
        texto("4,5", x + 70f, y + 6.5f, c, c.fondo, tamano = 8.5f, centrado = true, alfa = alfa)
    }

    when (v) {
        "ninguna" -> { vieja(26f, "Parcial", "4,0"); nueva(43f) }

        // Cae desde arriba y empuja: la vieja baja con ella.
        "cae" -> {
            translate(top = 17f * avance) { vieja(26f, "Parcial", "4,0") }
            translate(top = -22f * (1f - avance)) { nueva(26f, avance) }
        }

        "lateral" -> {
            vieja(43f, "Parcial", "4,0")
            nueva(26f, 1f, x = 12f + 92f * (1f - avance))
        }

        // Destello: ya esta puesta, y se enciende una vez.
        "destello" -> {
            vieja(43f, "Parcial", "4,0")
            nueva(26f)
            drawRoundRect(
                color = c.fondo.copy(alpha = (1f - avance) * 0.85f),
                topLeft = Offset(12f, 26f),
                size = Size(76f, 13f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
        }

        // Aqui lo que se mueve no es la fila: es el promedio de arriba contando.
        "contar" -> {
            vieja(43f, "Parcial", "4,0")
            nueva(26f)
            drawCircle(c.verde.copy(alpha = 0.25f * (1f - avance)), radius = 7f + 9f * avance, center = Offset(88f, 11f))
        }

        // El hueco se abre primero, y la fila llega despues a ocuparlo.
        "abre" -> {
            translate(top = 17f * avance) { vieja(26f, "Parcial", "4,0") }
            if (avance > 0.55f) nueva(26f, tramo(avance, 0.55f, 1f))
        }
    }
}

private fun DrawScope.subeNota(v: String, t: Float, c: TintaDemo) {
    /*
     * **El promedio pasa de 3,8 a 4,2, con la cifra escrita.**
     *
     * Era un circulo verde dando saltitos, que no decia que hubiera subido nada. Lo que hace
     * que se entienda es ver **las dos cifras**: la vieja tachada arriba y la nueva llegando,
     * como en el diseno.
     */
    val avance = tramo(t, 0.18f, 0.68f)
    val p = suave(avance)
    val subiendo = avance > 0f && avance < 1f

    // La materia, para que se sepa de que promedio se habla.
    texto("Cálculo III", 12f, 18f, c, c.tinta.copy(alpha = 0.75f), tamano = 8f, negrita = false)

    when (v) {
        "ninguna" -> texto("4,2", 12f, 40f, c, c.verde, tamano = 20f)

        // Salto: la cifra nueva sube desde abajo y se pasa de largo antes de asentarse.
        "salto" -> {
            val salto = if (subiendo) sin(p * PI.toFloat()) else 0f
            texto("3,8", 12f, 40f, c, c.tinta.copy(alpha = 0.25f * (1f - p)), tamano = 20f)
            texto("4,2", 12f, 40f - 12f * salto, c, c.verde.copy(alpha = 0.3f + 0.7f * p), tamano = 20f)
        }

        // Flecha: la cifra cambia y una flecha verde sube al lado diciendo por que.
        "flecha" -> {
            texto(if (p > 0.5f) "4,2" else "3,8", 12f, 40f, c, c.verde, tamano = 20f)
            val alza = suave(avance)
            val cima = Offset(58f, 44f - 20f * alza)
            drawPath(
                Path().apply {
                    moveTo(cima.x, cima.y)
                    lineTo(cima.x - 6f, cima.y + 9f)
                    lineTo(cima.x + 6f, cima.y + 9f)
                    close()
                },
                c.verde.copy(alpha = 1f - 0.5f * alza)
            )
            texto("+0,4", 68f, 34f, c, c.verde.copy(alpha = alza), tamano = 9f)
        }

        // Brillo: la cifra nueva se enciende, con un halo que se abre y se apaga.
        "brillo" -> {
            if (subiendo) {
                drawCircle(
                    color = c.verde.copy(alpha = 0.28f * (1f - p)),
                    radius = 10f + 22f * p,
                    center = Offset(26f, 40f)
                )
            }
            texto(if (p > 0.45f) "4,2" else "3,8", 12f, 40f, c, c.verde, tamano = 20f)
        }
    }
}

private fun DrawScope.recupera(v: String, t: Float, c: TintaDemo) {
    /*
     * **Sale del rojo: la nota cruza el aprobado y el estado cambia con ella.**
     *
     * Una barra que cambia de color no dice que se haya recuperado nada. Lo que lo dice es la
     * cifra pasando de 2,8 a 3,4 y el estado de «En riesgo» a «Al dia»: el color acompana, no
     * explica.
     */
    val avance = tramo(t, 0.15f, 0.7f)
    val p = suave(avance)
    val recuperada = p > 0.5f
    val y = 30f

    texto("Costos II", 12f, 15f, c, c.tinta.copy(alpha = 0.75f), tamano = 8.5f, negrita = false)

    fun barra(color: Color, x: Float = 12f, ancho: Float = 52f) {
        drawRoundRect(
            color = color,
            topLeft = Offset(x, y),
            size = Size(ancho, 12f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
    }

    when (v) {
        "seco" -> barra(if (recuperada) c.verde else c.rojo)
        // El color viaja por el ambar: es un degradado en el tiempo, no un corte.
        "viaje" -> barra(
            if (p < 0.5f) mezclar(c.rojo, c.ambar, p * 2f) else mezclar(c.ambar, c.verde, (p - 0.5f) * 2f)
        )
        // El pulso tambien parte del rojo: pintando siempre verde, la barra decia «recuperada»
        // mientras el texto de abajo seguia diciendo «En riesgo», y las dos cosas se
        // contradecian en la misma caja.
        "pulso" -> {
            barra(if (recuperada) c.verde else c.rojo)
            if (recuperada) {
                val pulso = abs(sin(t * 4f * PI.toFloat()))
                drawRoundRect(
                    color = c.verde.copy(alpha = 0.30f * pulso),
                    topLeft = Offset(9f, y - 3f),
                    size = Size(58f, 18f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
            }
        }
        // El verde entra por la izquierda sobre el rojo, con su borde a la vista.
        "barrido" -> {
            barra(c.rojo)
            val borde = 12f + 52f * p
            clipRect(12f, y, borde, y + 12f) { barra(c.verde) }
            if (p > 0f && p < 1f) {
                drawLine(c.fondo, Offset(borde, y - 1f), Offset(borde, y + 13f), strokeWidth = 2f)
            }
        }
        // Relevo: el rojo se encoge por la derecha mientras el verde crece por la izquierda.
        "relevo" -> {
            barra(c.rojo, x = 12f + 52f * p, ancho = 52f * (1f - p))
            barra(c.verde, x = 12f, ancho = 52f * p)
        }
    }

    // La cifra y el estado: lo que de verdad cuenta que se recupero.
    texto(if (recuperada) "3,4" else "2,8", 78f, y + 6f, c, if (recuperada) c.verde else c.rojo, tamano = 14f, centrado = true)
    texto(
        if (recuperada) "Al día" else "En riesgo",
        12f, 50f, c,
        if (recuperada) c.verde else c.rojo,
        tamano = 8f
    )
}

private fun DrawScope.sello(v: String, t: Float, c: TintaDemo) {
    /*
     * **Idea nueva: el corte queda cerrado con su nota dentro del sello.**
     *
     * Ni la version de la app ni la del diseno servian —un circulo vacio cayendo no dice que se
     * haya cerrado nada—. Lo que cierra un corte es que su nota **queda fijada**: ya no se
     * puede tocar. Por eso el sello lleva dentro la cifra y la palabra, y por eso debajo esta
     * el corte al que se le pone encima. Sin esas dos cosas es un adorno.
     */
    // El corte, debajo: lo que se esta cerrando.
    drawRoundRect(
        color = c.pieza,
        topLeft = Offset(10f, 12f),
        size = Size(80f, 40f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
    texto("Corte 2", 17f, 22f, c, c.tinta.copy(alpha = 0.55f), tamano = 8f, negrita = false)
    texto("4,25", 17f, 38f, c, c.tinta.copy(alpha = 0.5f), tamano = 14f)

    val centro = Offset(56f, 36f)
    val golpe = tramo(t, 0.15f, 0.55f)
    val asentado = suave(golpe)

    fun estampa(escala: Float, alfa: Float, giro: Float = -14f) {
        rotate(degrees = giro, pivot = centro) {
            scale(escala, pivot = centro) {
                // El sello, mas ancho y con la letra mas pequena: «CERRADO» a 8,5 no cabia en
                // cincuenta y dos y salia partido por la mitad.
                drawRoundRect(
                    color = c.verde.copy(alpha = alfa),
                    topLeft = Offset(centro.x - 30f, centro.y - 11f),
                    size = Size(60f, 22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f),
                    style = Stroke(2.2f)
                )
                texto("CERRADO", centro.x, centro.y, c, c.verde.copy(alpha = alfa), tamano = 7f, centrado = true)
            }
        }
    }

    when (v) {
        "ninguna" -> estampa(1f, 1f)
        // Baja desde muy arriba y aterriza: la escala grande al principio es la profundidad.
        "estampa" -> estampa(2.4f - 1.4f * asentado, suave(tramo(t, 0.15f, 0.4f)))
        // La tinta se corre por debajo al golpear.
        "tinta" -> {
            drawCircle(c.verde.copy(alpha = 0.20f * asentado), radius = 34f * asentado, center = centro)
            estampa(1f, asentado)
        }
        // El lacre cae, se aplasta y se recupera: por eso el ovalo cambia de proporcion.
        "lacre" -> {
            val caida = suave(tramo(t, 0.1f, 0.45f))
            val aplaste = 1f + 0.35f * tramo(t, 0.45f, 0.58f) - 0.35f * tramo(t, 0.58f, 0.75f)
            val y = 6f + 28f * caida
            drawOval(
                color = c.rojo,
                topLeft = Offset(centro.x - 15f * aplaste, y - 15f / aplaste),
                size = Size(30f * aplaste, 30f / aplaste)
            )
            texto("✓", centro.x, y, c, c.fondo, tamano = 13f, centrado = true, alfa = tramo(t, 0.55f, 0.75f))
        }
        // La cinta cruza el corte entero y el sello llega despues.
        "cinta" -> {
            val ancho = 104f * suave(golpe)
            rotate(degrees = -12f, pivot = Offset(50f, 32f)) {
                drawRoundRect(
                    color = c.verde,
                    topLeft = Offset(-2f, 25f),
                    size = Size(ancho, 15f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
                texto("CERRADO", 50f, 32f, c, c.fondo, tamano = 8.5f, centrado = true, alfa = tramo(golpe, 0.6f, 1f))
            }
        }
    }
}

private fun DrawScope.cierreSemestre(v: String, t: Float, c: TintaDemo) {
    /*
     * **El resumen del semestre, con su titulo y sus cifras.**
     *
     * Eran cuatro rectangulos armandose: se entendia el orden pero no que fuera un resumen. Con
     * el titulo y las tres cifras —promedio, materias, creditos— cada variante se lee como lo
     * que es: la forma en que aparece el balance del semestre que acaba.
     */
    val avance = suave(tramo(t, 0.12f, 0.72f))

    fun titulo(alfa: Float) {
        if (alfa <= 0.02f) return
        drawRoundRect(
            color = c.acento.copy(alpha = alfa),
            topLeft = Offset(12f, 8f),
            size = Size(76f, 16f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        texto("Semestre 2026-1", 50f, 16f, c, c.fondo, tamano = 8.5f, centrado = true, alfa = alfa)
    }

    fun cifra(indice: Int, alfa: Float, desvio: Float = 0f) {
        if (alfa <= 0.02f) return
        val datos = listOf("4,25" to "PROM", "6" to "MAT", "18" to "CRÉD")
        val x = 12f + indice * 26f
        translate(top = desvio) {
            drawRoundRect(
                color = c.pieza.copy(alpha = c.pieza.alpha * alfa),
                topLeft = Offset(x, 30f),
                size = Size(24f, 24f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            texto(datos[indice].first, x + 12f, 39f, c, c.verde, tamano = 9f, centrado = true, alfa = alfa)
            texto(datos[indice].second, x + 12f, 49f, c, c.tinta.copy(alpha = 0.5f), tamano = 6f, centrado = true, alfa = alfa)
        }
    }

    when (v) {
        "entero" -> { titulo(avance); repeat(3) { cifra(it, avance) } }
        // Pieza a pieza: primero el titulo, y las cifras en su turno.
        "pieza" -> {
            titulo(suave(tramo(t, 0.1f, 0.34f)))
            repeat(3) { cifra(it, suave(tramo(t, 0.26f + it * 0.14f, 0.5f + it * 0.14f))) }
        }
        // Cortina: todo esta puesto y lo que baja es el corte.
        "cortina" -> clipRect(0f, 0f, ANCHO, ALTO * avance) {
            titulo(1f); repeat(3) { cifra(it, 1f) }
        }
        // Apilado: llegan desde abajo y se reparten al llegar.
        "apilado" -> {
            titulo(suave(tramo(t, 0.08f, 0.35f)))
            repeat(3) { indice ->
                val p = suave(tramo(t, 0.18f + indice * 0.12f, 0.6f + indice * 0.12f))
                cifra(indice, p, desvio = 34f * (1f - p))
            }
        }
    }
}

private fun DrawScope.caja(r: Rect, color: Color, alfa: Float) {
    if (alfa <= 0f) return
    drawRoundRect(
        color = color.copy(alpha = color.alpha * alfa),
        topLeft = Offset(r.left, r.top),
        size = Size(r.width, r.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
}

// ---------------------------------------------------------------------- tareas y notas

private fun DrawScope.celebracion(v: String, t: Float, c: TintaDemo) {
    /*
     * **«¡Todo hecho!», como en el diseño.**
     *
     * Era un visto gris con particulas alrededor y no se entendia que se estaba celebrando. El
     * diseno escribe el mensaje y hace salir el confeti de detras: el texto es lo que da el
     * motivo, y el confeti la celebracion.
     */
    val golpe = tramo(t, 0.12f, 0.9f)
    val entrada = suave(tramo(t, 0.05f, 0.3f))
    val centro = Offset(50f, 32f)

    when (v) {
        "ninguna" -> texto("¡Todo hecho!", 50f, 32f, c, c.tinta, tamano = 11f, centrado = true)

        "confeti" -> {
            val colores = listOf(c.acento, c.verde, c.ambar, c.gasto)
            repeat(14) { indice ->
                val angulo = indice / 14f * 2f * PI.toFloat() + 0.4f
                val dist = 40f * suave(golpe)
                val caida = 26f * golpe * golpe
                val punto = Offset(
                    centro.x + cos(angulo) * dist,
                    centro.y + sin(angulo) * dist * 0.62f + caida
                )
                rotate(degrees = indice * 40f + golpe * 300f, pivot = punto) {
                    drawRect(
                        color = colores[indice % colores.size].copy(alpha = 1f - golpe),
                        topLeft = punto - Offset(3f, 1.6f),
                        size = Size(6f, 3.2f)
                    )
                }
            }
            scale(0.7f + 0.3f * entrada, pivot = centro) {
                texto("¡Todo hecho!", 50f, 32f, c, c.acento, tamano = 11f, centrado = true, alfa = entrada)
            }
        }

        "onda" -> {
            repeat(2) { indice ->
                val p = tramo(t, 0.1f + indice * 0.2f, 0.9f + indice * 0.2f)
                if (p > 0f && p < 1f) {
                    drawCircle(
                        color = c.acento.copy(alpha = 0.6f * (1f - p)),
                        radius = 14f + 38f * p,
                        center = centro,
                        style = Stroke(3f)
                    )
                }
            }
            texto("¡Todo hecho!", 50f, 32f, c, c.acento, tamano = 11f, centrado = true)
        }

        "sello" -> {
            val escala = 2.3f - 1.3f * suave(tramo(t, 0.1f, 0.45f))
            scale(escala, pivot = centro) {
                drawRoundRect(
                    color = c.verde.copy(alpha = entrada * 0.85f),
                    topLeft = Offset(16f, 22f),
                    size = Size(68f, 20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                    style = Stroke(2.5f)
                )
                texto("¡Todo hecho!", 50f, 32f, c, c.verde, tamano = 10f, centrado = true, alfa = entrada)
            }
        }

        "destello" -> {
            repeat(8) { indice ->
                val angulo = indice / 8f * 2f * PI.toFloat()
                val dentro = 24f + 10f * suave(golpe)
                drawLine(
                    color = c.ambar.copy(alpha = 1f - golpe),
                    start = centro + Offset(cos(angulo) * dentro, sin(angulo) * dentro * 0.7f),
                    end = centro + Offset(cos(angulo) * (dentro + 11f * (1f - golpe)), sin(angulo) * (dentro + 11f * (1f - golpe)) * 0.7f),
                    strokeWidth = 3f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
            texto("¡Todo hecho!", 50f, 32f, c, c.ambar, tamano = 11f, centrado = true)
        }
    }
}

private fun DrawScope.tachar(v: String, t: Float, c: TintaDemo) {
    /*
     * **Una tarea de verdad, con su casilla y su nombre.**
     *
     * Ni la version de la app ni la del diseno convencian, y las dos fallaban en lo mismo: una
     * barra gris con una raya encima no es una tarea completada, es una barra con una raya. Lo
     * que la hace legible es lo que tiene alrededor —la casilla que se marca a la izquierda y
     * el nombre que se apaga— porque tachar no es solo la linea: es que la fila **deja de
     * pedir atencion**.
     */
    val avance = suave(tramo(t, 0.18f, 0.68f))
    val y = 34f

    // La casilla, que se rellena y recibe su visto: es lo que dispara el tachado.
    val marcada = tramo(t, 0.1f, 0.3f)
    val caja = Offset(16f, y)
    drawRoundRect(
        color = c.verde.copy(alpha = marcada),
        topLeft = Offset(caja.x - 7f, caja.y - 7f),
        size = Size(14f, 14f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = c.tinta.copy(alpha = 0.35f * (1f - marcada)),
        topLeft = Offset(caja.x - 7f, caja.y - 7f),
        size = Size(14f, 14f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
        style = Stroke(1.8f)
    )
    visto(caja, 7f, marcada, c.fondo, grosor = 2f)

    // El nombre se apaga a la vez que se tacha: completada, la fila deja de pesar.
    val tinta = c.tinta.copy(alpha = 1f - 0.55f * avance)
    texto("Taller 2 de Cálculo", 28f, y, c, tinta, tamano = 9.5f, negrita = false)
    texto("Vence hoy", 28f, 47f, c, c.tinta.copy(alpha = 0.4f * (1f - avance)), tamano = 7.5f, negrita = false)

    val fin = 28f + 58f * avance
    when (v) {
        "ninguna" -> Unit
        "linea" -> drawLine(tinta, Offset(28f, y), Offset(fin, y), strokeWidth = 1.8f)
        // Marcador: banda ancha translucida, como un rotulador de verdad.
        "marcador" -> drawRoundRect(
            color = c.ambar.copy(alpha = 0.45f),
            topLeft = Offset(28f, y - 6f),
            size = Size(fin - 28f, 12f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
        // Visto encima: el trazo grande se dibuja sobre el nombre, no al lado.
        "visto" -> visto(Offset(56f, y), 16f, avance, c.verde, grosor = 3.5f)
        "doble" -> {
            drawLine(tinta, Offset(28f, y - 2.5f), Offset(fin, y - 2.5f), strokeWidth = 1.4f)
            drawLine(tinta, Offset(28f, y + 2.5f), Offset(fin, y + 2.5f), strokeWidth = 1.4f)
        }
        // La tinta cala: por donde paso, el nombre queda velado.
        "tinta" -> {
            drawRoundRect(
                color = c.tinta.copy(alpha = 0.18f),
                topLeft = Offset(28f, y - 7f),
                size = Size(fin - 28f, 14f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
            drawLine(tinta, Offset(28f, y), Offset(fin, y), strokeWidth = 2.4f)
        }
    }
}

private fun DrawScope.latido(v: String, t: Float, c: TintaDemo) {
    /*
     * **Una tarea vencida con su cuenta de dias, no un rectangulo rojo.**
     *
     * Lo que hay que entender aqui no es «esto late» sino «esto lleva tres dias esperandote»,
     * y eso solo lo dice el texto. El latido es lo que hace que la fila reclame sin gritar, y
     * cada variante reclama de una forma distinta: el pulso crece, el tic da dos golpes y
     * calla, y el borde se enciende sin mover nada.
     */
    val caja = Rect(12f, 18f, 88f, 48f)
    val centro = caja.center

    fun contenido(alfaFondo: Float, borde: Float) {
        drawRoundRect(
            color = c.rojo.copy(alpha = 0.16f * alfaFondo),
            topLeft = Offset(caja.left, caja.top),
            size = Size(caja.width, caja.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        if (borde > 0f) {
            drawRoundRect(
                color = c.rojo.copy(alpha = borde),
                topLeft = Offset(caja.left, caja.top),
                size = Size(caja.width, caja.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                style = Stroke(2f)
            )
        }
        texto("Parcial de Costos", 20f, 28f, c, c.tinta, tamano = 8.5f, negrita = false)
        texto("Venció hace 3 días", 20f, 40f, c, c.rojo, tamano = 8f)
    }

    when (v) {
        "ninguna" -> contenido(1f, 0.5f)
        "pulso" -> {
            val p = abs(sin(t * 2f * PI.toFloat()))
            scale(1f + 0.045f * p, pivot = centro) { contenido(1f, 0.35f + 0.4f * p) }
        }
        // Respira va en el color y no en el tamano: es el mas callado de los cinco.
        "respira" -> contenido(0.4f + 0.6f * (0.5f + 0.5f * sin(t * 2f * PI.toFloat())), 0.3f)
        "borde" -> contenido(0.6f, 0.25f + 0.75f * abs(sin(t * PI.toFloat())))
        // Dos golpes secos y una pausa larga: lo que hace un reloj, no un corazon.
        "tic" -> {
            val golpe = when {
                t < 0.06f -> 1f - t / 0.06f
                t in 0.12f..0.18f -> 1f - (t - 0.12f) / 0.06f
                else -> 0f
            }
            scale(1f + 0.05f * golpe, pivot = centro) { contenido(1f, 0.3f + 0.6f * golpe) }
        }
    }
}

private fun DrawScope.deshacer(v: String, t: Float, c: TintaDemo) {
    /*
     * **El hueco que dejo el borrado, y la fila volviendo a ocuparlo.**
     *
     * La fila aparecia entre otras dos y no se entendia que estuviera **volviendo**. Con el
     * hueco marcado con guiones se ve que ahi faltaba algo, y con el aviso de «Deshacer»
     * abajo se sabe de donde viene.
     */
    val avance = tramo(t, 0.12f, 0.68f)
    val p = suave(avance)

    fun otra(y: Float) {
        drawRoundRect(
            color = c.pieza,
            topLeft = Offset(12f, y),
            size = Size(76f, 12f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
    }
    otra(10f)
    otra(42f)

    // El hueco: se ve mientras la fila no ha llegado.
    if (p < 0.9f) {
        drawRoundRect(
            color = c.tinta.copy(alpha = 0.25f * (1f - p)),
            topLeft = Offset(12f, 26f),
            size = Size(76f, 12f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
            style = Stroke(1.2f, pathEffect = guiones)
        )
    }

    fun vuelta(y: Float, x: Float = 12f, alfa: Float = 1f, alto: Float = 12f, giro: Float = 0f) {
        rotate(degrees = giro, pivot = Offset(12f, y + alto / 2f)) {
            drawRoundRect(
                color = c.acento.copy(alpha = alfa),
                topLeft = Offset(x, y + (12f - alto) / 2f),
                size = Size(76f, alto),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            if (alto > 6f) texto("Taller 2", x + 5f, y + 6f, c, c.fondo, tamano = 7f, alfa = alfa)
        }
    }

    when (v) {
        "aparece" -> vuelta(26f, alfa = p)
        "vuelve" -> vuelta(26f, x = 12f + 92f * (1f - p))
        "cae" -> vuelta(26f - 24f * (1f - p), alfa = p)
        "despliega" -> vuelta(26f, alto = (12f * p).coerceAtLeast(1f))
        "rebota" -> {
            val m = if (avance < 1f) muelle(avance, 0.9f) else 1f
            vuelta(26f, x = 12f + 92f * (1f - m))
        }
        "gira" -> vuelta(26f, alfa = p, giro = -70f * (1f - p))
        "destello" -> {
            vuelta(26f, x = 12f + 92f * (1f - p))
            if (p > 0.85f) {
                drawRoundRect(
                    color = c.fondo.copy(alpha = 0.7f * (1f - tramo(p, 0.85f, 1f))),
                    topLeft = Offset(12f, 26f),
                    size = Size(76f, 12f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
            }
        }
    }

    // El aviso de donde salio: sin el, la fila «aparece» en vez de «volver».
    drawRoundRect(
        color = c.tinta.copy(alpha = 0.14f),
        topLeft = Offset(12f, 56f),
        size = Size(76f, 0.1f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(0f, 0f)
    )
    texto("Deshacer", 12f, 58f, c, c.acento.copy(alpha = 0.9f), tamano = 7.5f)
}

private fun DrawScope.guardado(v: String, t: Float, c: TintaDemo) {
    /*
     * **La nota que se esta escribiendo, y el aviso de que quedo guardada.**
     *
     * El aviso salia sobre una barra gris y no se sabia que se estaba guardando. Con las lineas
     * de la nota debajo se entiende: escribes, y arriba aparece la senal. Cada variante es una
     * forma distinta de dar esa senal sin interrumpir.
     */
    val entra = suave(tramo(t, 0.12f, 0.34f))
    val sale = 1f - suave(tramo(t, 0.72f, 0.94f))
    val alfa = entra * sale

    texto("Resumen de Cálculo", 12f, 32f, c, c.tinta.copy(alpha = 0.75f), tamano = 8f, negrita = false)
    listOf(42f, 50f).forEachIndexed { indice, y ->
        drawRoundRect(
            color = c.pieza,
            topLeft = Offset(12f, y),
            size = Size(if (indice == 0) 76f else 48f, 4f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
    }

    when (v) {
        "ninguna" -> Unit
        "pildora" -> {
            val y = 16f - 5f * (1f - entra)
            // 52 y no 40: «Guardado» a 7,5 no cabia en cuarenta y salia cortado.
            drawRoundRect(
                color = c.verde.copy(alpha = alfa),
                topLeft = Offset(24f, y - 8f),
                size = Size(52f, 16f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
            texto("Guardado", 50f, y, c, c.fondo, tamano = 7f, centrado = true, alfa = alfa)
        }
        "punto" -> {
            // El punto a la derecha y el rotulo a su izquierda, centrado: escrito desde la x
            // del punto se salia del lienzo y se veia «Guardad».
            drawCircle(c.verde.copy(alpha = alfa), radius = 3.5f, center = Offset(86f, 16f))
            texto("Guardado", 52f, 16f, c, c.verde.copy(alpha = alfa), tamano = 7f, centrado = true)
        }
        "visto" -> {
            visto(Offset(50f, 16f), 11f, entra, c.verde.copy(alpha = sale), grosor = 2.6f)
        }
        // El anillo se cierra mientras guarda: la vuelta entera es el guardado terminado.
        "anillo" -> {
            drawCircle(c.pieza, radius = 9f, center = Offset(50f, 16f), style = Stroke(2.6f))
            drawArc(
                color = c.verde,
                startAngle = -90f,
                sweepAngle = 360f * suave(tramo(t, 0.12f, 0.68f)),
                useCenter = false,
                topLeft = Offset(41f, 7f),
                size = Size(18f, 18f),
                style = Stroke(2.6f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        "filete" -> {
            val ancho = 100f * suave(tramo(t, 0.12f, 0.6f))
            drawRect(c.verde.copy(alpha = sale), topLeft = Offset(0f, 0f), size = Size(ancho, 3f))
            texto("Guardado", 50f, 14f, c, c.verde.copy(alpha = alfa), tamano = 7.5f, centrado = true)
        }
        "nube" -> {
            val y = 18f - 4f * entra
            drawCircle(c.verde.copy(alpha = alfa), radius = 6f, center = Offset(45f, y))
            drawCircle(c.verde.copy(alpha = alfa), radius = 8f, center = Offset(54f, y - 1f))
            drawRoundRect(
                color = c.verde.copy(alpha = alfa),
                topLeft = Offset(39f, y),
                size = Size(22f, 7f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.5f, 3.5f)
            )
        }
    }
}

private fun DrawScope.fijar(v: String, t: Float, c: TintaDemo) {
    /*
     * **Sube a «Fijadas», y se ve a donde sube.**
     *
     * La nota viajaba de abajo arriba sobre dos barras grises y no habia forma de saber que
     * arriba estaba la seccion de fijadas. Con el rotulo puesto, el viaje tiene destino: es lo
     * que convierte «una fila que se mueve» en «esta nota queda arriba del todo».
     */
    val avance = tramo(t, 0.14f, 0.68f)
    val p = suave(avance)
    val desde = 44f
    val hasta = 20f

    texto("FIJADAS", 12f, 11f, c, c.acento.copy(alpha = 0.75f), tamano = 6.5f)
    drawLine(
        color = c.pieza,
        start = Offset(12f, 15f),
        end = Offset(88f, 15f),
        strokeWidth = 1f,
        pathEffect = guiones
    )
    // La otra nota, la que se queda abajo.
    drawRoundRect(
        color = c.pieza,
        topLeft = Offset(12f, 44f),
        size = Size(76f, 12f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
    texto("Resumen tema 3", 17f, 50f, c, c.tinta.copy(alpha = 0.45f), tamano = 7f, negrita = false)

    fun nota(y: Float, x: Float = 12f, alfa: Float = 1f, escala: Float = 1f) {
        scale(escala, pivot = Offset(50f, y + 6f)) {
            drawRoundRect(
                color = c.acento.copy(alpha = alfa),
                topLeft = Offset(x, y),
                size = Size(76f, 12f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            texto("Fórmulas de examen", x + 5f, y + 6f, c, c.fondo, tamano = 7f, alfa = alfa)
        }
    }

    when (v) {
        "seco" -> nota(if (avance < 0.5f) desde else hasta)
        "salta" -> nota(desde + (hasta - desde) * p - sin(p * PI.toFloat()) * 10f)
        // Vuela en arco: se va por la derecha antes de subir.
        "vuela" -> nota(desde + (hasta - desde) * p, x = 12f + 24f * sin(p * PI.toFloat()))
        // El iman acelera: casi no se mueve al principio y llega de golpe.
        "iman" -> nota(desde + (hasta - desde) * (p * p * p))
        // Despega: primero se levanta —con su sombra— y despues viaja.
        "despega" -> {
            val alza = suave(tramo(t, 0.14f, 0.32f))
            val viaje = suave(tramo(t, 0.32f, 0.68f))
            val y = desde + (hasta - desde) * viaje
            drawRoundRect(
                color = c.tinta.copy(alpha = 0.20f * alza),
                topLeft = Offset(14f, y + 5f * alza),
                size = Size(76f, 12f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            nota(y, escala = 1f + 0.07f * alza)
        }
        "destello" -> {
            nota(desde + (hasta - desde) * p)
            if (p > 0.8f) {
                drawRoundRect(
                    color = c.acento.copy(alpha = 0.35f * (1f - tramo(p, 0.8f, 1f))),
                    topLeft = Offset(8f, hasta - 4f),
                    size = Size(84f, 20f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------- gastos y avisos

private fun DrawScope.presupuesto(v: String, t: Float, c: TintaDemo) {
    /*
     * **Las cifras a la vista, y el color diciendo lo que pasa.**
     *
     * Era una barra que crecia y nada mas. Pasarse del presupuesto no es «una barra larga»: es
     * «$61.000 de $50.000», y esa resta es lo unico que lo explica. Por eso las dos cifras van
     * escritas y el limite queda marcado en la pista con su linea.
     *
     * El color tambien cambia de significado a mitad: **ambar mientras vas justo, rojo cuando
     * te pasaste**. Antes iba todo del mismo tono, y con eso el aviso no avisaba de nada.
     */
    val avance = suave(tramo(t, 0.1f, 0.55f))
    val pista = Rect(12f, 33f, 88f, 43f)
    val lleno = 0.72f + 0.45f * avance
    val pasado = lleno > 1f
    val tono = if (pasado) c.rojo else c.ambar

    // El limite va **debajo** y no al lado: en una caja de dos centimetros, «$61.000» y
    // «de $50.000» en la misma linea se pisan, que es justo lo que pasaba.
    texto("Esta semana", 12f, 12f, c, c.tinta.copy(alpha = 0.6f), tamano = 7f, negrita = false)
    texto(
        if (pasado) "$61.000" else "$48.500",
        12f, 24f, c, tono, tamano = 13f
    )
    texto("límite $50.000", 12f, 51f, c, c.tinta.copy(alpha = 0.45f), tamano = 7f, negrita = false)

    drawRoundRect(
        color = c.pieza,
        topLeft = Offset(pista.left, pista.top),
        size = Size(pista.width, pista.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
    )

    val temblor = when (v) {
        "sacude" -> sin(t * 26f * PI.toFloat()) * 3.5f * (1f - avance)
        "alerta" -> if (avance > 0.85f) sin(t * 40f * PI.toFloat()) * 2f else 0f
        else -> 0f
    }
    translate(left = temblor) {
        when (v) {
            // Se pasa del final de verdad: el sobrante se sale de la pista y gotea.
            "desborda" -> clipRect(pista.left, pista.top - 16f, pista.right + 18f, pista.bottom + 18f) {
                drawRoundRect(
                    color = tono,
                    topLeft = Offset(pista.left, pista.top),
                    size = Size(pista.width * lleno, pista.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
                )
                if (pasado) {
                    val derrame = (lleno - 1f) * pista.width
                    drawCircle(tono, radius = 3.5f, center = Offset(pista.right + 3f, pista.bottom + derrame * 0.5f))
                    drawCircle(tono, radius = 2.5f, center = Offset(pista.right + 8f, pista.bottom + derrame))
                }
            }
            "grieta" -> drawRoundRect(
                color = tono.copy(alpha = 0.25f + 0.75f * avance),
                topLeft = Offset(pista.left, pista.top),
                size = Size(pista.width, pista.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
            )
            "parpadeo" -> drawRoundRect(
                color = tono.copy(alpha = if ((t * 6f).toInt() % 2 == 0) 1f else 0.3f),
                topLeft = Offset(pista.left, pista.top),
                size = Size(pista.width * min(lleno, 1f), pista.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
            )
            else -> drawRoundRect(
                color = tono,
                topLeft = Offset(pista.left, pista.top),
                size = Size(pista.width * min(lleno, 1f), pista.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
            )
        }
    }

    // El limite, marcado en la pista: sin esta linea no se ve donde estaba el tope.
    drawLine(
        color = c.tinta.copy(alpha = 0.55f),
        start = Offset(pista.right, pista.top - 3f),
        end = Offset(pista.right, pista.bottom + 3f),
        strokeWidth = 1.5f
    )

    when (v) {
        "alerta" -> if (avance > 0.5f) {
            triangulo(Offset(80f, 20f), 8f, c.rojo)
            texto("!", 80f, 22f, c, c.fondo, tamano = 8f, centrado = true)
        }
        "banner" -> {
            val baja = suave(tramo(t, 0.1f, 0.45f))
            drawRoundRect(
                color = c.rojo,
                topLeft = Offset(8f, -16f + 20f * baja),
                size = Size(84f, 16f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
            )
            texto("Te pasaste $11.000", 50f, -8f + 20f * baja, c, c.fondo, tamano = 8f, centrado = true, alfa = baja)
        }
        else -> Unit
    }
}

private fun DrawScope.triangulo(centro: Offset, radio: Float, color: Color) {
    val camino = Path().apply {
        moveTo(centro.x, centro.y - radio)
        lineTo(centro.x + radio, centro.y + radio * 0.8f)
        lineTo(centro.x - radio, centro.y + radio * 0.8f)
        close()
    }
    drawPath(camino, color)
}

private fun DrawScope.claseAhora(v: String, t: Float, c: TintaDemo) {
    /*
     * **La clase con su nombre, su hora y su «AHORA».**
     *
     * Sin los textos era una caja verde: no se sabia si estaba pasando algo o si aquello era
     * un estado bueno. La palabra «AHORA» es lo que lo convierte en «esto esta pasando», y la
     * hora es lo que da el contexto.
     *
     * Verde siempre, a proposito: es el color que dice «activo». Estuvo en el acento y ahi
     * competia con todo lo demas que va del acento.
     */
    val caja = Rect(10f, 14f, 90f, 50f)
    caja(caja, c.verde.copy(alpha = 0.16f), 1f)
    drawRoundRect(
        color = c.verde,
        topLeft = Offset(caja.left, caja.top),
        size = Size(caja.width, caja.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
        style = Stroke(1.8f)
    )
    texto("Cálculo III", 26f, 26f, c, c.tinta, tamano = 9f)
    texto("10:00 · 103F", 26f, 38f, c, c.tinta.copy(alpha = 0.6f), tamano = 7.5f, negrita = false)
    texto("AHORA", 78f, 22f, c, c.verde, tamano = 7f, centrado = true)

    when (v) {
        "quieta" -> drawCircle(c.verde, radius = 4f, center = Offset(18f, 32f))
        "respira" -> {
            val p = 0.5f + 0.5f * sin(t * 2f * PI.toFloat())
            caja(caja, c.verde.copy(alpha = 0.10f + 0.20f * p), 1f)
            drawCircle(c.verde, radius = 4f, center = Offset(18f, 32f))
        }
        "punto" -> {
            val late = 0.5f + 0.5f * sin(t * 4f * PI.toFloat())
            drawCircle(c.verde.copy(alpha = 0.35f), radius = 4f + 5f * late, center = Offset(18f, 32f))
            drawCircle(c.verde, radius = 4f, center = Offset(18f, 32f))
        }
        // Un punto de luz recorre el perimetro, esquinas incluidas.
        "recorre" -> {
            drawCircle(c.verde, radius = 4f, center = Offset(18f, 32f))
            val perimetro = 2f * (caja.width + caja.height)
            val d = (t * perimetro) % perimetro
            val punto = when {
                d < caja.width -> Offset(caja.left + d, caja.top)
                d < caja.width + caja.height -> Offset(caja.right, caja.top + (d - caja.width))
                d < 2f * caja.width + caja.height ->
                    Offset(caja.right - (d - caja.width - caja.height), caja.bottom)
                else -> Offset(caja.left, caja.bottom - (d - 2f * caja.width - caja.height))
            }
            drawCircle(c.verde, radius = 3.5f, center = punto)
        }
        "barre" -> {
            drawCircle(c.verde, radius = 4f, center = Offset(18f, 32f))
            clipRect(caja.left, caja.top, caja.right, caja.bottom) {
                val x = caja.left - 22f + (caja.width + 44f) * t
                rotate(degrees = 18f, pivot = Offset(x, caja.center.y)) {
                    drawRect(
                        color = c.verde.copy(alpha = 0.35f),
                        topLeft = Offset(x - 8f, caja.top - 14f),
                        size = Size(16f, caja.height + 28f)
                    )
                }
            }
        }
    }
}

private fun DrawScope.errorAviso(v: String, t: Float, c: TintaDemo) {
    /*
     * **Un campo con su rotulo y su mensaje, en rojo de peligro.**
     *
     * Era un rectangulo con contorno: no se sabia que era un campo ni que estaba mal. Con el
     * rotulo arriba, el hueco del texto y el mensaje debajo se lee como lo que es —un
     * formulario que no deja seguir— y el rojo del esquema es el unico color que dice eso. Con
     * el ambar que tenia antes parecia una sugerencia.
     */
    val avance = tramo(t, 0.1f, 0.6f)
    val temblor = if (v == "sacude") sin(t * 24f * PI.toFloat()) * 4.5f * (1f - avance) else 0f
    val parpadeo = if (v == "parpadea" && (t * 6f).toInt() % 2 == 1) 0.35f else 1f

    // El rotulo, mas arriba y mas pequeno: pegado al campo se metia dentro de su contorno.
    texto("Nombre de la materia", 13f, 11f, c, c.rojo.copy(alpha = parpadeo), tamano = 7f)
    translate(left = temblor) {
        drawRoundRect(
            color = c.rojo.copy(alpha = parpadeo),
            topLeft = Offset(12f, 19f),
            size = Size(76f, 20f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f),
            style = Stroke(2f)
        )
        // El cursor dentro, para que se lea como un campo vacio y no como una caja.
        drawLine(
            color = c.rojo.copy(alpha = parpadeo),
            start = Offset(19f, 24f),
            end = Offset(19f, 34f),
            strokeWidth = 1.5f
        )
    }

    // El mensaje: en «entra» llega desde arriba, en el resto ya esta.
    val p = if (v == "entra") suave(avance) else 1f
    texto(
        "Escribe un nombre",
        13f, 49f - 5f * (1f - p), c, c.rojo.copy(alpha = p * parpadeo), tamano = 7f, negrita = false
    )
}

// ---------------------------------------------------------------------- generales

private fun DrawScope.saludo(v: String, t: Float, c: TintaDemo) {
    /*
     * **El saludo escrito, como en el diseno.**
     *
     * Eran dos rectangulos, y con dos rectangulos «maquina de escribir» y «letra a letra» son
     * exactamente lo mismo: los dos crecen de izquierda a derecha. Con las palabras puestas se
     * ven distintas al instante —una revela caracteres con cursor y la otra los sube uno a uno—
     * que es justo lo que el diseno enseña.
     */
    val rotulo = "BUENAS TARDES"
    val nombre = "Kevin"

    when (v) {
        "golpe" -> {
            val p = if (t > 0.15f) 1f else 0f
            texto(rotulo, 12f, 22f, c, c.acento, tamano = 8f, alfa = p)
            texto(nombre, 12f, 40f, c, c.tinta, tamano = 18f, alfa = p)
        }

        // El rotulo entra, y el nombre despues: dos tiempos, no uno.
        "escalonado" -> {
            val a = suave(tramo(t, 0.1f, 0.35f))
            val b = suave(tramo(t, 0.28f, 0.55f))
            texto(rotulo, 12f, 22f + 6f * (1f - a), c, c.acento, tamano = 8f, alfa = a)
            texto(nombre, 12f, 40f + 8f * (1f - b), c, c.tinta, tamano = 18f, alfa = b)
        }

        // Maquina: revela caracteres y deja el cursor mientras escribe.
        "maquina" -> {
            texto(rotulo, 12f, 22f, c, c.acento, tamano = 8f)
            val letras = (suave(tramo(t, 0.15f, 0.7f)) * nombre.length).toInt()
            val escribiendo = letras < nombre.length
            texto(nombre.take(letras) + if (escribiendo && (t * 8f).toInt() % 2 == 0) "|" else "",
                12f, 40f, c, c.tinta, tamano = 18f)
        }

        // La cortina no mueve nada: las palabras ya estan y lo que baja es el corte.
        "cortina" -> {
            val corte = ALTO * suave(tramo(t, 0.1f, 0.6f))
            clipRect(0f, 0f, ANCHO, corte) {
                texto(rotulo, 12f, 22f, c, c.acento, tamano = 8f)
                texto(nombre, 12f, 40f, c, c.tinta, tamano = 18f)
            }
        }

        "lateral" -> {
            val p = suave(tramo(t, 0.1f, 0.55f))
            translate(left = -80f * (1f - p)) {
                texto(rotulo, 12f, 22f, c, c.acento, tamano = 8f)
                texto(nombre, 12f, 40f, c, c.tinta, tamano = 18f)
            }
        }

        // El desenfoque se finge con capas desplazadas: el de verdad vive en la app.
        "desenfoque" -> {
            val p = suave(tramo(t, 0.1f, 0.6f))
            val borron = 5f * (1f - p)
            listOf(-borron, 0f, borron).forEach { desvio ->
                texto(rotulo, 12f + desvio, 22f, c, c.acento, tamano = 8f, alfa = 0.35f + 0.65f * p)
                texto(nombre, 12f + desvio, 40f, c, c.tinta, tamano = 18f, alfa = 0.35f + 0.65f * p)
            }
        }

        // Letra a letra: cada caracter sube a su sitio con su turno. Sin cursor.
        "letras" -> {
            texto(rotulo, 12f, 22f, c, c.acento, tamano = 8f)
            var x = 12f
            nombre.forEachIndexed { indice, caracter ->
                val propio = suave(tramo(t, 0.12f + indice * 0.08f, 0.32f + indice * 0.08f))
                texto(caracter.toString(), x, 40f + 10f * (1f - propio), c, c.tinta, tamano = 18f, alfa = propio)
                x += 11f
            }
        }
    }
}

private fun DrawScope.fabScroll(v: String, t: Float, c: TintaDemo) {
    /*
     * **Se ve la lista bajando, que es lo que dispara el gesto.**
     *
     * El boton se encogia solo, sin motivo a la vista: no se entendia que lo que lo recoge es
     * **el scroll**. Ahora las filas suben mientras el se recoge, con su «+ Registrar» escrito,
     * asi que la causa y el efecto van juntos.
     */
    val recogido = suave(tramo(t, 0.22f, 0.6f))
    // Las filas suben: es el desplazamiento que recoge el boton.
    val desplazamiento = 26f * recogido
    clipRect(0f, 4f, ANCHO, 62f) {
        repeat(4) { indice ->
            val y = 10f + indice * 15f - desplazamiento
            drawRoundRect(
                color = c.pieza,
                topLeft = Offset(8f, y),
                size = Size(84f, 11f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.5f, 3.5f)
            )
        }
    }

    val y = 50f
    when (v) {
        "fijo" -> botonDeCrear(y, 1f, 1f, c)
        "encoge" -> botonDeCrear(y, 1f - recogido, 1f, c)
        "baja" -> translate(top = 28f * recogido) { botonDeCrear(y, 1f, 1f, c) }
        "desvanece" -> botonDeCrear(y, 1f, 1f - recogido, c)
    }
}

/** El boton de crear, con su texto mientras quepa. */
private fun DrawScope.botonDeCrear(y: Float, extension: Float, alfa: Float, c: TintaDemo) {
    val ancho = 22f + 40f * extension
    val izq = 90f - ancho
    drawRoundRect(
        color = c.acento.copy(alpha = alfa),
        topLeft = Offset(izq, y - 11f),
        size = Size(ancho, 22f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(11f, 11f)
    )
    texto("+", izq + 11f, y, c, c.fondo, tamano = 12f, centrado = true, alfa = alfa)
    // El rotulo se va antes que el ancho: es lo que hace que «encoge» se lea como que pierde
    // el texto y no como que se estruja.
    texto("Registrar", izq + 20f, y, c, c.fondo, tamano = 8f, alfa = alfa * tramo(extension, 0.45f, 0.9f))
}

private fun DrawScope.haptica(v: String, t: Float, c: TintaDemo) {
    /*
     * **Un telefono que vibra, no un ecualizador.**
     *
     * Las barras de onda parecian una app de musica, y con razon: una forma de onda es lo que
     * dibuja un reproductor. Lo que hay que ensenar aqui es **cuanto se sacude el aparato en la
     * mano**, asi que se dibuja el telefono moviendose y las ondas saliendo de el. Con «Nada»
     * el telefono se queda quieto y no sale ninguna, que es exactamente la diferencia.
     */
    val fuerza = when (v) {
        "ninguna" -> 0f
        "suave" -> 0.3f
        "fuerte" -> 1f
        else -> 0.6f
    }
    // Dos golpes al principio del ciclo y silencio: una vibracion es un aviso, no un zumbido.
    val golpe = when {
        t < 0.12f -> (1f - t / 0.12f)
        t in 0.18f..0.30f -> (1f - (t - 0.18f) / 0.12f)
        else -> 0f
    }
    val sacudida = sin(t * 60f * PI.toFloat()) * 3.2f * fuerza * golpe

    // Las ondas que salen a los lados, tantas como fuerza haya.
    val ondas = (fuerza * 3f).toInt()
    repeat(ondas) { indice ->
        val radio = 20f + indice * 9f + 5f * golpe
        val alfa = (0.5f - indice * 0.13f) * golpe
        listOf(-1f, 1f).forEach { lado ->
            drawArc(
                color = c.acento.copy(alpha = alfa.coerceAtLeast(0f)),
                startAngle = if (lado < 0) 120f else -60f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(50f - radio, 32f - radio),
                size = Size(radio * 2f, radio * 2f),
                style = Stroke(2.4f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
    }

    translate(left = sacudida) {
        // El telefono: marco, pantalla y su muesca.
        drawRoundRect(
            color = c.tinta.copy(alpha = 0.85f),
            topLeft = Offset(41f, 13f),
            size = Size(18f, 38f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.5f, 4.5f)
        )
        drawRoundRect(
            color = c.fondo,
            topLeft = Offset(43f, 17f),
            size = Size(14f, 30f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f, 2.5f)
        )
        drawRoundRect(
            color = c.tinta.copy(alpha = 0.85f),
            topLeft = Offset(47f, 15f),
            size = Size(6f, 1.6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f, 1f)
        )
    }

    if (fuerza == 0f) {
        texto("sin aviso", 50f, 58f, c, c.tinta.copy(alpha = 0.4f), tamano = 7f, centrado = true, negrita = false)
    }
}

// ---------------------------------------------------------------------- utilidad

/** Dos colores mezclados, para lo que viaja de un tono a otro. */
private fun mezclar(desde: Color, hasta: Color, fraccion: Float): Color {
    val f = fraccion.coerceIn(0f, 1f)
    return Color(
        red = desde.red + (hasta.red - desde.red) * f,
        green = desde.green + (hasta.green - desde.green) * f,
        blue = desde.blue + (hasta.blue - desde.blue) * f,
        alpha = desde.alpha + (hasta.alpha - desde.alpha) * f
    )
}

/** Sin uso directo aún; queda por si una variante necesita línea discontinua. */
internal val guiones: PathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))

// ---------------------------------------------------------------------- los cuatro de «otros»

/**
 * Los cuatro interruptores del final tambien se ven.
 *
 * Eran los unicos de la pantalla sin nada que mirar: un «Parallax en carruseles» encendido o
 * apagado no dice que hace el parallax. Cada uno pinta las dos caras a la vez —encendido a la
 * izquierda, apagado a la derecha— porque lo que hay que juzgar aqui no es una variante entre
 * varias sino la diferencia entre tenerlo y no tenerlo.
 */
internal fun DrawScope.pintarInterruptor(id: String, encendido: Boolean, t: Float, c: TintaDemo) {
    when (id) {
        "barraAnim" -> barraAnimada(encendido, t, c)
        "gestos" -> deslizarEnLista(encendido, t, c)
        "numeros" -> numerosQueCuentan(encendido, t, c)
    }
}

/** La pastilla del activo saltando entre tres pestanas, deslizandose o apareciendo de golpe. */
private fun DrawScope.barraAnimada(encendido: Boolean, t: Float, c: TintaDemo) {
    val paso = (t * 3f)
    val destino = paso.toInt().coerceIn(0, 2)
    val fraccion = suave((paso - destino).coerceIn(0f, 1f) * 2.2f)
    val anterior = if (destino == 0) 2 else destino - 1
    val posicion = if (encendido) {
        anterior + (destino - anterior) * fraccion
    } else {
        // Apagada, la pastilla no viaja: esta en una pestana o en la otra.
        destino.toFloat()
    }
    val ancho = 26f
    val hueco = 30f
    val inicio = 10f
    drawRoundRect(
        color = c.acento,
        topLeft = Offset(inicio + posicion * hueco, 24f),
        size = Size(ancho, 16f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
    )
    repeat(3) { indice ->
        val cx = inicio + indice * hueco + ancho / 2f
        drawCircle(
            color = if (indice == destino) c.fondo else c.pieza,
            radius = 4f,
            center = Offset(cx, 32f)
        )
        drawRoundRect(
            color = if (indice == destino) c.fondo else c.pieza,
            topLeft = Offset(cx - 7f, 40f),
            size = Size(14f, 3f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f, 1.5f)
        )
    }
}

/** Una fila que se arrastra dejando ver el fondo de borrar, o que no se mueve. */
private fun DrawScope.deslizarEnLista(encendido: Boolean, t: Float, c: TintaDemo) {
    fila(12f, c.pieza)
    fila(42f, c.pieza)
    val avance = if (!encendido) 0f else {
        val ida = tramo(t, 0.15f, 0.5f)
        val vuelta = tramo(t, 0.62f, 0.9f)
        suave(ida) - suave(vuelta)
    }
    if (avance > 0.02f) {
        // El fondo rojo que asoma por detras: es lo que dice para que sirve el gesto.
        drawRoundRect(
            color = c.rojo,
            topLeft = Offset(12f, 27f),
            size = Size(76f, 11f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.5f, 3.5f)
        )
        drawCircle(color = c.fondo, radius = 3f, center = Offset(80f, 32.5f))
    }
    translate(left = -52f * avance) { fila(27f, c.acento) }
}

/** Una cifra subiendo desde cero, o puesta de golpe. */
private fun DrawScope.numerosQueCuentan(encendido: Boolean, t: Float, c: TintaDemo) {
    val objetivo = 0.82f
    val valor = if (!encendido) {
        if (t < 0.12f) 0f else objetivo
    } else {
        objetivo * suave(tramo(t, 0.1f, 0.7f))
    }
    // El importe, como barra de digitos que crece: cuantos mas «digitos», mas alto el numero.
    val digitos = (valor * 5f).toInt().coerceAtLeast(1)
    repeat(digitos) { indice ->
        drawRoundRect(
            color = c.acento,
            topLeft = Offset(16f + indice * 13f, 22f),
            size = Size(10f, 20f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5f, 2.5f)
        )
    }
    drawRoundRect(
        color = c.pieza,
        topLeft = Offset(16f, 46f),
        size = Size(64f, 4f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
    )
}

