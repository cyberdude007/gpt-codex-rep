package com.splitpaisa.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.splitpaisa.core.search.TextNormalizer
import com.splitpaisa.data.local.db.PaisaSplitDatabase
import com.splitpaisa.data.local.entity.PartyEntity
import com.splitpaisa.data.local.entity.PartyMemberEntity
import com.splitpaisa.data.repo.PartiesRepository
import com.splitpaisa.data.repo.PartiesRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MemberSearchTest {
    private lateinit var db: PaisaSplitDatabase
    private lateinit var repo: PartiesRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, PaisaSplitDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = PartiesRepositoryImpl(db.partyDao(), db.partyMemberDao(), db.transactionDao(), db.splitDao(), db.settlementDao())
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun searchRanksAndExcludes() = runBlocking {
        db.partyDao().upsert(PartyEntity("p1", "Trip", 0))
        val members = listOf(
            PartyMemberEntity("m1", "p1", "Meera", null, TextNormalizer.normalize("Meera")),
            PartyMemberEntity("m2", "p1", "Ravi", null, TextNormalizer.normalize("Ravi"))
        )
        db.partyMemberDao().upsert(members)
        val res = repo.searchMembers("mr").first()
        assertEquals("Meera", res.first().displayName)
        val res2 = repo.searchMembers("r", setOf("m2")).first()
        assertTrue(res2.none { it.id == "m2" })
    }
}
