package com.splitpaisa.data.repo

import com.splitpaisa.core.model.Category
import com.splitpaisa.data.local.dao.CategoryDao
import com.splitpaisa.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface CategoriesRepository {
    fun observeAll(): Flow<List<Category>>
    suspend fun upsert(category: Category)
}

class CategoriesRepositoryImpl(private val dao: CategoryDao) : CategoriesRepository {
    override fun observeAll(): Flow<List<Category>> = dao.getAll().map { list ->
        list.map { it.toModel() }
    }

    override suspend fun upsert(category: Category) {
        dao.upsert(listOf(CategoryEntity.from(category)))
    }
}
