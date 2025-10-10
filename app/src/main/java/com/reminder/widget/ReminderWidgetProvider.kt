package com.reminder.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.reminder.MainActivity
import com.reminder.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 리마인더 위젯 Provider
 */
class ReminderWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.reminder.widget.ACTION_UPDATE_WIDGET"
        const val ACTION_ITEM_CLICK = "com.reminder.widget.ACTION_ITEM_CLICK"
        const val ACTION_TOGGLE_COMPLETE = "com.reminder.widget.ACTION_TOGGLE_COMPLETE"

        /**
         * 모든 위젯 업데이트
         */
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, ReminderWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val action = intent.getStringExtra("action") ?: intent.action

        when (action) {
            ACTION_UPDATE_WIDGET -> {
                // 모든 위젯 업데이트
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val widgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, ReminderWidgetProvider::class.java)
                )
                widgetIds.forEach { widgetId ->
                    updateWidget(context, appWidgetManager, widgetId)
                }
            }
            "open", ACTION_ITEM_CLICK -> {
                // 리마인더 아이템 클릭 시 앱 열기
                val reminderId = intent.getLongExtra("reminder_id", -1)
                val appIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    if (reminderId != -1L) {
                        putExtra("reminder_id", reminderId)
                    }
                }
                context.startActivity(appIntent)
            }
            "toggle_complete", ACTION_TOGGLE_COMPLETE -> {
                // 체크박스 클릭 시 완료 처리
                val reminderId = intent.getLongExtra("reminder_id", -1)
                if (reminderId != -1L) {
                    handleToggleComplete(context, reminderId)
                }
            }
        }
    }

    private fun handleToggleComplete(context: Context, reminderId: Long) {
        // Repository를 통해 완료 상태 토글
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as com.reminder.ReminderApplication
                val reminder = app.repository.getReminderById(reminderId)
                if (reminder != null) {
                    app.repository.toggleReminderCompletion(reminder)
                }
            } catch (e: Exception) {
                // 에러 무시
            }
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_reminder_list)

        // 제목 클릭 시 앱 열기
        val titleIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val titlePendingIntent = PendingIntent.getActivity(
            context,
            0,
            titleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.widget_title, titlePendingIntent)

        // 새로고침 버튼 클릭 시 위젯 업데이트
        val refreshIntent = Intent(context, ReminderWidgetProvider::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.widget_refresh_button, refreshPendingIntent)

        // 리스트뷰 설정
        val serviceIntent = Intent(context, ReminderRemoteViewsService::class.java).apply {
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }
        remoteViews.setRemoteAdapter(R.id.widget_list, serviceIntent)

        // 리스트 아이템 클릭 시 처리할 PendingIntent 템플릿
        val clickIntentTemplate = Intent(context, ReminderWidgetProvider::class.java).apply {
            action = ACTION_ITEM_CLICK
        }
        val clickPendingIntentTemplate = PendingIntent.getBroadcast(
            context,
            0,
            clickIntentTemplate,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        remoteViews.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate)

        // 빈 뷰 설정
        remoteViews.setEmptyView(R.id.widget_list, R.id.widget_empty_view)

        appWidgetManager.updateAppWidget(widgetId, remoteViews)
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_list)
    }

    override fun onEnabled(context: Context) {
        // v1.34.0: 첫 위젯이 생성될 때 주기적 업데이트 예약
        WidgetUpdateWorker.schedulePeriodicUpdate(context)
    }

    override fun onDisabled(context: Context) {
        // v1.34.0: 마지막 위젯이 제거될 때 주기적 업데이트 취소
        WidgetUpdateWorker.cancelPeriodicUpdate(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // v1.34.0: 위젯 삭제 시 설정도 함께 삭제
        val widgetPreferences = WidgetPreferences(context)
        appWidgetIds.forEach { widgetId ->
            widgetPreferences.deleteWidgetConfig(widgetId)
        }
    }
}
