package com.example.auspark_2_0

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Use the safe call operator or requireContext to avoid nullability issues
        val ctx = context ?: return

        Toast.makeText(ctx, "ALARM TRIGGERED!", Toast.LENGTH_LONG).show()

        val vibrator = ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Pattern: Sleep 0ms, Vibrate 500ms, Sleep 500ms, Vibrate 500ms
        val pattern = longArrayOf(0, 500, 500, 500)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // -1 means do not repeat. Change to 0 to repeat until cancelled.
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }

        // TODO: Trigger a Notification here to ensure the user sees it!
    }
}