package com.example.btcpricewidget

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    // global variable for all functions to use
    var data = Data()
    private val TAG = "Main Activity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this) {}
        var mAdView: AdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        val priceTv: TextView = findViewById(R.id.main_price_text)
        val changeTv: TextView = findViewById(R.id.main_day_change)

        priceTv.text = getString(R.string.loading_text1)
        changeTv.text = getString(R.string.loading_text1)

        // initial HTTP GET request
        fetchData()

        // when update button gets pressed
        val updateButton: ImageButton = findViewById(R.id.main_refresh_button)
        updateButton.setOnClickListener {
            Log.i(TAG, "Refresh button pressed.")

            // temp changes to show price is loading
            runOnUiThread {
                changeTv.setTextColor(priceTv.currentTextColor)
                priceTv.text = getString(R.string.loading_text1)
                changeTv.text = getString(R.string.loading_text1)
            }
            // make HTTP GET request
            fetchData()
        }
    }

    // function called from background thread to update main_activity layout
    private fun updateLayout(values: Data) {

        val priceTv: TextView = findViewById(R.id.main_price_text)
        val changeTv: TextView = findViewById(R.id.main_day_change)

        runOnUiThread {
            priceTv.text = values.price()
            changeTv.text = values.change24h()

            if (values.change24h().contains('+')) {
                // green color
                changeTv.setTextColor(ContextCompat.getColor(this, R.color.positive_green))
            } else {
                // red color
                changeTv.setTextColor(ContextCompat.getColor(this, R.color.negative_red))
            }

        }
    }

    // HTTP GET request using OkHttp library
    private fun fetchData() {
        val url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&ids=bitcoin"
        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                Log.i(TAG, "GET request successful.")

                // converts response into string
                val body = response.body?.string()

                // extracts object from JSON
                val tempList: Array<Data> = Gson().fromJson(body, Array<Data>::class.java)
                data = tempList[0]
                updateLayout(data)
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.i(TAG, "Failed to execute GET request.")
            }
        })
    }
}