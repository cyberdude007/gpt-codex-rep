package com.splitpaisa.core.csv

import java.io.InputStream

object CsvReader {
    fun preview(input: InputStream, limit: Int = 20): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        input.bufferedReader().useLines { seq ->
            seq.take(limit).forEach { line ->
                rows += line.split(",")
            }
        }
        return rows
    }
}
