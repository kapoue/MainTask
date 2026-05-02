package com.maintask.app.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.maintask.app.widget.WidgetRefreshWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object MidnightScheduler {

    private const val WORK_NAME = "maintask_midnight_refresh"

    fun schedule(context: Context) {
        val nextMidnight = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val delay = nextMidnight - System.currentTimeMillis()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
        )
    }
}
