package com.example.auspark_2_0

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
            intent.putExtra(WebViewActivity.EXTRA_URL, WebViewActivity.DEFAULT_URL)
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
            val events = if (currentFilter == "All") {
                db.auSparkDao().getAllEvents()
            } else {
                db.auSparkDao().getEventsByType(currentFilter)
            }
            withContext(Dispatchers.Main) {
                adapter.update(events)
                val emptyCard = findViewById<View>(R.id.emptyCard)
                val recycler = findViewById<RecyclerView>(R.id.eventsRecycler)
                if (events.isEmpty()) {
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
