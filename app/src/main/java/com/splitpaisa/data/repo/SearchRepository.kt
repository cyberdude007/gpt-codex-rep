package com.splitpaisa.data.repo

import com.splitpaisa.core.model.Category
import com.splitpaisa.core.model.Party
import com.splitpaisa.core.model.Transaction
import com.splitpaisa.core.search.Fuzzy
import com.splitpaisa.core.search.TextNormalizer
import com.splitpaisa.data.local.dao.CategoryDao
import com.splitpaisa.data.local.dao.PartyDao
import com.splitpaisa.data.local.dao.RecentSearchDao
import com.splitpaisa.data.local.dao.TransactionDao
import com.splitpaisa.data.local.entity.RecentSearchEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class SearchResults(
    val transactions: List<Transaction>,
    val categories: List<Category>,
    val parties: List<Party>
)

interface SearchRepository {
    fun searchAll(query: String, limitPerType: Int = 10): Flow<SearchResults>
    suspend fun recordRecentQuery(query: String)
    fun recentQueries(limit: Int = 10): Flow<List<String>>
}

class SearchRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val partyDao: PartyDao,
    private val transactionDao: TransactionDao,
    private val recentDao: RecentSearchDao,
) : SearchRepository {
    override fun searchAll(query: String, limitPerType: Int): Flow<SearchResults> = flow {
        if (query.isBlank()) {
            emit(SearchResults(emptyList(), emptyList(), emptyList()))
        } else {
            val pattern = TextNormalizer.pattern(query)
            val catEntities = categoryDao.search(pattern, limitPerType * 5)
            val partyEntities = partyDao.search(pattern, limitPerType * 5)
            val txEntities = transactionDao.search(pattern, limitPerType * 5)
            val categories = rank(query, catEntities) { it.name }.map { it.toModel() }.take(limitPerType)
            val parties = rank(query, partyEntities) { it.name }.map { it.toModel() }.take(limitPerType)
            val txs = rank(query, txEntities) { it.title }.map { it.toModel() }.take(limitPerType)
            emit(SearchResults(txs, categories, parties))
        }
    }

    override suspend fun recordRecentQuery(query: String) {
        val norm = TextNormalizer.normalize(query)
        recentDao.upsert(RecentSearchEntity(norm, System.currentTimeMillis()))
    }

    override fun recentQueries(limit: Int): Flow<List<String>> = recentDao.recentQueries(limit)

    private fun <T> rank(query: String, list: List<T>, name: (T) -> String): List<T> {
        return list.map { it to Fuzzy.rank(query, name(it)) }
            .sortedWith(
                compareByDescending<Pair<T, Int>> { it.second }
                    .thenBy { name(it.first).length }
                    .thenBy { name(it.first) }
            ).map { it.first }
    }
}
