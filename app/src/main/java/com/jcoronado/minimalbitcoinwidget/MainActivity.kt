package com.jcoronado.minimalbitcoinwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

// val TAG for Log information
private const val TAG = "Main Activity"

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    // var priceData will hold the information received from the HTTP Request
    var priceData = Data()
    private lateinit var loadingIndicator: CircularProgressIndicator
    private lateinit var priceContainer: LinearLayout
    private lateinit var priceCard : MaterialCardView
    private lateinit var releaseNotesButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // follow system theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        setContentView(R.layout.activity_main)

        // inflate toolbar
        val mToolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.m_toolbar)
        setSupportActionBar(mToolbar)

        loadingIndicator = findViewById(R.id.loading_indicator)
        priceContainer = findViewById(R.id.price_container)
        releaseNotesButton = findViewById(R.id.release_notes_button)
        priceCard = findViewById(R.id.price_card)

        // initial HTTP GET request
        fetchData()

        // when card is tapped or activity restarted
        priceCard.setOnClickListener {
            Log.i(TAG, "Refreshing price layout.")
            // make HTTP GET request
            fetchData()
        }

        releaseNotesButton.setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.release_notes_title))
                .setMessage(getString(R.string.release_notes_summary))
                .setNegativeButton(getString(R.string.close)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.blue_500))
            val messageView = dialog.findViewById<TextView>(android.R.id.message)
            messageView?.let {
                it.typeface = ResourcesCompat.getFont(this, R.font.manrope_regular)
            }
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

    private fun showLoading() {
        runOnUiThread {
            loadingIndicator.visibility = View.VISIBLE
            priceContainer.visibility = View.GONE
        }
    }

    private fun hideLoading() {
        runOnUiThread {
            loadingIndicator.visibility = View.GONE
            priceContainer.visibility = View.VISIBLE
        }
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
        showLoading()

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
            "brl" -> {
                symbol = "R$"
                isoCode = "BRL"
            }
        }

        // build API url string with selected currency
        val url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=$currency&ids=bitcoin"
        val request = Request.Builder().url(url).build()

        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    // failed GET request
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(500) // short delay
                        hideLoading()
                    }
                    Log.e(TAG, "Unsuccessful GET request: ${response.code}")
                    Log.e(TAG, "Error Body: ${response.body?.string()}")

                    if (response.code == 429) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, getString(R.string.rate_limit_toast), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, getString(R.string.failure_toast), Toast.LENGTH_SHORT).show()
                        }
                    }
                    // pass in "error" data to display - not sure if this is needed anymore?
                    // val errorData = Data(price_change_24h = 0.0f, current_price = 0.0)
                    // updateLayout(errorData, symbol, isoCode)
                    return
                }

                Log.i(TAG, "Successful GET request: ${response.code}")

                // converts response into string
                val body = response.body?.string()

                Log.d(TAG, "Body: $body")

                try {
                    // extracts object from JSON
                    val tempList: Array<Data> = Gson().fromJson(body, Array<Data>::class.java)
                    priceData = tempList[0]
                    // launch a coroutine to introduce a short delay before updating the UI
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(750)
                        hideLoading()
                        // update the activity_main layout with the new price data
                        updateLayout(priceData, symbol, isoCode)
                    }
                } catch (e: Exception) {
                    // catch potential JSON parsing errors
                    Log.e(TAG, "Failed to parse JSON", e)
                    hideLoading()
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.i(TAG, "Failed to execute GET request.")
                hideLoading()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Request timed out", Toast.LENGTH_SHORT).show()
                }
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
