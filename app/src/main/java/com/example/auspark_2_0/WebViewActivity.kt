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

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("MyTag", "Current URL: $url")

                if (url == null) return

                // Check if user has successfully landed on the home page after login
                val atHomePage = url == "https://auspark.au.edu/" || url == "https://auspark.au.edu"

                if (atHomePage) {
                    val cookies = cookieManager.getCookie(url)
                    if (!cookies.isNullOrEmpty()) {
                        Log.d("MyTag", "LOGIN SUCCESS! Starting Background Worker...")

                        val data = Data.Builder()
                            .putString("TARGET_URL", targetDataUrl)
                            .putString("COOKIES", cookies)
                            .build()

                        val scrapeRequest = OneTimeWorkRequestBuilder<ScrapeWorker>()
                            .setInputData(data)
                            .setConstraints(Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build())
                            .build()

                        val workManager = WorkManager.getInstance(applicationContext)

                        workManager.enqueueUniqueWork(
                            "schedule_scraping",
                            ExistingWorkPolicy.REPLACE,
                            scrapeRequest
                        )

                        // Corrected observation of LiveData
                        workManager.getWorkInfoByIdLiveData(scrapeRequest.id)
                            .observe(this@WebViewActivity, Observer { workInfo ->
                                if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                                    Toast.makeText(this@WebViewActivity, "Schedule Sync Complete!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@WebViewActivity, ClassSchedule::class.java)
                                    startActivity(intent)
                                    finish() // Close login activity after success
                                }
                            })
                    }
                }
            }
        }
        webView.loadUrl(homeUrl)
    }
}