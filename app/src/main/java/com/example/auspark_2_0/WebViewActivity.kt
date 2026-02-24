package com.example.auspark_2_0

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlin.math.log

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_testing)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val webView = findViewById<WebView>(R.id.webView)
        val homeUrl = "https://auspark.au.edu/"
        val targetDataUrl = "https://auspark.au.edu/ClassSchedule"
        val target2DataUrl = "https://auspark.au.edu/Examination"

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

//        webView.webViewClient = object : WebViewClient() {
//            override fun onPageFinished(view: WebView?, url: String?) {
//                super.onPageFinished(view, url)
//                Log.d("MyTag", "Current URL: $url")
//
//                if (url == null) return
//
//                // Check if user has successfully landed on the home page after login
//                val atHomePage = url == "https://auspark.au.edu/" || url == "https://auspark.au.edu"
//
//                // Inside WebViewActivity.kt -> onPageFinished
//                if (atHomePage) {
//                    val cookies = cookieManager.getCookie(url)
//                    if (!cookies.isNullOrEmpty()) {
//                        Log.d("MyTag", "LOGIN SUCCESS! Starting Background Sync...")
//
//                        // 1. Prepare data for the worker
//                        val data = Data.Builder()
//                            .putString("TARGET_URL", targetDataUrl)
//                            .putString("TARGET_URL_EXAM", target2DataUrl)
//                            .putString("COOKIES", cookies)
//                            .build()
//
//                        // 2. Build and Enqueue the WorkRequest
//                        val scrapeRequest = OneTimeWorkRequestBuilder<ScrapeWorker>()
//                            .setInputData(data)
//                            .setConstraints(Constraints.Builder()
//                                .setRequiredNetworkType(NetworkType.CONNECTED)
//                                .build())
//                            .build()
//
//                        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
//                            "au_sync",
//                            ExistingWorkPolicy.REPLACE,
//                            scrapeRequest
//                        )
//
//                        // 3. FIX: Don't wait here. Tell the user we are syncing and go to Schedule
//                        Toast.makeText(this, "Syncing data in background...", Toast.LENGTH_SHORT).show()
//                        val intent = Intent(this, Page_Schedule::class.java)
//                        startActivity(intent)
//                        finish() // This closes the WebView so the user never sees a white page
//                    }
//                }
//
////                if (atHomePage) {
////                    val cookies = cookieManager.getCookie(url)
////                    if (!cookies.isNullOrEmpty()) {
////                        Log.d("MyTag", "LOGIN SUCCESS! Starting Background Worker...")
////
////                        val data = Data.Builder()
////                            .putString("TARGET_URL", targetDataUrl)
////                            .putString("TARGET_URL_EXAM", target2DataUrl)
////                            .putString("COOKIES", cookies)
////                            .build()
////
////                        val scrapeRequest = OneTimeWorkRequestBuilder<ScrapeWorker>()
////                            .setInputData(data)
////                            .setConstraints(Constraints.Builder()
////                                .setRequiredNetworkType(NetworkType.CONNECTED)
////                                .build())
////                            .build()
////
////                        val workManager = WorkManager.getInstance(applicationContext)
////
////                        workManager.enqueueUniqueWork(
////                            "schedule_scraping",
////                            ExistingWorkPolicy.REPLACE,
////                            scrapeRequest
////                        )
////
////                        // Corrected observation of LiveData
////                        workManager.getWorkInfoByIdLiveData(scrapeRequest.id)
////                            .observe(this@WebViewActivity, Observer { workInfo ->
////                                if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
////                                    Toast.makeText(this@WebViewActivity, "Schedule Sync Complete!", Toast.LENGTH_SHORT).show()
////                                    val intent = Intent(this@WebViewActivity, ClassSchedule::class.java)
////                                    startActivity(intent)
////                                    finish() // Close login activity after success
////                                }
////                            })
////                    }
////                }
//            }
//        }
//

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                if (url == null) return
                val atHomePage = url == "https://auspark.au.edu/" || url == "https://auspark.au.edu"

                if (atHomePage) {
                    val cookies = CookieManager.getInstance().getCookie(url)
                    if (!cookies.isNullOrEmpty()) {
                        Log.d("MyTag", "LOGIN SUCCESS! Background Sync Triggered.")

                        // 1. Prepare data for the worker
                        val data = Data.Builder()
                            .putString("TARGET_URL", targetDataUrl)
                            .putString("TARGET_URL_EXAM", target2DataUrl)
                            .putString("COOKIES", cookies)
                            .build()

                        // 2. Build the WorkRequest
                        val scrapeRequest = OneTimeWorkRequestBuilder<ScrapeWorker>()
                            .setInputData(data)
                            .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build())
                            .build()

                        // 3. Enqueue using KEEP to ensure it only runs once per login session
                        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                            "au_sync",
                            ExistingWorkPolicy.KEEP, // KEEP prevents re-triggering if the user stays on homeUrl
                            scrapeRequest
                        )

                        // 4. Stay on the page and just notify the user
                        Toast.makeText(this@WebViewActivity, "Data extraction started in background...", Toast.LENGTH_LONG).show()

                        // Note: We removed 'finish()' and 'startActivity()' to stay on the current URL
                    }
                }
            }
        }

        webView.loadUrl(homeUrl)

        // Observe the sync status while the user stays on the page
        WorkManager.getInstance(this).getWorkInfosForUniqueWorkLiveData("au_sync")
            .observe(this) { workInfos ->
                if (workInfos.isNullOrEmpty()) return@observe

                val status = workInfos[0].state
                if (status == WorkInfo.State.SUCCEEDED) {
                    // Data has been saved to Room!
                    Toast.makeText(this@WebViewActivity, "Sync Complete! You can now view your schedule.", Toast.LENGTH_SHORT).show()
                } else if (status == WorkInfo.State.FAILED) {
                    Toast.makeText(this@WebViewActivity, "Sync failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}