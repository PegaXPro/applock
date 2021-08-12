package com.aviapp.app.security.applocker.service.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * @author Muhammad Mehran
 * Date: 25/04/2020
 * Time: 1:01 AM
 * E-mail: imuhammadmehran@gmail.com
 */
object WorkerBlocker {
    private const val UNIQUE_WORK_SERVICE_CHECKER = "UNIQUE_WORK_CALL_BLOCKER"

    fun startCallBlockerWorker(context: Context) {
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                UNIQUE_WORK_SERVICE_CHECKER,
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<CallBlockerWorker>(60, TimeUnit.MINUTES).build()
            )
    }
}