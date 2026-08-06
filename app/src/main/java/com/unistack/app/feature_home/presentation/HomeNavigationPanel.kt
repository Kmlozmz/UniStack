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
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.BuildConfig
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors

internal data class DrawerPanelAction(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val accent: Color,
    val badge: String? = null,
    val onClick: () -> Unit
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
    onProfileClick: () -> Unit
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
    val mutedAction = onClose

    val productivity = listOf(
        DrawerPanelAction(
            icon = Icons.Rounded.Description,
            title = "Trabajos",
            subtitle = "Plantillas, entregas y exportaciones",
            accent = Color(0xFFB04CFF),
            onClick = onWorksClick
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.Calculate,
            title = "Calculadora GPA",
            subtitle = "Simula y calcula tu promedio",
            accent = Color(0xFFB04CFF),
            onClick = onSemesterClick
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.EditNote,
            title = "Notas rápidas",
            subtitle = "Bloc de notas temporal",
            accent = Color(0xFFD05CFF),
            onClick = onTasksClick
        )
    )
    val preferences = listOf(
        DrawerPanelAction(
            icon = Icons.Rounded.Settings,
            title = "Configuración",
            subtitle = "Apariencia, recordatorios y preferencias",
            accent = Color(0xFF58A6FF),
            onClick = onSettingsClick
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.Backup,
            title = "Sincronización",
            subtitle = "Respaldos, importar, exportar y más",
            accent = Color(0xFF58A6FF),
            onClick = onDataClick
        )
    )
    val uniPlus = listOf(
        DrawerPanelAction(
            icon = Icons.Rounded.AutoAwesome,
            title = "UniStack AI",
            subtitle = "Tu asistente académico potenciado con IA",
            accent = Color(0xFFB04CFF),
            badge = "NUEVO",
            onClick = mutedAction
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.History,
            title = "Historial",
            subtitle = "Actividad reciente y cambios realizados",
            accent = Color(0xFF1FD18B),
            onClick = onNotificationsClick
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.RocketLaunch,
            title = "Novedades",
            subtitle = "Descubre qué hay de nuevo",
            accent = Color(0xFF58A6FF),
            onClick = mutedAction
        )
    )
    val extras = listOf(
        DrawerPanelAction(
            icon = Icons.AutoMirrored.Rounded.MenuBook,
            title = "Recursos",
            subtitle = "Biblioteca y enlaces útiles",
            accent = Color(0xFF52D65E),
            onClick = mutedAction
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.Science,
            title = "Labs",
            subtitle = "Funciones experimentales y beta features",
            accent = Color(0xFFFFB13B),
            badge = "BETA",
            onClick = mutedAction
        )
    )
    val support = listOf(
        DrawerPanelAction(
            icon = Icons.AutoMirrored.Rounded.Help,
            title = "Ayuda y soporte",
            subtitle = "Centro de ayuda y contacto",
            accent = Color(0xFF58A6FF),
            onClick = mutedAction
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.Lightbulb,
            title = "Enviar sugerencia",
            subtitle = "Cuéntanos cómo podemos mejorar",
            accent = Color(0xFFFFC44D),
            onClick = mutedAction
        ),
        DrawerPanelAction(
            icon = Icons.Rounded.Info,
            title = "Acerca de",
            subtitle = "Versión, novedades y políticas",
            accent = Color(0xFFB04CFF),
            onClick = onProfileClick
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    DrawerLogoutButton(onClick = onClose)
                }
                item {
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        color = UniStackColors.TextSecondary.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 2.dp, top = 1.dp)
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
        item {
            DrawerPanelItem(action = action)
        }
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
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFB04CFF), Color(0xFF4C14D9))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.School,
                contentDescription = null,
                tint = Color.White,
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
            color = if (UniStackColors.IsDarkTheme) Color(0xFFA9B5FF) else UniStackColors.Primary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.4.sp
        )
    }
}

@Composable
private fun DrawerPanelItem(action: DrawerPanelAction) {
    Surface(
        onClick = action.onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = AppShapes.MediumCard,
        color = UniStackColors.SurfaceVariant.copy(alpha = if (UniStackColors.IsDarkTheme) 0.58f else 0.82f),
        border = BorderStroke(1.dp, UniStackColors.SoftOutline.copy(alpha = 0.20f))
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
                    action.badge?.let { badge ->
                        Spacer(Modifier.width(6.dp))
                        DrawerBadge(text = badge, accent = action.accent)
                    }
                }
                Text(
                    action.subtitle,
                    color = UniStackColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
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

@Composable
private fun DrawerIconTile(
    icon: ImageVector,
    accent: Color
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        accent.copy(alpha = if (UniStackColors.IsDarkTheme) 0.42f else 0.24f),
                        accent.copy(alpha = if (UniStackColors.IsDarkTheme) 0.16f else 0.12f)
                    )
                )
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

@Composable
internal fun DrawerLogoutButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .padding(top = 4.dp),
        shape = AppShapes.MediumCard,
        color = UniStackColors.Coral.copy(alpha = if (UniStackColors.IsDarkTheme) 0.08f else 0.06f),
        border = BorderStroke(1.dp, UniStackColors.Coral.copy(alpha = 0.58f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ExitToApp,
                contentDescription = null,
                tint = UniStackColors.Coral,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(14.dp))
            Text(
                "Cerrar sesión",
                color = UniStackColors.Coral,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
