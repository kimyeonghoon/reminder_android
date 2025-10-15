package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.dao.SubTaskDao
import com.reminder.data.entity.SubTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * v1.68.1: SubTask 전용 ViewModel
 *
 * ReminderViewModel에서 분리됨 (God Class 리팩토링)
 * 서브태스크 CRUD 및 재정렬 기능 담당
 */
class SubTaskViewModel(
    private val subTaskDao: SubTaskDao,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    /**
     * 리마인더의 서브태스크 목록 조회
     */
    fun getSubTasks(reminderId: Long): Flow<List<SubTask>> {
        return subTaskDao.getSubTasksByReminderId(reminderId)
    }

    /**
     * 서브태스크 추가
     */
    fun addSubTask(reminderId: Long, title: String) {
        viewModelScope.launch {
            val subTask = SubTask(
                reminderId = reminderId,
                title = title,
                position = subTaskDao.getTotalSubTasksCount(reminderId)
            )
            subTaskDao.insert(subTask)

            // Analytics 이벤트 로깅
            analyticsHelper.logSubtaskAdded()
        }
    }

    /**
     * 서브태스크 완료 상태 토글
     */
    fun toggleSubTaskCompletion(subTask: SubTask) {
        viewModelScope.launch {
            val updated = subTask.copy(isCompleted = !subTask.isCompleted)
            subTaskDao.update(updated)
        }
    }

    /**
     * 서브태스크 삭제
     */
    fun deleteSubTask(subTask: SubTask) {
        viewModelScope.launch {
            subTaskDao.delete(subTask)
        }
    }

    /**
     * 서브태스크 재정렬 (드래그 앤 드롭)
     */
    fun reorderSubTasks(subTasks: List<SubTask>) {
        viewModelScope.launch {
            // position 값을 새로운 순서로 업데이트
            val reorderedSubTasks = subTasks.mapIndexed { index, subTask ->
                subTask.copy(position = index)
            }
            subTaskDao.updateAll(reorderedSubTasks)
        }
    }

    /**
     * 서브태스크 진행률 계산 (완료/전체)
     */
    suspend fun getSubTaskProgress(reminderId: Long): Pair<Int, Int> {
        val completed = subTaskDao.getCompletedSubTasksCount(reminderId)
        val total = subTaskDao.getTotalSubTasksCount(reminderId)
        return Pair(completed, total)
    }
}
