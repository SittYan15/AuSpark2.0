package com.example.auspark_2_0

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log

class AlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = getSharedPreferences("AU_SPARK_SETTINGS", Context.MODE_PRIVATE)

        // 1. Handle Audio
        val soundUriString = prefs.getString("ALARM_SOUND_URI", null)
        try {
            mediaPlayer = MediaPlayer()
            val uri = if (soundUriString != null) Uri.parse(soundUriString) else null

            if (uri != null) {
                mediaPlayer?.setDataSource(this, uri)
            } else {
                val defaultSound = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                mediaPlayer?.setDataSource(this, defaultSound)
            }
            mediaPlayer?.isLooping = true
            mediaPlayer?.prepare()
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e("AlarmService", "Error playing sound", e)
        }

        // 2. Handle Vibration System
        val patternType = prefs.getString("VIBRATION_PATTERN", "Standard")
        if (patternType != "None") {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val pattern = when (patternType) {
                "Heartbeat" -> longArrayOf(0, 200, 200, 200)
                "Rapid" -> longArrayOf(0, 100, 50, 100)
                else -> longArrayOf(0, 1000, 500)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0)) // 0 means repeat
            } else {
                vibrator?.vibrate(pattern, 0)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("AlarmService", "Stopping sound and vibration...")

        // 1. Stop and release MediaPlayer
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null

        // 2. Cancel Vibrator
        vibrator?.cancel()
        vibrator = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        // This is the static function your Dismiss Activity calls to stop the red text
        fun stopAlarm(context: Context) {
            val intent = Intent(context, AlarmService::class.java)
            context.stopService(intent)
        }
    }
}