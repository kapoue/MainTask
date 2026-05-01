package com.maintask.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Task::class], version = 4, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "maintask_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(SeedCallback())
                    .build()
                    .also { INSTANCE = it }
            }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val now = System.currentTimeMillis()
            listOf(
                Triple("Ventilation salle de bain", 180, "home"),
                Triple("Filtre aspirateur",          30,  "home"),
                Triple("Pression pneus scooter",     30,  "moto"),
                Triple("Pression pneus voiture",     30,  "car"),
                Triple("Pression vélo musculaire",   90,  "bike"),
                Triple("Pression vélo électrique",   30,  "bike"),
                Triple("Détecteurs incendie",        90,  "security"),
                Triple("Lave-glace voiture",         90,  "car")
            ).forEach { (title, interval, icon) ->
                db.execSQL(
                    "INSERT INTO tasks (title, intervalDays, lastDoneAt, iconKey) VALUES (?, ?, ?, ?)",
                    arrayOf(title, interval, now, icon)
                )
            }
        }
    }
}
