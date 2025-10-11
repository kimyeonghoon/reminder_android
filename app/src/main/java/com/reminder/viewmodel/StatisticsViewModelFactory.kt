package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.reminder.data.dao.GoalDao
import com.reminder.data.repository.ReminderRepository

class StatisticsViewModelFactory(
    private val repository: ReminderRepository,
    private val goalDao: GoalDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            return StatisticsViewModel(repository, goalDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
