package com.example.auspark_2_0

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.WorkInfo
import androidx.work.WorkManager

class Page_Schedule : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var adapter: EventAdapter
    private var currentFilter: String = "All"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_page_schedule)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = AppDatabase.getDatabase(this)
        val recycler = findViewById<RecyclerView>(R.id.eventsRecycler)
        recycler.layoutManager = LinearLayoutManager(this)
        // FIX 1: Ensure initial list type matches ScheduleUIItem
        adapter = EventAdapter(emptyList())
        recycler.adapter = adapter

        findViewById<View>(R.id.addFab).setOnClickListener {
            val intent = Intent(this, AddEventActivity::class.java)
            startActivity(intent)
        }
        findViewById<View>(R.id.settingsFab).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        findViewById<View>(R.id.addEventButton).setOnClickListener {
            val intent = Intent(this, AddEventActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        findViewById<View>(R.id.navProfile).setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
//            intent.putExtra(WebViewActivity.EXTRA_URL, WebViewActivity.DEFAULT_URL)
            startActivity(intent)
        }

        setupFilterChips()
        loadEvents()
    }

    override fun onResume() {
        super.onResume()
        loadEvents()
    }

    private fun setupFilterChips() {
        val chipAll = findViewById<LinearLayout>(R.id.chipAll)
        val chipClasses = findViewById<LinearLayout>(R.id.chipClasses)
        val chipExams = findViewById<LinearLayout>(R.id.chipExams)
        val chipSeminars = findViewById<LinearLayout>(R.id.chipSeminars)

        val chipAllIcon = findViewById<ImageView>(R.id.chipAllIcon)
        val chipClassesIcon = findViewById<ImageView>(R.id.chipClassesIcon)
        val chipExamsIcon = findViewById<ImageView>(R.id.chipExamsIcon)
        val chipSeminarsIcon = findViewById<ImageView>(R.id.chipSeminarsIcon)

        val chipAllText = findViewById<TextView>(R.id.chipAllText)
        val chipClassesText = findViewById<TextView>(R.id.chipClassesText)
        val chipExamsText = findViewById<TextView>(R.id.chipExamsText)
        val chipSeminarsText = findViewById<TextView>(R.id.chipSeminarsText)

        fun setSelected(
            selectedCard: LinearLayout,
            selectedIcon: ImageView,
            selectedText: TextView
        ) {
            val selectedBg = R.drawable.bg_chip_selected
            val unselectedBg = R.drawable.bg_chip_default
            val selectedColor = ContextCompat.getColor(this, R.color.au_red)
            val unselectedColor = ContextCompat.getColor(this, R.color.white)

            listOf(
                Triple(chipAll, chipAllIcon, chipAllText),
                Triple(chipClasses, chipClassesIcon, chipClassesText),
                Triple(chipExams, chipExamsIcon, chipExamsText),
                Triple(chipSeminars, chipSeminarsIcon, chipSeminarsText)
            ).forEach { (card, icon, text) ->
                card.setBackgroundResource(unselectedBg)
                icon.setColorFilter(unselectedColor)
                text.setTextColor(unselectedColor)
                text.setTypeface(text.typeface, android.graphics.Typeface.NORMAL)
            }

            selectedCard.setBackgroundResource(selectedBg)
            selectedIcon.setColorFilter(selectedColor)
            selectedText.setTextColor(selectedColor)
            selectedText.setTypeface(selectedText.typeface, android.graphics.Typeface.BOLD)
        }

        chipAll.setOnClickListener {
            currentFilter = "All"
            setSelected(chipAll, chipAllIcon, chipAllText)
            loadEvents()
        }
        chipClasses.setOnClickListener {
            currentFilter = "Class"
            setSelected(chipClasses, chipClassesIcon, chipClassesText)
            loadEvents()
        }
        chipExams.setOnClickListener {
            currentFilter = "Exam"
            setSelected(chipExams, chipExamsIcon, chipExamsText)
            loadEvents()
        }
        chipSeminars.setOnClickListener {
            currentFilter = "Seminar"
            setSelected(chipSeminars, chipSeminarsIcon, chipSeminarsText)
            loadEvents()
        }
    }

    private fun loadEvents() {
        CoroutineScope(Dispatchers.IO).launch {
            val uiList = mutableListOf<ScheduleUIItem>()
            val dao = db.auSparkDao()

            // 1. Fetch Manual Events
            val manualEvents = if (currentFilter == "All") {
                db.auSparkDao().getAllEvents()
            } else {
                db.auSparkDao().getEventsByType(currentFilter)
            }
            uiList.addAll(manualEvents.map {
                ScheduleUIItem(
                    it.title,
                    "${it.startTime} - ${it.endTime}",
                    it.type,
                    it.location,
                    false
                )
            })

            // 2. Fetch Scraped Classes (If "All" or "Class" filter is active)
            if (currentFilter == "All" || currentFilter == "Class") {
                val scrapedClasses = db.auSparkDao().getAllClasses()
                uiList.addAll(scrapedClasses.map {
                    ScheduleUIItem(
                        it.courseName,
                        "${it.day}: ${it.startTime}-${it.endTime}",
                        "Class",
                        it.room,
                        true
                    )
                })
            }

            // 3. Fetch Scraped Exams (If "All" or "Exam" filter is active)
            if (currentFilter == "All" || currentFilter == "Exam") {
                val scrapedExams = db.auSparkDao().getAllExams()
                uiList.addAll(scrapedExams.map {
                    ScheduleUIItem(
                        it.courseName,
                        "${it.examDate} (${it.examTime})",
                        "Exam",
                        it.room,
                        true
                    )
                })
            }

            withContext(Dispatchers.Main) {
                adapter.update(uiList)

                // Handle Empty State Visibility
                val emptyCard = findViewById<View>(R.id.emptyCard)
                val recycler = findViewById<RecyclerView>(R.id.eventsRecycler)

                if (uiList.isEmpty()) {
                    emptyCard.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                } else {
                    emptyCard.visibility = View.GONE
                    recycler.visibility = View.VISIBLE
                }
            }
        }
    }
}
