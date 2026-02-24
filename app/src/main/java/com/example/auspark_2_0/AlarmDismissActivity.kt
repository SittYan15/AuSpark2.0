package com.example.auspark_2_0

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class AlarmDismissActivity : AppCompatActivity() {
    // Inside AlarmDismissActivity.kt
// Inside AlarmDismissActivity.kt
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // 1. Critical: Bypass lock screen and turn on display
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        // Inside AlarmDismissActivity.kt onCreate
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        setContentView(R.layout.activity_alarm_dismiss)

        val controller = AlarmController(this)

        // Stop Button
        val btnStop = findViewById<com.google.android.material.card.MaterialCardView>(R.id.btnStopAlarm)

        btnStop.setOnClickListener {
            Log.d("AlarmDismiss", "Stopping alarm...")

            // 1. Tell the service to shut down
            // This triggers onDestroy() in AlarmService.kt to release MediaPlayer
            AlarmService.stopAlarm(this)

            // 2. Close the UI
            finish()
        }

        // Optional: If you add a Snooze button to your UI
//        findViewById<android.view.View>(R.id.btnSnoozeAlarm)?.setOnClickListener {
//            controller.snoozeAlarm(5) // Snooze for 5 minutes
//            finish()
//        }

        // Inside AlarmDismissActivity.kt

        findViewById<MaterialCardView>(R.id.btnStopAlarm).setOnClickListener {
            Log.d("AlarmTest", "Stop button clicked!")

            // Method A: Direct Stop (Most Reliable)
            val stopIntent = Intent(this, AlarmService::class.java)
            stopService(stopIntent)

            // Method B: Using your Controller (If you prefer)
            // val controller = AlarmController(this)
            // controller.stopAlarm()

            finish() // Close the dismissal page
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Update the UI with the new alarm title
        val title = intent.getStringExtra("EVENT_TITLE") ?: "Class Reminder"
        findViewById<TextView>(R.id.dismissTitle).text = title
    }
}