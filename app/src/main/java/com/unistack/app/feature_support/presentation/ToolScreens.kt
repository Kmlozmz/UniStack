@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.unistack.app.feature_support.presentation

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.dismissKeyboardOnTapOutside
import com.unistack.app.feature_support.domain.QuickNotesStore

import androidx.compose.material3.MaterialTheme
import com.unistack.app.core.design.theme.LocalSectionColors
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
/**
 * Notas rápidas: una hoja, y nada más.
 *
 * Era un campo con contorno de doce líneas, dentro de una tarjeta, dentro de la pantalla: tres
 * marcos para escribir una nota. Aquí el papel es la pantalla —sin caja, sin borde— y lo único
 * que lo acompaña es lo que hace falta saber: si está guardado y cuánto cabe.
 */
@Composable
fun QuickNotesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val stored by QuickNotesStore.observe(context).collectAsStateWithLifecycle()
    var text by remember(stored.loaded) { mutableStateOf(stored.text) }
    var confirmingClear by rememberSaveable { mutableStateOf(false) }

    // Se guarda al parar de escribir, no en cada tecla: escribir en un archivo por letra es
    // trabajo de disco para nada.
    LaunchedEffect(text) {
        if (stored.loaded && text != stored.text) {
            kotlinx.coroutines.delay(600)
            QuickNotesStore.save(context, text)
        }
    }

    val saved = stored.loaded && text == stored.text

    Scaffold(
        modifier = modifier.fillMaxSize().dismissKeyboardOnTapOutside(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    UniIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Atrás",
                        onClick = onBackClick
                    )
                },
                actions = {
                    /*
                     * «Guardado», dicho en voz alta.
                     *
                     * La nota se escribe en disco 600 ms después de la última tecla y nada lo
                     * indicaba: se salía de la pantalla con la duda de si se había perdido. El
                     * aviso solo aparece cuando hay algo escrito, para no saludar a una hoja
                     * en blanco.
                     */
                    AnimatedVisibility(visible = saved && text.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = LocalSectionColors.current.onTrackContainer,
                            contentColor = LocalSectionColors.current.onOnTrackContainer,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "Guardado",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 22.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${text.length} de 4000",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { confirmingClear = true },
                        enabled = text.isNotEmpty(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(7.dp))
                        Text("Vaciar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            Column(
                modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    "Notas rápidas",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "Toma notas o apunta lo que no quieras olvidar.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                if (text.isEmpty()) {
                    Text(
                        "Escribe aquí…",
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 22.dp, end = 22.dp)
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = { text = it.take(4000) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 22.dp, end = 22.dp, bottom = 22.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 25.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }

    if (confirmingClear) {
        /*
         * Vaciar preguntando.
         *
         * Borraba las cuatro mil letras de un toque, sin diálogo y sin deshacer, con el botón
         * a un dedo del teclado. Es el único sitio de la app donde se pierde texto escrito a
         * mano y no había copia de nada.
         */
        AlertDialog(
            onDismissRequest = { confirmingClear = false },
            title = { Text("¿Vaciar la nota?") },
            text = {
                Text(
                    "Se borra todo lo que hay escrito y no se puede deshacer. " +
                        "Son ${text.length} caracteres."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    text = ""
                    QuickNotesStore.save(context, "")
                    confirmingClear = false
                }) {
                    Text("Vaciar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingClear = false }) { Text("Cancelar") }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
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
            UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(46.dp)
                                .clip(MaterialTheme.shapes.large)
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
            UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Lo que traerá", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                    plans.forEach { plan ->
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                Modifier
                                    .padding(top = 6.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
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
