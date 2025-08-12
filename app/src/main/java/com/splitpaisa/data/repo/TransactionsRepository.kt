package com.splitpaisa.data.repo

import com.splitpaisa.core.model.Category
import com.splitpaisa.core.model.Party
import com.splitpaisa.core.model.Transaction
import com.splitpaisa.core.model.TransactionType
import com.splitpaisa.data.local.dao.CategoryDao
import com.splitpaisa.data.local.dao.PartyDao
import com.splitpaisa.data.local.dao.TransactionDao
import com.splitpaisa.data.local.dao.SplitDao
import com.splitpaisa.data.local.db.PaisaSplitDatabase
import com.splitpaisa.data.local.entity.SplitEntity
import com.splitpaisa.data.local.entity.TransactionEntity
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

interface TransactionsRepository {
    fun observeRecent(limit: Int = 20): Flow<List<TransactionWithCategoryAndPartyBasic>>
    fun observeMonthSummary(monthStartEpoch: Long, monthEndEpoch: Long): Flow<Summary>
    suspend fun addTransaction(t: Transaction)
    suspend fun deleteTransaction(id: String)

    suspend fun addPartyExpense(params: PartyExpenseParams): String
    suspend fun editPartyExpense(id: String, params: PartyExpenseParams)
    suspend fun deletePartyExpense(id: String)
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
    private val db: PaisaSplitDatabase,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val partyDao: PartyDao,
    private val splitDao: SplitDao
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

    override suspend fun addPartyExpense(params: PartyExpenseParams): String {
        val id = java.util.UUID.randomUUID().toString()
        db.withTransaction {
            val t = Transaction(
                id,
                TransactionType.EXPENSE,
                params.title,
                params.amountPaise,
                params.atEpochMillis,
                params.categoryId,
                null,
                params.partyId,
                params.payerId,
                params.notes,
                null,
                null
            )
            transactionDao.upsert(TransactionEntity.from(t))
            val splits = params.shares.map { (memberId, share) ->
                SplitEntity(java.util.UUID.randomUUID().toString(), id, memberId, share)
            }
            splitDao.upsert(splits)
        }
        return id
    }

    override suspend fun editPartyExpense(id: String, params: PartyExpenseParams) {
        db.withTransaction {
            val t = Transaction(
                id,
                TransactionType.EXPENSE,
                params.title,
                params.amountPaise,
                params.atEpochMillis,
                params.categoryId,
                null,
                params.partyId,
                params.payerId,
                params.notes,
                null,
                null
            )
            transactionDao.upsert(TransactionEntity.from(t))
            splitDao.deleteByTransaction(id)
            val splits = params.shares.map { (memberId, share) ->
                SplitEntity(java.util.UUID.randomUUID().toString(), id, memberId, share)
            }
            splitDao.upsert(splits)
        }
    }

    override suspend fun deletePartyExpense(id: String) {
        db.withTransaction {
            transactionDao.deleteById(id)
            splitDao.deleteByTransaction(id)
        }
    }
}

data class PartyExpenseParams(
    val title: String,
    val amountPaise: Long,
    val categoryId: String?,
    val partyId: String,
    val payerId: String,
    val shares: Map<String, Long>,
    val atEpochMillis: Long = System.currentTimeMillis(),
    val notes: String? = null
)
