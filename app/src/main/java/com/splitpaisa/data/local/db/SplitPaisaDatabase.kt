package com.splitpaisa.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.splitpaisa.core.model.*
import com.splitpaisa.data.local.converter.Converters
import com.splitpaisa.data.local.dao.*
import com.splitpaisa.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        PartyEntity::class,
        PartyMemberEntity::class,
        SplitEntity::class,
        SettlementEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SplitPaisaDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun partyDao(): PartyDao
    abstract fun partyMemberDao(): PartyMemberDao
    abstract fun splitDao(): SplitDao
    abstract fun settlementDao(): SettlementDao

    companion object {
        @Volatile private var INSTANCE: SplitPaisaDatabase? = null

        fun getInstance(context: Context): SplitPaisaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SplitPaisaDatabase::class.java,
                    "splitpaisa.db"
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                database.prepopulate(context)
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun prepopulate(context: Context) {
        runCatching {
            val json = context.assets.open("data/seed/demo_seed.json")
                .bufferedReader().use { it.readText() }
            val seed = Json.decodeFromString<SeedData>(json)
            accountDao().insertAll(seed.accounts.map { AccountEntity.from(it) })
            categoryDao().upsert(seed.categories.map { CategoryEntity.from(it) })
            val party = PartyEntity(seed.party.id, seed.party.name, System.currentTimeMillis())
            partyDao().upsert(party)
            partyMemberDao().upsert(seed.party.members.map {
                PartyMemberEntity(it.id, party.id, it.name, null)
            })
            transactionDao().upsert(seed.transactions.map { TransactionEntity.from(it) })
        }
    }
}
