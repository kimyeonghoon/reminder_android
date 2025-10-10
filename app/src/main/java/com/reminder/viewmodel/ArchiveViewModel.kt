package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.archive.ArchiveManager
import com.reminder.data.entity.ReminderEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * v1.43.0: Archive ViewModel
 *
 * 아카이브된 리마인더 관리 및 자동 아카이브 기능 제공
 */
class ArchiveViewModel(
    private val archiveManager: ArchiveManager
) : ViewModel() {

    /**
     * 아카이브된 리마인더 목록
     */
    val archivedReminders: StateFlow<List<ReminderEntity>> = archiveManager.getArchivedReminders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * 로딩 상태
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * 에러 메시지
     */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * 성공 메시지
     */
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    /**
     * 리마인더를 아카이브 처리
     */
    fun archiveReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                archiveManager.archiveReminder(reminder)
                _successMessage.value = "리마인더가 아카이브되었습니다"
            } catch (e: Exception) {
                _errorMessage.value = "아카이브 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 아카이브된 리마인더를 복원
     */
    fun unarchiveReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                archiveManager.unarchiveReminder(reminder)
                _successMessage.value = "리마인더가 복원되었습니다"
            } catch (e: Exception) {
                _errorMessage.value = "복원 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * N일 이상 완료된 리마인더를 자동 아카이브
     */
    fun autoArchiveOldCompleted(daysThreshold: Int = 30) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val count = archiveManager.autoArchiveOldCompletedReminders(daysThreshold)
                _successMessage.value = "${count}개의 리마인더가 아카이브되었습니다"
            } catch (e: Exception) {
                _errorMessage.value = "자동 아카이브 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 아카이브된 리마인더 영구 삭제
     */
    fun deleteArchivedReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                archiveManager.deleteArchivedReminder(reminder)
                _successMessage.value = "리마인더가 영구 삭제되었습니다"
            } catch (e: Exception) {
                _errorMessage.value = "삭제 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 모든 아카이브 일괄 삭제
     */
    fun deleteAllArchived() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val count = archiveManager.deleteAllArchived()
                _successMessage.value = "${count}개의 리마인더가 영구 삭제되었습니다"
            } catch (e: Exception) {
                _errorMessage.value = "일괄 삭제 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 메시지 초기화
     */
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
