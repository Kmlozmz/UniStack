@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_support.presentation

import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Gavel
import com.unistack.app.core.design.components.UniStackLogoMark
import com.unistack.app.core.design.components.BrandPurple
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.VerifiedUser
import com.unistack.app.feature_support.domain.ChangelogSection
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Surface

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
        subtitle = "Changelog e historial de versiones",
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        item(key = "tuya") {
            /*
             * La versión que tienes, arriba y destacada.
             *
             * Antes era una tarjeta con el logotipo y la línea «Tienes la 1.3.2» en gris de pie
             * de foto, del mismo peso que todo lo demás. Es el único dato de esta pantalla que
             * alguien viene a comprobar, así que se lee sin buscarlo.
             */
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            "TU VERSI\u00d3N",
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            BuildConfig.VERSION_NAME,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.headlineMediumEmphasized,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    sections.firstOrNull()?.date?.let { date ->
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Publicada",
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                date,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        if (sections.isEmpty()) {
            item(key = "vacio") {
                UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
                    Text(
                        "Todav\u00eda no hay nada publicado para esta versi\u00f3n.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            itemsIndexed(sections, key = { _, it -> it.version }) { index, section ->
                ReleaseTimelineEntry(
                    section = section,
                    current = index == 0,
                    last = index == sections.lastIndex
                )
            }
        }
    }
}

/**
 * Una versión en la línea de tiempo.
 *
 * Cada sección era una tarjeta suelta con la fecha en grande, así que dos versiones publicadas
 * el mismo día se leían como dos bloques sin relación. Con el raíl a la izquierda —un punto por
 * versión y la línea que los cose— la lista se recorre como lo que es: hacia atrás en el tiempo.
 *
 * La línea se dibuja con `IntrinsicSize.Min` para que mida lo que mida el texto de al lado; sin
 * eso, `fillMaxHeight` dentro de una fila no tiene contra qué medirse y no pinta nada.
 */
@Composable
private fun ReleaseTimelineEntry(
    section: ChangelogSection,
    current: Boolean,
    last: Boolean
) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Box(
            modifier = Modifier.width(16.dp).fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (!last) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .padding(top = 14.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .size(if (current) 14.dp else 10.dp)
                    .clip(CircleShape)
                    .background(
                        if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (last) 0.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Text(
                    text = section.date ?: "Sin fecha",
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (current) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    contentColor = if (current) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                ) {
                    Text(
                        text = section.version,
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
            ReleaseNotes(markdown = section.body, modifier = Modifier.fillMaxWidth())
        }
    }
}

private data class SupportLink(
    /** Las iniciales del sitio: dos letras como mucho, para que quepan en el cuadro. */
    val mark: String,
    val title: String,
    val domain: String,
    val url: String,
    val tone: ResourceTone
)

/** El color del cuadro de cada sitio, elegido por el grupo al que pertenece. */
private enum class ResourceTone { SEARCH, STUDY, CITE }

private data class ResourceGroup(
    val label: String,
    val help: String,
    val links: List<SupportLink>
)

/*
 * Seis sitios, agrupados por para qué sirven, y cada grupo con su «?».
 *
 * Iban los seis en fila con el mismo icono de libro repetido: una lista donde nada distingue
 * a nada. Y un nombre como «OpenStax» no le dice nada a quien no lo conoce, así que la lista
 * entera se quedaba sin usar por no saber qué hay detrás.
 *
 * La explicación va por grupo y no por sitio: con seis párrafos sería otra pared, y con tres
 * cada uno puede contar para qué sirve el grupo y qué aporta cada sitio dentro de él.
 */
private val ResourceGroups = listOf(
    ResourceGroup(
        label = "BUSCAR",
        help = "Para encontrar de dónde sacar lo que vas a escribir. Google Académico busca " +
                "artículos y tesis revisados por otros investigadores —lo que puedes citar sin que " +
                "te lo tumben— y te da la cita ya formateada. Wolfram Alpha resuelve la operación y " +
                "enseña el procedimiento paso a paso: sirve para comprobar un ejercicio que ya hiciste.",
        links = listOf(
            SupportLink(
                "GA",
                "Google Académico",
                "scholar.google.com",
                "https://scholar.google.com",
                ResourceTone.SEARCH
            ),
            SupportLink("W", "Wolfram Alpha", "wolframalpha.com", "https://www.wolframalpha.com", ResourceTone.SEARCH)
        )
    ),
    ResourceGroup(
        label = "ESTUDIAR",
        help = "Para entender un tema que no te entró en clase. Khan Academy son lecciones " +
                "cortas en vídeo con ejercicios para practicar, sobre todo de matemáticas, física y " +
                "química. OpenStax son libros de texto universitarios completos, gratuitos y legales " +
                "de descargar: los escriben profesores y se usan en universidades de verdad.",
        links = listOf(
            SupportLink("K", "Khan Academy", "es.khanacademy.org", "https://es.khanacademy.org", ResourceTone.STUDY),
            SupportLink("OS", "OpenStax", "openstax.org", "https://openstax.org", ResourceTone.STUDY)
        )
    ),
    ResourceGroup(
        label = "CITAR",
        help = "Para que la bibliografía no te reste puntos. Normas APA explica cómo se cita " +
                "dentro del texto y cómo se arma la lista del final, con ejemplos de cada tipo de " +
                "fuente. Zotero guarda cada fuente mientras investigas y luego te genera la " +
                "bibliografía entera en el formato que te pidan.",
        links = listOf(
            SupportLink("A", "Normas APA", "normas-apa.org", "https://normas-apa.org", ResourceTone.CITE),
            SupportLink("Z", "Zotero", "zotero.org", "https://www.zotero.org", ResourceTone.CITE)
        )
    )
)

/** Recursos: enlaces que se abren en el navegador. Nada se descarga ni se envía. */
@Composable
fun ResourcesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var openHelp by rememberSaveable { mutableStateOf<String?>(null) }

    SupportScaffold(
        title = "Recursos",
        subtitle = "Sitios web útiles",
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        ResourceGroups.forEach { group ->
            item(key = "sect-" + group.label) {
                ResourceSectionHeader(
                    label = group.label,
                    open = openHelp == group.label,
                    onToggle = { openHelp = if (openHelp == group.label) null else group.label }
                )
            }
            item(key = "help-" + group.label) {
                AnimatedVisibility(visible = openHelp == group.label) {
                    UniCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = group.help,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            item(key = "links-" + group.label) {
                UniCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Column {
                        group.links.forEach { link ->
                            ResourceRow(
                                link = link,
                                onClick = {
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * El rótulo del grupo, con el «?» pegado.
 *
 * El signo va aquí y no en la cabecera de la pantalla porque lo que explica es este grupo, y a
 * esta altura está lo que hay que explicar. Se abre en su sitio en vez de en una hoja: es texto
 * corto, y una hoja desde abajo taparía justo la lista sobre la que estás decidiendo.
 */
@Composable
private fun ResourceSectionHeader(
    label: String,
    open: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.padding(start = 2.dp, top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
        Surface(
            onClick = onToggle,
            shape = CircleShape,
            color = if (open) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = if (open) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "?",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun ResourceRow(
    link: SupportLink,
    onClick: () -> Unit
) {
    val tone = when (link.tone) {
        ResourceTone.SEARCH -> LocalSectionColors.current.schedule
        ResourceTone.STUDY -> LocalSectionColors.current.onTrack
        ResourceTone.CITE -> MaterialTheme.colorScheme.primary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // La inicial del sitio en vez del mismo icono de libro seis veces: distingue de un
        // vistazo, y no hay que inventar un icono para cada uno.
        Box(
            Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.small)
                .background(tone.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = link.mark,
                color = tone,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(link.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Text(
                link.domain,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        }
        // «Se va fuera de la app», que es lo que pasa. La flecha de antes decía «entras aquí
        // dentro» y era mentira: los seis abren el navegador.
        Icon(
            Icons.AutoMirrored.Rounded.OpenInNew,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp)
        )
    }
}

private data class FaqEntry(val question: String, val answer: String)

private data class FaqGroup(val label: String, val entries: List<FaqEntry>)

/*
 * Las ocho preguntas, repartidas en tres montones.
 *
 * Sueltas en una columna había que leerlas todas para encontrar la tuya, y la mitad no tenían
 * nada que ver entre sí: dónde viven tus datos y por qué Android pide permiso para instalar son
 * preguntas de dos personas distintas en dos momentos distintos.
 */
private val FaqGroups = listOf(
    FaqGroup(
        "TUS DATOS",
        listOf(
            FaqEntry(
                "¿Dónde se guardan mis datos?",
                "En tu teléfono. UniStack funciona sin cuenta y sin conexión; vincular Google solo " +
                        "sirve para respaldar y recuperar lo que ya tienes."
            ),
            FaqEntry(
                "Perdí mi teléfono, ¿puedo recuperar todo?",
                "Solo si hiciste una copia. En Configuración → Datos y respaldos puedes exportar " +
                        "un archivo y volver a importarlo en otro teléfono."
            )
        )
    ),
    FaqGroup(
        "NOTAS Y HORARIO",
        listOf(
            FaqEntry(
                "¿Cómo calcula la app mi promedio?",
                "Con lo que ya está evaluado: suma los puntos confirmados de cada corte y los " +
                        "divide entre el peso evaluado. No proyecta notas que todavía no existen."
            ),
            FaqEntry(
                "¿Qué son el suelo y el techo de una materia?",
                "El suelo es con cuánto terminarías sacando 0 en todo lo que falta, y el techo con " +
                        "cuánto terminarías sacándolo todo. Tu nota final va a caer entre esos dos, y la " +
                        "meta se dibuja como una marca dentro de esa franja: si queda fuera, ya no se alcanza."
            ),
            FaqEntry(
                "Cambié la escala de notas y perdí mis notas",
                "Cambiar de escala borra las notas registradas, porque un 4,5 sobre 5 no significa " +
                        "lo mismo sobre 100. Convertirlas inventaría un número que ningún profesor puso. " +
                        "La app avisa dos veces y te dice cuántas notas vas a perder."
            ),
            FaqEntry(
                "¿Por qué mi materia no aparece en el horario?",
                "El horario dibuja las clases que tengan días y hora. Si creaste la materia sin " +
                        "marcar días, abre la materia y añádele su horario."
            )
        )
    ),
    FaqGroup(
        "ACTUALIZACIONES",
        listOf(
            FaqEntry(
                "¿Cómo recibo las actualizaciones?",
                "En Configuración → Actualizaciones. La app mira lo último publicado y te lo " +
                        "ofrece; no hay canales ni códigos que pedir. Comprueba sola cada par de horas, " +
                        "así que puede tardar un rato en enterarse: si tienes prisa, entra y pulsa el " +
                        "botón de recargar."
            ),
            FaqEntry(
                "¿Por qué me pide permiso para instalar?",
                "Porque la app no viene de Play Store y se actualiza sola desde su archivo. " +
                        "Android pide autorizar a UniStack como origen una vez; luego ya no vuelve a " +
                        "preguntar."
            )
        )
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
    // La abierta se recuerda por su texto y no por su posición: con las preguntas repartidas
    // en grupos, el índice ya no identifica a ninguna.
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    var composing by rememberSaveable { mutableStateOf(false) }

    SupportScaffold(
        title = "Ayuda y soporte",
        subtitle = "Resuelve las dudas más comunes",
        onBackClick = onBackClick,
        modifier = modifier.dismissKeyboardOnTapOutside()
    ) {
        FaqGroups.forEach { group ->
            item(key = "faq-" + group.label) {
                Text(
                    text = group.label,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 2.dp, top = 6.dp)
                )
            }
            group.entries.forEach { entry ->
                item(key = entry.question) {
                    val isOpen = expanded == entry.question
                    UniCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        onClick = { expanded = if (isOpen) null else entry.question }
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
        }
        item {
            /*
             * Una fila, no dos.
             *
             * Hubo un tiempo en que había dos —«Reportar un fallo» y «Sugerir algo»— y cada una
             * abría su propia hoja. Obligaban a clasificar antes de contar nada, y quien tenía
             * una duda que no era ninguna de las dos no encontraba puerta. El motivo ahora se
             * elige dentro del formulario, donde además se puede cambiar de idea.
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
                        icon = Icons.AutoMirrored.Rounded.Chat,
                        accent = MaterialTheme.colorScheme.primary,
                        title = "Escríbenos",
                        subtitle = "Un fallo, una idea o cualquier otra cosa",
                        onClick = { composing = true }
                    )
                }
            }
        }
    }

    if (composing) {
        val ticketContext = TicketContext(
            appVersion = BuildConfig.VERSION_NAME,
            androidVersion = android.os.Build.VERSION.RELEASE,
            androidSdk = android.os.Build.VERSION.SDK_INT,
            device = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
        )
        FeedbackSheet(
            initialKind = TicketKind.BUG,
            ticketContext = ticketContext,
            onDismiss = { composing = false },
            onSend = { kind, text, contact ->
                val ticket = buildTicket(
                    kind = kind,
                    text = text,
                    contact = contact,
                    context = ticketContext
                )
                scope.launch {
                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("UniStack", ticket)))
                }
                // La hoja se queda abierta enseñando qué pasó; devolverle si Telegram llegó a
                // abrirse es lo que le deja escribir el paso que falta.
                openSupportTopic(context, kind)
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

/** Acerca de: qué versión llevas, de dónde salió y qué hace con tus datos. */
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SupportScaffold(
        title = "Acerca de",
        subtitle = "Versión, datos y condiciones",
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        item(key = "marca") {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                UniStackLogoMark(size = 68.dp)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    UniStackWordmark(fontSize = 26.sp)
                    Text(
                        text = buildAnnotatedString {
                            append("By ")

                            withStyle(
                                // El mismo morado de marca que «Stack» en el nombre de la app,
                                // no un tono parecido: dos violetas distintos en la misma
                                // pantalla se leen como un descuido, no como dos usos.
                                style = SpanStyle(color = BrandPurple)
                            ) {
                                append("Kmlozmz")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AboutChip(BuildConfig.VERSION_NAME, highlighted = true)
            }
        }
        item(key = "datos-titulo") { AboutSectionLabel("TUS DATOS") }
        item(key = "datos-local") {
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = LocalSectionColors.current.onTrackContainer
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                    Icon(
                        Icons.Rounded.VerifiedUser,
                        contentDescription = null,
                        tint = LocalSectionColors.current.onOnTrackContainer,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                                append("Tus datos se guardan localmente. ")
                            }
                            append(
                                "Tu información permanece en este dispositivo y solo se sincroniza " +
                                        "con nuestros servidores cuando una función lo requiere."
                            )
                        },
                        color = LocalSectionColors.current.onOnTrackContainer,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 19.sp
                    )
                }
            }
        }
        item(key = "datos-nube") {
            UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
                Row(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                    Icon(
                        Icons.Rounded.CloudQueue,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                append("Protege tus datos con un respaldo en la nube. ")
                            }
                            append(
                                "Si vinculas Google, podrás proteger y respaldar tu información. " +
                                        "Puedes desvincular tu cuenta cuando quieras."
                            )
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 19.sp
                    )
                }
            }
        }
        item(key = "info-titulo") { AboutSectionLabel("INFORMACIÓN") }
        item(key = "info") {
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Column {
                    // Ni \u00abNovedades\u00bb ni \u00abActualizaciones\u00bb: la primera es una fila del panel
                    // lateral y la segunda tiene su propio apartado en Ajustes. Repetir la puerta
                    // no ahorra un toque, solo obliga a leer dos veces para descubrir que da
                    // igual cu\u00e1l elijas.
                    /*
                     * Los dos documentos, visibles y apagados.
                     *
                     * Todavía no existen: cuando la web esté en pie, estas dos filas la abrirán.
                     * Salen igualmente, con su etiqueta, porque una app que guarda datos sin
                     * decir bajo qué condiciones deja esa pregunta sin sitio donde hacerse.
                     */
                    AboutRow(
                        icon = Icons.Rounded.Gavel,
                        title = "Términos y condiciones",
                        subtitle = "Qué puedes esperar de la app y qué esperamos de ti",
                        onClick = null
                    )
                    AboutRow(
                        icon = Icons.Rounded.PrivacyTip,
                        title = "Política de privacidad",
                        subtitle = "Qué datos se guardan, dónde y para qué",
                        onClick = null
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutSectionLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 2.dp, top = 6.dp)
    )
}

@Composable
private fun AboutChip(text: String, highlighted: Boolean = false) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        contentColor = if (highlighted) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun AboutRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?
) {
    val enabled = onClick != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.small)
                .background(
                    if (enabled) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.outline
                },
                fontWeight = FontWeight.Bold
            )
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                lineHeight = 15.sp
            )
        }
        if (enabled) {
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(
                    "PRONTO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
