package com.maintask.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.maintask.app.data.Task
import com.maintask.app.data.TaskDatabase
import com.maintask.app.data.TaskRepository
import com.maintask.app.backup.BackupManager
import com.maintask.app.notification.NotificationHelper
import com.maintask.app.notification.NotificationScheduler
import com.maintask.app.widget.MaintaskWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskRepository(
        TaskDatabase.getInstance(application).taskDao()
    )

    val tasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun markDone(task: Task) {
        viewModelScope.launch {
            repository.markDone(task)
            val updated = task.copy(lastDoneAt = System.currentTimeMillis(), snoozedUntil = 0L)
            NotificationScheduler.scheduleForTask(getApplication(), updated)
            doUpdateWidget()
        }
    }

    fun addTask(title: String, intervalDays: Int, iconKey: String) {
        viewModelScope.launch {
            val newTask = Task(
                title = title,
                intervalDays = intervalDays,
                lastDoneAt = System.currentTimeMillis(),
                iconKey = iconKey
            )
            val id = repository.insert(newTask)
            NotificationScheduler.scheduleForTask(getApplication(), newTask.copy(id = id.toInt()))
            doUpdateWidget()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.update(task)
            NotificationScheduler.scheduleForTask(getApplication(), task)
            doUpdateWidget()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
            NotificationScheduler.cancelForTask(getApplication(), task.id)
            doUpdateWidget()
        }
    }

    fun snooze(task: Task, days: Int) {
        viewModelScope.launch {
            repository.snooze(task, days)
            val snoozed = task.copy(snoozedUntil = System.currentTimeMillis() + days * 86_400_000L)
            NotificationScheduler.scheduleForTask(getApplication(), snoozed)
            doUpdateWidget()
        }
    }

    fun importTasks(json: String) {
        viewModelScope.launch {
            val imported = BackupManager.importFromJson(json)
            repository.deleteAll()
            imported.forEach { task ->
                val newId = repository.insert(task.copy(id = 0))
                NotificationScheduler.scheduleForTask(getApplication(), task.copy(id = newId.toInt()))
            }
            doUpdateWidget()
        }
    }

    fun testNotification(task: Task) {
        NotificationHelper.showNotification(
            getApplication(),
            task.id,
            task.title,
            "Ceci est une notification de test"
        )
    }

    private suspend fun doUpdateWidget() {
        try {
            MaintaskWidget().updateAll(getApplication<Application>())
        } catch (e: Exception) {
            android.util.Log.e("MainTask", "Widget update failed", e)
        }
    }
}
