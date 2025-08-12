package com.splitpaisa.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.splitpaisa.core.model.TransactionType
import com.splitpaisa.data.local.db.PaisaSplitDatabase
import com.splitpaisa.data.local.entity.CategoryEntity
import com.splitpaisa.core.search.TextNormalizer
import com.splitpaisa.data.local.entity.TransactionEntity
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
class StatsAggregationTest {
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
    fun tearDown() {
        db.close()
    }

    @Test
    fun spendByCategory_sumsAndSorts() = runBlocking {
        val food = CategoryEntity("c1", "Food", TransactionType.EXPENSE, "ic", "#ff0000", null, TextNormalizer.normalize("Food"))
        val travel = CategoryEntity("c2", "Travel", TransactionType.EXPENSE, "ic", "#00ff00", null, TextNormalizer.normalize("Travel"))
        db.categoryDao().upsert(listOf(food, travel))
        val (start, end) = lastNMonthsBounds(1).first().run { start to end }
        db.transactionDao().upsert(listOf(
            TransactionEntity("t1", TransactionType.EXPENSE, "A", 1000, start + 1, "c1", null, null, null, null, null, null),
            TransactionEntity("t2", TransactionType.EXPENSE, "B", 2000, start + 2, "c2", null, null, null, null, null, null)
        ))
        val slices = repo.observeSpendByCategory(start, end).first()
        assertEquals(2, slices.size)
        assertEquals("c2", slices.first().categoryId)
    }

    @Test
    fun monthlyTrend_gapFilled() = runBlocking {
        val bounds = lastNMonthsBounds(3)
        val food = CategoryEntity("c1", "Food", TransactionType.EXPENSE, "ic", "#ff0000", null, TextNormalizer.normalize("Food"))
        db.categoryDao().upsert(listOf(food))
        db.transactionDao().upsert(
            TransactionEntity("t1", TransactionType.EXPENSE, "A", 1000, bounds[0].start + 1, "c1", null, null, null, null, null, null)
        )
        val trend = repo.observeMonthlySpendIncome(3).first()
        assertEquals(3, trend.size)
        assertEquals(1000, trend[0].spendPaise)
        assertEquals(0, trend[1].spendPaise)
    }

    @Test
    fun budgetVsActual_basic() = runBlocking {
        val cat = CategoryEntity("c1", "Food", TransactionType.EXPENSE, "ic", "#ff0000", 2000, TextNormalizer.normalize("Food"))
        db.categoryDao().upsert(listOf(cat))
        val (start, end) = lastNMonthsBounds(1).first().run { start to end }
        db.transactionDao().upsert(
            TransactionEntity("t1", TransactionType.EXPENSE, "A", 2500, start + 1, "c1", null, null, null, null, null, null)
        )
        val bars = repo.observeBudgetVsActual(start, end).first()
        assertEquals(1, bars.size)
        assertEquals(2500, bars.first().actualPaise)
    }

    @Test
    fun topCategories_limit() = runBlocking {
        val food = CategoryEntity("c1", "Food", TransactionType.EXPENSE, "ic", "#ff0000", null, TextNormalizer.normalize("Food"))
        val travel = CategoryEntity("c2", "Travel", TransactionType.EXPENSE, "ic", "#00ff00", null, TextNormalizer.normalize("Travel"))
        val rent = CategoryEntity("c3", "Rent", TransactionType.EXPENSE, "ic", "#0000ff", null, TextNormalizer.normalize("Rent"))
        db.categoryDao().upsert(listOf(food, travel, rent))
        val (start, end) = lastNMonthsBounds(1).first().run { start to end }
        db.transactionDao().upsert(listOf(
            TransactionEntity("t1", TransactionType.EXPENSE, "A", 1000, start + 1, "c1", null, null, null, null, null, null),
            TransactionEntity("t2", TransactionType.EXPENSE, "B", 2000, start + 2, "c2", null, null, null, null, null, null),
            TransactionEntity("t3", TransactionType.EXPENSE, "C", 3000, start + 3, "c3", null, null, null, null, null, null)
        ))
        val top = repo.observeTopCategories(start, end, limit = 2).first()
        assertEquals(2, top.size)
        assertEquals("c3", top[0].categoryId)
    }
}

