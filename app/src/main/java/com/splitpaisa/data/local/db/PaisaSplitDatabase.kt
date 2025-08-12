package com.splitpaisa.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PaisaSplitDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun partyDao(): PartyDao
    abstract fun partyMemberDao(): PartyMemberDao
    abstract fun splitDao(): SplitDao
    abstract fun settlementDao(): SettlementDao

    companion object {
        @Volatile private var INSTANCE: PaisaSplitDatabase? = null
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN payerId TEXT")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_splits_transactionId_memberId ON splits(transactionId, memberId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_settlements_partyId_payerId_payeeId_atEpochMillis ON settlements(partyId, payerId, payeeId, atEpochMillis)")
            }
        }

        fun getInstance(context: Context): PaisaSplitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PaisaSplitDatabase::class.java,
                    "paisasplit.db"
                ).addMigrations(MIGRATION_1_2).addCallback(object : RoomDatabase.Callback() {
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
