package com.example.auspark_2_0

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Page_Schedule : AppCompatActivity() {

    private lateinit var adapter: ClassAdapter
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set up edge-to-edge display
        // enableEdgeToEdge()
        setContentView(R.layout.activity_page_schedule)

        // 1. Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with empty list first
        adapter = ClassAdapter(emptyList())
        recyclerView.adapter = adapter

        // 2. Initialize Database
        db = AppDatabase.getDatabase(this)

        // 3. Load Data (Default: Show "Monday" or "All")
        loadClasses("Monday")

        // 4. Setup Buttons
        setupDayButton(R.id.btnMon, "Monday")
        setupDayButton(R.id.btnTue, "Tuesday")
        setupDayButton(R.id.btnWed, "Wednesday")
        setupDayButton(R.id.btnThu, "Thursday")
        setupDayButton(R.id.btnFri, "Friday")

        findViewById<Button>(R.id.btnAll).setOnClickListener {
            loadAllClasses()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupDayButton(btnId: Int, dayName: String) {
        findViewById<Button>(btnId).setOnClickListener {
            loadClasses(dayName)
        }
    }

    private fun loadClasses(day: String) {
        CoroutineScope(Dispatchers.IO).launch {
            // Call the Query we wrote in AuSparkDao
            val classes = db.auSparkDao().getClassesForDay(day)

            withContext(Dispatchers.Main) {
                adapter.updateDate(classes)
            }
        }
    }

    private fun loadAllClasses() {
        CoroutineScope(Dispatchers.IO).launch {
            val classes = db.auSparkDao().getAllClasses()
            withContext(Dispatchers.Main) {
                adapter.updateDate(classes)
            }
        }
    }
}