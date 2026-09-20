@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import com.unistack.app.core.design.components.EvaluationBar
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.formaDeDistintivo
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.feature_user.domain.BadgeShape

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
    VentanaDeMuestra(titulo = stringResource(R.string.preview_win_subjects), modifier = modifier) {
        UniCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(formaDeDistintivo(BadgeShape.ALEATORIO, "calculo-iii"))
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
                            stringResource(R.string.preview_calculus),
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            stringResource(R.string.preview_grade_line),
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
                EvaluationBar(fraction = 0.68)
            }
        }
    }
}






