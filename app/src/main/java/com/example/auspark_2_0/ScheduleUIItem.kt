package com.example.auspark_2_0

data class ScheduleUIItem(
    val title: String,
    val time: String,
    val type: String, // "Class", "Exam", or "Seminar"
    val location: String,
    val isScraped: Boolean = false // To distinguish between manual and scraped data
)