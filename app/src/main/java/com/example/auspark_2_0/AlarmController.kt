package com.example.auspark_2_0

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AlarmController(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val sharedPrefs = context.getSharedPreferences("AU_SPARK_SETTINGS", Context.MODE_PRIVATE)

    /**
     * Entry point to refresh all academic alarms.
     * Should be called after a successful scrape or when settings change.
     */
    fun scheduleAllAlarms() {
        CoroutineScope(Dispatchers.IO).launch {
            val leadTimeMinutes = sharedPrefs.getInt("LEAD_TIME_MINUTES", 15)

            // 1. Fetch data from Room
            val classes = db.auSparkDao().getAllClasses()
            val exams = db.auSparkDao().getAllExams()

            // 2. Schedule each class (weekly repeating logic)
            classes.forEach { course ->
                val triggerTime = calculateClassTime(course.day, course.startTime, leadTimeMinutes)
                if (triggerTime > System.currentTimeMillis()) {
                    setSystemAlarm(triggerTime, course.courseName, course.id, "Class")
                }
            }

            // 3. Schedule each exam (specific date logic)
            exams.forEach { exam ->
                val triggerTime = calculateExamTime(exam.examDate, exam.examTime, leadTimeMinutes)
                if (triggerTime != null && triggerTime > System.currentTimeMillis()) {
                    setSystemAlarm(triggerTime, exam.courseName, exam.id + 1000, "Exam")
                }
            }
        }
    }

    private fun setSystemAlarm(triggerTime: Long, title: String, id: Int, type: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EVENT_TITLE", title)
            putExtra("EVENT_TYPE", type)
        }

        // Use unique ID (course/exam ID) to prevent alarms from overwriting each other
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) return
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
        Log.d("AlarmController", "Scheduled $type: $title at ${Date(triggerTime)}")
    }

    /**
     * Logic for Weekly Classes (e.g., "Monday" at "09:00")
     */
    private fun calculateClassTime(day: String, startTime: String, leadTime: Int): Long {
        val calendar = Calendar.getInstance()
        val dayOfWeek = when (day.lowercase(Locale.ENGLISH)) {
            "monday" -> Calendar.MONDAY
            "tuesday" -> Calendar.TUESDAY
            "wednesday" -> Calendar.WEDNESDAY
            "thursday" -> Calendar.THURSDAY
            "friday" -> Calendar.FRIDAY
            "saturday" -> Calendar.SATURDAY
            "sunday" -> Calendar.SUNDAY
            else -> calendar.get(Calendar.DAY_OF_WEEK)
        }

        val timeParts = startTime.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        // If time passed for this week, move to next week
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        return calendar.timeInMillis - (leadTime.toLong() * 60 * 1000)
    }

    /**
     * Logic for One-time Exams (e.g., "5 March 2026" at "09:00")
     */
    private fun calculateExamTime(dateStr: String, timeRange: String, leadTime: Int): Long? {
        return try {
            val startTime = timeRange.split("-")[0].trim() // "09:00" from "09:00 - 12:00"
            val fullDate = "$dateStr $startTime"
            val sdf = SimpleDateFormat("d MMMM yyyy HH:mm", Locale.ENGLISH)
            val date = sdf.parse(fullDate)
            date?.time?.minus(leadTime.toLong() * 60 * 1000)
        } catch (e: Exception) {
            null
        }
    }

    // Inside AlarmController.kt

    fun stopAlarm() {
        val intent = Intent(context, AlarmService::class.java)
        context.stopService(intent) // This tells the engine to stop sound and vibration
    }

    fun snoozeAlarm(minutes: Int = 5) {
        // 1. Stop the current ringing
        stopAlarm()

        // 2. Schedule a new one-time alarm for X minutes from now
        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EVENT_TITLE", "Snoozed Class Reminder")
            putExtra("EVENT_TYPE", "Snooze")
        }

        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            999, // Use a unique ID for snooze so it doesn't overwrite real classes
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            android.app.AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    // Inside AlarmController.kt

    fun triggerTestAlarm() {
        val triggerTime = System.currentTimeMillis() + 5000 // 5 seconds from now

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EVENT_TITLE", "Test Alarm")
            putExtra("EVENT_TYPE", "TEST")
        }

        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            888, // Unique ID for test
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

        // Use setExact to ensure it goes off immediately for testing
        alarmManager.setExactAndAllowWhileIdle(
            android.app.AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}