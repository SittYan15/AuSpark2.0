package com.example.auspark_2_0

data class StudentSchedule(
    val day: String,
    val startTime: String,
    val endTime: String,
    val courseCode: String,
    val section: String,
    val courseName: String,
    val room: String,
    val instructor: String,
    val campus: String,
    val ethicSeminar: Boolean,
)
