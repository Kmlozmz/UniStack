package com.unistack.app.feature_home.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.BuildConfig
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.BuildStage

internal data class DrawerPanelAction(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val accent: Color,
    val badge: String? = null,
    /** Nulo cuando la función todavía no existe: la fila se pinta apagada y no responde. */
    val onClick: (() -> Unit)? = null
)

@Composable
internal fun HomeNavigationPanel(
    displayName: String,
    subjectsCount: Int,
    onClose: () -> Unit,
    onSemesterClick: () -> Unit,
    onWorksClick: () -> Unit,
    onTasksClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onDataClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onWhatsNewClick: () -> Unit,
    onResourcesClick: () -> Unit,
    onHelpClick: () -> Unit,
    onAboutClick: () -> Unit,
    onGpaClick: () -> Unit,
    onQuickNotesClick: () -> Unit,
    onAiClick: () -> Unit,
    onLabsClick: () -> Unit
) {
    val panelShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
    val drawerSurface = if (UniStackColors.IsDarkTheme) {
        UniStackColors.Card.copy(alpha = 0.98f)
    } else {
        UniStackColors.Card
    }
    val semesterChip = when (subjectsCount) {
        0 -> "Semestre activo"
        1 -> "1 materia"
        else -> "$subjectsCount materias"
    }

    /*
     * Lo que no está terminado solo se abre en dev y alpha.
     *
     * En una beta, las pantallas a medio hacer llegan apagadas y con su etiqueta: quien la usa
     * para su semestre no tiene por qué toparse con algo incompleto, y así las pruebas de
     * verdad se hacen donde toca. En dev y alpha se abre todo, que es para lo que están.
     *
     * Se marcan sin `onClick`: la fila se pinta apagada, sin flecha y sin responder al toque,
     * en vez de cerrar el panel como si la app hubiera fallado.
     */
    val unfinished = BuildStage.of(BuildConfig.VERSION_NAME).allowsUnfinished
    val productivity = listOf(
        DrawerPanelAction(
            icon = Icons.AutoMirrored.Rounded.MenuBook,
            title = "Materias",
            subtitle = "Notas, cortes y promedios",
            accent = UniStackColors.Primary,
            onClick = onSemesterClick
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.EditNote,
            title = "Tareas",
            subtitle = "Entregas y pendientes",
            accent = UniStackColors.Primary,
            onClick = onTasksClick
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.Description,
            title = "Trabajos",
            subtitle = "Plantillas y exportaciones",
            accent = UniStackColors.Primary,
            badge = if (unfinished) null else "Pronto",
            onClick = onWorksClick.takeIf { unfinished }
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.Calculate,
            title = "Calculadora GPA",
            subtitle = "Simula y calcula tu promedio",
            accent = UniStackColors.Primary,
            onClick = onGpaClick
        )
    )
    val preferences = listOf(
        DrawerPanelAction(
            icon = Icons.Rounded.Settings,
            title = "Configuración",
            subtitle = "Apariencia, recordatorios y módulos",
            accent = UniStackColors.Blue,
            onClick = onSettingsClick
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.Backup,
            title = "Sincronización",
            subtitle = "Respaldos, importar y exportar",
            accent = UniStackColors.Blue,
            onClick = onDataClick
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.School,
            title = "Tu perfil",
            subtitle = "Nombre, cuenta y meta",
            accent = UniStackColors.Blue,
            onClick = onProfileClick
        )
    )
    val uniPlus = listOf(
        DrawerPanelAction(
            icon = Icons.Rounded.AutoAwesome,
            title = "UniStack AI",
            subtitle = "Tu asistente académico",
            accent = UniStackColors.Primary,
            badge = if (unfinished) null else "Pronto",
            onClick = onAiClick.takeIf { unfinished }
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.History,
            title = "Historial",
            subtitle = "Avisos y actividad reciente",
            accent = UniStackColors.Green,
            onClick = onNotificationsClick
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.RocketLaunch,
            title = "Novedades",
            subtitle = "Qué trae cada versión",
            accent = UniStackColors.Blue,
            onClick = onWhatsNewClick
        )
    )
    val extras = listOf(
        DrawerPanelAction(
            icon = Icons.AutoMirrored.Rounded.MenuBook,
            title = "Recursos",
            subtitle = "Biblioteca y enlaces útiles",
            accent = UniStackColors.Green,
            onClick = onResourcesClick
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.EditNote,
            title = "Notas rápidas",
            subtitle = "Bloc de notas temporal",
            accent = UniStackColors.Green,
            onClick = onQuickNotesClick
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.Science,
            title = "Labs",
            subtitle = "Funciones experimentales",
            accent = UniStackColors.Yellow,
            badge = if (unfinished) null else "Pronto",
            onClick = onLabsClick.takeIf { unfinished }
        )
    )
    val support = listOf(
        DrawerPanelAction(
            icon = Icons.AutoMirrored.Rounded.Help,
            title = "Ayuda y soporte",
            subtitle = "Preguntas frecuentes y contacto",
            accent = UniStackColors.Blue,
            onClick = onHelpClick
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.Lightbulb,
            title = "Enviar sugerencia",
            subtitle = "Cuéntanos cómo podemos mejorar",
            accent = UniStackColors.Yellow,
            onClick = onHelpClick
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.Info,
            title = "Acerca de",
            subtitle = "Versión, datos y políticas",
            accent = UniStackColors.Primary,
            onClick = onAboutClick
        )
    )

    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.92f)
            .widthIn(max = 360.dp),
        drawerContainerColor = Color.Transparent
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 8.dp),
            shape = panelShape,
            color = drawerSurface,
            border = BorderStroke(1.dp, UniStackColors.SoftOutline.copy(alpha = 0.55f))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                item {
                    DrawerPanelHeader(
                        displayName = displayName,
                        semesterChip = semesterChip,
                        onSemesterClick = onSemesterClick
                    )
                }
                item { DrawerPanelDivider() }
                drawerSection("Productividad", productivity)
                drawerSection("Preferencias y datos", preferences)
                drawerSection("UNI+", uniPlus)
                drawerSection("Extras", extras)
                drawerSection("Soporte", support)
                item {
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        color = UniStackColors.TextSecondary.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 2.dp, top = 8.dp)
                    )
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.drawerSection(
    title: String,
    actions: List<DrawerPanelAction>
) {
    item { DrawerPanelSection(title) }
    actions.forEach { action ->
        item { DrawerPanelItem(action = action) }
    }
}

@Composable
private fun DrawerPanelHeader(
    displayName: String,
    semesterChip: String,
    onSemesterClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(AppShapes.MediumCard)
                .background(
                    SolidColor(UniStackColors.Primary)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.School,
                contentDescription = null,
                tint = UniStackColors.OnPrimary,
                modifier = Modifier.size(31.dp)
            )
        }
        Spacer(Modifier.width(13.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Centro UniStack",
                color = UniStackColors.TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
            Text(
                "Hola, $displayName 👋",
                color = UniStackColors.TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Surface(
                onClick = onSemesterClick,
                shape = RoundedCornerShape(7.dp),
                color = UniStackColors.Primary.copy(alpha = if (UniStackColors.IsDarkTheme) 0.18f else 0.12f)
            ) {
                Text(
                    semesterChip,
                    color = UniStackColors.Primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun DrawerPanelDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(UniStackColors.SoftOutline.copy(alpha = 0.48f))
    )
}

@Composable
private fun DrawerPanelSection(text: String) {
    Row(
        modifier = Modifier.padding(top = 6.dp, start = 1.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(UniStackColors.Primary)
        )
        Text(
            text = text.uppercase(),
            color = UniStackColors.Primary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.4.sp
        )
    }
}

@Composable
private fun DrawerPanelItem(action: DrawerPanelAction) {
    val enabled = action.onClick != null
    Surface(
        onClick = action.onClick ?: {},
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = AppShapes.MediumCard,
        color = UniStackColors.SurfaceVariant.copy(
            alpha = when {
                !enabled -> if (UniStackColors.IsDarkTheme) 0.28f else 0.45f
                UniStackColors.IsDarkTheme -> 0.58f
                else -> 0.82f
            }
        ),
        border = BorderStroke(1.dp, UniStackColors.SoftOutline.copy(alpha = if (enabled) 0.20f else 0.10f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DrawerIconTile(icon = action.icon, accent = action.accent)
            Spacer(Modifier.width(11.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        action.title,
                        color = UniStackColors.TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val badge = action.badge ?: "Pronto".takeIf { !enabled }
                    badge?.let {
                        Spacer(Modifier.width(6.dp))
                        DrawerBadge(text = it, accent = action.accent)
                    }
                }
                Text(
                    action.subtitle,
                    color = UniStackColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (enabled) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = UniStackColors.TextSecondary.copy(alpha = 0.76f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun DrawerIconTile(
    icon: ImageVector,
    accent: Color
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(AppShapes.Small)
            .background(
                SolidColor(accent.copy(alpha = if (UniStackColors.IsDarkTheme) 0.30f else 0.18f))
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun DrawerBadge(
    text: String,
    accent: Color
) {
    Surface(
        shape = RoundedCornerShape(7.dp),
        color = accent.copy(alpha = if (UniStackColors.IsDarkTheme) 0.18f else 0.13f)
    ) {
        Text(
            text = text,
            color = accent,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            maxLines = 1
        )
    }
}
