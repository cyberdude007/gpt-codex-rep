package com.splitpaisa.feature.add

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AddUiState(
    val title: String = "",
    val amount: String = "",
    val isValid: Boolean = false
)

class AddViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AddUiState())
    val uiState: StateFlow<AddUiState> = _uiState

    fun setTitle(value: String) {
        _uiState.value = _uiState.value.copy(title = value).validate()
    }

    fun setAmount(value: String) {
        _uiState.value = _uiState.value.copy(amount = value).validate()
    }

    private fun AddUiState.validate(): AddUiState {
        val valid = title.isNotBlank() && amount.toLongOrNull()?.let { it > 0 } == true
        return copy(isValid = valid)
    }
}
