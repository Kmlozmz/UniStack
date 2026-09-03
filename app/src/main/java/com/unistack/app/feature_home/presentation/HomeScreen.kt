@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_home.presentation

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import com.unistack.app.feature_terms.presentation.NoActiveTermCard
import com.unistack.app.feature_terms.presentation.TermSummary
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.unistack.app.core.utils.greetingForNow
import com.unistack.app.core.utils.CurrencyFormatter
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
    onNewNoteClick: () -> Unit = {},
    onAiClick: () -> Unit = {},
    onLabsClick: () -> Unit = {},
    onDrawerOpenChange: (Boolean) -> Unit = {},
    /**
     * El último periodo cerrado, **solo cuando no hay ninguno activo**.
     *
     * Nulo es el caso normal: hay periodo en curso, o todavía no ha habido ninguno. Cuando
     * llega con valor, Inicio abre con el resumen de lo que se acaba de cerrar y una acción
     * única, porque cerrar no vacía la app: la cambia de estado.
     */
    noActiveTerm: TermSummary? = null,
    inheritedCutCount: Int = 0,
    onStartNewTermClick: () -> Unit = {},
    onOpenHistoryClick: () -> Unit = {}
) {
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
                        name = summary.userName.ifBlank { "Hola" },
                        photoUrl = summary.avatarPhotoUrl,
                        hasUnread = hasUnread,
                        onNotificationsClick = onNotificationsClick,
                        onAvatarClick = { scope.launch { drawerState.open() } }
                    )
                }

                noActiveTerm?.let { ultimo ->
                    item("sin-periodo") {
                        NoActiveTermCard(
                            lastClosed = ultimo,
                            cutCount = inheritedCutCount,
                            onStartNewTerm = onStartNewTermClick,
                            onOpenHistory = onOpenHistoryClick,
                            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                        )
                    }
                }

                if (appearance.showHomeHero) {
                    item("prioridad") {
                        HomePriorityCard(
                            summary = summary,
                            onPrimaryAction = {
                                val subjects = summary.subjects
                                when (summary.priority.action) {
                                    HomePriorityAction.SUBJECT -> when {
                                        subjects.isEmpty() -> onAddSubjectClick()
                                        summary.priority.subjectId != null ->
                                            onAddGradeClick(summary.priority.subjectId!!)
                                        subjects.size == 1 -> onAddGradeClick(subjects.first().id)
                                        else -> pickingSubjectForGrade = true
                                    }
                                    HomePriorityAction.SUBJECTS -> onSeeAllSubjectsClick()
                                    HomePriorityAction.TASKS -> onSeeTasksClick()
                                    HomePriorityAction.EXPENSES -> onSeeExpensesClick()
                                    HomePriorityAction.TEMPLATES -> onOpenTemplatesClick()
                                    HomePriorityAction.SCHEDULE -> onCalendarClick()
                                }
                            },
                            onSubjectClick = { id -> onSubjectClick(id) }
                        )
                    }
                }

                if (appearance.showHomeAgenda) {
                    item("hoy-cabecera") {
                        HomeSectionHeader(
                            title = todayLabel(),
                            actionLabel = "Horario",
                            onActionClick = onCalendarClick
                        )
                    }
                    item("hoy") {
                        HomeTodayCard(
                            summary = summary,
                            onEmptyClick = onCalendarClick,
                            onTasksClick = onSeeTasksClick,
                            onWorksClick = onOpenTemplatesClick,
                            onSubjectClick = onSubjectClick
                        )
                    }
                }

                if (appearance.showHomeSnapshot) {
                    item("cifras") {
                        HomeSnapshotRow(
                            summary = summary,
                            onAverageClick = onSeeAllSubjectsClick,
                            onPendingClick = onSeeTasksClick,
                            onExpensesClick = onSeeExpensesClick
                        )
                    }
                }
            }

            UniStackFabMenu(
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 20.dp),
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
        }
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
                    contentDescription = "Tu perfil",
                    initial = name.take(1).uppercase(SpanishLocale),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier.align(Alignment.CenterEnd),
                contentAlignment = Alignment.TopEnd
            ) {
                UniIconButton(
    icon = Icons.Rounded.NotificationsNone,
    contentDescription = "Avisos",
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
            rotulo = greetingForNow().uppercase(SpanishLocale),
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
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer)
                    )
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
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text(primaryActionLabel(priority.action))
                    }
                    if (subjectId != null) {
                        TextButton(onClick = { onSubjectClick(subjectId) }) {
                            Text("Ver materia", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }
    }
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
                    title = if (summary.overdueTasks == 1) "Tienes una tarea vencida" else "Tienes ${summary.overdueTasks} tareas vencidas",
                    detail = "No estaban en el horario de hoy, pero siguen sin cerrarse.",
                    actionLabel = "Ver tareas",
                    tint = MaterialTheme.colorScheme.error,
                    onClick = onTasksClick
                )
                risk != null -> HomeTodayFallback(
                    title = risk.subjectName,
                    detail = risk.detail,
                    actionLabel = "Ver materia",
                    tint = if (risk.severity == SubjectRiskSeverity.CRITICAL) MaterialTheme.colorScheme.error else sections.atRisk,
                    onClick = { onSubjectClick(risk.subjectId) }
                )
                summary.nextTask != null -> HomeTodayFallback(
                    title = summary.nextTask.title,
                    detail = "Vence " + summary.nextTask.dueText + ". No es de hoy, pero es lo próximo.",
                    actionLabel = "Ver tareas",
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = onTasksClick
                )
                summary.nextAcademicWork != null -> HomeTodayFallback(
                    title = summary.nextAcademicWork.title,
                    detail = "Vence " + summary.nextAcademicWork.dueText + ". No es de hoy, pero es lo próximo.",
                    actionLabel = "Ver trabajos",
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
                            "Hoy no tienes nada puesto, y vas al día con todo"
                        } else {
                            "Hoy no tienes nada puesto"
                        },
                        style = MaterialTheme.typography.titleSmallEmphasized
                    )
                    Text(
                        text = if (summary.upcomingItems.isEmpty()) {
                            "Ni clases ni entregas hoy, ni tareas vencidas, ni materias en riesgo."
                        } else {
                            "Nada vencido detrás. Esto es lo que viene:"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (summary.upcomingItems.isEmpty()) {
                        TextButton(onClick = onEmptyClick, contentPadding = PaddingValues(0.dp)) { Text("Abrir Horario") }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            summary.upcomingItems.forEach { item -> HomeUpcomingRow(item) }
                        }
                        TextButton(onClick = onEmptyClick, contentPadding = PaddingValues(0.dp)) { Text("Ver el horario completo") }
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
                    text = "AHORA",
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
                label = "PROMEDIO",
                value = summary.generalAverage?.let { GradingScaleUtils.formatGrade(it, scale) } ?: "—",
                footerText = "de " + GradingScaleUtils.formatGrade(GradingScaleUtils.maxGradeFor(scale), scale),
                onClick = onAverageClick,
                modifier = Modifier.weight(1f)
            )
        }
        if (AppModule.TASKS in modules) {
            val overdue = summary.overdueTasks
            val pending = summary.pendingTasks
            HomeTile(
                label = "PENDIENTES",
                value = pending.toString(),
                onClick = onPendingClick,
                modifier = Modifier.weight(1f),
                footer = {
                    HomeStatusPill(
                        text = when {
                            overdue > 0 -> if (overdue == 1) "1 vencida" else "$overdue vencidas"
                            pending > 0 -> "Sin vencer"
                            else -> "Al día"
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
                label = "ESTA SEMANA",
                value = CurrencyFormatter.formatCop(actual),
                valueColor = sections.expenses,
                // El pie compara con la semana pasada, que es lo unico que hace que la cifra
                // signifique algo. Decia «en 1 dia», que sonaba a reproche y no ayudaba a
                // decidir nada.
                footerText = when {
                    anterior > 0 -> {
                        val cambio = ((actual - anterior) * 100.0 / anterior).roundToInt()
                        when {
                            cambio > 0 -> "+$cambio% vs. la anterior"
                            cambio < 0 -> "$cambio% vs. la anterior"
                            else -> "igual que la anterior"
                        }
                    }
                    actual > 0 -> "tu primera semana"
                    else -> "aún sin gastos"
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
private fun heroLabel(timeframe: HomePriorityTimeframe): String = when (timeframe) {
    HomePriorityTimeframe.TODAY -> "LO PRIMERO DE HOY"
    HomePriorityTimeframe.TOMORROW -> "LO PRIMERO DE MAÑANA"
    HomePriorityTimeframe.LATER -> "LO SIGUIENTE"
}

private fun primaryActionLabel(action: HomePriorityAction): String = when (action) {
    HomePriorityAction.SUBJECT -> "Registrar nota"
    HomePriorityAction.SUBJECTS -> "Ver materias"
    HomePriorityAction.TASKS -> "Ver tareas"
    HomePriorityAction.EXPENSES -> "Ver gastos"
    HomePriorityAction.TEMPLATES -> "Ver trabajos"
    HomePriorityAction.SCHEDULE -> "Ver horario"
}

private fun todayLabel(): String {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE d", SpanishLocale)
    return "Hoy, " + today.format(formatter)
}
