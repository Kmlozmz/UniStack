package com.unistack.app.feature_profile.presentation

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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_profile.domain.FeatureGate
import com.unistack.app.feature_profile.domain.ProBenefit

@Composable
fun ProScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)
    var showUpgradeDialog by rememberSaveable { mutableStateOf(false) }

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
            item { ProHeroCard(onUpgradeClick = { showUpgradeDialog = true }) }
            item {
                Text(
                    text = "Beneficios Pro",
                    color = UniStackColors.TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            items(FeatureGate.proBenefits) { benefit ->
                BenefitCard(benefit = benefit)
            }
            item { BillingPlaceholderCard() }
        }
    }

    if (showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            title = { Text("Upgrade próximamente") },
            text = { Text("La estructura Pro ya está preparada, pero Billing aún no está integrado.") },
            confirmButton = {
                TextButton(onClick = { showUpgradeDialog = false }) {
                    Text("Entendido", color = UniStackColors.Primary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = UniStackColors.Card
        )
    }
}

@Composable
private fun ProHeroCard(onUpgradeClick: () -> Unit) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        brush = Brush.linearGradient(
            listOf(
                UniStackColors.Primary,
                Color(0xFF816DFB),
                Color(0xFFB8A9FF)
            )
        ),
        shape = AppShapes.LargeCard,
        tonalElevation = 8.dp,
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "UniStack Pro",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Listo para desbloquear materias ilimitadas, reportes y herramientas académicas avanzadas.",
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 14.sp,
                lineHeight = 19.sp
            )
            Button(
                onClick = onUpgradeClick,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = UniStackColors.Primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = "Upgrade próximamente",
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun BenefitCard(benefit: ProBenefit) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = AppShapes.MediumCard,
        tonalElevation = 4.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(UniStackColors.PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = UniStackColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(
                modifier = Modifier.padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(benefit.title, color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                Text(
                    benefit.description,
                    color = UniStackColors.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun BillingPlaceholderCard() {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.SurfaceVariant,
        shape = AppShapes.LargeCard,
        tonalElevation = 0.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.CreditCard, contentDescription = null, tint = UniStackColors.Primary)
            Column(
                modifier = Modifier.padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("Billing no implementado", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                Text("Esta pantalla deja la estructura lista sin activar pagos reales.", color = UniStackColors.TextSecondary, fontSize = 12.sp)
            }
        }
    }
}
