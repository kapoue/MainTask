package com.maintask.app.backup

import com.maintask.app.data.Task
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    private const val VERSION = 1

    fun exportToJson(tasks: List<Task>): String {
        val array = JSONArray()
        tasks.forEach { task ->
            array.put(JSONObject().apply {
                put("title", task.title)
                put("intervalDays", task.intervalDays)
                put("lastDoneAt", task.lastDoneAt)
                put("iconKey", task.iconKey)
                put("snoozedUntil", task.snoozedUntil)
            })
        }
        return JSONObject().apply {
            put("version", VERSION)
            put("tasks", array)
        }.toString(2)
    }

    fun importFromJson(json: String): List<Task> {
        val root = JSONObject(json)
        val array = root.getJSONArray("tasks")
        return (0 until array.length()).map { i ->
            array.getJSONObject(i).let { obj ->
                Task(
                    id = 0,
                    title = obj.getString("title"),
                    intervalDays = obj.getInt("intervalDays"),
                    lastDoneAt = obj.getLong("lastDoneAt"),
                    iconKey = obj.getString("iconKey"),
                    snoozedUntil = obj.optLong("snoozedUntil", 0L)
                )
            }
        }
    }
}
