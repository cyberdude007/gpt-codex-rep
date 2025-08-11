package com.splitpaisa.feature.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        currency = Currency.getInstance("INR")
    }
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Stats", style = MaterialTheme.typography.titleLarge)
        Text("Total expense: ${formatter.format(state.totalExpense / 100.0)}", color = Color.Red)
        Text("Total income: ${formatter.format(state.totalIncome / 100.0)}", color = Color.Green)
    }
}
