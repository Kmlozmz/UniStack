@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.LocalAccessibilityPreferences
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import com.unistack.app.feature_user.domain.TouchTargetSize
import com.unistack.app.feature_user.domain.UndoDuration

/**
 * Las piezas de Accesibilidad, en un sitio y no repartidas por las pantallas.
 *
 * La regla es la misma que en Movimiento: nadie lee la preferencia para decidir por su cuenta.
 * Quien quiere un separador llama a [UniDivider] y no a `HorizontalDivider`; quien quiere que
 * la pantalla no se apague llama a [MantenerPantallaEncendida]. Es lo que hace que apagar un
 * ajuste lo apague en todas partes y no solo donde alguien se acordó de comprobarlo.
 */

/**
 * El área mínima que responde al dedo.
 *
 * Los 48dp de Material son el mínimo recomendado, no un máximo: para quien tiene temblor o usa
 * el teléfono en movimiento, un icono de 48dp entre otros dos a 8dp de distancia sigue siendo
 * difícil de acertar. Agranda el área, no el dibujo, así que la pantalla se ve igual.
 */
@Composable
@ReadOnlyComposable
fun areaDeToque(): Dp = when (LocalAccessibilityPreferences.current.touchTargetSize) {
    TouchTargetSize.ESTANDAR -> 48.dp
    TouchTargetSize.GRANDE -> 56.dp
    TouchTargetSize.MAXIMO -> 64.dp
}

/** El modificador que aplica ese mínimo a un control pequeño. */
@Composable
fun Modifier.areaDeToqueMinima(): Modifier {
    val minimo = areaDeToque()
    return this.sizeIn(minWidth = minimo, minHeight = minimo)
}

/**
 * El separador de listas, que se puede apagar.
 *
 * Es «Separadores en las listas» de Componentes. Apagado no dibuja nada y deja el hueco tal
 * cual, que es lo que quiere quien prefiere que separe el aire y no una línea.
 */
@Composable
fun UniDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant
) {
    if (!LocalAppearancePreferences.current.listDividers) return
    HorizontalDivider(modifier = modifier, color = color)
}

/**
 * Cuánto dura el aviso de «Deshacer».
 *
 * Cinco segundos es lo que trae Material y lo que basta a la mayoría. Para quien lee despacio,
 * navega con un conmutador o tiene el teléfono en la mochila, cinco segundos es un botón que
 * desaparece antes de poder pulsarlo, y el borrado ya no se puede deshacer.
 *
 * Material solo entiende «corto», «largo» e «indefinido», así que los treinta segundos se
 * entregan como indefinido: se queda hasta que se toque «Deshacer» o se descarte.
 */
@Composable
@ReadOnlyComposable
fun duracionDeDeshacer(): SnackbarDuration =
    when (LocalAccessibilityPreferences.current.undoDuration) {
        UndoDuration.CORTA -> SnackbarDuration.Short
        UndoDuration.LARGA -> SnackbarDuration.Long
        UndoDuration.MAXIMA -> SnackbarDuration.Indefinite
    }

/**
 * Mantiene la pantalla encendida mientras esta pantalla esté a la vista.
 *
 * Se pone y se quita con la propia pantalla —de ahí el [DisposableEffect]—: dejar la bandera
 * puesta al salir mantendría el teléfono despierto en Inicio, que no es lo que se pidió.
 *
 * @param activo normalmente el ajuste de Accesibilidad, y además que la pantalla sea de las
 *   que tienen sentido leer sin tocar: una nota abierta, sí; un formulario, no.
 */
@Composable
fun MantenerPantallaEncendida(activo: Boolean) {
    val contexto = LocalContext.current
    DisposableEffect(activo, contexto) {
        val ventana = (contexto as? Activity)?.window
        if (activo && ventana != null) {
            ventana.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            if (activo && ventana != null) {
                ventana.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }
}
