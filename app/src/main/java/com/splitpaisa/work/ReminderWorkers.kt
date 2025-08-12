package com.splitpaisa.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class BudgetAlertWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        Log.d("BudgetAlert", "Budget alert worker running")
        return Result.success()
    }
}

class SettleUpReminderWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        Log.d("SettleUp", "Settle up reminder worker running")
        return Result.success()
    }
}

object ReminderScheduler {
    fun scheduleBudget(context: Context) {
        val request = PeriodicWorkRequestBuilder<BudgetAlertWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("budget-alert", ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun scheduleSettle(context: Context) {
        val request = PeriodicWorkRequestBuilder<SettleUpReminderWorker>(7, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("settle-reminder", ExistingPeriodicWorkPolicy.UPDATE, request)
    }
}
