package com.maintask.app.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskId    = inputData.getInt(KEY_TASK_ID, -1)
        val taskTitle = inputData.getString(KEY_TASK_TITLE) ?: return Result.failure()
        val type      = inputData.getString(KEY_TYPE) ?: TYPE_J

        val body = if (type == TYPE_J3) "À faire dans 3 jours" else "À faire aujourd'hui"
        NotificationHelper.showNotification(applicationContext, taskId, taskTitle, body)
        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID    = "task_id"
        const val KEY_TASK_TITLE = "task_title"
        const val KEY_TYPE       = "type"
        const val TYPE_J         = "j"
        const val TYPE_J3        = "j3"
    }
}
