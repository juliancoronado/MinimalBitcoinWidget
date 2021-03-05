package com.example.btcpricewidget

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    // global variable for all functions to use
    var data = Data()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        setContentView(R.layout.activity_main)

        val changeTv : TextView = findViewById(R.id.percent24h)
        val priceTv : TextView = findViewById(R.id.current_price)

        priceTv.text = getString(R.string.loading_text1)
        changeTv.text = getString(R.string.loading_text1)

        // initial HTTP GET request
        fetchData()

        // when update button gets pressed
        val updateButton : ImageButton = findViewById(R.id.update_button)
        updateButton.setOnClickListener {
            println("Main Activity: Update button pressed!")

            // temp changes to show price is loading
            runOnUiThread {
                priceTv.text = getString(R.string.loading_text1)
                changeTv.text = getString(R.string.loading_text1)
                changeTv.setTextColor(priceTv.textColors)
            }

            // make HTTP GET request
            fetchData()
        }

    }

    // function called from background thread
    private fun updateLayout(values: Data) {

        val priceTv : TextView = findViewById(R.id.current_price)
        val changeTv : TextView = findViewById(R.id.percent24h)

        runOnUiThread {
            priceTv.text = values.price()
            changeTv.text = values.change24h()

            if (values.change24h().contains('+')) {
                // green color
                changeTv.setTextColor(Color.parseColor("#1f6d00"))
            } else {
                // red color
                changeTv.setTextColor(Color.parseColor("#b50f04"))
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
                println("Main Activity : GET request successful.")

                // converts response into string
                val body = response.body?.string()

                // extracts object from JSON
                val tempList: Array<Data> = Gson().fromJson(body, Array<Data>::class.java)
                data = tempList[0]
                updateLayout(data)
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Main Activity: Failed to execute GET request.")
            }
        })
    }
}