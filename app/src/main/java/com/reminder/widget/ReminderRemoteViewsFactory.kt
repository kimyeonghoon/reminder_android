package com.reminder.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.reminder.R
import com.reminder.data.database.ReminderDatabase
import com.reminder.data.entity.Priority
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.repository.ReminderRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * 위젯 리스트에 데이터를 제공하는 Factory
 */
class ReminderRemoteViewsFactory(
    private val context: Context,
    private val intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private var widgetItems: List<WidgetData> = emptyList()
    private lateinit var repository: ReminderRepository
    private val widgetDataProvider = WidgetDataProvider()

    override fun onCreate() {
        // Repository 초기화
        val database = ReminderDatabase.getDatabase(context)
        repository = ReminderRepository(database.reminderDao())
    }

    override fun onDataSetChanged() {
        // 데이터 새로고침 (메인 스레드에서 호출됨)
        widgetItems = runBlocking {
            try {
                val reminders = repository.activeReminders.first()
                widgetDataProvider.prepareWidgetData(reminders)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    override fun onDestroy() {
        widgetItems = emptyList()
    }

    override fun getCount(): Int = widgetItems.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position >= widgetItems.size) {
            return getLoadingView()
        }

        val item = widgetItems[position]
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_reminder_item)

        // 제목 설정
        remoteViews.setTextViewText(R.id.widget_item_title, item.title)

        // 마감일 설정
        if (item.formattedDueDate.isNotEmpty()) {
            remoteViews.setTextViewText(R.id.widget_item_due_date, item.formattedDueDate)
        } else {
            remoteViews.setTextViewText(R.id.widget_item_due_date, "")
        }

        // 우선순위 색상 설정
        val priorityColor = when (item.priority) {
            Priority.HIGH -> context.getColor(R.color.priority_high)
            Priority.MEDIUM -> context.getColor(R.color.priority_medium)
            Priority.LOW -> context.getColor(R.color.priority_low)
        }
        remoteViews.setInt(R.id.widget_item_priority_indicator, "setBackgroundColor", priorityColor)

        // 체크 아이콘 설정 (항상 체크 안 됨 - 완료된 항목은 목록에 없음)
        // RemoteViews 호환: CheckBox → ImageView
        remoteViews.setImageViewResource(R.id.widget_item_checkbox, R.drawable.ic_checkbox_unchecked_24)

        // 컨테이너 클릭 시 앱 열기를 위한 Fill-in Intent
        val containerFillInIntent = Intent().apply {
            putExtra("reminder_id", item.id)
            putExtra("action", "open")
        }
        remoteViews.setOnClickFillInIntent(R.id.widget_item_container, containerFillInIntent)

        // 체크박스 클릭 시 완료 처리를 위한 Fill-in Intent
        val checkboxFillInIntent = Intent().apply {
            putExtra("reminder_id", item.id)
            putExtra("action", "toggle_complete")
        }
        remoteViews.setOnClickFillInIntent(R.id.widget_item_checkbox, checkboxFillInIntent)

        return remoteViews
    }

    override fun getLoadingView(): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_reminder_item)
    }

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return if (position < widgetItems.size) {
            widgetItems[position].id
        } else {
            position.toLong()
        }
    }

    override fun hasStableIds(): Boolean = true
}
