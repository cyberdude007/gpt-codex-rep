package com.splitpaisa.data.repo

import com.splitpaisa.core.model.Party
import com.splitpaisa.core.model.PartyMember
import com.splitpaisa.core.model.Split
import com.splitpaisa.core.model.Transaction
import com.splitpaisa.core.model.Settlement
import com.splitpaisa.data.local.dao.PartyDao
import com.splitpaisa.data.local.dao.PartyMemberDao
import com.splitpaisa.data.local.dao.TransactionDao
import com.splitpaisa.data.local.dao.SplitDao
import com.splitpaisa.data.local.dao.SettlementDao
import com.splitpaisa.data.local.entity.PartyEntity
import com.splitpaisa.data.local.entity.PartyMemberEntity
import com.splitpaisa.data.local.entity.PartyWithMembers
import com.splitpaisa.data.local.entity.SettlementEntity
import com.splitpaisa.data.local.entity.SplitEntity
import com.splitpaisa.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import com.splitpaisa.core.search.Fuzzy
import com.splitpaisa.core.search.TextNormalizer
import java.util.UUID

interface PartiesRepository {
    fun observeParties(): Flow<List<PartyWithMembersBasic>>
    fun getMembers(partyId: String): Flow<List<PartyMember>>
    fun searchMembers(query: String, limit: Int = 20): Flow<List<PartyMember>>
    suspend fun addParty(name: String, members: List<PartyMember>)
    fun observePartyBalances(partyId: String): Flow<Balances>
    suspend fun addSettlement(
        partyId: String,
        payerId: String,
        payeeId: String,
        amountPaise: Long,
        methodNote: String,
        atEpochMillis: Long
    )
    fun observePartyActivity(partyId: String): Flow<List<PartyActivityItem>>
}

data class PartyWithMembersBasic(
    val party: Party,
    val members: List<PartyMember>
)

sealed class PartyActivityItem {
    data class Expense(val transaction: Transaction, val splits: List<Split>) : PartyActivityItem()
    data class SettlementItem(val settlement: Settlement) : PartyActivityItem()
}

class PartiesRepositoryImpl(
    private val partyDao: PartyDao,
    private val memberDao: PartyMemberDao,
    private val transactionDao: TransactionDao,
    private val splitDao: SplitDao,
    private val settlementDao: SettlementDao
) : PartiesRepository {
    override fun observeParties(): Flow<List<PartyWithMembersBasic>> =
        partyDao.getPartiesWithMembers().map { list ->
            list.map { it.toBasic() }
        }

    override fun getMembers(partyId: String): Flow<List<PartyMember>> =
        memberDao.byParty(partyId).map { members -> members.map { it.toModel() } }

    override fun searchMembers(query: String, limit: Int): Flow<List<PartyMember>> = flow {
        val needle = TextNormalizer.normalize(query)
        if (needle.isBlank()) {
            emit(emptyList())
        } else {
            val candidates = memberDao.prefilterMembers(needle, limit * 5)
            val qTokens = TextNormalizer.tokenize(needle)
            val ranked = candidates.map { member ->
                val score = Fuzzy.rankTokens(qTokens, TextNormalizer.tokenize(member.displayName))
                member to score
            }.sortedWith(compareBy<Pair<PartyMemberEntity, Int>> { it.second }
                .thenBy { it.first.displayName.length }
                .thenBy { it.first.displayName })
                .take(limit)
            emit(ranked.map { it.first.toModel() })
        }
    }

    override suspend fun addParty(name: String, members: List<PartyMember>) {
        val partyId = UUID.randomUUID().toString()
        val party = PartyEntity(partyId, name, System.currentTimeMillis())
        partyDao.upsert(party)
        memberDao.upsert(members.map { PartyMemberEntity(it.id, partyId, it.displayName, it.contact) })
    }

    override fun observePartyBalances(partyId: String): Flow<Balances> =
        combine(
            transactionDao.byParty(partyId),
            splitDao.byParty(partyId),
            settlementDao.byParty(partyId)
        ) { txs, splits, settlements ->
            val splitMap = splits.groupBy { it.transactionId }.mapValues { it.value.map(SplitEntity::toModel) }
            BalanceCalculator.calculate(
                txs.map(TransactionEntity::toModel),
                splitMap,
                settlements.map(SettlementEntity::toModel)
            )
        }

    override suspend fun addSettlement(
        partyId: String,
        payerId: String,
        payeeId: String,
        amountPaise: Long,
        methodNote: String,
        atEpochMillis: Long
    ) {
        val settlement = Settlement(
            UUID.randomUUID().toString(),
            partyId,
            payerId,
            payeeId,
            amountPaise,
            methodNote,
            atEpochMillis,
            null
        )
        settlementDao.upsert(SettlementEntity.from(settlement))
    }

    override fun observePartyActivity(partyId: String): Flow<List<PartyActivityItem>> =
        combine(
            transactionDao.byParty(partyId),
            splitDao.byParty(partyId),
            settlementDao.byParty(partyId)
        ) { txs, splits, settlements ->
            val splitMap = splits.groupBy { it.transactionId }.mapValues { it.value.map(SplitEntity::toModel) }
            val txItems = txs.map {
                PartyActivityItem.Expense(it.toModel(), splitMap[it.id] ?: emptyList())
            }
            val settleItems = settlements.map { PartyActivityItem.SettlementItem(it.toModel()) }
            (txItems + settleItems).sortedByDescending {
                when (it) {
                    is PartyActivityItem.Expense -> it.transaction.atEpochMillis
                    is PartyActivityItem.SettlementItem -> it.settlement.atEpochMillis
                }
            }
        }

    private fun PartyWithMembers.toBasic() = PartyWithMembersBasic(
        party.toModel(),
        members.map { it.toModel() }
    )
}
