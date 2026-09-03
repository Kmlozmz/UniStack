@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * «Estoy en ello, y no sé cuánto falta.»
 *
 * Es el componente de carga de Material 3 Expressive: una forma que va mutando entre polígonos,
 * distinta a propósito de una barra de progreso. Hasta ahora la app no lo usaba y en su lugar
 * ponía la barra ondulada en modo indeterminado, con lo que **esperar sin número y avanzar con
 * número se veían igual**: una barra que no llega a ninguna parte parece una barra atascada.
 *
 * La regla, y por eso este componente existe aparte: si hay un número que enseñar es progreso y
 * va en una barra; si no lo hay, es espera y va aquí.
 */
@Composable
fun UniLoadingIndicator(modifier: Modifier = Modifier) {
    // La forma sale de Movimiento: circulo, formas de M3E, onda o puntos. Estuvo fija en
    // «formas», que sigue siendo la de por defecto porque es la que menos se confunde con una
    // barra de progreso atascada.
    UniLoading(modifier = modifier)
}

/**
 * Lo que hace el aparato: una descarga, un guardado, una copia.
 *
 * Un solo punto de entrada para los dos casos, porque separarlos es justo lo que se había
 * duplicado: la pareja «si el progreso es desconocido pon el indeterminado, si no pásale el
 * número» estaba escrita dos veces, palabra por palabra, en la pantalla de Actualizaciones y en
 * su hoja de detalle. Pasando el porcentaje como nulo, la decisión se toma en un sitio.
 *
 * **La forma no se configura.** La preferencia de Apariencia gobierna el progreso académico
 * —semestre, materia, trabajo—, que es el que se mira a diario. Cómo se ve una descarga de ocho
 * segundos no es una decisión que nadie quiera tomar.
 *
 * @param percent de 0 a 100, o `null` cuando todavía no se sabe cuánto falta.
 */
@Composable
fun SystemProgress(
    percent: Int?,
    modifier: Modifier = Modifier
) {
    if (percent == null) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            UniLoadingIndicator()
        }
    } else {
        LinearWavyProgressIndicator(
            progress = { (percent / 100f).coerceIn(0f, 1f) },
            modifier = modifier.fillMaxWidth()
        )
    }
}
