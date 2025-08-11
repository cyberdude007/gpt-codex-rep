package com.splitpaisa.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.splitpaisa.core.model.TransactionType
import com.splitpaisa.data.seed.SeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StatsUiState(
    val totalExpense: Long = 0,
    val totalIncome: Long = 0
)

class StatsViewModel(private val repository: SeedRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState

    init {
        viewModelScope.launch {
            val seed = repository.getSeedData()
            val expenses = seed.transactions.filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amountPaise }
            val incomes = seed.transactions.filter { it.type == TransactionType.INCOME }
                .sumOf { it.amountPaise }
            _uiState.value = StatsUiState(expenses, incomes)
        }
    }

    companion object {
        fun factory(repository: SeedRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return StatsViewModel(repository) as T
                }
            }
    }
}
