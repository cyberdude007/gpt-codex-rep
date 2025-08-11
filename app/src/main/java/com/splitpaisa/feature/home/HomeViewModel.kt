package com.splitpaisa.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.splitpaisa.data.repo.TransactionsRepository
import com.splitpaisa.di.ServiceLocator
import com.splitpaisa.core.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

 data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val totalExpense: Long = 0,
    val totalIncome: Long = 0
)

class HomeViewModel(private val repository: TransactionsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        viewModelScope.launch {
            repository.observeRecent().collect { list ->
                _uiState.update { it.copy(transactions = list.map { it.transaction }) }
            }
        }
        viewModelScope.launch {
            repository.observeMonthSummary(start, end).collect { summary ->
                _uiState.update { it.copy(totalExpense = summary.spentPaise, totalIncome = summary.incomePaise) }
            }
        }
    }

    companion object {
        fun factory(context: android.content.Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = ServiceLocator.transactionsRepository(context)
                    @Suppress("UNCHECKED_CAST")
                    return HomeViewModel(repo) as T
                }
            }
    }
}
