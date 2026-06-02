package com.maintask.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val intervalDays: Int,
    val lastDoneAt: Long,
    val iconKey: String,
    @ColumnInfo(defaultValue = "0")    val snoozedUntil: Long    = 0L,
    @ColumnInfo(defaultValue = "")     val note: String          = "",
    @ColumnInfo(defaultValue = "DAYS") val recurrenceType: String = "DAYS",
    @ColumnInfo(defaultValue = "0")    val weekDays: Int         = 0,
    // Bitmask : bit 0 = dernier jour du mois, bits 1-31 = jours 1-31
    @ColumnInfo(defaultValue = "0")    val monthDays: Int        = 0
)

val Task.nextDueAt: Long
    get() {
        return when (recurrenceType) {
            "WEEKLY" -> {
                if (weekDays == 0) return lastDoneAt + 7 * 86_400_000L
                val cal = Calendar.getInstance().apply {
                    timeInMillis = lastDoneAt
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
                }
                // Calendar.DAY_OF_WEEK : 1=Dim, 2=Lun, …, 7=Sam
                for (i in 0 until 7) {
                    if ((weekDays shr cal.get(Calendar.DAY_OF_WEEK)) and 1 == 1)
                        return cal.timeInMillis
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
                lastDoneAt + 7 * 86_400_000L
            }
            "MONTHLY" -> {
                if (monthDays == 0) return lastDoneAt + 30 * 86_400_000L
                val cal = Calendar.getInstance().apply {
                    timeInMillis = lastDoneAt
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
                }
                // On cherche le prochain jour correspondant (jusqu'à ~2 mois)
                for (i in 0 until 62) {
                    val dom    = cal.get(Calendar.DAY_OF_MONTH)
                    val maxDom = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val isLastDay   = (monthDays and 1 == 1) && dom == maxDom
                    val isDayBitSet = dom <= 31 && (monthDays shr dom) and 1 == 1
                    if (isLastDay || isDayBitSet) return cal.timeInMillis
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
                lastDoneAt + 30 * 86_400_000L
            }
            else -> lastDoneAt + intervalDays * 86_400_000L
        }
    }

val Task.isSnoozed: Boolean
    get() = snoozedUntil > System.currentTimeMillis() && snoozedUntil > nextDueAt

val Task.effectiveDueAt: Long
    get() = if (isSnoozed) snoozedUntil else nextDueAt

val Task.daysRemaining: Int
    get() {
        val dueCal = Calendar.getInstance().apply {
            timeInMillis = effectiveDueAt
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }
        return ((dueCal.timeInMillis - todayCal.timeInMillis) / 86_400_000L).toInt()
    }
