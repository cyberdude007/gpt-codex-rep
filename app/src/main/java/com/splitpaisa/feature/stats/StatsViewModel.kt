package com.splitpaisa.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.splitpaisa.data.repo.TransactionsRepository
import com.splitpaisa.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

 data class StatsUiState(
    val totalExpense: Long = 0,
    val totalIncome: Long = 0
)

class StatsViewModel(private val repository: TransactionsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState

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
            repository.observeMonthSummary(start, end).collect { summary ->
                _uiState.value = StatsUiState(summary.spentPaise, summary.incomePaise)
            }
        }
    }

    companion object {
        fun factory(context: android.content.Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = ServiceLocator.transactionsRepository(context)
                    @Suppress("UNCHECKED_CAST")
                    return StatsViewModel(repo) as T
                }
            }
    }
}
