package com.splitpaisa.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.splitpaisa.data.repo.BudgetBar
import com.splitpaisa.data.repo.CategorySlice
import com.splitpaisa.data.repo.MonthPoint
import com.splitpaisa.data.repo.TxFilter
import com.splitpaisa.data.repo.TopCat
import com.splitpaisa.data.repo.TransactionsRepository
import com.splitpaisa.data.repo.lastNMonthsBounds
import com.splitpaisa.data.repo.thisMonth
import com.splitpaisa.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

enum class RangeSelection { THIS_MONTH, LAST_SIX_MONTHS, CUSTOM }

data class StatsUiState(
    val selection: RangeSelection,
    val start: Long,
    val end: Long,
    val spendByCategory: List<CategorySlice> = emptyList(),
    val monthlyTrend: List<MonthPoint> = emptyList(),
    val budgetBars: List<BudgetBar> = emptyList(),
    val topCategories: List<TopCat> = emptyList(),
)

class StatsViewModel(private val repository: TransactionsRepository) : ViewModel() {
    private val range = MutableStateFlow(thisMonth())
    private val selection = MutableStateFlow(RangeSelection.THIS_MONTH)

    private val spendByCategory = range.flatMapLatest { (s, e) ->
        repository.observeSpendByCategory(s, e)
    }
    private val budget = range.flatMapLatest { (s, e) ->
        repository.observeBudgetVsActual(s, e)
    }
    private val top = range.flatMapLatest { (s, e) ->
        repository.observeTopCategories(s, e)
    }
    private val monthly = repository.observeMonthlySpendIncome(6)

    val uiState: StateFlow<StatsUiState> = combine(
        range, selection, spendByCategory, monthly, budget
    ) { r, sel, sbc, m, b ->
        StatsUiState(sel, r.first, r.second, sbc, m, b, emptyList())
    }.combine(top) { state, t ->
        state.copy(topCategories = t)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        StatsUiState(
            selection = RangeSelection.THIS_MONTH,
            start = range.value.first,
            end = range.value.second,
        ),
    )

    fun setThisMonth() {
        selection.value = RangeSelection.THIS_MONTH
        range.value = thisMonth()
    }

    fun setLastSixMonths() {
        selection.value = RangeSelection.LAST_SIX_MONTHS
        val bounds = lastNMonthsBounds(6)
        range.value = bounds.first().start to bounds.last().end
    }

    fun setCustom(start: Long, end: Long) {
        selection.value = RangeSelection.CUSTOM
        range.value = start to end
    }

    fun filterForCategory(categoryId: String?): TxFilter {
        val (s, e) = range.value
        return TxFilter(s, e, categoryId)
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

