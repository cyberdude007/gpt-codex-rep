package com.splitpaisa.feature.parties

import com.splitpaisa.core.model.PartyMember
import com.splitpaisa.data.repo.PartiesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MemberSearchTest {
    @Test
    fun excludesSelectedAndFlagsExisting() = runTest {
        val list = listOf(
            PartyMember("1", "Meera", null),
            PartyMember("2", "Ravi", null)
        )
        val repo = object : PartiesRepository {
            override fun searchMembers(query: String, limit: Int): Flow<List<PartyMember>> = flowOf(list)
            override fun observeParties() = TODO()
            override fun getMembers(partyId: String) = TODO()
            override suspend fun addParty(name: String, members: List<PartyMember>) = TODO()
            override fun observePartyBalances(partyId: String) = TODO()
            override suspend fun addSettlement(partyId: String, payerId: String, payeeId: String, amountPaise: Long, methodNote: String, atEpochMillis: Long) = TODO()
            override fun observePartyActivity(partyId: String) = TODO()
        }
        val vm = MemberSearchViewModel(repo, existing = listOf(list[1]))
        vm.onQueryChange("m")
        kotlinx.coroutines.test.advanceUntilIdle()
        val results = vm.results.value
        assertTrue(results.any { it.member.id == "2" && it.alreadyInParty })
        vm.toggle(list[0])
        val resultsAfter = vm.results.value
        assertFalse(resultsAfter.any { it.member.id == "1" })
    }
}
