package com.splitpaisa.feature.parties

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.splitpaisa.core.model.PartyMember
import com.splitpaisa.data.repo.PartiesRepository
import com.splitpaisa.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MemberSearchViewModel(
    private val repository: PartiesRepository,
    private val existing: List<PartyMember>
) : ViewModel() {
    private val query = MutableStateFlow("")
    val queryText: StateFlow<String> = query
    private val _selected = MutableStateFlow<List<PartyMember>>(emptyList())
    val selected: StateFlow<List<PartyMember>> = _selected

    val results: StateFlow<List<MemberResult>> = query.debounce(150).flatMapLatest { q ->
        repository.searchMembers(q).map { list ->
            val existingIds = existing.map { it.id }.toSet()
            list.filter { it.id !in existingIds && it.id !in _selected.value.map { m -> m.id } }
                .map { MemberResult(it, existingIds.contains(it.id)) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(q: String) { query.value = q }

    fun toggle(member: PartyMember) {
        _selected.value = if (_selected.value.any { it.id == member.id }) {
            _selected.value.filterNot { it.id == member.id }
        } else {
            _selected.value + member
        }
    }

    data class MemberResult(val member: PartyMember, val alreadyInParty: Boolean)

    companion object {
        fun factory(context: android.content.Context, existing: List<PartyMember>): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = ServiceLocator.partiesRepository(context)
                    @Suppress("UNCHECKED_CAST")
                    return MemberSearchViewModel(repo, existing) as T
                }
            }
    }
}
