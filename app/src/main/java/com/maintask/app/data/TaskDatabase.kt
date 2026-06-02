package com.maintask.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Task::class], version = 6, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tasks ADD COLUMN note TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE tasks ADD COLUMN recurrenceType TEXT NOT NULL DEFAULT 'DAYS'")
                database.execSQL("ALTER TABLE tasks ADD COLUMN weekDays INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE tasks ADD COLUMN monthDays INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): TaskDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "maintask_db"
                )
                    .fallbackToDestructiveMigrationFrom(1, 2, 3)
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
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
