package com.unistack.app.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import com.unistack.app.core.design.theme.motionActual
import com.unistack.app.feature_user.domain.ScreenTransition

/**
 * Las diez formas de entrar a una pantalla, elegibles en Movimiento.
 *
 * Hasta ahora el empuje lateral estaba escrito a mano en el `NavHost` y era el único: la app
 * entraba siempre igual, y el ajuste de movimiento solo podía apagarlo del todo. Aquí cada
 * variante dice qué hace la que entra, la que sale, y las dos al volver atrás.
 *
 * **El fondo opaco de `screen()` es lo que las hace posibles.** Sin él, cualquier transición
 * que desplace o escale deja ver la pantalla de debajo a través de la de encima, que es el
 * problema que tuvo el empuje hasta que cada destino se metió en su propia superficie.
 */
internal data class TransicionDePantalla(
    val entra: EnterTransition,
    val sale: ExitTransition,
    val vuelveEntrando: EnterTransition,
    val vuelveSaliendo: ExitTransition,
    /**
     * [entra] y [sale] vistas en un espejo, para ir a una pestaña que queda a la izquierda.
     *
     * Cambiar de pestaña no es volver atrás —no se deshace nada de la pila—, así que la que llega
     * se pinta encima, como al entrar; solo cambia el lado por el que llega. En las transiciones
     * que no tienen lado son las mismas.
     */
    val entraDesdeElOtroLado: EnterTransition = entra,
    val saleHaciaElOtroLado: ExitTransition = sale
)

internal fun transicionDe(
    estilo: ScreenTransition,
    desplazamiento: FiniteAnimationSpec<IntOffset>,
    fundido: FiniteAnimationSpec<Float>
): TransicionDePantalla = when (estilo) {
    ScreenTransition.NINGUNA -> TransicionDePantalla(
        EnterTransition.None, ExitTransition.None, EnterTransition.None, ExitTransition.None
    )

    ScreenTransition.FUNDIDO -> TransicionDePantalla(
        entra = fadeIn(fundido),
        sale = fadeOut(fundido),
        vuelveEntrando = fadeIn(fundido),
        vuelveSaliendo = fadeOut(fundido)
    )

    /*
     * El empuje de siempre. La que sale solo recorre un tercio: es lo que da la sensación de
     * que se queda esperando debajo en vez de irse del todo.
     */
    ScreenTransition.EJE -> TransicionDePantalla(
        entra = slideInHorizontally(desplazamiento) { ancho -> ancho },
        sale = slideOutHorizontally(desplazamiento) { ancho -> -ancho / 3 },
        vuelveEntrando = slideInHorizontally(desplazamiento) { ancho -> -ancho / 3 },
        vuelveSaliendo = slideOutHorizontally(desplazamiento) { ancho -> ancho },
        entraDesdeElOtroLado = slideInHorizontally(desplazamiento) { ancho -> -ancho },
        saleHaciaElOtroLado = slideOutHorizontally(desplazamiento) { ancho -> ancho / 3 }
    )

    /*
     * La pantalla nueva crece desde el sitio que ocupaba lo que tocaste, sin desplazarse. Es
     * la transformación de contenedor de Material: lo que se abre parece salir de lo abierto.
     */
    ScreenTransition.CONTENEDOR -> TransicionDePantalla(
        entra = scaleIn(fundido, initialScale = 0.88f) + fadeIn(fundido),
        sale = fadeOut(fundido),
        vuelveEntrando = fadeIn(fundido),
        vuelveSaliendo = scaleOut(fundido, targetScale = 0.88f) + fadeOut(fundido)
    )

    ScreenTransition.ABAJO -> TransicionDePantalla(
        entra = slideInVertically(desplazamiento) { alto -> alto },
        sale = fadeOut(fundido),
        vuelveEntrando = fadeIn(fundido),
        vuelveSaliendo = slideOutVertically(desplazamiento) { alto -> alto }
    )

    /*
     * Zoom: la que sale se aleja mientras la que entra llega desde delante. Las dos se mueven
     * en el mismo eje, que es lo que lo separa de «contenedor», donde la de debajo se queda
     * quieta.
     */
    ScreenTransition.ZOOM -> TransicionDePantalla(
        entra = scaleIn(fundido, initialScale = 1.18f) + fadeIn(fundido),
        sale = scaleOut(fundido, targetScale = 0.9f) + fadeOut(fundido),
        vuelveEntrando = scaleIn(fundido, initialScale = 0.9f) + fadeIn(fundido),
        vuelveSaliendo = scaleOut(fundido, targetScale = 1.18f) + fadeOut(fundido)
    )

    /*
     * La de arriba se descuelga. Es el reverso de «desde abajo», y sirve para quien tiene la
     * app en la mano y prefiere que lo nuevo caiga hacia el pulgar en vez de subir.
     */
    ScreenTransition.ARRIBA -> TransicionDePantalla(
        entra = slideInVertically(desplazamiento) { alto -> -alto },
        sale = fadeOut(fundido),
        vuelveEntrando = fadeIn(fundido),
        vuelveSaliendo = slideOutVertically(desplazamiento) { alto -> -alto }
    )

    /*
     * El mismo empuje del eje, girado. La que sale recorre un tercio hacia arriba: se queda
     * esperando debajo, igual que en horizontal.
     */
    ScreenTransition.EJE_VERTICAL -> TransicionDePantalla(
        entra = slideInVertically(desplazamiento) { alto -> alto },
        sale = slideOutVertically(desplazamiento) { alto -> -alto / 3 },
        vuelveEntrando = slideInVertically(desplazamiento) { alto -> -alto / 3 },
        vuelveSaliendo = slideOutVertically(desplazamiento) { alto -> alto }
    )

    /*
     * Tarjeta: la nueva sube entera por encima y la anterior se queda **detrás**, encogida y a
     * media luz. Es lo que separa esta de «desde abajo», donde la de debajo se va del todo:
     * aquí sigue ahí, y por eso volver atrás se siente como cerrar algo, no como navegar.
     */
    ScreenTransition.TARJETA -> TransicionDePantalla(
        entra = slideInVertically(desplazamiento) { alto -> alto },
        sale = scaleOut(fundido, targetScale = 0.92f) + fadeOut(fundido, targetAlpha = 0.6f),
        vuelveEntrando = scaleIn(fundido, initialScale = 0.92f) + fadeIn(fundido, initialAlpha = 0.6f),
        vuelveSaliendo = slideOutVertically(desplazamiento) { alto -> alto }
    )

    /*
     * Diagonal: los dos ejes a la vez, desde la esquina de abajo a la derecha. Es la más
     * llamativa de las diez y por eso no es la de casa, pero para quien quiere que se note
     * que cambió de sitio, se nota.
     */
    ScreenTransition.DIAGONAL -> TransicionDePantalla(
        entra = slideIn(desplazamiento) { tam -> IntOffset(tam.width, tam.height) },
        sale = fadeOut(fundido),
        vuelveEntrando = fadeIn(fundido),
        vuelveSaliendo = slideOut(desplazamiento) { tam -> IntOffset(tam.width, tam.height) }
    )
}

/**
 * La transición de Movimiento para lo que cambia de pantalla sin cambiar de ruta: los pasos de un
 * flujo y las vistas de un selector. Nula con el movimiento apagado.
 *
 * Existe porque esos cambios no pasan por el `NavHost` y saltaban de golpe: cerrar un periodo iba
 * del paso 1 al 2 sin moverse, igual que Materias y Tareas. Con la misma transición que las
 * pantallas, un paso se lee como la pantalla siguiente. Lo que va dentro tiene que llevar fondo
 * opaco, por lo mismo que `screen()`.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun transicionEntreVistas(): TransicionDePantalla? {
    if (LocalMotionDurationScale.current <= 0f) return null
    return transicionDe(
        estilo = motionActual().screenTransition,
        desplazamiento = MaterialTheme.motionScheme.defaultSpatialSpec(),
        fundido = MaterialTheme.motionScheme.defaultEffectsSpec()
    )
}

/**
 * El cambio de una vista a otra, hacia delante o hacia atrás según [orden].
 *
 * La capa también sale del orden: la vista de más adelante se pinta encima. Al avanzar, la nueva
 * tapa a la anterior; al volver, la que se va sigue encima mientras se retira, igual que en el
 * `NavHost`.
 *
 * [recortar] en falso deja que lo que se desliza se vea fuera de su caja. Hace falta cuando la
 * vista va dentro de los márgenes de la pantalla: recortada, desaparecería a veinte puntos del
 * borde en vez de salir por él.
 */
internal fun <S> AnimatedContentTransitionScope<S>.cambioDeVista(
    transicion: TransicionDePantalla?,
    recortar: Boolean = true,
    orden: (S) -> Int
): ContentTransform {
    val desde = orden(initialState)
    val hacia = orden(targetState)
    val cambio = when {
        transicion == null -> EnterTransition.None togetherWith ExitTransition.None
        hacia >= desde -> transicion.entra togetherWith transicion.sale
        else -> transicion.vuelveEntrando togetherWith transicion.vuelveSaliendo
    }
    return (cambio using SizeTransform(clip = recortar)).apply { targetContentZIndex = hacia.toFloat() }
}
