@file:OptIn(ExperimentalLayoutApi::class)

package com.unistack.app.feature_terms.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.R
import com.unistack.app.core.design.components.UniDatePickerDialog
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniSwitch
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.Textos
import com.unistack.app.feature_terms.domain.RepartoDeCortes
import com.unistack.app.feature_user.domain.GradingScale
import java.time.LocalDate
import androidx.compose.animation.AnimatedContent
import com.unistack.app.core.navigation.cambioDeVista
import com.unistack.app.core.navigation.transicionEntreVistas

private const val SUGERIDA = "sugerida"
private const val SEMANA_DESPUES = "semana"
private const val OTRA = "otra"
private const val IGUALES = "iguales"
private const val ULTIMO_LARGO = "final"
private const val A_MANO = "mano"

/**
 * El periodo nuevo en tres preguntas: cuándo empieza, dónde se corta y qué traes del anterior.
 *
 * Todo viene propuesto —el nombre, el inicio por tus años anteriores, la duración del último y
 * el reparto de cortes— y la regla del año se mueve con lo que eliges. Las materias nuevas no se
 * añaden aquí: van en Académico. «Revisar y crear» enseña el resumen antes de crear nada.
 */
@Composable
fun NewTermScreen(
    onBackClick: () -> Unit,
    onCreated: () -> Unit,
    onAcademicSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TermsViewModel = hiltViewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val propuesta = estado.propuesta
    val historial = estado.historial
    val perfil = estado.profile
    val previo = historial?.cerrados?.firstOrNull()
    val escala = perfil?.gradingScale ?: GradingScale.ZERO_TO_FIVE

    var paso by rememberSaveable { mutableStateOf(1) }
    var iniciado by rememberSaveable { mutableStateOf(false) }
    var eleccion by rememberSaveable { mutableStateOf(SUGERIDA) }
    var otraFecha by rememberSaveable { mutableStateOf<Long?>(null) }
    var semanas by rememberSaveable { mutableStateOf(18) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var reparto by rememberSaveable { mutableStateOf(IGUALES) }
    var aMano by rememberSaveable { mutableStateOf(ArrayList<Int>()) }
    var cortesLuego by rememberSaveable { mutableStateOf(false) }
    var traer by rememberSaveable { mutableStateOf(ArrayList<String>()) }
    var hoja by rememberSaveable { mutableStateOf<String?>(null) }
    var calendario by rememberSaveable { mutableStateOf(false) }
    var celebracion by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(propuesta != null, previo?.id) {
        if (iniciado || propuesta == null) return@LaunchedEffect
        nombre = propuesta.nombre
        semanas = propuesta.semanas
        traer = ArrayList(previo?.perdidas?.map { it.id }.orEmpty())
        iniciado = true
    }

    celebracion?.let { (titulo, sub) ->
        CelebracionDelHistorico(titulo = titulo, subtitulo = sub, onTerminada = onCreated)
        return
    }

    val atras: () -> Unit = { if (paso > 1) paso-- else onBackClick() }
    BackHandler(enabled = paso > 1) { paso-- }

    val cuts = perfil?.gradingCutScheme?.cuts?.sortedBy { it.order }.orEmpty()
    val pesos = cuts.map { Math.round(it.weight * 100).toInt() }
    val nCortes = cuts.size.coerceAtLeast(1)
    val inicio = when {
        propuesta == null -> LocalDate.now()
        eleccion == SEMANA_DESPUES -> propuesta.inicioSugerido.plusWeeks(1)
        eleccion == OTRA -> otraFecha?.let(LocalDate::ofEpochDay) ?: propuesta.inicioSugerido
        else -> propuesta.inicioSugerido
    }
    val acaba = propuesta?.finPara(inicio, semanas) ?: inicio.plusWeeks(semanas.toLong())
    val cortes = when (reparto) {
        ULTIMO_LARGO -> RepartoDeCortes.ultimoMasLargo(semanas, nCortes)
        A_MANO -> aMano.takeIf { it.size == nCortes && it.sum() == semanas } ?: RepartoDeCortes.iguales(semanas, nCortes)
        else -> RepartoDeCortes.iguales(semanas, nCortes)
    }
    val perdidas = previo?.perdidas.orEmpty()

    val transicion = transicionEntreVistas()
    Box(modifier = modifier.fillMaxSize().background(Paleta.fondo)) {
        // Cada paso entra como una pantalla, con la transición de Movimiento y su propio
        // desplazamiento: el paso siguiente empieza arriba, no donde se dejó el anterior.
        AnimatedContent(
            targetState = paso,
            transitionSpec = { cambioDeVista(transicion) { it } },
            label = "pasos del periodo nuevo",
            modifier = Modifier.fillMaxSize()
        ) { pasoVisible ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Paleta.fondo)
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 132.dp)
            ) {
                CabeceraDelHistorico(onBack = atras)
                if (propuesta == null || historial == null) return@Column
                val titulos = listOf(
                    stringResource(R.string.hist_nuevo_cuando),
                    stringResource(R.string.hist_nuevo_donde),
                    if (previo != null) stringResource(R.string.hist_nuevo_que_traes_de, previo.nombre) else stringResource(R.string.hist_nuevo_que_traes)
                )
                TituloDelHistorico(
                    titulo = titulos[pasoVisible - 1],
                    subtitulo = stringResource(R.string.hist_paso_de, pasoVisible, 3, nombre)
                )
                PasosDelFlujo(pasoVisible, 3)
                Pila {
                    when (pasoVisible) {
                        1 -> {
                            if (previo != null) {
                                Tarjeta(separacion = 6.dp) {
                                    Rotulo(stringResource(R.string.hist_asi_queda_tu_anio), conMargen = false)
                                    val textoLibres = stringResource(R.string.hist_semanas_libres)
                                    LineaDelAnio(
                                        previoInicio = previo.inicio,
                                        previoFin = previo.fin,
                                        previoNombre = previo.nombre,
                                        nuevoInicio = inicio,
                                        nuevoFin = acaba,
                                        nuevoNombre = nombre,
                                        hoy = historial.hoy,
                                        textoLibres = { String.format(textoLibres, it) },
                                        textoHoy = stringResource(R.string.hist_hoy)
                                    )
                                }
                            }
                            Rotulo(stringResource(R.string.hist_empieza))
                            val sugerido = propuesta.inicioSugerido
                            val detalleSugerido = if (propuesta.iniciosAnteriores.isNotEmpty()) {
                                stringResource(
                                    R.string.hist_sugerido_empezaste,
                                    Formato.lista(propuesta.iniciosAnteriores.map { Textos.get(R.string.hist_el_dia_de, Formato.diaMes(it), it.year) })
                                )
                            } else if (previo != null) {
                                stringResource(R.string.hist_sugerido_despues_de, previo.nombre)
                            } else {
                                stringResource(R.string.hist_sugerido)
                            }
                            OpcionDeInicio(eleccion == SUGERIDA, Icons.Rounded.AutoAwesome, Formato.conMayuscula(Formato.larga(sugerido)), detalleSugerido) { eleccion = SUGERIDA }
                            OpcionDeInicio(eleccion == SEMANA_DESPUES, Icons.Rounded.CalendarMonth, Formato.conMayuscula(Formato.larga(sugerido.plusWeeks(1))), stringResource(R.string.hist_una_semana_despues)) { eleccion = SEMANA_DESPUES }
                            val otra = otraFecha?.let(LocalDate::ofEpochDay)
                            OpcionDeInicio(
                                eleccion == OTRA,
                                Icons.Rounded.CalendarMonth,
                                if (eleccion == OTRA && otra != null) Formato.conMayuscula(Formato.larga(otra)) else stringResource(R.string.hist_otra_fecha),
                                stringResource(if (eleccion == OTRA && otra != null) R.string.hist_elegido_en_calendario else R.string.hist_elige_en_calendario)
                            ) { calendario = true }
                            Rotulo(stringResource(R.string.hist_dura))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                propuesta.opcionesDeSemanas.forEach { w ->
                                    Opcion(stringResource(R.string.hist_n_semanas, w), semanas == w, onClick = {
                                        semanas = w
                                        if (reparto == A_MANO) reparto = IGUALES
                                    })
                                }
                            }
                            Tarjeta {
                                Fila(separacion = 12.dp) {
                                    Azulejo(Icons.Rounded.Schedule, Tono.Acento)
                                    Columna(
                                        titulo = stringResource(R.string.hist_acaba_el, Formato.corta(acaba), acaba.year),
                                        sub = if (previo != null && semanas == propuesta.semanas) {
                                            stringResource(R.string.hist_previsto_como, semanas, previo.nombre)
                                        } else {
                                            stringResource(R.string.hist_previsto)
                                        }
                                    )
                                }
                            }
                            Tarjeta {
                                Fila(separacion = 12.dp) {
                                    Azulejo(Icons.Rounded.Edit, Tono.Acento)
                                    Columna(titulo = nombre, sub = stringResource(R.string.hist_se_llamara), subArriba = true)
                                    MiniBoton(stringResource(R.string.hist_cambiar), onClick = { hoja = "nombre" })
                                }
                            }
                        }
                        2 -> {
                            Tarjeta(separacion = 8.dp) {
                                val textoSemanas = stringResource(R.string.hist_n_semanas)
                                val textoSemanasCorto = stringResource(R.string.hist_n_semanas_corto)
                                BarraDeCortes(
                                    semanasPorCorte = cortes,
                                    pesos = pesos,
                                    inicio = inicio,
                                    conFechas = !cortesLuego,
                                    textoSemanas = { w, corto -> String.format(if (corto) textoSemanasCorto else textoSemanas, w) },
                                    etiquetaCorte = { n, peso -> Formato.etiquetaCorte(n, peso) }
                                )
                                Text(
                                    stringResource(if (cortesLuego) R.string.hist_sin_fechas_de_corte else R.string.hist_con_fechas_de_corte),
                                    style = Letra.sub,
                                    color = Paleta.apagado
                                )
                            }
                            if (nCortes > 1) {
                                Rotulo(stringResource(R.string.hist_reparto))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Opcion(stringResource(R.string.hist_reparto_iguales), reparto == IGUALES, onClick = { reparto = IGUALES })
                                    Opcion(stringResource(R.string.hist_reparto_ultimo), reparto == ULTIMO_LARGO, onClick = { reparto = ULTIMO_LARGO })
                                    Opcion(stringResource(R.string.hist_reparto_mano), reparto == A_MANO, onClick = {
                                        aMano = ArrayList(cortes)
                                        reparto = A_MANO
                                    })
                                }
                                if (reparto == A_MANO) {
                                    Tarjeta(relleno = PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
                                        cortes.forEachIndexed { k, w ->
                                            Fila(modifier = Modifier.lineaArriba(k > 0).padding(vertical = 10.dp), separacion = 12.dp) {
                                                Box(Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).background(colorDeCorte(k)))
                                                Text(cuts.getOrNull(k)?.name.orEmpty(), style = Letra.t15, color = Paleta.tinta, modifier = Modifier.weight(1f))
                                                if (k < cortes.lastIndex) {
                                                    val menos = RepartoDeCortes.ajustar(cortes, semanas, k, -1)
                                                    val mas = RepartoDeCortes.ajustar(cortes, semanas, k, 1)
                                                    Paso(
                                                        valor = w,
                                                        onMenos = { menos?.let { aMano = ArrayList(it) } },
                                                        onMas = { mas?.let { aMano = ArrayList(it) } },
                                                        puedeMenos = menos != null,
                                                        puedeMas = mas != null
                                                    )
                                                } else {
                                                    Text("$w", style = Letra.numero(18f), color = Paleta.tinta, modifier = Modifier.padding(end = 12.dp))
                                                }
                                            }
                                        }
                                    }
                                    Text(
                                        stringResource(R.string.hist_el_ultimo_se_ajusta, semanas),
                                        style = Letra.sub,
                                        color = Paleta.apagado,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                            }
                            Tarjeta {
                                Fila(separacion = 12.dp) {
                                    Azulejo(Icons.Rounded.Schedule, Tono.Acento)
                                    Columna(titulo = stringResource(R.string.hist_todavia_no_se_las_fechas), sub = stringResource(R.string.hist_las_pones_luego))
                                    UniSwitch(checked = cortesLuego, onCheckedChange = { cortesLuego = it })
                                }
                            }
                            Rotulo(stringResource(R.string.hist_se_mantiene))
                            Tarjeta(relleno = PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
                                val maxima = perfil?.let { GradingScaleUtils.maxGradeFor(it) } ?: 5.0
                                listOf(
                                    Triple(Icons.Rounded.Straighten, stringResource(R.string.hist_escala), stringResource(R.string.hist_escala_valor, notaMaximaTexto(maxima).removeSuffix(".0"))),
                                    Triple(Icons.Rounded.Star, stringResource(R.string.hist_apruebas_con), Formato.nota(perfil?.passingGrade, escala)),
                                    Triple(
                                        Icons.Rounded.EventAvailable,
                                        stringResource(R.string.hist_tope_de_faltas),
                                        perfil?.absenceLimit?.let { stringResource(R.string.hist_pierdes_con, it) } ?: stringResource(R.string.hist_sin_tope)
                                    )
                                ).forEachIndexed { i, (icono, etiqueta, valor) ->
                                    Fila(modifier = Modifier.lineaArriba(i > 0).padding(vertical = 10.dp), separacion = 12.dp) {
                                        Azulejo(icono)
                                        Text(etiqueta, style = Letra.t15, color = Paleta.tinta, modifier = Modifier.weight(1f))
                                        Text(valor, style = Letra.t15, color = Paleta.tinta)
                                    }
                                }
                                Box(modifier = Modifier.fillMaxWidth().lineaArriba(true).padding(vertical = 4.dp)) {
                                    BotonDeTexto(stringResource(R.string.hist_cambiar_en_configuracion), onClick = onAcademicSettingsClick)
                                }
                            }
                        }
                        else -> {
                            perdidas.forEach { materia ->
                                Tarjeta {
                                    Fila(separacion = 12.dp) {
                                        Azulejo(Icons.Rounded.Replay, Tono.Mal)
                                        Columna(
                                            titulo = materia.nombre,
                                            sub = stringResource(R.string.hist_la_perdiste_con, Formato.nota(materia.final, escala))
                                        )
                                        UniSwitch(
                                            checked = materia.id in traer,
                                            onCheckedChange = { activo ->
                                                traer = ArrayList(if (activo) traer + materia.id else traer - materia.id)
                                            }
                                        )
                                    }
                                }
                            }
                            if (perdidas.isEmpty()) {
                                Tarjeta {
                                    Fila(separacion = 12.dp) {
                                        Azulejo(Icons.Rounded.EventAvailable, Tono.Ok)
                                        Columna(
                                            titulo = stringResource(R.string.hist_nada_que_traer),
                                            sub = previo?.let { stringResource(R.string.hist_no_perdiste_en, it.nombre) } ?: stringResource(R.string.hist_no_perdiste)
                                        )
                                    }
                                }
                            }
                            Tarjeta {
                                Fila(separacion = 12.dp) {
                                    Azulejo(Icons.AutoMirrored.Rounded.MenuBook, Tono.Acento)
                                    Columna(titulo = stringResource(R.string.hist_y_las_nuevas), sub = stringResource(R.string.hist_y_las_nuevas_sub))
                                }
                            }
                        }
                    }
                }
            }
        }
        if (propuesta != null && historial != null) {
            Muelle {
                AnimatedContent(
                    targetState = paso,
                    transitionSpec = { cambioDelMuelle(transicion) },
                    label = "muelle del periodo nuevo",
                    modifier = Modifier.fillMaxWidth()
                ) { pasoVisible ->
                    if (pasoVisible < 3) {
                        UniStackButton(text = stringResource(R.string.hist_continuar), onClick = { paso++ }, modifier = Modifier.fillMaxWidth())
                    } else {
                        UniStackButton(text = stringResource(R.string.hist_revisar_y_crear), onClick = { hoja = "revisar" }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
        AvisosDelHistorico(margenAbajo = 104.dp)
    }

    if (calendario) {
        UniDatePickerDialog(
            selectedDate = inicio,
            onDateSelected = { fecha ->
                otraFecha = fecha.toEpochDay()
                eleccion = OTRA
                calendario = false
            },
            onDismiss = { calendario = false }
        )
    }

    if (hoja == "nombre") {
        var borrador by rememberSaveable { mutableStateOf(nombre) }
        HojaDelHistorico(onDismiss = { hoja = null }) {
            TituloDeHoja(stringResource(R.string.hist_nombre_del_periodo), stringResource(R.string.hist_nombre_intro))
            CampoDelHistorico(stringResource(R.string.hist_como_se_llama), borrador, { borrador = it })
            Spacer(Modifier.height(14.dp))
            UniStackButton(
                text = stringResource(R.string.hist_guardar),
                onClick = {
                    nombre = borrador.trim().ifBlank { propuesta?.nombre.orEmpty() }
                    hoja = null
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (hoja == "revisar" && propuesta != null) {
        val traidas = perdidas.filter { it.id in traer }
        HojaDelHistorico(onDismiss = { hoja = null }) {
            TituloDeHoja(stringResource(R.string.hist_todo_listo_para, nombre), stringResource(R.string.hist_todo_listo_intro))
            Tarjeta(relleno = PaddingValues(horizontal = 18.dp, vertical = 4.dp)) {
                FilaDeResumen(false, Icons.Rounded.CalendarMonth, stringResource(R.string.hist_de_a, Formato.corta(inicio), Formato.corta(acaba), acaba.year), stringResource(R.string.hist_n_semanas, semanas))
                FilaDeResumen(
                    true,
                    Icons.Rounded.Straighten,
                    stringResource(R.string.hist_n_cortes_pesos, nCortes, pesos.joinToString(" / ")),
                    if (cortesLuego) stringResource(R.string.hist_fechas_mas_adelante) else stringResource(R.string.hist_semanas_por_corte, cortes.joinToString(" · "))
                )
                FilaDeResumen(
                    true,
                    Icons.AutoMirrored.Rounded.MenuBook,
                    if (traidas.isEmpty()) stringResource(R.string.hist_sin_materias_todavia) else cuenta(traidas.size, R.string.hist_materia_una, R.string.hist_materia_varias),
                    if (traidas.isEmpty()) stringResource(R.string.hist_se_anaden_en_academico) else traidas.joinToString(" · ") { Textos.get(R.string.hist_desde_cero, it.nombre) }
                )
                val aviso = perfil?.termEndReminderEnabled == true
                FilaDeResumen(
                    true,
                    Icons.Rounded.Notifications,
                    if (aviso) stringResource(R.string.hist_te_aviso_el, Formato.corta(acaba)) else stringResource(R.string.hist_sin_aviso_al_acabar),
                    stringResource(if (aviso) R.string.hist_para_que_lo_cierres else R.string.hist_se_activa_en_configuracion)
                )
            }
            Spacer(Modifier.height(14.dp))
            UniStackButton(
                text = stringResource(R.string.hist_crear, nombre),
                onClick = {
                    hoja = null
                    val titulo = Textos.get(R.string.hist_empezaste, nombre)
                    val sub = Textos.get(R.string.hist_empezaste_sub, Formato.larga(inicio))
                    viewModel.crearPeriodo(
                        nombre = nombre,
                        inicio = inicio,
                        semanas = semanas,
                        semanasPorCorte = if (cortesLuego) null else cortes,
                        repetir = traidas.map { it.id }.toSet(),
                        onDone = { celebracion = titulo to sub }
                    )
                },
                leadingIcon = Icons.Rounded.Add,
                modifier = Modifier.fillMaxWidth()
            )
            BotonDeTexto(stringResource(R.string.hist_seguir_revisando), onClick = { hoja = null }, centrado = true)
        }
    }
}

@Composable
private fun OpcionDeInicio(elegida: Boolean, icono: ImageVector, titulo: String, detalle: String, onClick: () -> Unit) {
    FilaDeEleccion(elegida = elegida, onClick = onClick) {
        Azulejo(icono, if (elegida) Tono.Acento else Tono.Neutro)
        Columna(titulo = titulo, sub = detalle)
    }
}

@Composable
private fun FilaDeResumen(conLinea: Boolean, icono: ImageVector, titulo: String, sub: String) {
    Fila(modifier = Modifier.lineaArriba(conLinea).padding(vertical = 12.dp), separacion = 12.dp) {
        Azulejo(icono, Tono.Acento)
        Columna(titulo = titulo, sub = sub)
    }
}

/** El «− 6 +» de las semanas de un corte. */
@Composable
private fun Paso(valor: Int, onMenos: () -> Unit, onMas: () -> Unit, puedeMenos: Boolean, puedeMas: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        BotonRedondo(Icons.Rounded.Remove, stringResource(R.string.hist_menos_semanas), puedeMenos, onMenos)
        Text("$valor", style = estilo(18f, 24f, cifras = true), color = Paleta.tinta, textAlign = TextAlign.Center, modifier = Modifier.widthIn(min = 30.dp))
        BotonRedondo(Icons.Rounded.Add, stringResource(R.string.hist_mas_semanas), puedeMas, onMas)
    }
}

@Composable
private fun BotonRedondo(icono: ImageVector, descripcion: String, activo: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Paleta.alto)
            .clickable(enabled = activo, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = descripcion, tint = Paleta.tinta.copy(alpha = if (activo) 1f else 0.35f), modifier = Modifier.size(20.dp))
    }
}

/** El botón de solo texto en el acento, a la izquierda o centrado. */
@Composable
internal fun BotonDeTexto(texto: String, onClick: () -> Unit, centrado: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = if (centrado) 20.dp else 0.dp),
        contentAlignment = if (centrado) Alignment.Center else Alignment.CenterStart
    ) {
        Text(texto, style = estilo(16f, 24f, FontWeight.SemiBold), color = Paleta.acento)
    }
}
