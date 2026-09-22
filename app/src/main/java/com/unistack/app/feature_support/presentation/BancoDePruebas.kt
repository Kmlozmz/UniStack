@file:OptIn(ExperimentalMaterial3Api::class)

package com.unistack.app.feature_support.presentation

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.BuildConfig
import com.unistack.app.core.design.theme.tonosDeAjustes
import com.unistack.app.core.fallos.AlmacenDeFallos
import com.unistack.app.core.fallos.falloDeMuestra
import com.unistack.app.core.fallos.informeDeMuestra
import com.unistack.app.core.navigation.AppRoutes
import com.unistack.app.core.utils.BuildStage
import com.unistack.app.core.utils.performSafely
import com.unistack.app.feature_terms.presentation.HistoricoDeMuestra
import com.unistack.app.feature_user.domain.MotionCatalog
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * El panel de taller: una ventana flotante con lo que hace falta para probar la app.
 *
 * **Probar una animación costaba más que escribirla.** Ver el aviso de pasarse del presupuesto
 * pedía ir a Gastos, abrir el presupuesto, poner una cifra, volver y registrar gastos hasta
 * cruzarla; cambiarle la variante pedía cuatro pantallas de ida y cuatro de vuelta.
 *
 * Es **una ventana y no una hoja** a propósito: una hoja tapa la pantalla que se está mirando,
 * que es justo lo que aquí no puede pasar — se cambia una variante y se ve el efecto detrás,
 * sin cerrar nada.
 *
 * **Se mueve por toda la pantalla y se ancla sola** (22 sep 2026). Antes la burbuja vivía
 * clavada en una esquina y la ventana sólo se apartaba con un arrastre libre que la dejaba
 * medio fuera o debajo del teclado: ahora las dos se sueltan donde sea y se acomodan a la
 * esquina más cercana, que es una posición de la que no se pierden.
 *
 * Solo en dev y alpha. La beta se reparte, y ahí una ventana que fabrica clases y gastos falsos
 * no tiene nada que hacer.
 */
@Composable
fun BancoDePruebas(
    modifier: Modifier = Modifier,
    /** Lo que ocupa la barra de abajo: ni la burbuja ni la ventana deben quedar debajo. */
    insetInferior: Dp = 0.dp,
    onAbrirMovimiento: () -> Unit,
    /** Para las palancas que dejan un estado que se mira en otra pantalla: el histórico, Inicio. */
    onNavegar: (String) -> Unit = {}
) {
    val etapa = BuildStage.of(BuildConfig.VERSION_NAME)
    if (etapa != BuildStage.DEV && etapa != BuildStage.ALPHA) return

    var abierto by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier) {
        val densidad = LocalDensity.current
        val ancho = with(densidad) { maxWidth.toPx() }
        val alto = with(densidad) { maxHeight.toPx() }
        val margen = with(densidad) { 14.dp.toPx() }
        val fondo = with(densidad) { insetInferior.toPx() }

        if (!abierto) {
            BurbujaDePruebas(
                ancho = ancho,
                alto = alto,
                margen = margen,
                fondo = fondo,
                onAbrir = { abierto = true }
            )
        } else {
            VentanaDePruebas(
                ancho = ancho,
                alto = alto,
                margen = margen,
                fondo = fondo,
                onCerrar = { abierto = false },
                onAbrirMovimiento = onAbrirMovimiento,
                onNavegar = onNavegar
            )
        }
    }
}

/** El lado de la burbuja. Fuera de la función porque el ancla necesita la medida antes de pintar. */
private val LADO_BURBUJA = 46.dp

@Composable
private fun BurbujaDePruebas(
    ancho: Float,
    alto: Float,
    margen: Float,
    fondo: Float,
    onAbrir: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val alcance = rememberCoroutineScope()
    val lado = with(LocalDensity.current) { LADO_BURBUJA.toPx() }
    val posicion = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var colocada by remember { mutableStateOf(false) }

    LaunchedEffect(ancho, alto, fondo) {
        if (ancho <= 0f || alto <= 0f) return@LaunchedEffect
        if (!colocada) {
            posicion.snapTo(Offset(margen, alto - lado - margen - fondo))
            colocada = true
        } else {
            posicion.snapTo(esquinaMasCercana(posicion.value, ancho, alto, lado, lado, margen, fondo))
        }
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(posicion.value.x.roundToInt(), posicion.value.y.roundToInt()) }
            .size(LADO_BURBUJA)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .pointerInput(ancho, alto, fondo) {
                detectDragGestures(
                    onDrag = { cambio, arrastre ->
                        cambio.consume()
                        alcance.launch {
                            posicion.snapTo(
                                dentroDeLaPantalla(posicion.value + arrastre, ancho, alto, lado, lado, margen, fondo)
                            )
                        }
                    },
                    onDragEnd = {
                        alcance.launch {
                            posicion.animateTo(
                                esquinaMasCercana(posicion.value, ancho, alto, lado, lado, margen, fondo),
                                animationSpec = tween(280)
                            )
                        }
                    }
                )
            }
            .clickable {
                haptics.performSafely(HapticFeedbackType.SegmentTick)
                onAbrir()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Science,
            contentDescription = "Panel de pruebas",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(22.dp)
        )
    }
}

/** Mantiene una posición dentro de la pantalla, contando la barra de abajo. */
private fun dentroDeLaPantalla(
    donde: Offset,
    ancho: Float,
    alto: Float,
    anchoPieza: Float,
    altoPieza: Float,
    margen: Float,
    fondo: Float
): Offset {
    val maxX = (ancho - anchoPieza - margen).coerceAtLeast(margen)
    val maxY = (alto - altoPieza - margen - fondo).coerceAtLeast(margen)
    return Offset(donde.x.coerceIn(margen, maxX), donde.y.coerceIn(margen, maxY))
}

/**
 * La esquina más cercana a donde se soltó.
 *
 * Se compara el centro de la pieza con el centro de la pantalla en cada eje: es lo que hace
 * que soltarla en el medio-izquierda-abajo la mande abajo a la izquierda y no a la esquina
 * de la que venía.
 */
private fun esquinaMasCercana(
    donde: Offset,
    ancho: Float,
    alto: Float,
    anchoPieza: Float,
    altoPieza: Float,
    margen: Float,
    fondo: Float
): Offset {
    val maxX = (ancho - anchoPieza - margen).coerceAtLeast(margen)
    val maxY = (alto - altoPieza - margen - fondo).coerceAtLeast(margen)
    val x = if (donde.x + anchoPieza / 2f < ancho / 2f) margen else maxX
    val y = if (donde.y + altoPieza / 2f < alto / 2f) margen else maxY
    return Offset(x, y)
}

/** Las tres caras del panel. Son pocas a propósito: más pestañas es volver a navegar. */
private enum class Cara(val rotulo: String) {
    SIMULAR("Simular"),
    MOVIMIENTO("Movimiento"),
    ESTADO("Estado")
}

/** El tono de una palanca: lo que hace, lo que deshace, y lo que rompe algo a propósito. */
private enum class Tono { NORMAL, SUAVE, PELIGRO }

/** Una palanca: lo que hace arriba, para qué sirve abajo, y qué ejecuta al tocarla. */
private data class Palanca(
    val titulo: String,
    val detalle: String,
    val tono: Tono = Tono.NORMAL,
    val accion: (Context) -> Unit
)

/** Una categoría del panel, con su icono, su color y cuántas cosas suyas hay fabricadas. */
private data class Seccion(
    val rotulo: String,
    val icono: ImageVector,
    val color: Color,
    val cuenta: Int,
    val palancas: List<Palanca>
)

@Composable
private fun VentanaDePruebas(
    ancho: Float,
    alto: Float,
    margen: Float,
    fondo: Float,
    onCerrar: () -> Unit,
    onAbrirMovimiento: () -> Unit,
    onNavegar: (String) -> Unit
) {
    val vm: BancoDePruebasViewModel = hiltViewModel()
    val haptics = LocalHapticFeedback.current
    val alcance = rememberCoroutineScope()
    var cara by remember { mutableStateOf(Cara.SIMULAR) }
    var busqueda by remember { mutableStateOf("") }
    /*
     * `cuantasDePrueba()` es una cuenta, no un flujo: sin esto el contador se quedaría con la
     * cifra de cuando se abrió la ventana. Cada palanca lo sube y la cuenta se rehace.
     */
    var pulsaciones by remember { mutableIntStateOf(0) }
    val abiertas = remember { mutableStateMapOf<String, Boolean>() }

    val densidad = LocalDensity.current
    val altoVentana = with(densidad) { 540.dp.toPx() }
    val anchoVentana = (ancho - margen * 2).coerceAtLeast(0f)
    val posicion = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var colocada by remember { mutableStateOf(false) }

    LaunchedEffect(ancho, alto, fondo) {
        if (ancho <= 0f || alto <= 0f) return@LaunchedEffect
        if (!colocada) {
            posicion.snapTo(Offset(margen, (alto - altoVentana - margen - fondo).coerceAtLeast(margen)))
            colocada = true
        }
    }

    val secciones = seccionesDeSimular(vm, pulsaciones, onAbrirMovimiento, onNavegar)
    val filtro = busqueda.trim()
    val visibles = if (filtro.isBlank()) secciones else secciones.mapNotNull { sec ->
        val coinciden = sec.palancas.filter {
            it.titulo.contains(filtro, ignoreCase = true) || it.detalle.contains(filtro, ignoreCase = true)
        }
        if (coinciden.isEmpty()) null else sec.copy(palancas = coinciden)
    }

    Surface(
        modifier = Modifier
            .offset { IntOffset(posicion.value.x.roundToInt(), posicion.value.y.roundToInt()) }
            .width(with(densidad) { anchoVentana.toDp() })
            .heightIn(max = 540.dp),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        shadowElevation = 18.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(ancho, alto, fondo) {
                        detectDragGestures(
                            onDrag = { cambio, arrastre ->
                                cambio.consume()
                                alcance.launch {
                                    posicion.snapTo(
                                        dentroDeLaPantalla(
                                            posicion.value + arrastre,
                                            ancho, alto, anchoVentana, altoVentana, margen, fondo
                                        )
                                    )
                                }
                            },
                            onDragEnd = {
                                alcance.launch {
                                    posicion.animateTo(
                                        esquinaMasCercana(
                                            posicion.value,
                                            ancho, alto, anchoVentana, altoVentana, margen, fondo
                                        ),
                                        animationSpec = tween(280)
                                    )
                                }
                            }
                        )
                    }
                    .padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.DragIndicator,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Panel de pruebas",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "${secciones.size} categorías · ${secciones.sumOf { it.cuenta }} activas",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onCerrar() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (cara == Cara.SIMULAR) {
                BuscadorDePalancas(
                    texto = busqueda,
                    onTexto = { busqueda = it },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Cara.entries.forEach { c ->
                    val elegida = c == cara
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(if (elegida) 999.dp else 10.dp))
                            .background(
                                if (elegida) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                            .clickable {
                                haptics.performSafely(HapticFeedbackType.SegmentTick)
                                cara = c
                            }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            c.rotulo,
                            color = if (elegida) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when (cara) {
                    Cara.SIMULAR -> {
                        if (visibles.isEmpty()) {
                            item { Nota("Nada que se llame así.") }
                        }
                        itemsIndexed(visibles) { indice, seccion ->
                            AcordeonDeSeccion(
                                seccion = seccion,
                                // Con el buscador puesto se abren todas las que coinciden: si no,
                                // habría que abrirlas a mano una por una después de buscar.
                                abierta = if (filtro.isNotBlank()) true
                                else abiertas[seccion.rotulo] ?: (indice == 0),
                                onAbrirCerrar = {
                                    abiertas[seccion.rotulo] = !(abiertas[seccion.rotulo] ?: (indice == 0))
                                },
                                onPalanca = { pulsaciones++ }
                            )
                        }
                    }
                    Cara.MOVIMIENTO -> caraMovimiento(vm)
                    Cara.ESTADO -> caraEstado(vm, secciones) { pulsaciones++ }
                }
            }
        }
    }
}

@Composable
private fun BuscadorDePalancas(texto: String, onTexto: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (texto.isEmpty()) {
                Text(
                    "Buscar una palanca…",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.5.sp
                )
            }
            BasicTextField(
                value = texto,
                onValueChange = onTexto,
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.5.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (texto.isNotEmpty()) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Limpiar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onTexto("") }
            )
        }
    }
}

/**
 * Una categoría replegada en una línea, con su cuenta y su flecha.
 *
 * Eran diez secciones en un solo scroll plano: encontrar una palanca pedía bajar por todas las
 * demás. Replegadas, la ventana entera cabe de un vistazo y se abre sólo lo que se va a usar.
 */
@Composable
private fun AcordeonDeSeccion(
    seccion: Seccion,
    abierta: Boolean,
    onAbrirCerrar: () -> Unit,
    onPalanca: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val giro by animateFloatAsState(if (abierta) 180f else 0f, label = "flecha")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    haptics.performSafely(HapticFeedbackType.SegmentTick)
                    onAbrirCerrar()
                }
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(seccion.color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = seccion.icono,
                    contentDescription = null,
                    tint = seccion.color,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(Modifier.width(9.dp))
            Text(
                seccion.rotulo,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            if (seccion.cuenta > 0) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        "${seccion.cuenta}",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(giro)
            )
        }
        AnimatedVisibility(visible = abierta) {
            Column(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                seccion.palancas.forEach { palanca ->
                    FilaDePalanca(palanca = palanca, onPalanca = onPalanca)
                }
            }
        }
    }
}

@Composable
private fun FilaDePalanca(palanca: Palanca, onPalanca: () -> Unit) {
    val contexto = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val tono = when (palanca.tono) {
        Tono.PELIGRO -> MaterialTheme.colorScheme.error
        Tono.SUAVE -> MaterialTheme.colorScheme.onSurfaceVariant
        Tono.NORMAL -> MaterialTheme.colorScheme.onSurface
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable {
                haptics.performSafely(HapticFeedbackType.Confirm)
                palanca.accion(contexto)
                onPalanca()
            }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(palanca.titulo, color = tono, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
        Text(
            palanca.detalle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

// ==================================================================== el catálogo

/**
 * Todas las palancas, por categoría.
 *
 * Es una lista de datos y no una ristra de `item {}` como antes: así el buscador puede filtrar
 * sobre ella, el acordeón sabe cuántas hay en cada sitio, y la cuenta de fabricadas se pinta
 * junto a la categoría a la que pertenece.
 */
@Composable
private fun seccionesDeSimular(
    vm: BancoDePruebasViewModel,
    pulsaciones: Int,
    onAbrirMovimiento: () -> Unit,
    onNavegar: (String) -> Unit
): List<Seccion> {
    val tonos = tonosDeAjustes
    val resumen = remember(pulsaciones) { vm.cuantasDePrueba() }
    val historico = remember(pulsaciones) { if (HistoricoDeMuestra.enMarcha) 1 else 0 }

    return listOf(
        Seccion(
            rotulo = "Capturas y demo",
            icono = Icons.Rounded.PhotoCamera,
            color = tonos.violeta,
            cuenta = 0,
            palancas = listOf(
                Palanca(
                    "Llenar app con datos reales (para capturas)",
                    "Siembra 5 materias con notas, horario semanal L-V, tareas con subtareas, gastos de la semana y presupuesto. Todo lleva «prueba-»."
                ) { vm.sembrarAppCompleta() }
            )
        ),
        Seccion(
            rotulo = "Histórico",
            icono = Icons.Rounded.History,
            color = tonos.azul,
            cuenta = historico,
            palancas = listOf(
                Palanca(
                    "Simular el histórico del artifact",
                    "Los 4 periodos, con 2026-2 listo para cerrar y hoy en el sáb 5 dic 2026. Tus datos no se tocan"
                ) {
                    HistoricoDeMuestra.empezar()
                    onNavegar(AppRoutes.AcademicHistory)
                },
                Palanca("Saltar a Inicio sin periodo", "2026-2 ya cerrado hoy: Inicio cuenta cómo te fue") {
                    HistoricoDeMuestra.saltarASinPeriodo()
                    onNavegar(AppRoutes.Home)
                },
                Palanca("Saltar a periodo por empezar", "2027-1 creado, con Ecuaciones diferenciales traída") {
                    HistoricoDeMuestra.saltarAPorEmpezar()
                    onNavegar(AppRoutes.Home)
                },
                Palanca(
                    "Salir de la simulación",
                    "Vuelven tus periodos de verdad. Cerrar la app también la quita",
                    Tono.SUAVE
                ) { HistoricoDeMuestra.salir() }
            )
        ),
        Seccion(
            rotulo = "Horario",
            icono = Icons.Rounded.CalendarMonth,
            color = tonos.verde,
            cuenta = resumen.horario,
            palancas = listOf(
                Palanca("Clase en curso ahora", "Una clase que cubre este minuto") { vm.claseEnCursoAhora() },
                Palanca("Cruzar dos horarios", "Dos clases pisándose hoy") { vm.cruceDeHorarios() },
                Palanca("Dejar 3 sin marcar", "Llena «Ponerse al día»") { vm.clasesSinMarcar() },
                Palanca("Deshacer lo de Horario", "Solo las clases fabricadas", Tono.SUAVE) { vm.recogerHorario() }
            )
        ),
        Seccion(
            rotulo = "Gastos",
            icono = Icons.Rounded.ShoppingCart,
            color = tonos.rojo,
            cuenta = resumen.gastos,
            palancas = listOf(
                Palanca("Pasarse del presupuesto", "Lo pone justo por debajo de lo gastado: sale el aviso arriba") { vm.presupuestoPasado() },
                Palanca("Presupuesto holgado", "La misma fila sin aviso") { vm.presupuestoHolgado() },
                Palanca("Registrar un gasto", "Para cruzarlo en vivo y ver el momento") { vm.gastoDePrueba() },
                Palanca("Deshacer lo de Gastos", "Gastos falsos y presupuesto", Tono.SUAVE) { vm.recogerGastos() }
            )
        ),
        Seccion(
            rotulo = "Académico",
            icono = Icons.Rounded.School,
            color = tonos.ambar,
            cuenta = resumen.academico,
            palancas = listOf(
                Palanca("Materia en rojo", "Por debajo del aprobado") { vm.materiaEnRojo() },
                Palanca("Recuperarla", "La sube y dispara el barrido") { vm.materiaRecuperada() },
                Palanca("Corte listo para cerrar", "Repartido al 100 %") { vm.corteListoParaCerrar() },
                Palanca("Deshacer lo académico", "Solo las notas fabricadas", Tono.SUAVE) { vm.recogerAcademico() }
            )
        ),
        Seccion(
            rotulo = "Tareas",
            icono = Icons.Rounded.TaskAlt,
            color = tonos.indigo,
            cuenta = resumen.tareas,
            palancas = listOf(
                Palanca("Sembrar tareas del artifact", "7 tareas con subtareas y estados del diseño") { vm.sembrarTareasDelArtifact() },
                Palanca("Deshacer lo de Tareas", "Elimina tareas y materias de prueba", Tono.SUAVE) { vm.recogerTareas() }
            )
        ),
        Seccion(
            rotulo = "Actualizaciones",
            icono = Icons.Rounded.SystemUpdate,
            color = tonos.turquesa,
            cuenta = 0,
            palancas = listOf(
                Palanca(
                    "Fingir versión nueva",
                    "La escena del artifact: 1.0.1 sobre 1.0.0, historial incluido. La hoja en Inicio; el aviso llega en 6 s si sales de la app"
                ) { vm.fingirVersionNueva() },
                Palanca(
                    "Fingir descarga",
                    "Ocho segundos de barra hasta «lista para instalar»; los avisos, solo con la app detrás"
                ) { vm.fingirDescarga() },
                Palanca("Fingir al día", "La misma escena con «Estás al día»") { vm.fingirAlDia() },
                Palanca("Volver a lo real", "Comprueba en GitHub y se queda con lo que diga", Tono.SUAVE) { vm.actualizacionReal() }
            )
        ),
        Seccion(
            rotulo = "Notificaciones",
            icono = Icons.Rounded.Notifications,
            color = tonos.rosa,
            cuenta = 0,
            palancas = listOf(
                Palanca(
                    "Sembrar las 15 del artifact",
                    "Repartidas entre hoy, ayer y el lunes. Tres con destino."
                ) { contexto -> vm.sembrarNotificacionesDelArtifact(contexto) },
                Palanca(
                    "Deshacer lo de Notificaciones",
                    "Sólo los avisos sembrados; los de verdad se quedan.",
                    Tono.SUAVE
                ) { contexto -> vm.recogerNotificaciones(contexto) }
            )
        ),
        Seccion(
            rotulo = "Se cerró sola",
            icono = Icons.Rounded.BugReport,
            color = tonos.naranja,
            cuenta = 0,
            palancas = listOf(
                Palanca(
                    "Provocar un fallo ahora",
                    "Rompe la app de verdad: debe salir la pantalla al vuelo, sin diálogo de Android",
                    Tono.PELIGRO
                ) { throw falloDeMuestra() },
                Palanca(
                    "Reventar un hilo de fondo",
                    "La app NO debe cerrarse. El informe queda esperando al siguiente arranque",
                    Tono.PELIGRO
                ) { Thread { throw falloDeMuestra() }.start() },
                Palanca(
                    "Dejar un fallo esperando",
                    "Sin romper nada. Cierra la app del todo y vuelve a abrirla: sale por la puerta lenta"
                ) { contexto -> AlmacenDeFallos.guardar(contexto, informeDeMuestra(contexto)) },
                Palanca(
                    "Recoger el fallo esperando",
                    "Borra el informe pendiente sin pasar por la pantalla",
                    Tono.SUAVE
                ) { contexto -> AlmacenDeFallos.limpiar(contexto) }
            )
        ),
        Seccion(
            rotulo = "Ir a",
            icono = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            color = tonos.gris,
            cuenta = 0,
            palancas = listOf(
                Palanca("Ajustes de Movimiento", "La pantalla entera, si hace falta") { onAbrirMovimiento() }
            )
        )
    )
}

// ==================================================================== movimiento

/**
 * Los siete gestos con sus variantes, cambiables sin salir de aquí.
 *
 * Se recorre [MotionCatalog] en vez de escribirlos a mano: un gesto que se añada mañana
 * aparece solo, y uno que se retire desaparece. Es la misma lista que pinta Ajustes.
 */
private fun LazyListScope.caraMovimiento(vm: BancoDePruebasViewModel) {
    item { Nota("Cambia una variante y míralo detrás: la ventana no tapa la pantalla.") }
    MotionCatalog.grouped().forEach { (grupo, gestos) ->
        item {
            Text(
                text = grupo.uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.14.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
            )
        }
        gestos.forEach { gesto ->
            item {
                val perfil by vm.perfil.collectAsState()
                val puesta = perfil?.appearancePreferences?.motion?.let { gesto.read(it) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        gesto.displayName,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // Envueltas de dos en dos a mano: `FlowRow` pediría otra opt-in y aquí
                    // ningún gesto pasa de siete variantes.
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        gesto.options.chunked(2).forEach { pareja ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                pareja.forEach { opcion ->
                                    val activa = opcion.id == puesta?.id
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(
                                                if (activa) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceContainerHighest
                                            )
                                            .clickable { vm.ponVariante(gesto, opcion) }
                                            .padding(horizontal = 12.dp, vertical = 7.dp)
                                    ) {
                                        Text(
                                            opcion.label,
                                            color = if (activa) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    item {
        FilaDePalanca(
            palanca = Palanca("Movimiento de fábrica", "Los siete gestos a su valor original", Tono.SUAVE) {
                vm.movimientoDeFabrica()
            },
            onPalanca = {}
        )
    }
}

// ==================================================================== estado

private fun LazyListScope.caraEstado(
    vm: BancoDePruebasViewModel,
    secciones: List<Seccion>,
    onRecoger: () -> Unit
) {
    item {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Cifra("FABRICADAS AHORA", secciones.sumOf { it.cuenta }, Modifier.weight(1f))
            Cifra("CATEGORÍAS TOCADAS", secciones.count { it.cuenta > 0 }, Modifier.weight(1f))
        }
    }
    item {
        Nota(
            "Lo que hay fabricado ahora mismo. Todo lleva el prefijo «prueba-», " +
                "así que se retira sin tocar nada tuyo."
        )
    }
    secciones.filter { it.cuenta > 0 || it.rotulo in SITIOS_CON_CUENTA }.forEach { seccion ->
        item { Cuenta(seccion.rotulo, seccion.cuenta) }
    }
    item {
        FilaDePalanca(
            palanca = Palanca("Recogerlo todo", "Se lleva lo fabricado y el presupuesto de prueba", Tono.PELIGRO) {
                vm.recogerlo()
            },
            onPalanca = onRecoger
        )
    }
}

/** Las categorías cuya cuenta sabe dar el ViewModel: el resto no se enseña vacía por no mentir. */
private val SITIOS_CON_CUENTA = setOf("Horario", "Académico", "Gastos", "Tareas", "Histórico")

@Composable
private fun Cifra(rotulo: String, cuantos: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 13.dp, vertical = 11.dp)
    ) {
        Text(
            "$cuantos",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            rotulo,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun Cuenta(sitio: String, cuantos: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            sitio,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            if (cuantos == 0) "limpio" else "$cuantos",
            color = if (cuantos == 0) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.primary,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun Nota(texto: String) {
    Text(
        texto,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}
