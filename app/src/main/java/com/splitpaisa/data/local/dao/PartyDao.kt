package com.splitpaisa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.splitpaisa.data.local.entity.PartyEntity
import com.splitpaisa.data.local.entity.PartyWithMembers
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(party: PartyEntity)

    @Query("SELECT * FROM parties")
    fun getAll(): Flow<List<PartyEntity>>

    @Transaction
    @Query("SELECT * FROM parties")
    fun getPartiesWithMembers(): Flow<List<PartyWithMembers>>

    @Query("SELECT * FROM parties WHERE LOWER(name) LIKE '%' || :needle || '%' LIMIT :limit")
    suspend fun prefilterByName(needle: String, limit: Int): List<PartyEntity>

    @Query("SELECT * FROM parties")
    suspend fun getAllOnce(): List<PartyEntity>
}
