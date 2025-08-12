package com.splitpaisa.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.splitpaisa.core.model.Category
import com.splitpaisa.core.model.TransactionType
import com.splitpaisa.core.search.TextNormalizer

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"]), Index(value = ["normalizedName"])]
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val kind: TransactionType,
    val icon: String,
    val color: String,
    val monthlyBudgetPaise: Long?,
    val normalizedName: String,
) {
    fun toModel() = Category(id, name, kind, icon, color, monthlyBudgetPaise)

    companion object {
        fun from(model: Category) = CategoryEntity(
            model.id,
            model.name,
            model.kind,
            model.icon,
            model.color,
            model.monthlyBudgetPaise,
            TextNormalizer.normalize(model.name)
        )
    }
}
