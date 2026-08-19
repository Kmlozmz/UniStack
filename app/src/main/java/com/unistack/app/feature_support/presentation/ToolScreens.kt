package com.unistack.app.feature_support.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.dismissKeyboardOnTapOutside
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_support.domain.GpaRow
import com.unistack.app.feature_support.domain.QuickNotesStore
import com.unistack.app.feature_support.domain.weightedAverage

import androidx.compose.material3.MaterialTheme
import com.unistack.app.core.design.theme.LocalSectionColors
import androidx.compose.material3.Button
/**
 * Calculadora de promedio.
 *
 * Simula: se escriben notas con su peso en créditos y sale el promedio ponderado, sin tocar
 * nada de lo que hay registrado. El botón de traer las materias rellena la tabla con lo que ya
 * llevas evaluado, que es de donde sale la pregunta de verdad —«si saco esto, ¿en cuánto
 * quedo?»— y ahorra copiarlo a mano.
 */
@Composable
fun GpaCalculatorScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GpaCalculatorViewModel = hiltViewModel()
) {
    val subjects by viewModel.subjectRows.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
    var rows by remember { mutableStateOf(listOf(GpaRow(), GpaRow(), GpaRow())) }

    val average = weightedAverage(rows)
    val averageText = average?.let { value ->
        scale?.let { GradingScaleUtils.formatGrade(value, it) } ?: String.format("%.2f", value)
    } ?: "—"

    SupportScaffold(
        title = "Calculadora GPA",
        subtitle = "Simula tu promedio sin tocar tus notas",
        onBackClick = onBackClick,
        modifier = modifier.dismissKeyboardOnTapOutside()
    ) {
        item {
            UniCard(modifier = Modifier.fillMaxWidth(), shape = AppShapes.LargeCard) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Promedio simulado", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Text(
                        averageText,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 38.sp,
                        lineHeight = 40.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        if (average == null) {
                            "Escribe al menos una nota con su peso."
                        } else {
                            "Sobre ${GradingScaleUtils.formatGrade(maxGrade, scale ?: com.unistack.app.feature_user.domain.GradingScale.ZERO_TO_FIVE)} · ${rows.count { it.isUsable }} materias contadas"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { rows = rows + GpaRow() },
                    modifier = Modifier.weight(1f),
                    shape = AppShapes.Pill,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("Añadir fila", fontSize = 13.sp)
                }
                Button(
                    onClick = {
                        rows = subjects.ifEmpty { rows }
                    },
                    enabled = subjects.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    shape = AppShapes.Pill,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Traer mis materias", fontSize = 13.sp)
                }
            }
        }
        itemsIndexedRows(rows) { index, row ->
            UniCard(modifier = Modifier.fillMaxWidth(), shape = AppShapes.MediumCard) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = row.name,
                            onValueChange = { value ->
                                rows = rows.mapIndexed { i, current -> if (i == index) current.copy(name = value.take(30)) else current }
                            },
                            label = { Text("Materia") },
                            singleLine = true,
                            shape = AppShapes.MediumCard,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { rows = rows.filterIndexed { i, _ -> i != index } },
                            enabled = rows.size > 1
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "Quitar fila", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = row.grade,
                            onValueChange = { value ->
                                rows = rows.mapIndexed { i, current -> if (i == index) current.copy(grade = value.filter { it.isDigit() || it == '.' || it == ',' }.take(6)) else current }
                            },
                            label = { Text("Nota") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = AppShapes.MediumCard,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = row.credits,
                            onValueChange = { value ->
                                rows = rows.mapIndexed { i, current -> if (i == index) current.copy(credits = value.filter { it.isDigit() || it == '.' || it == ',' }.take(4)) else current }
                            },
                            label = { Text("Créditos") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = AppShapes.MediumCard,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        item {
            Text(
                "Los créditos pueden quedar en blanco: sin ellos todas las materias pesan igual.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.itemsIndexedRows(
    rows: List<GpaRow>,
    content: @Composable (Int, GpaRow) -> Unit
) {
    rows.forEachIndexed { index, row ->
        item(key = row.id) { content(index, row) }
    }
}

/**
 * Bloc de notas.
 *
 * Se guarda solo, en este teléfono, en cuanto dejas de escribir. No entra en la agenda ni en
 * las tareas a propósito: es el papel donde se apunta el aula que cambió o el tema del parcial,
 * y obligar a elegir materia y fecha para eso es justo lo que hace que no se apunte.
 */
@Composable
fun QuickNotesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val stored by QuickNotesStore.observe(context).collectAsStateWithLifecycle()
    var text by remember(stored.loaded) { mutableStateOf(stored.text) }

    // Se guarda al parar de escribir, no en cada tecla: escribir en un archivo por letra es
    // trabajo de disco para nada.
    LaunchedEffect(text) {
        if (stored.loaded && text != stored.text) {
            kotlinx.coroutines.delay(600)
            QuickNotesStore.save(context, text)
        }
    }

    SupportScaffold(
        title = "Notas rápidas",
        subtitle = "Se guarda solo en este teléfono",
        onBackClick = onBackClick,
        modifier = modifier.dismissKeyboardOnTapOutside()
    ) {
        item {
            UniCard(modifier = Modifier.fillMaxWidth(), shape = AppShapes.LargeCard) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it.take(4000) },
                        label = { Text("Escribe aquí") },
                        minLines = 12,
                        shape = AppShapes.MediumCard,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${text.length} de 4000",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                text = ""
                                QuickNotesStore.save(context, "")
                            },
                            enabled = text.isNotEmpty(),
                            shape = AppShapes.Pill,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Vaciar", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Lo que se está construyendo, contado sin fingir que ya está.
 *
 * Una pantalla propia y no una fila apagada: el panel lleva a algún sitio siempre, y ese sitio
 * explica qué va a hacer la función y en qué punto está.
 */
@Composable
private fun ComingSoonScreen(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    headline: String,
    body: String,
    plans: List<String>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SupportScaffold(
        title = title,
        subtitle = subtitle,
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        item {
            UniCard(modifier = Modifier.fillMaxWidth(), shape = AppShapes.LargeCard) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(46.dp)
                                .clip(AppShapes.MediumCard)
                                .background(accent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(headline, color = MaterialTheme.colorScheme.onSurface, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                            Text("En construcción", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 19.sp)
                }
            }
        }
        item {
            UniCard(modifier = Modifier.fillMaxWidth(), shape = AppShapes.LargeCard) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Lo que traerá", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                    plans.forEach { plan ->
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                Modifier
                                    .padding(top = 6.dp)
                                    .size(6.dp)
                                    .clip(AppShapes.Pill)
                                    .background(accent)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(plan, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiAssistantScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    ComingSoonScreen(
        title = "UniStack AI",
        subtitle = "Tu asistente académico",
        icon = Icons.Rounded.AutoAwesome,
        accent = MaterialTheme.colorScheme.primary,
        headline = "El asistente todavía no está listo",
        body = "La idea es que responda sobre lo que ya tienes registrado: cuánto necesitas en el " +
            "parcial que viene, qué semana se te junta todo, qué materia conviene atender primero. " +
            "Mientras no funcione de verdad, no va a estar encendido a medias.",
        plans = listOf(
            "Preguntas sobre tus notas y tu horario, en lenguaje normal.",
            "Aviso cuando una meta deje de ser alcanzable, con la cuenta hecha.",
            "Resumen de la semana con lo que hay que entregar y estudiar."
        ),
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@Composable
fun LabsScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    ComingSoonScreen(
        title = "Labs",
        subtitle = "Funciones experimentales",
        icon = Icons.Rounded.Science,
        accent = LocalSectionColors.current.atRisk,
        headline = "Todavía no hay experimentos abiertos",
        body = "Labs será donde se puedan encender funciones a medio hacer, con el aviso de que " +
            "pueden fallar. Ahora mismo no hay ninguna: las que están a medias se prueban en el " +
            "canal alpha, que ya cumple ese papel.",
        plans = listOf(
            "Interruptores para probar funciones antes de que estén terminadas.",
            "Un sitio para decir qué tal fue cada experimento.",
            "Apagarlas todas de golpe si algo se tuerce."
        ),
        onBackClick = onBackClick,
        modifier = modifier
    )
}
