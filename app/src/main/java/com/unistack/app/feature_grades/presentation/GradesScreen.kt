@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_grades.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.unistack.app.core.design.components.cleanClickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.CategoricalSubjectAccents
import com.unistack.app.core.design.theme.anchoredButtonRoom
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.TargetOutlook
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import java.util.Locale

import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.contentColorOn
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.foundation.layout.defaultMinSize
private val SpanishLocale: java.util.Locale = java.util.Locale.forLanguageTag("es")

@Composable
fun GradesScreen(
    onAddSubjectClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = hiltViewModel(),
    embedded: Boolean = false,
    /** Texto por el que filtrar las materias. Vacío es no filtrar. */
    nameQuery: String = ""
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val classSessions by viewModel.classSessions.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
    val averages = subjects.mapNotNull(viewModel::currentAverage)
    val generalAverage = averages.takeIf { it.isNotEmpty() }?.average()
    val evaluatedSubjects = subjects.count { it.grades.isNotEmpty() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // En curso / En riesgo / Cerradas. Sale de lo evaluado y del pronóstico, así que son
        // estados comprobables y no etiquetas que alguien tenga que mantener a mano.
        var filter by rememberSaveable { mutableStateOf(SubjectFilter.ACTIVE) }
        val calculations = subjects.associateWith(viewModel::calculationFor)
        val query = nameQuery.trim().lowercase(SpanishLocale)
        val visible = subjects.filter { subject ->
            query.isBlank() || subject.name.lowercase(SpanishLocale).contains(query)
        }.filter { subject ->
            val calculation = calculations.getValue(subject)
            when (filter) {
                SubjectFilter.ACTIVE -> !calculation.isFinished
                SubjectFilter.AT_RISK -> calculation.outlook == TargetOutlook.AT_RISK ||
                    calculation.outlook == TargetOutlook.UNREACHABLE
                SubjectFilter.CLOSED -> calculation.isFinished
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = if (embedded) 4.dp else 58.dp,
                end = 20.dp,
                bottom = scrollBottomRoom + anchoredButtonRoom
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!embedded) {
                item {
                    FeatureHeader(
                        title = "Materias",
                        subtitle = "Administra tus materias, notas y porcentajes."
                    )
                }
            }
            item("filtros") {
                /*
                 * Los filtros, con la interacción entre vecinos.
                 *
                 * `animateWidth` es el efecto: al pulsar uno se estira y los de al lado se
                 * comprimen para dejarle sitio.
                 *
                 * **El texto va con `softWrap = false`, y ese es el arreglo.** Mientras dura
                 * el estirado, el botón mide menos de lo que ocupa su rótulo por un instante,
                 * y `Text` hacía lo que hace siempre en ese caso: partirlo en dos líneas. Se
                 * veía «Pendient / e» durante la pulsación. Sin ajuste de línea no hay dónde
                 * partirlo, así que la palabra aguanta entera hasta que el botón recupera su
                 * ancho.
                 */
                ButtonGroup(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SubjectFilter.entries.forEach { option ->
                        val interactionSource = remember { MutableInteractionSource() }
                        val isSelected = filter == option
                        OutlinedToggleButton(
                            checked = isSelected,
                            onCheckedChange = { filter = option },
                            interactionSource = interactionSource,
                            /*
                             * Marcado, el contenedor del acento.
                             *
                             * Llevaba el contenedor secundario, que en esta paleta es un gris
                             * violáceo: puesto debajo del selector de arriba —relleno con el
                             * acento a plena fuerza— no se leía como elegido, se leía como
                             * apagado. Con el contenedor del acento pertenece a la misma
                             * familia sin competir con él.
                             */
                            colors = ToggleButtonDefaults.outlinedToggleButtonColors(
                                checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 38.dp)
                                .animateWidth(interactionSource)
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.size(6.dp))
                            }
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
            if (subjects.isEmpty()) {
                item { EmptyGradesCard() }
            } else if (visible.isEmpty()) {
                item("vacio-filtro") {
                    Text(
                        text = when (filter) {
                            SubjectFilter.ACTIVE -> "No tienes materias en curso."
                            SubjectFilter.AT_RISK -> "Ninguna materia está en riesgo. Bien ahí."
                            SubjectFilter.CLOSED -> "Todavía no has cerrado ninguna materia."
                        }.takeIf { query.isBlank() } ?: "Ninguna materia se llama así.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(visible, key = { it.id }) { subject ->
                    SubjectRow(
                        subject = subject,
                        calculation = calculations.getValue(subject),
                        gradingScale = scale,
                        onClick = { onSubjectClick(subject.id) },
                        classSession = classSessions.firstOrNull { it.subjectId == subject.id }
                    )
                }
            }
        }

        if (!embedded) {
            // Fuera de la pestaña de Académico esta pantalla no tiene menú de crear, así que
            // conserva su botón anclado. Dentro sí lo hay, y dos formas de crear lo mismo a
            // diez píxeles una de otra se tapaban entre ellas.
            AddSubjectButton(
                onClick = onAddSubjectClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
            )
        }
    }
}



private data class SubjectTone(
    val color: Color,
    val label: String
)


@Composable
private fun EmptyGradesCard() {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 0.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Rounded.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("Aún no tienes materias.", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Text("Crea tu primera materia para empezar a calcular tu promedio.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FeatureHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AddSubjectButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .cleanClickable(onClick),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = contentColorOn(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "Agregar materia",
                color = contentColorOn(MaterialTheme.colorScheme.primary),
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
@ReadOnlyComposable
fun subjectAccent(type: SubjectVisualType): Color = when (type) {
    SubjectVisualType.TEAL -> MaterialTheme.colorScheme.tertiary
    SubjectVisualType.BLUE -> LocalSectionColors.current.schedule
    SubjectVisualType.CORAL -> MaterialTheme.colorScheme.error
    SubjectVisualType.PURPLE -> MaterialTheme.colorScheme.primary
    SubjectVisualType.GREEN -> LocalSectionColors.current.onTrack
    SubjectVisualType.YELLOW -> LocalSectionColors.current.atRisk
    SubjectVisualType.ROSE -> CategoricalSubjectAccents.Rose
    SubjectVisualType.INDIGO -> CategoricalSubjectAccents.Indigo
    SubjectVisualType.ORANGE -> CategoricalSubjectAccents.Orange
    SubjectVisualType.CYAN -> CategoricalSubjectAccents.Cyan
    SubjectVisualType.LIME -> CategoricalSubjectAccents.Lime
    SubjectVisualType.SLATE -> CategoricalSubjectAccents.Slate
}

@Composable
@ReadOnlyComposable
fun subjectAccent(subject: Subject): Color {
    return subject.customColor?.let { Color(it) } ?: subjectAccent(subject.visualType)
}


/** Los tres estados por los que se filtra la lista de materias. */
private enum class SubjectFilter(val label: String) {
    ACTIVE("En curso"),
    AT_RISK("En riesgo"),
    CLOSED("Cerradas")
}
