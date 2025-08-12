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

class RepositoryRoundTripTest {
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
    fun addEditDeleteExpenseUpdatesBalances() = runBlocking {
        val partyId = "p1"
        val members = listOf(
            PartyMember("you", partyId, "You"),
            PartyMember("a", partyId, "A")
        )
        partiesRepo.addParty("Party", members)

        val id = transactionsRepo.addPartyExpense(
            PartyExpenseParams(
                "Lunch", 300, null, partyId, "you",
                mapOf("you" to 150L, "a" to 150L),
                atEpochMillis = 0L
            )
        )
        var balances = partiesRepo.observePartyBalances(partyId).first()
        assertEquals(150L, balances.perMember["you"])
        assertEquals(-150L, balances.perMember["a"])

        transactionsRepo.editPartyExpense(
            id,
            PartyExpenseParams(
                "Lunch", 400, null, partyId, "you",
                mapOf("you" to 200L, "a" to 200L),
                atEpochMillis = 1L
            )
        )
        balances = partiesRepo.observePartyBalances(partyId).first()
        assertEquals(200L, balances.perMember["you"])
        assertEquals(-200L, balances.perMember["a"])

        transactionsRepo.deletePartyExpense(id)
        balances = partiesRepo.observePartyBalances(partyId).first()
        assertEquals(0L, balances.perMember.values.sum())
    }
}
