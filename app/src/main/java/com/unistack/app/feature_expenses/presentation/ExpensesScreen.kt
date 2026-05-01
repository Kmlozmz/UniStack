package com.unistack.app.feature_expenses.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCard
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.CurrencyFormatter

@Composable
fun ExpensesScreen(onAddExpenseClick: () -> Unit, modifier: Modifier = Modifier) {
    val expenses = listOf("Transporte" to 42000, "Comida" to 58000, "Copias" to 12000)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Gastos", color = UniStackColors.TextPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text("Registra gastos personales y académicos.", color = UniStackColors.TextSecondary)
            }
        }
        items(expenses) { expense ->
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                color = UniStackColors.Card,
                shape = AppShapes.MediumCard
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = UniStackColors.Primary)
                    Text(expense.first, color = UniStackColors.TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp).weight(1f))
                    Text(CurrencyFormatter.formatCop(expense.second), color = UniStackColors.Primary, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        item {
            Button(
                onClick = onAddExpenseClick,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Coral)
            ) {
                Icon(Icons.Rounded.AddCard, contentDescription = null)
                Spacer(modifier = Modifier.padding(3.dp))
                Text("Registrar gasto")
            }
        }
    }
}
