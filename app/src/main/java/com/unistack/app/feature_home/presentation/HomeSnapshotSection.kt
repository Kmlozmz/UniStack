package com.unistack.app.feature_home.presentation

import com.unistack.app.core.design.theme.AppShapes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.expressivePress
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.CurrencyFormatter
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_home.domain.HomeSummary
import com.unistack.app.feature_user.domain.AppModule

@Composable
internal fun SemesterSnapshot(
    summary: HomeSummary,
    compact: Boolean,
    onSubjectsClick: () -> Unit,
    onTasksClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onWorksClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // El valor es la cifra a secas y nada más. Antes cada casilla llevaba etiqueta, valor y
    // una tercera línea de matiz, y con textos como «Sin notas» la cifra dejaba de leerse de
    // un vistazo, que es lo único que un tablero tiene que hacer. Cuando no hay dato se pone
    // una raya: comunica «todavía nada» sin ocupar el sitio del número.
    val averageText = summary.generalAverage
        ?.let { GradingScaleUtils.formatGrade(it, summary.gradingScale) }
        ?: "—"
    val pendingValue = when {
        summary.overdueTasks > 0 -> summary.overdueTasks
        summary.tasksToday > 0 -> summary.tasksToday
        else -> summary.pendingTasks
    }
    val pendingLabel = when {
        summary.overdueTasks > 0 -> "Vencidas"
        summary.tasksToday > 0 -> "Para hoy"
        else -> "Pendientes"
    }
    val worksText = when {
        summary.openAcademicWorks > 0 -> summary.openAcademicWorks.toString()
        summary.nextAcademicWork != null -> "1"
        else -> "—"
    }
    val moneyText = when {
        AppModule.EXPENSES !in summary.enabledModules -> "—"
        summary.weeklyExpenseTotal > 0 -> CurrencyFormatter.formatCop(summary.weeklyExpenseTotal)
        else -> "—"
    }
    val showWorks = AppModule.ACADEMIC_TEMPLATES in summary.enabledModules &&
        (summary.openAcademicWorks > 0 || summary.nextAcademicWork != null)

    // Rejilla y separación comparten medida: el hueco entre columnas es el mismo que entre
    // filas y que el que separa el título de las casillas. Con tres valores distintos, como
    // había antes, la rejilla se leía torcida aunque cada pieza estuviera bien.
    val gap = if (compact) 10.dp else 12.dp

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(gap)) {
        // Sin «Actualizado»: era una etiqueta fija que no informaba de nada —no había un
        // «desactualizado» posible— y ocupaba el sitio de la derecha como si fuera una acción.
        Text(
            text = "Tu semestre",
            color = HomeText,
            fontSize = 15.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(gap), modifier = Modifier.fillMaxWidth()) {
            SnapshotMetric(
                label = "Materias",
                value = summary.subjectsCount.toString(),
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                accent = HomePurple,
                compact = compact,
                onClick = onSubjectsClick,
                modifier = Modifier.weight(1f)
            )
            SnapshotMetric(
                label = "Promedio",
                value = averageText,
                icon = Icons.AutoMirrored.Rounded.TrendingUp,
                accent = HomeTeal,
                compact = compact,
                onClick = onSubjectsClick,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(gap), modifier = Modifier.fillMaxWidth()) {
            SnapshotMetric(
                label = pendingLabel,
                value = pendingValue.toString(),
                icon = Icons.AutoMirrored.Rounded.EventNote,
                accent = HomeYellow,
                compact = compact,
                onClick = onTasksClick,
                modifier = Modifier.weight(1f)
            )
            SnapshotMetric(
                label = if (showWorks) "Trabajos" else "Gastos",
                value = if (showWorks) worksText else moneyText,
                icon = if (showWorks) Icons.Rounded.Description else Icons.Rounded.Wallet,
                accent = HomeCoral,
                compact = compact,
                onClick = if (showWorks) onWorksClick else onExpensesClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Casilla del tablero: chip de color arriba, cifra grande y etiqueta debajo.
 *
 * La cifra manda y por eso va sola en su línea, en el color del texto y no en el del acento:
 * el acento ya lo lleva el chip, y repetirlo en el número hacía que las cuatro casillas
 * compitieran entre sí en vez de leerse como una rejilla.
 */
@Composable
private fun SnapshotMetric(
    label: String,
    value: String,
    icon: ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // El relleno es igual en los cuatro lados: la casilla es un cuadrado de contenido y
    // cualquier asimetría se nota al ponerlas en rejilla.
    val inset = if (compact) 14.dp else 16.dp
    Column(
        modifier = modifier
            .clip(AppShapes.SmallCard)
            .background(HomeSnapshotTile)
            // Contorno de un pelo: sin él las casillas y el fondo quedaban casi al mismo
            // tono y la rejilla se leía como una mancha en vez de como cuatro piezas.
            .border(1.dp, HomeBorder, AppShapes.SmallCard)
            .expressivePress(onClick = onClick)
            .padding(inset),
        verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 32.dp else 36.dp)
                .clip(AppShapes.Small)
                .background(accent.copy(alpha = if (UniStackColors.IsDarkTheme) 0.22f else 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(if (compact) 17.dp else 19.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                value,
                color = HomeText,
                fontSize = if (compact) 21.sp else 23.sp,
                lineHeight = if (compact) 25.sp else 27.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                label,
                color = HomeMuted,
                fontSize = if (compact) 11.sp else 12.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
