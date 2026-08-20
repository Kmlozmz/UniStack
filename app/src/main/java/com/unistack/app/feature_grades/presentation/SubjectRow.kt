@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_grades.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.style.TextOverflow
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
    classSession: ClassSession? = null
) {
    val sections = LocalSectionColors.current
    val accent = subjectAccent(subject)
    val atRisk = calculation.outlook == TargetOutlook.AT_RISK ||
        calculation.outlook == TargetOutlook.UNREACHABLE

    val container = if (atRisk) sections.atRiskContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val onContainer = if (atRisk) sections.onAtRiskContainer else MaterialTheme.colorScheme.onSurface
    val support = if (atRisk) sections.onAtRiskContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = onContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SubjectMark(letter = subject.name.take(1).uppercase(), color = accent, seed = subject.id)

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
                LinearWavyProgressIndicator(
                    progress = { calculation.evaluatedSemesterFraction.toFloat().coerceIn(0f, 1f) },
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
private fun SubjectMark(letter: String, color: Color, seed: String) {
    val polygon = remember(seed) { markShapeFor(seed) }
    val path = polygon.toPath()

    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(48.dp)) {
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
private fun markShapeFor(seed: String): RoundedPolygon {
    val index = Math.floorMod(seed.hashCode(), MarkShapes.size)
    return MarkShapes[index]()
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
    // fallo que se corrigió al dejar activePeriodId vacío de nacimiento.
    val period = subject.chosenPeriodId
        ?.let { id -> subject.periodScheme.periods.firstOrNull { it.id == id } }
        ?.name
    val state = progressState(calculation, gradingScale)
    // Mayúscula al principio y en ningún otro sitio. Las piezas se escriben en minúscula
    // porque cualquiera de ellas puede ir en medio: con el corte delante, «Falta el 35 %»
    // quedaba como «Corte 2 · Falta el 35 %», con una mayúscula suelta a media frase.
    return listOfNotNull(period, state).joinToString(" · ").replaceFirstChar(Char::uppercase)
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
