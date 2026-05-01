package com.unistack.app.feature_profile.presentation

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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.AppContainer
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_profile.domain.UserPlan

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val profile by AppContainer.userRepository.userProfile.collectAsState()
    val plan = UserPlan(isPro = false, maxSubjects = 5)
    val name = profile?.preferredName?.takeIf { it.isNotBlank() } ?: "Estudiante"
    val benefits = listOf(
        "Materias ilimitadas",
        "Exportar PDF",
        "Simulador avanzado",
        "Recordatorios inteligentes",
        "Gráficos de gastos",
        "Plantillas premium",
        "Sin anuncios"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Perfil", color = UniStackColors.TextPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        }
        item {
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                brush = Brush.linearGradient(listOf(UniStackColors.PrimaryLight, UniStackColors.Card)),
                shape = AppShapes.LargeCard
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(UniStackColors.Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Person, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                    }
                    Column(modifier = Modifier.padding(start = 14.dp)) {
                        Text(name, color = UniStackColors.TextPrimary, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Estudiante UniStack", color = UniStackColors.TextSecondary)
                    }
                }
            }
        }
        item {
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                color = UniStackColors.Card,
                shape = AppShapes.LargeCard
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = UniStackColors.Primary)
                    Text("Plan actual: Gratis", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                    Text("${plan.maxSubjects} materias disponibles", color = UniStackColors.TextSecondary)
                    Button(
                        onClick = {},
                        shape = AppShapes.Pill,
                        colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary)
                    ) {
                        Text("Mejorar a Pro")
                    }
                }
            }
        }
        items(benefits) { benefit ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = UniStackColors.Green, modifier = Modifier.size(20.dp))
                Text(benefit, color = UniStackColors.TextPrimary, modifier = Modifier.padding(start = 10.dp), fontWeight = FontWeight.Medium)
            }
        }
    }
}
