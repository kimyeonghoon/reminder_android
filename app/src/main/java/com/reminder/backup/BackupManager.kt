package com.reminder.backup

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.reminder.data.database.ReminderDatabase
import com.reminder.data.entity.ReminderEntity
import com.reminder.data.entity.SubTask
import com.reminder.recurrence.RecurrenceEnd
import com.reminder.recurrence.RecurrenceRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 백업 및 복원 관리자
 *
 * JSON 형식으로 로컬 백업 및 복원 기능을 제공합니다.
 */
class BackupManager(
    private val context: Context,
    private val database: ReminderDatabase
) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val gson = Gson()

    /**
     * 모든 데이터를 JSON으로 내보내기
     *
     * @param uri 저장할 파일 URI
     * @return 백업 성공 여부
     */
    suspend fun exportToJson(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val reminders = database.reminderDao().getAllRemindersList()
            val subTasks = mutableListOf<SubTask>()

            // 모든 리마인더의 서브태스크 수집
            reminders.forEach { reminder ->
                val tasks = database.subTaskDao().getSubTasksByReminderId(reminder.id).first()
                subTasks.addAll(tasks)
            }

            val jsonObject = JSONObject().apply {
                put("version", 1)
                put("exportDate", LocalDateTime.now().format(dateFormatter))
                put("reminders", remindersToJsonArray(reminders))
                put("subTasks", subTasksToJsonArray(subTasks))
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonObject.toString(2)) // Pretty print
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * JSON 파일에서 데이터 가져오기
     *
     * @param uri 읽을 파일 URI
     * @return 복원 성공 여부
     */
    suspend fun importFromJson(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext false

            val jsonObject = JSONObject(jsonString)
            val remindersArray = jsonObject.getJSONArray("reminders")
            val subTasksArray = jsonObject.getJSONArray("subTasks")

            // 리마인더 복원
            val reminders = jsonArrayToReminders(remindersArray)
            reminders.forEach { reminder ->
                database.reminderDao().insertReminder(reminder)
            }

            // 서브태스크 복원
            val subTasks = jsonArrayToSubTasks(subTasksArray)
            subTasks.forEach { subTask ->
                database.subTaskDao().insert(subTask)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun remindersToJsonArray(reminders: List<ReminderEntity>): JSONArray {
        val jsonArray = JSONArray()
        reminders.forEach { reminder ->
            jsonArray.put(JSONObject().apply {
                put("id", reminder.id)
                put("title", reminder.title)
                put("description", reminder.description)
                put("dueDateTime", reminder.dueDateTime?.format(dateFormatter))
                put("priority", reminder.priority.ordinal)
                put("category", reminder.category)
                put("isCompleted", reminder.isCompleted)
                put("createdAt", reminder.createdAt.format(dateFormatter))
                put("updatedAt", reminder.updatedAt.format(dateFormatter))
                // v1.64.0: RecurrenceRule 직렬화
                put("recurrenceRule", reminder.recurrenceRule?.let { gson.toJson(it) })
                put("recurrenceEnd", reminder.recurrenceEnd?.let { gson.toJson(it) })
            })
        }
        return jsonArray
    }

    private fun subTasksToJsonArray(subTasks: List<SubTask>): JSONArray {
        val jsonArray = JSONArray()
        subTasks.forEach { subTask ->
            jsonArray.put(JSONObject().apply {
                put("id", subTask.id)
                put("reminderId", subTask.reminderId)
                put("title", subTask.title)
                put("isCompleted", subTask.isCompleted)
                put("position", subTask.position)
                put("createdAt", subTask.createdAt.format(dateFormatter))
            })
        }
        return jsonArray
    }

    private fun jsonArrayToReminders(jsonArray: JSONArray): List<ReminderEntity> {
        val reminders = mutableListOf<ReminderEntity>()
        for (i in 0 until jsonArray.length()) {
            val json = jsonArray.getJSONObject(i)
            reminders.add(
                ReminderEntity(
                    id = json.getLong("id"),
                    title = json.getString("title"),
                    description = json.getString("description"),
                    dueDateTime = json.optString("dueDateTime").takeIf { it.isNotEmpty() }
                        ?.let { LocalDateTime.parse(it, dateFormatter) },
                    priority = com.reminder.data.entity.Priority.values()[json.getInt("priority")],
                    category = json.getString("category"),
                    isCompleted = json.getBoolean("isCompleted"),
                    createdAt = LocalDateTime.parse(json.getString("createdAt"), dateFormatter),
                    updatedAt = LocalDateTime.parse(json.getString("updatedAt"), dateFormatter),
                    // v1.64.0: RecurrenceRule 역직렬화
                    recurrenceRule = json.optString("recurrenceRule").takeIf { it.isNotEmpty() }
                        ?.let { gson.fromJson(it, RecurrenceRule::class.java) },
                    recurrenceEnd = json.optString("recurrenceEnd").takeIf { it.isNotEmpty() }
                        ?.let { gson.fromJson(it, RecurrenceEnd::class.java) }
                )
            )
        }
        return reminders
    }

    private fun jsonArrayToSubTasks(jsonArray: JSONArray): List<SubTask> {
        val subTasks = mutableListOf<SubTask>()
        for (i in 0 until jsonArray.length()) {
            val json = jsonArray.getJSONObject(i)
            subTasks.add(
                SubTask(
                    id = json.getLong("id"),
                    reminderId = json.getLong("reminderId"),
                    title = json.getString("title"),
                    isCompleted = json.getBoolean("isCompleted"),
                    position = json.getInt("position"),
                    createdAt = LocalDateTime.parse(json.getString("createdAt"), dateFormatter)
                )
            )
        }
        return subTasks
    }
}
