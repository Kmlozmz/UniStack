package com.unistack.app.feature_support.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.core.fallos.AlmacenDeFallos
import com.unistack.app.core.fallos.InformeDeFallo
import com.unistack.app.core.utils.Textos
import com.unistack.app.feature_profile.presentation.BackupFiles

/**
 * La pantalla de fallo con sus tres salidas conectadas.
 *
 * Existe aparte de [PantallaDeFallo] para que esa sea sólo pintura: así la misma pantalla la
 * puede enseñar la actividad que salta al vuelo y la puerta del arranque, y el trasiego de
 * intents vive en un sitio.
 *
 * **Verlo ya cierra el asunto**: el pendiente se borra al entrar, no al elegir. Un fallo que
 * el usuario ya vio no puede volver a salirle en el siguiente arranque, salga por donde salga
 * de la pantalla. Guardar es, además, la respuesta a que Telegram no deje escribir en un
 * grupo por ti: en cuanto el `.txt` existe en el teléfono, da lo mismo por dónde acabe yendo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlujoDeFallo(
    informe: InformeDeFallo,
    enElActo: Boolean,
    onTerminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    var contando by remember { mutableStateOf(false) }
    var relato by remember { mutableStateOf("") }

    /*
     * El pendiente se borra **al enseñarlo**, no al pulsar un botón.
     *
     * Estaba atado a las tres salidas, y eso dejaba una puerta abierta: salir con el gesto
     * de atrás, o barriendo la app de recientes, no pasaba por ninguna de las tres. El
     * informe se quedaba en disco y la pantalla volvía a salir en cada arranque hasta que
     * dabas con el botón correcto. Un aviso que no se va cuando lo cierras deja de ser un
     * aviso y pasa a ser un peaje.
     *
     * Lo que se manda o se guarda no depende del archivo: [informe] ya está en memoria.
     */
    LaunchedEffect(Unit) { AlmacenDeFallos.limpiar(contexto) }

    fun cerrar() = onTerminar()

    /*
     * Qué se ha hecho ya con el archivo, para decirlo en la propia tarjeta.
     *
     * Sin esto, enviar y guardar no dejaban ninguna señal: el selector se cerraba y la
     * pantalla quedaba exactamente igual que antes de tocarlo, sin forma de saber si había
     * pasado algo. Aquí no hay Scaffold donde colgar un aviso, así que lo dice la línea que
     * ya estaba debajo del nombre del archivo.
     */
    var hecho by remember { mutableStateOf<Int?>(null) }

    val guardarArchivo = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        BackupFiles.writeText(contexto, uri, conElRelato(informe, relato))
            .onSuccess { hecho = R.string.fallo_archivo_guardado }
    }

    PantallaDeFallo(
        informe = informe,
        enElActo = enElActo,
        estadoDelArchivo = hecho?.let { Textos.get(it) },
        onContar = { contando = true },
        onGuardar = { guardarArchivo.launch(nombreDelArchivo(informe)) },
        onSalir = { cerrar() },
        modifier = modifier
    )

    if (contando) {
        ModalBottomSheet(
            onDismissRequest = { contando = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = Textos.get(R.string.fallo_hoja_titulo),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = Textos.get(R.string.fallo_hoja_cuerpo),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                OutlinedTextField(
                    value = relato,
                    onValueChange = { relato = it },
                    label = { Text(Textos.get(R.string.fallo_hoja_campo)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 110.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    UniStackButton(
                        text = Textos.get(R.string.common_cancel),
                        onClick = { contando = false },
                        variant = UniStackButtonVariant.Tonal,
                        modifier = Modifier.weight(1f)
                    )
                    UniStackButton(
                        text = Textos.get(R.string.fallo_hoja_enviar),
                        onClick = {
                            /*
                             * Aquí **no** se cierra la pantalla.
                             *
                             * Antes sí, y por eso enviar «no hacía nada»: el selector del
                             * sistema se abría y en el mismo gesto se arrancaba MainActivity
                             * con la tarea limpia, que se llevaba por delante el selector
                             * recién abierto. Lo que se veía era la app reiniciándose sola.
                             *
                             * Compartir es un viaje de ida y vuelta: se va al selector y se
                             * vuelve aquí. Salir de esta pantalla lo decide el usuario con el
                             * botón de abajo, como con cualquier otra cosa que hace.
                             */
                            contando = false
                            BackupFiles.shareText(
                                context = contexto,
                                fileName = nombreDelArchivo(informe),
                                mimeType = "text/plain",
                                text = conElRelato(informe, relato)
                            ).onSuccess { hecho = R.string.fallo_archivo_enviado }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Lo que escribió el usuario va **arriba del todo**, antes que la traza.
 *
 * Es el dato que de verdad sirve y el único que no se puede deducir del resto: la pila dice
 * dónde reventó, pero no qué estaba intentando hacer alguien cuando reventó. Si va al final,
 * queda detrás de cuarenta líneas de marcos y no lo lee nadie.
 */
private fun conElRelato(informe: InformeDeFallo, relato: String): String {
    val limpio = relato.trim()
    if (limpio.isEmpty()) return informe.comoTexto()
    return buildString {
        appendLine(Textos.get(R.string.fallo_archivo_relato))
        appendLine(limpio)
        appendLine()
        append(informe.comoTexto())
    }
}
