package com.example.auspark_2_0

import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import java.util.Calendar
import java.util.Locale
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ClassAdapter
    private lateinit var db: AppDatabase
    private lateinit var emptyMessage: TextView

    private var selectedDayCard: LinearLayout? = null
    private var selectedDayLabel: TextView? = null
    private var selectedDateLabel: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        setupScheduleSection()
        setupNavigation()
        setupDayCards()
    }

//    override fun onWindowFocusChanged(hasFocus: Boolean) {
//        super.onWindowFocusChanged(hasFocus)
//        if (hasFocus) {
//            hideSystemBars()
//        }
//    }

//    private fun hideSystemBars() {
//        val controller = WindowInsetsControllerCompat(window, window.decorView)
//        controller.systemBarsBehavior =
//            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//        controller.hide(WindowInsetsCompat.Type.systemBars())
//    }

    private fun setupScheduleSection() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ClassAdapter(emptyList())
        recyclerView.adapter = adapter

        emptyMessage = findViewById(R.id.emptyMessage)

        db = AppDatabase.getDatabase(this)

        loadClasses("Tuesday")
    }

    private fun loadClasses(day: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val classes = db.auSparkDao().getClassesForDay(day)
            withContext(Dispatchers.Main) {
                adapter.updateDate(classes)
                updateEmptyState(classes.isEmpty())
            }
        }
    }

    private fun loadAllClasses() {
        CoroutineScope(Dispatchers.IO).launch {
            val classes = db.auSparkDao().getAllClasses()
            withContext(Dispatchers.Main) {
                adapter.updateDate(classes)
                updateEmptyState(classes.isEmpty())
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        emptyMessage.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun setupNavigation() {
        findViewById<View>(R.id.navSchedule).setOnClickListener {
            val intent = Intent(this, Page_Schedule::class.java)
            startActivity(intent)
        }
        findViewById<View>(R.id.navProfile).setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
//            intent.putExtra(WebViewActivity.EXTRA_URL, WebViewActivity.DEFAULT_URL)
            startActivity(intent)
        }
    }

    private fun setupDayCards() {
        val dayCards = arrayOf(
            findViewById<LinearLayout>(R.id.dayCard0),
            findViewById(R.id.dayCard1),
            findViewById(R.id.dayCard2),
            findViewById(R.id.dayCard3),
            findViewById(R.id.dayCard4)
        )
        val dayLabels = arrayOf(
            findViewById<TextView>(R.id.dayLabel0),
            findViewById(R.id.dayLabel1),
            findViewById(R.id.dayLabel2),
            findViewById(R.id.dayLabel3),
            findViewById(R.id.dayLabel4)
        )
        val dateLabels = arrayOf(
            findViewById<TextView>(R.id.dateLabel0),
            findViewById(R.id.dateLabel1),
            findViewById(R.id.dateLabel2),
            findViewById(R.id.dateLabel3),
            findViewById(R.id.dateLabel4)
        )

        val calendar = Calendar.getInstance(Locale.getDefault())
        for (i in dayCards.indices) {
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            dayLabels[i].text = dayAbbrev(dayOfWeek)
            dateLabels[i].text = calendar.get(Calendar.DAY_OF_MONTH).toString()

            if (i == 0) {
                dayCards[i].setBackgroundResource(R.drawable.bg_day_selected)
                dayLabels[i].setTextColor(getColor(R.color.white))
                dateLabels[i].setTextColor(getColor(R.color.white))
                // Set initial selection (Today)
                updateCardSelection(dayCards[i], dayLabels[i], dateLabels[i])
            } else {
                dayCards[i].setBackgroundResource(R.drawable.bg_day_default)
                dayLabels[i].setTextColor(getColor(R.color.au_text_muted))
                dateLabels[i].setTextColor(getColor(R.color.au_text_dark))
            }

            // Pass everything to the click setup
            setupDayButton(dayCards[i], dayLabels[i], dateLabels[i], dayFullName(dayOfWeek))

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun updateCardSelection(newCard: LinearLayout, newDayLabel: TextView, newDateLabel: TextView) {
        // 1. Reset the previous selection if it exists
        selectedDayCard?.apply {
            setBackgroundResource(R.drawable.bg_day_default)
        }
        selectedDayLabel?.setTextColor(getColor(R.color.au_text_muted))
        selectedDateLabel?.setTextColor(getColor(R.color.au_text_dark))

        // 2. Apply the "Selected" (Red) style to the new card
        newCard.setBackgroundResource(R.drawable.bg_day_selected)
        newDayLabel.setTextColor(getColor(R.color.white))
        newDateLabel.setTextColor(getColor(R.color.white))

        // 3. Update the tracking variables
        selectedDayCard = newCard
        selectedDayLabel = newDayLabel
        selectedDateLabel = newDateLabel
    }

    private fun setupDayButton(card: LinearLayout, dayLabel: TextView, dateLabel: TextView, dayName: String) {
        card.setOnClickListener {
            // Change the colors visually
            updateCardSelection(card, dayLabel, dateLabel)

            // Load the actual data from Room
            loadClasses(dayName)
        }
    }

    private fun dayAbbrev(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            else -> "SUN"
        }
    }
    private fun dayFullName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Sunday"
        }
    }
}
