package com.splitpaisa.feature.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun FilteredTransactionsScreen(viewModel: FilteredTransactionsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        currency = Currency.getInstance("INR")
    }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Transactions", style = MaterialTheme.typography.titleLarge)
        LazyColumn {
            items(state.transactions) { wrapper ->
                val t = wrapper.transaction
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column { Text(t.title); Text(t.type.name) }
                    val color = if (t.type.name == "EXPENSE") Color.Red else Color.Green
                    Text(formatter.format(t.amountPaise / 100.0), color = color)
                }
            }
        }
    }
}

