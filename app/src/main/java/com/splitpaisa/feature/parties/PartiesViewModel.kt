package com.splitpaisa.feature.parties

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.splitpaisa.data.repo.PartiesRepository
import com.splitpaisa.data.repo.PartyWithMembersBasic
import com.splitpaisa.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

 data class PartiesUiState(val parties: List<PartyWithMembersBasic> = emptyList())

class PartiesViewModel(private val repository: PartiesRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PartiesUiState())
    val uiState: StateFlow<PartiesUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.observeParties().collect { list ->
                _uiState.value = PartiesUiState(list)
            }
        }
    }

    companion object {
        fun factory(context: android.content.Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = ServiceLocator.partiesRepository(context)
                    @Suppress("UNCHECKED_CAST")
                    return PartiesViewModel(repo) as T
                }
            }
    }
}
