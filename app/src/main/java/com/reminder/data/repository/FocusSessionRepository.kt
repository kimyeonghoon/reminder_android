package com.reminder.data.repository

import com.reminder.data.dao.FocusSessionDao
import com.reminder.data.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * v1.51.0: 포커스 세션 저장소
 *
 * 포커스 세션 데이터 접근의 단일 진실 공급원
 */
class FocusSessionRepository(private val focusSessionDao: FocusSessionDao) {

    /**
     * 세션 삽입
     */
    suspend fun insertSession(session: FocusSessionEntity): Long {
        return focusSessionDao.insertSession(session)
    }

    /**
     * 세션 업데이트
     */
    suspend fun updateSession(session: FocusSessionEntity) {
        focusSessionDao.updateSession(session)
    }

    /**
     * 세션 삭제
     */
    suspend fun deleteSession(session: FocusSessionEntity) {
        focusSessionDao.deleteSession(session)
    }

    /**
     * ID로 세션 조회
     */
    suspend fun getSessionById(id: Long): FocusSessionEntity? {
        return focusSessionDao.getSessionById(id)
    }

    /**
     * 모든 세션 조회 (Flow)
     */
    fun getAllSessions(): Flow<List<FocusSessionEntity>> {
        return focusSessionDao.getAllSessions()
    }

    /**
     * 활성 세션 조회
     */
    fun getActiveSessions(): Flow<List<FocusSessionEntity>> {
        return focusSessionDao.getActiveSessions()
    }

    /**
     * 완료된 세션만 조회
     */
    fun getCompletedSessions(): Flow<List<FocusSessionEntity>> {
        return focusSessionDao.getCompletedSessions()
    }

    /**
     * 특정 리마인더의 세션들 조회
     */
    fun getSessionsByReminderId(reminderId: Long): Flow<List<FocusSessionEntity>> {
        return focusSessionDao.getSessionsByReminderId(reminderId)
    }

    /**
     * 날짜 범위로 세션 조회
     */
    fun getSessionsBetweenDates(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<FocusSessionEntity>> {
        return focusSessionDao.getSessionsBetweenDates(startDate, endDate)
    }

    /**
     * 총 집중 시간 계산
     */
    suspend fun getTotalFocusMinutes(): Int {
        return focusSessionDao.getTotalFocusMinutes()
    }

    /**
     * 오래된 세션 삭제 (90일 이상)
     */
    suspend fun deleteOldSessions(cutoffDate: LocalDateTime) {
        focusSessionDao.deleteOldSessions(cutoffDate)
    }

    /**
     * 모든 세션 삭제 (테스트용)
     */
    suspend fun deleteAllSessions() {
        focusSessionDao.deleteAllSessions()
    }
}
