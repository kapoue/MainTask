package com.maintask.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY MAX(lastDoneAt + intervalDays * 86400000, snoozedUntil)")
    fun getAllSortedByDueDate(): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY MAX(lastDoneAt + intervalDays * 86400000, snoozedUntil)")
    suspend fun getAllSortedByDueDateOnce(): List<Task>

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}
