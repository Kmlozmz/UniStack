@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.duracion
import com.unistack.app.core.design.theme.hayMovimiento
import com.unistack.app.core.design.theme.motionActual
import com.unistack.app.core.design.theme.tweenDeMovimiento
import com.unistack.app.feature_user.domain.GreetingMotion
import kotlinx.coroutines.delay

/**
 * El saludo de Inicio, con la variante elegida en Movimiento.
 *
 * Es lo primero que se ve al abrir la app, así que es donde más se nota lo que se elija: por
 * eso tiene siete variantes y no tres. Las dos que escriben —máquina y letra a letra— son
 * distintas a propósito: la máquina revela **caracteres** del nombre con cursor, y «letra a
 * letra» hace entrar cada carácter desde abajo con su propio retardo, sin cursor.
 *
 * @param rotulo el «BUENAS TARDES», que entra primero en la variante escalonada.
 * @param nombre tu nombre, que es lo que escriben las variantes de escritura.
 */
@Composable
fun SaludoAnimado(
    rotulo: String,
    nombre: String,
    estiloRotulo: TextStyle,
    estiloNombre: TextStyle,
    modifier: Modifier = Modifier
) {
    val estilo = motionActual().greeting
    val animar = hayMovimiento()

    /*
     * El saludo se anima **al abrir la app**, no cada vez que se vuelve a Inicio.
     *
     * Estaba atado a que la pantalla entrara en composicion, y a Inicio se vuelve veinte veces
     * al dia: el saludo se escribia letra a letra cada vez que se tocaba la primera pestana, lo
     * cual deja de ser una bienvenida y pasa a ser un tic. [SaludoYaVisto] es un objeto de
     * proceso: se pone a cierto la primera vez y vuelve a falso solo cuando Android mata la
     * app, que es justo cuando volver a saludar tiene sentido.
     *
     * Cambiar el nombre si vuelve a lanzarlo: ahi hay algo nuevo que ensenar.
     */
    val debeAnimar = animar && !SaludoYaVisto.visto(nombre)
    var lanzado by remember(nombre) { mutableStateOf(!debeAnimar) }
    LaunchedEffect(nombre, debeAnimar) {
        if (debeAnimar) {
            delay(60)
            lanzado = true
            SaludoYaVisto.marcar(nombre)
        }
    }
    val avance by animateFloatAsState(
        targetValue = if (lanzado) 1f else 0f,
        animationSpec = tweenDeMovimiento(baseMs = 520),
        label = "saludo"
    )
    val avanceNombre by animateFloatAsState(
        targetValue = if (lanzado) 1f else 0f,
        // El nombre entra después del rótulo: los 140 ms son lo que separa «escalonado» de
        // «de golpe», donde los dos llegan a la vez.
        animationSpec = tweenDeMovimiento(
            baseMs = 520,
            retrasoMs = if (estilo == GreetingMotion.ESCALONADO) 140 else 0
        ),
        label = "saludoNombre"
    )

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
        when (estilo) {
            GreetingMotion.GOLPE -> {
                Text(rotulo, style = estiloRotulo, color = MaterialTheme.colorScheme.primary)
                NombreLlano(nombre, estiloNombre)
            }

            GreetingMotion.ESCALONADO -> {
                Text(
                    rotulo,
                    style = estiloRotulo,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.graphicsLayer {
                        alpha = avance
                        translationY = 18f * (1f - avance)
                    }
                )
                NombreLlano(
                    nombre, estiloNombre,
                    Modifier.graphicsLayer {
                        alpha = avanceNombre
                        translationY = 22f * (1f - avanceNombre)
                    }
                )
            }

            // Máquina de escribir: revela caracteres del nombre y deja el cursor al final
            // mientras escribe. Termina con el nombre entero y sin cursor.
            GreetingMotion.MAQUINA -> {
                Text(rotulo, style = estiloRotulo, color = MaterialTheme.colorScheme.primary)
                val letras = (nombre.length * avanceNombre).toInt().coerceIn(0, nombre.length)
                val escribiendo = avanceNombre < 1f
                NombreLlano(nombre.take(letras) + if (escribiendo) "|" else "", estiloNombre)
            }

            // La cortina no mueve nada: recorta de arriba abajo lo que ya está puesto.
            GreetingMotion.CORTINA -> {
                Column(
                    modifier = Modifier
                        .clipToBounds()
                        .graphicsLayer {
                            // Se recorta con escala vertical desde arriba, que es lo más
                            // parecido a una cortina sin medir el alto a mano.
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                            scaleY = avance
                        }
                ) {
                    Text(rotulo, style = estiloRotulo, color = MaterialTheme.colorScheme.primary)
                    NombreLlano(nombre, estiloNombre)
                }
            }

            GreetingMotion.LATERAL -> Column(
                modifier = Modifier.graphicsLayer {
                    alpha = avance
                    translationX = -120f * (1f - avance)
                }
            ) {
                Text(rotulo, style = estiloRotulo, color = MaterialTheme.colorScheme.primary)
                NombreLlano(nombre, estiloNombre)
            }

            // El desenfoque real necesitaría un `RenderEffect`, que pide API 31. Aquí se hace
            // con escala y opacidad: se lee como algo que se enfoca, y funciona en todas.
            GreetingMotion.DESENFOQUE -> Column(
                modifier = Modifier.graphicsLayer {
                    alpha = avance * avance
                    scaleX = 1.06f - 0.06f * avance
                    scaleY = 1.06f - 0.06f * avance
                }
            ) {
                Text(rotulo, style = estiloRotulo, color = MaterialTheme.colorScheme.primary)
                NombreLlano(nombre, estiloNombre)
            }

            // Letra a letra: cada carácter tiene su turno y sube a su sitio. Nada de cursor,
            // que es lo que la separa de la máquina de escribir.
            GreetingMotion.LETRAS -> {
                Text(rotulo, style = estiloRotulo, color = MaterialTheme.colorScheme.primary)
                Row {
                    nombre.forEachIndexed { indice, caracter ->
                        val propio = ((avanceNombre * nombre.length) - indice).coerceIn(0f, 1f)
                        Text(
                            text = caracter.toString(),
                            style = estiloNombre,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.graphicsLayer {
                                alpha = propio
                                translationY = 20f * (1f - propio)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NombreLlano(texto: String, estilo: TextStyle, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = estilo,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

/** Sin uso directo; queda a mano para quien necesite el compás del saludo. */
@Composable
internal fun compasDelSaludo(): Int = duracion(520)

/**
 * Si el saludo ya se enseno en esta sesion de la app.
 *
 * Es estado de proceso y no de composicion a proposito: lo que hay que recordar es «esta
 * apertura de la app», y eso vive mas que cualquier pantalla. Guardarlo en disco seria peor
 * —el saludo no volveria nunca tras la primera instalacion— y guardarlo en la composicion es
 * lo que estaba haciendo que se repitiera en cada vuelta a Inicio.
 */
private object SaludoYaVisto {
    private var nombreVisto: String? = null

    fun visto(nombre: String): Boolean = nombreVisto == nombre

    fun marcar(nombre: String) {
        nombreVisto = nombre
    }
}
