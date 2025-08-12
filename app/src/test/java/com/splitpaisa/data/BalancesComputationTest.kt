package com.splitpaisa.data

import androidx.room.Room
import com.splitpaisa.core.model.PartyMember
import com.splitpaisa.data.local.db.PaisaSplitDatabase
import com.splitpaisa.data.repo.PartiesRepository
import com.splitpaisa.data.repo.PartiesRepositoryImpl
import com.splitpaisa.data.repo.TransactionsRepository
import com.splitpaisa.data.repo.TransactionsRepositoryImpl
import com.splitpaisa.data.repo.PartyExpenseParams
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import androidx.test.core.app.ApplicationProvider

class BalancesComputationTest {
    private lateinit var db: PaisaSplitDatabase
    private lateinit var partiesRepo: PartiesRepository
    private lateinit var transactionsRepo: TransactionsRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, PaisaSplitDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        partiesRepo = PartiesRepositoryImpl(db.partyDao(), db.partyMemberDao(), db.transactionDao(), db.splitDao(), db.settlementDao())
        transactionsRepo = TransactionsRepositoryImpl(db, db.transactionDao(), db.categoryDao(), db.partyDao(), db.splitDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun balancesReflectTransactionsAndSettlements() = runBlocking {
        val partyId = "p1"
        val members = listOf(
            PartyMember("you", partyId, "You"),
            PartyMember("a", partyId, "A"),
            PartyMember("b", partyId, "B")
        )
        partiesRepo.addParty("Party", members)

        transactionsRepo.addPartyExpense(
            PartyExpenseParams(
                "Dinner", 1200, null, partyId, "you",
                mapOf("you" to 400L, "a" to 400L, "b" to 400L),
                atEpochMillis = 0L
            )
        )
        transactionsRepo.addPartyExpense(
            PartyExpenseParams(
                "Taxi", 300, null, partyId, "a",
                mapOf("you" to 100L, "a" to 100L, "b" to 100L),
                atEpochMillis = 1L
            )
        )
        partiesRepo.addSettlement(partyId, "a", "you", 200, "cash", 2L)

        val balances = partiesRepo.observePartyBalances(partyId).first()
        assertEquals(0L, balances.perMember.values.sum())
        assertEquals(500L, balances.perMember["you"])
        assertEquals(-500L, balances.perMember["b"])
    }
}
