package com.maintask.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.floor

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val intervalDays: Int,
    val lastDoneAt: Long,
    val iconKey: String,
    @ColumnInfo(defaultValue = "0") val snoozedUntil: Long = 0L
)

val Task.nextDueAt: Long
    get() = lastDoneAt + intervalDays * 86_400_000L

val Task.isSnoozed: Boolean
    get() = snoozedUntil > System.currentTimeMillis() && snoozedUntil > nextDueAt

val Task.effectiveDueAt: Long
    get() = if (isSnoozed) snoozedUntil else nextDueAt

val Task.daysRemaining: Int
    get() = floor((effectiveDueAt - System.currentTimeMillis()).toDouble() / 86_400_000.0).toInt()
