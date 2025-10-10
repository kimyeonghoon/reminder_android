package com.reminder.sync

import com.google.gson.Gson
import com.reminder.data.dao.ConflictLogDao
import com.reminder.data.entity.ChosenDataSource
import com.reminder.data.entity.ConflictLogEntity
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.ResolutionStrategy
import java.time.LocalDateTime

/**
 * v1.38.0: 동기화 충돌 해결
 *
 * 로컬과 원격 데이터가 충돌할 때 해결 전략을 적용합니다.
 */
class ConflictResolver(
    private val conflictLogDao: ConflictLogDao
) {

    private val gson = Gson()

    /**
     * 충돌 해결: Last Write Wins 전략
     *
     * 가장 최근에 수정된 데이터를 선택합니다.
     */
    suspend fun resolveLastWriteWins(
        local: ReminderEntity,
        remote: ReminderEntity
    ): Pair<ReminderEntity, ChosenDataSource> {
        val chosen = if (local.updatedAt.isAfter(remote.updatedAt)) {
            ChosenDataSource.LOCAL
        } else {
            ChosenDataSource.REMOTE
        }

        val winner = if (chosen == ChosenDataSource.LOCAL) local else remote

        // 충돌 로그 저장
        logConflict(
            reminderId = local.id,
            local = local,
            remote = remote,
            strategy = ResolutionStrategy.LAST_WRITE_WINS,
            chosen = chosen
        )

        return Pair(winner, chosen)
    }

    /**
     * 충돌 해결: 필드별 병합 전략
     *
     * 각 필드마다 최신 값을 선택하여 병합합니다.
     */
    suspend fun resolveFieldLevelMerge(
        local: ReminderEntity,
        remote: ReminderEntity
    ): Pair<ReminderEntity, ChosenDataSource> {
        val merged = ReminderEntity(
            id = local.id,
            title = if (local.updatedAt.isAfter(remote.updatedAt)) local.title else remote.title,
            description = if (local.updatedAt.isAfter(remote.updatedAt)) local.description else remote.description,
            dueDateTime = if (local.updatedAt.isAfter(remote.updatedAt)) local.dueDateTime else remote.dueDateTime,
            priority = if (local.updatedAt.isAfter(remote.updatedAt)) local.priority else remote.priority,
            category = if (local.updatedAt.isAfter(remote.updatedAt)) local.category else remote.category,
            isCompleted = if (local.updatedAt.isAfter(remote.updatedAt)) local.isCompleted else remote.isCompleted,
            createdAt = if (local.createdAt.isBefore(remote.createdAt)) local.createdAt else remote.createdAt,
            updatedAt = if (local.updatedAt.isAfter(remote.updatedAt)) local.updatedAt else remote.updatedAt,
            recurrencePattern = if (local.updatedAt.isAfter(remote.updatedAt)) local.recurrencePattern else remote.recurrencePattern,
            recurrenceInterval = if (local.updatedAt.isAfter(remote.updatedAt)) local.recurrenceInterval else remote.recurrenceInterval,
            recurrenceDaysOfWeek = if (local.updatedAt.isAfter(remote.updatedAt)) local.recurrenceDaysOfWeek else remote.recurrenceDaysOfWeek,
            recurrenceEndDate = if (local.updatedAt.isAfter(remote.updatedAt)) local.recurrenceEndDate else remote.recurrenceEndDate,
            tags = if (local.updatedAt.isAfter(remote.updatedAt)) local.tags else remote.tags,
            snoozeUntil = if (local.updatedAt.isAfter(remote.updatedAt)) local.snoozeUntil else remote.snoozeUntil,
            locationLatitude = if (local.updatedAt.isAfter(remote.updatedAt)) local.locationLatitude else remote.locationLatitude,
            locationLongitude = if (local.updatedAt.isAfter(remote.updatedAt)) local.locationLongitude else remote.locationLongitude,
            locationName = if (local.updatedAt.isAfter(remote.updatedAt)) local.locationName else remote.locationName,
            locationRadius = if (local.updatedAt.isAfter(remote.updatedAt)) local.locationRadius else remote.locationRadius,
            webLink = if (local.updatedAt.isAfter(remote.updatedAt)) local.webLink else remote.webLink,
            readAloud = if (local.updatedAt.isAfter(remote.updatedAt)) local.readAloud else remote.readAloud,
            recurrenceRule = if (local.updatedAt.isAfter(remote.updatedAt)) local.recurrenceRule else remote.recurrenceRule,
            recurrenceEnd = if (local.updatedAt.isAfter(remote.updatedAt)) local.recurrenceEnd else remote.recurrenceEnd
        )

        // 충돌 로그 저장
        logConflict(
            reminderId = local.id,
            local = local,
            remote = remote,
            strategy = ResolutionStrategy.FIELD_LEVEL_MERGE,
            chosen = ChosenDataSource.MERGED
        )

        return Pair(merged, ChosenDataSource.MERGED)
    }

    /**
     * 충돌 로그 저장
     */
    private suspend fun logConflict(
        reminderId: Long,
        local: ReminderEntity,
        remote: ReminderEntity,
        strategy: ResolutionStrategy,
        chosen: ChosenDataSource
    ) {
        val log = ConflictLogEntity(
            reminderId = reminderId,
            conflictedAt = LocalDateTime.now(),
            resolutionStrategy = strategy,
            localData = gson.toJson(local),
            remoteData = gson.toJson(remote),
            chosenData = chosen,
            isResolved = true,
            resolvedAt = LocalDateTime.now()
        )
        conflictLogDao.insertConflictLog(log)
    }

    /**
     * 미해결 충돌 조회
     */
    suspend fun getUnresolvedConflicts() =
        conflictLogDao.getUnresolvedConflicts()

    /**
     * 충돌 로그 삭제 (30일 이상 된 것)
     */
    suspend fun cleanupOldConflictLogs() {
        val cutoffDate = LocalDateTime.now().minusDays(30)
        conflictLogDao.deleteOldConflictLogs(cutoffDate)
    }
}
