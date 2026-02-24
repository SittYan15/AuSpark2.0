package com.example.auspark_2_0

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Prevent duplicate sound loops by stopping any previous instance
        context.stopService(Intent(context, AlarmService::class.java))

        val title = intent.getStringExtra("EVENT_TITLE") ?: "Class Reminder"

        // 1. Start the Sound/Vibration engine
        val serviceIntent = Intent(context, AlarmService::class.java)
        context.startService(serviceIntent)

        // 2. Intent to open the Alarm UI with flags to prevent stacking
        val dismissIntent = Intent(context, AlarmDismissActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("EVENT_TITLE", title)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Intent to stop alarm when notification is swiped away
        val deleteIntent = Intent(context, AlarmService::class.java).apply {
            action = "ACTION_STOP_ALARM"
        }
        val deletePendingIntent = PendingIntent.getService(
            context, 1, deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Build high-priority notification
        val builder = NotificationCompat.Builder(context, "ALARM_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_schedule)
            .setContentTitle("AU Spark Alarm")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true) // Makes UI pop up
            .setDeleteIntent(deletePendingIntent)     // Stops alarm on swipe
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
    }
}