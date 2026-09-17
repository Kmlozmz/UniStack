package com.unistack.app.feature_terms.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.R
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.core.design.components.UniSwitch
import com.unistack.app.core.utils.Textos
import com.unistack.app.feature_user.domain.GradingScale

/**
 * Compartir el boletín de un periodo: la imagen tal como sale, con lo que lleva a elegir.
 *
 * Guardar la deja en Galería y compartir abre la hoja del sistema. Las dos salen de la misma
 * imagen, dibujada aparte a 1080 de ancho.
 */
@Composable
fun TermBulletinScreen(
    termId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TermsViewModel = hiltViewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val historial = estado.historial
    val periodo = historial?.periodo(termId)
    val escala = estado.profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val contexto = LocalContext.current
    var conCortes by rememberSaveable { mutableStateOf(true) }
    var conAsistencia by rememberSaveable { mutableStateOf(true) }
    val opciones = OpcionesDelBoletin(conCortes, conAsistencia)

    Box(modifier = modifier.fillMaxSize().background(Paleta.fondo)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 132.dp)
        ) {
            CabeceraDelHistorico(onBack = onBackClick)
            if (historial == null || periodo == null) return@Column
            TituloDelHistorico(
                titulo = stringResource(R.string.hist_compartir_boletin),
                subtitulo = stringResource(R.string.hist_boletin_subtitulo, periodo.nombre)
            )
            Pila {
                PapelDelBoletin(periodo, opciones, escala, historial.notaMaxima, historial.aprobado)
                Rotulo(stringResource(R.string.hist_que_lleva))
                Tarjeta(relleno = PaddingValues(horizontal = 18.dp, vertical = 6.dp)) {
                    Fila(modifier = Modifier.padding(vertical = 12.dp)) {
                        Columna(
                            titulo = stringResource(R.string.hist_boletin_los_cortes, periodo.esquema.cuts.size),
                            sub = stringResource(R.string.hist_boletin_los_cortes_sub)
                        )
                        UniSwitch(checked = conCortes, onCheckedChange = { conCortes = it })
                    }
                    Fila(modifier = Modifier.lineaArriba(true).padding(vertical = 12.dp)) {
                        Columna(
                            titulo = stringResource(R.string.hist_boletin_la_asistencia),
                            sub = stringResource(R.string.hist_boletin_la_asistencia_sub)
                        )
                        UniSwitch(checked = conAsistencia, onCheckedChange = { conAsistencia = it })
                    }
                }
            }
        }
        if (historial != null && periodo != null) {
            Muelle {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    UniStackButton(
                        text = stringResource(R.string.hist_guardar),
                        onClick = {
                            val imagen = ImagenDelBoletin.dibujar(periodo, opciones, escala, historial.notaMaxima, historial.aprobado)
                            ImagenDelBoletin.guardar(contexto, imagen, periodo)
                                .onSuccess { enGaleria ->
                                    if (enGaleria != null) {
                                        MensajesDelHistorico.publicar(
                                            Textos.get(R.string.hist_boletin_guardado, periodo.nombre),
                                            abrirImagen = enGaleria.toString()
                                        )
                                    }
                                }
                                .onFailure { MensajesDelHistorico.publicar(Textos.get(R.string.hist_boletin_no_se_pudo)) }
                        },
                        variant = UniStackButtonVariant.Outlined,
                        leadingIcon = Icons.Rounded.FileDownload,
                        modifier = Modifier.weight(1f)
                    )
                    UniStackButton(
                        text = stringResource(R.string.hist_compartir),
                        onClick = {
                            val imagen = ImagenDelBoletin.dibujar(periodo, opciones, escala, historial.notaMaxima, historial.aprobado)
                            ImagenDelBoletin.compartir(contexto, imagen, periodo)
                                .onFailure { MensajesDelHistorico.publicar(Textos.get(R.string.hist_boletin_no_se_pudo)) }
                        },
                        leadingIcon = Icons.Rounded.Share,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        AvisosDelHistorico(margenAbajo = 104.dp)
    }
}
