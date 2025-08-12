package com.splitpaisa.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LockScreen(onSubmit: (String) -> Unit) {
    val pin = remember { mutableStateOf("") }
    Column(modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Enter PIN")
        TextField(value = pin.value, onValueChange = { pin.value = it })
        Button(onClick = { onSubmit(pin.value) }, modifier = Modifier.fillMaxWidth()) { Text("Unlock") }
    }
}
