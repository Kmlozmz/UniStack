package com.unistack.app.feature_support.presentation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.BuildConfig
import com.unistack.app.core.design.components.SquishyButton
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniStackWordmark
import com.unistack.app.core.design.components.dismissKeyboardOnTapOutside
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_updates.presentation.ReleaseNotes

/**
 * Las pantallas que el panel lateral prometía y no existían.
 *
 * «Novedades», «Recursos», «Ayuda y soporte» y «Acerca de» eran filas que cerraban el panel sin
 * llevar a ninguna parte. Ninguna necesita servidor ni cuenta: el registro de cambios ya está
 * escrito, las preguntas frecuentes también, y una sugerencia se manda por donde el propio
 * teléfono sepa mandarla.
 */

@Composable
private fun SupportScaffold(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    BackHandler(onBack = onBackClick)
    val spacing = LocalInterfaceSpacing.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = 8.dp,
            bottom = scrollBottomRoom
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Volver",
                        tint = UniStackColors.TextPrimary
                    )
                }
                Column(Modifier.padding(start = 2.dp)) {
                    Text(
                        title,
                        color = UniStackColors.TextPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(subtitle, color = UniStackColors.TextSecondary, fontSize = 12.sp)
                }
            }
        }
        content()
    }
}

/** Novedades: el registro de cambios que viaja dentro de la app, sin pedir red. */
@Composable
fun WhatsNewScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val changelog = remember {
        runCatching {
            context.assets.open("changelog.md").bufferedReader().use { it.readText() }
        }.getOrNull()
    }
    // Del archivo entero se enseña de la primera versión publicada en adelante: la cabecera
    // explica cómo se escribe el archivo, que es cosa de quien lo edita y no de quien lo lee.
    val notes = remember(changelog) {
        changelog?.substringAfter("## [", "")?.let { "## [$it" }?.takeIf { it.length > 4 }
    }

    SupportScaffold(
        title = "Novedades",
        subtitle = "Lo que ha cambiado en cada versión",
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        item {
            UniCard(modifier = Modifier.fillMaxWidth(), shape = AppShapes.LargeCard) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    UniStackWordmark(fontSize = 20.sp)
                    Text(
                        "Versión instalada: ${BuildConfig.VERSION_NAME}",
                        color = UniStackColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
        item {
            UniCard(modifier = Modifier.fillMaxWidth(), shape = AppShapes.LargeCard) {
                if (notes.isNullOrBlank()) {
                    Text(
                        "No se pudo leer el registro de cambios de esta versión.",
                        color = UniStackColors.TextSecondary
                    )
                } else {
                    ReleaseNotes(markdown = notes, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

private data class SupportLink(
    val title: String,
    val subtitle: String,
    val url: String
)

private val ResourceLinks = listOf(
    SupportLink("Google Académico", "Artículos y citas para tus trabajos", "https://scholar.google.com"),
    SupportLink("Khan Academy", "Clases gratis de matemáticas y ciencias", "https://es.khanacademy.org"),
    SupportLink("OpenStax", "Libros de texto universitarios abiertos", "https://openstax.org"),
    SupportLink("Normas APA", "Cómo citar y referenciar", "https://normas-apa.org"),
    SupportLink("Zotero", "Gestor de referencias gratuito", "https://www.zotero.org"),
    SupportLink("Wolfram Alpha", "Resuelve y explica paso a paso", "https://www.wolframalpha.com")
)

/** Recursos: enlaces que se abren en el navegador. Nada se descarga ni se envía. */
@Composable
fun ResourcesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    SupportScaffold(
        title = "Recursos",
        subtitle = "Enlaces útiles para el semestre",
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        items(ResourceLinks) { link ->
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.MediumCard,
                onClick = {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))
                    }
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(AppShapes.Small)
                            .background(UniStackColors.Primary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = null,
                            tint = UniStackColors.Primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(link.title, color = UniStackColors.TextPrimary, fontWeight = FontWeight.Bold)
                        Text(link.subtitle, color = UniStackColors.TextSecondary, fontSize = 12.sp)
                    }
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = UniStackColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private data class FaqEntry(val question: String, val answer: String)

private val Faq = listOf(
    FaqEntry(
        "¿Dónde se guardan mis datos?",
        "En tu teléfono. UniStack funciona sin cuenta y sin conexión; vincular Google solo sirve " +
            "para respaldar y recuperar lo que ya tienes."
    ),
    FaqEntry(
        "¿Por qué mi materia no aparece en el horario?",
        "El horario dibuja las clases que tengan días y hora. Si creaste la materia sin marcar " +
            "días, abre la materia y añádele su horario."
    ),
    FaqEntry(
        "¿Cómo calcula la app mi promedio?",
        "Con lo que ya está evaluado: suma los puntos confirmados de cada corte y los divide " +
            "entre el peso evaluado. No proyecta notas que todavía no existen."
    ),
    FaqEntry(
        "Cambié la escala de notas y perdí mis notas",
        "Cambiar de escala borra las notas registradas, porque un 4,5 sobre 5 no significa lo " +
            "mismo sobre 100. La app avisa dos veces antes de hacerlo."
    ),
    FaqEntry(
        "¿Cómo recibo las versiones de prueba?",
        "En Configuración → Actualizaciones puedes elegir canal. Beta y Alpha piden un código " +
            "que entrega quien publica la app."
    ),
    FaqEntry(
        "Perdí mi teléfono, ¿puedo recuperar todo?",
        "Solo si hiciste una copia. En Configuración → Datos y respaldos puedes exportar un " +
            "archivo y volver a importarlo en otro teléfono."
    )
)

/**
 * Ayuda y soporte, con la sugerencia dentro.
 *
 * El envío no tiene dirección propia: abre el selector del teléfono con el texto escrito, y
 * cada quien lo manda por donde quiera. Poner un correo aquí lo dejaría dentro del APK, a la
 * vista de cualquiera que lo abra.
 */
@Composable
fun HelpScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expanded by rememberSaveable { mutableStateOf<Int?>(null) }
    var suggestion by rememberSaveable { mutableStateOf("") }

    SupportScaffold(
        title = "Ayuda y soporte",
        subtitle = "Preguntas frecuentes y sugerencias",
        onBackClick = onBackClick,
        modifier = modifier.dismissKeyboardOnTapOutside()
    ) {
        Faq.forEachIndexed { index, entry ->
            item {
                val isOpen = expanded == index
                UniCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.MediumCard,
                    onClick = { expanded = if (isOpen) null else index }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                entry.question,
                                modifier = Modifier.weight(1f),
                                color = UniStackColors.TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Icon(
                                if (isOpen) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = UniStackColors.TextSecondary
                            )
                        }
                        AnimatedVisibility(visible = isOpen) {
                            Text(
                                entry.answer,
                                color = UniStackColors.TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
        item {
            UniCard(modifier = Modifier.fillMaxWidth(), shape = AppShapes.LargeCard) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enviar sugerencia",
                        color = UniStackColors.TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "Cuéntanos qué te falta o qué se rompió. Elige después por dónde enviarlo.",
                        color = UniStackColors.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    OutlinedTextField(
                        value = suggestion,
                        onValueChange = { suggestion = it.take(600) },
                        label = { Text("Tu sugerencia") },
                        minLines = 3,
                        shape = AppShapes.MediumCard,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SquishyButton(
                        onClick = {
                            val body = buildString {
                                appendLine(suggestion.trim())
                                appendLine()
                                appendLine("---")
                                appendLine("UniStack ${BuildConfig.VERSION_NAME}")
                                appendLine("Android ${android.os.Build.VERSION.RELEASE} · ${android.os.Build.MODEL}")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Sugerencia para UniStack")
                                putExtra(Intent.EXTRA_TEXT, body)
                            }
                            try {
                                context.startActivity(Intent.createChooser(intent, "Enviar sugerencia"))
                            } catch (_: ActivityNotFoundException) {
                                // Sin nada instalado que sepa enviar texto no hay nada que hacer,
                                // y tampoco hay por qué tirar la pantalla abajo.
                            }
                        },
                        enabled = suggestion.isNotBlank(),
                        shape = AppShapes.Pill,
                        colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Enviar")
                    }
                }
            }
        }
    }
}

/** Acerca de: qué versión llevas, de dónde salió y qué hace con tus datos. */
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    onWhatsNewClick: () -> Unit,
    onUpdatesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SupportScaffold(
        title = "Acerca de",
        subtitle = "Versión, novedades y datos",
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        item {
            UniCard(modifier = Modifier.fillMaxWidth(), shape = AppShapes.LargeCard) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UniStackWordmark(fontSize = 24.sp)
                    Text(
                        "Tu semestre en un solo sitio: notas, horario, tareas y gastos.",
                        color = UniStackColors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    AboutFact("Versión", BuildConfig.VERSION_NAME)
                    AboutFact("Compilación", BuildConfig.VERSION_CODE.toString())
                }
            }
        }
        item {
            AboutRow(
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                title = "Novedades",
                subtitle = "Qué cambió en cada versión",
                onClick = onWhatsNewClick
            )
        }
        item {
            AboutRow(
                icon = Icons.Rounded.Send,
                title = "Actualizaciones",
                subtitle = "Canal, comprobación e instalación",
                onClick = onUpdatesClick
            )
        }
        item {
            UniCard(modifier = Modifier.fillMaxWidth(), shape = AppShapes.LargeCard) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Tus datos", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                    Text(
                        "Todo lo que registras se guarda en este teléfono. La app no manda tus " +
                            "notas, tareas ni gastos a ningún servidor. Si vinculas una cuenta de " +
                            "Google, se usa solo para el respaldo que tú pidas, y puedes " +
                            "desvincularla cuando quieras desde tu perfil.",
                        color = UniStackColors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutFact(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), color = UniStackColors.TextSecondary, fontSize = 13.sp)
        Text(value, color = UniStackColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AboutRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    UniCard(modifier = Modifier.fillMaxWidth(), shape = AppShapes.MediumCard, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(AppShapes.Small)
                    .background(UniStackColors.Primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = UniStackColors.Primary, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = UniStackColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text(subtitle, color = UniStackColors.TextSecondary, fontSize = 12.sp)
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = UniStackColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
