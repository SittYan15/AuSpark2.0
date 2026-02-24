package com.example.auspark_2_0

import androidx.room.Entity
import androidx.room.PrimaryKey

// Table 1: Student Profile
@Entity(tableName = "student_profile")
data class StudentEntity(
    @PrimaryKey val studentId: String, // e.g., "6722114"
    val name: String,
    val major: String,
    val gpa: String,
    val credits: String
)

// Table 2: Class Schedule
// We use autoGenerate = true because one course code (like ELE2001)
// might appear twice in a week (Tue/Fri), so we need a unique ID for each row.
@Entity(tableName = "class_schedule")
data class ClassEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val day: String,           // "Monday"
    val startTime: String,     // "13:30"
    val endTime: String,       // "16:30"
    val courseCode: String,    // "CSX4407"
    val section: String,       // "541"
    val courseName: String,    // "ENTERPRISE APPLICATION..."
    val room: String,          // "VMES1004"
    val instructor: String,    // "CHAYAPOL..."
    val campus: String,        // "SUVARNABHUMI"
    val ethicSeminar: Boolean  // True/False
)

// Table 3: Custom Events
@Entity(tableName = "custom_events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String,       // dd/MM/yyyy
    val startTime: String,  // HH:mm
    val endTime: String,    // HH:mm
    val type: String,       // Class / Exam / Seminar
    val location: String,
    val description: String
)

@Entity(tableName = "exam_schedule")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseCode: String,
    val courseName: String,
    val examDate: String, // e.g., "5 March 2026"
    val examTime: String, // e.g., "09:00 - 12:00"
    val room: String,
    val seat: String      // Set to "N/A" if not found in raw text
)
