package com.example.auspark_2_0

import android.app.NotificationManager
import android.app.PendingIntent // <--- This is the one causing the error
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val title = intent.getStringExtra("EVENT_TITLE") ?: "Class Reminder"

// 1. Start the Sound/Vibration engine
        val serviceIntent = Intent(context, AlarmService::class.java)
        context.startService(serviceIntent)

// 2. Create the Intent for the Stop Page
        val dismissIntent = Intent(context, AlarmDismissActivity::class.java).apply {
            // These three flags ensure the activity pops up even if the app is open
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

            putExtra("EVENT_TITLE", title)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Fixed line 25
        )

// 3. Build a high-priority notification that "launches" the UI
        val builder = androidx.core.app.NotificationCompat.Builder(context, "ALARM_CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_schedule) // Ensure this exists in your drawables
            .setContentTitle("AU Spark Alarm")
            .setContentText(title)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true) // This pops up the UI!
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(1, builder.build())
    }
}