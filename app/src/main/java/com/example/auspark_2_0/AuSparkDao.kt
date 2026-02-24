package com.example.auspark_2_0

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuSparkDao {

    // --- Profile Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: StudentEntity)

    @Query("SELECT * FROM student_profile LIMIT 1")
    suspend fun getProfile(): StudentEntity?

    // --- Schedule Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<ClassEntity>)

    @Query("DELETE FROM class_schedule")
    suspend fun clearSchedule() // Clear old schedule before saving new one

    // Get all classes
    @Query("SELECT * FROM class_schedule")
    suspend fun getAllClasses(): List<ClassEntity>

    // Get classes for a specific day (Useful for UI!)
    @Query("SELECT * FROM class_schedule WHERE day = :dayName ORDER BY startTime ASC")
    suspend fun getClassesForDay(dayName: String): List<ClassEntity>

    // --- Custom Events ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Query("SELECT * FROM custom_events ORDER BY date ASC, startTime ASC")
    suspend fun getAllEvents(): List<EventEntity>

    @Query("SELECT * FROM custom_events WHERE type = :type ORDER BY date ASC, startTime ASC")
    suspend fun getEventsByType(type: String): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<ExamEntity>)

    @Query("DELETE FROM exam_schedule")
    suspend fun clearExams()

    @Query("SELECT * FROM exam_schedule ORDER BY id ASC")
    suspend fun getAllExams(): List<ExamEntity>

    // Add these to your existing interface
    @Query("SELECT * FROM exam_schedule ORDER BY examDate ASC")
    suspend fun getAllExamsByDate(): List<ExamEntity>

    @Query("SELECT * FROM class_schedule ORDER BY day ASC, startTime ASC")
    suspend fun getAllClassesByDate(): List<ClassEntity>
}
