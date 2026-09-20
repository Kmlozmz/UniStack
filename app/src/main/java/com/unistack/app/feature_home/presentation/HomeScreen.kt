@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_home.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import com.unistack.app.feature_terms.presentation.AvisosDelHistorico
import com.unistack.app.feature_terms.presentation.HojaDelAvisoParaEmpezar
import com.unistack.app.feature_terms.presentation.InicioConPeriodoPorEmpezar
import com.unistack.app.feature_terms.presentation.InicioSinPeriodo
import com.unistack.app.feature_terms.presentation.TermsUiState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.res.pluralStringResource
import com.unistack.app.feature_grades.presentation.SubjectMark
import com.unistack.app.feature_grades.presentation.subjectAccent
import com.unistack.app.feature_user.domain.HomeSection
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.feature_profile.presentation.AccountAvatar
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.core.design.components.UniStackButtonDefaults
import com.unistack.app.core.design.components.UniStackFabMenu
import com.unistack.app.core.design.theme.duracion
import com.unistack.app.core.design.theme.hayMovimiento
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.components.UniStackLogoMark
import com.unistack.app.core.design.components.UniStackWordmark
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.notifications.NotificationHistoryStore
import com.unistack.app.feature_home.domain.HomePriorityAction
import com.unistack.app.feature_home.domain.HomeSummary
import com.unistack.app.feature_home.domain.HomeTimelineKind
import com.unistack.app.feature_home.domain.HomeTimelineState
import com.unistack.app.feature_home.domain.HomeTimelineSummary
import com.unistack.app.feature_home.domain.HomeUpcomingItem
import com.unistack.app.feature_home.domain.SubjectRiskSeverity
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import com.unistack.app.core.design.components.SaludoAnimado
import com.unistack.app.core.design.components.numeroQueCuenta
import com.unistack.app.core.utils.greetingForNow
import com.unistack.app.core.utils.formatCurrency
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.roundToInt
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch
import com.unistack.app.core.utils.GradingScaleUtils
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.rounded.AutoAwesome
import com.unistack.app.core.design.components.floatingOffset
import com.unistack.app.feature_home.domain.HomePriorityTimeframe
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.unistack.app.R

private val SpanishLocale: Locale = Locale.forLanguageTag("es")

/**
 * Inicio.
 *
 * Cuatro bloques, en este orden y sin posibilidad de reordenarlos: quién eres, lo primero que
 * tienes que hacer hoy, qué hay hoy, y tres cifras. La versión anterior dejaba mover y rotar
 * estas piezas desde Apariencia, y el resultado era que la pantalla no tenía una forma: cada
 * quien veía una distinta y ninguna estaba diseñada.
 *
 * El panel lateral se abre desde el avatar, y solo lleva lo que no tiene otra puerta.
 *
 * Tenía dieciséis filas: seis repetían una pestaña de la barra de abajo, dos acababan en la
 * misma pantalla y tres estaban apagadas esperando a existir. Un menú que repite el menú de al
 * lado no ahorra un toque, solo obliga a leer el doble para descubrir que da igual cuál elijas.
 * Quedan ocho, y ninguna se alcanza de otra forma.
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAddSubjectClick: () -> Unit,
    onSeeAllSubjectsClick: () -> Unit,
    onSeeTasksClick: () -> Unit,
    onSeeExpensesClick: () -> Unit,
    onOpenTemplatesClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onAddGradeClick: (String) -> Unit,
    onAddTaskClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCalendarClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onWhatsNewClick: () -> Unit = {},
    onResourcesClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onGpaClick: () -> Unit = {},
    onQuickNotesClick: () -> Unit = {},
    onNoteClick: (String) -> Unit = {},
    onNewNoteClick: () -> Unit = {},
    onAiClick: () -> Unit = {},
    onLabsClick: () -> Unit = {},
    onDrawerOpenChange: (Boolean) -> Unit = {},
    /**
     * Los periodos, para los dos estados en que Inicio cambia de forma.
     *
     * Sin periodo activo y con alguno cerrado, Inicio cuenta cómo te fue y propone el siguiente.
     * Con uno creado que todavía no empieza, enseña la cuenta atrás. En cualquier otro caso es el
     * Inicio de siempre: cerrar no vacía la app, la cambia de estado.
     */
    historico: TermsUiState? = null,
    onStartNewTermClick: () -> Unit = {},
    onOpenHistoryClick: () -> Unit = {},
    onOpenTermClick: (String) -> Unit = {},
    onAcademicSettingsClick: () -> Unit = {},
    onNextTermReminderChange: (Boolean) -> Unit = {},
    onNextTermReminderDayChange: (Int) -> Unit = {},
    onUndoTermClose: (String, String) -> Unit = { _, _ -> }
) {
    var avisoAbierto by rememberSaveable { mutableStateOf(false) }
    val sinPeriodo = historico?.historial?.let { it.activo == null && it.cerrados.isNotEmpty() } == true
    val porEmpezar = historico?.historial?.activo?.porEmpezar == true
    val summary = uiState.summary
    val appearance = LocalAppearancePreferences.current
    val context = LocalContext.current
    val notifications by remember(context) {
        NotificationHistoryStore.observe(context)
    }.collectAsStateWithLifecycle()
    val hasUnread = notifications.any { !it.read }

    var pickingSubjectForGrade by rememberSaveable { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(drawerState.isOpen) { onDrawerOpenChange(drawerState.isOpen) }

    val closeAndRun: (() -> Unit) -> Unit = { action ->
        scope.launch { drawerState.close() }
        action()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeNavigationPanel(
                displayName = summary.userName,
                educationLine = summary.educationLine,
                photoUrl = summary.avatarPhotoUrl,
                onWorksClick = { closeAndRun(onOpenTemplatesClick) },
                onGpaClick = { closeAndRun(onGpaClick) },
                onQuickNotesClick = { closeAndRun(onQuickNotesClick) },
                onResourcesClick = { closeAndRun(onResourcesClick) },
                onWhatsNewClick = { closeAndRun(onWhatsNewClick) },
                onHelpClick = { closeAndRun(onHelpClick) },
                onAboutClick = { closeAndRun(onAboutClick) },
                onAiClick = { closeAndRun(onAiClick) },
                onLabsClick = { closeAndRun(onLabsClick) }
            )
        }
    ) {
        Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().statusBarsPadding(),
                // Lo que ocupa el boton de crear mas su margen, para que la ultima casilla no
                // quede debajo de el.
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item("cabecera") {
                    HomeHeader(
                        name = summary.userName.ifBlank { stringResource(R.string.home_header_greeting_default) },
                        photoUrl = summary.avatarPhotoUrl,
                        hasUnread = hasUnread,
                        onNotificationsClick = onNotificationsClick,
                        onAvatarClick = { scope.launch { drawerState.open() } }
                    )
                }

                if (sinPeriodo) {
                    item("sin-periodo") {
                        InicioSinPeriodo(
                            estado = historico,
                            onVerPeriodo = onOpenTermClick,
                            onEmpezar = onStartNewTermClick,
                            onHistorico = onOpenHistoryClick,
                            onAviso = { avisoAbierto = true },
                            onAvisoActivo = onNextTermReminderChange,
                            onTareas = onSeeTasksClick,
                            onNotas = onQuickNotesClick,
                            onGastos = onSeeExpensesClick
                        )
                    }
                } else if (porEmpezar) {
                    item("por-empezar") {
                        InicioConPeriodoPorEmpezar(
                            estado = historico,
                            onHorario = onCalendarClick,
                            onHistorico = onOpenHistoryClick
                        )
                    }
                }

                // En el orden de Apariencia › Tu inicio; cada bloque solo si está encendido.
                // La lista la arma [bloquesDeInicio], que es la misma que pinta la maqueta de
                // esa pantalla.
                if (!sinPeriodo && !porEmpezar) {
                    bloquesDeInicio(
                        summary = summary,
                        orden = appearance.homeSectionOrder.filter(appearance::showsSection),
                        acciones = AccionesDeInicio(
                            onAddSubject = onAddSubjectClick,
                            onSeeAllSubjects = onSeeAllSubjectsClick,
                            onSeeTasks = onSeeTasksClick,
                            onSeeExpenses = onSeeExpensesClick,
                            onOpenTemplates = onOpenTemplatesClick,
                            onCalendar = onCalendarClick,
                            onSubject = onSubjectClick,
                            onAddGrade = onAddGradeClick,
                            onQuickNotes = onQuickNotesClick,
                            onNote = onNoteClick,
                            onPickSubjectForGrade = { pickingSubjectForGrade = true }
                        )
                    ).forEach { bloque -> item(bloque.clave) { bloque.contenido() } }
                }
            }

            UniStackFabMenu(
                // En la esquina, como el botón de pruebas al otro lado (14dp del borde y de la
                // barra). El menú de Material ya trae 16dp de aire por dentro, y de ahí el
                // desplazamiento (16→14 a los lados, 20→14 abajo) para igualarlo.
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 6.dp),
                onAddGradeClick = {
                    val subjects = summary.subjects
                    when {
                        subjects.isEmpty() -> onAddSubjectClick()
                        subjects.size == 1 -> onAddGradeClick(subjects.first().id)
                        else -> pickingSubjectForGrade = true
                    }
                },
                onAddNoteClick = onNewNoteClick,
                showAddNote = true,
                onAddTaskClick = onAddTaskClick,
                onAddExpenseClick = onAddExpenseClick,
                onAddSubjectClick = onAddSubjectClick,
                showAddGrade = AppModule.GRADES in summary.enabledModules,
                showAddTask = AppModule.TASKS in summary.enabledModules,
                showAddExpense = AppModule.EXPENSES in summary.enabledModules,
                showAddSubject = false
            )
            AvisosDelHistorico(margenAbajo = 88.dp, onDeshacerCierre = onUndoTermClose)
        }
    }

    if (avisoAbierto && historico != null) {
        HojaDelAvisoParaEmpezar(
            estado = historico,
            onDismiss = { avisoAbierto = false },
            onActivo = onNextTermReminderChange,
            onOpcion = onNextTermReminderDayChange,
            onAbrirConfiguracion = {
                avisoAbierto = false
                onAcademicSettingsClick()
            }
        )
    }

    if (pickingSubjectForGrade) {
        SubjectPickerSheet(
            subjects = summary.subjects,
            onDismiss = { pickingSubjectForGrade = false },
            onSelected = { id ->
                pickingSubjectForGrade = false
                onAddGradeClick(id)
            }
        )
    }
}

/**
 * La marca arriba y el saludo debajo.
 *
 * Eran dos líneas en una esquina y no se leían como la cabecera de nada: el saludo y el nombre
 * pesaban casi lo mismo, y de la app no quedaba ni el nombre. Ahora hay dos alturas. La de
 * arriba es de la app —el logo y la palabra en el centro, los avisos y el perfil a la
 * derecha—; la de abajo es tuya, con el saludo en versales del acento y el nombre en grande,
 * que es el mismo salto que usa la tarjeta de «PRÓXIMA CLASE» y el héroe de aquí al lado.
 */
@Composable
private fun HomeHeader(
    name: String,
    photoUrl: String?,
    hasUnread: Boolean,
    onNotificationsClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(start = 20.dp, end = 12.dp)
        ) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UniStackLogoMark(size = 22.dp)
                UniStackWordmark(fontSize = 18.sp)
            }

            // El perfil a la izquierda y los avisos a la derecha, uno a cada lado de la
            // marca. Con los dos a la derecha, la mitad izquierda de la barra quedaba vacía
            // y el centro dejaba de leerse como centro: parecía un hueco al lado del nombre.
            // Y el panel se abre desde la izquierda, que es de donde ahora sale su botón.
            /*
             * El mismo violeta que el avatar en todas partes.
             *
             * Iba en `tertiaryContainer`, un color que no pinta ningún otro retrato de la app
             * -Ajustes, el panel lateral, Cuenta y perfil usan todos `primaryContainer`-, y era
             * porque este botón se dibujaba a mano en vez de apoyarse en `AccountAvatar`. Con
             * el componente compartido, el color deja de poder desviarse.
             */
            Surface(
                onClick = onAvatarClick,
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.align(Alignment.CenterStart).size(38.dp)
            ) {
                AccountAvatar(
                    photoUrl = photoUrl,
                    contentDescription = stringResource(R.string.profile_picture),
                    initial = name.take(1).uppercase(Locale.getDefault()),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier.align(Alignment.CenterEnd),
                contentAlignment = Alignment.TopEnd
            ) {
                UniIconButton(
                    icon = Icons.Rounded.NotificationsNone,
                    contentDescription = stringResource(R.string.settings_notifications),
                    onClick = onNotificationsClick
                )
                if (hasUnread) {
                    Box(
                        modifier = Modifier
                            .offset(x = (-10).dp, y = 10.dp)
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                    )
                }
            }
        }

        /*
         * El saludo, con la variante que se haya elegido en Movimiento.
         *
         * Es lo primero que se ve al abrir la app, asi que es donde mas se nota: por eso tiene
         * siete variantes propias en vez de heredar la entrada generica de las listas.
         */
        SaludoAnimado(
            rotulo = saludoDeInicio().uppercase(Locale.getDefault()),
            nombre = name,
            estiloRotulo = SectionLabelStyle,
            estiloNombre = MaterialTheme.typography.headlineLargeEmphasized,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 10.dp, bottom = 10.dp)
        )
    }
}

/**
 * Lo primero de hoy.
 *
 * El rótulo sale de la acción que trae la prioridad, no de olfatear su propio texto. Antes se
 * deducía por palabras clave, así que reescribir el mensaje podía cambiar el botón sin querer.
 */
@Composable
private fun HomePriorityCard(
    summary: HomeSummary,
    onPrimaryAction: () -> Unit,
    onSubjectClick: (String) -> Unit
) {
    val priority = summary.priority
    val subjectId = priority.subjectId

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Box {
            /*
             * Los adornos del hero: dos círculos a la deriva y dos destellos.
             *
             * Los círculos se salen del recuadro a propósito —el recorte de la tarjeta los
             * corta— y eso es lo que les da la sensación de estar detrás de ella. Los
             * destellos giran y laten despacio sobre ellos, nunca sobre el texto.
             *
             * Antes de esto había aquí un polígono de nueve lóbulos recortando una caja, y
             * salía un rectángulo: la forma no llegaba a aplicarse y lo que se veía era el
             * bloque sin recortar. Se dibuja con Canvas, que no depende de recortar nada.
             */
            val ornament = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.07f)
            val star = MaterialTheme.colorScheme.onPrimaryContainer
            val circleBigFloat = floatingOffset(travel = 5f, durationMillis = 4200, label = "home-hero-circle-big")
            val circleSmallFloat = floatingOffset(travel = 3.5f, durationMillis = 5600, label = "home-hero-circle-small")
            val sparkleBig = floatingOffset(travel = 0.22f, durationMillis = 1500, label = "home-hero-sparkle-big")
            val sparkleSmall = floatingOffset(travel = 0.28f, durationMillis = 1900, label = "home-hero-sparkle-small")

            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = ornament,
                    radius = size.height * 0.42f,
                    center = Offset(
                        x = size.width * 0.92f,
                        y = size.height * 0.02f + circleBigFloat.dp.toPx()
                    )
                )
                drawCircle(
                    color = ornament,
                    radius = size.height * 0.26f,
                    center = Offset(
                        x = size.width * 0.80f,
                        y = size.height * 1.02f + circleSmallFloat.dp.toPx()
                    )
                )
            }

            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = star.copy(alpha = 0.62f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-26).dp, y = 30.dp)
                    .graphicsLayer {
                        scaleX = 1f + sparkleBig
                        scaleY = 1f + sparkleBig
                        rotationZ = sparkleBig * 45f
                        alpha = 0.72f + sparkleBig
                    }
                    .size(18.dp)
            )
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = star.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-54).dp, y = 54.dp)
                    .graphicsLayer {
                        scaleX = 1f + sparkleSmall
                        scaleY = 1f + sparkleSmall
                        rotationZ = -sparkleSmall * 55f
                        alpha = 0.66f + sparkleSmall
                    }
                    .size(10.dp)
            )

            Column(
                modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PuntoDelHero(enCurso = priority.timeframe == HomePriorityTimeframe.NOW)
                    Text(text = heroLabel(priority.timeframe), style = SectionLabelStyle)
                }
                Text(
                    text = priority.title,
                    style = MaterialTheme.typography.headlineSmallEmphasized
                )
                Text(
                    text = priority.shortDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.86f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Button(
                        shapes = UniStackButtonDefaults.shapes,
                        onClick = onPrimaryAction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        // Un «+» delante de «Ver horario» prometia crear algo. El icono va con la
                        // accion, no fijo.
                        Icon(
                            imageVector = if (priority.action == HomePriorityAction.SCHEDULE) Icons.Rounded.Schedule else Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text(primaryActionLabel(priority.action))
                    }
                    if (subjectId != null) {
                        TextButton(onClick = { onSubjectClick(subjectId) }) {
                            Text(stringResource(R.string.home_view_subject), color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }
    }
}

/**
 * La luz del rotulo del hero.
 *
 * Con una clase en curso se pone en verde y late, con el mismo compas que la pildora «CLASE EN
 * CURSO» del horario: es la misma cosa dicha en dos sitios, y tiene que moverse igual. El resto
 * del tiempo es el punto quieto de siempre.
 */
@Composable
private fun PuntoDelHero(enCurso: Boolean) {
    val verde = LocalSectionColors.current.onTrack
    val late = enCurso && hayMovimiento()
    val ciclo = rememberInfiniteTransition(label = "hero-punto")
    val t by ciclo.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(duracion(1950).coerceAtLeast(1), easing = LinearEasing)),
        label = "hero-punto"
    )
    val escala = if (late) 1f + 0.7f * abs(sin(t * PI.toFloat())) else 1f
    val opacidad = if (late) 1f - 0.45f * abs(sin(t * PI.toFloat())) else 1f
    Box(
        modifier = Modifier
            .size(8.dp)
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
                alpha = opacidad
            }
            .clip(CircleShape)
            .background(if (enCurso) verde else MaterialTheme.colorScheme.onPrimaryContainer)
    )
}

@Composable
private fun HomeSectionHeader(title: String, actionLabel: String, onActionClick: () -> Unit) {
    // 18dp arriba dejaban un hueco: la fila ya mide 40dp por el botón de texto, y el título
    // va centrado en ellos, así que a la separación de arriba se le sumaban seis puntos de
    // centrado más el hombro de la letra. Entre el hero y este título se abría un vacío que
    // no se correspondía con ninguna otra separación de la pantalla.
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onActionClick) { Text(actionLabel) }
    }
}

/**
 * Lo de hoy: hora, un riel del color de lo que sea, y el detalle.
 *
 * Sin nada en el horario de hoy, la tarjeta decía siempre lo mismo -- «Hoy no tienes nada
 * puesto» -- mirando solo las horas de hoy. Eso daba una falsa sensación de calma: una tarea
 * vencida de la semana pasada, un trabajo por entregar el viernes o una materia que se está
 * cayendo no pasan por el horario de hoy, y la tarjeta los tapaba con un mensaje tranquilo.
 *
 * Ahora, vacía de horario, mira el resto de señales que ya calcula [HomeSummary] antes de
 * decir que todo está en calma -- en el mismo orden de urgencia que usa el resto de Inicio:
 * tareas vencidas primero, luego una materia en riesgo, luego lo próximo que vence. Solo si
 * ninguna de esas existe se enseña el mensaje tranquilo, y entonces sí lo es de verdad.
 */
@Composable
private fun HomeTodayCard(
    summary: HomeSummary,
    onEmptyClick: () -> Unit,
    onTasksClick: () -> Unit,
    onWorksClick: () -> Unit,
    onSubjectClick: (String) -> Unit
) {
    val sections = LocalSectionColors.current
    val items = summary.todayItems

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        if (items.isNotEmpty()) {
            Column(modifier = Modifier.padding(6.dp)) {
                items.forEach { item -> HomeTodayRow(item) }
            }
        } else {
            val risk = summary.riskSubject?.takeIf { it.severity != SubjectRiskSeverity.STABLE }
            val fallback = when {
                summary.overdueTasks > 0 -> HomeTodayFallback(
                    title = if (summary.overdueTasks == 1) stringResource(R.string.home_today_overdue_single) else stringResource(R.string.home_today_overdue_multiple, summary.overdueTasks),
                    detail = stringResource(R.string.home_today_overdue_detail),
                    actionLabel = stringResource(R.string.home_today_view_tasks),
                    tint = MaterialTheme.colorScheme.error,
                    onClick = onTasksClick
                )
                risk != null -> HomeTodayFallback(
                    title = risk.subjectName,
                    detail = risk.detail,
                    actionLabel = stringResource(R.string.home_view_subject),
                    tint = if (risk.severity == SubjectRiskSeverity.CRITICAL) MaterialTheme.colorScheme.error else sections.atRisk,
                    onClick = { onSubjectClick(risk.subjectId) }
                )
                summary.nextTask != null -> HomeTodayFallback(
                    title = summary.nextTask.title,
                    detail = stringResource(R.string.home_today_due_format, summary.nextTask.dueText),
                    actionLabel = stringResource(R.string.home_today_view_tasks),
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = onTasksClick
                )
                summary.nextAcademicWork != null -> HomeTodayFallback(
                    title = summary.nextAcademicWork.title,
                    detail = stringResource(R.string.home_today_due_format, summary.nextAcademicWork.dueText),
                    actionLabel = stringResource(R.string.home_today_view_works),
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = onWorksClick
                )
                else -> null
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (fallback != null) {
                    Text(
                        fallback.title,
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        color = fallback.tint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        fallback.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = fallback.onClick, contentPadding = PaddingValues(0.dp)) { Text(fallback.actionLabel) }
                } else {
                    Text(
                        text = if (summary.upcomingItems.isEmpty()) {
                            stringResource(R.string.home_today_calm_title)
                        } else {
                            stringResource(R.string.home_today_empty_title)
                        },
                        style = MaterialTheme.typography.titleSmallEmphasized
                    )
                    Text(
                        text = if (summary.upcomingItems.isEmpty()) {
                            stringResource(R.string.home_today_calm_detail)
                        } else {
                            stringResource(R.string.home_today_empty_detail)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (summary.upcomingItems.isEmpty()) {
                        TextButton(onClick = onEmptyClick, contentPadding = PaddingValues(0.dp)) { Text(stringResource(R.string.home_today_open_schedule)) }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            summary.upcomingItems.forEach { item -> HomeUpcomingRow(item) }
                        }
                        TextButton(onClick = onEmptyClick, contentPadding = PaddingValues(0.dp)) { Text(stringResource(R.string.home_today_see_full_schedule)) }
                    }
                }
            }
        }
    }
}

private data class HomeTodayFallback(
    val title: String,
    val detail: String,
    val actionLabel: String,
    val tint: Color,
    val onClick: () -> Unit
)

/** Una parada de los próximos días, dentro de la tarjeta de hoy. */
@Composable
private fun HomeUpcomingRow(item: HomeUpcomingItem) {
    val sections = LocalSectionColors.current
    val accent = when (item.kind) {
        HomeTimelineKind.CLASS -> sections.schedule
        HomeTimelineKind.TASK, HomeTimelineKind.WORK -> sections.atRisk
        HomeTimelineKind.EXAM -> MaterialTheme.colorScheme.primary
        HomeTimelineKind.FOCUS -> MaterialTheme.colorScheme.tertiary
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 30.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = listOf(item.dayLabel, item.timeText, item.subtitle)
                    .filter { it.isNotBlank() }
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HomeTodayRow(item: HomeTimelineSummary) {
    val sections = LocalSectionColors.current
    val rail = when {
        item.state == HomeTimelineState.DONE -> MaterialTheme.colorScheme.outlineVariant
        item.kind == HomeTimelineKind.CLASS -> sections.schedule
        item.kind == HomeTimelineKind.EXAM -> MaterialTheme.colorScheme.primary
        item.kind == HomeTimelineKind.TASK || item.kind == HomeTimelineKind.WORK -> sections.atRisk
        else -> MaterialTheme.colorScheme.tertiary
    }
    val timeColor = if (item.kind == HomeTimelineKind.TASK || item.kind == HomeTimelineKind.WORK) {
        sections.atRisk
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = item.timeText,
            style = MaterialTheme.typography.labelLargeEmphasized,
            color = timeColor,
            modifier = Modifier.width(46.dp)
        )
        Box(modifier = Modifier.width(4.dp).height(34.dp).clip(CircleShape).background(rail))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.subtitle.isNotBlank()) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.kind == HomeTimelineKind.TASK) sections.atRisk else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (item.state == HomeTimelineState.CURRENT) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = sections.scheduleContainer,
                contentColor = sections.onScheduleContainer
            ) {
                Text(
                    text = stringResource(R.string.home_badge_now),
                    style = SectionLabelStyle,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Promedio, pendientes y gasto de la semana, en tres casillas del mismo tamaño.
 *
 * Antes cada una medía lo que midiera su contenido, así que la de gasto -- sin pie -- quedaba
 * más baja que las otras dos y la fila entera se veía descuadrada. Ahora las tres comparten
 * altura y reparten su contenido igual: rótulo arriba, cifra en medio y pie abajo, cada uno en
 * su sitio aunque lo que lleve dentro cambie de tamaño.
 */
@Composable
private fun HomeSnapshotRow(
    summary: HomeSummary,
    onAverageClick: () -> Unit,
    onPendingClick: () -> Unit,
    onExpensesClick: () -> Unit
) {
    val sections = LocalSectionColors.current
    val modules = summary.enabledModules
    val scale = summary.gradingScale

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 14.dp, bottom = 4.dp)
            // La altura la marca la casilla más alta, y las tres se estiran hasta ella. Es lo
            // que mantiene la fila cuadrada sin fijar una altura a ojo que se rompa en cuanto
            // el texto crezca por accesibilidad.
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (AppModule.GRADES in modules) {
            HomeTile(
                label = stringResource(R.string.home_tile_average),
                // «Numeros que cuentan»: el promedio sube desde cero al abrir Inicio. Se
                // cuenta sobre el numero y se formatea despues, porque contar sobre el texto
                // ya formateado daria cifras imposibles a mitad de camino.
                value = summary.generalAverage
                    ?.let { GradingScaleUtils.formatGrade(numeroQueCuenta(it.toFloat(), "promedio").toDouble(), scale) }
                    ?: "—",
                footerText = stringResource(R.string.home_tile_average_out_of, GradingScaleUtils.formatGrade(GradingScaleUtils.maxGradeFor(scale), scale)),
                onClick = onAverageClick,
                modifier = Modifier.weight(1f)
            )
        }
        if (AppModule.TASKS in modules) {
            val overdue = summary.overdueTasks
            val pending = summary.pendingTasks
            HomeTile(
                label = stringResource(R.string.home_tile_pending),
                value = numeroQueCuenta(pending.toFloat(), "pendientes").toInt().toString(),
                onClick = onPendingClick,
                modifier = Modifier.weight(1f),
                footer = {
                    HomeStatusPill(
                        text = when {
                            overdue > 0 -> if (overdue == 1) stringResource(R.string.home_status_overdue_single) else stringResource(R.string.home_status_overdue_multiple, overdue)
                            pending > 0 -> stringResource(R.string.home_status_upcoming)
                            else -> stringResource(R.string.home_status_all_caught_up)
                        },
                        ink = when {
                            overdue > 0 -> MaterialTheme.colorScheme.onErrorContainer
                            pending > 0 -> sections.onAtRiskContainer
                            else -> sections.onOnTrackContainer
                        },
                        container = when {
                            overdue > 0 -> MaterialTheme.colorScheme.errorContainer
                            pending > 0 -> sections.atRiskContainer
                            else -> sections.onTrackContainer
                        }
                    )
                }
            )
        }
        if (AppModule.EXPENSES in modules) {
            val anterior = summary.previousWeekExpenseTotal
            val actual = summary.weeklyExpenseTotal
            HomeTile(
                label = stringResource(R.string.home_tile_this_week),
                value = formatCurrency(numeroQueCuenta(actual.toFloat(), "semana").toInt()),
                valueColor = sections.expenses,
                // El pie compara con la semana pasada, que es lo unico que hace que la cifra
                // signifique algo. Decia «en 1 dia», que sonaba a reproche y no ayudaba a
                // decidir nada.
                footerText = when {
                    anterior > 0 -> {
                        val cambio = ((actual - anterior) * 100.0 / anterior).roundToInt()
                        when {
                            cambio > 0 -> stringResource(R.string.home_expense_vs_previous_plus, cambio)
                            cambio < 0 -> stringResource(R.string.home_expense_vs_previous, cambio)
                            else -> stringResource(R.string.home_expense_same_as_previous)
                        }
                    }
                    actual > 0 -> stringResource(R.string.home_expense_first_week)
                    else -> stringResource(R.string.home_expense_no_expenses_yet)
                },
                onClick = onExpensesClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** La etiqueta de estado del pie de «Pendientes». */
@Composable
private fun HomeStatusPill(text: String, ink: Color, container: Color) {
    Surface(shape = MaterialTheme.shapes.small, color = container) {
        Text(
            text = text,
            color = ink,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun HomeTile(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.Unspecified,
    /** Un pie de texto normal, que es lo que llevan casi todas. */
    footerText: String? = null,
    /** O un pie con forma propia, como la etiqueta de estado de «Pendientes». */
    footer: (@Composable () -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 13.dp),
            // El rótulo arriba, la cifra en medio y el pie abajo del todo. Repartir así hace
            // que las tres casillas alineen sus tres partes entre ellas, aunque una lleve una
            // etiqueta y otra una línea de texto.
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = SectionLabelStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            // Se encoge sola hasta que quepa entera.
            //
            // «$22.400» no cabe a 24sp en un tercio de pantalla, asi que se cortaba y se leia
            // «$22.40»: un numero distinto y creible, que es la peor forma de cortar un texto.
            // Con autoSize baja de tamaño lo justo y nunca miente.
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 15.sp,
                    maxFontSize = MaterialTheme.typography.headlineSmallEmphasized.fontSize,
                    stepSize = 0.5.sp
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            )
            when {
                footer != null -> footer()
                footerText != null -> Text(
                    text = footerText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * El rótulo del hero, según para cuándo sea lo que anuncia.
 *
 * Antes ponía «lo primero de hoy» siempre, y con la próxima clase a dos días vista eso era
 * sencillamente falso.
 */
@Composable
private fun heroLabel(timeframe: HomePriorityTimeframe): String = when (timeframe) {
    HomePriorityTimeframe.NOW -> stringResource(R.string.home_hero_now)
    HomePriorityTimeframe.TODAY -> stringResource(R.string.home_hero_today)
    HomePriorityTimeframe.TOMORROW -> stringResource(R.string.home_hero_tomorrow)
    HomePriorityTimeframe.LATER -> stringResource(R.string.home_hero_later)
}

@Composable
private fun primaryActionLabel(action: HomePriorityAction): String = when (action) {
    HomePriorityAction.SUBJECT -> stringResource(R.string.home_action_grade)
    HomePriorityAction.SUBJECTS -> stringResource(R.string.home_action_subjects)
    HomePriorityAction.TASKS -> stringResource(R.string.home_action_tasks)
    HomePriorityAction.EXPENSES -> stringResource(R.string.home_action_expenses)
    HomePriorityAction.TEMPLATES -> stringResource(R.string.home_action_templates)
    HomePriorityAction.SCHEDULE -> stringResource(R.string.home_action_schedule)
}

@Composable
private fun todayLabel(): String {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE d", Locale.getDefault())
    val formatted = today.format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    return stringResource(R.string.home_today_prefix, formatted)
}

// ------------------------------------------------------------------ los bloques, en orden

/** A dónde lleva cada bloque. La maqueta de Apariencia los pinta con todo apagado. */
class AccionesDeInicio(
    val onAddSubject: () -> Unit = {},
    val onSeeAllSubjects: () -> Unit = {},
    val onSeeTasks: () -> Unit = {},
    val onSeeExpenses: () -> Unit = {},
    val onOpenTemplates: () -> Unit = {},
    val onCalendar: () -> Unit = {},
    val onSubject: (String) -> Unit = {},
    val onAddGrade: (String) -> Unit = {},
    val onQuickNotes: () -> Unit = {},
    val onNote: (String) -> Unit = {},
    val onPickSubjectForGrade: () -> Unit = {}
)

/** Una pieza de Inicio: su clave estable para la lista perezosa y lo que pinta. */
class BloqueDeInicio(val clave: String, val contenido: @Composable () -> Unit)

/**
 * Los bloques de Inicio, en el orden que se pida, como piezas sueltas.
 *
 * Es una lista y no un `LazyListScope` a propósito: Inicio los mete en su lista perezosa y la
 * maqueta de Apariencia › Tu inicio los apila en una columna a escala. Los dos pintan las
 * mismas tarjetas con los mismos datos, que es lo que hace a la maqueta fiel.
 */
fun bloquesDeInicio(
    summary: HomeSummary,
    orden: List<HomeSection>,
    acciones: AccionesDeInicio
): List<BloqueDeInicio> = buildList {
    fun bloque(clave: String, contenido: @Composable () -> Unit) = add(BloqueDeInicio(clave, contenido))
    orden.forEach { seccion ->
        when (seccion) {
            HomeSection.HERO -> bloque("prioridad") {
                HomePriorityCard(
                    summary = summary,
                    onPrimaryAction = {
                        val subjects = summary.subjects
                        when (summary.priority.action) {
                            HomePriorityAction.SUBJECT -> when {
                                subjects.isEmpty() -> acciones.onAddSubject()
                                summary.priority.subjectId != null -> acciones.onAddGrade(summary.priority.subjectId!!)
                                subjects.size == 1 -> acciones.onAddGrade(subjects.first().id)
                                else -> acciones.onPickSubjectForGrade()
                            }
                            HomePriorityAction.SUBJECTS -> acciones.onSeeAllSubjects()
                            HomePriorityAction.TASKS -> acciones.onSeeTasks()
                            HomePriorityAction.EXPENSES -> acciones.onSeeExpenses()
                            HomePriorityAction.TEMPLATES -> acciones.onOpenTemplates()
                            HomePriorityAction.SCHEDULE -> acciones.onCalendar()
                        }
                    },
                    onSubjectClick = acciones.onSubject
                )
            }

            HomeSection.AGENDA -> {
                bloque("hoy-cabecera") {
                    HomeSectionHeader(
                        title = todayLabel(),
                        actionLabel = stringResource(R.string.home_action_schedule_short),
                        onActionClick = acciones.onCalendar
                    )
                }
                bloque("hoy") {
                    HomeTodayCard(
                        summary = summary,
                        onEmptyClick = acciones.onCalendar,
                        onTasksClick = acciones.onSeeTasks,
                        onWorksClick = acciones.onOpenTemplates,
                        onSubjectClick = acciones.onSubject
                    )
                }
            }

            HomeSection.SNAPSHOT -> bloque("cifras") {
                HomeSnapshotRow(
                    summary = summary,
                    onAverageClick = acciones.onSeeAllSubjects,
                    onPendingClick = acciones.onSeeTasks,
                    onExpensesClick = acciones.onSeeExpenses
                )
            }

            HomeSection.SUBJECTS -> if (AppModule.GRADES in summary.enabledModules) {
                bloque("materias-cabecera") {
                    HomeSectionHeader(
                        title = stringResource(R.string.home_block_subjects),
                        actionLabel = stringResource(R.string.home_block_see_all),
                        onActionClick = acciones.onSeeAllSubjects
                    )
                }
                bloque("materias") {
                    HomeSubjectsCard(summary = summary, onSubjectClick = acciones.onSubject, onEmptyClick = acciones.onAddSubject)
                }
            }

            HomeSection.WEEK -> if (AppModule.TASKS in summary.enabledModules) {
                bloque("semana-cabecera") {
                    HomeSectionHeader(
                        title = stringResource(R.string.home_block_week),
                        actionLabel = stringResource(R.string.home_today_view_tasks),
                        onActionClick = acciones.onSeeTasks
                    )
                }
                bloque("semana") { HomeWeekCard(summary = summary, onClick = acciones.onSeeTasks) }
            }

            HomeSection.ATTENDANCE -> if (AppModule.GRADES in summary.enabledModules) {
                bloque("asistencia-cabecera") {
                    HomeSectionHeader(
                        title = stringResource(R.string.home_block_attendance),
                        actionLabel = stringResource(R.string.home_action_schedule_short),
                        onActionClick = acciones.onCalendar
                    )
                }
                bloque("asistencia") {
                    HomeAttendanceCard(summary = summary, onSubjectClick = acciones.onSubject, onEmptyClick = acciones.onCalendar)
                }
            }

            HomeSection.EXPENSES -> if (AppModule.EXPENSES in summary.enabledModules) {
                bloque("gastos-cabecera") {
                    HomeSectionHeader(
                        title = stringResource(R.string.home_block_expenses),
                        actionLabel = stringResource(R.string.home_block_see_expenses),
                        onActionClick = acciones.onSeeExpenses
                    )
                }
                bloque("gastos") { HomeExpensesCard(summary = summary, onClick = acciones.onSeeExpenses) }
            }

            HomeSection.NOTES -> {
                bloque("notas-cabecera") {
                    HomeSectionHeader(
                        title = stringResource(R.string.home_block_notes),
                        actionLabel = stringResource(R.string.home_block_see_notes),
                        onActionClick = acciones.onQuickNotes
                    )
                }
                bloque("notas") {
                    HomeNotesCard(summary = summary, onNoteClick = acciones.onNote, onEmptyClick = acciones.onQuickNotes)
                }
            }
        }
    }
}

// ------------------------------------------------------------------ los bloques opcionales

/**
 * El saludo de Inicio: «Buenas tardes» según la hora o un «Hola» a secas, como se haya
 * elegido en Apariencia › Tu inicio.
 */
@Composable
fun saludoDeInicio(): String {
    return if (LocalAppearancePreferences.current.greetingWithTimeOfDay) {
        greetingForNow()
    } else {
        stringResource(R.string.home_header_greeting_default)
    }
}

/** La tarjeta base de los bloques opcionales: el mismo lienzo que «Hoy». */
@Composable
private fun TarjetaDeBloque(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(6.dp), content = content)
    }
}

/** Una fila tocable dentro de un bloque, con el aire de las de «Hoy». */
@Composable
private fun FilaDeBloque(onClick: () -> Unit, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

/** Lo que dice un bloque cuando no tiene nada: una línea y a dónde ir. */
@Composable
private fun BloqueVacio(texto: String, onClick: () -> Unit) {
    FilaDeBloque(onClick = onClick) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
        )
    }
}

/** Tu lista de materias con el promedio de cada una. */
@Composable
private fun HomeSubjectsCard(summary: HomeSummary, onSubjectClick: (String) -> Unit, onEmptyClick: () -> Unit) {
    TarjetaDeBloque {
        if (summary.subjects.isEmpty()) {
            BloqueVacio(stringResource(R.string.home_block_subjects_empty), onEmptyClick)
            return@TarjetaDeBloque
        }
        summary.subjects.forEach { subject ->
            FilaDeBloque(onClick = { onSubjectClick(subject.id) }) {
                SubjectMark(
                    letter = subject.name.take(1).uppercase(),
                    color = subjectAccent(subject.type),
                    seed = subject.id,
                    markSize = 32.dp
                )
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = subject.average?.let { GradingScaleUtils.formatGrade(it, summary.gradingScale) } ?: "—",
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = if (subject.average != null && subject.average < subject.targetAverage) LocalSectionColors.current.atRisk else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/** Las entregas de los próximos siete días. */
@Composable
private fun HomeWeekCard(summary: HomeSummary, onClick: () -> Unit) {
    TarjetaDeBloque {
        if (summary.weekDeliveries.isEmpty()) {
            BloqueVacio(stringResource(R.string.home_block_week_empty), onClick)
            return@TarjetaDeBloque
        }
        summary.weekDeliveries.forEach { item ->
            FilaDeBloque(onClick = onClick) { HomeUpcomingRow(item) }
        }
    }
}

/** Las faltas que te quedan en cada materia. */
@Composable
private fun HomeAttendanceCard(summary: HomeSummary, onSubjectClick: (String) -> Unit, onEmptyClick: () -> Unit) {
    val sections = LocalSectionColors.current
    TarjetaDeBloque {
        if (summary.attendanceLines.isEmpty()) {
            BloqueVacio(stringResource(R.string.home_block_attendance_empty), onEmptyClick)
            return@TarjetaDeBloque
        }
        summary.attendanceLines.forEach { line ->
            val restantes = line.remaining
            val apurada = restantes != null && restantes <= 1
            FilaDeBloque(onClick = { onSubjectClick(line.subjectId) }) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = line.subjectName,
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = when {
                            restantes == null -> pluralStringResource(R.plurals.home_block_absences, line.absent, line.absent)
                            restantes == 0 -> stringResource(R.string.home_block_absences_none_left)
                            else -> pluralStringResource(R.plurals.home_block_absences_left, restantes, restantes)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (apurada) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = if (line.limit != null) "${line.absent}/${line.limit}" else line.absent.toString(),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = if (apurada) MaterialTheme.colorScheme.error else if (line.absent == 0) sections.onTrack else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/** Lo que llevas gastado esta semana y el presupuesto. */
@Composable
private fun HomeExpensesCard(summary: HomeSummary, onClick: () -> Unit) {
    val sections = LocalSectionColors.current
    val gastado = summary.weeklyExpenseTotal
    val presupuesto = summary.weeklyBudget
    val pasado = presupuesto > 0 && gastado > presupuesto
    TarjetaDeBloque {
        FilaDeBloque(onClick = onClick) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = formatCurrency(gastado),
                        style = MaterialTheme.typography.headlineSmallEmphasized,
                        color = if (pasado) MaterialTheme.colorScheme.error else sections.expenses
                    )
                    Text(
                        text = if (presupuesto > 0) {
                            stringResource(R.string.home_block_expenses_of, formatCurrency(presupuesto))
                        } else {
                            stringResource(R.string.home_block_expenses_no_budget)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
                if (presupuesto > 0) {
                    LinearProgressIndicator(
                        progress = { (gastado.toFloat() / presupuesto).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (pasado) MaterialTheme.colorScheme.error else sections.expenses,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                    )
                }
            }
        }
    }
}

/** Las notas rápidas que dejes fijadas. */
@Composable
private fun HomeNotesCard(summary: HomeSummary, onNoteClick: (String) -> Unit, onEmptyClick: () -> Unit) {
    TarjetaDeBloque {
        if (summary.pinnedNotes.isEmpty()) {
            BloqueVacio(stringResource(R.string.home_block_notes_empty), onEmptyClick)
            return@TarjetaDeBloque
        }
        summary.pinnedNotes.forEach { note ->
            FilaDeBloque(onClick = { onNoteClick(note.id) }) {
                Icon(
                    imageVector = Icons.Rounded.PushPin,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title.ifBlank { stringResource(R.string.notes_this_note) },
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (note.preview.isNotBlank()) {
                        Text(
                            text = note.preview,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
