package com.splitpaisa.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.splitpaisa.core.model.TransactionType
import com.splitpaisa.data.local.db.PaisaSplitDatabase
import com.splitpaisa.data.local.entity.AccountEntity
import com.splitpaisa.data.local.entity.CategoryEntity
import com.splitpaisa.data.local.entity.PartyEntity
import com.splitpaisa.data.local.entity.PartyMemberEntity
import com.splitpaisa.data.local.entity.SplitEntity
import com.splitpaisa.data.local.entity.TransactionEntity
import com.splitpaisa.core.search.TextNormalizer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DatabaseTest {
    private lateinit var db: PaisaSplitDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, PaisaSplitDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun accountAndCategoryRoundTrip() = runBlocking {
        db.accountDao().insertAll(listOf(AccountEntity("a1", "Wallet", "CASH")))
        db.categoryDao().upsert(listOf(CategoryEntity("c1", "Food", TransactionType.EXPENSE, "ic", "#fff", null, TextNormalizer.normalize("Food"))))

        val accounts = db.accountDao().getAll().first()
        val categories = db.categoryDao().getAll().first()

        assertEquals(1, accounts.size)
        assertEquals("a1", accounts.first().id)
        assertEquals(1, categories.size)
        assertEquals("c1", categories.first().id)
    }

    @Test
    fun transactionQueries() = runBlocking {
        val t1 = TransactionEntity("t1", TransactionType.EXPENSE, "Lunch", 5000, 1, "c1", "a1", null, null, null, null, null)
        val t2 = TransactionEntity("t2", TransactionType.INCOME, "Salary", 10000, 2, "c2", "a1", null, null, null, null, null)
        db.transactionDao().upsert(listOf(t1, t2))

        val sumExpense = db.transactionDao().sumByTypeInRange("EXPENSE", 0, 5)
        val sumIncome = db.transactionDao().sumByTypeInRange("INCOME", 0, 5)
        assertEquals(5000, sumExpense)
        assertEquals(10000, sumIncome)

        val recent = db.transactionDao().getRecent(1).first()
        assertEquals("t2", recent.first().id)
    }

    @Test
    fun partyAndSplitLinkage() = runBlocking {
        val party = PartyEntity("p1", "Trip", 0)
        db.partyDao().upsert(party)
        val members = listOf(
            PartyMemberEntity("m1", "p1", "A", null, TextNormalizer.normalize("A")),
            PartyMemberEntity("m2", "p1", "B", null, TextNormalizer.normalize("B"))
        )
        db.partyMemberDao().upsert(members)
        val tx = TransactionEntity("t1", TransactionType.EXPENSE, "Taxi", 1000, 1, null, null, "p1", null, null, null, null)
        db.transactionDao().upsert(tx)
        db.splitDao().upsert(listOf(
            SplitEntity("s1", "t1", "m1", 500),
            SplitEntity("s2", "t1", "m2", 500)
        ))

        val parties = db.partyDao().getPartiesWithMembers().first()
        assertEquals(1, parties.size)
        assertEquals(2, parties.first().members.size)
        val splits = db.splitDao().byTransaction("t1").first()
        assertEquals(2, splits.size)
    }
}
