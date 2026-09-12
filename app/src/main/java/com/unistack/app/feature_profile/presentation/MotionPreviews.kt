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
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.unistack.app.core.design.components.pincelDeCometa
import com.unistack.app.core.design.theme.LocalSectionColors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import com.unistack.app.core.utils.Textos
import com.unistack.app.R
import com.unistack.app.feature_user.domain.Corte

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
        /*
         * **El rojo es el de la app, no el `error` del esquema.**
         *
         * En oscuro, el `error` de Material es un salmon claro —esta pensado para leerse como
         * texto sobre fondo negro, no para gritar—, y con el todos los avisos salian color
         * piel: ni peligro, ni riesgo, ni alerta. El rojo de Gastos es un rojo de verdad en
         * los dos modos, y es **el rojo que esta app ya tiene**.
         */
        rojo = secciones.expenses,
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
        /*
         * **La escala tiene que ser la misma en los dos ejes.**
         *
         * Estirando cada eje por su cuenta, la caja real —mas ancha que alta— aplastaba el
         * dibujo: un circulo salia ovalado y cada letra un tercio mas ancha de lo que le toca.
         * Eso es lo que se veia como «relacion de aspecto extrana», y ningun retoque de un
         * dibujo suelto lo arreglaba porque el fallo estaba aqui.
         *
         * Con una sola escala y el lienzo centrado, lo que sobra es margen a los lados, no
         * deformacion. La caja de variante mide justo para que ese margen sea casi nada.
         */
        val escala = minOf(size.width / ANCHO, size.height / ALTO)
        val margenX = (size.width - ANCHO * escala) / 2f
        val margenY = (size.height - ALTO * escala) / 2f
        translate(left = margenX, top = margenY) {
            scale(scaleX = escala, scaleY = escala, pivot = Offset.Zero) {
                dibujo(t)
            }
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
    /*
     * **El tamano y la medida van en unidades del lienzo, las dos.**
     *
     * Aqui habia el fallo que descolocaba los ciento treinta dibujos. `measure` devuelve
     * pixeles, y dentro de la transformacion un pixel **es** una unidad del lienzo; al dividir
     * por la densidad, el ancho salia casi tres veces menor que el de verdad. Con eso, centrar
     * corria el texto a la derecha en vez de centrarlo y la `y` lo dejaba caido: de ahi los
     * rotulos pisando los dibujos y las cifras saliendose por el borde.
     *
     * `toSp` es la conversion correcta —divide por densidad **y** por la escala de fuente del
     * sistema—, asi que un texto de ocho mide ocho unidades tenga el movil la letra que tenga.
     * Con `tamano / density` a secas, quien lleve la letra grande veia estos dibujos rotos.
     */
    val estilo = TextStyle(
        color = color.copy(alpha = color.alpha * alfa),
        fontSize = tamano.toSp(),
        fontWeight = if (negrita) FontWeight.ExtraBold else FontWeight.Normal
    )
    val medida = tinta.medidor.measure(valor, estilo)
    val ancho = medida.size.width.toFloat()
    val alto = medida.size.height.toFloat()
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
        "sello" -> sello(variante, t, tinta)
        "cierreSem" -> cierreSemestre(variante, t, tinta)
        "celebracion" -> celebracion(variante, t, tinta)
        "deshacer" -> deshacer(variante, t, tinta)
        "claseAhora" -> claseAhora(variante, t, tinta)
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
    val sale = Textos.get(R.string.academic_tab_subjects)
    val entra = Textos.get(R.string.preview_calculus)
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
        "arriba" -> {
            panel(13f, c.pieza, sale, c)
            translate(top = -62f * (1f - avance)) { panel(13f, c.acento, entra, c) }
        }
        // Las dos se mueven, como en el eje horizontal: la que sale solo recorre un tercio.
        "ejeV" -> {
            translate(top = -21f * avance) { panel(13f, c.pieza, sale, c) }
            translate(top = 62f * (1f - avance)) { panel(13f, c.acento, entra, c) }
        }
        // La de debajo no se va: se encoge y se apaga a medias, y se sigue viendo.
        "tarjeta" -> {
            panel(13f, c.pieza, sale, c, alfa = 1f - 0.45f * avance, escala = 1f - 0.14f * avance)
            translate(top = 62f * (1f - avance)) { panel(13f, c.acento, entra, c) }
        }
        "diagonal" -> {
            panel(13f, c.pieza, sale, c, alfa = 1f - avance)
            translate(left = 88f * (1f - avance), top = 58f * (1f - avance)) {
                panel(13f, c.acento, entra, c)
            }
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
    val nombres = listOf(Textos.get(R.string.settings_motion_demo_task), Textos.get(R.string.tasks_type_midterm), Textos.get(R.string.preview_quiz_3))
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
            // Cascada cae desde justo encima de su sitio y no desde el de la fila anterior:
            // con dieciocho, la segunda se dibujaba sobre la primera y se veian cruzadas.
            "cascada" -> {
                val p = suave(bruto)
                translate(top = -11f * (1f - p)) { tarea(alfa = p * p) }
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
    texto("${Corte.Singular} 2", 17f, 22f, c, c.tinta.copy(alpha = 0.55f), tamano = 8f, negrita = false)
    texto("4,25", 17f, 38f, c, c.tinta.copy(alpha = 0.5f), tamano = 14f)

    val centro = Offset(56f, 36f)
    /*
     * **Las mismas fases que el sello de verdad.**
     *
     * La demo iba con una curva suave de principio a fin, y la pieza ya no: cae rapido, pega
     * y la onda se va. Si la vista previa flotara y la tarjeta golpeara, se elegiria una cosa
     * y se veria otra. El reloj de la demo tiene un tramo muerto al principio y al final para
     * que el bucle no empalme el asentado con la caida siguiente.
     */
    val reloj = tramo(t, 0.08f, 0.86f)
    val asentado = suave(tramo(reloj, 0.62f, 1f))
    val vida = 1f - 0.78f * asentado
    val ancho = 60f
    val alto = 22f

    fun palabra(color: Color, alfa: Float, tamano: Float = 7f) =
        texto("CERRADO", centro.x, centro.y, c, color, tamano = tamano, centrado = true, alfa = alfa)

    fun marco(escala: Float, alfa: Float, giro: Float) {
        rotate(degrees = giro, pivot = centro) {
            scale(escala, pivot = centro) {
                drawRoundRect(
                    color = c.verde.copy(alpha = alfa),
                    topLeft = Offset(centro.x - ancho / 2f, centro.y - alto / 2f),
                    size = Size(ancho, alto),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f),
                    style = Stroke(2.2f)
                )
                // El marco interior fino: lo que lo hace sello de goma y no un recuadro.
                drawRoundRect(
                    color = c.verde.copy(alpha = alfa * 0.8f),
                    topLeft = Offset(centro.x - ancho / 2f + 3.6f, centro.y - alto / 2f + 3.6f),
                    size = Size(ancho - 7.2f, alto - 7.2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f),
                    style = Stroke(0.7f)
                )
                palabra(c.verde, alfa)
            }
        }
    }

    when (v) {
        "ninguna" -> marco(1f, 1f, -14f)
        // Cae acelerando, pega, y una onda con su forma se expande y se apaga.
        "estampa" -> {
            val caida = tramo(reloj, 0f, 0.16f)
            val cae = caida * caida * caida
            val onda = tramo(reloj, 0.16f, 0.46f)
            if (onda > 0f && onda < 1f) {
                val e = 1f - (1f - onda) * (1f - onda) * (1f - onda)
                rotate(degrees = -14f, pivot = centro) {
                    drawRoundRect(
                        color = c.verde.copy(alpha = 0.28f * (1f - e)),
                        topLeft = Offset(centro.x - ancho / 2f, centro.y - alto / 2f),
                        size = Size(ancho, alto),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
                    )
                    val a = ancho * (1f + 0.42f * e)
                    val h = alto * (1f + 0.42f * e)
                    drawRoundRect(
                        color = c.verde.copy(alpha = 0.55f * (1f - e)),
                        topLeft = Offset(centro.x - a / 2f, centro.y - h / 2f),
                        size = Size(a, h),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                        style = Stroke(0.6f + 1.4f * (1f - e))
                    )
                }
            }
            marco(1.9f - 0.9f * cae, tramo(caida, 0f, 0.35f) * vida, -22f + 8f * cae)
        }
        // Una gota, la mancha que se corre y la palabra que sube de ella como bloque.
        "tinta" -> {
            val gota = 1f - (1f - tramo(reloj, 0f, 0.12f)).let { it * it * it }
            val mancha = tramo(reloj, 0.10f, 0.55f).let { 1f - (1f - it) * (1f - it) * (1f - it) * (1f - it) }
            val sube = tramo(reloj, 0.22f, 0.60f).let { 1f - (1f - it) * (1f - it) * (1f - it) }
            listOf(
                Offset(0f, 0f) to 26f,
                Offset(-13f, -5f) to 15f,
                Offset(14f, 4f) to 17f,
                Offset(-4f, 13f) to 14f
            ).forEach { (d, radio) ->
                val rr = radio * (0.12f + 0.88f * mancha)
                drawCircle(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        0f to c.verde.copy(alpha = 0.22f * vida),
                        0.7f to c.verde.copy(alpha = 0.13f * vida),
                        1f to c.verde.copy(alpha = 0f),
                        center = centro + d,
                        radius = rr.coerceAtLeast(0.5f)
                    ),
                    radius = rr,
                    center = centro + d
                )
            }
            drawCircle(c.verde.copy(alpha = 0.9f * gota * (1f - mancha)), radius = 3f * gota, center = centro)
            if (sube > 0f) {
                rotate(degrees = -8f, pivot = centro) {
                    scale(0.9f + 0.1f * sube, pivot = centro) {
                        drawRoundRect(
                            color = c.verde.copy(alpha = sube * vida),
                            topLeft = Offset(centro.x - ancho / 2f, centro.y - alto / 2f),
                            size = Size(ancho, alto),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
                        )
                        palabra(c.fondo, sube * (0.45f + 0.55f * vida), tamano = 8f)
                    }
                }
            }
        }
        // Se desenrolla con un tiron al final, con las puntas en diagonal, y destapa la palabra.
        "cinta" -> {
            val tira = muelle(tramo(reloj, 0f, 0.45f), 0f)
            val grosor = 15f
            val largo = 108f * tira
            rotate(degrees = -12f, pivot = Offset(50f, 32f)) {
                val x0 = -4f
                val y0 = 32f - grosor / 2f
                val corte = grosor * 0.35f
                val camino = Path().apply {
                    moveTo(x0, y0)
                    lineTo(x0 + largo, y0)
                    lineTo(x0 + largo - corte, y0 + grosor)
                    lineTo(x0 + corte, y0 + grosor)
                    close()
                }
                drawPath(camino, c.verde.copy(alpha = 0.9f * vida))
                if (largo > corte) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.22f * vida),
                        start = Offset(x0 + corte * 0.6f, y0 + grosor * 0.27f),
                        end = Offset(x0 + largo - corte * 0.6f, y0 + grosor * 0.27f),
                        strokeWidth = 0.9f
                    )
                }
                clipRect(right = x0 + largo - corte) {
                    texto("CERRADO", 50f, 32f, c, c.fondo, tamano = 8.5f, centrado = true, alfa = vida)
                }
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
        // 7,5 y no 8,5: quince letras a ocho y medio miden setenta y cuatro unidades y la
        // pildora setenta y seis, asi que el titulo salia tocando los dos bordes.
        texto(Textos.get(R.string.preview_semester_name), 50f, 16f, c, c.fondo, tamano = 7.5f, centrado = true, alfa = alfa)
    }

    fun cifra(indice: Int, alfa: Float, desvio: Float = 0f) {
        if (alfa <= 0.02f) return
        val datos = listOf("4,25" to Textos.get(R.string.preview_stat_avg), "6" to Textos.get(R.string.preview_stat_subjects), "18" to Textos.get(R.string.preview_stat_credits))
        // Veintiseis de ancho y no veinticuatro: la cifra a nueve mide veintiuna unidades, y
        // en una caja de veinticuatro quedaba pegada a los dos lados.
        val x = 10f + indice * 27f
        translate(top = desvio) {
            drawRoundRect(
                color = c.pieza.copy(alpha = c.pieza.alpha * alfa),
                topLeft = Offset(x, 30f),
                size = Size(26f, 24f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            texto(datos[indice].first, x + 13f, 39f, c, c.verde, tamano = 8.5f, centrado = true, alfa = alfa)
            texto(datos[indice].second, x + 13f, 49f, c, c.tinta.copy(alpha = 0.5f), tamano = 6f, centrado = true, alfa = alfa)
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
        "ninguna" -> texto(Textos.get(R.string.celebration_all_done), 50f, 32f, c, c.tinta, tamano = 11f, centrado = true)

        // Dos canones en las esquinas de abajo, como en la pantalla: cada papel sube frenando,
        // cae y voltea. Es la misma fisica de la pieza, a escala.
        "confeti" -> {
            val colores = listOf(c.acento, c.verde, c.ambar, c.gasto)
            repeat(28) { indice ->
                val azar = kotlin.random.Random(indice * 7919 + 13)
                val izquierda = indice % 2 == 0
                val retraso = azar.nextFloat() * 0.16f
                val tau = ((t - 0.05f - retraso) / (0.95f - retraso)).coerceIn(0f, 1f)
                if (tau <= 0f || tau >= 1f) return@repeat
                val boca = Offset(if (izquierda) 8f else 92f, 66f)
                val empuje = 0.45f + azar.nextFloat() * 0.5f
                val lateral = (0.25f + azar.nextFloat() * 0.75f) * (if (izquierda) 1f else -1f)
                val punto = Offset(
                    boca.x + 100f * lateral * tau,
                    boca.y - 64f * empuje * (2.2f * tau - 1.7f * tau * tau)
                )
                val alfa = if (tau < 0.66f) 1f else 1f - (tau - 0.66f) / 0.34f
                val volteo = abs(cos(tau * (6f + azar.nextFloat() * 8f)))
                rotate(degrees = azar.nextFloat() * 360f + (1f + azar.nextFloat() * 2.5f) * 360f * tau, pivot = punto) {
                    drawRect(
                        color = colores[indice % colores.size].copy(alpha = alfa),
                        topLeft = punto - Offset(2.6f * (0.25f + 0.75f * volteo), 1.4f),
                        size = Size(5.2f * (0.25f + 0.75f * volteo), 2.8f)
                    )
                }
            }
            scale(0.7f + 0.3f * entrada, pivot = centro) {
                texto(Textos.get(R.string.celebration_all_done), 50f, 32f, c, c.acento, tamano = 11f, centrado = true, alfa = entrada)
            }
        }

        "onda" -> {
            val resplandor = (1f - t / 0.5f).coerceIn(0f, 1f)
            if (resplandor > 0f) {
                drawCircle(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        0f to c.acento.copy(alpha = 0.30f * resplandor),
                        1f to c.acento.copy(alpha = 0f),
                        center = centro,
                        radius = 60f
                    ),
                    radius = 60f,
                    center = centro
                )
            }
            repeat(4) { indice ->
                val p = tramo(t, 0.05f + indice * 0.13f, 0.67f + indice * 0.13f)
                if (p > 0f && p < 1f) {
                    val e = 1f - (1f - p) * (1f - p) * (1f - p)
                    drawCircle(
                        color = c.acento.copy(alpha = 0.7f * (1f - e)),
                        radius = 8f + 54f * e,
                        center = centro,
                        style = Stroke(4f * (1f - 0.7f * e) + 0.6f)
                    )
                }
            }
            texto(Textos.get(R.string.celebration_all_done), 50f, 32f, c, c.acento, tamano = 11f, centrado = true)
        }

        // El aro cae de fuera hacia dentro, pega con su onda, y dentro se dibuja el visto.
        "sello" -> {
            val caida = tramo(t, 0.05f, 0.33f).let { it * it * it }
            val radio = 40f - 22f * caida
            val onda = tramo(t, 0.33f, 0.65f)
            val trazo = tramo(t, 0.35f, 0.6f).let { 1f - (1f - it) * (1f - it) * (1f - it) }
            val vida = 1f - suave(tramo(t, 0.75f, 1f))
            val alfa = (tramo(t, 0.05f, 0.33f) / 0.4f).coerceAtMost(1f) * vida
            if (onda > 0f && onda < 1f) {
                val e = 1f - (1f - onda) * (1f - onda) * (1f - onda)
                drawCircle(c.verde.copy(alpha = 0.55f * (1f - e)), radius = 18f + 50f * e, center = centro, style = Stroke(3f * (1f - e) + 0.4f))
            }
            drawCircle(c.verde.copy(alpha = alfa), radius = radio, center = centro, style = Stroke(4f))
            if (trazo > 0f) {
                val a = centro + Offset(-radio * 0.42f, radio * 0.02f)
                val b = centro + Offset(-radio * 0.12f, radio * 0.32f)
                val cc = centro + Offset(radio * 0.46f, -radio * 0.30f)
                val primero = (trazo / 0.4f).coerceAtMost(1f)
                val segundo = tramo(trazo, 0.4f, 1f)
                drawLine(c.verde.copy(alpha = alfa), a, a + (b - a) * primero, 4f, androidx.compose.ui.graphics.StrokeCap.Round)
                if (segundo > 0f) drawLine(c.verde.copy(alpha = alfa), b, b + (cc - b) * segundo, 4f, androidx.compose.ui.graphics.StrokeCap.Round)
            }
        }

        "destello" -> {
            val rayos = tramo(t, 0.05f, 0.6f).let { 1f - (1f - it) * (1f - it) * (1f - it) }
            val vida = 1f - suave(tramo(t, 0.55f, 1f))
            rotate(degrees = t * 24f, pivot = centro) {
                repeat(12) { indice ->
                    val angulo = indice / 12f * 2f * PI.toFloat()
                    val dentro = 8f + 14f * rayos
                    val fuera = dentro + 30f * rayos
                    drawLine(
                        color = c.ambar.copy(alpha = 0.55f * vida),
                        start = centro + Offset(cos(angulo) * dentro, sin(angulo) * dentro),
                        end = centro + Offset(cos(angulo) * fuera, sin(angulo) * fuera),
                        strokeWidth = 2f * (1f - 0.6f * rayos) + 0.4f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
            repeat(12) { indice ->
                val azar = kotlin.random.Random(indice * 4241 + 7)
                val desde = azar.nextFloat() * 0.6f
                val p = tramo(t, desde, desde + 0.38f)
                if (p <= 0f || p >= 1f) return@repeat
                val vivo = sin(p * PI.toFloat())
                val r = (3f + azar.nextFloat() * 4f) * vivo
                val punto = Offset(6f + azar.nextFloat() * 88f, 5f + azar.nextFloat() * 54f)
                val estrella = Path().apply {
                    moveTo(punto.x, punto.y - r)
                    quadraticTo(punto.x, punto.y, punto.x + r, punto.y)
                    quadraticTo(punto.x, punto.y, punto.x, punto.y + r)
                    quadraticTo(punto.x, punto.y, punto.x - r, punto.y)
                    quadraticTo(punto.x, punto.y, punto.x, punto.y - r)
                    close()
                }
                drawPath(estrella, color = (if (azar.nextFloat() < 0.7f) c.ambar else c.tinta).copy(alpha = vivo))
            }
            texto(Textos.get(R.string.celebration_all_done), 50f, 32f, c, c.ambar, tamano = 11f, centrado = true)
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
            if (alto > 6f) texto(Textos.get(R.string.settings_motion_demo_task), x + 5f, y + 6f, c, c.fondo, tamano = 7f, alfa = alfa)
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
    texto(Textos.get(R.string.action_undo), 12f, 58f, c, c.acento.copy(alpha = 0.9f), tamano = 7.5f)
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
    // La tarjeta baja y se estrecha de alto: la insignia ya no vive dentro, asi que no hace
    // falta reservarle una franja y el aire de arriba y el de abajo quedan iguales.
    val caja = Rect(10f, 23f, 90f, 51f)
    caja(caja, c.verde.copy(alpha = 0.16f), 1f)
    drawRoundRect(
        color = c.verde,
        topLeft = Offset(caja.left, caja.top),
        size = Size(caja.width, caja.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
        style = Stroke(1.8f)
    )
    /*
     * **«AHORA» va fuera de la tarjeta, sobre su esquina superior derecha.**
     *
     * Dentro obligaba a reservarle una franja entera arriba, y por eso la tarjeta salia con un
     * hueco vertical enorme y el nombre descolgado hacia abajo. Fuera, la tarjeta se ajusta a
     * sus dos renglones —mismo aire arriba que abajo— y la insignia se lee como lo que es:
     * una marca pegada a la esquina, alineada con el borde derecho.
     */
    drawRoundRect(
        color = c.verde,
        topLeft = Offset(64f, 12f),
        size = Size(26f, 10f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
    )
    texto("AHORA", 77f, 17f, c, c.fondo, tamano = 6f, centrado = true)
    texto(Textos.get(R.string.preview_calculus), 28f, 32f, c, c.tinta, tamano = 8.5f)
    texto("10:00 · 103F", 28f, 43f, c, c.tinta.copy(alpha = 0.6f), tamano = 7f, negrita = false)

    when (v) {
        "quieta" -> drawCircle(c.verde, radius = 4f, center = Offset(19f, 37f))
        "respira" -> {
            val p = 0.5f + 0.5f * sin(t * 2f * PI.toFloat())
            caja(caja, c.verde.copy(alpha = 0.10f + 0.20f * p), 1f)
            drawCircle(c.verde, radius = 4f, center = Offset(19f, 37f))
        }
        "punto" -> {
            val late = 0.5f + 0.5f * sin(t * 4f * PI.toFloat())
            drawCircle(c.verde.copy(alpha = 0.35f), radius = 4f + 5f * late, center = Offset(19f, 37f))
            drawCircle(c.verde, radius = 4f, center = Offset(19f, 37f))
        }
        // Una luz que da la vuelta al contorno: el mismo pincel que lleva la fila de verdad.
        "recorre" -> {
            drawCircle(c.verde, radius = 4f, center = Offset(19f, 37f))
            val pincel = pincelDeCometa(c.verde, t, caja.center)
            drawRoundRect(
                brush = pincel,
                topLeft = Offset(caja.left, caja.top),
                size = Size(caja.width, caja.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                style = Stroke(5f),
                alpha = 0.35f
            )
            drawRoundRect(
                brush = pincel,
                topLeft = Offset(caja.left, caja.top),
                size = Size(caja.width, caja.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                style = Stroke(2f)
            )
        }
        "barre" -> {
            drawCircle(c.verde, radius = 4f, center = Offset(19f, 37f))
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

// ---------------------------------------------------------------------- generales

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
    // Un halo del color del fondo por detras: el boton **flota sobre** la lista, y sin ese
    // hueco las filas le pasaban por encima y parecia que estuviera roto.
    drawRoundRect(
        color = c.fondo,
        topLeft = Offset(izq - 3.5f, y - 14.5f),
        size = Size(ancho + 7f, 29f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.5f, 14.5f)
    )
    drawRoundRect(
        color = c.acento.copy(alpha = alfa),
        topLeft = Offset(izq, y - 11f),
        size = Size(ancho, 22f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(11f, 11f)
    )
    texto("+", izq + 11f, y, c, c.fondo, tamano = 12f, centrado = true, alfa = alfa)
    // El rotulo se va antes que el ancho: es lo que hace que «encoge» se lea como que pierde
    // el texto y no como que se estruja.
    texto(Textos.get(R.string.tasks_action_record), izq + 20f, y, c, c.fondo, tamano = 7f, alfa = alfa * tramo(extension, 0.45f, 0.9f))
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
        texto(Textos.get(R.string.schedule_no_reminder_short).lowercase(), 50f, 58f, c, c.tinta.copy(alpha = 0.4f), tamano = 7f, centrado = true, negrita = false)
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

