@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.unistack.app.feature_support.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.components.UniStackButtonDefaults
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_user.domain.GradingScale

/**
 * Qué hace la pestaña en la que estás, con un ejemplo con números.
 *
 * Un ejemplo y no una definición: «promedio ponderado por el peso de cada evaluación» describe
 * la cuenta a quien ya la sabe. «Sacaste 4,2 en el parcial, que vale el 30 %» la enseña.
 */
@Composable
internal fun CalculatorHelpSheet(
    tab: CalculatorTab,
    maxGrade: Double,
    scale: GradingScale,
    onDismiss: () -> Unit
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val top = GradingScaleUtils.formatGrade(maxGrade, scale)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state) {
        Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = tab.label,
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = when (tab) {
                    CalculatorTab.SUBJECT ->
                        "Para saber cómo vas en una sola materia, cuando cada nota vale un porcentaje distinto del curso."
                    CalculatorTab.SEMESTER ->
                        "Para el promedio de todo el semestre, donde cada materia pesa según sus créditos. " +
                            "Sirve igual para tus materias que para las de otra persona."
                    CalculatorTab.NEEDED ->
                        "Para saber qué nota necesitas en lo que te queda por evaluar si quieres acabar con una nota concreta."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("UN EJEMPLO", style = SectionLabelStyle, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = when (tab) {
                            CalculatorTab.SUBJECT ->
                                "Sacaste 4,2 en el primer parcial, que vale el 30 %. Escribes 4,2, tocas la flecha, " +
                                    "escribes 30 y tocas el más. Arriba verás 4,20 y que llevas el 30 % evaluado: " +
                                    "es tu nota si el curso acabara ahí."
                            CalculatorTab.SEMESTER ->
                                "Cálculo con 4,1 y 4 créditos, Álgebra con 3,0 y 3 créditos. El promedio no es 3,55: " +
                                    "es 3,63, porque Cálculo pesa más. Escribes la nota, tocas la flecha, escribes los " +
                                    "créditos y tocas el más."
                            CalculatorTab.NEEDED ->
                                "Llevas 3,5 con el 60 % evaluado y quieres acabar con 4,0. Hace falta un 4,75 en el 40 % " +
                                    "que queda. Si te pidiera más de $top, esa meta ya no se alcanza."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Text(
                text = when (tab) {
                    CalculatorTab.SUBJECT ->
                        "Los porcentajes no pueden pasar de 100 entre todos: si te pasas, te avisa y te dice cuánto " +
                            "queda libre. El nombre de arriba es opcional y solo sirve para no perderte si calculas " +
                            "varias materias seguidas."
                    CalculatorTab.SEMESTER ->
                        "Los créditos los pones tú: la app no los guarda por materia. Una materia sin créditos no " +
                            "entra en el promedio, y se avisa arriba."
                    CalculatorTab.NEEDED ->
                        "Nada de lo que hagas aquí toca tus notas registradas."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Cuáles de tus materias entran en la cuenta, y si vienen con sus notas.
 *
 * Elegir de una lista y no traerlas todas: quien calcula el semestre puede querer solo las de
 * un corte, o dejar fuera la que va a repetir. Y el interruptor de las notas existe porque las
 * dos preguntas son válidas —«cómo voy» y «cómo acabaría si sacara esto»— y la segunda pide la
 * materia sin su nota.
 */
@Composable
internal fun SubjectPickerSheet(
    subjects: List<CalculatorSubject>,
    scale: GradingScale,
    onDismiss: () -> Unit,
    onConfirm: (List<CalculatorSubject>, Boolean) -> Unit
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var withGrades by remember { mutableStateOf(true) }
    var picked by remember { mutableStateOf(setOf<String>()) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state) {
        Column(
            modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Traer mis materias",
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Elige cuáles quieres en la cuenta. Lo que hagas aquí no toca nada de lo que tienes registrado.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (subjects.isEmpty()) {
                Text(
                    text = "Todavía no tienes materias registradas. Cuando las tengas, aparecerán aquí.",
                    modifier = Modifier.padding(vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Surface(
                    onClick = { withGrades = !withGrades },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Traer también sus notas",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (withGrades) {
                                    "Entran con el promedio que llevas. Podrás cambiarlo aquí sin tocar la materia."
                                } else {
                                    "Entran en blanco, para que pongas tú la nota final."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = withGrades, onCheckedChange = { withGrades = it })
                    }
                }

                LazyColumn(
                    modifier = Modifier.heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(subjects.size, key = { subjects[it].id }) { index ->
                        val subject = subjects[index]
                        val on = subject.id in picked
                        Surface(
                            onClick = {
                                picked = if (on) picked - subject.id else picked + subject.id
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = if (on) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            contentColor = if (on) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(MaterialTheme.shapes.extraSmall)
                                        .background(
                                            if (on) MaterialTheme.colorScheme.primary
                                            else androidx.compose.ui.graphics.Color.Transparent
                                        )
                                        .border(
                                            2.dp,
                                            if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            MaterialTheme.shapes.extraSmall
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (on) {
                                        Icon(
                                            Icons.Rounded.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = subject.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = when {
                                            subject.average == null -> "Sin notas registradas"
                                            withGrades -> "Entra con ${GradingScaleUtils.formatGrade(subject.average, scale)}"
                                            else -> "Llevas ${GradingScaleUtils.formatGrade(subject.average, scale)}"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (on) {
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "Los créditos los tienes que poner tú: la app no los guarda por materia.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancelar")
                }
                Button(
                    shapes = UniStackButtonDefaults.shapes,
                    onClick = { onConfirm(subjects.filter { it.id in picked }, withGrades) },
                    enabled = picked.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = UniStackButtonDefaults.PrimaryHeight)
                ) {
                    Text(
                        if (picked.isEmpty()) "Elige alguna"
                        else if (picked.size == 1) "Traer 1 materia"
                        else "Traer ${picked.size} materias"
                    )
                }
            }
        }
    }
}
