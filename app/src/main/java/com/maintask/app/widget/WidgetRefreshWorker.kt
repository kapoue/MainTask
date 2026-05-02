package com.maintask.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.maintask.app.notification.MidnightScheduler

class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            MaintaskWidget().updateAll(applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("MainTask", "Midnight widget refresh failed", e)
        }
        MidnightScheduler.schedule(applicationContext)
        return Result.success()
    }
}
