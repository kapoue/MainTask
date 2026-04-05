package com.maintask.app.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.maintask.app.data.Task
import com.maintask.app.data.effectiveDueAt
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleForTask(context: Context, task: Task) {
        cancelForTask(context, task.id)
        val wm = WorkManager.getInstance(context)
        val dueMillis = task.effectiveDueAt

        // Notification Jour J
        val delayJ = delayUntil11h(dueMillis)
        if (delayJ > 0) {
            wm.enqueueUniqueWork(
                "maintask_j_${task.id}",
                ExistingWorkPolicy.REPLACE,
                buildRequest(task, NotificationWorker.TYPE_J, delayJ)
            )
        }

        // Notification J-3
        val delayJ3 = delayUntil11h(dueMillis - 3 * 86_400_000L)
        if (delayJ3 > 0) {
            wm.enqueueUniqueWork(
                "maintask_j3_${task.id}",
                ExistingWorkPolicy.REPLACE,
                buildRequest(task, NotificationWorker.TYPE_J3, delayJ3)
            )
        }
    }

    fun cancelForTask(context: Context, taskId: Int) {
        val wm = WorkManager.getInstance(context)
        wm.cancelUniqueWork("maintask_j_$taskId")
        wm.cancelUniqueWork("maintask_j3_$taskId")
    }

    private fun buildRequest(task: Task, type: String, delayMs: Long) =
        OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putInt(NotificationWorker.KEY_TASK_ID, task.id)
                    .putString(NotificationWorker.KEY_TASK_TITLE, task.title)
                    .putString(NotificationWorker.KEY_TYPE, type)
                    .build()
            )
            .build()

    private fun delayUntil11h(targetDayMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = targetDayMillis
            set(Calendar.HOUR_OF_DAY, 11)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis - System.currentTimeMillis()
    }
}
