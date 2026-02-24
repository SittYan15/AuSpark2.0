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

    private lateinit var sharedPrefs: android.content.SharedPreferences

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

        // Inside SettingsActivity.kt onCreate

        val btnTest = findViewById<Button>(R.id.btn_alarm)
        val controller = AlarmController(this)

        btnTest.setOnClickListener {
            Toast.makeText(this, "Alarm will trigger in 5 seconds...", Toast.LENGTH_SHORT).show()
            controller.triggerTestAlarm()
        }

        sharedPrefs = getSharedPreferences("AU_SPARK_SETTINGS", Context.MODE_PRIVATE)
        val leadTimeGroup = findViewById<android.widget.RadioGroup>(R.id.leadTimeGroup)

        // Load the previously saved setting
        val savedTime = sharedPrefs.getInt("LEAD_TIME_MINUTES", 15)
        when (savedTime) {
            15 -> findViewById<android.widget.RadioButton>(R.id.time15m).isChecked = true
            60 -> findViewById<android.widget.RadioButton>(R.id.time1h).isChecked = true
            90 -> findViewById<android.widget.RadioButton>(R.id.time1h30m).isChecked = true
            150 -> findViewById<android.widget.RadioButton>(R.id.time2h30m).isChecked = true
        }

        // Save the setting whenever a radio button is clicked
        leadTimeGroup.setOnCheckedChangeListener { _, checkedId ->
            val minutes = when (checkedId) {
                R.id.time15m -> 15
                R.id.time1h -> 60
                R.id.time1h30m -> 90
                R.id.time2h30m -> 150
                else -> 15
            }
            sharedPrefs.edit().putInt("LEAD_TIME_MINUTES", minutes).apply()
            Toast.makeText(this, "Reminder set to $minutes minutes before class", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. Simplified signature (removed redundant context parameter)
    fun setAlarm(classStartTimeMillis: Long) {

        // Now classStartTimeMillis is recognized!
        val leadTimeMinutes = sharedPrefs.getInt("LEAD_TIME_MINUTES", 15)
        val triggerTime = classStartTimeMillis - (leadTimeMinutes * 60 * 1000)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        // Use triggerTime (the adjusted time) instead of the raw class start time
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )

        Toast.makeText(this, "Alarm set for ${Date(triggerTime)}", Toast.LENGTH_LONG).show()
    }
}
