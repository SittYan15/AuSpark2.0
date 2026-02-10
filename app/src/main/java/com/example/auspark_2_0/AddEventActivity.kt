package com.example.auspark_2_0

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar
import java.util.Locale

class AddEventActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private var selectedType: String = "Class"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_event)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<android.view.View>(R.id.backButton).setOnClickListener {
            finish()
        }

        db = AppDatabase.getDatabase(this)
        setupTypeSelector()
        setupDateTimePickers()
        setupSaveButton()
    }

    private fun setupTypeSelector() {
        val classCard = findViewById<LinearLayout>(R.id.typeClass)
        val examCard = findViewById<LinearLayout>(R.id.typeExam)
        val seminarCard = findViewById<LinearLayout>(R.id.typeSeminar)

        val classIcon = findViewById<ImageView>(R.id.typeClassIcon)
        val examIcon = findViewById<ImageView>(R.id.typeExamIcon)
        val seminarIcon = findViewById<ImageView>(R.id.typeSeminarIcon)

        val classText = findViewById<TextView>(R.id.typeClassText)
        val examText = findViewById<TextView>(R.id.typeExamText)
        val seminarText = findViewById<TextView>(R.id.typeSeminarText)

        fun setSelected(
            selectedCard: LinearLayout,
            selectedIcon: ImageView,
            selectedText: TextView
        ) {
            val unselectedBg = R.drawable.bg_chip_unselected
            val selectedBg = R.drawable.bg_chip_selected_blue
            val muted = ContextCompat.getColor(this, R.color.au_muted_icon)
            val active = ContextCompat.getColor(this, R.color.au_chip_blue)

            listOf(
                Triple(classCard, classIcon, classText),
                Triple(examCard, examIcon, examText),
                Triple(seminarCard, seminarIcon, seminarText)
            ).forEach { (card, icon, text) ->
                card.setBackgroundResource(unselectedBg)
                icon.setColorFilter(muted)
                text.setTextColor(muted)
                text.setTypeface(text.typeface, android.graphics.Typeface.NORMAL)
            }

            selectedCard.setBackgroundResource(selectedBg)
            selectedIcon.setColorFilter(active)
            selectedText.setTextColor(active)
            selectedText.setTypeface(selectedText.typeface, android.graphics.Typeface.BOLD)
        }

        classCard.setOnClickListener {
            selectedType = "Class"
            setSelected(classCard, classIcon, classText)
        }
        examCard.setOnClickListener {
            selectedType = "Exam"
            setSelected(examCard, examIcon, examText)
        }
        seminarCard.setOnClickListener {
            selectedType = "Seminar"
            setSelected(seminarCard, seminarIcon, seminarText)
        }
    }

    private fun setupDateTimePickers() {
        val dateField = findViewById<LinearLayout>(R.id.dateField)
        val dateValue = findViewById<TextView>(R.id.dateValue)
        val startTimeField = findViewById<LinearLayout>(R.id.startTimeField)
        val startTimeValue = findViewById<TextView>(R.id.startTimeValue)
        val endTimeField = findViewById<LinearLayout>(R.id.endTimeField)
        val endTimeValue = findViewById<TextView>(R.id.endTimeValue)

        val calendar = Calendar.getInstance()

        dateField.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(Calendar.YEAR, y)
                calendar.set(Calendar.MONTH, m)
                calendar.set(Calendar.DAY_OF_MONTH, d)
                dateValue.text = String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y)
                dateValue.setTextColor(ContextCompat.getColor(this, R.color.au_text_dark))
            }, year, month, day).show()
        }

        startTimeField.setOnClickListener {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            TimePickerDialog(this, { _, h, m ->
                startTimeValue.text = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                startTimeValue.setTextColor(ContextCompat.getColor(this, R.color.au_text_dark))
            }, hour, minute, true).show()
        }

        endTimeField.setOnClickListener {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            TimePickerDialog(this, { _, h, m ->
                endTimeValue.text = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                endTimeValue.setTextColor(ContextCompat.getColor(this, R.color.au_text_dark))
            }, hour, minute, true).show()
        }
    }

    private fun setupSaveButton() {
        val saveBar = findViewById<LinearLayout>(R.id.saveBar)
        val saveIcon = findViewById<ImageView>(R.id.saveIcon)
        val saveText = findViewById<TextView>(R.id.saveText)
        val titleInput = findViewById<EditText>(R.id.titleInput)
        val locationInput = findViewById<EditText>(R.id.locationInput)
        val descriptionInput = findViewById<EditText>(R.id.descriptionInput)
        val dateValue = findViewById<TextView>(R.id.dateValue)
        val startTimeValue = findViewById<TextView>(R.id.startTimeValue)
        val endTimeValue = findViewById<TextView>(R.id.endTimeValue)

        // Enable visuals for clickable save button (placeholder behavior).
        saveBar.setBackgroundResource(R.drawable.bg_add_button)
        saveIcon.setColorFilter(ContextCompat.getColor(this, R.color.white))
        saveText.setTextColor(ContextCompat.getColor(this, R.color.white))

        saveBar.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val date = dateValue.text.toString().trim()
            val startTime = startTimeValue.text.toString().trim()
            val endTime = endTimeValue.text.toString().trim()
            val location = locationInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (date == "dd/mm/yyyy") {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (startTime == "--:--" || endTime == "--:--") {
                Toast.makeText(this, "Please select start and end time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val event = EventEntity(
                title = title,
                date = date,
                startTime = startTime,
                endTime = endTime,
                type = selectedType,
                location = location,
                description = description
            )

            CoroutineScope(Dispatchers.IO).launch {
                db.auSparkDao().insertEvent(event)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddEventActivity, "Event saved", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
