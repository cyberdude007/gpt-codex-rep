package com.splitpaisa.core.csv

import java.io.ByteArrayOutputStream
import java.io.OutputStream

object CsvWriter {
    fun writeAccounts(accounts: List<AccountCsv>, out: OutputStream) = write(
        listOf("id,name,type"),
        accounts.map { listOf(it.id, it.name, it.type) },
        out
    )

    fun writeCategories(categories: List<CategoryCsv>, out: OutputStream) = write(
        listOf("id,name,kind,icon,color,monthlyBudgetPaise"),
        categories.map { listOf(it.id, it.name, it.kind, it.icon ?: "", it.color ?: "", it.monthlyBudgetPaise?.toString() ?: "") },
        out
    )

    fun writeTransactions(txs: List<TransactionCsv>, out: OutputStream) = write(
        listOf("id,type,title,amountPaise,atEpochMillis,categoryId,accountId,partyId,notes"),
        txs.map { listOf(it.id, it.type, it.title, it.amountPaise.toString(), it.atEpochMillis.toString(), it.categoryId ?: "", it.accountId ?: "", it.partyId ?: "", it.notes ?: "") },
        out
    )

    fun writeParties(parties: List<PartyCsv>, out: OutputStream) = write(
        listOf("id,name,createdAt"),
        parties.map { listOf(it.id, it.name, it.createdAt.toString()) },
        out
    )

    fun writeMembers(members: List<MemberCsv>, out: OutputStream) = write(
        listOf("id,partyId,displayName,contact"),
        members.map { listOf(it.id, it.partyId, it.displayName, it.contact ?: "") },
        out
    )

    fun writeSplits(splits: List<SplitCsv>, out: OutputStream) = write(
        listOf("id,transactionId,memberId,sharePaise"),
        splits.map { listOf(it.id, it.transactionId, it.memberId, it.sharePaise.toString()) },
        out
    )

    fun writeSettlements(rows: List<SettlementCsv>, out: OutputStream) = write(
        listOf("id,partyId,payerId,payeeId,amountPaise,methodNote,atEpochMillis,memo"),
        rows.map { listOf(it.id, it.partyId, it.payerId, it.payeeId, it.amountPaise.toString(), it.methodNote ?: "", it.atEpochMillis.toString(), it.memo ?: "") },
        out
    )

    private fun write(header: List<String>, rows: List<List<String>>, out: OutputStream) {
        out.bufferedWriter().use { writer ->
            writer.appendLine(header.joinToString(","))
            rows.forEach { row ->
                writer.appendLine(row.joinToString(","))
            }
        }
    }
}
