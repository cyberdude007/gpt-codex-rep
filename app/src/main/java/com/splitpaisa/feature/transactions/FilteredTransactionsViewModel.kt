package com.splitpaisa.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.splitpaisa.data.repo.TxFilter
import com.splitpaisa.data.repo.TransactionWithJoins
import com.splitpaisa.data.repo.TransactionsRepository
import com.splitpaisa.di.ServiceLocator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class FilteredTxUiState(val transactions: List<TransactionWithJoins> = emptyList())

class FilteredTransactionsViewModel(
    private val repo: TransactionsRepository,
    private val filter: TxFilter,
) : ViewModel() {
    val uiState: StateFlow<FilteredTxUiState> = repo.listTransactions(filter)
        .map { FilteredTxUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FilteredTxUiState())

    companion object {
        fun factory(context: android.content.Context, filter: TxFilter): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = ServiceLocator.transactionsRepository(context)
                    @Suppress("UNCHECKED_CAST")
                    return FilteredTransactionsViewModel(repo, filter) as T
                }
            }
    }
}

