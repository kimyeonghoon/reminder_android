package com.reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.reminder.archive.ArchiveManager

/**
 * v1.43.0: ArchiveViewModel Factory
 */
class ArchiveViewModelFactory(
    private val archiveManager: ArchiveManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArchiveViewModel::class.java)) {
            return ArchiveViewModel(archiveManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
