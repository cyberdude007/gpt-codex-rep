package com.splitpaisa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.splitpaisa.data.local.entity.SplitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(splits: List<SplitEntity>)

    @Query("SELECT * FROM splits WHERE transactionId = :transactionId")
    fun byTransaction(transactionId: String): Flow<List<SplitEntity>>
}
