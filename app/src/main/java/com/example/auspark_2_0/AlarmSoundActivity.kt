package com.example.auspark_2_0

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.OpenableColumns
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AlarmSoundActivity : AppCompatActivity() {

    private lateinit var currentSoundText: TextView

    private val localPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val prefs = getSharedPreferences("AU_SPARK_SETTINGS", Context.MODE_PRIVATE)
            prefs.edit().putString("ALARM_SOUND_URI", uri.toString()).apply()

            currentSoundText.text = getDisplayName(uri) ?: "Local file selected"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alarm_sound)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentSoundText = findViewById(R.id.currentSound)

        findViewById<android.view.View>(R.id.backButton).setOnClickListener { finish() }
        findViewById<android.view.View>(R.id.pickLocalButton).setOnClickListener {
            localPickerLauncher.launch(arrayOf("audio/*"))
        }

        // --- VIBRATION SYSTEM LOGIC ---
        val sharedPrefs = getSharedPreferences("AU_SPARK_SETTINGS", Context.MODE_PRIVATE)
        val vibrationGroup = findViewById<RadioGroup>(R.id.vibrationPatternGroup)

        // Load the saved pattern (defaulting to Standard)
        val savedPattern = sharedPrefs.getString("VIBRATION_PATTERN", "Standard")
        when (savedPattern) {
            "None" -> findViewById<RadioButton>(R.id.vibNone).isChecked = true
            "Standard" -> findViewById<RadioButton>(R.id.vibStandard).isChecked = true
            "Heartbeat" -> findViewById<RadioButton>(R.id.vibHeartbeat).isChecked = true
            "Rapid" -> findViewById<RadioButton>(R.id.vibRapid).isChecked = true
        }

        // Save pattern and provide a quick haptic preview
        vibrationGroup.setOnCheckedChangeListener { _, checkedId ->
            val pattern = when (checkedId) {
                R.id.vibNone -> "None"
                R.id.vibHeartbeat -> "Heartbeat"
                R.id.vibRapid -> "Rapid"
                else -> "Standard"
            }
            sharedPrefs.edit().putString("VIBRATION_PATTERN", pattern).apply()
            testVibration(pattern)
        }
    }

    private fun testVibration(pattern: String) {
        if (pattern == "None") return
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val effect = when (pattern) {
            "Heartbeat" -> longArrayOf(0, 100, 100, 100)
            "Rapid" -> longArrayOf(0, 50, 50, 50)
            else -> longArrayOf(0, 500) // Standard
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(effect, -1))
        } else {
            vibrator.vibrate(effect, -1)
        }
    }

    private fun getDisplayName(uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
        return null
    }
}