package com.example.auspark_2_0

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class AlarmDismissActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lock screen
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        setContentView(R.layout.activity_alarm_dismiss)

        val title = intent.getStringExtra("EVENT_TITLE") ?: "Class Reminder"
        findViewById<TextView>(R.id.dismissTitle).text = title

        findViewById<MaterialCardView>(R.id.btnStopAlarm).setOnClickListener {
            // Use the helper to stop the service
            AlarmService.stopAlarm(this)
            finish() // Close this page to reveal the app underneath
        }
    }

    // Prevents the "Stop Button Disappearing" bug
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing - forces user to use the STOP button
    }
}