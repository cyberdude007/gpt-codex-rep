package com.splitpaisa.data.local.dao

data class CategorySliceRow(
    val categoryId: String,
    val name: String,
    val color: String,
    val spendPaise: Long,
)

data class MonthTrendRow(
    val year: Int,
    val month: Int,
    val spendPaise: Long,
    val incomePaise: Long,
)

data class BudgetBarRow(
    val categoryId: String,
    val name: String,
    val budgetPaise: Long,
    val actualPaise: Long,
)

data class TopCatRow(
    val categoryId: String,
    val name: String,
    val color: String,
    val spendPaise: Long,
)
