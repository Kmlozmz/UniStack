@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_support.presentation

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.BuildConfig
import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniStackWordmark
import com.unistack.app.core.design.components.dismissKeyboardOnTapOutside
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_support.domain.changelogFor
import com.unistack.app.feature_support.domain.SupportChannel
import com.unistack.app.feature_support.domain.TicketContext
import com.unistack.app.feature_support.domain.TicketKind
import com.unistack.app.feature_support.domain.buildTicket
import com.unistack.app.feature_updates.presentation.ReleaseNotes
import kotlinx.coroutines.launch

import com.unistack.app.core.design.theme.LocalSectionColors
import androidx.compose.material3.Button
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.foundation.layout.heightIn
import com.unistack.app.core.design.components.UniStackButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
/**
 * Las pantallas que el panel lateral prometía y no existían.
 *
 * «Novedades», «Recursos», «Ayuda y soporte» y «Acerca de» eran filas que cerraban el panel sin
 * llevar a ninguna parte. Ninguna necesita servidor ni cuenta: el registro de cambios ya está
 * escrito, las preguntas frecuentes también, y una sugerencia se manda por donde el propio
 * teléfono sepa mandarla.
 */

@Composable
internal fun SupportScaffold(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    val spacing = LocalInterfaceSpacing.current

    LargeTitleScaffold(
        title = title,
        subtitle = subtitle,
        onBackClick = onBackClick,
        modifier = modifier.imePadding(),
        horizontalPadding = spacing.screenHorizontal,
        bottomPadding = scrollBottomRoom,
        content = content
    )
}

/**
 * Novedades: lo que trae la versión que tienes puesta.
 *
 * Solo versiones publicadas, y solo las de tu canal: quien va por betas nunca instaló las
 * alphas de en medio, y a quien tiene la definitiva le da igual qué se arregló en una beta que
 * no tuvo. El reparto vive en [changelogFor], con sus pruebas.
 */
@Composable
fun WhatsNewScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sections = remember {
        val markdown = runCatching {
            context.assets.open("changelog.md").bufferedReader().use { it.readText() }
        }.getOrNull().orEmpty()
        changelogFor(markdown, BuildConfig.VERSION_NAME)
    }

    SupportScaffold(
        title = "Novedades",
        subtitle = "Lo que trae tu versión",
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        item {
            UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    UniStackWordmark(fontSize = 20.sp)
                    Text(
                        "Tienes la ${BuildConfig.VERSION_NAME}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
        if (sections.isEmpty()) {
            item {
                UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
                    Text(
                        "Todavía no hay nada publicado para esta versión.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(sections, key = { it.version }) { section ->
                /*
                 * La fecha manda y la versión va colgada de ella.
                 *
                 * Antes cada sección era una tarjeta con la versión en grande y la fecha en
                 * pequeño al lado, así que dos versiones publicadas el mismo día se leían como
                 * dos bloques sin relación. Puesta la fecha delante, la lista se recorre como
                 * lo que es: una línea de tiempo hacia atrás.
                 */
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = section.date ?: "Sin fecha",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(
                                text = section.version,
                                style = MaterialTheme.typography.labelLargeEmphasized,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                    ReleaseNotes(markdown = section.body, modifier = Modifier.fillMaxWidth())
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                shape = MaterialTheme.shapes.large,
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
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(link.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text(link.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
        "¿Qué son el suelo y el techo de una materia?",
        "El suelo es con cuánto terminarías sacando 0 en todo lo que falta, y el techo con " +
            "cuánto terminarías sacándolo todo. Tu nota final va a caer entre esos dos, y la " +
            "meta se dibuja como una marca dentro de esa franja: si queda fuera, ya no se alcanza."
    ),
    FaqEntry(
        "Cambié la escala de notas y perdí mis notas",
        "Cambiar de escala borra las notas registradas, porque un 4,5 sobre 5 no significa lo " +
            "mismo sobre 100. Convertirlas inventaría un número que ningún profesor puso. La app " +
            "avisa dos veces y te dice cuántas notas vas a perder."
    ),
    FaqEntry(
        "¿Cómo recibo las actualizaciones?",
        "En Configuración → Actualizaciones. La app mira lo último publicado y te lo ofrece; no " +
            "hay canales ni códigos que pedir. Comprueba sola cada par de horas, así que puede " +
            "tardar un rato en enterarse: si tienes prisa, entra y pulsa el botón de recargar."
    ),
    FaqEntry(
        "¿Por qué me pide permiso para instalar?",
        "Porque la app no viene de Play Store y se actualiza sola desde su archivo. Android pide " +
            "autorizar a UniStack como origen una vez; luego ya no vuelve a preguntar."
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
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var expanded by rememberSaveable { mutableStateOf<Int?>(null) }
    var composing by rememberSaveable { mutableStateOf<TicketKind?>(null) }
    var opened by rememberSaveable { mutableStateOf<Boolean?>(null) }

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
                    shape = MaterialTheme.shapes.large,
                    onClick = { expanded = if (isOpen) null else index }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                entry.question,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Icon(
                                if (isOpen) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AnimatedVisibility(visible = isOpen) {
                            Text(
                                entry.answer,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
        item {
            /*
             * Dos filas, no dos botones.
             *
             * Iban como un botón relleno y otro vacío, y eso en una app se lee como «esta es la
             * opción elegida»: parecía un selector con una respuesta ya marcada, no dos caminos
             * que llevan a sitios distintos. Con icono, descripción y flecha, cada uno dice lo
             * que hace y ninguno pesa más que el otro.
             */
            UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "¿No está aquí lo tuyo?",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "Escríbelo y se abre Telegram en el tema que corresponda, con tu versión " +
                            "y tu teléfono ya apuntados.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    SupportOptionRow(
                        icon = Icons.Rounded.BugReport,
                        accent = MaterialTheme.colorScheme.error,
                        title = "Reportar un fallo",
                        subtitle = "Algo no funciona como debería",
                        onClick = { composing = TicketKind.BUG }
                    )
                    SupportOptionRow(
                        icon = Icons.Rounded.Lightbulb,
                        accent = LocalSectionColors.current.atRisk,
                        title = "Sugerir algo",
                        subtitle = "Algo que te falta o mejorarías",
                        onClick = { composing = TicketKind.IDEA }
                    )
                }
            }
        }
    }

    opened?.let { wasOpened ->
        AlertDialog(
            onDismissRequest = { opened = null },
            title = { Text(if (wasOpened) "Ya está copiado" else "No se pudo abrir Telegram") },
            text = {
                Text(
                    if (wasOpened) {
                        "Pega el mensaje en el tema que se abrió y envíalo."
                    } else {
                        // El ticket ya está en el portapapeles, así que el trabajo no se
                        // pierde aunque no haya podido abrirse nada.
                        "El mensaje quedó copiado. Busca el grupo @${SupportChannel.HANDLE} en " +
                            "Telegram y pégalo ahí."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { opened = null }) { Text("Entendido") }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }

    composing?.let { kind ->
        TicketComposer(
            kind = kind,
            onDismiss = { composing = null },
            onSend = { text ->
                val ticket = buildTicket(
                    kind = kind,
                    text = text,
                    context = TicketContext(
                        appVersion = BuildConfig.VERSION_NAME,
                        androidVersion = android.os.Build.VERSION.RELEASE,
                        device = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
                    )
                )
                scope.launch {
                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("UniStack", ticket)))
                }
                opened = openSupportTopic(context, kind)
                composing = null
            }
        )
    }
}

@Composable
private fun SupportOptionRow(
    icon: ImageVector,
    accent: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(MaterialTheme.shapes.small)
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Abre el tema del grupo, primero por la app y luego por la web.
 *
 * Devuelve si algo llegó a abrirse, para poder decirlo en pantalla: antes, cuando el enlace
 * fallaba, la app se quedaba callada y el ticket parecía enviado.
 */
private fun openSupportTopic(context: android.content.Context, kind: TicketKind): Boolean {
    val intents = listOf(
        Intent(Intent.ACTION_VIEW, Uri.parse(SupportChannel.appUriFor(kind))),
        Intent(Intent.ACTION_VIEW, Uri.parse(SupportChannel.webUrlFor(kind)))
    )
    intents.forEach { intent ->
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (runCatching { context.startActivity(intent) }.isSuccess) return true
    }
    return false
}

/**
 * La caja de escribir el ticket.
 *
 * Va en una hoja inferior y no en un diálogo: el teclado la empuja hacia arriba en vez de
 * taparla, y deja sitio para el texto largo que hace falta al describir un fallo.
 *
 * Dos salidas al pie. **El correo se ve pero está apagado**, con la opacidad de un control
 * deshabilitado: existe como destino previsto y todavía no está montado, y esconderlo hasta
 * entonces haría pensar que Telegram es la única vía que va a haber nunca.
 *
 * Telegram no deja rellenar el mensaje de un grupo desde un enlace —solo funciona con bots—,
 * así que el último paso lo da quien reporta: pegar. Se dice antes de pulsar, para que no
 * parezca que la app se quedó a medias.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TicketComposer(
    kind: TicketKind,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var text by rememberSaveable(kind) { mutableStateOf("") }
    val minimumLength = 15
    val enoughWritten = text.trim().length >= minimumLength

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Con el teclado abierto la hoja no cabía y había que desplazarla a mano para
                // llegar a los botones. imePadding la levanta, y el scroll cubre las pantallas
                // bajas o el texto en grande.
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (kind == TicketKind.BUG) "Reportar un fallo" else "Sugerir algo",
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Cerrar")
                }
            }

            Text(
                text = if (kind == TicketKind.BUG) {
                    "Cuenta qué hacías, qué esperabas y qué pasó. Puedes escribirnos por Telegram."
                } else {
                    "Cuenta qué te falta y para qué lo usarías. Puedes escribirnos por Telegram."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(1500) },
                label = { Text(if (kind == TicketKind.BUG) "¿Qué falló?" else "¿Qué te falta?") },
                placeholder = { Text("Describe el problema o tu idea…") },
                minLines = 4,
                maxLines = 8,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Tu versión y tu teléfono se añaden solos. El grupo es público, así que " +
                        "no escribas nada que no quieras que se lea.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    shapes = UniStackButtonDefaults.shapes,
                    onClick = { onSend(text) },
                    enabled = enoughWritten,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = UniStackButtonDefaults.PrimaryHeight)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Telegram")
                }
                Button(
                    shapes = UniStackButtonDefaults.shapes,
                    onClick = {},
                    enabled = false,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = UniStackButtonDefaults.PrimaryHeight)
                ) {
                    Icon(Icons.Rounded.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Correo")
                }
            }

            Text(
                text = "El correo todavía no está disponible.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UniStackWordmark(fontSize = 24.sp)
                    Text(
                        "Tu semestre en un solo sitio: notas, horario, tareas y gastos.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Tus datos", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                    Text(
                        "Todo lo que registras se guarda en este teléfono. La app no manda tus " +
                            "notas, tareas ni gastos a ningún servidor. Si vinculas una cuenta de " +
                            "Google, se usa solo para el respaldo que tú pidas, y puedes " +
                            "desvincularla cuando quieras desde tu perfil.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AboutRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
