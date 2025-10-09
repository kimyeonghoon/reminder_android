package com.reminder.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.reminder.R

/**
 * 빠른 메모 위젯 Provider
 *
 * 홈 화면에서 바로 리마인더를 추가할 수 있는 작은 위젯입니다.
 */
class QuickNoteWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_quick_note)

        // 위젯 전체 클릭 시 QuickNoteActivity 열기
        val intent = Intent(context, QuickNoteActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            widgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setOnClickPendingIntent(R.id.quick_note_container, pendingIntent)

        appWidgetManager.updateAppWidget(widgetId, remoteViews)
    }

    override fun onEnabled(context: Context) {
        // 첫 위젯이 생성될 때
    }

    override fun onDisabled(context: Context) {
        // 마지막 위젯이 제거될 때
    }
}
