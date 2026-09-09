@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.core.design.components.UniSwitch
import com.unistack.app.core.design.components.formaDeDistintivo
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.feature_user.domain.BadgeShape
import com.unistack.app.feature_user.domain.ChipStyle
import com.unistack.app.feature_user.domain.ProgressShape
import com.unistack.app.feature_user.domain.TextFieldStyle

/**
 * Las vistas previas de Apariencia, hechas con piezas **de la app** y no con rectángulos.
 *
 * La versión anterior enseñaba tres cajas grises al elegir superficie y una fila de puntos al
 * elegir distintivo. No servían: tres cajas del mismo tono se ven iguales con «plana» y con
 * «filete», y un punto suelto no dice qué va a pasar en una lista de materias.
 *
 * Aquí cada muestra usa el componente de verdad —[UniCard], [UniStackButton], [UniSwitch], la
 * marca de materia, la barra de progreso— con una materia y una tarea inventadas pero con la
 * forma que tienen las reales. Cambiar un ajuste mueve la muestra porque mueve el componente,
 * no porque la muestra lo imite: si mañana [UniCard] cambia, la muestra cambia con ella.
 */

/**
 * El marco de una muestra: una **ventana** a la app, no más contenido de la pantalla.
 *
 * Sin marco, la muestra se leía como un ajuste más: los botones de «Guardar» y «Cancelar»
 * parecían botones de esta pantalla y no un ejemplo de cómo van a verse en otra. El fondo
 * distinto, el filete y el rótulo con el punto rojo la separan de todo lo que sí se toca.
 *
 * El punto es el de una grabación, y está a propósito: dice «esto se está viendo pasar», que
 * es exactamente lo que hacen las muestras que se animan.
 */
@Composable
internal fun VentanaDeMuestra(
    titulo: String,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val esquema = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            // El fondo de la app y no el de la tarjeta: dentro de la ventana se ve la app
            // como es, con su propio fondo detrás.
            .background(esquema.background)
            .border(1.dp, esquema.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(esquema.surfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(esquema.error)
            )
            Text(
                text = titulo,
                style = SectionLabelStyle,
                color = esquema.onSurfaceVariant
            )
        }
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = contenido
        )
    }
}

/**
 * Un trozo real de Materias: la tarjeta con su marca, su promedio y su barra.
 *
 * Es la muestra de superficie, esquinas y densidad a la vez, porque las tres se juzgan en lo
 * mismo: cuánto se despega una tarjeta del fondo, cómo son sus esquinas y cuánto aire lleva
 * dentro. Separadas en tres cajas abstractas no se notaba ninguna.
 */
@Composable
fun VistaPreviaDeTarjeta(modifier: Modifier = Modifier) {
    val secciones = LocalSectionColors.current
    val apariencia = LocalAppearancePreferences.current
    VentanaDeMuestra(titulo = "MATERIAS", modifier = modifier) {
        UniCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(formaDeDistintivo(apariencia.badgeShape, "calculo-iii"))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "C",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Cálculo III",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "Vas 4,25 · te falta 3,1 en el tercer corte",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        "4,25",
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = secciones.onTrack
                    )
                }
                BarraDeProgresoReal(progreso = 0.68f)
            }
        }
    }
}

/**
 * La barra de progreso académico, con la forma elegida y avanzando.
 *
 * **Se anima a propósito.** Quieta, la recta y la ondulada se distinguen mal en una barra al
 * 68%: la onda de Material 3 Expressive se reconoce por cómo se mueve, no por su silueta.
 */
@Composable
fun BarraDeProgresoReal(progreso: Float = 0.68f, modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "progreso")
    val avance by transicion.animateFloat(
        initialValue = 0.12f,
        targetValue = progreso,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "avance"
    )
    if (LocalAppearancePreferences.current.academicProgressShape == ProgressShape.WAVY) {
        LinearWavyProgressIndicator(progress = { avance }, modifier = modifier.fillMaxWidth())
    } else {
        LinearProgressIndicator(progress = { avance }, modifier = modifier.fillMaxWidth())
    }
}

/**
 * Los dos botones que la app usa de verdad: la acción principal anclada y la de descartar.
 *
 * La versión anterior enseñaba un «Guardar» suelto, y con eso no se veía lo que más cambia al
 * tocar la forma: cómo queda una pareja de botones al lado de otro, que es como salen siempre
 * al pie de un formulario.
 */
@Composable
fun VistaPreviaDeBotones(modifier: Modifier = Modifier) {
    VentanaDeMuestra(titulo = "AL PIE DE UN FORMULARIO", modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            UniStackButton(
                text = stringResource(R.string.common_save),
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            UniStackButton(
                text = stringResource(R.string.common_cancel),
                onClick = {},
                modifier = Modifier.weight(1f),
                variant = UniStackButtonVariant.Tonal
            )
        }
    }
}

/**
 * Los chips de filtro tal como salen en Tareas, con uno marcado.
 *
 * Uno marcado y dos sin marcar, que es el estado normal: con los tres iguales no se veía la
 * diferencia entre «relleno» y «filete», que está justo en cómo se marca el activo.
 */
@Composable
fun VistaPreviaDeChips(modifier: Modifier = Modifier) {
    val esquema = MaterialTheme.colorScheme
    val estilo = LocalAppearancePreferences.current.chipStyle
    VentanaDeMuestra(titulo = "TAREAS", modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            run {
            val isEn = java.util.Locale.getDefault().language == "en"
            listOf((if (isEn) "Overdue" else "Vencidas") to true, (if (isEn) "Today" else "Hoy") to false, (if (isEn) "No course" else "Sin materia") to false)
        }.forEach { (texto, activo) ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .then(
                            when (estilo) {
                                ChipStyle.RELLENO -> Modifier.background(
                                    if (activo) esquema.primary else esquema.surfaceContainerHighest
                                )
                                ChipStyle.FILETE -> Modifier
                                    .background(if (activo) esquema.primaryContainer else Color.Transparent)
                                    .border(1.dp, esquema.outlineVariant, RoundedCornerShape(percent = 50))
                                ChipStyle.TEXTO -> Modifier
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (activo) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = when {
                                estilo == ChipStyle.RELLENO -> esquema.onPrimary
                                else -> esquema.primary
                            }
                        )
                    }
                    Text(
                        texto,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            estilo == ChipStyle.RELLENO && activo -> esquema.onPrimary
                            activo -> esquema.primary
                            else -> esquema.onSurface
                        }
                    )
                }
            }
        }
    }
}

/** El campo de «Nombre de la materia» del formulario, con su rótulo y su texto escrito. */
@Composable
fun VistaPreviaDeCampo(modifier: Modifier = Modifier) {
    val esquema = MaterialTheme.colorScheme
    val estilo = LocalAppearancePreferences.current.textFieldStyle
    VentanaDeMuestra(titulo = "CREAR UNA MATERIA", modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    when (estilo) {
                        TextFieldStyle.RELLENO -> Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .background(esquema.surfaceContainerHighest)
                        TextFieldStyle.FILETE -> Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .border(1.5.dp, esquema.primary, MaterialTheme.shapes.medium)
                        TextFieldStyle.SUBRAYADO -> Modifier
                    }
                )
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            val isEnMat = java.util.Locale.getDefault().language == "en"
            Text(if (isEnMat) "Subject name" else "Nombre de la materia", style = MaterialTheme.typography.labelSmall, color = esquema.primary)
            val isEnCal = java.util.Locale.getDefault().language == "en"
            Text(if (isEnCal) "Calculus III" else "Cálculo III", style = MaterialTheme.typography.bodyLarge, color = esquema.onSurface)
        }
        if (estilo == TextFieldStyle.SUBRAYADO) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(esquema.primary)
            )
        }
    }
}

/**
 * Cinco materias con sus distintivos, como se ven en la lista.
 *
 * Cinco y no una: la gracia de «Aleatorio» es que dos materias del mismo color se distinguen
 * por la forma, y eso solo se ve con varias juntas.
 */
@Composable
fun VistaPreviaDeDistintivos(modifier: Modifier = Modifier) {
    val apariencia = LocalAppearancePreferences.current
    val esquema = MaterialTheme.colorScheme
    val secciones = LocalSectionColors.current
    val materias = listOf(
        Triple("calculo", "Cálculo III", esquema.primary),
        Triple("fisica", "Física II", secciones.schedule),
        Triple("progra", "Programación", secciones.onTrack),
        Triple("estad", "Estadística", secciones.expenses),
        Triple("ingles", "Inglés IV", esquema.tertiary)
    )
    VentanaDeMuestra(titulo = "MATERIAS", modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            materias.take(3).forEach { (id, nombre, color) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(formaDeDistintivo(apariencia.badgeShape, id))
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            nombre.first().toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = esquema.surface
                        )
                    }
                    Text(nombre, style = MaterialTheme.typography.bodyMedium)
                }
            }
            // Las otras dos, solo la marca: es donde se ve el reparto de formas de un vistazo.
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.padding(top = 2.dp)) {
                materias.forEach { (id, _, color) ->
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(formaDeDistintivo(apariencia.badgeShape, id))
                            .background(color)
                    )
                }
                if (apariencia.badgeShape != BadgeShape.ALEATORIO) {
                    Text(
                        "todas iguales",
                        style = MaterialTheme.typography.labelSmall,
                        color = esquema.outline,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}

/** Dos filas de ajustes con sus interruptores, encendida y apagada, como en cualquier pantalla. */
@Composable
fun VistaPreviaDeInterruptores(modifier: Modifier = Modifier) {
    VentanaDeMuestra(titulo = "AJUSTES", modifier = modifier) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                val isEnSwitch = java.util.Locale.getDefault().language == "en"
                listOf(
                    Triple(if (isEnSwitch) "Class reminders" else "Recordar mis clases", if (isEnSwitch) "15 minutes before" else "15 minutos antes", true),
                    Triple(if (isEnSwitch) "Assignment alerts" else "Avisar de entregas", if (isEnSwitch) "The day before" else "El día anterior", false)
                ).forEach { (titulo, detalle, marcado) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(titulo, style = MaterialTheme.typography.titleSmallEmphasized)
                            Text(
                                detalle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        UniSwitch(checked = marcado, onCheckedChange = {})
                    }
                }
            }
        }
    }
}

/**
 * Una clase de Horario con su hora: donde se ve el primer día de la semana en contexto.
 *
 * La tira de siete letras sola no decía nada; con la fila de la clase debajo se entiende que
 * lo que cambia es por dónde empieza a contarse la semana en Horario y en Gastos.
 */
@Composable
fun VistaPreviaDeSemana(letras: List<String>, indiceDeHoy: Int, modifier: Modifier = Modifier) {
    val esquema = MaterialTheme.colorScheme
    VentanaDeMuestra(titulo = "HORARIO", modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            letras.forEachIndexed { indice, dia ->
                val hoy = indice == indiceDeHoy
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (hoy) esquema.primary else esquema.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        dia,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (hoy) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (hoy) esquema.onPrimary else esquema.onSurface
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                Icons.Rounded.CalendarMonth,
                contentDescription = null,
                tint = esquema.primary,
                modifier = Modifier.size(18.dp)
            )
            Text("Cálculo III · 10:00", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            Text("103F", style = MaterialTheme.typography.labelMedium, color = esquema.onSurfaceVariant)
        }
    }
}
