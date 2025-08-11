package com.splitpaisa.di

import android.content.Context
import com.splitpaisa.data.local.db.SplitPaisaDatabase
import com.splitpaisa.data.repo.*

object ServiceLocator {
    @Volatile private var database: SplitPaisaDatabase? = null

    private fun db(context: Context): SplitPaisaDatabase {
        return database ?: SplitPaisaDatabase.getInstance(context).also { database = it }
    }

    fun accountsRepository(context: Context): AccountsRepository =
        AccountsRepositoryImpl(db(context).accountDao())

    fun categoriesRepository(context: Context): CategoriesRepository =
        CategoriesRepositoryImpl(db(context).categoryDao())

    fun transactionsRepository(context: Context): TransactionsRepository =
        TransactionsRepositoryImpl(db(context).transactionDao(), db(context).categoryDao(), db(context).partyDao())

    fun partiesRepository(context: Context): PartiesRepository =
        PartiesRepositoryImpl(db(context).partyDao(), db(context).partyMemberDao())
}
