@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_grades.presentation

import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.runtime.remember
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.unistack.app.core.utils.performSafely
import com.unistack.app.core.design.components.uniReorderableItem
import com.unistack.app.core.design.components.uniReorderHandle
import com.unistack.app.core.design.components.rememberUniReorderState
import com.unistack.app.core.design.components.SectionHeader
import com.unistack.app.core.design.components.UniConfirmDeleteDialog
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.core.design.components.UniSelectionToolbar
import com.unistack.app.core.design.components.UniChoiceRow
import com.unistack.app.core.design.components.UniSegmentedOption
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.setValue
private val SpanishLocale: java.util.Locale = java.util.Locale.forLanguageTag("es")

@Composable
fun GradesScreen(
    onAddSubjectClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    /** Abrir el formulario de una materia. Nulo cuando la pantalla no puede editar. */
    onEditSubjectClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = hiltViewModel(),
    embedded: Boolean = false,
    /** Texto por el que filtrar las materias. Vacío es no filtrar. */
    nameQuery: String = "",
    /**
     * Cuántas materias hay marcadas.
     *
     * Lo necesita Académico para esconder su botón de crear: con la barra de selección abajo y
     * el botón encima, los dos se pisan y el de crear no es lo que vas a pulsar mientras tienes
     * cinco materias marcadas.
     */
    onSelectionChange: (Int) -> Unit = {}
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
        // Los identificadores de lo marcado, no las materias enteras: `rememberSaveable` guarda
        // lo que quepa en un Bundle, y una lista de textos cabe.
        var selectedIds by rememberSaveable { mutableStateOf(listOf<String>()) }
        /*
         * El orden que has elegido tú, y el arrastre que lo cambia.
         *
         * `order` vive aquí y no en el perfil mientras dura el gesto: permutar dos materias
         * escribe en la lista veinte veces mientras cruzas la pantalla, y guardar en disco cada
         * una de esas veces es una escritura por fila recorrida. Se guarda al soltar.
         */
        val savedOrder = profile?.appearancePreferences?.subjectOrder.orEmpty()
        var order by remember(savedOrder, subjects) {
            mutableStateOf(
                // Lo guardado manda; lo que no aparezca —materias nuevas, o de antes de que
                // esto existiera— se queda detrás, en el orden en que se creó.
                savedOrder.filter { id -> subjects.any { it.id == id } } +
                    subjects.map { it.id }.filterNot(savedOrder::contains)
            )
        }
        val reorder = rememberUniReorderState()
        val haptics = LocalHapticFeedback.current

        // Marcar y desmarcar pasa siempre por aquí: así el golpecito y el aviso a Académico no
        // dependen de acordarse de ponerlos en cada sitio que toca la selección.
        fun toggle(id: String) {
            haptics.performSafely(HapticFeedbackType.LongPress)
            selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
        }

        LaunchedEffect(selectedIds.size) { onSelectionChange(selectedIds.size) }
        var pendingBulkDelete by rememberSaveable { mutableStateOf(false) }
        val selecting = selectedIds.isNotEmpty()
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
        }.sortedBy { subject ->
            order.indexOf(subject.id).takeIf { it >= 0 } ?: Int.MAX_VALUE
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = if (embedded) 10.dp else 58.dp,
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
                 * Los filtros van con contorno, no rellenos.
                 *
                 * Justo encima está el selector de Materias/Tareas, que sí va relleno porque
                 * cambia lo que hay debajo. Estos no: acotan la lista que ya estás mirando.
                 * Con los dos rellenos y uno pegado al otro, la pantalla enseñaba dos barras
                 * moradas seguidas y no se veía cuál mandaba sobre cuál.
                 */
                UniChoiceRow(
                    selected = filter,
                    options = SubjectFilter.entries.map {
                        UniSegmentedOption(value = it, label = it.label)
                    },
                    onSelected = { filter = it },
                    modifier = Modifier.fillMaxWidth()
                )
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
                        modifier = Modifier.uniReorderableItem(reorder, subject.id),
                        // El asa solo con la selección abierta.
                        //
                        // Permanente, cinco asas en la lista repetían un mando que casi nunca se
                        // usa y le robaban ancho al nombre de la materia, que es lo que se viene
                        // a leer: «ESTADOS FINAN...» en vez del nombre entero. Ordenar es algo
                        // que se hace una vez al empezar el semestre, no cada vez que se abre la
                        // pantalla.
                        dragHandle = if (!selecting) null else {
                            {
                            Icon(
                                imageVector = Icons.Rounded.DragHandle,
                                contentDescription = "Mover la materia",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .uniReorderHandle(
                                        state = reorder,
                                        key = subject.id,
                                        index = { order.indexOf(subject.id) },
                                        itemCount = { order.size },
                                        onMove = { from, to ->
                                            order = order.toMutableList()
                                                .apply { add(to, removeAt(from)) }
                                        },
                                        onSettle = { viewModel.saveSubjectOrder(order) }
                                    )
                            )
                            }
                        },
                        // Mantener pulsada la fila marca; mantener pulsada el asa mueve.
                        // Se pregunta por el asa y no por si ya está arrastrando: cuando esto
                        // se ejecuta, el arrastre puede no haber empezado todavía.
                        onLongClick = {
                            if (!reorder.isHandleHeld(subject.id)) toggle(subject.id)
                        },
                        subject = subject,
                        calculation = calculations.getValue(subject),
                        gradingScale = scale,
                        onClick = {
                            // Con una selección abierta, tocar marca y desmarca en vez de
                            // entrar: entrar a la materia a mitad de seleccionar cinco es
                            // perder las cinco.
                            if (selecting) {
                                toggle(subject.id)
                            } else {
                                onSubjectClick(subject.id)
                            }
                        },
                        selected = subject.id in selectedIds,
                        classSession = classSessions.firstOrNull { it.subjectId == subject.id }
                    )
                }
            }
        }

        /*
         * La barra de selección tapa el botón de crear, y eso es lo que se quiere.
         *
         * Mientras hay materias marcadas, crear una nueva no es lo que vas a hacer: lo que vas
         * a hacer es algo con las que marcaste. Cuando desmarcas la última, el botón vuelve.
         */
        UniSelectionToolbar(
            selectedCount = selectedIds.size,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        ) {
            UniIconButton(
                icon = Icons.Rounded.Close,
                contentDescription = "Quitar la selección",
                onClick = { selectedIds = emptyList() }
            )
            /*
             * Con una marcada y con varias no se ofrece lo mismo.
             *
             * Sobre una sola materia lo útil es editarla: abrirla ya se hace con un toque en
             * la fila, así que ofrecerlo aquí son dos toques para lo que costaba uno. Sobre
             * varias, editar no significa nada y lo que hace falta
             * es poder marcarlas todas de golpe. Enseñar las dos siempre obliga a mirar cuál
             * está apagada, y un botón apagado en una barra de cuatro es un estorbo.
             */
            if (selectedIds.size == 1 && onEditSubjectClick != null) {
                UniIconButton(
                    icon = Icons.Rounded.Edit,
                    contentDescription = "Editar la materia",
                    onClick = {
                        val only = selectedIds.first()
                        selectedIds = emptyList()
                        onEditSubjectClick(only)
                    }
                )
            } else {
                UniIconButton(
                    icon = Icons.Rounded.SelectAll,
                    contentDescription = "Marcar todas las de la lista",
                    onClick = { selectedIds = visible.map { it.id } }
                )
            }
            UniIconButton(
                icon = Icons.Rounded.Delete,
                contentDescription = if (selectedIds.size == 1) "Eliminar la materia" else "Eliminar las materias",
                onClick = { pendingBulkDelete = true }
            )
        }

        if (pendingBulkDelete) {
            UniConfirmDeleteDialog(
                title = if (selectedIds.size == 1) "¿Eliminar la materia?" else "¿Eliminar ${selectedIds.size} materias?",
                body = if (selectedIds.size == 1) {
                    "Se borran también sus notas. No se puede deshacer."
                } else {
                    "Se borran también todas sus notas. No se puede deshacer."
                },
                onConfirm = {
                    selectedIds.forEach(viewModel::deleteSubject)
                    selectedIds = emptyList()
                    pendingBulkDelete = false
                },
                onDismiss = { pendingBulkDelete = false }
            )
        }

        if (!embedded && !selecting) {
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
    SectionHeader(title = title, subtitle = subtitle)
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
