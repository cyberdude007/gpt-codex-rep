package com.splitpaisa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.splitpaisa.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(transactions: List<TransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM transactions ORDER BY atEpochMillis DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT IFNULL(SUM(amountPaise),0) FROM transactions WHERE type = :type AND atEpochMillis BETWEEN :start AND :end")
    suspend fun sumByTypeInRange(type: String, start: Long, end: Long): Long

    @Query("SELECT * FROM transactions WHERE atEpochMillis BETWEEN :start AND :end ORDER BY atEpochMillis DESC")
    fun byMonth(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE partyId = :partyId")
    fun byParty(partyId: String): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT t.categoryId AS categoryId, c.name AS name, c.color AS color, SUM(t.amountPaise) AS spendPaise
        FROM transactions t JOIN categories c ON c.id = t.categoryId
        WHERE t.type = 'EXPENSE' AND t.atEpochMillis BETWEEN :start AND :end
        GROUP BY t.categoryId
        ORDER BY spendPaise DESC
        """
    )
    fun spendByCategory(start: Long, end: Long): Flow<List<CategorySliceEntity>>

    @Query(
        """
        SELECT year, month,
               SUM(CASE WHEN type='EXPENSE' THEN amountPaise ELSE 0 END) AS spendPaise,
               SUM(CASE WHEN type='INCOME' THEN amountPaise ELSE 0 END) AS incomePaise
        FROM (
            SELECT strftime('%Y', datetime(t.atEpochMillis/1000,'unixepoch')) AS year,
                   strftime('%m', datetime(t.atEpochMillis/1000,'unixepoch')) AS month,
                   t.type, t.amountPaise
            FROM transactions t
            WHERE t.atEpochMillis BETWEEN :start AND :end
        )
        GROUP BY year, month
        ORDER BY year, month
        """
    )
    fun monthlyTrend(start: Long, end: Long): Flow<List<MonthTrendEntity>>

    @Query(
        """
        SELECT c.id AS categoryId, c.name AS name,
               COALESCE(c.monthlyBudgetPaise,0) AS budgetPaise,
               COALESCE(SUM(t.amountPaise),0) AS actualPaise
        FROM categories c
        LEFT JOIN transactions t ON t.categoryId = c.id
             AND t.type='EXPENSE'
             AND t.atEpochMillis BETWEEN :start AND :end
        WHERE c.monthlyBudgetPaise IS NOT NULL
        GROUP BY c.id
        ORDER BY actualPaise DESC
        """
    )
    fun budgetVsActual(start: Long, end: Long): Flow<List<BudgetBarEntity>>

    @Query(
        """
        SELECT t.categoryId AS categoryId, c.name AS name, c.color AS color, SUM(t.amountPaise) AS spendPaise
        FROM transactions t JOIN categories c ON c.id = t.categoryId
        WHERE t.type = 'EXPENSE' AND t.atEpochMillis BETWEEN :start AND :end
        GROUP BY t.categoryId
        ORDER BY spendPaise DESC
        LIMIT :limit
        """
    )
    fun topCategories(start: Long, end: Long, limit: Int): Flow<List<TopCatEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE atEpochMillis BETWEEN :start AND :end
          AND (:categoryId IS NULL OR categoryId = :categoryId)
        ORDER BY atEpochMillis DESC
        """
    )
    fun listTransactions(start: Long, end: Long, categoryId: String?): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT t.* FROM transactions t
        LEFT JOIN categories c ON c.id = t.categoryId
        LEFT JOIN parties p ON p.id = t.partyId
        WHERE LOWER(t.title) LIKE '%' || :needle || '%'
           OR LOWER(c.name) LIKE '%' || :needle || '%'
           OR LOWER(p.name) LIKE '%' || :needle || '%'
        ORDER BY t.atEpochMillis DESC
        LIMIT :limit
        """
    )
    suspend fun searchTransactions(needle: String, limit: Int): List<TransactionEntity>
}

data class CategorySliceEntity(
    val categoryId: String,
    val name: String,
    val color: String,
    val spendPaise: Long,
)

data class MonthTrendEntity(
    val year: Int,
    val month: Int,
    val spendPaise: Long,
    val incomePaise: Long,
)

data class BudgetBarEntity(
    val categoryId: String,
    val name: String,
    val budgetPaise: Long,
    val actualPaise: Long,
)

data class TopCatEntity(
    val categoryId: String,
    val name: String,
    val color: String,
    val spendPaise: Long,
)

