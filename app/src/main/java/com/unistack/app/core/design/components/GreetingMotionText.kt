@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.hayMovimiento
import com.unistack.app.core.design.theme.tweenDeMovimiento
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
        animationSpec = tweenDeMovimiento(baseMs = 520, retrasoMs = 140),
        label = "saludoNombre"
    )

    /*
     * **Una sola forma, y ya no se elige.**
     *
     * Eran siete variantes para algo que se ve **una vez al dia**: el saludo solo se anima al
     * arrancar la app, no cada vez que se vuelve a Inicio. Siete formas de entrar para un
     * momento que casi nadie llega a comparar es catalogo por catalogo.
     *
     * Se queda la que venia de fabrica: el rotulo entra y el nombre justo despues. El escalon
     * de 140 ms es lo unico que hay que acertar aqui, y es lo que separa «llegan los dos» de
     * «llega uno y detras el otro».
     */
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
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
