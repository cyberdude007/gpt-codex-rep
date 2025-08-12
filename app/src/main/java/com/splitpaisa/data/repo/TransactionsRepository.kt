package com.splitpaisa.data.repo

import com.splitpaisa.core.model.Category
import com.splitpaisa.core.model.Party
import com.splitpaisa.core.model.Transaction
import com.splitpaisa.core.model.TransactionType
import com.splitpaisa.data.local.dao.CategoryDao
import com.splitpaisa.data.local.dao.PartyDao
import com.splitpaisa.data.local.dao.TransactionDao
import com.splitpaisa.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

interface TransactionsRepository {
    fun observeRecent(limit: Int = 20): Flow<List<TransactionWithCategoryAndPartyBasic>>
    fun observeMonthSummary(monthStartEpoch: Long, monthEndEpoch: Long): Flow<Summary>
    suspend fun addTransaction(t: Transaction)
    suspend fun deleteTransaction(id: String)
}

data class TransactionWithCategoryAndPartyBasic(
    val transaction: Transaction,
    val category: Category?,
    val party: Party?
)

data class Summary(
    val spentPaise: Long,
    val incomePaise: Long,
    val netPaise: Long
)

class TransactionsRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val partyDao: PartyDao
) : TransactionsRepository {
    override fun observeRecent(limit: Int): Flow<List<TransactionWithCategoryAndPartyBasic>> =
        combine(
            transactionDao.getRecent(limit),
            categoryDao.getAll(),
            partyDao.getAll()
        ) { txs, cats, parties ->
            val catMap = cats.associateBy { it.id }
            val partyMap = parties.associateBy { it.id }
            txs.map { tx ->
                TransactionWithCategoryAndPartyBasic(
                    tx.toModel(),
                    catMap[tx.categoryId]?.toModel(),
                    partyMap[tx.partyId]?.toModel()
                )
            }
        }

    override fun observeMonthSummary(monthStartEpoch: Long, monthEndEpoch: Long): Flow<Summary> =
        transactionDao.byMonth(monthStartEpoch, monthEndEpoch).map { list ->
            val spent = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountPaise }
            val income = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amountPaise }
            Summary(spent, income, income - spent)
        }

    override suspend fun addTransaction(t: Transaction) {
        transactionDao.upsert(TransactionEntity.from(t))
    }

    override suspend fun deleteTransaction(id: String) {
        transactionDao.deleteById(id)
    }
}
