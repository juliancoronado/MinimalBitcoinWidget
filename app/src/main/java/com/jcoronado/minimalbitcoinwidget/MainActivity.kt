package com.jcoronado.minimalbitcoinwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

// val TAG for Log information
private const val TAG = "Main Activity"

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    // var data will hold the information received from the HTTP Request
    var data = Data()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // follow system theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        setContentView(R.layout.activity_main)

        // inflate toolbar
        val mToolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.m_toolbar)
        setSupportActionBar(mToolbar)

        // create TextView objects that contain reference to layout objects
        val priceTv: TextView = findViewById(R.id.main_price_text)
        val changeTv: TextView = findViewById(R.id.main_day_change)
        val symbolTv: TextView = findViewById(R.id.main_symbol)

        // set TextViews text to loading string
        priceTv.text = getString(R.string.loading_text)
        changeTv.text = getString(R.string.loading_text)
        symbolTv.text = ""

        // initial HTTP GET request
        fetchData()

        // when update button gets pressed or activity restarted
        val updateButton: ImageButton = findViewById(R.id.main_refresh_button)
        updateButton.setOnClickListener {
            Log.i(TAG, "Refreshing price layout.")

            // temp changes to show price is loading
            runOnUiThread {
                changeTv.setTextColor(priceTv.currentTextColor)
                priceTv.text = getString(R.string.loading_text)
                changeTv.text = getString(R.string.loading_text)
                symbolTv.text = ""
            }
            // make HTTP GET request
            fetchData()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.version_info -> {
                val intent = Intent(this, InfoActivity::class.java)
                startActivity(intent)
            }

            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }

        return true
    }

    /**
     * Updates the price information layout
     */
    private fun updateLayout(values: Data, symbol: String, isoCode: String) {

        // create TextView objects that contain reference to layout objects
        val priceTv: TextView = findViewById(R.id.main_price_text)
        val changeTv: TextView = findViewById(R.id.main_day_change)
        val isoCodeTv: TextView = findViewById(R.id.main_iso_code)
        val symbolTv: TextView = findViewById(R.id.main_symbol)

        runOnUiThread {

            // update the layout with new data
            priceTv.text = values.priceString()
            changeTv.text = values.dayChangeString()
            isoCodeTv.text = isoCode
            symbolTv.text = symbol

            // check for positive or negative change to set color accordingly
            if (values.dayChangeString() == "0.0%") {
                // no change
                changeTv.setTextColor(priceTv.currentTextColor)
            } else if (values.dayChangeString().contains('+')) {
                // green color
                changeTv.setTextColor(ContextCompat.getColor(this, R.color.positive_green))
            } else {
                // red color
                changeTv.setTextColor(ContextCompat.getColor(this, R.color.negative_red))
            }

        }
    }

    /**
     * HTTP GET request using the OkHttp library
     */
    private fun fetchData() {

        // set up shared preferences
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(this)

        // store current currency selection
        val currency = prefs.getString("currency", "usd")

        var symbol = ""
        var isoCode = ""

        // set symbol and isoCode depending on selected currency
        when (currency) {
            "usd" -> {
                symbol = "$"
                isoCode = "USD"
            }
            "gbp" -> {
                symbol = "£"
                isoCode = "GBP"
            }
            "eur" -> {
                symbol = "€"
                isoCode = "EUR"
            }
            "cad" -> {
                symbol = "$"
                isoCode = "CAD"
            }
            "mxn" -> {
                symbol = "$"
                isoCode = "MXN"
            }
            "aud" -> {
                symbol = "$"
                isoCode = "AUD"
            }
        }

        // build API url string with selected currency
        val url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=$currency&ids=bitcoin"
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
                // update the activity_main layout with the new price data
                updateLayout(data, symbol, isoCode)
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.i(TAG, "Failed to execute GET request.")
            }
        })
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // HTTP GET the new data using new currency selection
        Log.i(TAG, "Currency preference changed.")
        fetchData()

        // update home screen widget
        // build intent to call opUpdate()
        val intent = Intent(this, PriceWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

        val ids: IntArray = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(application, PriceWidget::class.java)
        )

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }
}