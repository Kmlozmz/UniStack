package com.unistack.app.feature_templates.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.feature_templates.domain.AcademicTemplateLibrary
import com.unistack.app.feature_templates.domain.ApaTip
import com.unistack.app.feature_templates.domain.ChecklistItem
import com.unistack.app.feature_templates.domain.EssayTemplate

@Composable
fun AcademicTemplatesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)
    val templates = AcademicTemplateLibrary.essayTemplates
    var selectedTemplateId by rememberSaveable { mutableStateOf(templates.first().id) }
    var completedChecklistIds by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var showExportDialog by rememberSaveable { mutableStateOf(false) }
    val selectedTemplate = templates.firstOrNull { it.id == selectedTemplateId } ?: templates.first()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = UniStackColors.Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(UniStackColors.Background)
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                }
            }
            item { HeaderCard() }
            item {
                ChecklistCard(
                    completedIds = completedChecklistIds,
                    onToggle = { item ->
                        completedChecklistIds = if (item.id in completedChecklistIds) {
                            completedChecklistIds - item.id
                        } else {
                            completedChecklistIds + item.id
                        }
                    }
                )
            }
            item {
                Text(
                    text = "Plantillas de ensayo",
                    color = UniStackColors.TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            item {
                TemplatePicker(
                    templates = templates,
                    selectedTemplateId = selectedTemplateId,
                    onTemplateSelected = { selectedTemplateId = it }
                )
            }
            item { TemplateDetailCard(template = selectedTemplate) }
            item {
                Text(
                    text = "Formato APA básico",
                    color = UniStackColors.TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            items(AcademicTemplateLibrary.apaTips) { tip ->
                ApaTipCard(tip = tip)
            }
            item {
                ExportPlaceholderCard(onExportClick = { showExportDialog = true })
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exportación preparada") },
            text = { Text("La estructura queda lista para PDF/Word, pero la exportación real se implementará en una fase posterior.") },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Entendido", color = UniStackColors.Primary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = UniStackColors.Card
        )
    }
}

@Composable
private fun HeaderCard() {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        brush = Brush.linearGradient(
            listOf(
                UniStackColors.PrimaryLight,
                Color(0xFFF7F2FF),
                Color.White
            )
        ),
        shape = AppShapes.LargeCard,
        tonalElevation = 6.dp,
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(
                icon = Icons.AutoMirrored.Rounded.Assignment,
                background = UniStackColors.Primary,
                tint = Color.White
            )
            Column(
                modifier = Modifier.padding(start = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Trabajos académicos",
                    color = UniStackColors.TextPrimary,
                    fontSize = 22.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Checklist, estructura de ensayo y guía rápida APA.",
                    color = UniStackColors.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ChecklistCard(
    completedIds: List<String>,
    onToggle: (ChecklistItem) -> Unit
) {
    val items = AcademicTemplateLibrary.checklist
    val completedCount = completedIds.size.coerceAtMost(items.size)

    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = AppShapes.LargeCard,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    icon = Icons.Rounded.CheckCircle,
                    background = UniStackColors.GreenLight,
                    tint = UniStackColors.Green
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text("Checklist de entrega", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                    Text("$completedCount de ${items.size} pasos listos", color = UniStackColors.TextSecondary, fontSize = 12.sp)
                }
            }
            items.forEach { item ->
                ChecklistRow(
                    item = item,
                    checked = item.id in completedIds,
                    onToggle = { onToggle(item) }
                )
            }
        }
    }
}

@Composable
private fun ChecklistRow(
    item: ChecklistItem,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.MediumCard)
            .background(if (checked) UniStackColors.GreenLight else UniStackColors.SurfaceVariant)
            .bounceClick(onToggle)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, color = UniStackColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(item.detail, color = UniStackColors.TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun TemplatePicker(
    templates: List<EssayTemplate>,
    selectedTemplateId: String,
    onTemplateSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        templates.forEach { template ->
            TemplateOption(
                template = template,
                selected = selectedTemplateId == template.id,
                onClick = { onTemplateSelected(template.id) }
            )
        }
    }
}

@Composable
private fun TemplateOption(
    template: EssayTemplate,
    selected: Boolean,
    onClick: () -> Unit
) {
    UniCard(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(onClick),
        color = if (selected) UniStackColors.PrimaryLight else UniStackColors.Card,
        shape = AppShapes.MediumCard,
        tonalElevation = if (selected) 5.dp else 2.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(
                icon = if (selected) Icons.Rounded.Star else Icons.AutoMirrored.Rounded.MenuBook,
                background = if (selected) UniStackColors.Primary else UniStackColors.SurfaceVariant,
                tint = if (selected) Color.White else UniStackColors.Primary
            )
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(template.title, color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                Text(
                    template.description,
                    color = UniStackColors.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = UniStackColors.TextSecondary)
        }
    }
}

@Composable
private fun TemplateDetailCard(template: EssayTemplate) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = AppShapes.LargeCard,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(template.title, color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            template.sections.forEachIndexed { index, section ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(UniStackColors.PrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", color = UniStackColors.Primary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Column(
                        modifier = Modifier.padding(start = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(section.title, color = UniStackColors.TextPrimary, fontWeight = FontWeight.Bold)
                        Text(section.prompt, color = UniStackColors.TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ApaTipCard(tip: ApaTip) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = AppShapes.MediumCard,
        tonalElevation = 3.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            IconBadge(
                icon = Icons.Rounded.Check,
                background = UniStackColors.BlueLight,
                tint = UniStackColors.Blue
            )
            Column(
                modifier = Modifier.padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(tip.title, color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                Text(tip.description, color = UniStackColors.TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
    }
}

@Composable
private fun ExportPlaceholderCard(onExportClick: () -> Unit) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.SurfaceVariant,
        shape = AppShapes.LargeCard,
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    icon = Icons.Rounded.AutoAwesome,
                    background = UniStackColors.PrimaryLight,
                    tint = UniStackColors.Primary
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text("Exportar PDF/Word", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                    Text("Preparado para una fase posterior.", color = UniStackColors.TextSecondary, fontSize = 12.sp)
                }
            }
            Button(
                onClick = onExportClick,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Exportar próximamente")
            }
        }
    }
}

@Composable
private fun IconBadge(
    icon: ImageVector,
    background: Color,
    tint: Color
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(21.dp))
    }
}
