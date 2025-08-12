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
    fun observeRecent(limit: Int = 20): Flow<List<TransactionWithJoins>>
    fun observeMonthSummary(monthStartEpoch: Long, monthEndEpoch: Long): Flow<Summary>

    fun observeSpendByCategory(start: Long, end: Long): Flow<List<CategorySlice>>
    fun observeMonthlySpendIncome(lastNMonths: Int): Flow<List<MonthPoint>>
    fun observeBudgetVsActual(start: Long, end: Long): Flow<List<BudgetBar>>
    fun observeTopCategories(start: Long, end: Long, limit: Int = 5): Flow<List<TopCat>>
    fun listTransactions(filter: TxFilter): Flow<List<TransactionWithJoins>>

    suspend fun addTransaction(t: Transaction)
    suspend fun deleteTransaction(id: String)

    suspend fun addPartyExpense(params: PartyExpenseParams): String
    suspend fun editPartyExpense(id: String, params: PartyExpenseParams)
    suspend fun deletePartyExpense(id: String)
}

data class TransactionWithJoins(
    val transaction: Transaction,
    val category: Category?,
    val party: Party?,
)

data class Summary(
    val spentPaise: Long,
    val incomePaise: Long,
    val netPaise: Long,
)

data class CategorySlice(
    val categoryId: String,
    val name: String,
    val color: String,
    val spendPaise: Long,
)

data class MonthPoint(
    val year: Int,
    val month: Int,
    val spendPaise: Long,
    val incomePaise: Long,
)

data class BudgetBar(
    val categoryId: String,
    val name: String,
    val budgetPaise: Long,
    val actualPaise: Long,
)

data class TopCat(
    val categoryId: String,
    val name: String,
    val spendPaise: Long,
)

data class TxFilter(
    val start: Long,
    val end: Long,
    val categoryId: String? = null,
)

class TransactionsRepositoryImpl(
    private val db: PaisaSplitDatabase,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val partyDao: PartyDao,
    private val splitDao: SplitDao,
) : TransactionsRepository {
    override fun observeRecent(limit: Int): Flow<List<TransactionWithJoins>> =
        combine(
            transactionDao.getRecent(limit),
            categoryDao.getAll(),
            partyDao.getAll(),
        ) { txs, cats, parties ->
            val catMap = cats.associateBy { it.id }
            val partyMap = parties.associateBy { it.id }
            txs.map { tx ->
                TransactionWithJoins(
                    tx.toModel(),
                    catMap[tx.categoryId]?.toModel(),
                    partyMap[tx.partyId]?.toModel(),
                )
            }
        }

    override fun observeMonthSummary(monthStartEpoch: Long, monthEndEpoch: Long): Flow<Summary> =
        transactionDao.byMonth(monthStartEpoch, monthEndEpoch).map { list ->
            val spent = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountPaise }
            val income = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amountPaise }
            Summary(spent, income, income - spent)
        }

    override fun observeSpendByCategory(start: Long, end: Long): Flow<List<CategorySlice>> =
        transactionDao.spendByCategory(start, end).map { list ->
            list.map { CategorySlice(it.categoryId, it.name, it.color, it.spendPaise) }
        }

    override fun observeMonthlySpendIncome(lastNMonths: Int): Flow<List<MonthPoint>> {
        val bounds = lastNMonthsBounds(lastNMonths)
        val start = bounds.first().start
        val end = bounds.last().end
        return transactionDao.monthlyTrend(start, end).map { list ->
            val map = list.associateBy { it.year to it.month }
            bounds.map { mb ->
                val point = map[mb.year to mb.month]
                MonthPoint(mb.year, mb.month, point?.spendPaise ?: 0, point?.incomePaise ?: 0)
            }
        }
    }

    override fun observeBudgetVsActual(start: Long, end: Long): Flow<List<BudgetBar>> =
        transactionDao.budgetVsActual(start, end).map { list ->
            list.map { BudgetBar(it.categoryId, it.name, it.budgetPaise, it.actualPaise) }
        }

    override fun observeTopCategories(start: Long, end: Long, limit: Int): Flow<List<TopCat>> =
        transactionDao.topCategories(start, end, limit).map { list ->
            list.map { TopCat(it.categoryId, it.name, it.spendPaise) }
        }

    override fun listTransactions(filter: TxFilter): Flow<List<TransactionWithJoins>> =
        combine(
            transactionDao.listTransactions(filter.start, filter.end, filter.categoryId),
            categoryDao.getAll(),
            partyDao.getAll(),
        ) { txs, cats, parties ->
            val catMap = cats.associateBy { it.id }
            val partyMap = parties.associateBy { it.id }
            txs.map { tx ->
                TransactionWithJoins(
                    tx.toModel(),
                    catMap[tx.categoryId]?.toModel(),
                    partyMap[tx.partyId]?.toModel(),
                )
            }
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
                null,
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
                null,
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
    val notes: String? = null,
)

