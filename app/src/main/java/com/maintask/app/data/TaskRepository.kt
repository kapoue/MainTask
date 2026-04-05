package com.maintask.app.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    val allTasks: Flow<List<Task>> = dao.getAllSortedByDueDate()

    suspend fun markDone(task: Task) {
        dao.update(task.copy(lastDoneAt = System.currentTimeMillis(), snoozedUntil = 0L))
    }

    suspend fun snooze(task: Task, days: Int) {
        val until = System.currentTimeMillis() + days * 86_400_000L
        dao.update(task.copy(snoozedUntil = until))
    }

    suspend fun update(task: Task) = dao.update(task)
    suspend fun insert(task: Task): Long = dao.insert(task)
    suspend fun delete(task: Task) = dao.delete(task)
    suspend fun deleteAll() = dao.deleteAll()
}
