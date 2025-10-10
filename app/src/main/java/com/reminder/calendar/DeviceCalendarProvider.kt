package com.reminder.calendar

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.reminder.data.entity.ReminderEntity
import java.util.*

/**
 * v1.40.0: 기기 캘린더 제공자
 *
 * Android CalendarContract API를 사용하여 기기 캘린더와 통합합니다.
 */
class DeviceCalendarProvider(private val context: Context) {

    /**
     * 캘린더 권한 확인
     */
    fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_CALENDAR
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 기기의 모든 캘린더 조회
     */
    fun getDeviceCalendars(): List<DeviceCalendar> {
        if (!hasCalendarPermission()) {
            return emptyList()
        }

        val calendars = mutableListOf<DeviceCalendar>()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR
        )

        val cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(CalendarContract.Calendars._ID)
            val nameIndex = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
            val accountIndex = it.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
            val colorIndex = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR)

            while (it.moveToNext()) {
                calendars.add(
                    DeviceCalendar(
                        id = it.getString(idIndex),
                        name = it.getString(nameIndex),
                        accountName = it.getString(accountIndex),
                        color = it.getInt(colorIndex)
                    )
                )
            }
        }

        return calendars
    }

    /**
     * 리마인더를 캘린더 이벤트로 추가
     */
    fun addReminderToCalendar(reminder: ReminderEntity, calendarId: String): Long? {
        if (!hasCalendarPermission()) {
            return null
        }

        val dueDateTime = reminder.dueDateTime ?: return null

        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, reminder.title)
            put(CalendarContract.Events.DESCRIPTION, reminder.description)
            put(CalendarContract.Events.DTSTART, dueDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
            put(CalendarContract.Events.DTEND, dueDateTime.plusHours(1).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.HAS_ALARM, 1)
        }

        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        return uri?.let { ContentUris.parseId(it) }
    }

    /**
     * 캘린더 이벤트 삭제
     */
    fun deleteCalendarEvent(eventId: Long): Boolean {
        if (!hasCalendarPermission()) {
            return false
        }

        val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        val rows = context.contentResolver.delete(uri, null, null)
        return rows > 0
    }

    /**
     * 캘린더 이벤트 업데이트
     */
    fun updateCalendarEvent(eventId: Long, reminder: ReminderEntity): Boolean {
        if (!hasCalendarPermission()) {
            return false
        }

        val dueDateTime = reminder.dueDateTime ?: return false

        val values = ContentValues().apply {
            put(CalendarContract.Events.TITLE, reminder.title)
            put(CalendarContract.Events.DESCRIPTION, reminder.description)
            put(CalendarContract.Events.DTSTART, dueDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
            put(CalendarContract.Events.DTEND, dueDateTime.plusHours(1).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
        }

        val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        val rows = context.contentResolver.update(uri, values, null, null)
        return rows > 0
    }
}

/**
 * 기기 캘린더 데이터 클래스
 */
data class DeviceCalendar(
    val id: String,
    val name: String,
    val accountName: String,
    val color: Int
)
