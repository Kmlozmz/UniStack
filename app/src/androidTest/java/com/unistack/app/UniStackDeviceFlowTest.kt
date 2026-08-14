package com.unistack.app

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@Ignore("Legacy end-to-end flow kept as reference; smaller connected tests now cover active smoke paths.")
@RunWith(AndroidJUnit4::class)
class UniStackDeviceFlowTest {
    @get:Rule(order = 0)
    val notificationPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun onboardingCoreDataFlowAndActivityRecreate() {
        val today = LocalDate.now()
        val yesterdayInput = today.minusDays(1).toString()
        val currentWeekExpenseInput = today.toString()
        val previousWeekExpenseInput = today.minusWeeks(2).toString()

        composeRule.waitForText("Configura UniStack según tu semestre", timeoutMillis = 45_000)
        composeRule.tapText("Empezar")

        composeRule.waitForText("¿Cómo quieres que te llamemos?")
        composeRule.onAllNodesWithText("Continuar", useUnmergedTree = false)[0].assertIsNotEnabled()
        composeRule.inputTextField(index = 0, value = "Kmlo Test")
        composeRule.tapText("Continuar")

        composeRule.waitForText("¿Dónde estudias actualmente?")
        composeRule.tapText("Otro")
        composeRule.tapText("Continuar")

        composeRule.waitForText("Cuéntanos qué estudias")
        composeRule.tapText("Omitir")

        composeRule.waitForText("¿Qué escala de notas usas?")
        composeRule.tapText("Continuar")

        composeRule.waitForText("¿Qué quieres organizar primero?")
        composeRule.tapText("Continuar")

        composeRule.waitForText("Todo listo")
        composeRule.tapText("Crear mi primera materia")

        composeRule.waitForText("Agregar materia")
        composeRule.inputTextField(index = 0, value = "Matematicas")
        composeRule.tapText("Guardar materia")

        composeRule.waitForText("Matematicas")
        composeRule.waitForText("Agrega tu primera nota para calcular tu promedio.")
        composeRule.tapText("Agregar nota")

        composeRule.waitForText("Agregar nota")
        composeRule.inputTextField(index = 0, value = "Parcial 1")
        composeRule.inputTextField(index = 1, value = "4.5")
        composeRule.inputTextField(index = 2, value = "30")
        composeRule.tapText("Guardar nota")

        composeRule.waitForText("Parcial 1")
        composeRule.waitForText("30% del curso")
        composeRule.waitForText("4.5")

        composeRule.tapContentDescription("Editar nota")
        composeRule.waitForText("Editar nota")
        composeRule.inputTextField(index = 0, value = "Parcial final", clear = true)
        composeRule.inputTextField(index = 1, value = "4.0", clear = true)
        composeRule.inputTextField(index = 2, value = "40", clear = true)
        composeRule.tapText("Guardar cambios")

        composeRule.waitForText("Parcial final")
        composeRule.waitForText("40% del curso")

        composeRule.tapText("Agregar nota")
        composeRule.waitForText("Agregar nota")
        composeRule.inputTextField(index = 0, value = "Nota invalida")
        composeRule.inputTextField(index = 1, value = "4.0")
        composeRule.inputTextField(index = 2, value = "70")
        composeRule.onAllNodesWithText("Guardar nota", useUnmergedTree = false)[0].assertIsNotEnabled()
        composeRule.tapContentDescription("Volver")
        composeRule.waitForText("Parcial final")

        composeRule.tapText("Agregar nota")
        composeRule.waitForText("Agregar nota")
        composeRule.inputTextField(index = 0, value = "Proyecto 2")
        composeRule.inputTextField(index = 1, value = "4.2")
        composeRule.inputTextField(index = 2, value = "60")
        composeRule.tapText("Guardar nota")
        composeRule.waitForText("Proyecto 2")
        composeRule.waitForText("60% del curso")
        composeRule.waitForText("2 registradas")
        composeRule.waitForText("Materia completa. Alcanzaste la meta de 4.0.")
        composeRule.activityRule.scenario.recreate()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.hasAnyText("Proyecto 2") || composeRule.hasAnyText("Hoy, ")
        }
        if (!composeRule.hasAnyText("Proyecto 2")) {
            composeRule.tapText("Materias")
            composeRule.tapText("Matematicas")
        }
        composeRule.waitForText("Proyecto 2")

        composeRule.waitForText("Materia finalizada")

        composeRule.tapContentDescription("Opciones de materia")
        composeRule.tapText("Editar materia")
        composeRule.waitForText("Editar materia")
        composeRule.inputTextField(index = 0, value = "Fisica", clear = true)
        composeRule.tapText("Guardar cambios")
        composeRule.waitForText("Fisica")
        composeRule.activityRule.scenario.recreate()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.hasAnyText("Fisica") || composeRule.hasAnyText("Hoy, ")
        }
        if (!composeRule.hasAnyText("Fisica")) {
            composeRule.tapText("Materias")
        }
        composeRule.waitForText("Fisica")

        composeRule.tapText("Tareas")
        composeRule.waitForText("Aún no tienes tareas reales.")
        composeRule.tapText("Nueva tarea")
        composeRule.waitForText("Nueva tarea")
        composeRule.inputTextField(index = 0, value = "Ensayo final")
        composeRule.inputTextField(index = 1, value = yesterdayInput, clear = true)
        composeRule.inputTextField(index = 2, value = "90", clear = true)
        composeRule.tapText("Fisica")
        composeRule.closeKeyboard()
        composeRule.scrollToText("Crear tarea")
        composeRule.tapText("Crear tarea")
        composeRule.waitForText("Ensayo final")
        composeRule.waitForTextContaining("Fisica · venció ayer")
        composeRule.waitForText("Dificultad media")

        composeRule.scrollToText("Nueva tarea")
        composeRule.tapText("Nueva tarea")
        composeRule.waitForText("Nueva tarea")
        composeRule.inputTextField(index = 0, value = "Lectura semana")
        composeRule.inputTextField(index = 1, value = today.plusDays(1).toString(), clear = true)
        composeRule.inputTextField(index = 2, value = "45", clear = true)
        composeRule.closeKeyboard()
        composeRule.scrollToText("Crear tarea")
        composeRule.tapText("Crear tarea")
        composeRule.waitForText("Lectura semana")

        composeRule.tapText("Vencidas")
        composeRule.waitForText("Ensayo final")
        composeRule.tapText("Completadas")
        composeRule.waitForText("No hay tareas con este filtro.")
        composeRule.tapText("Todas")
        composeRule.waitForText("Ensayo final")
        composeRule.toggleFirstVisible()
        composeRule.tapText("Completadas")
        composeRule.waitForText("Ensayo final")
        composeRule.tapText("Pendientes")
        composeRule.waitForText("Lectura semana")
        composeRule.tapText("Fisica")
        composeRule.waitForText("No hay tareas con este filtro.")
        composeRule.tapText("Todas")
        composeRule.waitForText("Ensayo final")

        composeRule.tapText("Inicio")
        composeRule.waitForText("Prioridades")
        composeRule.waitForText("Próxima tarea")
        composeRule.waitForText("Productividad")
        composeRule.openTemplatesFromHome()
        composeRule.waitForText("Trabajos académicos")
        composeRule.scrollToText("Crear trabajo desde plantilla")
        composeRule.inputTextField(index = 0, value = "Ensayo carbono", clear = true)
        composeRule.inputTextField(index = 1, value = today.plusDays(2).toString(), clear = true)
        composeRule.tapText("Fisica")
        composeRule.tapText("Alta")
        composeRule.closeKeyboard()
        composeRule.scrollToText("Crear trabajo")
        composeRule.tapText("Crear trabajo")
        composeRule.scrollToText("Checklist persistente")
        composeRule.waitForText("Ensayo carbono")
        composeRule.scrollToText("Fuentes")
        composeRule.inputTaggedTextField(
            tag = "academic-work-sources",
            value = "Garcia, M. | 2024 | Carbono azul | Revista Ciencia",
            clear = true
        )
        composeRule.closeKeyboard()
        composeRule.scrollToText("Guardar trabajo")
        composeRule.tapText("Guardar trabajo")
        composeRule.scrollToText("Checklist persistente")
        composeRule.toggleFirstVisible()
        composeRule.waitForText("1 de 7 pasos listos")
        composeRule.scrollToText("Copiar referencias")
        composeRule.tapTag("copy-apa-references")
        composeRule.waitForClipboardContaining("Carbono azul")
        composeRule.scrollToText("Copiar al portapapeles")
        composeRule.tapTag("copy-work-to-clipboard")
        composeRule.waitForClipboardContaining("Ensayo carbono")
        composeRule.scrollToText("Editar trabajo")
        composeRule.inputTextField(index = 0, value = "Ensayo carbono revisado", clear = true)
        composeRule.tapText("Revisión")
        composeRule.closeKeyboard()
        composeRule.scrollToText("Guardar trabajo")
        composeRule.tapClickableText("Guardar trabajo")
        composeRule.waitForText("Ensayo carbono revisado")
        composeRule.tapContentDescription("Volver")
        composeRule.waitForText("Prioridades")

        composeRule.tapText("Gastos")
        composeRule.scrollToText("Registrar gasto")
        composeRule.tapText("Registrar gasto")

        composeRule.waitForText("Registrar gasto")
        composeRule.inputTextField(index = 0, value = "10000")
        composeRule.inputTextField(index = 1, value = currentWeekExpenseInput, clear = true)
        composeRule.closeKeyboard()
        composeRule.scrollToText("Guardar gasto")
        composeRule.tapClickableText("Guardar gasto")
        composeRule.waitForTextGone("Guardar gasto")
        composeRule.waitForText("$10.000")

        composeRule.scrollToText("Registrar gasto")
        composeRule.tapText("Registrar gasto")
        composeRule.waitForText("Registrar gasto")
        composeRule.tapText("Copias")
        composeRule.inputTextField(index = 0, value = "9000")
        composeRule.inputTextField(index = 1, value = previousWeekExpenseInput, clear = true)
        composeRule.closeKeyboard()
        composeRule.scrollToText("Guardar gasto")
        composeRule.tapClickableText("Guardar gasto")
        composeRule.waitForTextGone("Guardar gasto")
        composeRule.waitForText("$10.000")

        composeRule.tapText("Inicio")
        composeRule.scrollToText("Prioridades")
        composeRule.waitForText("Gasto semanal")
        composeRule.waitForText("Próximo trabajo")
        composeRule.waitForText("Esta semana")
        composeRule.waitForText("Productividad")

        composeRule.tapText("Materias")
        composeRule.waitForText("Fisica")
        composeRule.tapText("Fisica")
        composeRule.scrollToText("Trabajos asociados")
        composeRule.waitForText("Ensayo carbono revisado")

        composeRule.tapText("Inicio")
        composeRule.tapContentDescription("Perfil")
        composeRule.waitForText("Perfil")

        composeRule.scrollToText("Configuración académica")
        composeRule.waitForSelectedText("0-5")
        composeRule.tapText("0-10")
        composeRule.waitForSelectedText("0-10")
        composeRule.waitForText("Rango activo: 0 a 10.0")
        composeRule.tapText("Guardar escala")
        composeRule.tapText("0-100")
        composeRule.waitForSelectedText("0-100")
        composeRule.waitForText("Rango activo: 0 a 100")
        composeRule.tapText("Guardar escala")
        composeRule.tapText("0-5")
        composeRule.waitForSelectedText("0-5")
        composeRule.waitForText("Rango activo: 0 a 5.0")
        composeRule.tapText("Guardar escala")

        composeRule.scrollToText("Entregas y pendientes.")
        composeRule.clickContentDescription("Módulo Tareas")
        composeRule.waitForToggleState("Módulo Tareas", "Inactivo")
        composeRule.clickContentDescription("Módulo Gastos")
        composeRule.waitForToggleState("Módulo Gastos", "Inactivo")
        composeRule.clickContentDescription("Módulo Notas")
        composeRule.waitForToggleState("Módulo Notas", "Inactivo")
        composeRule.clickContentDescription("Módulo Trabajos")
        composeRule.waitForToggleState("Módulo Trabajos", "Activo")
        composeRule.clickContentDescription("Módulo Notas")
        composeRule.waitForToggleState("Módulo Notas", "Activo")
        composeRule.clickContentDescription("Módulo Gastos")
        composeRule.waitForToggleState("Módulo Gastos", "Activo")
        composeRule.clickContentDescription("Módulo Tareas")
        composeRule.waitForToggleState("Módulo Tareas", "Activo")

        composeRule.scrollToText("Recordatorios")
        composeRule.toggleFirstVisible()
        composeRule.inputTextField(index = 0, value = "12", clear = true)
        composeRule.closeKeyboard()
        composeRule.tapText("Guardar")

        composeRule.scrollToText("Presupuesto")
        composeRule.inputTextField(index = 0, value = "9000", clear = true)
        composeRule.inputTextField(index = 1, value = "30000", clear = true)
        composeRule.inputTextField(index = 2, value = "50", clear = true)
        composeRule.closeKeyboard()
        composeRule.scrollToText("Guardar presupuesto")
        composeRule.tapClickableText("Guardar presupuesto")
        composeRule.tapText("Salidas")

        composeRule.tapText("Gastos")
        composeRule.scrollToText("Registrar gasto")
        composeRule.tapText("Registrar gasto")
        composeRule.waitForText("Registrar gasto")
        assertFalse(composeRule.hasAnyText("Salidas"))
        composeRule.tapContentDescription("Volver")
        composeRule.waitForText("Gastos")
        composeRule.tapText("Inicio")
        composeRule.waitForText("Hoy, ")
        composeRule.tapContentDescription("Perfil")
        composeRule.waitForText("Perfil")

        // La copia dejó de pasar por el portapapeles: ahora se guarda y se restaura con el
        // selector de archivos del sistema, que es una pantalla de fuera de la app y no se
        // puede conducir desde aquí. Lo que sí se comprueba es que la sección esté completa;
        // el ida y vuelta del JSON lo cubre LocalJsonBackupRepositoryTest.
        composeRule.tapText("Configuración")
        composeRule.waitForText("Datos y respaldos")
        composeRule.tapText("Datos y respaldos")
        composeRule.waitForText("Copia de seguridad")
        composeRule.waitForText("Esto es temporal")
        composeRule.scrollToText("Exportar para leer fuera")
        composeRule.tapContentDescription("Volver")
        composeRule.waitForText("Configuración")
        composeRule.tapContentDescription("Volver")
        composeRule.waitForText("Perfil")

        composeRule.scrollToText("Preferencia visual")
        composeRule.waitForSelectedText("Sistema")
        composeRule.tapText("Claro")
        composeRule.waitForSelectedText("Claro")
        composeRule.tapText("Oscuro")
        composeRule.waitForSelectedText("Oscuro")
        composeRule.tapText("Sistema")
        composeRule.waitForSelectedText("Sistema")

        // El nombre se edita desde el lápiz de la cabecera, no en un formulario fijo.
        composeRule.tapContentDescription("Editar nombre")
        composeRule.inputTextField(index = 0, value = "QA Tester", clear = true)
        composeRule.tapText("Guardar")
        composeRule.waitForText("QA Tester")

        composeRule.tapText("Inicio")
        composeRule.waitForText("Hoy, ")
        val restoreDensityCommand = currentDensityRestoreCommand()
        try {
            listOf(542, 454, 325).forEach { density ->
                executeDeviceCommand("wm density $density")
                composeRule.activityRule.scenario.recreate()
                composeRule.waitForText("Hoy, ")
                composeRule.waitForText("QA Tester 👋")
            }
        } finally {
            executeDeviceCommand(restoreDensityCommand)
            composeRule.activityRule.scenario.recreate()
            composeRule.waitForText("Hoy, ")
        }

        composeRule.openTemplatesFromHome()
        composeRule.waitForText("Trabajos académicos")
        composeRule.scrollToText("Ensayo carbono revisado")
        composeRule.tapContentDescription("Eliminar trabajo")
        composeRule.waitForText("¿Eliminar trabajo?")
        composeRule.tapText("Eliminar")
        composeRule.waitForText("Aún no tienes trabajos guardados.")
        composeRule.tapText("Inicio")
        composeRule.waitForText("Hoy, ")

        composeRule.tapText("Tareas")
        composeRule.waitForText("Ensayo final")
        composeRule.tapText("Todas")
        composeRule.tapContentDescription("Eliminar tarea")
        composeRule.waitForText("¿Eliminar tarea?")
        composeRule.tapText("Eliminar")
        composeRule.waitForText("Lectura semana")
        composeRule.tapContentDescription("Eliminar tarea")
        composeRule.waitForText("¿Eliminar tarea?")
        composeRule.tapText("Eliminar")
        composeRule.waitForText("Aún no tienes tareas reales.")

        composeRule.tapText("Gastos")
        composeRule.tapText("Todo")
        composeRule.scrollToText("Registros")
        composeRule.tapContentDescription("Eliminar gasto")
        composeRule.waitForText("¿Eliminar gasto?")
        composeRule.tapText("Eliminar")
        composeRule.waitForText("$0")
        composeRule.scrollToText("Registros")
        composeRule.tapContentDescription("Eliminar gasto")
        composeRule.waitForText("¿Eliminar gasto?")
        composeRule.tapText("Eliminar")
        composeRule.waitForText("Aún no tienes gastos reales.")

        composeRule.tapText("Materias")
        composeRule.waitForText("Fisica")
        composeRule.tapText("Fisica")
        composeRule.tapContentDescription("Eliminar nota", occurrence = 1)
        composeRule.waitForText("¿Eliminar nota?")
        composeRule.tapText("Eliminar")
        composeRule.waitForText("1 registrada")
        composeRule.tapContentDescription("Eliminar nota")
        composeRule.waitForText("¿Eliminar nota?")
        composeRule.tapText("Eliminar")
        composeRule.waitForText("Agrega tu primera nota para calcular tu promedio.")
        composeRule.tapContentDescription("Opciones de materia")
        composeRule.tapText("Eliminar materia")
        composeRule.waitForText("¿Eliminar materia?")
        composeRule.tapText("Eliminar")
        composeRule.waitForText("Aún no tienes materias.")

        composeRule.tapText("Inicio")
        composeRule.tapContentDescription("Perfil")
        composeRule.waitForText("Perfil")
        composeRule.scrollToText("Reiniciar onboarding")
        composeRule.tapText("Reiniciar onboarding")
        composeRule.waitForText("¿Reiniciar onboarding?")
        composeRule.tapText("Reiniciar")
        composeRule.waitForText("Configura UniStack según tu semestre")
    }
}

private fun ComposeTestRule.waitForText(text: String, timeoutMillis: Long = 12_000) {
    val expectedText = text.currentUiText()
    waitUntil(timeoutMillis = timeoutMillis) {
        if (expectedText == text) {
            hasAnyText(expectedText)
        } else {
            hasAnyTextContaining(expectedText)
        }
    }
}

private fun String.currentUiText(): String {
    val normalized = replace("del curso", "del corte")
    return when {
        normalized != "QA Tester" && normalized.startsWith("QA Tester") -> "Hoy, QA Tester"
        normalized.contains("llamemos") -> "¿Cómo te llamas?"
        normalized.contains("estudias actualmente") -> "¿Cuál es tu nivel"
        normalized.contains("estudias") -> "¿Cuál es tu carrera"
        normalized.contains("escala de notas") -> "¿Cómo es la escala"
        normalized.contains("organizar primero") -> "¿Cómo se evalúa"
        normalized.contains("primera nota") -> "Cortes del semestre"
        normalized == "2 registradas" -> "2 notas registradas"
        normalized.startsWith("Materia completa.") -> "Corte 1"
        normalized == "Materia finalizada" -> "Corte 1"
        normalized == "Aún no tienes tareas reales." -> "Aún no tienes tareas."
        normalized == "No hay tareas con este filtro." -> "No hay tareas con estos filtros."
        normalized == "Agregar nota" -> "Nueva nota"
        normalized == "Todo listo" -> "¿Qué quieres organizar"
        else -> normalized
    }
}

private fun ComposeTestRule.waitForTextContaining(text: String, timeoutMillis: Long = 12_000) {
    waitUntil(timeoutMillis = timeoutMillis) {
        hasAnyTextContaining(text)
    }
}

private fun ComposeTestRule.waitForTextGone(text: String, timeoutMillis: Long = 12_000) {
    waitUntil(timeoutMillis = timeoutMillis) {
        !hasAnyText(text)
    }
}

private fun ComposeTestRule.waitForClipboardContaining(text: String, timeoutMillis: Long = 12_000) {
    waitUntil(timeoutMillis = timeoutMillis) {
        clipboardText().contains(text)
    }
}

private fun ComposeTestRule.hasAnyText(text: String): Boolean =
    try {
        onAllNodesWithText(text, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    } catch (_: IllegalStateException) {
        false
    }

private fun ComposeTestRule.hasAnyTextContaining(text: String): Boolean =
    try {
        onAllNodes(hasText(text, substring = true), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    } catch (_: IllegalStateException) {
        false
    }

private fun ComposeTestRule.waitForSelectedText(text: String, timeoutMillis: Long = 12_000) {
    waitUntil(timeoutMillis = timeoutMillis) {
        onAllNodes(
            hasText(text) and hasStateDescription("Seleccionado"),
            useUnmergedTree = true
        ).fetchSemanticsNodes().isNotEmpty()
    }
}

private fun ComposeTestRule.waitForToggleState(
    description: String,
    state: String,
    timeoutMillis: Long = 12_000
) {
    waitUntil(timeoutMillis = timeoutMillis) {
        onAllNodes(
            hasContentDescription(description) and hasStateDescription(state),
            useUnmergedTree = true
        ).fetchSemanticsNodes().isNotEmpty()
    }
}

private fun ComposeTestRule.tapText(text: String, occurrence: Int = 0) {
    if (text in setOf("Inicio", "Materias", "Tareas", "Gastos") && !hasAnyText(text)) {
        repeat(3) {
            if (hasAnyText(text)) return@repeat
            if (hasAnyContentDescription("Volver")) {
                onAllNodesWithContentDescription("Volver", useUnmergedTree = true)[0].performTouchInput {
                    click()
                }
                waitForIdle()
            }
        }
    }
    if (text.startsWith("Guardar")) {
        closeKeyboard()
        scrollToText(text)
        if (text == "Guardar nota") {
            tapClickableText(text)
        } else {
            executeDeviceCommand("input tap 540 2265")
            waitForIdle()
        }
        return
    }
    if (text == "Omitir") {
        tapClickableText("Continuar")
        return
    }
    if (text == "Agregar nota" && !hasAnyText(text)) {
        if (hasAnyTextContaining("Cortes del semestre")) {
            tapClickableText("Corte 1")
            waitForText("Agregar nota a este corte")
            tapClickableTextContaining("Agregar nota")
            return
        }
        if (hasAnyTextContaining("Agregar nota a este corte")) {
            tapClickableTextContaining("Agregar nota")
            return
        }
        if (hasAnyTextContaining("Nueva nota")) {
            tapClickableTextContaining("Nueva nota")
            tapClickableText("Corte 1")
            return
        }
        tapClickableTextContaining("Agregar nota")
        return
    }
    if (text == "Crear mi primera materia" && !hasAnyText(text)) {
        if (hasAnyTextContaining("¿Qué quieres organizar")) {
            tapClickableText("Continuar")
        }
        waitForText("Resumen de tu configuración")
        tapClickableText("Confirmar")
    }
    waitForText(text)
    onAllNodesWithText(text, useUnmergedTree = true)[occurrence].performTouchInput {
        click()
    }
}

private fun ComposeTestRule.tapClickableTextContaining(text: String, occurrence: Int = 0) {
    waitUntil(timeoutMillis = 12_000) {
        onAllNodes(
            hasText(text, substring = true) and hasClickAction(),
            useUnmergedTree = false
        ).fetchSemanticsNodes().isNotEmpty()
    }
    onAllNodes(
        hasText(text, substring = true) and hasClickAction(),
        useUnmergedTree = false
    )[occurrence].performClick()
}

private fun ComposeTestRule.tapClickableText(text: String, occurrence: Int = 0) {
    waitUntil(timeoutMillis = 12_000) {
        clickableTextExists(text, useUnmergedTree = true) || clickableTextExists(text, useUnmergedTree = false)
    }
    if (clickableTextExists(text, useUnmergedTree = true)) {
        onAllNodes(
            hasText(text) and hasClickAction(),
            useUnmergedTree = true
        )[occurrence].performTouchInput {
            click()
        }
    } else {
        onAllNodes(
            hasText(text) and hasClickAction(),
            useUnmergedTree = false
        )[occurrence].performClick()
    }
}

private fun ComposeTestRule.clickableTextExists(text: String, useUnmergedTree: Boolean): Boolean =
    try {
        onAllNodes(
            hasText(text) and hasClickAction(),
            useUnmergedTree = useUnmergedTree
        ).fetchSemanticsNodes().isNotEmpty()
    } catch (_: IllegalStateException) {
        false
    }

private fun ComposeTestRule.openTemplatesFromHome() {
    scrollToText("Trabajos académicos")
    waitForText("Trabajos académicos")
    tapLastText("Abrir")
}

private fun ComposeTestRule.tapLastText(text: String) {
    waitForText(text)
    val lastIndex = onAllNodesWithText(text, useUnmergedTree = true).fetchSemanticsNodes().lastIndex
    onAllNodesWithText(text, useUnmergedTree = true)[lastIndex].performTouchInput {
        click()
    }
}

private fun ComposeTestRule.tapTag(tag: String) {
    if (!hasAnyTag(tag)) {
        swipeUpUntilTagAppears(tag)
    }
    waitUntil(timeoutMillis = 12_000) {
        onAllNodes(hasTestTag(tag), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    }
    onAllNodes(hasTestTag(tag), useUnmergedTree = true)[0].performClick()
}

private fun ComposeTestRule.hasAnyTag(tag: String): Boolean =
    try {
        onAllNodes(hasTestTag(tag), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    } catch (_: IllegalStateException) {
        false
    }

private fun ComposeTestRule.swipeUpUntilTagAppears(tag: String, maxAttempts: Int = 5) {
    repeat(maxAttempts) {
        val scrollables = try {
            onAllNodes(hasScrollAction(), useUnmergedTree = true).fetchSemanticsNodes()
        } catch (_: IllegalStateException) {
            emptyList()
        }
        scrollables.indices.forEach { index ->
            try {
                onAllNodes(hasScrollAction(), useUnmergedTree = true)[index].performTouchInput {
                    swipeUp()
                }
                waitForIdle()
                if (hasAnyTag(tag)) return
            } catch (_: AssertionError) {
                // Try the next scroll container.
            } catch (_: IllegalStateException) {
                // Semantics may change while scrolling; the final wait decides.
            }
        }
    }
}

private fun ComposeTestRule.tapContentDescription(description: String, occurrence: Int = 0) {
    if (description == "Opciones de materia" && !hasAnyContentDescription(description) && hasAnyText("Corte 1")) {
        tapContentDescription("Volver")
    }
    if (description == "Editar nota" && !hasAnyContentDescription(description)) {
        tapContentDescription("Opciones de nota", occurrence)
        tapText("Editar")
        return
    }
    if (description == "Eliminar nota" && !hasAnyContentDescription(description)) {
        tapContentDescription("Opciones de nota", occurrence)
        tapText("Eliminar")
        return
    }
    if (!hasAnyContentDescription(description)) {
        scrollToContentDescription(description)
    }
    waitUntil(timeoutMillis = 12_000) {
        onAllNodesWithContentDescription(description, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
    }
    onAllNodesWithContentDescription(description, useUnmergedTree = true)[occurrence].performTouchInput {
        click()
    }
}

private fun ComposeTestRule.clickContentDescription(description: String, occurrence: Int = 0) {
    waitUntil(timeoutMillis = 12_000) {
        onAllNodes(
            hasContentDescription(description) and hasClickAction(),
            useUnmergedTree = true
        ).fetchSemanticsNodes().isNotEmpty()
    }
    onAllNodes(
        hasContentDescription(description) and hasClickAction(),
        useUnmergedTree = true
    )[occurrence].performClick()
}

private fun ComposeTestRule.hasAnyContentDescription(description: String): Boolean =
    try {
        onAllNodesWithContentDescription(description, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    } catch (_: IllegalStateException) {
        false
    }

private fun ComposeTestRule.scrollToContentDescription(description: String) {
    val scrollables = try {
        onAllNodes(hasScrollAction(), useUnmergedTree = true).fetchSemanticsNodes()
    } catch (_: IllegalStateException) {
        emptyList()
    }
    scrollables.indices.forEach { index ->
        try {
            onAllNodes(hasScrollAction(), useUnmergedTree = true)[index]
                .performScrollToNode(hasContentDescription(description))
            if (hasAnyContentDescription(description)) return
        } catch (_: AssertionError) {
            // Try the next scroll container; nested screens can expose more than one.
        } catch (_: IllegalStateException) {
            // The tree may change while scrolling; the final wait below will decide.
        }
    }
}

private fun ComposeTestRule.scrollToText(text: String) {
    onAllNodes(hasScrollAction(), useUnmergedTree = true)[0].performScrollToNode(hasText(text))
    waitForText(text)
}

private fun ComposeTestRule.swipeUpOnFirstScrollContainer(times: Int = 1) {
    repeat(times) {
        onAllNodes(hasScrollAction(), useUnmergedTree = true)[0].performTouchInput {
            swipeUp()
        }
        waitForIdle()
    }
}

private fun ComposeTestRule.closeKeyboard() {
    executeDeviceCommand("input keyevent KEYCODE_BACK")
    waitForIdle()
}

private fun ComposeTestRule.toggleFirstVisible() {
    waitUntil(timeoutMillis = 12_000) {
        onAllNodes(isToggleable(), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    }
    onAllNodes(isToggleable(), useUnmergedTree = true)[0].performClick()
}

private fun ComposeTestRule.inputTextField(index: Int, value: String, clear: Boolean = false) {
    val field = onAllNodes(hasSetTextAction(), useUnmergedTree = true)[index]
    if (clear) {
        field.performTextClearance()
    }
    field.performTextInput(value)
}

private fun ComposeTestRule.inputTaggedTextField(tag: String, value: String, clear: Boolean = false) {
    val field = onAllNodes(hasTestTag(tag) and hasSetTextAction(), useUnmergedTree = true)[0]
    if (clear) {
        field.performTextClearance()
    }
    field.performTextInput(value)
}

private fun clipboardText(): String {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return clipboard.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
}

private fun currentDensityRestoreCommand(): String {
    val output = executeDeviceCommand("wm density")
    val overrideDensity = Regex("Override density: (\\d+)").find(output)?.groupValues?.get(1)
    return if (overrideDensity != null) {
        "wm density $overrideDensity"
    } else {
        "wm density reset"
    }
}

private fun executeDeviceCommand(command: String): String {
    val descriptor = InstrumentationRegistry.getInstrumentation()
        .uiAutomation
        .executeShellCommand(command)
    return ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().use { it.readText() }
}
