package com.splitpaisa.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.splitpaisa.core.model.TransactionType
import com.splitpaisa.data.local.db.PaisaSplitDatabase
import com.splitpaisa.data.local.entity.CategoryEntity
import com.splitpaisa.core.search.TextNormalizer
import com.splitpaisa.data.local.entity.TransactionEntity
import com.splitpaisa.data.repo.TxFilter
import com.splitpaisa.data.repo.TransactionsRepository
import com.splitpaisa.data.repo.TransactionsRepositoryImpl
import com.splitpaisa.data.repo.lastNMonthsBounds
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FilteringTest {
    private lateinit var db: PaisaSplitDatabase
    private lateinit var repo: TransactionsRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, PaisaSplitDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = TransactionsRepositoryImpl(
            db,
            db.transactionDao(),
            db.categoryDao(),
            db.partyDao(),
            db.splitDao()
        )
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun listTransactions_filtersByCategoryAndDate() = runBlocking {
        val cat1 = CategoryEntity("c1", "Food", TransactionType.EXPENSE, "ic", "#ff0000", null, TextNormalizer.normalize("Food"))
        val cat2 = CategoryEntity("c2", "Travel", TransactionType.EXPENSE, "ic", "#00ff00", null, TextNormalizer.normalize("Travel"))
        db.categoryDao().upsert(listOf(cat1, cat2))
        val bounds = lastNMonthsBounds(1).first()
        db.transactionDao().upsert(listOf(
            TransactionEntity("t1", TransactionType.EXPENSE, "A", 1000, bounds.start + 1, "c1", null, null, null, null, null, null),
            TransactionEntity("t2", TransactionType.EXPENSE, "B", 2000, bounds.start - 1000, "c1", null, null, null, null, null, null),
            TransactionEntity("t3", TransactionType.EXPENSE, "C", 3000, bounds.start + 2, "c2", null, null, null, null, null, null)
        ))
        val list = repo.listTransactions(TxFilter(bounds.start, bounds.end, "c1")).first()
        assertEquals(1, list.size)
        assertEquals("t1", list.first().transaction.id)
    }
}

