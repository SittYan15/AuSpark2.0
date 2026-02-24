package com.example.auspark_2_0

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Bundle
import android.content.Intent
import android.os.Build
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Date

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<android.view.View>(R.id.backButton).setOnClickListener { finish() }
        findViewById<android.view.View>(R.id.alarmSoundOption).setOnClickListener {
            val intent = Intent(this, AlarmSoundActivity::class.java)
            startActivity(intent)
        }

        val btnALarm = findViewById<Button>(R.id.btn_alarm)
        btnALarm.setOnClickListener {
            // 2. You need to provide a time (e.g., current time + 10 seconds for testing)
            val triggerTime = System.currentTimeMillis() + 10000
            setAlarm(triggerTime)
        }
    }

    // 3. Simplified signature (removed redundant context parameter)
    fun setAlarm(timeInMillis: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Add this inside your setAlarm function to check for permission on newer Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent
        )

        Toast.makeText(this, "Alarm set for ${Date(timeInMillis)}", Toast.LENGTH_LONG).show()
    }
}
