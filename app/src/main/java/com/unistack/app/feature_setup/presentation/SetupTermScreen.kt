package com.unistack.app.feature_setup.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.CutDatesSection
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniDatePickerDialog
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.feature_terms.domain.AcademicTermType
import com.unistack.app.feature_user.domain.Corte
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Paso 1 de 3 del periodo: cómo se organiza el calendario.
 *
 * La cinta va horizontal y con el nombre a secas. Debajo, en vez de repetir lo que acabas de
 * tocar, se enseña **cómo queda tu año**: una barra partida en tantos tramos como periodos
 * caben, con el primero destacado.
 *
 * Ese cambio es la diferencia entre una pantalla viva y una apagada. Antes la tarjeta decía
 * «Semestral» debajo de una ficha que ya decía «Semestral», y ocupaba el sitio de más peso
 * visual con algo que el usuario acababa de leer. Lo que le faltaba era información, no
 * adorno: una barra que cambia al tocar hace que la elección tenga efecto visible.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SetupTermTypeScreen(
    type: AcademicTermType?,
    cutCount: Int,
    cutWeights: List<String>,
    onTypeSelected: (AcademicTermType) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
    totalSteps: Int = 8
) {
    BackHandler(onBack = onBackClick)
    SetupScaffold(
        step = SetupSteps.Term,
        totalSteps = totalSteps,
        modifier = modifier,
        actions = {
            UniStackButton(
                text = "Continuar",
                onClick = onContinueClick,
                enabled = type != null,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SetupPlainTitle(
                title = "Tu periodo",
                subtitle = "Cómo se organiza el calendario de tu universidad."
            )

            ResueltoAntes(
                titulo = "$cutCount ${if (cutCount == 1) Corte.Singular.lowercase() else Corte.Plural.lowercase()}",
                detalle = cutWeights.filter { it.isNotBlank() }.joinToString(" · ") { "$it%" }
            )

            SetupSectionLabel("¿Cómo se manejan los periodos académicos en tu universidad?")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AcademicTermType.entries.forEach { opcion ->
                    TermTypeChip(
                        label = opcion.label,
                        selected = type == opcion,
                        onClick = { onTypeSelected(opcion) }
                    )
                }
            }

            Revelado(visible = type != null) {
                type?.let { AnoPartido(it) }
            }
            Revelado(visible = type == null) { EsperandoEleccion() }
        }
    }
}

/**
 * Cómo queda el año con el tipo elegido.
 *
 * Los tramos salen de dividir el año entre las semanas típicas de ese tipo, así que anual
 * pinta uno solo y por bloques pinta seis. Es una ilustración, no un calendario: sirve para
 * reconocer «sí, así es lo mío» de un vistazo.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AnoPartido(type: AcademicTermType) {
    val hoy = LocalDate.now()
    var porqueVisible by remember { mutableStateOf(false) }
    // El mismo numero que dice el texto. Dividir 52 entre las semanas daba tres para
    // semestral, que son dos: la barra contradecia a la linea de al lado.
    val cuantos = type.perYear.coerceIn(1, 6)
    // El tramo que se ilumina es en el que estas hoy, no el primero del año.
    val actual = type.blockFor(hoy).coerceIn(1, cuantos)
    UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Así queda tu año:",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            /*
             * El rotulo va encima del tramo y apunta hacia abajo.
             *
             * Debajo funcionaba, pero dejaba el aire de la tarjeta partido en dos: el hueco
             * util quedaba encima de la barra, vacio, y el rotulo empujaba hacia abajo a los
             * meses y a la explicacion. Arriba ocupa un sitio que ya estaba libre.
             *
             * Sigue colgando del propio recuadro —misma fila de pesos que la barra, asi que
             * cae siempre sobre el que toca— y por eso senala sin explicarse. El rotulo no se
             * recorta con seis tramos porque no se le deja partir y la casilla no recorta a
             * sus hijos: se desborda sobre las vecinas, que estan vacias.
             */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(cuantos) { indice ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                        if (indice + 1 == actual) {
                            Column(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable { porqueVisible = !porqueVisible }
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = "Estás aquí",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        softWrap = false,
                                        overflow = TextOverflow.Visible
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                                        contentDescription = if (porqueVisible) {
                                            "Ocultar el porqué"
                                        } else {
                                            "Por qué este tramo"
                                        },
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Text(
                                    text = "▼",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 9.sp,
                                    lineHeight = 10.sp
                                )
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(10.dp)),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(cuantos) { indice ->
                    val primero = indice + 1 == actual
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(
                                if (primero) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${indice + 1}",
                            color = if (primero) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // Los extremos del año natural, que es el eje que dibuja la barra.
                Text("enero", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                Text("diciembre", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
            Revelado(visible = porqueVisible) {
                Text(
                    text = "Hoy es ${fechaLarga(hoy)}, y esa fecha cae en el $actual.º de los $cuantos tramos del año. Es solo para orientarte: las fechas exactas las pones tú en el paso siguiente.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
            Text(
                text = if (cuantos == 1) {
                    "Dura el año entero, unas ${type.weeks} semanas de clase."
                } else {
                    "Cada uno dura unos ${type.months} meses: $cuantos al año, de unas ${type.weeks} semanas de clase. Hoy estarías en el $actual.º, que es el que vas a configurar."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Paso 2 de 3: cuándo empieza.
 *
 * Primero se pregunta si ya empezó, que es algo que cualquiera contesta sin mirar un
 * calendario; de ahí sale una fecha propuesta que sigue siendo editable. Antes venía puesta
 * con la de hoy, así que la pantalla afirmaba en lugar de preguntar, y casi nadie configura
 * la app el día exacto en que arranca su semestre.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SetupTermDatesScreen(
    type: AcademicTermType,
    name: String,
    cutCount: Int,
    start: LocalDate?,
    plannedEnd: LocalDate?,
    knowsCutDates: Boolean?,
    onStartChange: (LocalDate) -> Unit,
    onPlannedEndChange: (LocalDate) -> Unit,
    onKnowsCutDatesChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
    totalSteps: Int = 8
) {
    var picking by remember { mutableStateOf<TermDateTarget?>(null) }
    var ayudaVisible by remember { mutableStateOf(false) }
    val fechasPuestas = start != null && plannedEnd != null
    /*
     * Para seguir basta el inicio.
     *
     * El fin se pide como **previsión**, y así está escrito en `AcademicTerm`: sirve para
     * avisarte cuando llegue, y el periodo no se cierra ese día. Pero la pantalla lo exigía
     * igual que al inicio, así que quien todavía no lo sabe —que en agosto es casi todo el
     * mundo— se quedaba encallado inventándose un día para poder pasar.
     *
     * El inicio sí es obligatorio y no por capricho: de él cuelgan las semanas del periodo,
     * los tramos de los cortes y la asistencia. Sin él la app no distingue una clase anterior
     * al semestre de una que se olvidó marcar, que es el problema que esto vino a resolver.
     */
    val listo = start != null && (cutCount <= 1 || knowsCutDates != null)

    BackHandler(onBack = onBackClick)
    SetupScaffold(
        step = SetupSteps.TermDates,
        totalSteps = totalSteps,
        modifier = modifier,
        actions = {
            UniStackButton(
                text = "Continuar",
                onClick = onContinueClick,
                enabled = listo,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SetupPlainTitle(
                title = "Cuándo empieza",
                subtitle = "Con esto podremos llevar un mejor orden de tus fechas."
            )

            SetupSectionLabel("Tu periodo")
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "${type.label} · $cutCount ${if (cutCount == 1) Corte.Singular.lowercase() else Corte.Plural.lowercase()}",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmallEmphasized
                        )
                        if (name.isNotBlank()) {
                            Text(
                                text = name,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            /*
             * Dos campos vacios, sin pregunta previa ni fechas propuestas.
             *
             * Hubo una pregunta —«¿ya empezo?»— cuyo unico trabajo era decidir si proponer una
             * fecha hacia atras o hacia delante. Quitada la propuesta, la pregunta no decidia
             * nada: era un paso mas para llegar al mismo sitio.
             */
            SetupSectionLabel("Fechas")
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                TermDateField(
                    label = "Empieza",
                    date = start,
                    modifier = Modifier.weight(1f),
                    onClick = { picking = TermDateTarget.START }
                )
                TermDateField(
                    label = "Acaba (si lo sabes)",
                    date = plannedEnd,
                    vacio = "Aún no",
                    modifier = Modifier.weight(1f),
                    onClick = { picking = TermDateTarget.PLANNED_END }
                )
            }

            // Lo que pasa si se deja en blanco, dicho antes de que haga falta preguntarlo.
            Revelado(visible = start != null && plannedEnd == null) {
                UniCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("👌", fontSize = 15.sp)
                        Text(
                            text = "Puedes seguir sin ella. Es solo una previsión para avisarte cuando llegue el final, y la pones cuando la sepas en Ajustes › Configuración académica.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Revelado(visible = fechasPuestas) {
                UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("💡", fontSize = 15.sp)
                        Text(
                            text = "Son ${semanasEntre(start!!, plannedEnd!!)} semanas. " +
                                "El periodo no se cierra en esa fecha: se cierra cuando tú lo cierres.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Revelado(visible = fechasPuestas && cutCount > 1) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    /*
                     * Se pregunta por lo que vas a hacer, no por lo que sabes.
                     *
                     * «¿Sabes cuando cierra cada corte?» con un «Si, las se» debajo sonaba a
                     * examen, y ademas no era la pregunta: la app no necesita saber si te las
                     * sabes, necesita saber si las escribes ahora o luego.
                     */
                    /*
                     * Un rotulo que nombra el dato no basta: hay que decir para que sirve.
                     *
                     * «Las fechas de cada corte» no decia si eran obligatorias, que pasaba si
                     * faltaban ni por que se piden. La interrogacion lo cuenta a quien lo
                     * pregunte, sin cargarle el texto a quien ya lo sabe.
                     */
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        SetupSectionLabel("¿Cuándo cierra cada ${Corte.Singular.lowercase()}?")
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                            contentDescription = if (ayudaVisible) "Ocultar la explicación" else "Qué es esto",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .clickable { ayudaVisible = !ayudaVisible }
                        )
                    }
                    Revelado(visible = ayudaVisible) {
                        UniCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Text(
                                text = "Cada ${Corte.Singular.lowercase()} termina un día concreto. Si escribes esos días, cada nota que registres se va sola al ${Corte.Singular.lowercase()} que le toca por su fecha. Si no, lo eliges tú a mano en cada nota.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                    UniSegmentedControl(
                        selected = knowsCutDates,
                        options = listOf(
                            UniSegmentedOption<Boolean?>(value = true, label = "Ponerlas ahora"),
                            UniSegmentedOption<Boolean?>(value = false, label = "Más adelante")
                        ),
                        onSelected = { valor -> valor?.let(onKnowsCutDatesChange) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    /*
                     * Dejarlo para luego dice que pasa mientras y donde se hace.
                     *
                     * Antes solo se saltaba el paso, asi que aplazarlo parecia renunciar a algo
                     * sin saber a que ni si tenia vuelta atras.
                     */
                    Revelado(visible = knowsCutDates == false) {
                        UniCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("🗓", fontSize = 15.sp)
                                Text(
                                    text = "Sin problema. Mientras tanto eliges a mano el ${Corte.Singular.lowercase()} de cada nota que pongas, y en cuanto las sepas las añades en Ajustes › Configuración académica.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    picking?.let { target ->
        UniDatePickerDialog(
            selectedDate = if (target == TermDateTarget.START) start else plannedEnd,
            onDateSelected = { fecha ->
                if (target == TermDateTarget.START) onStartChange(fecha) else onPlannedEndChange(fecha)
                picking = null
            },
            onDismiss = { picking = null }
        )
    }
}

/**
 * Paso 3 de 3: dónde se corta.
 *
 * Sigues escribiendo **el día en que acaba** cada corte —eso es lo que hace imposibles los
 * huecos y los solapes, porque el siguiente empieza al día posterior— pero debajo se lee el
 * tramo entero: «27 ago → 3 oct · 5 semanas».
 *
 * Ahí estaba la confusión: la pantalla pedía finales y el usuario piensa en tramos, así que
 * tenía que calcularlos de cabeza. Con el tramo delante, que el corte 2 empiece justo donde
 * acaba el 1 deja de ser una promesa del texto y pasa a verse.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SetupTermCutDatesScreen(
    start: LocalDate,
    plannedEnd: LocalDate?,
    cutWeights: List<String>,
    cutEndDates: List<LocalDate?>,
    isValid: Boolean,
    onCutDateChange: (Int, LocalDate) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
    totalSteps: Int = 9
) {
    BackHandler(onBack = onBackClick)
    SetupScaffold(
        step = SetupSteps.TermCutDates,
        totalSteps = totalSteps,
        modifier = modifier,
        actions = {
            UniStackButton(
                text = "Terminar",
                onClick = onContinueClick,
                enabled = isValid,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SetupPlainTitle(
                title = "¿Dónde se corta?",
                subtitle = plannedEnd?.let {
                    "Tu periodo va del ${fechaCorta(start)} al ${fechaCorta(it)}. Pon el último día de cada ${Corte.Singular.lowercase()}."
                } ?: "Pon el último día de cada ${Corte.Singular.lowercase()}."
            )

            /*
             * Se dice de entrada como funciona, en vez de dejar que se deduzca.
             *
             * El encadenado —cada corte empieza donde acabo el anterior— habia que descubrirlo
             * mirando las fechas un rato. Escrito arriba, la pantalla se entiende antes de
             * tocarla.
             */
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("💡", fontSize = 15.sp)
                    Text(
                        text = "Solo el último día. Cada ${Corte.Singular.lowercase()} empieza al día siguiente del anterior, y el último acaba con el periodo.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            CutDatesSection(
                termStart = start,
                termPlannedEnd = plannedEnd,
                cutWeights = cutWeights,
                cutEndDates = cutEndDates,
                onCutDateChange = onCutDateChange
            )
        }
    }
}

/** Lo que ya quedó resuelto en el paso anterior, para no perder el hilo. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ResueltoAntes(titulo: String, detalle: String) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(LocalSectionColors.current.onTrack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(19.dp)
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmallEmphasized
                )
                Text(
                    text = detalle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun EsperandoEleccion() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Elige una para ver cómo queda tu año",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

/**
 * Lo que aparece, aparece creciendo y no de golpe.
 *
 * Con las preguntas saliendo de una en una, un bloque que se materializa seco se lee como un
 * salto de la pantalla y no como la consecuencia de lo que acabas de tocar.
 */
@Composable
internal fun Revelado(visible: Boolean, retardoMs: Int = 0, content: @Composable () -> Unit) {
    /*
     * El retardo solo cuenta al aparecer.
     *
     * Encadenandolo, un bloque de tres cosas entra como tres cosas y no como un muro, que es
     * justo lo que se veia al elegir cuantos cortes hay. Al irse no se escalona: hacer esperar
     * a algo que ya sobra se lee como que la pantalla va lenta.
     */
    var mostrado by remember { mutableStateOf(visible && retardoMs == 0) }
    LaunchedEffect(visible) {
        if (!visible) {
            mostrado = false
        } else {
            if (retardoMs > 0) delay(retardoMs.toLong())
            mostrado = true
        }
    }
    AnimatedVisibility(
        visible = mostrado,
        enter = fadeIn(animationSpec = tween(180)) + expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ),
        exit = fadeOut(animationSpec = tween(110)) + shrinkVertically(animationSpec = tween(160))
    ) {
        content()
    }
}

/** Cuál de las dos fechas del periodo se está eligiendo. */
private enum class TermDateTarget { START, PLANNED_END }

/** Una ficha de la cinta: el nombre a secas, que el detalle va debajo. */
@Composable
private fun TermTypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun SetupSectionLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun TermDateField(
    label: String,
    date: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    vacio: String = "Elegir",
    enabled: Boolean = true
) {
    UniCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = if (enabled) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        onClick = if (enabled) onClick else null
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Text(
                text = date?.let(::fechaLarga) ?: vacio,
                color = when {
                    date != null -> MaterialTheme.colorScheme.onSurface
                    enabled -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private val MesesCortos =
    listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

private fun fechaLarga(date: LocalDate): String =
    "${date.dayOfMonth} ${MesesCortos[date.monthValue - 1]} ${date.year}"

/** Sin el año: en un tramo del mismo periodo, repetirlo tres veces es ruido. */
private fun fechaCorta(date: LocalDate): String =
    "${date.dayOfMonth} ${MesesCortos[date.monthValue - 1]}"

/** Redondeadas: «5 semanas» informa, «4,7 semanas» no. Nunca menos de una. */
private fun semanasEntre(desde: LocalDate, hasta: LocalDate): Long =
    (ChronoUnit.DAYS.between(desde, hasta) / 7).coerceAtLeast(1)
