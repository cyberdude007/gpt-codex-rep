package com.splitpaisa.data.local.converter

import androidx.room.TypeConverter
import com.splitpaisa.core.model.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? = value?.let { TransactionType.valueOf(it) }
}
