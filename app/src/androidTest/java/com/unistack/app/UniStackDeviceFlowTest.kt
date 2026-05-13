package com.unistack.app

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
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
import androidx.compose.ui.test.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UniStackDeviceFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun onboardingCoreDataFlowAndActivityRecreate() {
        composeRule.waitForText("Bienvenido a UniStack")
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
            composeRule.hasAnyText("Proyecto 2") || composeRule.hasAnyText("Hola, ")
        }
        if (!composeRule.hasAnyText("Proyecto 2")) {
            composeRule.tapText("Materias")
            composeRule.tapText("Matematicas")
        }
        composeRule.waitForText("Proyecto 2")

        composeRule.tapContentDescription("Eliminar nota", occurrence = 1)
        composeRule.waitForText("¿Eliminar nota?")
        composeRule.tapText("Eliminar")
        composeRule.waitForText("1 registrada")
        composeRule.tapContentDescription("Eliminar nota")
        composeRule.waitForText("¿Eliminar nota?")
        composeRule.tapText("Eliminar")
        composeRule.waitForText("Agrega tu primera nota para calcular tu promedio.")

        composeRule.tapContentDescription("Opciones de materia")
        composeRule.tapText("Editar materia")
        composeRule.waitForText("Editar materia")
        composeRule.inputTextField(index = 0, value = "Fisica", clear = true)
        composeRule.tapText("Guardar cambios")
        composeRule.waitForText("Fisica")
        composeRule.activityRule.scenario.recreate()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.hasAnyText("Fisica") || composeRule.hasAnyText("Hola, ")
        }
        if (!composeRule.hasAnyText("Fisica")) {
            composeRule.tapText("Materias")
        }
        composeRule.waitForText("Fisica")

        composeRule.tapText("Tareas")
        composeRule.waitForText("Aún no tienes tareas reales.")
        composeRule.tapText("Nueva tarea")
        composeRule.waitForText("Nueva tarea")
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForText("Aún no tienes tareas reales.")
        composeRule.tapText("Nueva tarea")

        composeRule.waitForText("Nueva tarea")
        composeRule.inputTextField(index = 0, value = "Ensayo final")
        composeRule.tapText("Fisica")
        composeRule.tapText("Crear tarea")

        composeRule.waitForText("Ensayo final")
        composeRule.waitForText("Dificultad media")
        composeRule.tapContentDescription("Editar tarea")
        composeRule.waitForText("Editar tarea")
        composeRule.inputTextField(index = 0, value = "Proyecto final", clear = true)
        composeRule.tapText("Guardar cambios")
        composeRule.waitForText("Proyecto final")
        composeRule.toggleAt(index = 0)
        composeRule.onAllNodes(isToggleable(), useUnmergedTree = true)[0].assertIsOn()
        composeRule.activityRule.scenario.recreate()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.hasAnyText("Proyecto final") || composeRule.hasAnyText("Hola, ")
        }
        if (!composeRule.hasAnyText("Proyecto final")) {
            composeRule.tapText("Tareas")
        }
        composeRule.waitForText("Proyecto final")
        composeRule.onAllNodes(isToggleable(), useUnmergedTree = true)[0].assertIsOn()
        composeRule.tapContentDescription("Eliminar tarea")
        composeRule.waitForText("¿Eliminar tarea?")
        composeRule.tapText("Eliminar")
        composeRule.waitForText("Aún no tienes tareas reales.")

        composeRule.tapText("Gastos")
        composeRule.waitForText("Aún no tienes gastos reales.")
        composeRule.tapText("Registrar gasto")

        composeRule.waitForText("Registrar gasto")
        composeRule.inputTextField(index = 0, value = "12000")
        composeRule.tapText("Transporte")
        composeRule.tapText("Guardar gasto")

        composeRule.waitForText("Transporte")
        composeRule.waitForText("\$12.000")
        composeRule.tapContentDescription("Editar gasto")
        composeRule.waitForText("Editar gasto")
        composeRule.inputTextField(index = 0, value = "15000", clear = true)
        composeRule.tapText("Materiales")
        composeRule.tapText("Guardar cambios")

        composeRule.waitForText("Materiales")
        composeRule.waitForText("\$15.000")
        composeRule.activityRule.scenario.recreate()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.hasAnyText("Materiales") || composeRule.hasAnyText("Hola, ")
        }
        if (!composeRule.hasAnyText("Materiales")) {
            composeRule.tapText("Gastos")
        }
        composeRule.waitForText("Materiales")
        composeRule.waitForText("\$15.000")
        composeRule.tapContentDescription("Eliminar gasto")
        composeRule.waitForText("¿Eliminar gasto?")
        composeRule.tapText("Eliminar")
        composeRule.waitForText("Aún no tienes gastos reales.")

        composeRule.tapText("Materias")
        composeRule.waitForText("Fisica")
        composeRule.tapText("Fisica")
        composeRule.tapContentDescription("Opciones de materia")
        composeRule.tapText("Eliminar materia")
        composeRule.waitForText("¿Eliminar materia?")
        composeRule.tapText("Eliminar")
        composeRule.waitForText("Aún no tienes materias.")

        composeRule.tapText("Inicio")
        composeRule.tapContentDescription("Perfil")
        composeRule.waitForText("Perfil")
        composeRule.scrollToText("Entregas y pendientes.")
        composeRule.tapText("Entregas y pendientes.")
        composeRule.waitForTextCount("Tareas", count = 1)
        composeRule.tapText("Entregas y pendientes.")
        composeRule.waitForTextCount("Tareas", count = 2)
        composeRule.scrollToText("Nombre preferido")
        composeRule.inputTextField(index = 0, value = "QA Tester", clear = true)
        composeRule.tapText("Guardar nombre")
        composeRule.waitForText("QA Tester")
        composeRule.scrollToText("Reiniciar onboarding")
        composeRule.tapText("Reiniciar onboarding")
        composeRule.waitForText("¿Reiniciar onboarding?")
        composeRule.tapText("Reiniciar")
        composeRule.waitForText("Bienvenido a UniStack")
    }
}

private fun ComposeTestRule.waitForText(text: String, timeoutMillis: Long = 12_000) {
    waitUntil(timeoutMillis = timeoutMillis) {
        hasAnyText(text)
    }
}

private fun ComposeTestRule.hasAnyText(text: String): Boolean =
    try {
        onAllNodesWithText(text, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    } catch (_: IllegalStateException) {
        false
    }

private fun ComposeTestRule.waitForTextCount(text: String, count: Int, timeoutMillis: Long = 12_000) {
    waitUntil(timeoutMillis = timeoutMillis) {
        onAllNodesWithText(text, useUnmergedTree = true).fetchSemanticsNodes().size == count
    }
}

private fun ComposeTestRule.tapText(text: String, occurrence: Int = 0) {
    waitForText(text)
    onAllNodesWithText(text, useUnmergedTree = true)[occurrence].performTouchInput {
        click()
    }
}

private fun ComposeTestRule.tapContentDescription(description: String, occurrence: Int = 0) {
    waitUntil(timeoutMillis = 12_000) {
        onAllNodesWithContentDescription(description, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
    }
    onAllNodesWithContentDescription(description, useUnmergedTree = true)[occurrence].performTouchInput {
        click()
    }
}

private fun ComposeTestRule.scrollToText(text: String) {
    onAllNodes(hasScrollAction(), useUnmergedTree = true)[0].performScrollToNode(hasText(text))
    waitForText(text)
}

private fun ComposeTestRule.toggleAt(index: Int) {
    onAllNodes(isToggleable(), useUnmergedTree = true)[index].performClick()
}

private fun ComposeTestRule.inputTextField(index: Int, value: String, clear: Boolean = false) {
    val field = onAllNodes(hasSetTextAction(), useUnmergedTree = true)[index]
    if (clear) {
        field.performTextClearance()
    }
    field.performTextInput(value)
}
