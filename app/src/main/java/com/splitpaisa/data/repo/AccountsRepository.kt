package com.splitpaisa.data.repo

import com.splitpaisa.core.model.Account
import com.splitpaisa.data.local.dao.AccountDao
import com.splitpaisa.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AccountsRepository {
    fun observeAll(): Flow<List<Account>>
    suspend fun upsert(account: Account)
}

class AccountsRepositoryImpl(private val dao: AccountDao) : AccountsRepository {
    override fun observeAll(): Flow<List<Account>> = dao.getAll().map { list ->
        list.map { it.toModel() }
    }

    override suspend fun upsert(account: Account) {
        dao.insertAll(listOf(AccountEntity.from(account)))
    }
}
