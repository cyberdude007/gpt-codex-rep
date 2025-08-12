package com.splitpaisa.data.repo

import com.splitpaisa.core.model.Category
import com.splitpaisa.core.model.Party
import com.splitpaisa.data.local.dao.CategoryDao
import com.splitpaisa.data.local.dao.PartyDao
import com.splitpaisa.data.local.dao.RecentSearchDao
import com.splitpaisa.data.local.dao.TransactionDao
import com.splitpaisa.data.local.entity.RecentSearchEntity
import com.splitpaisa.core.search.Fuzzy
import com.splitpaisa.core.search.TextNormalizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class SearchResults(
    val transactions: List<TransactionWithJoins> = emptyList(),
    val categories: List<Category> = emptyList(),
    val parties: List<Party> = emptyList(),
)

interface SearchRepository {
    fun searchAll(query: String, limitPerType: Int = 10): Flow<SearchResults>
    suspend fun recordRecentQuery(query: String)
    fun recentQueries(limit: Int = 10): Flow<List<String>>
    fun recentEntities(limit: Int = 10): Flow<List<SearchSuggestion>>
}

data class SearchSuggestion(val type: String, val id: String, val title: String)

class SearchRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val partyDao: PartyDao,
    private val recentDao: RecentSearchDao,
) : SearchRepository {
    override fun searchAll(query: String, limitPerType: Int): Flow<SearchResults> = flow {
        val needle = TextNormalizer.normalize(query)
        if (needle.isBlank()) {
            emit(SearchResults())
        } else {
            val qTokens = TextNormalizer.tokenize(needle)
            val catMap = categoryDao.getAllOnce().associateBy { it.id }
            val partyMap = partyDao.getAllOnce().associateBy { it.id }

            val txCandidates = transactionDao.searchTransactions(needle, limitPerType * 5)
            val rankedTx = txCandidates.map { tx ->
                val candidate = buildString {
                    append(tx.title)
                    catMap[tx.categoryId]?.name?.let { append(' ').append(it) }
                    partyMap[tx.partyId]?.name?.let { append(' ').append(it) }
                }
                val score = Fuzzy.rankTokens(qTokens, TextNormalizer.tokenize(candidate))
                TransactionWithJoins(tx.toModel(), catMap[tx.categoryId]?.toModel(), partyMap[tx.partyId]?.toModel()) to score
            }.sortedWith(compareBy<Pair<TransactionWithJoins, Int>> { it.second }
                .thenBy { it.first.transaction.title.length }
                .thenBy { it.first.transaction.title })
                .take(limitPerType)
                .map { it.first }

            val catCandidates = categoryDao.prefilterByName(needle, limitPerType * 5)
            val rankedCats = catCandidates.map { cat ->
                val score = Fuzzy.rankTokens(qTokens, TextNormalizer.tokenize(cat.name))
                cat.toModel() to score
            }.sortedWith(compareBy<Pair<Category, Int>> { it.second }
                .thenBy { it.first.name.length }
                .thenBy { it.first.name })
                .take(limitPerType)
                .map { it.first }

            val partyCandidates = partyDao.prefilterByName(needle, limitPerType * 5)
            val rankedParties = partyCandidates.map { party ->
                val score = Fuzzy.rankTokens(qTokens, TextNormalizer.tokenize(party.name))
                party.toModel() to score
            }.sortedWith(compareBy<Pair<Party, Int>> { it.second }
                .thenBy { it.first.name.length }
                .thenBy { it.first.name })
                .take(limitPerType)
                .map { it.first }

            emit(SearchResults(rankedTx, rankedCats, rankedParties))
        }
    }

    override suspend fun recordRecentQuery(query: String) {
        val norm = TextNormalizer.normalize(query)
        if (norm.isBlank()) return
        recentDao.upsert(RecentSearchEntity(norm, System.currentTimeMillis()))
    }

    override fun recentQueries(limit: Int): Flow<List<String>> = recentDao.recent(limit)

    override fun recentEntities(limit: Int): Flow<List<SearchSuggestion>> = flow { emit(emptyList()) }
}
