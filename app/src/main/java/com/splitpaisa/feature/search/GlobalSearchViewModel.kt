package com.splitpaisa.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.splitpaisa.data.repo.SearchRepository
import com.splitpaisa.data.repo.SearchResults
import com.splitpaisa.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GlobalSearchViewModel(private val repository: SearchRepository) : ViewModel() {
    val query = MutableStateFlow("")

    val results: StateFlow<SearchResults> = query.debounce(150).flatMapLatest { q ->
        if (q.isBlank()) flowOf(SearchResults()) else repository.searchAll(q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResults())

    val recent: StateFlow<List<String>> = repository.recentQueries().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun onQueryChange(q: String) { query.value = q }

    fun submit() {
        val q = query.value
        viewModelScope.launch { repository.recordRecentQuery(q) }
    }

    companion object {
        fun factory(context: android.content.Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = ServiceLocator.searchRepository(context)
                    @Suppress("UNCHECKED_CAST")
                    return GlobalSearchViewModel(repo) as T
                }
            }
    }
}
