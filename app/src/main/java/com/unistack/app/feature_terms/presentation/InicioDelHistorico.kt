package com.unistack.app.feature_terms.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unistack.app.R
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.core.design.components.UniSwitch
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.Textos
import com.unistack.app.feature_terms.domain.HistorialAcademico
import com.unistack.app.feature_terms.domain.OpcionesDelAviso
import com.unistack.app.feature_terms.domain.PropuestaDePeriodo
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.GradingScale
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/*
 * Inicio mientras no hay periodo, y mientras el nuevo todavía no empieza.
 *
 * Cerrar no vacía la app: la cambia de estado. Sin periodo, Inicio cuenta cómo te fue, propone
 * el siguiente con su fecha, recuerda lo que se lleva y enseña lo que sigue funcionando. Con uno
 * creado que aún no empieza, la cuenta atrás y el paso que falta: poner el horario.
 */

@Composable
fun InicioSinPeriodo(
    estado: TermsUiState,
    onVerPeriodo: (String) -> Unit,
    onEmpezar: () -> Unit,
    onHistorico: () -> Unit,
    onAviso: () -> Unit,
    onAvisoActivo: (Boolean) -> Unit,
    onTareas: () -> Unit,
    onNotas: () -> Unit,
    onGastos: () -> Unit
) {
    val historial = estado.historial ?: return
    val ultimo = historial.cerrados.firstOrNull() ?: return
    val propuesta = estado.propuesta ?: return
    val perfil = estado.profile
    val escala = perfil?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val anterior = historial.anteriorA(ultimo)
    val cerro = ultimo.term.closedEpochDay?.let(LocalDate::ofEpochDay) ?: historial.hoy
    val dias = ChronoUnit.DAYS.between(cerro, historial.hoy)

    Pila(modifier = Modifier.padding(top = 10.dp)) {
        Tarjeta(color = Paleta.acentoContenedor, separacion = 14.dp) {
            Fila {
                GalletaConCifra(72.dp, Paleta.acento, Formato.promedio(ultimo.promedio, historial.notaMaxima), Paleta.sobreAcento, 17f)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Rotulo(stringResource(R.string.hist_sin_periodo_activo), conMargen = false)
                    Text(
                        text = when (dias) {
                            0L -> stringResource(R.string.hist_cerraste_hoy, ultimo.nombre)
                            1L -> stringResource(R.string.hist_cerraste_hace_uno, ultimo.nombre)
                            else -> stringResource(R.string.hist_cerraste_hace, ultimo.nombre, dias)
                        },
                        style = Letra.nombre,
                        color = Paleta.tinta
                    )
                    if (anterior != null) PastillaDeDiferencia(ultimo.promedio, anterior.promedio, anterior.nombre, historial.notaMaxima)
                }
            }
            FilaDeMetricas {
                Metrica(Icons.Rounded.EventAvailable, Formato.porcentaje(ultimo.asistencia), stringResource(R.string.hist_asistencia), Modifier.weight(1f).fillMaxHeight(), fondo = Paleta.acentoSuave)
                Metrica(Icons.AutoMirrored.Rounded.MenuBook, ultimo.materias.size.toString(), stringResource(R.string.hist_materias), Modifier.weight(1f).fillMaxHeight(), fondo = Paleta.acentoSuave)
                Metrica(
                    Icons.Rounded.Cancel,
                    ultimo.perdidas.size.toString(),
                    stringResource(if (ultimo.perdidas.size == 1) R.string.hist_perdida_etiqueta_una else R.string.hist_perdidas),
                    Modifier.weight(1f).fillMaxHeight(),
                    fondo = Paleta.acentoSuave
                )
            }
            BotonDeTexto(stringResource(R.string.hist_ver, ultimo.nombre), onClick = { onVerPeriodo(ultimo.id) }, centrado = true)
        }

        Tarjeta(color = Paleta.superficie, separacion = 12.dp) {
            Rotulo(stringResource(R.string.hist_tu_proximo_periodo), conMargen = false)
            Fila(separacion = 12.dp) {
                Azulejo(Icons.Rounded.AutoAwesome, Tono.Acento)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(propuesta.nombre, style = estilo(22f, 28f, FontWeight.Bold), color = Paleta.tinta)
                    Text(sugeridoPara(propuesta), style = Letra.sub, color = Paleta.apagado)
                }
            }
            val maxima = perfil?.let { GradingScaleUtils.maxGradeFor(it) } ?: historial.notaMaxima
            Text(
                stringResource(
                    R.string.hist_traigo,
                    notaMaximaTexto(maxima).removeSuffix(".0"),
                    Formato.nota(perfil?.passingGrade, escala),
                    cuenta(perfil?.gradingCutScheme?.cuts?.size ?: 3, R.string.hist_corte_uno, R.string.hist_corte_varios)
                ),
                style = Letra.sub,
                color = Paleta.apagado
            )
            UniStackButton(
                text = stringResource(R.string.hist_empezar, propuesta.nombre),
                onClick = onEmpezar,
                leadingIcon = Icons.Rounded.Add,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (ultimo.perdidas.isNotEmpty()) {
            Rotulo(stringResource(R.string.hist_lo_que_se_lleva))
            Tarjeta(relleno = PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
                ultimo.perdidas.forEachIndexed { i, materia ->
                    Fila(modifier = Modifier.lineaArriba(i > 0).padding(vertical = 12.dp), separacion = 12.dp) {
                        Azulejo(Icons.Rounded.Replay, Tono.Mal)
                        Columna(titulo = materia.nombre, sub = stringResource(R.string.hist_perdida_con_traes, Formato.nota(materia.final, escala)))
                    }
                }
            }
        }

        TuRecorrido(historial, onHistorico)

        Tarjeta {
            Fila(separacion = 12.dp) {
                Azulejo(Icons.Rounded.Notifications, Tono.Acento)
                val activo = perfil?.nextTermReminderEnabled == true
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onAviso),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (activo) {
                            stringResource(R.string.hist_avisame_el, Formato.diaMes(propuesta.diaDelAviso(perfil?.nextTermReminderOffset ?: 0)))
                        } else {
                            stringResource(R.string.hist_sin_aviso_para_empezar)
                        },
                        style = Letra.t15,
                        color = Paleta.tinta
                    )
                    Text(
                        text = if (activo) stringResource(R.string.hist_si_no_has_creado, propuesta.nombre) else stringResource(R.string.hist_toca_para_ponerlo),
                        style = Letra.sub,
                        color = Paleta.apagado
                    )
                }
                UniSwitch(checked = activo, onCheckedChange = onAvisoActivo)
            }
        }

        Rotulo(stringResource(R.string.hist_mientras_tanto))
        val modulos = perfil?.enabledModules.orEmpty()
        FilaDeMetricas {
            if (AppModule.TASKS in modulos) {
                Metrica(null, estado.tareasPendientes.toString(), stringResource(R.string.hist_tareas_pendientes), Modifier.weight(1f).fillMaxHeight(), onClick = onTareas)
            }
            Metrica(null, estado.notasRapidas.toString(), stringResource(R.string.hist_notas_rapidas), Modifier.weight(1f).fillMaxHeight(), onClick = onNotas)
            if (AppModule.EXPENSES in modulos) {
                Metrica(
                    null,
                    Formato.dineroCorto(estado.gastosDelMes),
                    stringResource(R.string.hist_gastos_en, Formato.mes(historial.hoy).take(3)),
                    Modifier.weight(1f).fillMaxHeight(),
                    onClick = onGastos
                )
            }
        }
        Text(
            stringResource(R.string.hist_sin_periodo_esperan, propuesta.nombre),
            style = Letra.sub,
            color = Paleta.apagado,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun InicioConPeriodoPorEmpezar(
    estado: TermsUiState,
    onHorario: () -> Unit,
    onHistorico: () -> Unit
) {
    val historial = estado.historial ?: return
    val periodo = historial.activo?.takeIf { it.porEmpezar } ?: return
    val dias = ChronoUnit.DAYS.between(historial.hoy, periodo.inicio)
    Pila(modifier = Modifier.padding(top = 10.dp)) {
        Tarjeta(color = Paleta.acentoContenedor, separacion = 12.dp) {
            Fila {
                Column(modifier = Modifier.weight(1f)) {
                    Rotulo(stringResource(R.string.hist_tu_periodo), conMargen = false)
                    Text(periodo.nombre, style = estilo(22f, 28f, FontWeight.Bold), color = Paleta.tinta)
                }
                Pastilla(
                    if (dias == 1L) stringResource(R.string.hist_en_un_dia) else stringResource(R.string.hist_en_dias, dias),
                    Tono.Acento
                )
            }
            Onda(0f)
            Text(
                stringResource(
                    R.string.hist_empieza_detalle,
                    Formato.larga(periodo.inicio),
                    stringResource(R.string.hist_n_semanas, periodo.semanas),
                    cuenta(periodo.materias.size, R.string.hist_materia_una, R.string.hist_materia_varias)
                ),
                style = Letra.sub,
                color = Paleta.apagado
            )
            UniStackButton(
                text = stringResource(R.string.hist_poner_horario),
                onClick = onHorario,
                leadingIcon = Icons.Rounded.CalendarMonth,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (periodo.materias.isNotEmpty()) {
            Rotulo(stringResource(R.string.hist_tus_materias))
            Tarjeta(relleno = PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
                periodo.materias.forEachIndexed { i, materia ->
                    Fila(modifier = Modifier.lineaArriba(i > 0).padding(vertical = 12.dp)) {
                        Text(materia.nombre, style = Letra.t15, color = Paleta.tinta, modifier = Modifier.weight(1f))
                        if (materia.esRepetida) Pastilla(stringResource(R.string.hist_repetida), Tono.Mal)
                    }
                }
            }
        }
        TuRecorrido(historial, onHistorico)
    }
}

/** El acumulado con su línea, que lleva al histórico. Nada si todavía no hay ninguno cerrado. */
@Composable
private fun TuRecorrido(historial: HistorialAcademico, onHistorico: () -> Unit) {
    if (historial.cerrados.isEmpty()) return
    Rotulo(stringResource(R.string.hist_tu_recorrido))
    Tarjeta(onClick = onHistorico) {
        Fila {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.hist_promedio_acumulado_frase), style = Letra.sub, color = Paleta.apagado)
                Text(Formato.promedio(historial.acumulado, historial.notaMaxima), style = Letra.numero(28f), color = Paleta.tinta)
                Text(cuenta(historial.cerrados.size, R.string.hist_cerrado_uno, R.string.hist_cerrado_varios), style = Letra.sub, color = Paleta.apagado)
            }
            Chispa(historial.evolucion)
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = Paleta.apagado, modifier = Modifier.size(22.dp))
        }
    }
}

/** «Sugerido para el lun 25 ene: empezaste el 26 ene de 2026 y el 3 feb de 2025». */
@Composable
private fun sugeridoPara(propuesta: PropuestaDePeriodo): String {
    val fecha = Formato.corta(propuesta.inicioSugerido)
    return if (propuesta.iniciosAnteriores.isEmpty()) {
        stringResource(R.string.hist_sugerido_para, fecha)
    } else {
        stringResource(
            R.string.hist_sugerido_para_empezaste,
            fecha,
            Formato.lista(propuesta.iniciosAnteriores.map { Textos.get(R.string.hist_el_dia_de, Formato.diaMes(it), it.year) })
        )
    }
}

/**
 * La hoja del aviso para empezar el periodo: encenderlo y elegir el día.
 *
 * Los días se cuentan desde el inicio sugerido. Desde Inicio recuerda que también está en
 * Configuración académica; desde allí no hace falta decirlo.
 */
@Composable
fun HojaDelAvisoParaEmpezar(
    estado: TermsUiState,
    onDismiss: () -> Unit,
    onActivo: (Boolean) -> Unit,
    onOpcion: (Int) -> Unit,
    onAbrirConfiguracion: (() -> Unit)?
) {
    val propuesta = estado.propuesta ?: return
    val perfil = estado.profile
    val activo = perfil?.nextTermReminderEnabled == true
    val elegida = perfil?.nextTermReminderOffset ?: 0
    HojaDelHistorico(onDismiss = onDismiss) {
        TituloDeHoja(stringResource(R.string.hist_aviso_para_empezar, propuesta.nombre), stringResource(R.string.hist_aviso_intro))
        Tarjeta {
            Fila(separacion = 12.dp) {
                Azulejo(Icons.Rounded.Notifications, Tono.Acento)
                Text(stringResource(R.string.hist_avisarme), style = Letra.t15, color = Paleta.tinta, modifier = Modifier.weight(1f))
                UniSwitch(checked = activo, onCheckedChange = onActivo)
            }
        }
        Spacer(Modifier.height(10.dp))
        Column(
            modifier = Modifier.alpha(if (activo) 1f else 0.4f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OpcionesDelAviso.todas.forEach { opcion ->
                FilaDeEleccion(elegida = activo && elegida == opcion, onClick = { if (activo) onOpcion(opcion) }) {
                    Columna(
                        titulo = stringResource(R.string.hist_el_dia, Formato.larga(propuesta.diaDelAviso(opcion))),
                        sub = stringResource(
                            when (opcion) {
                                OpcionesDelAviso.EL_DIA -> R.string.hist_aviso_el_dia
                                OpcionesDelAviso.DESPUES -> R.string.hist_aviso_despues
                                else -> R.string.hist_aviso_antes
                            }
                        )
                    )
                }
            }
        }
        if (onAbrirConfiguracion != null) {
            Text(
                stringResource(R.string.hist_tambien_en_configuracion),
                style = Letra.hojaIntro,
                color = Paleta.apagado,
                modifier = Modifier.padding(top = 14.dp, bottom = 16.dp)
            )
            UniStackButton(
                text = stringResource(R.string.hist_abrir_configuracion),
                onClick = onAbrirConfiguracion,
                variant = UniStackButtonVariant.Tonal,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Los avisos del periodo en Configuración académica: al acabar y para empezar el siguiente.
 */
@Composable
fun AvisosDelPeriodoEnConfiguracion(
    estado: TermsUiState,
    onAlAcabar: (Boolean) -> Unit,
    onParaEmpezar: (Boolean) -> Unit,
    onElegirDia: () -> Unit
) {
    val perfil = estado.profile ?: return
    val propuesta = estado.propuesta
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Tarjeta(relleno = PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
            Fila(modifier = Modifier.padding(vertical = 12.dp), separacion = 12.dp) {
                Azulejo(Icons.Rounded.Notifications, Tono.Acento)
                Columna(titulo = stringResource(R.string.hist_cuando_acabe), sub = stringResource(R.string.hist_cuando_acabe_sub))
                UniSwitch(checked = perfil.termEndReminderEnabled, onCheckedChange = onAlAcabar)
            }
            Fila(modifier = Modifier.lineaArriba(true).padding(vertical = 12.dp), separacion = 12.dp) {
                Azulejo(Icons.Rounded.Schedule, Tono.Acento)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onElegirDia),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(stringResource(R.string.hist_si_no_has_empezado), style = Letra.t15, color = Paleta.tinta)
                    Text(
                        text = if (perfil.nextTermReminderEnabled && propuesta != null) {
                            stringResource(R.string.hist_el_toca_para_cambiar, Formato.diaMes(propuesta.diaDelAviso(perfil.nextTermReminderOffset)))
                        } else {
                            stringResource(R.string.hist_apagado_toca)
                        },
                        style = Letra.sub,
                        color = Paleta.apagado
                    )
                }
                UniSwitch(checked = perfil.nextTermReminderEnabled, onCheckedChange = onParaEmpezar)
            }
        }
        Text(
            stringResource(R.string.hist_llegan_como),
            style = Letra.sub,
            color = Paleta.apagado,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
