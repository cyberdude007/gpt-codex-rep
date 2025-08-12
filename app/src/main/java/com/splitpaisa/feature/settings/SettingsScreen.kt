package com.splitpaisa.feature.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.splitpaisa.core.csv.CsvReader
import com.splitpaisa.core.prefs.ThemeMode
import com.splitpaisa.core.prefs.SettingsRepository
import com.splitpaisa.core.prefs.settingsDataStore
import com.splitpaisa.core.security.AppLockManager

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) {
            Toast.makeText(context, "Exported", Toast.LENGTH_SHORT).show()
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use {
                val preview = CsvReader.preview(it)
                Toast.makeText(context, "Imported ${preview.size} rows", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "Settings", style = MaterialTheme.typography.titleLarge)

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Offline-only", modifier = Modifier.weight(1f))
            Switch(checked = state.offlineOnly, onCheckedChange = { viewModel.toggleOfflineOnly(it) })
        }

        Text("Theme", style = MaterialTheme.typography.titleMedium)
        for (mode in ThemeMode.values()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = state.themeMode == mode, onClick = { viewModel.setTheme(mode) })
                Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Hide amounts on Home", modifier = Modifier.weight(1f))
            Switch(checked = state.hideAmounts, onCheckedChange = { viewModel.toggleHideAmounts(it) })
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("App lock", modifier = Modifier.weight(1f))
            val showDialog = remember { mutableStateOf(false) }
            val lockManager = remember { AppLockManager(context, SettingsRepository(context.settingsDataStore)) }
            Switch(checked = state.appLockEnabled, onCheckedChange = {
                if (it) showDialog.value = true else viewModel.setAppLock(false)
            })
            if (showDialog.value) {
                PinSetDialog(onSet = { pin ->
                    lockManager.savePin(pin)
                    viewModel.setAppLock(true)
                    showDialog.value = false
                }, onCancel = {
                    showDialog.value = false
                })
            }
        }
        if (state.appLockEnabled) {
            var minsText by remember { mutableStateOf(state.autoLockMinutes.toString()) }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Auto-lock minutes", modifier = Modifier.weight(1f))
                TextField(value = minsText, onValueChange = {
                    minsText = it
                    it.toIntOrNull()?.let(viewModel::setAutoLock)
                })
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Data", style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { exportLauncher.launch("paisa-export.zip") }) { Text("Export") }
            Button(onClick = { importLauncher.launch(arrayOf("text/*", "application/zip")) }) { Text("Import") }
        }

        Spacer(Modifier.height(8.dp))
        Text("Notifications", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Budget 75%", modifier = Modifier.weight(1f))
            Switch(checked = state.budgetAlert75, onCheckedChange = { viewModel.setBudgetAlerts(it, state.budgetAlert100) })
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Budget 100%", modifier = Modifier.weight(1f))
            Switch(checked = state.budgetAlert100, onCheckedChange = { viewModel.setBudgetAlerts(state.budgetAlert75, it) })
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Settle-up reminders", modifier = Modifier.weight(1f))
            Switch(checked = state.settleUpReminder, onCheckedChange = { viewModel.setSettleReminder(it) })
        }

        Spacer(Modifier.height(8.dp))
        Text("Date format: ${state.dateFormat}")
    }
}

@Composable
fun PinSetDialog(onSet: (String) -> Unit, onCancel: () -> Unit) {
    val pin1 = remember { mutableStateOf("") }
    val pin2 = remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            Button(onClick = { if (pin1.value == pin2.value && pin1.value.length >= 4) onSet(pin1.value) }) {
                Text("Set")
            }
        },
        dismissButton = { Button(onClick = onCancel) { Text("Cancel") } },
        title = { Text("Set PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = pin1.value, onValueChange = { pin1.value = it }, label = { Text("PIN") })
                TextField(value = pin2.value, onValueChange = { pin2.value = it }, label = { Text("Confirm PIN") })
            }
        }
    )
}
