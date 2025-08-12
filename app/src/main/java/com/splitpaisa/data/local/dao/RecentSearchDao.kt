package com.splitpaisa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.splitpaisa.data.local.entity.RecentSearchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RecentSearchEntity)

    @Query("SELECT query FROM recent_searches ORDER BY updatedAt DESC LIMIT :limit")
    fun recentQueries(limit: Int): Flow<List<String>>

    @Query("DELETE FROM recent_searches")
    suspend fun clearAll()
}
