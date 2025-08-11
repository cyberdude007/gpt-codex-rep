package com.splitpaisa.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "Settings", style = MaterialTheme.typography.titleLarge)

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Offline-only", modifier = Modifier.weight(1f))
            Switch(checked = state.offlineOnly, onCheckedChange = viewModel::toggleOfflineOnly)
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Hide amounts on Home", modifier = Modifier.weight(1f))
            Switch(checked = state.hideAmounts, onCheckedChange = viewModel::toggleHideAmounts)
        }

        Text("Theme", style = MaterialTheme.typography.titleMedium)
        ThemeMode.values().forEach { mode ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = state.theme == mode, onClick = { viewModel.setTheme(mode) })
                Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Date format: dd-MM-yyyy")
    }
}
