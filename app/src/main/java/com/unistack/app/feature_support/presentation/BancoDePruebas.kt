@file:OptIn(ExperimentalMaterial3Api::class)

package com.unistack.app.feature_support.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.BuildConfig
import com.unistack.app.core.utils.BuildStage
import com.unistack.app.core.utils.performSafely
import com.unistack.app.feature_user.domain.MotionCatalog
import kotlin.math.roundToInt

/**
 * El panel de taller: una ventana flotante con lo que hace falta para probar la app.
 *
 * **Probar una animación costaba más que escribirla.** Ver el aviso de pasarse del presupuesto
 * pedía ir a Gastos, abrir el presupuesto, poner una cifra, volver y registrar gastos hasta
 * cruzarla; cambiarle la variante pedía cuatro pantallas de ida y cuatro de vuelta. Y todo eso
 * otra vez por cada duración que se toca.
 *
 * Es **una ventana y no una hoja** a propósito: una hoja tapa la pantalla que se está mirando,
 * que es justo lo que aquí no puede pasar — se cambia una variante y se ve el efecto detrás,
 * sin cerrar nada. Se arrastra por su cabecera para apartarla de lo que estorbe.
 *
 * Solo en dev y alpha. La beta se reparte, y ahí una ventana que fabrica clases y gastos falsos
 * no tiene nada que hacer.
 */
@Composable
fun BancoDePruebas(
    modifier: Modifier = Modifier,
    onAbrirMovimiento: () -> Unit
) {
    val etapa = BuildStage.of(BuildConfig.VERSION_NAME)
    if (etapa != BuildStage.DEV && etapa != BuildStage.ALPHA) return

    var abierto by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    if (!abierto) {
        Box(
            modifier = modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .clickable {
                    haptics.performSafely(HapticFeedbackType.SegmentTick)
                    abierto = true
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Science,
                contentDescription = "Panel de pruebas",
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(21.dp)
            )
        }
    } else {
        VentanaDePruebas(onCerrar = { abierto = false }, onAbrirMovimiento = onAbrirMovimiento)
    }
}

/** Las tres caras del panel. Son pocas a propósito: más pestañas es volver a navegar. */
private enum class Cara(val rotulo: String) {
    SIMULAR("Simular"),
    MOVIMIENTO("Movimiento"),
    ESTADO("Estado")
}

@Composable
private fun VentanaDePruebas(onCerrar: () -> Unit, onAbrirMovimiento: () -> Unit) {
    val vm: BancoDePruebasViewModel = hiltViewModel()
    val haptics = LocalHapticFeedback.current
    var cara by remember { mutableStateOf(Cara.SIMULAR) }
    // Arrastrable: la ventana se aparta de lo que estorbe sin cerrarse.
    var x by remember { mutableFloatStateOf(0f) }
    var y by remember { mutableFloatStateOf(0f) }

    Surface(
        modifier = Modifier
            .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
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
                    .pointerInput(Unit) {
                        detectDragGestures { _, arrastre ->
                            x += arrastre.x
                            y += arrastre.y
                        }
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
                Text(
                    "Panel de pruebas",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
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

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Cara.entries.forEach { c ->
                    val elegida = c == cara
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(if (c == Cara.SIMULAR) 999.dp else 10.dp))
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
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when (cara) {
                    Cara.SIMULAR -> caraSimular(vm, onAbrirMovimiento)
                    Cara.MOVIMIENTO -> caraMovimiento(vm)
                    Cara.ESTADO -> caraEstado(vm)
                }
            }
        }
    }
}

// ==================================================================== simular

private fun LazyListScope.caraSimular(
    vm: BancoDePruebasViewModel,
    onAbrirMovimiento: () -> Unit
) {
    seccion("Horario")
    palanca("Clase en curso ahora", "Una clase que cubre este minuto") { vm.claseEnCursoAhora() }
    palanca("Cruzar dos horarios", "Dos clases pisándose hoy") { vm.cruceDeHorarios() }
    palanca("Dejar 3 sin marcar", "Llena «Ponerse al día»") { vm.clasesSinMarcar() }
    palanca("Deshacer lo de Horario", "Solo las clases fabricadas", suave = true) { vm.recogerHorario() }

    seccion("Gastos")
    palanca("Pasarse del presupuesto", "Lo pone justo por debajo de lo gastado: sale el aviso arriba") { vm.presupuestoPasado() }
    palanca("Presupuesto holgado", "La misma fila sin aviso") { vm.presupuestoHolgado() }
    palanca("Registrar un gasto", "Para cruzarlo en vivo y ver el momento") { vm.gastoDePrueba() }
    palanca("Deshacer lo de Gastos", "Gastos falsos y presupuesto", suave = true) { vm.recogerGastos() }

    seccion("Académico")
    palanca("Materia en rojo", "Por debajo del aprobado") { vm.materiaEnRojo() }
    palanca("Recuperarla", "La sube y dispara el barrido") { vm.materiaRecuperada() }
    palanca("Corte listo para cerrar", "Repartido al 100 %") { vm.corteListoParaCerrar() }
    palanca("Deshacer lo académico", "Solo las notas fabricadas", suave = true) { vm.recogerAcademico() }

    seccion("Tareas")
    palanca("Sembrar tareas del artifact", "7 tareas con subtareas y estados del diseño") { vm.sembrarTareasDelArtifact() }
    palanca("Deshacer lo de Tareas", "Elimina tareas y materias de prueba", suave = true) { vm.recogerTareas() }

    seccion("Notificaciones")
    item {
        val contexto = LocalContext.current
        val haptics = LocalHapticFeedback.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .clickable {
                    haptics.performSafely(HapticFeedbackType.Confirm)
                    vm.sembrarNotificacionesDelArtifact(contexto)
                }
                .padding(horizontal = 13.dp, vertical = 11.dp)
        ) {
            Text(
                "Sembrar las 15 del artifact",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Repartidas entre hoy, ayer y el lunes. Tres con destino.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.5.sp
            )
        }
    }
    item {
        val contexto = LocalContext.current
        val haptics = LocalHapticFeedback.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .clickable {
                    haptics.performSafely(HapticFeedbackType.Confirm)
                    vm.recogerNotificaciones(contexto)
                }
                .padding(horizontal = 13.dp, vertical = 11.dp)
        ) {
            Text(
                "Deshacer lo de Notificaciones",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Sólo los avisos sembrados; los de verdad se quedan.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.5.sp
            )
        }
    }

    seccion("Ir a")
    palanca("Ajustes de Movimiento", "La pantalla entera, si hace falta") { onAbrirMovimiento() }
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
        seccion(grupo)
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
    seccion("Deshacer")
    palanca("Movimiento de fábrica", "Los siete gestos a su valor original", suave = true) {
        vm.movimientoDeFabrica()
    }
}

// ==================================================================== estado

private fun LazyListScope.caraEstado(vm: BancoDePruebasViewModel) {
    item {
        val resumen = vm.cuantasDePrueba()
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Nota(
                "Lo que hay fabricado ahora mismo. Todo lleva el prefijo «prueba-», " +
                    "así que se retira sin tocar nada tuyo."
            )
            Cuenta("Horario", resumen.horario, "clases")
            Cuenta("Académico", resumen.academico, "notas")
            Cuenta("Gastos", resumen.gastos, "gastos")
            Cuenta("Tareas", resumen.tareas, "tareas")
        }
    }
    seccion("Deshacer")
    palanca("Recogerlo todo", "Se lleva lo fabricado y el presupuesto de prueba", peligro = true) {
        vm.recogerlo()
    }
}

@Composable
private fun Cuenta(sitio: String, cuantos: Int, que: String) {
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
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            if (cuantos == 0) "limpio" else "$cuantos $que",
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

// ==================================================================== piezas

private fun LazyListScope.seccion(texto: String) {
    item {
        Text(
            text = texto.uppercase(),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.14.sp,
            modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)
        )
    }
}

/**
 * Una palanca: lo que hace arriba y para qué sirve abajo.
 *
 * El subtítulo no es adorno — sin él hay que pulsar para saber qué hace cada una, que es
 * justo el rato que este panel viene a ahorrar.
 */
private fun LazyListScope.palanca(
    titulo: String,
    detalle: String,
    peligro: Boolean = false,
    suave: Boolean = false,
    onClick: () -> Unit
) {
    item {
        val haptics = LocalHapticFeedback.current
        val tono = when {
            peligro -> MaterialTheme.colorScheme.error
            suave -> MaterialTheme.colorScheme.onSurfaceVariant
            else -> MaterialTheme.colorScheme.onSurface
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .clickable {
                    haptics.performSafely(HapticFeedbackType.Confirm)
                    onClick()
                }
                .padding(horizontal = 13.dp, vertical = 11.dp)
        ) {
            Text(titulo, color = tono, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(detalle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.5.sp)
        }
    }
}
