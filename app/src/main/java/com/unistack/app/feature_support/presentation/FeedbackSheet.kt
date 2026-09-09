@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_support.presentation

import androidx.compose.runtime.remember
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.ui.unit.IntSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.Spring
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.core.utils.performSafely
import com.unistack.app.feature_support.domain.SupportChannel
import com.unistack.app.feature_support.domain.TicketContext
import com.unistack.app.feature_support.domain.TicketKind
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

/** Lo mínimo que hay que escribir para que el mensaje diga algo. */
private const val MinimumLength = 15

/**
 * El formulario de contacto: uno solo para fallos, ideas y lo demás.
 *
 * Antes había dos puertas en Ayuda —«Reportar un fallo» y «Sugerir algo»— y cada una abría su
 * propia hoja. Obligaban a clasificar antes de contar, y no dejaban sitio a lo que no era
 * ninguna de las dos. Ahora el motivo es un selector dentro del formulario: se puede cambiar
 * después de escribir, y el texto ya redactado no se pierde al cambiarlo.
 *
 * Va en una hoja inferior y no en un diálogo: el teclado la empuja hacia arriba en vez de
 * taparla, y deja sitio para el texto largo que hace falta al describir un fallo. Se abre
 * entera —`skipPartiallyExpanded`— porque a media altura el formulario salía cortado por el
 * primer campo y parecía que no hubiera más.
 *
 * [onSend] devuelve si Telegram llegó a abrirse, y con eso la hoja se queda enseñando qué ha
 * pasado en lugar de cerrarse. Cerrarse sin más era lo que hacía dudar de si el mensaje había
 * salido; y como Telegram no deja que una app rellene el mensaje de un grupo, el último paso
 * —pegar— lo da quien escribe, así que hay que decírselo justo cuando toca hacerlo.
 */
@Composable
internal fun FeedbackSheet(
    initialKind: TicketKind,
    ticketContext: TicketContext,
    onDismiss: () -> Unit,
    onSend: (kind: TicketKind, text: String, contact: String?) -> Boolean
) {
    // El motivo se recuerda, pero el texto **no** se ata a él: cambiar de Fallo a Sugerencia a
    // media redacción es justo el caso que este formulario existe para permitir.
    var kind by rememberSaveable { mutableStateOf(initialKind) }
    var text by rememberSaveable { mutableStateOf("") }
    var contact by rememberSaveable { mutableStateOf("") }
    /*
     * Tres pasos, no dos: escribir, leer como va a salir, y mandarlo.
     *
     * Antes «Enviar» copiaba el mensaje y abría Telegram de golpe, y solo después aparecia un
     * cartel explicando que faltaba pegarlo. Quien pulsaba ya estaba en otra app cuando se le
     * decía lo que tenía que hacer allí, así que volvía, leía, y volvia a salir.
     *
     * Ahora el paso de en medio se lee antes de salir de UniStack: dice qué va a pasar y el
     * botón lleva el nombre de lo que hace.
     */
    var instructing by rememberSaveable { mutableStateOf(false) }
    var opened by rememberSaveable { mutableStateOf<Boolean?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        val opening = opened
        if (opening == null && !instructing) {
            FeedbackForm(
                kind = kind,
                onKindChange = { kind = it },
                text = text,
                onTextChange = { text = it },
                contact = contact,
                onContactChange = { contact = it },
                ticketContext = ticketContext,
                onDismiss = onDismiss,
                onSend = { instructing = true }
            )
        } else if (opening == null) {
            BeforeSending(
                onSend = {
                    opened = onSend(kind, text, contact.trim().takeIf { it.isNotEmpty() })
                }
            )
        } else {
            Copied(telegramOpened = opening, onClose = onDismiss)
        }
    }
}

@Composable
private fun FeedbackForm(
    kind: TicketKind,
    onKindChange: (TicketKind) -> Unit,
    text: String,
    onTextChange: (String) -> Unit,
    contact: String,
    onContactChange: (String) -> Unit,
    ticketContext: TicketContext,
    onDismiss: () -> Unit,
    onSend: () -> Unit
) {
    val enoughWritten = text.trim().length >= MinimumLength

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
                text = stringResource(R.string.support_feedback_title),
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            UniIconButton(
                icon = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.support_feedback_close),
                onClick = onDismiss
            )
        }

        Text(
            text = stringResource(R.string.support_feedback_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        KindChooser(selected = kind, onSelected = onKindChange)

        OutlinedTextField(
            value = text,
            onValueChange = { onTextChange(it.take(1500)) },
            label = { Text(stringResource(R.string.support_feedback_field_message)) },
            placeholder = { Text(stringResource(R.string.support_feedback_field_message_hint)) },
            minLines = 4,
            maxLines = 8,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(
                value = contact,
                onValueChange = { onContactChange(it.take(120)) },
                label = { Text(stringResource(R.string.support_feedback_field_contact)) },
                placeholder = { Text(stringResource(R.string.support_feedback_field_contact_hint)) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(R.string.support_feedback_field_contact_helper),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        WhatWillBeSent(ticketContext = ticketContext)

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
                text = stringResource(R.string.support_feedback_public_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Dice «Enviar» y no «Enviar por Telegram»: este botón no abre Telegram todavía,
        // lleva al paso donde se explica lo que va a pasar. Prometer el destino aquí y llevar
        // a otro sitio es justo lo que hacía dudar de si el mensaje había salido.
        UniStackButton(
            text = stringResource(R.string.support_feedback_btn_send),
            onClick = onSend,
            enabled = enoughWritten
        )
    }
}

/**
 * Lo que va a pasar al pulsar, antes de que pase.
 *
 * Telegram no deja que otra app escriba en un grupo por ti: lo máximo que se puede hacer es
 * dejar el mensaje copiado y abrir el tema. El último paso —pegar y darle a enviar— lo da quien
 * escribe, y esto lo dice mientras todavía está mirando UniStack.
 *
 * **Sin botón de volver.** Este paso no pregunta nada: cuenta cómo va a salir el mensaje. Si
 * hay que cambiar algo se cierra la hoja, que es lo que hace la cruz de arriba y el gesto de
 * bajarla; poner un «Volver» al lado del que envía hace dudar de cuál es el que sigue.
 */
@Composable
private fun BeforeSending(onSend: () -> Unit) {
    val entered = remember { MutableTransitionState(false).apply { targetState = true } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visibleState = entered,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = 0.45f,
                    stiffness = Spring.StiffnessMediumLow
                ),
                initialScale = 0.6f
            ) + fadeIn()
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        Text(
            text = stringResource(R.string.support_feedback_how_title),
            style = MaterialTheme.typography.headlineSmallEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.support_feedback_how_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SendingStep(number = "1", text = stringResource(R.string.support_feedback_step1))
                SendingStep(number = "2", text = stringResource(R.string.support_feedback_step2))
            }
        }

        UniStackButton(
            text = stringResource(R.string.support_feedback_btn_telegram),
            onClick = onSend,
            leadingIcon = Icons.AutoMirrored.Rounded.Send
        )
    }
}

/** Un paso numerado de las instrucciones. */
@Composable
private fun SendingStep(number: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * El selector de motivo.
 *
 * Está escrito a mano y no con el segmentado de Material por una razón concreta: aquel sube al
 * frente el segmento que estás pulsando, y como los segmentos de en medio son rectángulos, al
 * mantener pulsado aparecía un contorno cuadrado encima de un control redondeado. Aquí el
 * recorte lo pone la fila entera, así que nada puede dibujarse fuera de ella.
 *
 * Lo que sí se conserva es el gesto que lo hace legible: al elegir uno, el check **entra por la
 * izquierda** empujando el rótulo, que se recoloca en el centro. Es lo que distingue marcar una
 * casilla de cambiar de pestaña —para eso está `UniSegmentedControl`, que va relleno—.
 */
@Composable
private fun KindChooser(
    selected: TicketKind,
    onSelected: (TicketKind) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val shape = RoundedCornerShape(percent = 50)
    val line = MaterialTheme.colorScheme.outline

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(shape)
            .border(width = 1.dp, color = line, shape = shape)
    ) {
        TicketKind.entries.forEachIndexed { index, kind ->
            if (index > 0) {
                VerticalDivider(color = line, thickness = 1.dp)
            }
            val chosen = kind == selected
            val fill by animateColorAsState(
                targetValue = if (chosen) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    Color.Transparent
                },
                animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                label = "fondo del motivo"
            )
            val ink by animateColorAsState(
                targetValue = if (chosen) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                label = "tinta del motivo"
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(fill)
                    .selectable(
                        selected = chosen,
                        role = Role.RadioButton,
                        onClick = {
                            // Un toque seco al cambiar, y solo al cambiar: repetirlo sobre el
                            // que ya está elegido convierte el aviso en ruido.
                            if (!chosen) haptics.performSafely(HapticFeedbackType.SegmentTick)
                            onSelected(kind)
                        }
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = chosen,
                    enter = expandHorizontally(
                        // Un muelle con rebote de verdad, no el del tema.
                        //
                        // `defaultSpatialSpec` está amortiguado casi al máximo: entra recto y
                        // no se nota que sea un muelle. Bajando la amortiguación, el rótulo se
                        // pasa un pelo de su sitio y vuelve, que es el gesto que se pedía.
                        animationSpec = spring(
                            dampingRatio = 0.45f,
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = IntSize.VisibilityThreshold
                        ),
                        expandFrom = Alignment.Start
                    ) + fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
                    exit = shrinkHorizontally(
                        animationSpec = spring(
                            dampingRatio = 0.45f,
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = IntSize.VisibilityThreshold
                        ),
                        shrinkTowards = Alignment.Start
                    ) + fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec())
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = ink,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                }
                // Sin ajuste de línea: mientras el check entra, el rótulo dispone de menos
                // ancho del que mide y `Text` lo partiría en dos a mitad de la animación.
                Text(
                    text = kind.label,
                    color = ink,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

/**
 * Lo que se va a mandar, escrito antes de mandarlo.
 *
 * La app añade sola la versión y el teléfono, y decirlo en una línea pequeña no es lo mismo que
 * enseñarlo: aquí se ve el dato exacto, tal cual va a viajar. Empieza plegado porque con el
 * teclado abierto la hoja ya va justa, y quien quiera comprobarlo lo abre.
 *
 * El contacto no está en la lista aunque también viaje: lo acaba de escribir quien lo escribe,
 * en el campo de arriba, y repetírselo dos centímetros más abajo no le enseña nada.
 */
@Composable
private fun WhatWillBeSent(ticketContext: TicketContext) {
    var open by rememberSaveable { mutableStateOf(false) }
    val turn by animateFloatAsState(if (open) 180f else 0f, label = "flecha")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable { open = !open }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.support_feedback_send_info_title),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Rounded.ExpandMore,
                contentDescription = if (open) stringResource(R.string.support_feedback_btn_hide) else stringResource(R.string.support_feedback_btn_view),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(turn)
            )
        }
        AnimatedVisibility(visible = open) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SentRow(stringResource(R.string.support_feedback_device), ticketContext.device)
                SentRow("Android", "${ticketContext.androidVersion} (SDK ${ticketContext.androidSdk})")
                SentRow(stringResource(R.string.support_feedback_app_version), ticketContext.appVersion)
            }
        }
    }
}

@Composable
private fun SentRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Lo que pasa después de pulsar enviar.
 *
 * No dice «enviado», porque no lo está: Telegram no deja que una app rellene el mensaje de un
 * grupo, así que la app copia el texto y abre el tema, y el envío lo hace quien escribe. Decir
 * aquí que ya está mandado sería mentir justo en el momento en que todavía falta un paso.
 */
@Composable
private fun Copied(telegramOpened: Boolean, onClose: () -> Unit) {
    /*
     * El acuse de recibo, con el círculo grande y el visto dentro.
     *
     * El círculo entra con un muelle: aparecer de golpe no se lee como «ha pasado algo», se lee
     * como que la pantalla ha cambiado sin más.
     *
     * El titular no dice «enviado» cuando Telegram se abrió, dice que falta un paso. Telegram no
     * deja que una app escriba en un grupo por ti, así que el mensaje está copiado y esperando a
     * que lo pegues. Decir «enviado» aquí sería mentir, y la gente cerraría la hoja creyendo que
     * ya está.
     */
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visibleState = visible,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = 0.45f,
                    stiffness = Spring.StiffnessMediumLow
                ),
                initialScale = 0.6f
            ) + fadeIn()
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        if (telegramOpened) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (telegramOpened) Icons.Rounded.Check else Icons.Rounded.ContentPaste,
                    contentDescription = null,
                    tint = if (telegramOpened) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Text(
            text = if (telegramOpened) stringResource(R.string.support_feedback_copied_and_opened) else stringResource(R.string.support_feedback_telegram_failed),
            style = MaterialTheme.typography.headlineSmallEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = if (telegramOpened) {
                stringResource(R.string.support_feedback_sent_success)
            } else {
                stringResource(R.string.support_feedback_sent_fallback, SupportChannel.HANDLE)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        UniStackButton(
            text = stringResource(R.string.support_feedback_close),
            onClick = onClose
        )
    }
}
