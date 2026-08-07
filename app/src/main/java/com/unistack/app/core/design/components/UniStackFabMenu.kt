package com.unistack.app.core.design.components

import com.unistack.app.core.design.theme.AppShapes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.UniStackColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class FabMenuItem(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val contentDescription: String,
    val onClick: () -> Unit
)

@Composable
fun UniStackFabMenu(
    onAddGradeClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    modifier: Modifier = Modifier,
    showAddGrade: Boolean = true,
    showAddTask: Boolean = true,
    showAddExpense: Boolean = true,
    showAddSubject: Boolean = false,
    expandedBottomPadding: Dp = 22.dp,
    expandedEndPadding: Dp = 20.dp
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var renderMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrimAlpha by animateFloatAsState(
        targetValue = if (expanded) 0.18f else 0f,
        animationSpec = tween(durationMillis = 240, easing = LinearOutSlowInEasing),
        label = "fabMenuScrimAlpha"
    )
    val items = buildList {
        if (showAddGrade) {
            add(
                FabMenuItem(
                    label = "Agregar nota",
                    icon = Icons.Rounded.Add,
                    color = UniStackColors.Primary,
                    contentDescription = "Agregar nota",
                    onClick = onAddGradeClick
                )
            )
        }
        if (showAddTask) {
            add(
                FabMenuItem(
                    label = "Nueva tarea",
                    icon = Icons.AutoMirrored.Rounded.Assignment,
                    color = UniStackColors.Blue,
                    contentDescription = "Nueva tarea",
                    onClick = onAddTaskClick
                )
            )
        }
        if (showAddExpense) {
            add(
                FabMenuItem(
                    label = "Registrar gasto",
                    icon = Icons.Rounded.AccountBalanceWallet,
                    color = UniStackColors.Coral,
                    contentDescription = "Registrar gasto",
                    onClick = onAddExpenseClick
                )
            )
        }
        if (showAddSubject) {
            add(
                FabMenuItem(
                    label = "Nueva materia",
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    color = UniStackColors.Green,
                    contentDescription = "Nueva materia",
                    onClick = onAddSubjectClick
                )
            )
        }
    }

    if (items.isEmpty()) return

    fun openMenu() {
        scope.launch {
            renderMenu = true
            delay(32)
            expanded = true
        }
    }

    fun closeMenu() {
        expanded = false
    }

    fun selectItem(item: FabMenuItem) {
        expanded = false
        item.onClick()
    }

    LaunchedEffect(expanded, renderMenu) {
        if (expanded && !renderMenu) {
            renderMenu = true
        } else if (!expanded && renderMenu) {
            delay(320)
            renderMenu = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (renderMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(UniStackColors.Scrim.copy(alpha = scrimAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = ::closeMenu
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = expandedEndPadding, bottom = expandedBottomPadding + 72.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items.forEachIndexed { index, item ->
                    FabMenuOption(
                        item = item,
                        index = index,
                        totalItems = items.size,
                        expanded = expanded,
                        onSelected = { selectItem(item) }
                    )
                }
            }
        }

        FabMenuButton(
            expanded = expanded,
            onClick = {
                if (expanded) closeMenu() else openMenu()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = expandedEndPadding, bottom = expandedBottomPadding)
        )
    }
}

@Composable
private fun FabMenuOption(
    item: FabMenuItem,
    index: Int,
    totalItems: Int,
    expanded: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enterDelay = (totalItems - index - 1) * 52
    val exitDelay = index * 20

    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn(
            animationSpec = tween(190, delayMillis = enterDelay, easing = LinearOutSlowInEasing)
        ) + scaleIn(
            initialScale = 0.58f,
            transformOrigin = TransformOrigin(1f, 1f),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + slideInVertically(
            initialOffsetY = { it + 76 + (totalItems - index) * 14 },
            animationSpec = tween(300, delayMillis = enterDelay, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(
            animationSpec = tween(120, delayMillis = exitDelay, easing = FastOutSlowInEasing)
        ) + scaleOut(
            targetScale = 0.76f,
            transformOrigin = TransformOrigin(1f, 1f),
            animationSpec = tween(170, delayMillis = exitDelay, easing = FastOutSlowInEasing)
        ) + slideOutVertically(
            targetOffsetY = { it / 2 },
            animationSpec = tween(170, delayMillis = exitDelay, easing = FastOutSlowInEasing)
        )
    ) {
        Surface(
            onClick = onSelected,
            modifier = modifier.semantics { contentDescription = item.contentDescription },
            shape = AppShapes.MediumCard,
            color = if (UniStackColors.IsDarkTheme) {
                UniStackColors.Card
            } else {
                MaterialTheme.colorScheme.surface
            },
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, UniStackColors.SoftOutline.copy(alpha = 0.44f))
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 9.dp, end = 9.dp, bottom = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.label,
                    color = UniStackColors.TextPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(item.color.copy(alpha = if (UniStackColors.IsDarkTheme) 0.20f else 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FabMenuButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "fabIconRotation"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = AppShapes.MediumCard,
        containerColor = UniStackColors.Primary,
        contentColor = UniStackColors.OnPrimary
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = if (expanded) "Cerrar menú de acciones" else "Abrir menú de acciones",
            modifier = Modifier
                .size(27.dp)
                .graphicsLayer { rotationZ = rotation }
        )
    }
}
