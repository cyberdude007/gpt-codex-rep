package com.splitpaisa.data.repo

import com.splitpaisa.core.model.Party
import com.splitpaisa.core.model.PartyMember
import com.splitpaisa.data.local.dao.PartyDao
import com.splitpaisa.data.local.dao.PartyMemberDao
import com.splitpaisa.data.local.entity.PartyEntity
import com.splitpaisa.data.local.entity.PartyMemberEntity
import com.splitpaisa.data.local.entity.PartyWithMembers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface PartiesRepository {
    fun observeParties(): Flow<List<PartyWithMembersBasic>>
    fun getMembers(partyId: String): Flow<List<PartyMember>>
    suspend fun addParty(name: String, members: List<PartyMember>)
}

data class PartyWithMembersBasic(
    val party: Party,
    val members: List<PartyMember>
)

class PartiesRepositoryImpl(
    private val partyDao: PartyDao,
    private val memberDao: PartyMemberDao
) : PartiesRepository {
    override fun observeParties(): Flow<List<PartyWithMembersBasic>> =
        partyDao.getPartiesWithMembers().map { list ->
            list.map { it.toBasic() }
        }

    override fun getMembers(partyId: String): Flow<List<PartyMember>> =
        memberDao.byParty(partyId).map { members -> members.map { it.toModel() } }

    override suspend fun addParty(name: String, members: List<PartyMember>) {
        val partyId = UUID.randomUUID().toString()
        val party = PartyEntity(partyId, name, System.currentTimeMillis())
        partyDao.upsert(party)
        memberDao.upsert(members.map { PartyMemberEntity(it.id, partyId, it.displayName, it.contact) })
    }

    private fun PartyWithMembers.toBasic() = PartyWithMembersBasic(
        party.toModel(),
        members.map { it.toModel() }
    )
}
