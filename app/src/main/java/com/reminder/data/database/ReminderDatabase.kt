package com.reminder.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.reminder.data.dao.CalendarSyncConfigDao
import com.reminder.data.dao.ConflictLogDao
import com.reminder.data.dao.FocusSessionDao
import com.reminder.data.dao.GoalDao
import com.reminder.data.dao.HabitDao
import com.reminder.data.dao.MLTrainingDataDao
import com.reminder.data.dao.PendingActionDao
import com.reminder.data.dao.PomodoroSessionDao
import com.reminder.data.dao.RecurrenceExceptionDao
import com.reminder.data.dao.ReminderAttachmentDao
import com.reminder.data.dao.ReminderDao
import com.reminder.data.dao.ReminderImageDao
import com.reminder.data.dao.SavedFilterDao
import com.reminder.data.dao.SubTaskDao
import com.reminder.data.entity.CalendarSyncConfig
import com.reminder.data.entity.ConflictLogEntity
import com.reminder.data.entity.FocusSessionEntity
import com.reminder.data.entity.GoalEntity
import com.reminder.data.entity.HabitCompletion
import com.reminder.data.entity.HabitEntity
import com.reminder.data.entity.MLTrainingDataEntity
import com.reminder.data.entity.PendingActionEntity
import com.reminder.data.entity.PomodoroSession
import com.reminder.data.entity.RecurrenceExceptionEntity
import com.reminder.data.entity.ReminderAttachment
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.ReminderImage
import com.reminder.data.entity.SavedFilterEntity
import com.reminder.data.entity.SubTask

@Database(
    entities = [ReminderEntity::class, SubTask::class, ReminderImage::class, com.reminder.data.entity.ReminderTemplate::class, SavedFilterEntity::class, GoalEntity::class, RecurrenceExceptionEntity::class, MLTrainingDataEntity::class, PendingActionEntity::class, ConflictLogEntity::class, ReminderAttachment::class, CalendarSyncConfig::class, HabitEntity::class, HabitCompletion::class, PomodoroSession::class, FocusSessionEntity::class],
    version = 27,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun subTaskDao(): SubTaskDao
    abstract fun reminderImageDao(): ReminderImageDao
    abstract fun reminderTemplateDao(): com.reminder.data.dao.ReminderTemplateDao
    abstract fun savedFilterDao(): SavedFilterDao
    abstract fun goalDao(): GoalDao
    abstract fun recurrenceExceptionDao(): RecurrenceExceptionDao
    abstract fun mlTrainingDataDao(): MLTrainingDataDao
    abstract fun pendingActionDao(): PendingActionDao
    abstract fun conflictLogDao(): ConflictLogDao
    abstract fun reminderAttachmentDao(): ReminderAttachmentDao
    abstract fun calendarSyncConfigDao(): CalendarSyncConfigDao
    abstract fun habitDao(): HabitDao // v1.44.0
    abstract fun pomodoroSessionDao(): PomodoroSessionDao // v1.45.0
    abstract fun focusSessionDao(): FocusSessionDao // v1.51.0

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

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 서브태스크 테이블 생성
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS subtasks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        reminderId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        position INTEGER NOT NULL DEFAULT 0,
                        createdAt TEXT NOT NULL,
                        FOREIGN KEY(reminderId) REFERENCES reminders(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                // 서브태스크 인덱스 추가
                db.execSQL("CREATE INDEX IF NOT EXISTS index_subtasks_reminderId ON subtasks(reminderId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_subtasks_reminderId_position ON subtasks(reminderId, position)")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 리마인더 이미지 테이블 생성
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS reminder_images (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        reminderId INTEGER NOT NULL,
                        imageUri TEXT NOT NULL,
                        createdAt TEXT NOT NULL,
                        FOREIGN KEY(reminderId) REFERENCES reminders(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                // 이미지 인덱스 추가
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminder_images_reminderId ON reminder_images(reminderId)")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 리마인더 템플릿 테이블 생성
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS reminder_templates (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        titleTemplate TEXT NOT NULL,
                        descriptionTemplate TEXT NOT NULL,
                        defaultPriority INTEGER NOT NULL,
                        defaultCategory TEXT NOT NULL,
                        defaultRecurrencePattern TEXT NOT NULL,
                        defaultRecurrenceInterval INTEGER NOT NULL,
                        createdAt TEXT NOT NULL,
                        updatedAt TEXT NOT NULL
                    )
                """.trimIndent())

                // 템플릿 인덱스 추가
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminder_templates_name ON reminder_templates(name)")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 태그 컬럼 추가
                db.execSQL("ALTER TABLE reminders ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 스누즈 기능 컬럼 추가
                db.execSQL("ALTER TABLE reminders ADD COLUMN snoozeUntil TEXT")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 위치 기반 리마인더 컬럼 추가
                db.execSQL("ALTER TABLE reminders ADD COLUMN locationLatitude REAL")
                db.execSQL("ALTER TABLE reminders ADD COLUMN locationLongitude REAL")
                db.execSQL("ALTER TABLE reminders ADD COLUMN locationName TEXT")
                db.execSQL("ALTER TABLE reminders ADD COLUMN locationRadius REAL")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 웹 링크 첨부 컬럼 추가
                db.execSQL("ALTER TABLE reminders ADD COLUMN webLink TEXT")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 음성 알림 (TTS) 컬럼 추가
                db.execSQL("ALTER TABLE reminders ADD COLUMN readAloud INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 저장된 필터 (스마트 컬렉션) 테이블 생성
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS saved_filters (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        icon TEXT NOT NULL DEFAULT 'filter_list',
                        filterJson TEXT NOT NULL,
                        createdAt TEXT NOT NULL,
                        `order` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                // 저장된 필터 인덱스 추가
                db.execSQL("CREATE INDEX IF NOT EXISTS index_saved_filters_order ON saved_filters(`order`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_saved_filters_createdAt ON saved_filters(createdAt)")
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // v1.33.0: 목표 설정 테이블 생성
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS goals (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        targetCount INTEGER NOT NULL,
                        category TEXT,
                        startDate TEXT NOT NULL,
                        endDate TEXT NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        createdAt TEXT NOT NULL
                    )
                """.trimIndent())

                // 목표 인덱스 추가
                db.execSQL("CREATE INDEX IF NOT EXISTS index_goals_type ON goals(type)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_goals_isActive ON goals(isActive)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_goals_startDate_endDate ON goals(startDate, endDate)")
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // v1.35.0: 반복 예외 날짜 테이블 생성
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS recurrence_exceptions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        reminderId INTEGER NOT NULL,
                        exceptionDate TEXT NOT NULL,
                        FOREIGN KEY(reminderId) REFERENCES reminders(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                // 반복 예외 인덱스 추가
                db.execSQL("CREATE INDEX IF NOT EXISTS index_recurrence_exceptions_reminderId ON recurrence_exceptions(reminderId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_recurrence_exceptions_exceptionDate ON recurrence_exceptions(exceptionDate)")

                // v1.35.0: 반복 규칙 고급 옵션 컬럼 추가
                db.execSQL("ALTER TABLE reminders ADD COLUMN recurrenceRule TEXT")
                db.execSQL("ALTER TABLE reminders ADD COLUMN recurrenceEnd TEXT")
            }
        }

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // v1.37.0: ML 학습 데이터 테이블 생성
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS ml_training_data (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        dataType TEXT NOT NULL,
                        inputText TEXT NOT NULL,
                        outputLabel TEXT NOT NULL,
                        category TEXT,
                        dayOfWeek INTEGER,
                        confidence REAL NOT NULL,
                        usageCount INTEGER NOT NULL,
                        createdAt TEXT NOT NULL,
                        lastUsedAt TEXT NOT NULL
                    )
                """.trimIndent())

                // ML 학습 데이터 인덱스 추가
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ml_training_data_dataType ON ml_training_data(dataType)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ml_training_data_dataType_inputText ON ml_training_data(dataType, inputText)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ml_training_data_createdAt ON ml_training_data(createdAt)")
            }
        }

        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // v1.38.0: 오프라인 작업 큐 테이블 생성
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS pending_actions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        reminderId INTEGER NOT NULL,
                        actionType TEXT NOT NULL,
                        createdAt TEXT NOT NULL,
                        retryCount INTEGER NOT NULL,
                        lastRetryAt TEXT,
                        errorMessage TEXT
                    )
                """.trimIndent())

                // 오프라인 작업 큐 인덱스 추가
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pending_actions_reminderId ON pending_actions(reminderId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pending_actions_createdAt ON pending_actions(createdAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pending_actions_retryCount ON pending_actions(retryCount)")

                // v1.38.0: 충돌 로그 테이블 생성
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS conflict_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        reminderId INTEGER NOT NULL,
                        conflictedAt TEXT NOT NULL,
                        resolutionStrategy TEXT NOT NULL,
                        localData TEXT NOT NULL,
                        remoteData TEXT NOT NULL,
                        chosenData TEXT NOT NULL,
                        isResolved INTEGER NOT NULL,
                        resolvedAt TEXT
                    )
                """.trimIndent())

                // 충돌 로그 인덱스 추가
                db.execSQL("CREATE INDEX IF NOT EXISTS index_conflict_logs_reminderId ON conflict_logs(reminderId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_conflict_logs_isResolved ON conflict_logs(isResolved)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_conflict_logs_conflictedAt ON conflict_logs(conflictedAt)")
            }
        }

        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // v1.39.0: 첨부파일 테이블 생성
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS reminder_attachments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        reminderId INTEGER NOT NULL,
                        fileName TEXT NOT NULL,
                        fileType TEXT NOT NULL,
                        localPath TEXT NOT NULL,
                        cloudUrl TEXT,
                        fileSize INTEGER NOT NULL,
                        mimeType TEXT NOT NULL,
                        isUploaded INTEGER NOT NULL,
                        createdAt TEXT NOT NULL,
                        extractedText TEXT,
                        FOREIGN KEY(reminderId) REFERENCES reminders(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                // 첨부파일 인덱스 추가
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminder_attachments_reminderId ON reminder_attachments(reminderId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminder_attachments_fileType ON reminder_attachments(fileType)")
            }
        }

        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // v1.40.0: 캘린더 동기화 설정 테이블 생성
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS calendar_sync_config (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        calendarId TEXT NOT NULL,
                        calendarName TEXT NOT NULL,
                        accountName TEXT NOT NULL,
                        isSyncEnabled INTEGER NOT NULL,
                        syncDirection TEXT NOT NULL,
                        calendarColor INTEGER NOT NULL,
                        lastSyncedAt TEXT,
                        createdAt TEXT NOT NULL
                    )
                """.trimIndent())

                // 캘린더 동기화 설정 인덱스 추가
                db.execSQL("CREATE INDEX IF NOT EXISTS index_calendar_sync_config_calendarId ON calendar_sync_config(calendarId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_calendar_sync_config_isSyncEnabled ON calendar_sync_config(isSyncEnabled)")
            }
        }

        private val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // v1.43.0: 아카이브 기능 컬럼 추가
                db.execSQL("ALTER TABLE reminders ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")

                // 아카이브 인덱스 추가
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_isArchived ON reminders(isArchived)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_isCompleted_isArchived ON reminders(isCompleted, isArchived)")
            }
        }

        private val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // v1.44.0: 습관 추적 테이블 생성
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS habits (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        frequency INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        createdAt TEXT NOT NULL,
                        updatedAt TEXT NOT NULL
                    )
                """.trimIndent())

                // 습관 인덱스 추가
                db.execSQL("CREATE INDEX IF NOT EXISTS index_habits_isActive ON habits(isActive)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_habits_createdAt ON habits(createdAt)")

                // v1.44.0: 습관 완료 기록 테이블 생성
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS habit_completions (
                        habitId INTEGER NOT NULL,
                        completedDate TEXT NOT NULL,
                        PRIMARY KEY(habitId, completedDate),
                        FOREIGN KEY(habitId) REFERENCES habits(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                // 습관 완료 인덱스 추가
                db.execSQL("CREATE INDEX IF NOT EXISTS index_habit_completions_habitId ON habit_completions(habitId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_habit_completions_completedDate ON habit_completions(completedDate)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_habit_completions_habitId_completedDate ON habit_completions(habitId, completedDate)")
            }
        }

        private val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // v1.45.0: 포모도로 세션 테이블 생성
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS pomodoro_sessions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        reminderId INTEGER,
                        sessionType TEXT NOT NULL,
                        duration INTEGER NOT NULL,
                        startedAt TEXT NOT NULL,
                        completedAt TEXT,
                        isCompleted INTEGER NOT NULL,
                        createdAt TEXT NOT NULL,
                        FOREIGN KEY(reminderId) REFERENCES reminders(id) ON DELETE SET NULL
                    )
                """.trimIndent())

                // 포모도로 세션 인덱스 추가
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pomodoro_sessions_reminderId ON pomodoro_sessions(reminderId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pomodoro_sessions_startedAt ON pomodoro_sessions(startedAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pomodoro_sessions_isCompleted ON pomodoro_sessions(isCompleted)")
            }
        }

        private val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // v1.47.0: Eisenhower Matrix - urgency 필드 추가
                db.execSQL("ALTER TABLE reminders ADD COLUMN urgency INTEGER NOT NULL DEFAULT 1")

                // urgency 인덱스 추가
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_urgency ON reminders(urgency)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_priority_urgency ON reminders(priority, urgency)")
            }
        }

        private val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // v1.51.0: 포커스 세션 테이블 생성
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS focus_sessions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        reminderId INTEGER,
                        focusType TEXT NOT NULL,
                        startTime TEXT NOT NULL,
                        endTime TEXT,
                        targetDurationMinutes INTEGER NOT NULL,
                        actualDurationMinutes INTEGER NOT NULL,
                        isCompleted INTEGER NOT NULL,
                        isInterrupted INTEGER NOT NULL,
                        createdAt TEXT NOT NULL,
                        FOREIGN KEY(reminderId) REFERENCES reminders(id) ON DELETE SET NULL
                    )
                """.trimIndent())

                // 포커스 세션 인덱스 추가
                db.execSQL("CREATE INDEX IF NOT EXISTS index_focus_sessions_reminderId ON focus_sessions(reminderId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_focus_sessions_startTime ON focus_sessions(startTime)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_focus_sessions_isCompleted ON focus_sessions(isCompleted)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_focus_sessions_focusType ON focus_sessions(focusType)")
            }
        }

        private val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // v1.66.0: 미리 알림 기능 추가
                db.execSQL("ALTER TABLE reminders ADD COLUMN advanceNotificationMinutes INTEGER")
            }
        }

        private val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // v1.66.0: 시간 설정 여부 필드 추가
                db.execSQL("ALTER TABLE reminders ADD COLUMN hasTime INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // v1.67.1: 레거시 반복 필드 제거 (recurrencePattern, recurrenceInterval, recurrenceDaysOfWeek, recurrenceEndDate)
                // SQLite는 ALTER TABLE DROP COLUMN을 지원하지 않으므로 테이블 재생성 필요

                // === reminders 테이블 ===
                // 1. 새 테이블 생성 (레거시 필드 제외)
                db.execSQL("""
                    CREATE TABLE reminders_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        dueDateTime TEXT,
                        priority INTEGER NOT NULL,
                        urgency INTEGER NOT NULL,
                        category TEXT NOT NULL,
                        tags TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL,
                        completedAt TEXT,
                        isArchived INTEGER NOT NULL,
                        createdAt TEXT NOT NULL,
                        updatedAt TEXT NOT NULL,
                        imageUri TEXT,
                        snoozeUntil TEXT,
                        locationLatitude REAL,
                        locationLongitude REAL,
                        locationName TEXT,
                        locationRadius REAL,
                        webLink TEXT,
                        readAloud INTEGER NOT NULL,
                        recurrenceRule TEXT,
                        recurrenceEnd TEXT,
                        advanceNotificationMinutes INTEGER,
                        hasTime INTEGER NOT NULL
                    )
                """.trimIndent())

                // 2. 데이터 복사 (레거시 필드 제외)
                db.execSQL("""
                    INSERT INTO reminders_new (
                        id, title, description, dueDateTime, priority, urgency, category, tags,
                        isCompleted, completedAt, isArchived, createdAt, updatedAt, imageUri, snoozeUntil,
                        locationLatitude, locationLongitude, locationName, locationRadius,
                        webLink, readAloud, recurrenceRule, recurrenceEnd, advanceNotificationMinutes, hasTime
                    )
                    SELECT
                        id, title, description, dueDateTime, priority, urgency, category, tags,
                        isCompleted, completedAt, isArchived, createdAt, updatedAt, imageUri, snoozeUntil,
                        locationLatitude, locationLongitude, locationName, locationRadius,
                        webLink, readAloud, recurrenceRule, recurrenceEnd, advanceNotificationMinutes, hasTime
                    FROM reminders
                """.trimIndent())

                // 3. 기존 인덱스 제거
                db.execSQL("DROP INDEX IF EXISTS index_reminders_isCompleted")
                db.execSQL("DROP INDEX IF EXISTS index_reminders_dueDateTime")
                db.execSQL("DROP INDEX IF EXISTS index_reminders_priority")
                db.execSQL("DROP INDEX IF EXISTS index_reminders_category")
                db.execSQL("DROP INDEX IF EXISTS index_reminders_updatedAt")
                db.execSQL("DROP INDEX IF EXISTS index_reminders_isCompleted_dueDateTime")
                db.execSQL("DROP INDEX IF EXISTS index_reminders_urgency")
                db.execSQL("DROP INDEX IF EXISTS index_reminders_priority_urgency")
                db.execSQL("DROP INDEX IF EXISTS index_reminders_isArchived")
                db.execSQL("DROP INDEX IF EXISTS index_reminders_isCompleted_isArchived")
                db.execSQL("DROP INDEX IF EXISTS index_reminders_snoozeUntil")
                db.execSQL("DROP INDEX IF EXISTS index_reminders_isCompleted_updatedAt")

                // 4. 기존 테이블 삭제
                db.execSQL("DROP TABLE reminders")

                // 5. 새 테이블 이름 변경
                db.execSQL("ALTER TABLE reminders_new RENAME TO reminders")

                // 6. 인덱스 재생성
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_isCompleted ON reminders(isCompleted)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_dueDateTime ON reminders(dueDateTime)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_priority ON reminders(priority)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_category ON reminders(category)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_updatedAt ON reminders(updatedAt)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_isCompleted_dueDateTime ON reminders(isCompleted, dueDateTime)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_urgency ON reminders(urgency)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_priority_urgency ON reminders(priority, urgency)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_isArchived ON reminders(isArchived)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_isCompleted_isArchived ON reminders(isCompleted, isArchived)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_snoozeUntil ON reminders(snoozeUntil)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_isCompleted_updatedAt ON reminders(isCompleted, updatedAt)")

                // === reminder_templates 테이블 ===
                // 1. 새 테이블 생성 (레거시 필드 제외)
                db.execSQL("""
                    CREATE TABLE reminder_templates_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        titleTemplate TEXT NOT NULL,
                        descriptionTemplate TEXT NOT NULL,
                        defaultPriority INTEGER NOT NULL,
                        defaultCategory TEXT NOT NULL,
                        defaultRecurrenceRule TEXT,
                        defaultRecurrenceEnd TEXT,
                        createdAt TEXT NOT NULL,
                        updatedAt TEXT NOT NULL
                    )
                """.trimIndent())

                // 2. 데이터 복사 (레거시 필드 제외, 새 필드는 NULL)
                db.execSQL("""
                    INSERT INTO reminder_templates_new (
                        id, name, titleTemplate, descriptionTemplate, defaultPriority, defaultCategory,
                        defaultRecurrenceRule, defaultRecurrenceEnd, createdAt, updatedAt
                    )
                    SELECT
                        id, name, titleTemplate, descriptionTemplate, defaultPriority, defaultCategory,
                        NULL, NULL, createdAt, updatedAt
                    FROM reminder_templates
                """.trimIndent())

                // 3. 기존 인덱스 제거
                db.execSQL("DROP INDEX IF EXISTS index_reminder_templates_name")

                // 4. 기존 테이블 삭제
                db.execSQL("DROP TABLE reminder_templates")

                // 5. 새 테이블 이름 변경
                db.execSQL("ALTER TABLE reminder_templates_new RENAME TO reminder_templates")

                // 6. 인덱스 재생성
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminder_templates_name ON reminder_templates(name)")
            }
        }

        fun getDatabase(context: Context): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "reminder_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25, MIGRATION_25_26, MIGRATION_26_27)
                    .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
