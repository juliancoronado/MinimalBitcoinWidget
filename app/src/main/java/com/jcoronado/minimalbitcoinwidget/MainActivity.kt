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
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
import java.text.NumberFormat
import java.util.concurrent.TimeUnit

// val TAG for Log information
private const val TAG = "Main Activity"

data class CurrencyInfo(val symbol: String, val isoCode: String)

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {

        /**
         * Returns currency symbol and ISO code based on the provided currency string.
         * Defaults to USD if the currency is not recognized.
         *
         * @param currency The currency code string (e.g., "usd", "gbp").
         * @return A [CurrencyInfo] object containing the symbol and ISO code.
         */
        fun getCurrencyInfo(currency: String?): CurrencyInfo {
            return when (currency) {
                "usd" -> CurrencyInfo("$", "USD")
                "gbp" -> CurrencyInfo("£", "GBP")
                "eur" -> CurrencyInfo("€", "EUR")
                "cad" -> CurrencyInfo("$", "CAD")
                "mxn" -> CurrencyInfo("$", "MXN")
                "aud" -> CurrencyInfo("$", "AUD")
                "brl" -> CurrencyInfo("R$", "BRL")
                else -> CurrencyInfo("$", "USD") // default to USD
            }
        }
    }

    private lateinit var loadingIndicator: CircularProgressIndicator
    private lateinit var priceContainer: LinearLayout
    private lateinit var priceCard: MaterialCardView
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

        priceCard.setOnClickListener {
            Log.d(TAG, "Card tapped.")
            // make HTTP GET request
            fetchData()
        }

        releaseNotesButton.setOnClickListener {
            val header = getString(R.string.version_only_main) + " - " + getString(R.string.release_notes_date)
            val dialog =
                MaterialAlertDialogBuilder(this).setTitle(getString(R.string.release_notes_title))
                    .setMessage(header + "\n" + getString(R.string.release_notes_summary))
                    .setNegativeButton(getString(R.string.close)) { dialog, _ ->
                        dialog.dismiss()
                    }.show()
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.blue_500))
            val messageView = dialog.findViewById<TextView>(android.R.id.message)
            messageView?.let {
                it.typeface = ResourcesCompat.getFont(this, R.font.manrope_regular)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchData()
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
     * Shows the loading indicator and hides the price content container.
     *
     * This utility function is called at the beginning of a data fetch operation to provide
     * immediate visual feedback to the user that content is being loaded. It ensures the
     * visibility changes occur on the main thread.
     */
    private fun showLoading() {
        runOnUiThread {
            loadingIndicator.visibility = View.VISIBLE
            priceContainer.visibility = View.GONE
        }
    }

    /**
     * Hides the loading indicator and shows the price content container.
     *
     * This utility function is called after data has been successfully fetched and the UI is
     * ready to be displayed. It ensures the visibility changes occur on the main thread.
     */
    private fun hideLoading() {
        runOnUiThread {
            loadingIndicator.visibility = View.GONE
            priceContainer.visibility = View.VISIBLE
        }
    }

    /**
     * Updates the price information layout
     */
    private fun updateLayoutValues(priceData: PriceData, currencyInfo: CurrencyInfo) {
        val symbol = currencyInfo.symbol
        val isoCode = currencyInfo.isoCode

        // create TextView objects that contain reference to layout objects
        val priceTv: TextView = findViewById(R.id.main_price_text)
        val changeTv: TextView = findViewById(R.id.main_day_change)
        val isoCodeTv: TextView = findViewById(R.id.main_iso_code)
        val symbolTv: TextView = findViewById(R.id.main_symbol)

        runOnUiThread {
            // update the layout with new data
            val numberFormatter = NumberFormat.getNumberInstance().apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
            priceTv.text = numberFormatter.format(priceData.currentPrice)

            val percentFormatter = NumberFormat.getPercentInstance().apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
            changeTv.text = percentFormatter.format(priceData.priceChangePercentage24h / 100)

            isoCodeTv.text = isoCode
            symbolTv.text = symbol

            if (priceData.priceChangePercentage24h == 0.0) {
                // no change
                changeTv.setTextColor(priceTv.currentTextColor)
            } else if (priceData.priceChangePercentage24h < 0) {
                // negative
                changeTv.setTextColor(ContextCompat.getColor(this, R.color.negative_red))
            } else {
                // positive
                changeTv.setTextColor(ContextCompat.getColor(this, R.color.positive_green))
            }
        }
    }

    /**
     * Main data-fetching logic for the activity.
     *
     * This function acts as a router. It first checks if a recent, valid price is available in the cache.
     * If a valid cache exists, it loads the data from SharedPreferences. Otherwise, it delegates
     * to [fetchFromNetwork] to retrieve fresh data.
     *
     * @param force A boolean flag to bypass the cache check and force a network request.
     *              Defaults to false.
     */

    private fun fetchData(force: Boolean = false) {
        showLoading()

        if (force) Log.d(TAG, "Force refresh: true")

        // set up shared preferences
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.registerOnSharedPreferenceChangeListener(this)
        var lastApiCallTime = prefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, 0L)
        if (force) {
            lastApiCallTime = 0L
        }
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastApiCallTime < AppConstants.CACHE_DURATION_MILLIS) {
            // still within the cache duration - check shared prefs for cached data
            val cachedDataJson = prefs.getString(Prefs.CACHED_PRICE_DATA, null)
            Log.d(TAG, "Cached Data: $cachedDataJson")
            if (cachedDataJson != null) {
                // cachedData is not null
                Log.d(TAG, "Loading data from cache.")
                val currency = prefs.getString(Prefs.SELECTED_CURRENCY, Prefs.CURRENCY_DEFAULT)
                // use the cached data instead of making a network call
                val cachedData: PriceData = Gson().fromJson(cachedDataJson, PriceData::class.java)
                val currencyInfo = getCurrencyInfo(currency)
                updateLayoutValues(cachedData, currencyInfo)
                CoroutineScope(Dispatchers.Main).launch {
                    delay(500) // short delay
                    hideLoading()
                }
                return // stop here on valid cache
            }
        }

        Log.d(TAG, "Cache is empty or old. Fetching from network.")
        fetchFromNetwork(prefs)
    }

    /**
     * Performs a live network request to the CoinGecko API to get the latest Bitcoin price.
     *
     * This function builds and executes an asynchronous OkHttp GET request. On a successful
     * response, it parses the JSON, updates the UI via [updateLayoutValues], caches the new
     * data and timestamp in SharedPreferences, and triggers a widget update. It also handles
     * various failure scenarios, such as network errors or unsuccessful HTTP responses
     * (e.g., rate limiting), providing user feedback via toasts.
     *
     * @param prefs The application's default SharedPreferences instance, used to get the
     *              currently selected currency and to store the new data upon success.
     */
    private fun fetchFromNetwork(prefs: SharedPreferences) {
        val currency = prefs.getString(Prefs.SELECTED_CURRENCY, Prefs.CURRENCY_DEFAULT)
        val currencyInfo = getCurrencyInfo(currency)
        val url = Api.COINGECKO_API_URL + currency
        val request = Request.Builder().url(url).build()

        val client = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.i(TAG, "Successful GET request: ${response.code}")
                    val body = response.body.string()
                    Log.d(TAG, "Body: $body")
                    try {
                        val gson = Gson()
                        val type = object : TypeToken<Map<String, Map<String, Double>>>() {}.type
                        // Map<"bitcoin", Map<"$currency", Value>>
                        val data: Map<String, Map<String, Double>> = gson.fromJson(body, type)

                        val priceDataJson = data["bitcoin"] ?: return

                        val price = priceDataJson[currency] ?: 0.0
                        val change24h = priceDataJson["${currency}_24h_change"] ?: 0.0
                        val priceData = PriceData(price, change24h)

                        // store the new data
                        prefs.edit {
                            putLong(Prefs.LAST_API_CALL_TIMESTAMP, System.currentTimeMillis())
                            putString(Prefs.CACHED_PRICE_DATA, gson.toJson(priceData))
                            apply()
                        }

                        // launch a coroutine to introduce a short delay before updating the UI
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(750)
                            hideLoading()
                            // update the activity_main layout with the new price data
                            updateLayoutValues(
                                priceData, currencyInfo
                            )
                            redrawWidgets()
                        }

                    } catch (e: Exception) {
                        // handle JSON parsing error
                        Log.e(TAG, "Failed to parse JSON", e)
                        hideLoading()
                    }
                } else {
                    // failed GET request
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(500) // short delay
                        hideLoading()
                    }
                    Log.w(TAG, "Unsuccessful GET request: ${response.code}")
                    Log.w(TAG, "Error Body: ${response.body.string()}")

                    if (response.code == 429) {
                        // show message on 429 response
                        runOnUiThread {
                            Toast.makeText(
                                this@MainActivity,
                                getString(R.string.rate_limit_toast),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // all other responses
                        runOnUiThread {
                            Toast.makeText(
                                this@MainActivity,
                                getString(R.string.failure_toast),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    return
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // launch a coroutine to introduce a short delay before updating the UI
                CoroutineScope(Dispatchers.Main).launch {
                    delay(500)
                    hideLoading()
                    Toast.makeText(
                        this@MainActivity, getString(R.string.failure_toast), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    /**
     * Called when a shared preference is changed, added, or removed.
     *
     * This implementation listens specifically for changes to the `SELECTED_CURRENCY` preference.
     * If a change is detected, it triggers a forced data refresh by calling [fetchData] to
     * immediately reflect the new currency selection in the UI.
     *
     * @param sharedPreferences The SharedPreferences that received the change.
     * @param key The key of the preference that was changed.
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // only fetch new data when the "currency" preference is changed
        if (key != Prefs.SELECTED_CURRENCY) return

        fetchData(force = true)
    }

    /**
     * Triggers a manual update for all placed instances of [PriceWidget].
     *
     * This method constructs and sends a broadcast Intent with the `ACTION_APPWIDGET_UPDATE`
     * action. This effectively simulates a system-initiated widget update, causing the `onUpdate`
     * method in the [PriceWidget] provider to be called for all active widget IDs. It is
     * used to ensure the home screen widgets are synchronized with the latest data fetched
     * by the main application.
     */
    private fun redrawWidgets() {
        // build intent to call onUpdate() for widgets
        val intent = Intent(this, PriceWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

        val ids: IntArray = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(application, PriceWidget::class.java)
        )

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }
}