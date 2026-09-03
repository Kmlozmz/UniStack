@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.unistack.app.feature_grades.presentation

import androidx.compose.material3.Icon
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.shape.CircleShape
import com.unistack.app.feature_user.domain.BadgeShape
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import androidx.compose.foundation.background
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.togetherWith
import androidx.compose.animation.scaleOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.unistack.app.core.design.components.EvaluationBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.compose.material3.toPath
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.SubjectGradeCalculation
import com.unistack.app.core.utils.TargetOutlook
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_schedule.domain.ClassSession

/**
 * Una materia en la lista.
 *
 * Se lee en diagonal: quién, cuánto lleva evaluado, y cómo va. La cifra grande a la derecha es
 * el promedio y debajo va el estado en una palabra, que es lo que se mira primero al buscar
 * una materia concreta entre varias.
 *
 * La barra es [LinearWavyProgressIndicator] y lleva el color de la materia, nunca uno de
 * rendimiento: su longitud mide avance, y pintarla de rojo por ir mal la haría decir dos cosas
 * a la vez. El rendimiento lo llevan la cifra y el rótulo.
 */
@Composable
fun SubjectRow(
    subject: Subject,
    calculation: SubjectGradeCalculation,
    gradingScale: GradingScale,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** El bloque de clase, si lo tiene: de ahí salen el aula y el profesor. */
    classSession: ClassSession? = null,
    /** Marcada dentro de una selección. Solo tiene sentido con [onLongClick] puesto. */
    selected: Boolean = false,
    /**
     * Mantener pulsado. Es lo que abre la selección múltiple: no hay botón de «seleccionar»
     * porque un botón permanente ocupa sitio en una pantalla que casi siempre se usa para
     * mirar, no para borrar.
     */
    onLongClick: (() -> Unit)? = null,
    /**
     * El asa de arrastre, si la lista se puede ordenar.
     *
     * Se dibuja siempre y no solo con algo marcado: es lo único que dice que las materias se
     * pueden mover de sitio. Un gesto que no deja rastro en la pantalla no lo encuentra nadie,
     * y esa fue la primera versión de esto: mantener pulsada la fila servía para marcar y para
     * mover a la vez, y cuál de las dos obtenías dependía de si tu dedo se movía.
     */
    dragHandle: (@Composable () -> Unit)? = null
) {
    val sections = LocalSectionColors.current
    val accent = subjectAccent(subject)
    val atRisk = calculation.outlook == TargetOutlook.AT_RISK ||
        calculation.outlook == TargetOutlook.UNREACHABLE

    // Marcada manda sobre «en riesgo»: mientras seleccionas, lo que importa es cuáles llevas
    // marcadas, no cuál va mal. El aviso de riesgo vuelve al salir de la selección.
    val container = when {
        selected -> MaterialTheme.colorScheme.secondaryContainer
        atRisk -> sections.atRiskContainer
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val onContainer = when {
        selected -> MaterialTheme.colorScheme.onSecondaryContainer
        atRisk -> sections.onAtRiskContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    val support = if (atRisk) sections.onAtRiskContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier
            .fillMaxWidth()
            // El recorte va ANTES del toque, y ese es el arreglo.
            //
            // `Surface` recorta lo que lleva dentro, pero este modificador se le pasa desde
            // fuera: la onda del toque se dibujaba en el rectángulo completo de la fila y se
            // salía por las cuatro esquinas redondeadas. Recortando aquí, la onda no puede
            // pintar donde la tarjeta no llega.
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onLongClickLabel = if (onLongClick != null) "Marcar la materia" else null
            ),
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = onContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            /*
             * Marcada, el círculo de la inicial se convierte en un visto.
             *
             * El color de fondo por sí solo no bastaba: en una lista de siete materias, tres
             * marcadas y cuatro no se distinguían por un tono, y había que compararlas entre
             * ellas para saber cuál era cuál. El visto se lee fila a fila, sin comparar.
             *
             * Ocupa el sitio de la inicial en vez de añadir una casilla a un lado, porque una
             * casilla más empuja el texto y hace que la lista entera baile al entrar y salir
             * de la selección.
             */
            AnimatedContent(
                targetState = selected,
                transitionSpec = {
                    (scaleIn(spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow)) + fadeIn())
                        .togetherWith(scaleOut(targetScale = 0.7f) + fadeOut())
                },
                label = "marca de la materia"
            ) { isSelected ->
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Marcada",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    SubjectMark(letter = subject.name.take(1).uppercase(), color = accent, seed = subject.id)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = supportLine(subject, calculation, gradingScale),
                    style = MaterialTheme.typography.bodySmall,
                    color = support,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val place = classSession?.place
                val context = listOfNotNull(
                    place?.room?.takeIf(String::isNotBlank),
                    place?.professor?.takeIf(String::isNotBlank)
                ).joinToString(" · ")
                if (context.isNotBlank()) {
                    Text(
                        text = context,
                        style = MaterialTheme.typography.labelMedium,
                        color = support,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                EvaluationBar(
                    fraction = calculation.evaluatedSemesterFraction,
                    modifier = Modifier.fillMaxWidth(),
                    color = accent,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = calculation.currentAverage
                        ?.let { GradingScaleUtils.formatGrade(it, gradingScale) }
                        ?: "—",
                    style = MaterialTheme.typography.headlineSmallEmphasized
                )
                Text(
                    text = outlookLabel(calculation.outlook),
                    style = SectionLabelStyle,
                    color = if (atRisk) sections.onAtRiskContainer else sections.onTrack
                )
            }

            // El asa, al borde derecho. Ahí es donde la busca el pulgar, y donde la ponen las
            // listas ordenables de otras apps.
            dragHandle?.invoke()
        }
    }
}

/**
 * La marca de la materia: su inicial dentro de una forma de nueve lóbulos.
 *
 * Se dibuja con [Canvas] y no recortando una caja con la forma. Recortando no funcionaba —lo
 * que salía era el rectángulo sin recortar— y el mismo fallo se coló ya una vez en el hero de
 * Inicio; dibujar el trazado no depende de que el recorte llegue a aplicarse.
 */
@Composable
internal fun SubjectMark(letter: String, color: Color, seed: String, markSize: Dp = 48.dp) {
    /*
     * La forma sale del ajuste, y solo cae en el sorteo por identificador cuando el ajuste
     * dice «Aleatorio».
     *
     * El reparto por identificador estuvo fijo desde el principio y era lo unico que habia:
     * quien queria todas las materias con la misma forma no tenia como pedirlo. Ahora es una
     * de las seis opciones —y sigue siendo la interesante, porque es la que deja distinguir
     * dos materias del mismo color— pero ya no es la unica.
     */
    val estilo = LocalAppearancePreferences.current.badgeShape
    val polygon = remember(seed, estilo) { markShapeFor(seed, estilo) }
    val path = polygon.toPath()

    Box(modifier = Modifier.size(markSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(markSize)) {
            withTransform({ scale(size.width, size.height, pivot = Offset.Zero) }) {
                drawPath(path, color)
            }
        }
        Text(
            text = letter,
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = MaterialTheme.colorScheme.surface
        )
    }
}

/**
 * La familia de formas de Material 3 Expressive, construidas con su mismo motor de polígonos.
 *
 * Cada una cambia en tres cosas: cuántos lóbulos tiene, cuánto se hunde entre ellos y cuánto
 * se redondean las puntas. De ahí salen la galleta, el trébol, el estallido, el sol y la flor.
 */
private val MarkShapes: List<() -> RoundedPolygon> = listOf(
    { markPolygon(vertices = 9, innerRatio = 0.84f, rounding = 0.22f) },   // galleta
    { markPolygon(vertices = 4, innerRatio = 0.62f, rounding = 0.35f) },   // trébol
    { markPolygon(vertices = 12, innerRatio = 0.80f, rounding = 0.12f) },  // estallido suave
    { markPolygon(vertices = 8, innerRatio = 0.88f, rounding = 0.28f) },   // sol
    { markPolygon(vertices = 6, innerRatio = 0.66f, rounding = 0.32f) },   // flor
    { markPolygon(vertices = 7, innerRatio = 0.78f, rounding = 0.20f) },   // galleta de siete
    { markPolygon(vertices = 5, innerRatio = 0.72f, rounding = 0.30f) },   // pentágono blando
    { markPolygon(vertices = 10, innerRatio = 0.90f, rounding = 0.18f) }   // margarita
)

private fun markPolygon(vertices: Int, innerRatio: Float, rounding: Float): RoundedPolygon =
    RoundedPolygon.star(
        numVerticesPerRadius = vertices,
        radius = 0.5f,
        innerRadius = 0.5f * innerRatio,
        rounding = CornerRounding(rounding),
        centerX = 0.5f,
        centerY = 0.5f
    )

/**
 * Qué forma le toca a una materia.
 *
 * Sale de su identificador y no de un sorteo: así es distinta de la de al lado pero **siempre
 * la misma** para la misma materia. Una forma que cambiara en cada recomposición dejaría de
 * servir para reconocerla de un vistazo, que es justo para lo que está.
 */
private fun markShapeFor(seed: String, estilo: BadgeShape): RoundedPolygon = when (estilo) {
    BadgeShape.CIRCULO -> markPolygon(vertices = 30, innerRatio = 1f, rounding = 0f)
    BadgeShape.GALLETA -> MarkShapes[0]()
    BadgeShape.TREBOL -> MarkShapes[1]()
    BadgeShape.SOL -> MarkShapes[3]()
    BadgeShape.ROMBO -> markPolygon(vertices = 4, innerRatio = 1f, rounding = 0.06f)
    // El reparto de siempre: sale del identificador y no de un sorteo, asi que es distinta de
    // la de al lado pero **siempre la misma** para la misma materia. Una forma que cambiara en
    // cada recomposicion dejaria de servir para reconocerla de un vistazo.
    BadgeShape.ALEATORIO -> MarkShapes[Math.floorMod(seed.hashCode(), MarkShapes.size)]()
}

/**
 * La línea de apoyo.
 *
 * Con la meta en riesgo cambia de tema a propósito: cuánto queda por evaluar deja de ser lo
 * útil, y lo que hace falta saber es qué nota hay que sacar en lo que falta para alcanzarla.
 */
private fun supportLine(
    subject: Subject,
    calculation: SubjectGradeCalculation,
    gradingScale: GradingScale
): String {
    // El corte va delante cuando el usuario ha elegido uno. Mientras no lo haya elegido no se
    // nombra ninguno: la app no sabe en qué punto del semestre va, y suponerlo fue justo el
    // fallo que se corrigió al dejar activeCutId vacío de nacimiento.
    val cut = subject.chosenCutId
        ?.let { id -> subject.cutScheme.cuts.firstOrNull { it.id == id } }
        ?.name
    val state = progressState(calculation, gradingScale)
    // Mayúscula al principio y en ningún otro sitio. Las piezas se escriben en minúscula
    // porque cualquiera de ellas puede ir en medio: con el corte delante, «Falta el 35 %»
    // quedaba como «Corte 2 · Falta el 35 %», con una mayúscula suelta a media frase.
    return listOfNotNull(cut, state).joinToString(" · ").replaceFirstChar(Char::uppercase)
}

private fun progressState(
    calculation: SubjectGradeCalculation,
    gradingScale: GradingScale
): String {
    val remaining = ((1.0 - calculation.evaluatedSemesterFraction) * 100).toInt().coerceIn(0, 100)
    val needed = calculation.neededForTarget
    return when {
        calculation.outlook == TargetOutlook.UNREACHABLE -> "la meta ya no se alcanza"
        calculation.outlook == TargetOutlook.AT_RISK && needed != null ->
            "necesitas ${GradingScaleUtils.formatGrade(needed, gradingScale)} en lo que falta"
        calculation.outlook == TargetOutlook.NO_DATA -> "sin notas todavía"
        remaining == 0 -> "todo evaluado"
        else -> "falta el $remaining %"
    }
}

private fun outlookLabel(outlook: TargetOutlook): String = when (outlook) {
    TargetOutlook.NO_DATA -> "SIN NOTAS"
    TargetOutlook.SECURED -> "ASEGURADA"
    TargetOutlook.ON_TRACK -> "AL DÍA"
    TargetOutlook.AT_RISK -> "EN RIESGO"
    TargetOutlook.UNREACHABLE -> "FUERA DE ALCANCE"
}
