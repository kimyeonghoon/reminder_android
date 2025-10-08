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
    version = 4,
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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 반복 리마인더 컬럼 추가
                db.execSQL("ALTER TABLE reminders ADD COLUMN recurrencePattern TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL("ALTER TABLE reminders ADD COLUMN recurrenceInterval INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE reminders ADD COLUMN recurrenceDaysOfWeek TEXT")
                db.execSQL("ALTER TABLE reminders ADD COLUMN recurrenceEndDate TEXT")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 성능 최적화를 위한 인덱스 추가
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_isCompleted ON reminders(isCompleted)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_dueDateTime ON reminders(dueDateTime)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_priority ON reminders(priority)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_category ON reminders(category)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_updatedAt ON reminders(updatedAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_isCompleted_dueDateTime ON reminders(isCompleted, dueDateTime)")
            }
        }

        fun getDatabase(context: Context): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "reminder_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
