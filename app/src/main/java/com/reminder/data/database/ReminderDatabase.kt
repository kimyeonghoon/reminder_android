package com.reminder.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.reminder.data.dao.ReminderDao
import com.reminder.data.entity.ReminderEntity

@Database(
    entities = [ReminderEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: ReminderDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Priority를 문자열에서 정수로 변환
                db.execSQL("""
                    CREATE TABLE reminders_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        dueDateTime TEXT,
                        priority INTEGER NOT NULL,
                        category TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL,
                        createdAt TEXT NOT NULL,
                        updatedAt TEXT NOT NULL
                    )
                """.trimIndent())

                // 데이터 복사 (Priority 문자열을 정수로 변환)
                db.execSQL("""
                    INSERT INTO reminders_new (id, title, description, dueDateTime, priority, category, isCompleted, createdAt, updatedAt)
                    SELECT id, title, description, dueDateTime,
                        CASE
                            WHEN priority = 'LOW' THEN 1
                            WHEN priority = 'MEDIUM' THEN 2
                            WHEN priority = 'HIGH' THEN 3
                            ELSE 2
                        END,
                        category, isCompleted, createdAt, updatedAt
                    FROM reminders
                """.trimIndent())

                db.execSQL("DROP TABLE reminders")
                db.execSQL("ALTER TABLE reminders_new RENAME TO reminders")
            }
        }

        fun getDatabase(context: Context): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "reminder_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
