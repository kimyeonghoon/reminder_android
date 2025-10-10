package com.reminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * 동기화 충돌 로그 엔티티
 *
 * 로컬과 원격 데이터가 충돌할 때 기록을 저장합니다.
 */
@Entity(tableName = "conflict_logs")
data class ConflictLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * 충돌이 발생한 리마인더 ID
     */
    val reminderId: Long,

    /**
     * 충돌 발생 시간
     */
    val conflictedAt: LocalDateTime = LocalDateTime.now(),

    /**
     * 충돌 해결 전략
     */
    val resolutionStrategy: ResolutionStrategy,

    /**
     * 로컬 데이터 (JSON)
     */
    val localData: String,

    /**
     * 원격 데이터 (JSON)
     */
    val remoteData: String,

    /**
     * 최종 선택된 데이터 (LOCAL, REMOTE, MERGED)
     */
    val chosenData: ChosenDataSource,

    /**
     * 충돌 해결 완료 여부
     */
    val isResolved: Boolean = false,

    /**
     * 해결 시간
     */
    val resolvedAt: LocalDateTime? = null
)

/**
 * 충돌 해결 전략 Enum
 */
enum class ResolutionStrategy {
    LAST_WRITE_WINS,    // 마지막 수정 우선 (자동)
    MANUAL,             // 사용자 선택
    FIELD_LEVEL_MERGE   // 필드별 병합
}

/**
 * 선택된 데이터 소스 Enum
 */
enum class ChosenDataSource {
    LOCAL,    // 로컬 데이터 선택
    REMOTE,   // 원격 데이터 선택
    MERGED    // 필드별 병합
}
