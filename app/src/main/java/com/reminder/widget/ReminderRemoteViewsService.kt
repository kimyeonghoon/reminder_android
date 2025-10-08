package com.reminder.widget

import android.content.Intent
import android.widget.RemoteViewsService

/**
 * 위젯 리스트를 위한 RemoteViewsService
 */
class ReminderRemoteViewsService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ReminderRemoteViewsFactory(applicationContext, intent)
    }
}
