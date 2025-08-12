package com.splitpaisa.data

import com.splitpaisa.data.local.dao.CategoryDao
import com.splitpaisa.data.local.dao.PartyDao
import com.splitpaisa.data.local.dao.RecentSearchDao
import com.splitpaisa.data.local.dao.TransactionDao
import com.splitpaisa.data.local.entity.CategoryEntity
import com.splitpaisa.data.local.entity.PartyEntity
import com.splitpaisa.data.local.entity.RecentSearchEntity
import com.splitpaisa.data.local.entity.TransactionEntity
import com.splitpaisa.data.repo.SearchRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GlobalSearchTest {
    class InMemoryRecent : RecentSearchDao {
        private val map = LinkedHashMap<String, Long>()
        override fun recent(limit: Int): Flow<List<String>> =
            flowOf(map.entries.sortedByDescending { it.value }.map { it.key }.take(limit))
        override suspend fun upsert(entity: RecentSearchEntity) { map[entity.query] = entity.updatedAt }
    }

    class StubTxDao : TransactionDao {
        override suspend fun upsert(transactions: List<TransactionEntity>) = Unit
        override suspend fun upsert(transaction: TransactionEntity) = Unit
        override suspend fun deleteById(id: String) = Unit
        override fun getRecent(limit: Int): Flow<List<TransactionEntity>> = flowOf(emptyList())
        override suspend fun sumByTypeInRange(type: String, start: Long, end: Long): Long = 0
        override fun byMonth(start: Long, end: Long): Flow<List<TransactionEntity>> = flowOf(emptyList())
        override fun byParty(partyId: String): Flow<List<TransactionEntity>> = flowOf(emptyList())
        override fun spendByCategory(start: Long, end: Long): Flow<List<com.splitpaisa.data.local.dao.CategorySliceEntity>> = flowOf(emptyList())
        override fun monthlyTrend(start: Long, end: Long): Flow<List<com.splitpaisa.data.local.dao.MonthTrendEntity>> = flowOf(emptyList())
        override fun budgetVsActual(start: Long, end: Long): Flow<List<com.splitpaisa.data.local.dao.BudgetBarEntity>> = flowOf(emptyList())
        override fun topCategories(start: Long, end: Long, limit: Int): Flow<List<com.splitpaisa.data.local.dao.TopCatEntity>> = flowOf(emptyList())
        override fun listTransactions(start: Long, end: Long, categoryId: String?): Flow<List<TransactionEntity>> = flowOf(emptyList())
        override suspend fun searchTransactions(needle: String, limit: Int): List<TransactionEntity> = emptyList()
    }

    class StubCategoryDao : CategoryDao {
        override suspend fun upsert(categories: List<CategoryEntity>) {}
        override fun getAll(): Flow<List<CategoryEntity>> = flowOf(emptyList())
        override fun getExpenseCategories(): Flow<List<CategoryEntity>> = flowOf(emptyList())
        override fun getIncomeCategories(): Flow<List<CategoryEntity>> = flowOf(emptyList())
        override suspend fun prefilterByName(needle: String, limit: Int): List<CategoryEntity> = emptyList()
        override suspend fun getAllOnce(): List<CategoryEntity> = emptyList()
    }

    class StubPartyDao : PartyDao {
        override suspend fun upsert(party: PartyEntity) {}
        override fun getAll(): Flow<List<PartyEntity>> = flowOf(emptyList())
        override fun getPartiesWithMembers(): Flow<List<com.splitpaisa.data.local.entity.PartyWithMembers>> = flowOf(emptyList())
        override suspend fun prefilterByName(needle: String, limit: Int): List<PartyEntity> = emptyList()
        override suspend fun getAllOnce(): List<PartyEntity> = emptyList()
    }

    @Test
    fun recentQueriesDeduped() = runTest {
        val repo = SearchRepositoryImpl(StubTxDao(), StubCategoryDao(), StubPartyDao(), InMemoryRecent())
        repo.recordRecentQuery("alpha")
        repo.recordRecentQuery("beta")
        repo.recordRecentQuery("alpha")
        val list = repo.recentQueries().first()
        assertEquals(listOf("alpha", "beta"), list)
    }
}
