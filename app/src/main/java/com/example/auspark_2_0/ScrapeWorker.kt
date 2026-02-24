package com.example.auspark_2_0

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.jsoup.Jsoup

class ScrapeWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // Retrieve data passed from Activity
        val targetUrl = inputData.getString("TARGET_URL") ?: return Result.failure()
        val examUrl = inputData.getString("TARGET_URL_EXAM") ?: return Result.failure()
        val cookieString = inputData.getString("COOKIES") ?: return Result.failure()

        return try {
            Log.d("ScrapeWorker", "Starting multi-page scrape...")

            // 1. Perform Jsoup Scrape
//            val doc = Jsoup.connect(targetUrl)
//                .header("Cookie", cookieString)
//                .userAgent("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Mobile Safari/537.36")
//                .timeout(30000)
//                .get()

            val scheduleDoc = Jsoup.connect(targetUrl).header("Cookie", cookieString).get()
//            val examDoc = Jsoup.connect(examUrl).header("Cookie", cookieString).get()

            val rawData = scheduleDoc.body().text()

            // 2. Parse the data
            val parser = AuSparkParser()
            val result = parser.parse(rawData)

            // 3. Save to Database
            val db = AppDatabase.getDatabase(applicationContext)
            val dao = db.auSparkDao()

            val profileEntity = StudentEntity(
                studentId = result.profile.studentId,
                name = result.profile.name,
                major = result.profile.major,
                gpa = result.profile.gpa,
                credits = result.profile.credits
            )

            val classEntities = result.schedule.map { item ->
                ClassEntity(
                    day = item.day,
                    startTime = item.startTime,
                    endTime = item.endTime,
                    courseCode = item.courseCode,
                    section = item.section,
                    courseName = item.courseName,
                    room = item.room,
                    instructor = item.instructor,
                    campus = item.campus,
                    ethicSeminar = item.ethicSeminar
                )
            }

            dao.saveProfile(profileEntity)
            dao.clearSchedule()
            dao.insertClasses(classEntities)

            Log.d("ScrapeWorker", "Background Scrape & Save Successful!")


            // 1. Connect to the Examination Page
            val doc2 = Jsoup.connect(examUrl)
                .header("Cookie", cookieString)
                .userAgent("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Mobile Safari/537.36")
                .timeout(30000)
                .get()

            // 2. Extract Raw Body Text
            val rawExamData = doc2.body().text()

            // Inside doWork() after you get rawExamData
            val parsedExams = parser.parseExams(rawExamData)

            // Clear and Save to Room
            dao.clearExams()
            dao.insertExams(parsedExams)

            Log.d("ScrapeWorker", "Successfully parsed and saved ${parsedExams.size} exams.")

            val alarmController = AlarmController(applicationContext)
            // Re-schedule all alarms now that the database has fresh data
            alarmController.scheduleAllAlarms()

            Result.success()
        } catch (e: Exception) {
            Log.e("ScrapeWorker", "Background Scrape Failed", e)
            Result.retry() // WorkManager will try again later if it fails
        }
    }
}