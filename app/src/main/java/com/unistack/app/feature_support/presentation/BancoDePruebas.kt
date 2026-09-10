@file:OptIn(ExperimentalMaterial3Api::class)

package com.unistack.app.feature_support.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.BuildConfig
import com.unistack.app.core.utils.BuildStage
import com.unistack.app.core.utils.performSafely

/**
 * El banco de pruebas: un botón que deja la app en el estado que hace falta mirar.
 *
 * **Probar una animación costaba más que escribirla.** Para ver el aviso de pasarse del
 * presupuesto había que ir a Gastos, abrir el presupuesto, poner una cifra, volver y registrar
 * gastos hasta cruzarla; para ver el barrido de recuperación, bajar una materia del aprobado y
 * subirla; para ver el aviso de cruce, inventarse dos clases a la misma hora. Y todo eso otra
 * vez cada vez que se toca una duración.
 *
 * Va **sobre todas las pantallas** y no dentro de una: el problema era justo tener que
 * navegar. Y solo en dev, alpha y beta — en una versión publicada, un botón que fabrica clases
 * y gastos falsos no tiene nada que hacer.
 *
 * Todo lo que crea lleva el prefijo `prueba-`, y «Recogerlo todo» se lo lleva sin tocar nada
 * de verdad.
 */
@Composable
fun BancoDePruebas(
    modifier: Modifier = Modifier,
    onAbrirMovimiento: () -> Unit
) {
    if (!BuildStage.of(BuildConfig.VERSION_NAME).allowsUnfinished) return

    var abierto by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

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
            contentDescription = "Banco de pruebas",
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.size(21.dp)
        )
    }

    if (abierto) {
        val vm: BancoDePruebasViewModel = hiltViewModel()
        val estado = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { abierto = false },
            sheetState = estado,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            "Banco de pruebas",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "Cada botón deja la app lista para mirar una cosa. " +
                                "Todo lo que crea se recoge abajo.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                seccion("Horario")
                palanca("Clase en curso ahora", "Una clase que cubre este minuto") { vm.claseEnCursoAhora() }
                palanca("Cruzar dos horarios", "Dos clases pisándose hoy") { vm.cruceDeHorarios() }
                palanca("Dejar 3 sin marcar", "Llena «Ponerse al día»") { vm.clasesSinMarcar() }

                seccion("Gastos")
                palanca("Pasarse del presupuesto", "Lo pone justo por debajo de lo gastado") { vm.presupuestoPasado() }
                palanca("Presupuesto holgado", "La misma fila sin alarma") { vm.presupuestoHolgado() }
                palanca("Registrar un gasto", "Para cruzarlo en vivo y ver el momento") { vm.gastoDePrueba() }
                palanca("Quitar el presupuesto", "Vuelve a «Sin presupuesto»") { vm.sinPresupuesto() }

                seccion("Académico")
                palanca("Materia en rojo", "Por debajo del aprobado") { vm.materiaEnRojo() }
                palanca("Recuperarla", "La sube y dispara el barrido") { vm.materiaRecuperada() }
                palanca("Corte listo para cerrar", "Repartido al 100 %") { vm.corteListoParaCerrar() }

                seccion("Ir a")
                palanca("Ajustes de Movimiento", "Cambiar variantes sin buscarlas") {
                    abierto = false
                    onAbrirMovimiento()
                }

                seccion("Al terminar")
                palanca(
                    "Recogerlo todo",
                    "Se lleva lo del prefijo «prueba-» y nada más",
                    peligro = true
                ) { vm.recogerlo() }
            }
        }
    }
}

/** El rótulo de un grupo de palancas. */
private fun LazyListScope.seccion(texto: String) {
    item {
        Text(
            text = texto.uppercase(),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.12.sp,
            modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
        )
    }
}

/**
 * Una palanca: lo que hace arriba y por qué sirve abajo.
 *
 * El subtítulo no es adorno — sin él hay que pulsar para saber qué hace cada uno, que es
 * exactamente el rato que este panel viene a ahorrar.
 */
private fun LazyListScope.palanca(
    titulo: String,
    detalle: String,
    peligro: Boolean = false,
    onClick: () -> Unit
) {
    item {
        val haptics = LocalHapticFeedback.current
        val tono = if (peligro) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable {
                    haptics.performSafely(HapticFeedbackType.Confirm)
                    onClick()
                }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    titulo,
                    color = tono,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    detalle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
