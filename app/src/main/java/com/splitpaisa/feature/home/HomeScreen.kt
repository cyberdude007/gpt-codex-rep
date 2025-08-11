package com.splitpaisa.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.splitpaisa.feature.settings.SettingsViewModel
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun HomeScreen(viewModel: HomeViewModel, settingsViewModel: SettingsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val settings by settingsViewModel.uiState.collectAsState()
    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            currency = Currency.getInstance("INR")
        }
    }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("This month", style = MaterialTheme.typography.titleLarge)
        Row {
            Text("Spent: ")
            if (!settings.hideAmounts) {
                Text(formatter.format(state.totalExpense / 100.0), color = Color.Red)
            }
        }
        Row {
            Text("Income: ")
            if (!settings.hideAmounts) {
                Text(formatter.format(state.totalIncome / 100.0), color = Color.Green)
            }
        }
        val net = state.totalIncome - state.totalExpense
        Row {
            Text("Net: ")
            if (!settings.hideAmounts) {
                Text(
                    formatter.format(net / 100.0),
                    color = if (net >= 0) Color.Green else Color.Red
                )
            }
        }
        Divider()
        LazyColumn {
            items(state.transactions) { t ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column { Text(t.title); Text(t.type.name) }
                    if (!settings.hideAmounts) {
                        val color = if (t.type.name == "EXPENSE") Color.Red else Color.Green
                        Text(formatter.format(t.amountPaise / 100.0), color = color)
                    }
                }
            }
        }
    }
}
