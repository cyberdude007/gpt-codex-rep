package com.splitpaisa.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.splitpaisa.core.model.TransactionType
import com.splitpaisa.core.search.TextNormalizer
import com.splitpaisa.data.local.db.PaisaSplitDatabase
import com.splitpaisa.data.local.entity.CategoryEntity
import com.splitpaisa.data.local.entity.PartyEntity
import com.splitpaisa.data.local.entity.TransactionEntity
import com.splitpaisa.data.repo.SearchRepository
import com.splitpaisa.data.repo.SearchRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GlobalSearchTest {
    private lateinit var db: PaisaSplitDatabase
    private lateinit var repo: SearchRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, PaisaSplitDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = SearchRepositoryImpl(db.categoryDao(), db.partyDao(), db.transactionDao(), db.recentSearchDao())
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun searchAndRecents() = runBlocking {
        val cat = CategoryEntity("c1", "Food", TransactionType.EXPENSE, "ic", "#ff0000", null, TextNormalizer.normalize("Food"))
        db.categoryDao().upsert(listOf(cat))
        db.partyDao().upsert(PartyEntity("p1", "Dinner Club", 0))
        db.transactionDao().upsert(
            TransactionEntity("t1", TransactionType.EXPENSE, "Dinner", 1000, 1, "c1", null, null, null, null, null, null)
        )
        val res = repo.searchAll("din").first()
        assertEquals(1, res.transactions.size)
        assertEquals("t1", res.transactions.first().id)
        repo.recordRecentQuery("din")
        repo.recordRecentQuery("food")
        repo.recordRecentQuery("din")
        val recents = repo.recentQueries().first()
        assertEquals(listOf("din", "food"), recents)
    }
}
