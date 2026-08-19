package com.unistack.app.feature_profile.presentation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.di.rememberUniStackEntryPoint
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.design.components.SquishyButton
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_billing.domain.BillingState
import com.unistack.app.feature_profile.domain.FeatureGate
import com.unistack.app.feature_profile.domain.ProBenefit

@Composable
fun ProScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entryPoint = rememberUniStackEntryPoint()
    val billingRepository = remember { entryPoint.billingRepository() }
    val billingState by billingRepository.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findActivity()

    LaunchedEffect(Unit) {
        billingRepository.start()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = scrollBottomRoom),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                }
            }
            item {
                ProHeroCard(
                    billingState = billingState,
                    onUpgradeClick = {
                        val product = billingState.products.firstOrNull() ?: return@ProHeroCard
                        val host = activity ?: return@ProHeroCard
                        billingRepository.launchPurchase(host, product.productId)
                    }
                )
            }
            item {
                Text(
                    text = "Beneficios Pro",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            items(FeatureGate.proBenefits) { benefit ->
                BenefitCard(benefit = benefit)
            }
            item {
                BillingStatusCard(
                    billingState = billingState,
                    onRefreshClick = billingRepository::refreshPurchases
                )
            }
        }
    }
}

@Composable
private fun ProHeroCard(
    billingState: BillingState,
    onUpgradeClick: () -> Unit
) {
    val product = billingState.products.firstOrNull()
    val canBuy = product != null && billingState.isBillingAvailable && !billingState.isLoading && !billingState.isPro

    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        shape = AppShapes.LargeCard,
        tonalElevation = 8.dp,
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "UniStack Pro",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = if (billingState.isPro) {
                    "Tu plan Pro está activo en este dispositivo."
                } else {
                    "Desbloquea materias ilimitadas para organizar todos tus semestres."
                },
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f),
                fontSize = 14.sp,
                lineHeight = 19.sp
            )
            SquishyButton(
                onClick = onUpgradeClick,
                enabled = canBuy,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = when {
                        billingState.isPro -> "Pro activo"
                        billingState.isLoading -> "Cargando oferta..."
                        product != null -> "Activar por ${product.price}"
                        else -> "Oferta no disponible"
                    },
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
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = AppShapes.MediumCard,
        tonalElevation = 4.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(
                modifier = Modifier.padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(benefit.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                Text(
                    benefit.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun BillingStatusCard(
    billingState: BillingState,
    onRefreshClick: () -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = if (billingState.errorMessage == null) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.errorContainer,
        shape = AppShapes.LargeCard,
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.CreditCard,
                    contentDescription = null,
                    tint = if (billingState.errorMessage == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Column(
                    modifier = Modifier.padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = when {
                            billingState.isPro -> "Compra verificada"
                            billingState.isLoading -> "Consultando Google Play"
                            billingState.isBillingAvailable -> "Google Play Billing disponible"
                            else -> "Google Play Billing no disponible"
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = billingState.errorMessage
                            ?: billingState.message
                            ?: billingState.products.firstOrNull()?.description
                            ?: "Verifica que la app esté instalada desde una cuenta con acceso al producto Pro.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
            SquishyButton(
                onClick = onRefreshClick,
                enabled = !billingState.isLoading,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Revisar estado", modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
