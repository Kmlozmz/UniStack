@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FormatLineSpacing
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SwipeLeft
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_user.domain.MotionCatalog
import com.unistack.app.feature_user.domain.MotionChoice
import com.unistack.app.feature_user.domain.MotionGesture
import com.unistack.app.feature_user.domain.MotionPreference
import com.unistack.app.feature_user.domain.MotionPreferences
import com.unistack.app.feature_user.domain.MotionToggle
import com.unistack.app.core.utils.Textos

/**
 * Movimiento, con la forma exacta del diseño aprobado.
 *
 * La estructura no es libre: sale del artifact medida a medida —tarjeta de 16dp con 12 de
 * relleno, icono de 32 en su cuadrado teñido al 13%, rótulo de grupo con el recuento a la
 * derecha, y la rejilla de variantes **a dos columnas** con borde de 2dp y caja de 66dp de
 * alto—. Se había construido a tres columnas y con las tarjetas del sistema de ajustes, y por
 * eso no se parecía por mucho que las animaciones fueran las mismas.
 *
 * Tres formas de elegir, y cada una es la que le toca a su tipo de ajuste:
 *
 * - **Base**: un segmentado, porque «Rápida / Normal / Lenta» es una escala y una escala se
 *   lee mejor en fila que en rejilla. Debajo, un demo que se abre al pulsar.
 * - **Los veinte gestos**: la rejilla de variantes animadas, que es donde se compara.
 * - **Otros**: un interruptor y un demo que enseña las dos caras a la vez, porque ahí no hay
 *   variantes que comparar sino tenerlo o no tenerlo.
 */
@Composable
fun MotionSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val current = profile ?: return
    val appearance = current.appearancePreferences
    val motion = appearance.motion
    val activo = appearance.motionPreference == MotionPreference.FULL

    val base = MotionCatalog.gestures.filter { it.group == MotionCatalog.GROUP_BASE }
    val porGrupo = MotionCatalog.grouped().filter { it.first != MotionCatalog.GROUP_BASE }

    LargeTitleScaffold(
        title = stringResource(R.string.settings_motion_title),
        subtitle = stringResource(R.string.settings_motion_subtitle),
        onBackClick = onBackClick,
        modifier = modifier,
        horizontalPadding = spacing.screenHorizontal,
        topPadding = 8.dp,
        bottomPadding = scrollBottomRoom,
        itemSpacing = 6.dp
    ) {
        item { InterruptorMaestro(appearance.motionPreference, activo, viewModel) }

        val isEn = java.util.Locale.getDefault().language == "en"
        val baseCountStr = Textos.get(R.string.motion_ajustes, base.size)
        item { RotuloDeGrupo("BASE", baseCountStr) }
        items(base.size, key = { base[it].id }) { indice ->
            val gesto = base[indice]
            TarjetaDeAjuste(
                icono = iconoDe(gesto.id),
                color = colorDe(gesto.id),
                nombre = gesto.displayName,
                detalle = gesto.displayDetail
            ) {
                /*
                 * Escala en fila y no en rejilla: «Nada / Rapida / Normal / Lenta» se lee de
                 * izquierda a derecha, y en rejilla ese orden se pierde.
                 *
                 * Y es **el segmentado de la app**, el mismo de «Completo / Reducido / Nada»
                 * de la tarjeta de arriba. Estuvo siendo una tira estrecha con filete, copiada
                 * del diseno web: en la misma pantalla salian dos segmentados distintos a dos
                 * dedos de distancia, y no habia nada que explicara por que.
                 */
                UniSegmentedControl(
                    selected = gesto.read(motion).id,
                    options = gesto.options.map { UniSegmentedOption(value = it.id, label = it.displayLabel) },
                    onSelected = { id ->
                        gesto.options.firstOrNull { it.id == id }?.let { opcion ->
                            viewModel.updateAppearance { p -> p.copy(motion = gesto.write(p.motion, opcion)) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                // El demo de verdad, no la miniatura de la rejilla ampliada: cinco barras
                // con el muelle, un boton que se pulsa, el indicador real.
                DemoDesplegable(
                    gestoId = gesto.id,
                    tituloDeVentana = ventanaDe(gesto.id)
                ) { DemoDeBase(gesto.id, motion) }
            }
        }

        porGrupo.forEach { (grupo, gestos) ->
            item(key = "rotulo-$grupo") {
                val groupTitle = motionGroupDisplay(grupo)
                val countStr = stringResource(R.string.motion_gestos_variantes, gestos.size, gestos.sumOf { it.options.size })
                RotuloDeGrupo(
                    groupTitle,
                    countStr
                )
            }
            items(gestos.size, key = { gestos[it].id }) { indice ->
                val gesto = gestos[indice]
                TarjetaDeAjuste(
                    icono = iconoDe(gesto.id),
                    color = colorDe(gesto.id),
                    nombre = gesto.displayName,
                    detalle = gesto.displayDetail
                ) {
                    RejillaDeVariantes(
                        gesto = gesto,
                        motion = motion,
                        animar = activo,
                        onElegir = { opcion ->
                            viewModel.updateAppearance { p -> p.copy(motion = gesto.write(p.motion, opcion)) }
                        }
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------ piezas del diseño

/**
 * El rótulo de grupo: nombre a la izquierda, recuento a la derecha.
 *
 * Nueve píxeles, ochocientos de peso y setenta centésimas de espaciado entre letras, que son
 * las medidas del diseño. El recuento a la derecha no es adorno: dice cuánto hay debajo antes
 * de bajar a mirarlo.
 */
@Composable
private fun RotuloDeGrupo(nombre: String, recuento: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp, top = 16.dp, bottom = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = nombre,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.7.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = recuento,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.7.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

/**
 * La tarjeta de un ajuste: icono teñido, nombre, descripción y lo que venga debajo.
 *
 * **Los cuatro niveles del diseño.** Se veía todo plano porque la tarjeta estaba a un solo paso
 * del fondo y la caja de variante a otro: dos tonos casi iguales para tres capas distintas. El
 * diseño usa cuatro y bien separados —fondo, tarjeta, caja, y el filete un paso por encima de
 * la caja— y son exactamente los que el tema ya deriva:
 *
 * | Diseño | En la app |
 * |---|---|
 * | `--bg` | `background` |
 * | `--card` | `surfaceContainer` |
 * | `--card2` | `surfaceContainerHigh` |
 * | `--card3` y `--line` | `surfaceContainerHighest` |
 *
 * El cuadrado del icono va relleno al trece por ciento de su color y el icono a plena
 * intensidad, que es lo que hace que veinticinco tarjetas seguidas se distingan de un vistazo
 * sin leer ni un nombre.
 */
@Composable
private fun TarjetaDeAjuste(
    icono: ImageVector,
    color: Color,
    nombre: String,
    detalle: String,
    contenido: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    /*
     * La tarjeta es **la de la app**, no una inventada aqui.
     *
     * Estuvo siendo un `Surface` con radio 16 y su propio tono, copiados del diseno web. El
     * diseno web no tiene `UniCard`: tiene su propia escala de radios y sus propios grises, y
     * traerlos tal cual dejaba estas veinticinco tarjetas con una forma que no existe en
     * ninguna otra pantalla -- ni el radio de las esquinas, ni el filete, ni la sombra que
     * pida el ajuste de superficie. Lo que hay que copiar del diseno es **lo de dentro**: el
     * icono tenido, la rejilla a dos columnas, las medidas del texto. El envoltorio ya lo
     * tiene la app resuelto, y ademas obedece a Forma y superficie como todo lo demas.
     *
     * `medium` y no `large`: sigue saliendo de la escala del tema —asi que el ajuste de
     * esquinas la mueve como a todo lo demas— pero un escalon por debajo. Con `large`, una
     * tarjeta de veinticinco lineas seguidas de otras veinticuatro se leia como una hilera de
     * pastillas; el radio grande esta pensado para tarjetas sueltas, no para una lista larga.
     */
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        borderWidth = 1.dp,
        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 12.dp)
    ) {
        // UniCard pinta su contenido en un Box: sin la Column, el encabezado y lo de abajo se
        // superponen. Es el mismo tropiezo que ya dio la pantalla de Gastos.
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        nombre,
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        detalle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            contenido()
        }
    }
}

/**
 * La rejilla de variantes: **dos columnas**, como en el diseño.
 *
 * Estuvo a tres, y a tres la caja de cada variante mide sesenta y pocos píxeles de ancho: una
 * transición de pantalla dentro de eso es una mancha. A dos, la caja tiene sitio para que se
 * entienda qué entra y qué sale, que es de lo que va la pantalla.
 */
@Composable
private fun RejillaDeVariantes(
    gesto: MotionGesture,
    motion: MotionPreferences,
    animar: Boolean,
    onElegir: (MotionChoice) -> Unit
) {
    val elegida = gesto.read(motion)
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        gesto.options.chunked(2).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                fila.forEach { opcion ->
                    CajaDeVariante(
                        gestoId = gesto.id,
                        opcion = opcion,
                        elegida = opcion.id == elegida.id,
                        animar = animar,
                        // Cada variante arranca un poco despues que la anterior: sin esto van
                        // en fase y las cuatro se ven vacias en el mismo instante.
                        desfase = gesto.options.indexOf(opcion) * 0.11f,
                        modifier = Modifier.weight(1f),
                        onClick = { onElegir(opcion) }
                    )
                }
                // Una fila impar deja el hueco en vez de estirar la última al doble.
                if (fila.size == 1) Box(modifier = Modifier.weight(1f))
            }
        }
    }
}

/** Una variante: borde de 2dp, caja de 88dp con la animación, y el nombre debajo. */
@Composable
private fun CajaDeVariante(
    gestoId: String,
    opcion: MotionChoice,
    elegida: Boolean,
    animar: Boolean,
    desfase: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val tinta = tintaDemo()
    val esquema = MaterialTheme.colorScheme
    val t = bucle(duracionMs = duracionDe(gestoId), etiqueta = gestoId + opcion.id, desfase = desfase)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(if (elegida) esquema.primary.copy(alpha = 0.12f) else Color.Transparent)
            .border(
                width = 2.dp,
                // El filete sale del nivel de encima y no de `outlineVariant`: con la
                // superficie plana ese contorno casi no se ve, y una rejilla de seis cajas sin
                // borde visible se lee como una mancha.
                color = if (elegida) esquema.primary else esquema.surfaceContainerHighest,
                shape = RoundedCornerShape(13.dp)
            )
            .cleanClickable(onClick = onClick)
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                /*
                 * **La altura sale de la proporcion del lienzo, no a ojo.**
                 *
                 * Los dibujos se pintan sobre cien por sesenta y cuatro y ahora se escalan sin
                 * deformar, asi que una caja con otra proporcion solo anade margen muerto a los
                 * lados. A dos columnas en un movil normal la caja mide unos ciento treinta y
                 * cinco de ancho: con ochenta y ocho de alto y cinco de aire, lo de dentro
                 * queda en ciento veinticinco por setenta y ocho, que es la proporcion del
                 * lienzo casi clavada.
                 *
                 * De paso el dibujo sale un cuarto mas grande que con setenta y seis, y los
                 * rotulos de dentro por fin se leen.
                 */
                .height(88.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(esquema.surfaceContainerHigh)
                .padding(5.dp)
        ) {
            LienzoDemo(t = if (animar) t else 0.55f) { reloj ->
                pintarVariante(gestoId, opcion.id, reloj, tinta)
            }
        }
        Text(
            text = if (elegida) "✓ " + opcion.label else opcion.label,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (elegida) esquema.primary else esquema.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}


/**
 * El demo que se abre al pulsar, como el «▶ Ver» del diseño.
 *
 * Cerrado por defecto y no siempre abierto: con veinticinco tarjetas, cuatro demos permanentes
 * arriba del todo empujan la lista media pantalla hacia abajo antes de haber elegido nada.
 */
@Composable
private fun DemoDesplegable(
    gestoId: String,
    texto: String = "",
    tituloDeVentana: String = "",
    contenido: @Composable () -> Unit
) {
    var abierto by rememberSaveable(gestoId) { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        BotonDeDemo(abierto = abierto, texto = texto) { abierto = !abierto }
        AnimatedVisibility(
            visible = abierto,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            /*
             * El demo va dentro del marco de ventana, no en una caja lisa.
             *
             * Es el mismo que usan las muestras de Apariencia, y esta aqui por lo mismo: sin
             * marco, un boton «Pulsame» dentro de una tarjeta de ajustes parece un boton **de
             * la pantalla** y no un ejemplo. La cabecera con el punto dice «esto esta pasando»,
             * que es justo lo que hace un demo que corre en bucle.
             */
            VentanaDeMuestra(titulo = if (tituloDeVentana.isEmpty()) stringResource(R.string.settings_motion_in_app) else tituloDeVentana) { contenido() }
        }
    }
}

/** Cual de los demos de base toca. */
@Composable
private fun DemoDeBase(gestoId: String, motion: MotionPreferences) {
    when (gestoId) {
        "velocidad" -> DemoDeVelocidad(motion)
        else -> DemoDeCarga(motion)
    }
}

@Composable
private fun BotonDeDemo(abierto: Boolean, texto: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .cleanClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            if (abierto) Icons.Rounded.Check else Icons.Rounded.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            if (abierto) stringResource(R.string.settings_motion_hide) else if (texto.isEmpty()) stringResource(R.string.settings_motion_see) else texto,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** El interruptor maestro, que manda sobre los veinticinco de abajo. */
@Composable
private fun InterruptorMaestro(
    preferencia: MotionPreference,
    activo: Boolean,
    viewModel: ProfileViewModel
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        // Apagado, la tarjeta se tine: se nota que lo de abajo esta en pausa sin leer.
        color = if (activo) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        shape = MaterialTheme.shapes.medium,
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        borderWidth = 1.dp,
        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Animation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_motion_how_much), style = MaterialTheme.typography.titleSmallEmphasized)
                    Text(
                        text = if (activo) {
                            stringResource(R.string.settings_motion_master_active, MotionCatalog.gestures.size)
                        } else {
                            stringResource(R.string.settings_motion_master_paused)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            UniSegmentedControl(
                selected = preferencia,
                options = MotionPreference.entries.map { UniSegmentedOption(value = it, label = it.etiqueta()) },
                onSelected = { valor -> viewModel.updateAppearance { it.copy(motionPreference = valor) } },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ------------------------------------------------------------------ iconos y colores

/**
 * El icono de cada ajuste, con el color que le da el diseño.
 *
 * Van aquí y no en [MotionCatalog] para que el catálogo siga sin depender de Compose: es
 * dominio, y el dominio no tiene por qué saber qué es un `ImageVector`.
 */
private fun iconoDe(id: String): ImageVector = when (id) {
    "velocidad" -> Icons.Rounded.Bolt
    "carga" -> Icons.Rounded.Refresh
    "transicion" -> Icons.Rounded.Animation
    "listas" -> Icons.Rounded.FormatLineSpacing
    "claseAhora" -> Icons.Rounded.Schedule
    else -> Icons.Rounded.AutoAwesome
}

/** Los colores del diseño, uno por ajuste. Son los que hacen la lista legible de un vistazo. */
private fun colorDe(id: String): Color = when (id) {
    "velocidad" -> Color(0xFFE8693A)
    "carga", "celebracion" -> Color(0xFFE0A63C)
    "transicion", "sello" -> Color(0xFF3F8FE0)
    "listas", "claseAhora" -> Color(0xFF5FC96E)
    else -> Color(0xFF7F77DD)
}

/**
 * De donde sale lo que ensena cada demo.
 *
 * El titulo de la ventana no es decorativo: dice en que parte de la app se va a ver eso. Un
 * «Pulsame» bajo el rotulo «EN CUALQUIER TARJETA» se entiende sin leer la descripcion.
 */
private fun ventanaDe(id: String): String = Textos.get(
    when (id) {
        "velocidad", "listas" -> R.string.motion_window_list
        "carga" -> R.string.motion_window_loading
        "transicion" -> R.string.motion_window_anything
        "sello" -> R.string.motion_window_cut
        "celebracion" -> R.string.motion_window_tasks
        "claseAhora" -> R.string.motion_window_schedule
        else -> R.string.motion_window_app
    }
)

/** Cuánto dura una vuelta del bucle, por gesto. */
private fun duracionDe(gestoId: String): Int = when (gestoId) {
    "claseAhora" -> 2200
    "carga" -> 1800
    "velocidad" -> 1700
    "celebracion" -> 2600
    else -> 2200
}

private fun MotionPreference.etiqueta(): String {
    val isEn = java.util.Locale.getDefault().language == "en"
    return when (this) {
        MotionPreference.FULL -> Textos.get(R.string.motion_completo)
        MotionPreference.REDUCED -> Textos.get(R.string.a11y_motion_reduced)
        MotionPreference.NONE -> Textos.get(R.string.a11y_motion_none)
    }
}

private fun motionGroupDisplay(group: String): String = Textos.get(MotionCatalog.groupNameRes(group))


