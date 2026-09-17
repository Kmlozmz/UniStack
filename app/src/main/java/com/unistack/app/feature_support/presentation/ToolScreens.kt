@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.unistack.app.feature_support.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.UniCard

import androidx.compose.material3.MaterialTheme
import com.unistack.app.core.design.theme.LocalSectionColors
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
/**
 * Lo que se está construyendo, contado sin fingir que ya está.
 *
 * Una pantalla propia y no una fila apagada: el panel lleva a algún sitio siempre, y ese sitio
 * explica qué va a hacer la función y en qué punto está.
 */
@Composable
private fun ComingSoonScreen(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    headline: String,
    body: String,
    plans: List<String>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SupportScaffold(
        title = title,
        subtitle = subtitle,
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        item {
            UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(46.dp)
                                .clip(MaterialTheme.shapes.large)
                                .background(accent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(headline, color = MaterialTheme.colorScheme.onSurface, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                            Text(stringResource(R.string.tool_under_construction), color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 19.sp)
                }
            }
        }
        item {
            UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text(stringResource(R.string.tool_what_it_will_bring), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                    plans.forEach { plan ->
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                Modifier
                                    .padding(top = 6.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(accent)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(plan, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiAssistantScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    ComingSoonScreen(
        title = stringResource(R.string.tool_assistant_title),
        subtitle = stringResource(R.string.tool_assistant_subtitle),
        icon = Icons.Rounded.AutoAwesome,
        accent = MaterialTheme.colorScheme.primary,
        headline = stringResource(R.string.tool_assistant_headline),
        body = stringResource(R.string.tool_assistant_body),
        plans = listOf(
            stringResource(R.string.tool_assistant_plan1),
            stringResource(R.string.tool_assistant_plan2),
            stringResource(R.string.tool_assistant_plan3)
        ),
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@Composable
fun LabsScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    ComingSoonScreen(
        title = stringResource(R.string.tool_labs_title),
        subtitle = stringResource(R.string.tool_labs_subtitle),
        icon = Icons.Rounded.Science,
        accent = LocalSectionColors.current.atRisk,
        headline = stringResource(R.string.tool_labs_headline),
        body = stringResource(R.string.tool_labs_body),
        plans = listOf(
            stringResource(R.string.tool_labs_plan1),
            stringResource(R.string.tool_labs_plan2),
            stringResource(R.string.tool_labs_plan3)
        ),
        onBackClick = onBackClick,
        modifier = modifier
    )
}
