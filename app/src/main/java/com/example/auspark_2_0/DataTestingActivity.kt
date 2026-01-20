package com.example.auspark_2_0

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class DataTestingActivity : AppCompatActivity() {

    var rawData = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_testing) // Always set content view first!

        // Handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val webView = findViewById<WebView>(R.id.webView)

        // 1. The URL we start with (and end with)
        val homeUrl = "https://auspark.au.edu/"

        // 2. The URL we actually want to scrape data from
        val targetDataUrl = "https://auspark.au.edu/ClassSchedule"

        // IMPORTANT: Enable Cookies and JavaScript
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true // Required for many modern logins

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Debug: See exactly where we are
                Log.d("MyTag", "Current URL: $url")

                if (url == null) return

                // --- THE LOGIC ---

                // 1. Check if we are at the Home URL (The "Success" Destination)
                // We use equals() or endsWith() to be precise so we don't trigger on "Login"
                val atHomePage = url == "https://auspark.au.edu/" || url == "https://auspark.au.edu"

                if (atHomePage) {
                    // 2. Check if we have cookies (Proof that we are logged in)
                    val cookies = cookieManager.getCookie(url)

                    if (cookies != null && cookies.isNotEmpty()) {
                        Log.d("MyTag", "LOGIN SUCCESS! Found Cookies: $cookies")

                        // 3. Optional: Hide WebView now that we are done with it
                        // webView.visibility = View.GONE

                        // 4. Go get the Class Schedule!
                        scrapeClassSchedule(targetDataUrl, cookies)
                    } else {
                        Log.d("MyTag", "At home page, but no cookies yet. Waiting for redirect...")
                    }
                }
            }
        }

        // Start the process
        webView.loadUrl(homeUrl)
    }

    private fun scrapeClassSchedule(url: String, cookieString: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("MyTag", "Starting background scrape for: $url")

                // 1. Connect to the Schedule Page using the Login Cookies
                val doc = Jsoup.connect(url)
                    .header("Cookie", cookieString) // <--- The Key!
                    .userAgent("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Mobile Safari/537.36")
                    .get()

                // 2. DEBUG: Print the page title to prove we are in
                val title = doc.title()
                Log.d("MyTag", "Scrape Success! Page Title: $title")

                // 3. DEBUG: Print the whole body text to see your classes in the log
                // (You can filter this later)
                val bodyText = doc.body().text()
                rawData = bodyText
                Log.d("MyTag", "Page Content: $bodyText")

                textTheParser(rawData)

                // Data Saving Method Tow using txt file (can delete)


//                val fileName = "schedule_cache.txt"
//                val fileBody = rawData
//
//                openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
//                    output.write(fileBody.toByteArray())
//                }

                // 4. Update the UI
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Found Schedule: $title", Toast.LENGTH_LONG).show()

                    // Transition to ClassSchedule activity with the scraped data
                    val intent = Intent(this@DataTestingActivity, ClassSchedule::class.java)
                    intent.putExtra("classSchedule", rawData)
                    startActivity(intent)
                }

            } catch (e: Exception) {
                Log.e("MyTag", "Error Scraping: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun saveParsedDataToDb(parsedResult: StudentPrasedResult) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val dao = db.auSparkDao()

            // 1. Convert your Parsed Profile to an Entity
            val profileEntity = StudentEntity(
                studentId = parsedResult.profile.studentId,
                name = parsedResult.profile.name,
                major = parsedResult.profile.major,
                gpa = parsedResult.profile.gpa,
                credits = parsedResult.profile.credits
            )

            // 2. Convert your Parsed Schedule List to Entity List
            val classEntities = parsedResult.schedule.map { item ->
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

            // 3. Save to Database (Clear old data first!)
            dao.saveProfile(profileEntity)

            dao.clearSchedule() // Wipe the old schedule so we don't get duplicates
            dao.insertClasses(classEntities)

            Log.d("MyTag", "Database Updated: Saved ${classEntities.size} classes")

            // 4. Test Reading it back
            val mondaysClasses = dao.getClassesForDay("Monday")
            mondaysClasses.forEach {
                Log.d("MyTag", "Monday Class: ${it.courseName} at ${it.startTime}")
            }
        }
    }

    private fun textTheParser(rawData: String) {
        val rawText = rawData.trimIndent()

        val parser = AuSparkParser()
        val result = parser.parse(rawText)

        saveParsedDataToDb(result)

        // Log the Profile
        println("Student: ${result.profile.name}")
        println("Student: ${result.profile.studentId}")
        println("GPA: ${result.profile.gpa}")

        // Loop through the schedule
        result.schedule.forEach { classItem ->
            println("--- Class Found ---")
            println("Day: ${classItem.day}")
            println("Time: ${classItem.startTime} - ${classItem.endTime}")
            println("Subject: ${classItem.courseCode} : ${classItem.courseName}")
            println("Room: ${classItem.room}")
        }
    }
}