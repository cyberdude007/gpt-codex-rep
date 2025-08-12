package com.splitpaisa.data.repo

import java.util.Calendar

data class MonthBound(
    val year: Int,
    val month: Int,
    val start: Long,
    val end: Long,
)

fun thisMonth(): Pair<Long, Long> {
    val cal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val start = cal.timeInMillis
    cal.add(Calendar.MONTH, 1)
    val end = cal.timeInMillis
    return start to end
}

fun lastNMonthsBounds(n: Int): List<MonthBound> {
    val cal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.MONTH, -(n - 1))
    }
    val list = mutableListOf<MonthBound>()
    repeat(n) {
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        list += MonthBound(year, month, start, end)
    }
    return list
}

