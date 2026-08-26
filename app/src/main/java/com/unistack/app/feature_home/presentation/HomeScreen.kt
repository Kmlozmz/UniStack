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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
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
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import com.unistack.app.core.utils.greetingForNow
import com.unistack.app.core.utils.CurrencyFormatter
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
    onAiClick: () -> Unit = {},
    onLabsClick: () -> Unit = {},
    onDrawerOpenChange: (Boolean) -> Unit = {}
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
                        HomeTodayCard(items = summary.todayItems, onEmptyClick = onCalendarClick)
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
            Surface(
                onClick = onAvatarClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.align(Alignment.CenterStart).size(38.dp)
            ) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Tu perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = name.take(1).uppercase(SpanishLocale),
                            style = MaterialTheme.typography.titleMediumEmphasized
                        )
                    }
                }
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

        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 10.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = greetingForNow().uppercase(SpanishLocale),
                style = SectionLabelStyle,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = name,
                style = MaterialTheme.typography.headlineLargeEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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

/** Lo de hoy: hora, un riel del color de lo que sea, y el detalle. */
@Composable
private fun HomeTodayCard(items: List<HomeTimelineSummary>, onEmptyClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        if (items.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Hoy no tienes nada puesto", style = MaterialTheme.typography.titleSmallEmphasized)
                Text(
                    "Ni clases ni entregas. Si falta algo, añádelo desde Horario.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onEmptyClick, contentPadding = PaddingValues(0.dp)) { Text("Abrir Horario") }
            }
        } else {
            Column(modifier = Modifier.padding(6.dp)) {
                items.forEach { item -> HomeTodayRow(item) }
            }
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

/** Promedio, pendientes y gasto del mes. */
@Composable
private fun HomeSnapshotRow(
    summary: HomeSummary,
    onAverageClick: () -> Unit,
    onPendingClick: () -> Unit,
    onExpensesClick: () -> Unit
) {
    val sections = LocalSectionColors.current
    val modules = summary.enabledModules

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (AppModule.GRADES in modules) {
            HomeTile(
                label = "PROMEDIO",
                value = summary.generalAverage?.let { String.format(SpanishLocale, "%.1f", it) } ?: "—",
                onClick = onAverageClick,
                modifier = Modifier.weight(1f)
            ) {
                /*
                 * La referencia de la escala, no una barra.
                 *
                 * Aquí hubo un indicador de progreso ondulado y no quería decir nada: un
                 * promedio de 4,0 no es «el 80 % de algo», es una nota. La barra invitaba a
                 * leerlo como un avance que se llena, que es justo lo que no es.
                 */
                Text(
                    text = "de " + GradingScaleUtils.formatGrade(
                        GradingScaleUtils.maxGradeFor(summary.gradingScale),
                        summary.gradingScale
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
        if (AppModule.TASKS in modules) {
            HomeTile(
                label = "PENDIENTES",
                value = summary.pendingTasks.toString(),
                onClick = onPendingClick,
                modifier = Modifier.weight(1f)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    val overdue = summary.overdueTasks
                    repeat(3) { index ->
                        val filled = index < overdue.coerceAtMost(3)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(if (filled) sections.atRisk else MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }
            }
        }
        if (AppModule.EXPENSES in modules) {
            HomeTile(
                label = "ESTA SEMANA",
                value = CurrencyFormatter.formatCop(summary.weeklyExpenseTotal),
                valueColor = sections.expenses,
                onClick = onExpensesClick,
                modifier = Modifier.weight(1f)
            ) {
                val bars = summary.weeklyExpenses?.chartValues.orEmpty().takeLast(5)
                val peak = (bars.maxOrNull() ?: 0).coerceAtLeast(1)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.fillMaxWidth().height(16.dp)
                ) {
                    bars.forEach { value ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height((4 + 12f * value / peak).dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(sections.expenses)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTile(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.Unspecified,
    footer: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = label, style = SectionLabelStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor,
                maxLines = 1
            )
            Box(modifier = Modifier.height(16.dp), contentAlignment = Alignment.CenterStart) { footer() }
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
