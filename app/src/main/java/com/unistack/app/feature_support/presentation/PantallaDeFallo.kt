package com.unistack.app.feature_support.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.core.design.components.bottomActionInsets
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.fallos.InformeDeFallo
import com.unistack.app.core.utils.Textos

/**
 * Lo que ve el usuario cuando la app se cierra sola. Una pantalla, dos puertas.
 *
 * [enElActo] distingue de cuál de las dos viene, y es lo único que cambia: si el fallo se cazó
 * al vuelo, la pantalla habla en presente y abajo ofrece reiniciar; si se recogió al arrancar,
 * habla del cierre anterior y abajo ofrece seguir. El informe, los botones y el archivo son
 * los mismos, porque son el mismo fallo.
 *
 * **Los textos no adivinan qué estaba haciendo el usuario.** La app rara vez lo sabe con
 * certeza —el fallo puede venir de un aviso en segundo plano, de una corrutina o de tres
 * pantallas atrás— y quedar en ridículo inventándolo es peor que no decirlo. Lo concreto lo
 * pone quien lo vivió, en el campo de texto de la hoja de reporte.
 */
@Composable
fun PantallaDeFallo(
    informe: InformeDeFallo,
    enElActo: Boolean,
    /** Qué se ha hecho ya con el archivo, o nulo si todavía nada. */
    estadoDelArchivo: String?,
    onContar: () -> Unit,
    onGuardar: () -> Unit,
    onSalir: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Esta pantalla la abre una Activity suya, sin andamiaje de ninguna clase: si no
            // se aparta ella de las barras del sistema, no lo hace nadie. Con edge-to-edge
            // obligatorio desde targetSdk 36, la cabecera acababa debajo del reloj.
            .statusBarsPadding()
    ) {
        /*
         * El aviso en el centro y las acciones abajo del todo.
         *
         * Con todo apilado desde arriba, los botones caían a media pantalla y dejaban un
         * palmo de vacío debajo: el dedo tenía que subir a buscarlos y la pantalla parecía
         * cortada. Así hay dos zonas con un trabajo cada una — lo que se lee y lo que se
         * toca— y el hueco sobrante se reparte alrededor de lo que se lee, no al final.
         *
         * El centro se desplaza por su cuenta: con la letra grande de Accesibilidad el
         * informe crece y tiene que poder recorrerse sin empujar los botones fuera.
         */
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(19.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CabeceraDelFallo(informe = informe, enElActo = enElActo)
            TarjetaDelInforme(informe = informe, estadoDelArchivo = estadoDelArchivo)
        }

        /*
         * Dos en fila y una debajo.
         *
         * Los dos de arriba son hermanos —las dos formas de quedarse con el archivo— y el de
         * abajo es el contrario: irse sin hacer nada. La fila los agrupa como lo que son.
         *
         * Las etiquetas son cortas a propósito: el botón de la app recorta a una línea con
         * puntos suspensivos, y a media anchura, con el icono delante, «Contar qué pasó» no
         * cabía. Lo largo se dice en la hoja que abre.
         */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .bottomActionInsets()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                UniStackButton(
                    text = Textos.get(R.string.fallo_btn_contar),
                    onClick = onContar,
                    leadingIcon = Icons.AutoMirrored.Rounded.Send,
                    modifier = Modifier.weight(1f)
                )
                UniStackButton(
                    text = Textos.get(R.string.fallo_btn_guardar),
                    onClick = onGuardar,
                    variant = UniStackButtonVariant.Tonal,
                    leadingIcon = Icons.Rounded.SaveAlt,
                    modifier = Modifier.weight(1f)
                )
            }
            UniStackButton(
                text = if (enElActo) {
                    Textos.get(R.string.fallo_btn_reiniciar)
                } else {
                    Textos.get(R.string.fallo_btn_seguir)
                },
                onClick = onSalir,
                variant = UniStackButtonVariant.Outlined
            )
        }
    }
}

/**
 * Las medidas salen del diseño aprobado, **escaladas**, no copiadas.
 *
 * El boceto se dibujó sobre un lienzo de 290 px de ancho y el teléfono ronda los 411 dp, así
 * que un «18 px» de allí son 25 dp aquí. Trasladar los números tal cual —que es lo que se hizo
 * la primera vez— deja la pantalla con todo diminuto y nadando en hueco vacío: se parece al
 * boceto en un pantallazo recortado y no se parece en nada en la mano.
 *
 * El factor es 411/290 ≈ 1,42 y está aplicado a todo lo de esta pantalla: tipos, rellenos,
 * huecos e iconos. Lo único que no se toca son la forma y el alto de las tarjetas y los
 * botones, que salen de Apariencia porque el usuario los elige.
 */
@Composable
private fun CabeceraDelFallo(informe: InformeDeFallo, enElActo: Boolean) {
    UniCard(
        color = MaterialTheme.colorScheme.errorContainer,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(26.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PriorityHigh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(38.dp)
                )
            }
            Text(
                text = if (enElActo) {
                    Textos.get(R.string.fallo_titulo_en_el_acto)
                } else {
                    Textos.get(R.string.fallo_titulo_al_volver)
                },
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 25.5.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = InformeDeFallo.fechaLegible(informe.fecha),
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.75f),
                fontSize = 15.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * El informe en fichas, no en tabla.
 *
 * Cada dato va en su propio bloque, con el rótulo encima y el valor debajo. **Sin líneas
 * divisorias**, que eran lo que hacía que la tarjeta se leyera como una pantalla de ajustes:
 * la separación la dan los bloques, no unas rayas.
 *
 * El error ocupa la fila entera y va primero porque es lo único que cambia de un fallo a otro.
 * Fecha y versión se reparten la siguiente, que son cortos; el teléfono vuelve a ancho completo
 * porque no cabe en media.
 *
 * Las medidas van escaladas desde el boceto — ver [CabeceraDelFallo].
 */
@Composable
private fun TarjetaDelInforme(informe: InformeDeFallo, estadoDelArchivo: String?) {
    var abierto by remember { mutableStateOf(false) }
    val secciones = LocalSectionColors.current
    val fecha = InformeDeFallo.fechaLegible(informe.fecha)

    UniCard(contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Ficha(
                etiqueta = Textos.get(R.string.fallo_dato_error),
                valor = informe.tipo,
                color = MaterialTheme.colorScheme.error,
                tamano = 17.5.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                Ficha(
                    etiqueta = Textos.get(R.string.fallo_dato_fecha),
                    // Partida en dos renglones: a media anchura, el día y la hora en una sola
                    // línea se recortaban.
                    valor = fecha.replace(" ", "\n"),
                    modifier = Modifier.weight(1f)
                )
                Ficha(
                    etiqueta = Textos.get(R.string.fallo_dato_version),
                    valor = informe.version + "\n(" + informe.codigoDeVersion + ")",
                    modifier = Modifier.weight(1f)
                )
            }
            Ficha(
                etiqueta = Textos.get(R.string.fallo_dato_telefono),
                valor = informe.dispositivo + " · " + informe.android,
                modifier = Modifier.fillMaxWidth()
            )

            /*
             * La nota de qué lleva el archivo, como nota al pie y no como franja de color.
             *
             * Era un bloque verde saturado a todo lo ancho, justo debajo de una cabecera roja
             * saturada. Lo que dice no es una alarma, así que no tiene por qué vestirse de
             * una. El escudo se queda, en su color, del tamaño de lo que es.
             */
            Row(
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Rounded.VerifiedUser,
                    contentDescription = null,
                    tint = secciones.onTrack,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(19.dp)
                )
                Text(
                    text = Textos.get(R.string.fallo_no_se_envia_solo),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.5.sp,
                    lineHeight = 20.sp
                )
            }

            /*
             * El nombre del archivo es lo que despliega: es justo lo que uno toca cuando
             * quiere ver qué se va a mandar.
             *
             * En reposo va **solo**, una línea y su flecha, como en el diseño. El segundo
             * renglón aparece únicamente cuando ya se ha hecho algo con el archivo —enviado o
             * guardado— porque hasta entonces no hay nada que contar: que está listo ya se
             * entiende por los dos botones de abajo.
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .cleanClickable(shape = RoundedCornerShape(12.dp)) { abierto = !abierto },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nombreDelArchivo(informe),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 21.sp
                    )
                    if (estadoDelArchivo != null) {
                        Text(
                            text = estadoDelArchivo,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
                Icon(
                    imageVector = if (abierto) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(25.dp)
                )
            }

            AnimatedVisibility(visible = abierto) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(17.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(15.dp)
                ) {
                    Text(
                        text = resumenDeLoQuitado(informe),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = informe.comoTexto(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}

/** Un dato en su bloque: el rótulo en versales encima, el valor en monoespaciado debajo. */
@Composable
private fun Ficha(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    tamano: androidx.compose.ui.unit.TextUnit = 15.sp
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Text(
            text = etiqueta.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            letterSpacing = 0.1.em,
            lineHeight = 16.sp
        )
        Text(
            text = valor,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = tamano,
            lineHeight = 21.sp,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

/**
 * Qué se quitó del informe, dicho con los números de verdad.
 *
 * Nada de «se protegieron tus datos» en abstracto: o se quitó algo y se dice cuánto, o no
 * había nada que quitar y también se dice. Es la única forma de que la frase signifique algo
 * la segunda vez que se lee.
 */
private fun resumenDeLoQuitado(informe: InformeDeFallo): String {
    if (!informe.huboRecorte) return Textos.get(R.string.fallo_limpio_nada)
    val piezas = buildList {
        when {
            informe.correosQuitados == 1 -> add(Textos.get(R.string.fallo_limpio_correo_uno))
            informe.correosQuitados > 1 ->
                add(Textos.get(R.string.fallo_limpio_correo_varios, informe.correosQuitados))
        }
        when {
            informe.textosQuitados == 1 -> add(Textos.get(R.string.fallo_limpio_texto_uno))
            informe.textosQuitados > 1 ->
                add(Textos.get(R.string.fallo_limpio_texto_varios, informe.textosQuitados))
        }
    }
    val total = informe.correosQuitados + informe.textosQuitados
    val lista = piezas.joinToString(" · ")
    return if (total == 1) {
        Textos.get(R.string.fallo_limpio_uno, lista)
    } else {
        Textos.get(R.string.fallo_limpio_varios, total, lista)
    }
}

/** `unistack-fallo-2026-09-15.txt`: la fecha va delante para que ordene sola. */
fun nombreDelArchivo(informe: InformeDeFallo): String {
    val fecha = InformeDeFallo.fechaLegible(informe.fecha)
        .take(10)
        .split("/")
        .reversed()
        .joinToString("-")
    return "unistack-fallo-$fecha.txt"
}
