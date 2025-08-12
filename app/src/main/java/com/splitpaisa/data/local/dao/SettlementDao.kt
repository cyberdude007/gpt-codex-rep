package com.splitpaisa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.splitpaisa.data.local.entity.SettlementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettlementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settlement: SettlementEntity)

    @Query("SELECT * FROM settlements WHERE partyId = :partyId")
    fun byParty(partyId: String): Flow<List<SettlementEntity>>
}
