package com.splitpaisa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.splitpaisa.data.local.entity.PartyMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyMemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(members: List<PartyMemberEntity>)

    @Query("SELECT * FROM party_members WHERE partyId = :partyId")
    fun byParty(partyId: String): Flow<List<PartyMemberEntity>>

    @Query("SELECT * FROM party_members WHERE normalizedName LIKE '%' || :needle || '%' OR LOWER(displayName) LIKE '%' || :needle || '%' LIMIT :limit")
    suspend fun search(needle: String, limit: Int): List<PartyMemberEntity>
}
