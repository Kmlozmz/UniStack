@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.unistack.app.feature_support.presentation

import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.core.design.components.dismissKeyboardOnTapOutside
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_user.domain.GradingScale

/** Las tres preguntas que sabe responder la calculadora. */
enum class CalculatorTab(val label: String) {
    SUBJECT("Materia"),
    SEMESTER("Semestre"),
    NEEDED("Me falta")
}

/**
 * La calculadora: tres cuentas distintas con un teclado común.
 *
 * Era una tabla en blanco de tres filas y nueve campos de texto que pedía créditos para sacar
 * el promedio de **una** materia —una pregunta donde los créditos no pintan nada—. Ahora cada
 * pregunta tiene su pestaña y sus datos: porcentajes en Materia, créditos en Semestre, una
 * meta en «Me falta».
 *
 * El teclado va abajo y fijo. Con campos de texto, meter diez notas eran diez enfoques y diez
 * veces el teclado de Android tapando media pantalla; aquí son tres toques por nota y la
 * pantalla entera sigue a la vista.
 */
@Composable
fun GpaCalculatorScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GpaCalculatorViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
    val passing = profile?.passingGrade ?: (maxGrade * 0.6)
    val target = profile?.targetAverage ?: (maxGrade * 0.8)

    var tab by rememberSaveable { mutableStateOf(CalculatorTab.SUBJECT) }
    val holder = rememberSaveableStateHolder()
    var toast by remember { mutableStateOf<String?>(null) }
    var helpOpen by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            // El nombre de la materia abre el teclado de texto, y ese teclado tapaba el de la
            // calculadora y aplastaba la tarjeta del resultado contra la cabecera. Con esto la
            // zona de escritura sube por encima y la lista se queda con lo que reste.
            .imePadding()
            // Tocar fuera del nombre suelta el foco y baja el teclado. Sin esto había que
            // darle a la flecha de Android para recuperar la calculadora.
            .dismissKeyboardOnTapOutside()
    ) {
        CalculatorHeader(onBackClick = onBackClick, onHelpClick = { helpOpen = true })
        UniSegmentedControl(
            selected = tab,
            options = CalculatorTab.entries.map { UniSegmentedOption(value = it, label = it.label) },
            onSelected = {
                tab = it
                toast = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.screenHorizontal)
        )

        /*
         * Lo tecleado sobrevive al cambio de pestana.
         *
         * Cada pestana guarda lo suyo con `rememberSaveable`, pero al cambiar de una a otra la
         * que se va sale de la composicion y se lleva su estado: volver a Materia despues de
         * mirar Semestre encontraba la lista vacia. `SaveableStateHolder` guarda lo de cada una
         * bajo su clave y se lo devuelve al volver.
         *
         * Solo mientras estas dentro: al salir de la calculadora se pierde todo, que es lo que
         * tiene que pasar. Es una calculadora, no un cuaderno.
         */
        holder.SaveableStateProvider(tab) {
        when (tab) {
            CalculatorTab.SUBJECT -> SubjectCalculator(
                maxGrade = maxGrade,
                scale = scale,
                toast = toast,
                onToast = { toast = it }
            )
            CalculatorTab.SEMESTER -> SemesterCalculator(
                maxGrade = maxGrade,
                scale = scale,
                passing = passing,
                target = target,
                available = subjects,
                toast = toast,
                onToast = { toast = it }
            )
            CalculatorTab.NEEDED -> NeededCalculator(
                maxGrade = maxGrade,
                scale = scale,
                defaultTarget = target,
                toast = toast,
                onToast = { toast = it }
            )
        }
        }
    }

    if (helpOpen) {
        CalculatorHelpSheet(tab = tab, maxGrade = maxGrade, scale = scale) { helpOpen = false }
    }
}

@Composable
private fun CalculatorHeader(onBackClick: () -> Unit, onHelpClick: () -> Unit) {
    val spacing = LocalInterfaceSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = spacing.screenHorizontal - 12.dp, end = spacing.screenHorizontal, top = 4.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UniIconButton(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Volver",
            onClick = onBackClick
        )
        Text(
            text = "Calculadora",
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        // Con rótulo no cabe, pero sin explicación un signo suelto no invita a nada: la hoja
        // que abre cuenta para qué sirve la pestaña en la que estás, con un ejemplo.
        UniIconButton(
            icon = Icons.Rounded.HelpOutline,
            contentDescription = "Cómo funciona esta pestaña",
            onClick = onHelpClick
        )
    }
}
