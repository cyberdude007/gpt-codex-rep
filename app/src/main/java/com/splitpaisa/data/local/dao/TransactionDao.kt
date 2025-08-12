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
}
