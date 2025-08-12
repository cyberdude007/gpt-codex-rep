package com.splitpaisa.feature.parties

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.splitpaisa.core.model.PartyMember

@Composable
fun MemberSearchScreen(
    viewModel: MemberSearchViewModel,
    onDone: (List<PartyMember>) -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.queryText.collectAsState()
    val selected by viewModel.selected.collectAsState()
    val results by viewModel.results.collectAsState()
    Column(modifier.fillMaxSize().padding(16.dp)) {
        TextField(value = query, onValueChange = viewModel::onQueryChange, label = { Text("Search members") })
        if (selected.isNotEmpty()) {
            Text("Selected (${selected.size})", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            selected.forEach { m ->
                Text(m.displayName, modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.toggle(m) })
            }
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp), modifier = Modifier.weight(1f)) {
            items(results, key = { it.member.id }) { res ->
                val mem = res.member
                Text(
                    mem.displayName + if (res.alreadyInParty) " (Already in party)" else "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !res.alreadyInParty) { viewModel.toggle(mem) }
                        .padding(vertical = 8.dp)
                )
                Divider()
            }
        }
        Button(onClick = { onDone(selected) }, enabled = selected.isNotEmpty(), modifier = Modifier.padding(top = 8.dp)) {
            Text("Add")
        }
    }
}
