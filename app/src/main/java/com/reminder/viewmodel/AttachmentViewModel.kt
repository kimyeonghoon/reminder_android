package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminder.analytics.AnalyticsHelper
import com.reminder.data.dao.ReminderImageDao
import com.reminder.data.entity.ReminderImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * v1.68.1: Attachment 전용 ViewModel
 *
 * ReminderViewModel에서 분리됨 (God Class 리팩토링)
 * 이미지 첨부 CRUD 기능 담당
 */
class AttachmentViewModel(
    private val reminderImageDao: ReminderImageDao,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    /**
     * 리마인더의 이미지 목록 조회
     */
    fun getImages(reminderId: Long): Flow<List<ReminderImage>> {
        return reminderImageDao.getImagesByReminderId(reminderId)
    }

    /**
     * 이미지 추가
     */
    fun addImage(reminderId: Long, imageUri: String) {
        viewModelScope.launch {
            val image = ReminderImage(
                reminderId = reminderId,
                imageUri = imageUri
            )
            reminderImageDao.insert(image)

            // Analytics 이벤트 로깅
            analyticsHelper.logImageAttached()
        }
    }

    /**
     * 이미지 삭제
     */
    fun deleteImage(image: ReminderImage) {
        viewModelScope.launch {
            reminderImageDao.delete(image)
        }
    }
}
