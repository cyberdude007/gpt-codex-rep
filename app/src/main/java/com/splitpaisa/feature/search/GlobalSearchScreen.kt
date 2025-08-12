package com.splitpaisa.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.splitpaisa.core.model.Category
import com.splitpaisa.core.model.Party
import com.splitpaisa.data.repo.TransactionWithJoins

@Composable
fun GlobalSearchScreen(
    viewModel: GlobalSearchViewModel,
    onTransaction: (TransactionWithJoins) -> Unit,
    onCategory: (Category) -> Unit,
    onParty: (Party) -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    val recents by viewModel.recent.collectAsState()
    Column(modifier.fillMaxSize().padding(16.dp)) {
        TextField(value = query, onValueChange = viewModel::onQueryChange, label = { Text("Search") })
        if (query.isBlank()) {
            recents.forEach { q ->
                Text(q, modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.onQueryChange(q) })
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 80.dp), modifier = Modifier.weight(1f)) {
                if (results.transactions.isNotEmpty()) {
                    item { Text("Transactions", modifier = Modifier.padding(vertical = 8.dp)) }
                    items(results.transactions, key = { it.transaction.id }) { tx ->
                        Text(tx.transaction.title, modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTransaction(tx); viewModel.submit() }
                            .padding(vertical = 8.dp))
                        Divider()
                    }
                }
                if (results.categories.isNotEmpty()) {
                    item { Text("Categories", modifier = Modifier.padding(vertical = 8.dp)) }
                    items(results.categories, key = { it.id }) { cat ->
                        Text(cat.name, modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategory(cat); viewModel.submit() }
                            .padding(vertical = 8.dp))
                        Divider()
                    }
                }
                if (results.parties.isNotEmpty()) {
                    item { Text("Parties", modifier = Modifier.padding(vertical = 8.dp)) }
                    items(results.parties, key = { it.id }) { party ->
                        Text(party.name, modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onParty(party); viewModel.submit() }
                            .padding(vertical = 8.dp))
                        Divider()
                    }
                }
            }
        }
    }
}
