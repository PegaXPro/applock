package com.aviapp.app.security.applocker.service.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class CallBlockerWorker(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    override fun doWork(): Result {
        return Result.success()
    }
}