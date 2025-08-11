package com.splitpaisa.feature.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(viewModel: AddViewModel, onDone: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Add") }) }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::setTitle,
                label = { Text("Title") }
            )
            OutlinedTextField(
                value = state.amount,
                onValueChange = viewModel::setAmount,
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Button(onClick = onDone, enabled = state.isValid) {
                Text("Save")
            }
        }
    }
}
