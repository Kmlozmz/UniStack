package com.unistack.app.feature_terms.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.R
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.core.utils.Textos
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.presentation.AttendanceAbsent
import com.unistack.app.feature_schedule.presentation.AttendanceAttended
import com.unistack.app.feature_terms.domain.HistorialAcademico
import com.unistack.app.feature_terms.domain.PendienteDeCierre
import com.unistack.app.feature_terms.domain.PeriodoDelHistorico
import com.unistack.app.feature_user.domain.GradingScale

/**
 * Cerrar el periodo en tres pasos: revisar, ver cómo te fue y cerrar.
 *
 * Lo que falta se arregla aquí mismo, cada cosa en su hoja, y el anillo avanza. Se puede cerrar
 * con cosas a medias. La confirmación es deslizar: un toque suelto no cierra nada. Después viene
 * la celebración y a Inicio, con unos segundos para deshacer.
 */
@Composable
fun TermCloseScreen(
    onBackClick: () -> Unit,
    onClosed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TermsViewModel = hiltViewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val pendientes by viewModel.pendientes.collectAsStateWithLifecycle()
    val resueltos by viewModel.resueltos.collectAsStateWithLifecycle()
    val historial = estado.historial
    val escala = estado.profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    var paso by rememberSaveable { mutableStateOf(1) }
    var hoja by rememberSaveable { mutableStateOf<String?>(null) }
    var celebracion by remember { mutableStateOf<Triple<String, String, String>?>(null) }

    val atras: () -> Unit = { if (paso > 1) paso-- else onBackClick() }
    BackHandler(enabled = paso > 1 && celebracion == null) { paso-- }

    celebracion?.let { (termId, nombre, subtitulo) ->
        CelebracionDelHistorico(
            titulo = stringResource(R.string.hist_cerraste, nombre),
            subtitulo = subtitulo,
            onTerminada = {
                MensajesDelHistorico.publicar(Textos.get(R.string.hist_cerraste, nombre), deshacerCierre = termId to nombre)
                onClosed()
            }
        )
        return
    }

    val periodo = historial?.activo?.takeIf { !it.porEmpezar }
    Box(modifier = modifier.fillMaxSize().background(Paleta.fondo)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 132.dp)
        ) {
            CabeceraDelHistorico(onBack = atras)
            if (historial == null || periodo == null) return@Column
            val nombres = listOf(R.string.hist_paso_revisar, R.string.hist_paso_tu_periodo, R.string.hist_paso_cerrar)
            TituloDelHistorico(
                titulo = stringResource(R.string.hist_cerrar_titulo, periodo.nombre),
                subtitulo = stringResource(R.string.hist_paso_de, paso, 3, stringResource(nombres[paso - 1]))
            )
            PasosDelFlujo(paso, 3)
            when (paso) {
                1 -> PasoRevisar(periodo, pendientes, resueltos, onResolver = { hoja = it.clave })
                2 -> PasoTuPeriodo(periodo, historial, escala)
                else -> PasoCerrar(periodo, historial, pendientes.size, onRevisar = { paso = 1 })
            }
        }
        if (historial != null && periodo != null) {
            Muelle {
                when (paso) {
                    1 -> UniStackButton(
                        text = stringResource(if (pendientes.isEmpty()) R.string.hist_continuar else R.string.hist_seguir_con_lo_que_falta),
                        onClick = { paso = 2 },
                        modifier = Modifier.fillMaxWidth()
                    )
                    2 -> UniStackButton(
                        text = stringResource(R.string.hist_continuar),
                        onClick = { paso = 3 },
                        modifier = Modifier.fillMaxWidth()
                    )
                    else -> {
                        val quedara = historial.acumuladoAlCerrar(periodo)
                        DeslizarParaConfirmar(
                            texto = stringResource(R.string.hist_desliza_para_cerrar, periodo.nombre),
                            onConfirmado = {
                                viewModel.cerrarPeriodo { id, nombre, acumulado ->
                                    celebracion = Triple(
                                        id,
                                        nombre,
                                        (acumulado ?: quedara)?.let {
                                            Textos.get(R.string.hist_acumulado_queda_en, Formato.promedio(it, historial.notaMaxima))
                                        } ?: Textos.get(R.string.hist_pasa_al_historico)
                                    )
                                }
                            }
                        )
                        NotaDelMuelle(stringResource(R.string.hist_sueltalo_antes))
                    }
                }
            }
        }
        AvisosDelHistorico(margenAbajo = 104.dp)
    }

    val abierta = hoja?.let { clave -> pendientes.firstOrNull { it.clave == clave } }
    when (abierta) {
        is PendienteDeCierre.Nota -> HojaDeNota(abierta, historial, escala, viewModel, onCerrar = { hoja = null })
        is PendienteDeCierre.Clases -> HojaDeClases(abierta, viewModel, onCerrar = { hoja = null })
        is PendienteDeCierre.Tarea -> HojaDeTarea(abierta, viewModel, onCerrar = { hoja = null })
        null -> Unit
    }
}

// ================================================================== paso 1

@Composable
private fun PasoRevisar(
    periodo: PeriodoDelHistorico,
    pendientes: List<PendienteDeCierre>,
    resueltos: List<ResueltoEnElCierre>,
    onResolver: (PendienteDeCierre) -> Unit
) {
    Pila {
        val total = periodo.materias.size
        val hechas = periodo.completas
        Tarjeta(color = Paleta.acentoContenedor) {
            Fila(separacion = 16.dp) {
                AnilloDeProgreso(hechas, total)
                Columna(
                    titulo = if (total > 0 && hechas >= total) stringResource(R.string.hist_todo_completo) else stringResource(R.string.hist_materias_completas, hechas, total),
                    sub = stringResource(if (pendientes.isEmpty()) R.string.hist_al_dia else R.string.hist_arreglalo_aqui),
                    estiloTitulo = Letra.nombre
                )
            }
        }
        if (pendientes.isNotEmpty()) {
            Rotulo(stringResource(R.string.hist_lo_que_falta))
            Tarjeta(relleno = PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
                pendientes.forEachIndexed { i, pendiente ->
                    Fila(modifier = Modifier.lineaArriba(i > 0).padding(vertical = 12.dp), separacion = 12.dp) {
                        when (pendiente) {
                            is PendienteDeCierre.Nota -> {
                                Azulejo(Icons.Rounded.EditNote, Tono.Ambar)
                                Columna(
                                    titulo = pendiente.materia.nombre,
                                    sub = if (pendiente.corte.evaluado <= 0.0) {
                                        stringResource(R.string.hist_falta_el_corte, pendiente.corte.numero, pendiente.corte.pesoPorcentaje)
                                    } else {
                                        stringResource(R.string.hist_al_corte_le_falta, pendiente.corte.numero, Math.round((1 - pendiente.corte.evaluado) * 100))
                                    }
                                )
                                MiniBoton(stringResource(R.string.hist_poner_nota), onClick = { onResolver(pendiente) })
                            }
                            is PendienteDeCierre.Clases -> {
                                Azulejo(Icons.Rounded.CalendarMonth, Tono.Ambar)
                                val fechas = pendiente.clases.map { Formato.corta(it.date) }
                                val lista = if (fechas.size > 4) {
                                    Formato.lista(fechas.take(3) + stringResource(R.string.hist_n_mas, fechas.size - 3))
                                } else {
                                    Formato.lista(fechas)
                                }
                                Columna(
                                    titulo = pendiente.materia.nombre,
                                    sub = cuenta(pendiente.clases.size, R.string.hist_clase_sin_marcar_una, R.string.hist_clase_sin_marcar_varias) + ": " + lista
                                )
                                MiniBoton(stringResource(R.string.hist_marcar), onClick = { onResolver(pendiente) })
                            }
                            is PendienteDeCierre.Tarea -> {
                                Azulejo(Icons.Rounded.AssignmentTurnedIn, Tono.Ambar)
                                Columna(
                                    titulo = pendiente.materia?.nombre ?: pendiente.tarea.title,
                                    sub = stringResource(R.string.hist_vencio_el, pendiente.tarea.title, Formato.corta(pendiente.vence))
                                )
                                MiniBoton(stringResource(R.string.hist_resolver), onClick = { onResolver(pendiente) })
                            }
                        }
                    }
                }
            }
        }
        if (resueltos.isNotEmpty()) {
            Rotulo(stringResource(R.string.hist_resuelto_ahora))
            Tarjeta(relleno = PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
                resueltos.forEachIndexed { i, resuelto ->
                    Fila(modifier = Modifier.lineaArriba(i > 0).padding(vertical = 12.dp), separacion = 12.dp) {
                        Azulejo(Icons.Rounded.Check, Tono.Ok)
                        Columna(titulo = resuelto.titulo, sub = resuelto.detalle)
                    }
                }
            }
        }
        val conPendientes = pendientes.flatMap {
            when (it) {
                is PendienteDeCierre.Nota -> listOf(it.materia.id)
                is PendienteDeCierre.Clases -> listOf(it.materia.id)
                is PendienteDeCierre.Tarea -> listOfNotNull(it.materia?.id)
            }
        }.toSet()
        val bien = periodo.materias.filter { it.completa && it.id !in conPendientes && resueltos.none { r -> r.titulo == it.nombre } }
        if (bien.isNotEmpty()) {
            Rotulo(stringResource(R.string.hist_ya_estaba_completo))
            Tarjeta {
                Fila(separacion = 12.dp) {
                    Azulejo(Icons.Rounded.EventAvailable, Tono.Ok)
                    Text(
                        stringResource(R.string.hist_al_dia_lista, Formato.lista(bien.map { it.nombre })),
                        style = Letra.sub,
                        color = Paleta.apagado,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ================================================================== paso 2

@Composable
private fun PasoTuPeriodo(periodo: PeriodoDelHistorico, historial: HistorialAcademico, escala: GradingScale) {
    val anterior = historial.anteriorA(periodo)
    val antes = historial.acumulado
    val despues = historial.acumuladoAlCerrar(periodo)
    val sinFinal = periodo.materias.filter { it.final == null }
    val mejor = periodo.materias.filter { it.final != null }.maxByOrNull { it.final ?: 0.0 }
    val mejorAsistencia = periodo.materias.filter { it.asistencia.porcentaje != null }.maxByOrNull { it.asistencia.porcentaje ?: 0.0 }
    val mejorRacha = periodo.materias.maxByOrNull { it.asistencia.racha }?.takeIf { it.asistencia.racha > 0 }
    Pila {
        Tarjeta(color = Paleta.acentoContenedor) {
            Fila(separacion = 16.dp) {
                GalletaConCifra(88.dp, Paleta.acento, Formato.promedio(periodo.promedio, historial.notaMaxima), Paleta.sobreAcento, 22f)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Rotulo(stringResource(R.string.hist_promedio_de, periodo.nombre), conMargen = false)
                    if (anterior != null) PastillaDeDiferencia(periodo.promedio, anterior.promedio, anterior.nombre, historial.notaMaxima)
                    if (sinFinal.isNotEmpty()) {
                        Text(stringResource(R.string.hist_sin_le_falta_nota, Formato.lista(sinFinal.map { it.nombre })), style = Letra.sub, color = Paleta.apagado)
                    }
                }
            }
        }
        FilaDeMetricas {
            Metrica(Icons.Rounded.EventAvailable, Formato.porcentaje(periodo.asistencia), stringResource(R.string.hist_asistencia), Modifier.weight(1f).fillMaxHeight())
            Metrica(Icons.AutoMirrored.Rounded.MenuBook, periodo.materias.size.toString(), stringResource(R.string.hist_materias), Modifier.weight(1f).fillMaxHeight())
            Metrica(Icons.Rounded.Cancel, periodo.perdidas.size.toString(), cuentaSinNumero(periodo.perdidas.size, R.string.hist_perdida_etiqueta_una, R.string.hist_perdidas), Modifier.weight(1f).fillMaxHeight())
        }
        if (mejor != null || mejorAsistencia != null || mejorRacha != null) {
            Rotulo(stringResource(R.string.hist_lo_mejor))
            Tarjeta(relleno = PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
                var indice = 0
                mejor?.let {
                    FilaDeLoMejor(indice++ > 0, Icons.Rounded.Star, Tono.Acento, stringResource(R.string.hist_mejor_nota), it.nombre, Formato.nota(it.final, escala))
                }
                mejorAsistencia?.let {
                    FilaDeLoMejor(indice++ > 0, Icons.Rounded.EventAvailable, Tono.Ok, stringResource(R.string.hist_donde_mas_fuiste), it.nombre, Formato.porcentaje(it.asistencia.porcentaje))
                }
                mejorRacha?.let {
                    FilaDeLoMejor(indice++ > 0, Icons.Rounded.LocalFireDepartment, Tono.Ambar, stringResource(R.string.hist_tu_racha), it.nombre, cuenta(it.asistencia.racha, R.string.hist_clase_una, R.string.hist_clase_varias))
                }
            }
        }
        if (periodo.perdidas.isNotEmpty()) {
            Rotulo(stringResource(R.string.hist_perdiste))
            Tarjeta(relleno = PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
                periodo.perdidas.forEachIndexed { i, materia ->
                    Fila(modifier = Modifier.lineaArriba(i > 0).padding(vertical = 12.dp), separacion = 12.dp) {
                        Azulejo(Icons.Rounded.Replay, Tono.Mal)
                        Columna(titulo = materia.nombre, sub = stringResource(R.string.hist_la_podras_traer))
                        Text(Formato.nota(materia.final, escala), style = Letra.numero(18f), color = Paleta.tinta)
                    }
                }
            }
        }
        Rotulo(stringResource(R.string.hist_tu_acumulado))
        val sube = antes == null || despues == null || despues >= antes
        Tarjeta {
            Fila(separacion = 12.dp) {
                Azulejo(if (sube) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown, if (sube) Tono.Ok else Tono.Mal)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.hist_ahora), style = Letra.sub, color = Paleta.apagado)
                    Text(Formato.promedio(antes, historial.notaMaxima), style = Letra.numero(22f), color = Paleta.tinta)
                }
                Text("→", style = estilo(22f, 28f), color = Paleta.apagado)
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(R.string.hist_al_cerrar), style = Letra.sub, color = Paleta.apagado)
                    Text(Formato.promedio(despues, historial.notaMaxima), style = Letra.numero(22f), color = if (sube) AttendanceAttended else AttendanceAbsent)
                }
            }
        }
    }
}

@Composable
private fun FilaDeLoMejor(conLinea: Boolean, icono: androidx.compose.ui.graphics.vector.ImageVector, tono: Tono, etiqueta: String, nombre: String, valor: String) {
    Fila(modifier = Modifier.lineaArriba(conLinea).padding(vertical = 12.dp), separacion = 12.dp) {
        Azulejo(icono, tono)
        Columna(titulo = nombre, sub = etiqueta, subArriba = true)
        Text(valor, style = Letra.numero(18f), color = Paleta.tinta)
    }
}

/** La etiqueta en singular o plural, sin la cifra: la cifra ya va encima. */
@Composable
private fun cuentaSinNumero(n: Int, uno: Int, varios: Int): String = stringResource(if (n == 1) uno else varios)

// ================================================================== paso 3

@Composable
private fun PasoCerrar(periodo: PeriodoDelHistorico, historial: HistorialAcademico, pendientes: Int, onRevisar: () -> Unit) {
    Pila {
        Rotulo(stringResource(R.string.hist_al_cerrar_periodo, periodo.nombre))
        Tarjeta(relleno = PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
            listOf(
                Triple(Icons.Rounded.Archive, Tono.Acento, R.string.hist_pasa_al_historico_con),
                Triple(Icons.Rounded.Edit, Tono.Acento, R.string.hist_se_siguen_editando),
                Triple(Icons.Rounded.AssignmentTurnedIn, Tono.Acento, R.string.hist_siguen_igual),
                Triple(Icons.Rounded.Lock, Tono.Mal, R.string.hist_no_vuelve)
            ).forEachIndexed { i, (icono, tono, texto) ->
                Fila(modifier = Modifier.lineaArriba(i > 0).padding(vertical = 12.dp), separacion = 12.dp) {
                    Azulejo(icono, tono)
                    Text(stringResource(texto), style = Letra.t15, color = Paleta.tinta, modifier = Modifier.weight(1f))
                }
            }
        }
        if (pendientes > 0) {
            val ambar = Tono.Ambar.par()
            Tarjeta(color = ambar.contenedor) {
                Fila(separacion = 12.dp) {
                    Icon(Icons.Rounded.Warning, contentDescription = null, tint = ambar.sobre, modifier = Modifier.size(24.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(cuenta(pendientes, R.string.hist_cierras_con_una, R.string.hist_cierras_con_varias), style = Letra.t15, color = ambar.sobre)
                        Text(stringResource(R.string.hist_se_pueden_arreglar), style = Letra.sub, color = ambar.sobre.copy(alpha = 0.85f))
                    }
                    MiniBoton(
                        texto = stringResource(R.string.hist_revisar),
                        onClick = onRevisar,
                        fondo = ambar.sobre.copy(alpha = 0.16f),
                        tinta = ambar.sobre
                    )
                }
            }
        }
        Rotulo(stringResource(R.string.hist_justo_despues))
        Tarjeta {
            Fila(separacion = 12.dp) {
                Azulejo(Icons.Rounded.AutoAwesome, Tono.Acento)
                val siguiente = propuestaDespuesDe(historial)
                Text(
                    negrita(stringResource(R.string.hist_inicio_te_ofrece, "\u0000"), siguiente),
                    style = Letra.sub,
                    color = Paleta.apagado,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** El nombre que tendrá el siguiente, contado como si este ya estuviera cerrado. */
private fun propuestaDespuesDe(historial: HistorialAcademico): String =
    com.unistack.app.feature_terms.domain.PropuestaDelSiguientePeriodo.build(historial.periodos.map { it.term }, historial.hoy).nombre

// ================================================================== hojas

@Composable
private fun HojaDeNota(
    pendiente: PendienteDeCierre.Nota,
    historial: HistorialAcademico?,
    escala: GradingScale,
    viewModel: TermsViewModel,
    onCerrar: () -> Unit
) {
    val maxima = historial?.notaMaxima ?: 5.0
    var texto by rememberSaveable { mutableStateOf("") }
    val valor = leerNota(texto, maxima)
    val valido = valor != null && !valor.isNaN()
    val vista = if (valido) viewModel.vistaPrevia(pendiente.materia.id, emptyMap(), mapOf(pendiente.corte.cut.id to valor!!)) else null
    HojaDelHistorico(onDismiss = onCerrar) {
        TituloDeHoja(
            titulo = stringResource(R.string.hist_materia_corte, pendiente.materia.nombre, pendiente.corte.numero),
            intro = stringResource(R.string.hist_nota_intro, pendiente.corte.pesoPorcentaje)
        )
        Tarjeta(relleno = PaddingValues(horizontal = 18.dp, vertical = 6.dp)) {
            pendiente.corte.notas.forEachIndexed { i, nota ->
                Fila(modifier = Modifier.lineaArriba(i > 0).padding(vertical = 10.dp)) {
                    Columna(titulo = nota.name, sub = stringResource(R.string.hist_del_corte, Math.round(nota.percentage * 100)))
                    Text(Formato.nota(nota.value, escala), style = Letra.numero(16f), color = Paleta.tinta)
                }
            }
            Fila(modifier = Modifier.lineaArriba(pendiente.corte.notas.isNotEmpty()).padding(vertical = 10.dp)) {
                Columna(titulo = stringResource(R.string.hist_nota_del_corte), sub = stringResource(R.string.hist_nota_del_corte_sub))
                EntradaDeNota(
                    valor = texto,
                    onValor = { texto = it },
                    invalida = valor?.isNaN() == true,
                    descripcion = stringResource(R.string.hist_nota_del_corte)
                )
            }
        }
        Fila(modifier = Modifier.padding(horizontal = 4.dp, vertical = 14.dp)) {
            Text(stringResource(R.string.hist_final_de_la_materia), style = Letra.sub, color = Paleta.apagado, modifier = Modifier.weight(1f))
            Text(Formato.nota(vista?.final, escala), style = estilo(14f, 20f, androidx.compose.ui.text.font.FontWeight.Bold, cifras = true), color = Paleta.tinta)
        }
        UniStackButton(
            text = stringResource(R.string.hist_guardar),
            onClick = {
                viewModel.ponerNotaDeCorte(pendiente.materia.id, pendiente.corte.cut.id, valor!!)
                onCerrar()
            },
            enabled = valido,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HojaDeClases(pendiente: PendienteDeCierre.Clases, viewModel: TermsViewModel, onCerrar: () -> Unit) {
    val marcas = remember(pendiente.clave) { mutableStateMapOf<Int, ClassAttendanceStatus>() }
    val ok = Tono.Ok.par()
    val mal = Tono.Mal.par()
    HojaDelHistorico(onDismiss = onCerrar) {
        TituloDeHoja(titulo = pendiente.materia.nombre, intro = stringResource(R.string.hist_fuiste_a_estas))
        Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
            Tarjeta(relleno = PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
                pendiente.clases.forEachIndexed { i, clase ->
                    Fila(modifier = Modifier.lineaArriba(i > 0).padding(vertical = 12.dp), separacion = 8.dp) {
                        Text(Formato.conMayuscula(Formato.larga(clase.date)), style = Letra.t15, color = Paleta.tinta, modifier = Modifier.weight(1f))
                        val marca = marcas[i]
                        MiniBoton(
                            texto = stringResource(R.string.hist_fui),
                            onClick = { marcas[i] = ClassAttendanceStatus.ATTENDED },
                            fondo = if (marca == ClassAttendanceStatus.ATTENDED) ok.contenedor else Paleta.acentoSuave,
                            tinta = if (marca == ClassAttendanceStatus.ATTENDED) ok.sobre else Paleta.acento
                        )
                        MiniBoton(
                            texto = stringResource(R.string.hist_falte),
                            onClick = { marcas[i] = ClassAttendanceStatus.ABSENT },
                            fondo = if (marca == ClassAttendanceStatus.ABSENT) mal.contenedor else Paleta.acentoSuave,
                            tinta = if (marca == ClassAttendanceStatus.ABSENT) mal.sobre else Paleta.acento
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        UniStackButton(
            text = stringResource(R.string.hist_guardar),
            onClick = {
                viewModel.marcarClases(
                    pendiente.materia.id,
                    marcas.mapKeys { (i, _) -> pendiente.clases[i] }
                )
                onCerrar()
            },
            enabled = marcas.size == pendiente.clases.size,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HojaDeTarea(pendiente: PendienteDeCierre.Tarea, viewModel: TermsViewModel, onCerrar: () -> Unit) {
    HojaDelHistorico(onDismiss = onCerrar) {
        TituloDeHoja(
            titulo = pendiente.tarea.title,
            intro = if (pendiente.materia != null) {
                stringResource(R.string.hist_tarea_intro_con_materia, pendiente.materia.nombre, Formato.larga(pendiente.vence))
            } else {
                stringResource(R.string.hist_tarea_intro, Formato.larga(pendiente.vence))
            }
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            UniStackButton(
                text = stringResource(R.string.hist_si_la_entregue),
                onClick = {
                    viewModel.resolverTarea(pendiente, entregada = true)
                    onCerrar()
                },
                leadingIcon = Icons.Rounded.Check,
                modifier = Modifier.fillMaxWidth()
            )
            UniStackButton(
                text = stringResource(R.string.hist_no_la_entregue),
                onClick = {
                    viewModel.resolverTarea(pendiente, entregada = false)
                    onCerrar()
                },
                variant = UniStackButtonVariant.Outlined,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
