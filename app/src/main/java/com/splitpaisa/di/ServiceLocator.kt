package com.splitpaisa.di

import android.content.Context
import com.splitpaisa.data.local.db.PaisaSplitDatabase
import com.splitpaisa.data.repo.*

object ServiceLocator {
    @Volatile private var database: PaisaSplitDatabase? = null

    private fun db(context: Context): PaisaSplitDatabase {
        return database ?: PaisaSplitDatabase.getInstance(context).also { database = it }
    }

    fun accountsRepository(context: Context): AccountsRepository =
        AccountsRepositoryImpl(db(context).accountDao())

    fun categoriesRepository(context: Context): CategoriesRepository =
        CategoriesRepositoryImpl(db(context).categoryDao())

    fun transactionsRepository(context: Context): TransactionsRepository =
        TransactionsRepositoryImpl(
            db(context),
            db(context).transactionDao(),
            db(context).categoryDao(),
            db(context).partyDao(),
            db(context).splitDao()
        )

    fun partiesRepository(context: Context): PartiesRepository =
        PartiesRepositoryImpl(
            db(context).partyDao(),
            db(context).partyMemberDao(),
            db(context).transactionDao(),
            db(context).splitDao(),
            db(context).settlementDao()
        )

    fun searchRepository(context: Context): SearchRepository =
        SearchRepositoryImpl(
            db(context).transactionDao(),
            db(context).categoryDao(),
            db(context).partyDao(),
            db(context).recentSearchDao()
        )
}
