package com.splitpaisa.feature.parties

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.splitpaisa.core.model.Party
import com.splitpaisa.data.seed.SeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PartiesUiState(val party: Party? = null)

class PartiesViewModel(private val repository: SeedRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PartiesUiState())
    val uiState: StateFlow<PartiesUiState> = _uiState

    init {
        viewModelScope.launch {
            val seed = repository.getSeedData()
            _uiState.value = PartiesUiState(seed.party)
        }
    }

    companion object {
        fun factory(repository: SeedRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return PartiesViewModel(repository) as T
                }
            }
    }
}
