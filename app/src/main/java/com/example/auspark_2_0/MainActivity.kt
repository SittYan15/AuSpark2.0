package com.example.auspark_2_0

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupTestDataButton()
    }

    private fun setupTestDataButton() {

        // Reload Data Button
        val button = findViewById<Button>(R.id.testButton)
        button.setOnClickListener {
            val intent = Intent(this, DataTestingActivity::class.java)
            startActivity(intent)
        }

        // Schedule Button
        val showScheduleButton = findViewById<Button>(R.id.showSchedule)
        showScheduleButton.setOnClickListener {
            val intent = Intent(this, Page_Schedule::class.java)
            startActivity(intent)
        }
    }
}