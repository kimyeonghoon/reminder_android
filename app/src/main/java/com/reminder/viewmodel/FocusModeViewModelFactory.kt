package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.reminder.data.repository.DndRepository
import com.reminder.data.repository.FocusSessionRepository

/**
 * v1.51.0: FocusModeViewModel Factory
 * v1.54.0: DndRepository 추가
 */
class FocusModeViewModelFactory(
    private val repository: FocusSessionRepository,
    private val dndRepository: DndRepository? = null // v1.54.0: DND 지원 (API 23+)
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FocusModeViewModel::class.java)) {
            return FocusModeViewModel(repository, dndRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
