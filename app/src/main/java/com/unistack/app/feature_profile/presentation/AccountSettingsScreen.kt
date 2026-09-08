@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.runtime.remember
import androidx.compose.foundation.clickable
import android.net.Uri
import com.unistack.app.feature_user.domain.portraitUrl
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.SettingsGroupCard
import com.unistack.app.core.design.components.SettingsRowIcon
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.dismissKeyboardOnTapOutside
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.TextValidators
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.unistack.app.feature_setup.presentation.InstitutionField
import com.unistack.app.feature_setup.presentation.OTHER_OPTION
import com.unistack.app.feature_setup.presentation.Revelado
import com.unistack.app.feature_setup.presentation.SetupCustomProgramField
import com.unistack.app.feature_setup.presentation.SetupDropdownField
import com.unistack.app.feature_setup.presentation.labelFor
import com.unistack.app.feature_setup.presentation.programIcon
import com.unistack.app.feature_setup.presentation.programsFor
import com.unistack.app.feature_setup.presentation.studyAreaForLabel
import com.unistack.app.feature_setup.presentation.studyAreaIcon
import com.unistack.app.feature_terms.domain.AcademicTermType
import com.unistack.app.feature_user.domain.StudyArea
import com.unistack.app.feature_user.domain.UserProfile

/**
 * Cuenta y perfil: quién eres y a qué cuenta está atado esto.
 *
 * El retrato manda y el lápiz va encima de él, no en una fila aparte que había que buscar.
 * Lo que se fue de aquí es «Tu semestre»: la escala, la meta y los cortes se contaban también
 * en el centro de ajustes y otra vez en la pantalla académica, tres veces el mismo dato en
 * tres sitios que se desincronizaban entre sí.
 */
@Composable
fun AccountSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val activeTerm by viewModel.activeTerm.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val context = LocalContext.current
    val current = profile ?: return

    var editingName by rememberSaveable { mutableStateOf(false) }
    var nameInput by rememberSaveable(current.userId) { mutableStateOf(current.preferredName) }
    var showUnlinkDialog by rememberSaveable { mutableStateOf(false) }
    var editingProgress by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    /*
     * La foto se copia dentro de la app, no se referencia.
     *
     * El selector devuelve un `content://` con permiso de lectura que dura lo que dure el
     * proceso: al reabrir la app el retrato sería un hueco. Y aunque el permiso se persistiera,
     * la foto sigue siendo de la galería —si la borran de ahí, desaparece de aquí—. Copiarla
     * cuesta un archivo pequeño y quita los dos problemas.
     */
    /*
     * Elegir y encuadrar son dos pasos, y el segundo es el que importa.
     *
     * El selector devuelve la foto entera; el retrato se pinta redondo y recortado al centro,
     * así que sin este paso el encuadre lo decidía el azar. Lo que se guarda es ya el recorte.
     */
    var editing by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) editing = uri
    }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }

    LargeTitleScaffold(
        title = "Cuenta y perfil",
        subtitle = "Nombre, foto y sincronización",
        onBackClick = onBackClick,
        modifier = modifier.dismissKeyboardOnTapOutside(),
        horizontalPadding = spacing.screenHorizontal,
        topPadding = 8.dp,
        bottomPadding = scrollBottomRoom,
        itemSpacing = 12.dp
    ) {
        item {
            AccountPortrait(
                name = current.preferredName.takeIf { it.isNotBlank() } ?: "Estudiante",
                detail = current.educationSummary(),
                photoUrl = current.portraitUrl,

                /*
                 * Un solo botón, y lo demás dentro.
                 *
                 * Debajo del retrato había dos enlaces sueltos, «Ajustar encuadre» y «Quitar mi
                 * foto», permanentes. El primero era el editor, que es exactamente lo que se
                 * abre al tocar la foto; y quitarla es algo que se decide cuando la estás
                 * mirando, no una opción que tenga que estar ahí siempre.
                 *
                 * Ahora la foto se toca y se abre el editor —con el original guardado, para no
                 * recortar un recorte—. Si no hay retrato propio, o viene de una versión sin
                 * original, se pide una foto directamente.
                 */
                onPhotoClick = {
                    val origen = ProfilePhotoFiles.sourceOf(context, current.localPhotoUri)
                    if (origen != null) {
                        editing = origen
                    } else {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                },
                onEditNameClick = {
                    nameInput = current.preferredName
                    editingName = true
                }
            )
        }
        item {
            Column {
                Text(
                    text = "QUÉ ESTUDIAS",
                    style = SectionLabelStyle,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 9.dp)
                )
                AcademicIdentityFields(
                    profile = current,
                    onAreaSelected = viewModel::updateStudyArea,
                    onCustomAreaChange = viewModel::updateCustomStudyArea,
                    onProgramChange = viewModel::updateCareerOrProgram,
                    onInstitutionChange = viewModel::updateInstitutionName
                )
            }
        }
        item {
            Column {
                Text(
                    text = "TU PROGRESO",
                    style = SectionLabelStyle,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 9.dp)
                )
                SemesterProgressCard(
                    currentSemester = current.currentSemester,
                    totalSemesters = current.totalSemesters,
                    termWeeks = activeTerm?.type?.weeks ?: AcademicTermType.SEMESTER.weeks,
                    onEditClick = { editingProgress = true }
                )
            }
        }
        item {
            SettingsGroupCard(label = "CUENTA") {
                AccountLinkRow(
                    linked = currentUser.isLinked,
                    available = viewModel.accountLinkAvailable,
                    title = if (currentUser.isLinked) currentUser.accountLabel() else "Sin cuenta vinculada",
                    detail = currentUser.email
                        ?: if (viewModel.accountLinkAvailable) {
                            "Vincula una para respaldar en la nube"
                        } else {
                            "Podrás respaldar tus datos y recuperarlos si cambias de teléfono"
                        },
                    isBusy = actionState.isAccountBusy,
                    onClick = {
                        if (currentUser.isLinked) showUnlinkDialog = true else viewModel.connectGoogle(context)
                    }
                )
            }
        }
        feedback?.let { message ->
            item {
                Text(
                    message,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    color = if (message.startsWith("Revisa")) {
                        MaterialTheme.colorScheme.error
                    } else {
                        LocalSectionColors.current.onTrack
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
        actionState.errorMessage?.let { message ->
            item {
                Text(
                    message,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (editingName) {
        val validation = TextValidators.validateDisplayName(nameInput)
        AlertDialog(
            onDismissRequest = { editingName = false },
            title = { Text("Tu nombre") },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it.take(30) },
                    label = { Text("Nombre preferido") },
                    singleLine = true,
                    isError = nameInput.isNotBlank() && !validation.isValid,
                    supportingText = {
                        if (nameInput.isNotBlank() && !validation.isValid) {
                            Text(validation.errorMessage ?: "Ingresa un nombre válido")
                        }
                    },
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = validation.isValid,
                    onClick = {
                        feedback = if (viewModel.updatePreferredName(nameInput)) {
                            editingName = false
                            "Nombre actualizado."
                        } else {
                            "Revisa el nombre antes de guardar."
                        }
                    }
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        nameInput = current.preferredName
                        editingName = false
                    }
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }

    editing?.let { origen ->
        ProfilePhotoEditor(
            source = origen,
            canRemove = current.localPhotoUri != null,
            onPickAnother = {
                editing = null
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onRemove = {
                editing = null
                ProfilePhotoFiles.clear(context)
                viewModel.updateLocalPhoto(null)
            },
            onCancel = { editing = null },
            onSave = { ruta ->
                editing = null
                viewModel.updateLocalPhoto(ruta)
            }
        )
    }

    if (showUnlinkDialog) {
        AlertDialog(
            onDismissRequest = { showUnlinkDialog = false },
            title = { Text("¿Desvincular cuenta?") },
            text = { Text("Tus datos locales se mantienen en este dispositivo.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnlinkDialog = false
                        viewModel.unlinkAccount()
                    }
                ) {
                    Text("Desvincular", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlinkDialog = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }

    if (editingProgress) {
        SemesterProgressDialog(
            initialCurrent = current.currentSemester ?: 1,
            initialTotal = current.totalSemesters ?: 8,
            onDismiss = { editingProgress = false },
            onConfirm = { semesterActual, semestresTotal ->
                viewModel.updateSemesterProgress(semesterActual, semestresTotal)
                editingProgress = false
            }
        )
    }
}

/**
 * El retrato, el lápiz y el nombre, centrados.
 */
@Composable
private fun AccountPortrait(
    name: String,
    detail: String,
    photoUrl: String?,
    onPhotoClick: () -> Unit,
    onEditNameClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box {
            AccountAvatar(
                photoUrl = photoUrl,
                contentDescription = "Foto de perfil",
                initial = name.first().uppercase(),
                modifier = Modifier
                    .size(96.dp)
                    // Sin recortar, la onda del toque salía cuadrada detrás de un retrato
                    // redondo.
                    .clip(CircleShape)
                    .clickable(onClick = onPhotoClick)
            )
            /*
             * Dos cosas editables, dos botones.
             *
             * Había un solo lápiz, sobre el retrato, y abría el nombre. No había forma de
             * adivinarlo —un lápiz encima de una foto promete cambiar la foto— y la foto no se
             * podía cambiar de ninguna manera. Ahora la cámara hace lo que parece, y el nombre
             * tiene su propio lápiz al lado.
             */
            Surface(
                onClick = onPhotoClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .size(34.dp)
                    // El borde del color del fondo separa el botón del retrato: sin él, dos
                    // círculos pegados se leen como una sola mancha con un bulto.
                    .border(3.dp, MaterialTheme.colorScheme.background, CircleShape),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.PhotoCamera,
                        contentDescription = "Cambiar foto de perfil",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Surface(
                    onClick = onEditNameClick,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = "Editar nombre",
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

        }
    }
}

/**
 * La cuenta: en qué estado está y el botón que la cambia, en la misma fila.
 */
@Composable
private fun AccountLinkRow(
    linked: Boolean,
    available: Boolean,
    title: String,
    detail: String,
    isBusy: Boolean,
    onClick: () -> Unit
) {
    val sections = LocalSectionColors.current
    // Apagada al 45% mientras no haya proyecto configurado, igual que el bloque de copia en
    // la nube de Datos. Antes se veía a plena luz y se dejaba pulsar: fallaba siempre, y
    // encima prometía un respaldo que todavía no existe.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (available) 1f else 0.45f)
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        SettingsRowIcon(
            icon = if (linked) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
            color = if (linked) sections.onTrack else sections.schedule
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    // Cede el ancho que necesite la insignia en lugar de quedarselo todo.
                    // Sin esto «Pronto» se quedaba sin sitio y se partia letra a letra, en
                    // vertical, empujando el resto de la fila fuera de la tarjeta.
                    modifier = Modifier.weight(1f, fill = false),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!available) {
                    Box(
                        Modifier
                            .clip(CircleShape)
                            .background(sections.atRisk.copy(alpha = 0.22f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "Pronto",
                            color = sections.atRisk,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Surface(
            onClick = onClick,
            enabled = available && !isBusy,
            shape = CircleShape,
            color = if (linked) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.primary,
            contentColor = if (linked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
        ) {
            Text(
                text = when {
                    isBusy -> "..."
                    linked -> "Desvincular"
                    else -> "Vincular"
                },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Área, carrera e institución, igual que en el onboarding.
 *
 * Con área propia no hay catálogo que ofrecer, así que el paso de elegir «Otra» en una lista
 * de una sola opción sobra: el campo de carrera pasa directo a texto libre. Con área del
 * catálogo, la carrera se sigue pudiendo escribir a mano si no aparece en su lista.
 */
@Composable
private fun AcademicIdentityFields(
    profile: UserProfile,
    onAreaSelected: (StudyArea) -> Unit,
    onCustomAreaChange: (String) -> Unit,
    onProgramChange: (String) -> Unit,
    onInstitutionChange: (String) -> Unit
) {
    var areaExpanded by remember { mutableStateOf(false) }
    var programExpanded by remember { mutableStateOf(false) }
    val areaIsCustom = profile.studyArea == StudyArea.OTHER
    val catalogPrograms = profile.studyArea?.let(::programsFor).orEmpty()
    val programIsCustom = profile.careerOrProgram != null && profile.careerOrProgram !in catalogPrograms

    // Búfer local para que escribir no espere la vuelta del guardado a cada letra -- el mismo
    // motivo por el que InstitutionField lleva el suyo propio. Se reinicia solo al entrar o
    // salir del modo libre, no en cada tecla.
    var areaDraft by remember(areaIsCustom) { mutableStateOf(profile.customStudyArea.orEmpty()) }
    var programDraft by remember(programIsCustom) { mutableStateOf(profile.careerOrProgram.orEmpty()) }

    // Cajas de icono neutras a propósito. El mismo morado ya lo llevan el avatar y el punto
    // "ahora" del progreso; repetirlo aquí no distingue nada, solo compite.
    val fieldIconBoxColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val fieldIconTint = MaterialTheme.colorScheme.onSurfaceVariant

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SetupDropdownField(
            label = "Área de estudio",
            value = profile.studyArea?.let(::labelFor).orEmpty(),
            options = StudyArea.entries.map(::labelFor),
            enabled = true,
            expanded = areaExpanded,
            leadingIcon = profile.studyArea?.let(::studyAreaIcon) ?: Icons.Rounded.School,
            optionIcon = { option -> studyAreaForLabel(option)?.let(::studyAreaIcon) ?: Icons.Rounded.GridView },
            onExpandedChange = { expanded ->
                areaExpanded = expanded
                if (expanded) programExpanded = false
            },
            onOptionSelected = { selectedLabel ->
                StudyArea.entries.firstOrNull { labelFor(it) == selectedLabel }?.let(onAreaSelected)
            },
            leadingIconContainerColor = fieldIconBoxColor,
            leadingIconContentColor = fieldIconTint
        )
        if (areaIsCustom) {
            SetupCustomProgramField(
                value = areaDraft,
                validation = null,
                onValueChange = { areaDraft = it; onCustomAreaChange(it) },
                label = "Nombre del área",
                placeholder = "Ej: Ciencias del deporte"
            )
        }

        if (areaIsCustom) {
            // Sin catálogo que ofrecer, directo a texto libre: sin el paso de elegir "Otra"
            // en una lista que solo tendría esa opción.
            SetupCustomProgramField(
                value = programDraft,
                validation = null,
                onValueChange = { programDraft = it; onProgramChange(it) },
                label = "Programa o carrera",
                placeholder = "Escribe tu programa"
            )
        } else {
            Revelado(visible = profile.studyArea != null) {
                SetupDropdownField(
                    label = "Programa o carrera",
                    value = if (programIsCustom) OTHER_OPTION else profile.careerOrProgram.orEmpty(),
                    options = catalogPrograms,
                    enabled = profile.studyArea != null,
                    expanded = programExpanded,
                    leadingIcon = Icons.Rounded.School,
                    optionIcon = { option -> programIcon(option) },
                    onExpandedChange = { expanded ->
                        programExpanded = expanded
                        if (expanded) areaExpanded = false
                    },
                    onOptionSelected = { picked ->
                        onProgramChange(if (picked == OTHER_OPTION) "" else picked)
                    },
                    leadingIconContainerColor = fieldIconBoxColor,
                    leadingIconContentColor = fieldIconTint
                )
            }
            if (programIsCustom) {
                SetupCustomProgramField(
                    value = programDraft,
                    validation = null,
                    onValueChange = { programDraft = it; onProgramChange(it) },
                    label = "Nombre del programa",
                    placeholder = "Ej: Ingeniería Biomédica"
                )
            }
        }

        InstitutionField(
            value = profile.institutionName.orEmpty(),
            label = "Institución",
            placeholder = "Nombre de tu universidad",
            onValueChange = onInstitutionChange
        )
    }
}

/**
 * Inicio, ahora y meta, y cuánto falta aproximado a una fecha.
 *
 * Tres paradas en vez de un punto por semestre: el relato no depende de cuántos semestres
 * tenga tu carrera, y solo el nodo que importa -- dónde estás -- lleva número. Debajo va lo
 * que de verdad añade la cuenta atrás: cuánto falta, aproximado a partir de cuánto dura tu
 * tipo de periodo -- no una fecha exacta, una estimación al ritmo de siempre.
 */
@Composable
private fun SemesterProgressCard(
    currentSemester: Int?,
    totalSemesters: Int?,
    termWeeks: Int,
    onEditClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (currentSemester == null || totalSemesters == null) {
                EmptyProgressPrompt(onClick = onEditClick)
            } else {
                MilestoneRow(current = currentSemester, total = totalSemesters)

                val goalReached = currentSemester >= totalSemesters
                val percent = if (totalSemesters > 0) currentSemester * 100 / totalSemesters else 0
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (goalReached) "Tu último semestre" else "Semestre $currentSemester de $totalSemesters",
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (goalReached) {
                            "Cuando lo cierres, tu carrera queda completa."
                        } else {
                            "Llevas $percent% de tu carrera recorrido."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = etaAnnotatedString(
                            remaining = (totalSemesters - currentSemester).coerceAtLeast(0),
                            termWeeks = termWeeks
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Surface(
                        onClick = onEditClick,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Cambiar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyProgressPrompt(onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Flag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Aún no dijiste en qué semestre vas",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Añádelo y arma el camino hasta tu meta.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Text(
                "Añadir",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Tres paradas en vez de una por semestre: de dónde saliste, dónde estás, y a dónde vas.
 *
 * Un punto por semestre dejaba de leerse de un vistazo en cuanto la carrera pasaba de
 * seis u ocho periodos: demasiadas cifras pequeñas compitiendo por la misma fila. El
 * relato de inicio-ahora-meta no depende de cuántos semestres tenga tu carrera, y solo
 * el nodo que importa -- dónde estás -- lleva número y peso visual.
 */
@Composable
private fun MilestoneRow(current: Int, total: Int) {
    val reached = current >= total
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val dotSlot = 48.dp
    val linkOffset = dotSlot / 2 - 1.dp

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.width(52.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.height(dotSlot).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(primaryContainer)
                )
            }
            Text(
                "Inicio",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        MilestoneLink(filled = true, modifier = Modifier.weight(1f).padding(top = linkOffset))

        Column(modifier = Modifier.width(64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.height(dotSlot).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(dotSlot)
                        .clip(CircleShape)
                        .background(primary.copy(alpha = 0.22f))
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(current.toString(), color = onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                "Ahora",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = onSurface,
                textAlign = TextAlign.Center
            )
        }

        MilestoneLink(filled = reached, dashed = !reached, modifier = Modifier.weight(1f).padding(top = linkOffset))

        Column(modifier = Modifier.width(64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.height(dotSlot).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (reached) primary else Color.Transparent)
                        .then(if (!reached) Modifier.border(1.6.dp, primary, CircleShape) else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Flag,
                        contentDescription = "Meta",
                        tint = if (reached) onPrimary else primary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Text(
                "Meta · $total",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MilestoneLink(filled: Boolean, modifier: Modifier = Modifier, dashed: Boolean = false) {
    val filledColor = MaterialTheme.colorScheme.primaryContainer
    val dashedColor = MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier = modifier
            .height(2.dp)
            .then(
                if (dashed) {
                    Modifier.drawBehind {
                        drawLine(
                            color = dashedColor,
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2),
                            strokeWidth = size.height,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))
                        )
                    }
                } else {
                    Modifier.background(if (filled) filledColor else dashedColor)
                }
            )
    )
}

@Composable
private fun etaAnnotatedString(remaining: Int, termWeeks: Int): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        if (remaining <= 0) {
            append("Ya llegaste a tu último semestre.")
        } else {
            val months = remaining * (termWeeks / 4.33)
            val years = (months / 12).toInt()
            val restMonths = Math.round(months % 12).toInt()
            val etaText = if (years > 0) {
                buildString {
                    append(years)
                    append(if (years == 1) " año" else " años")
                    if (restMonths > 0) {
                        append(" y ")
                        append(restMonths)
                        append(" m.")
                    }
                }
            } else {
                "${Math.round(months)} meses"
            }
            append("Estimado: ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(etaText) }
            append(" más para llegar a la meta, al ritmo de siempre.")
        }
    }
}

/** Semestre actual y total, con dos contadores en vez de teclado. */
@Composable
private fun SemesterProgressDialog(
    initialCurrent: Int,
    initialTotal: Int,
    onDismiss: () -> Unit,
    onConfirm: (currentSemester: Int, totalSemesters: Int) -> Unit
) {
    var draftCurrent by remember { mutableIntStateOf(initialCurrent) }
    var draftTotal by remember { mutableIntStateOf(initialTotal) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tu progreso") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(
                    "¿En qué semestre vas, y cuántos dura tu programa en total?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SemesterStepperRow(
                    label = "Semestre actual",
                    value = draftCurrent,
                    onDecrement = { if (draftCurrent > 1) draftCurrent-- },
                    onIncrement = { if (draftCurrent < draftTotal) draftCurrent++ }
                )
                SemesterStepperRow(
                    label = "Total de semestres",
                    value = draftTotal,
                    onDecrement = {
                        if (draftTotal > 1) {
                            draftTotal--
                            if (draftCurrent > draftTotal) draftCurrent = draftTotal
                        }
                    },
                    onIncrement = { if (draftTotal < 30) draftTotal++ }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(draftCurrent, draftTotal) }) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    )
}

@Composable
private fun SemesterStepperRow(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            SemesterStepButton(icon = Icons.Rounded.Remove, description = "Menos", onClick = onDecrement)
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMediumEmphasized,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 22.dp)
            )
            SemesterStepButton(icon = Icons.Rounded.Add, description = "Más", onClick = onIncrement)
        }
    }
}

@Composable
private fun SemesterStepButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, modifier = Modifier.size(16.dp))
        }
    }
}
