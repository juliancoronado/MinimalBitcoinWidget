package com.jcoronado.minimalbitcoinwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jcoronado.minimalbitcoinwidget.MainActivity.Companion.getCurrencyInfo
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.text.NumberFormat

/**
 * Implementation of App Widget functionality.
 */

private const val TAG = "Price Widget"

class PriceWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        // update all available widgets
        for (appWidgetId in appWidgetIds) {
            Log.d(TAG, "Updating widget with ID: $appWidgetId")
            drawWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            Log.d(TAG, "Deleted widget with ID: $appWidgetId")
        }
    }
}

internal fun drawWidget(
    context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int
) {
    // create remote view
    val views = RemoteViews(context.packageName, R.layout.price_widget)

    // set widgets to display loading state
    setWidgetViews(context, views, null, CurrencyInfo("", ""), loading = true)

    // refresh widget UI
    appWidgetManager.updateAppWidget(appWidgetId, views) // continues after this

    // refresh button logic
    val intentUpdate = Intent(context, PriceWidget::class.java)
    intentUpdate.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

    val idArray = intArrayOf(appWidgetId)
    intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray)

    // set up pending intent
    val pendingUpdate = PendingIntent.getBroadcast(
        context, appWidgetId, intentUpdate, PendingIntent.FLAG_IMMUTABLE
    )

    views.setOnClickPendingIntent(R.id.widget_refresh_button, pendingUpdate)

    // function call to fetch data from cache OR HTTP GET request
    refreshData(appWidgetManager, appWidgetId, views, context)
}

fun showDelay(millis: Long) {
    try {
        Thread.sleep(millis)
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
}

fun refreshData(
    appWidgetManager: AppWidgetManager, appWidgetId: Int, views: RemoteViews, context: Context
) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val lastApiCallTime = prefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, 0L)
    val currentTime = System.currentTimeMillis()

    if (currentTime - lastApiCallTime < AppConstants.CACHE_DURATION_MILLIS) {
        Log.i(TAG, "Use cached data instead of making network call.")
        val cachedDataJson = prefs.getString(Prefs.CACHED_PRICE_DATA, null)
        if (cachedDataJson == null) {
            Log.w(TAG, "Cache was null. Fetching fresh data from network.")
            // cache is null, fetch from network
            fetchFromNetwork(appWidgetManager, appWidgetId, views, context, prefs)
            return
        }

        val gson = Gson()
        val cachedPriceData: PriceData = gson.fromJson(cachedDataJson, PriceData::class.java)
        val currency = prefs.getString(Prefs.SELECTED_CURRENCY, Prefs.CURRENCY_DEFAULT)
        val currencyInfo = getCurrencyInfo(currency)
        // set widget views with cached data
        setWidgetViews(context, views, cachedPriceData, currencyInfo, loading = false)
        showDelay(500)
        // update widget UI
        appWidgetManager.updateAppWidget(appWidgetId, views)
        return
    } else {
        Log.i(TAG, "Cache was stale. Fetching fresh data from network.")
        fetchFromNetwork(appWidgetManager, appWidgetId, views, context, prefs)
        return
    }
}

fun fetchFromNetwork(
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    views: RemoteViews,
    context: Context,
    prefs: SharedPreferences
) {
    Log.d(TAG, "Fetching from network.")
    val currency = prefs.getString(Prefs.SELECTED_CURRENCY, Prefs.CURRENCY_DEFAULT)
    // current CoinGecko url to send GET request
    val url = Api.COINGECKO_API_URL + currency
    val currencyInfo = getCurrencyInfo(currency)

    // OkHttp
    val request = Request.Builder().url(url).build()

    val client = OkHttpClient()

    client.newCall(request).enqueue(object : Callback {
        val gson = Gson()
        override fun onResponse(call: Call, response: Response) {
            if (!response.isSuccessful) {
                // unsuccessful GET request
                Log.w(TAG, "Unsuccessful GET request: ${response.code}")
                showDelay(300)
                // if get request fails, show cached data
                val cachedDataJson = prefs.getString(Prefs.CACHED_PRICE_DATA, null)

                if (cachedDataJson == null) {
                    // if no cached data, show zeros as default values
                    setWidgetViews(context, views, PriceData(currentPrice = 0.0, priceChangePercentage24h = 0.0), currencyInfo, loading = false)
                } else {
                    val cachedPriceData: PriceData = gson.fromJson(cachedDataJson, PriceData::class.java)
                    setWidgetViews(context, views, cachedPriceData, currencyInfo, loading = false)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            // successful GET request
            Log.i(TAG, "GET request successful: ${response.code}")

            // converts response into string
            val body = response.body.string()

            // extracts data from JSON
            val type = object : TypeToken<Map<String, Map<String, Double>>>() {}.type
            // Map<"bitcoin", Map<"$currency", Value>>
            val data: Map<String, Map<String, Double>> = gson.fromJson(body, type)

            val priceDataJson = data["bitcoin"] ?: return

            val price = priceDataJson[currency] ?: 0.0
            val change24h = priceDataJson["${currency}_24h_change"] ?: 0.0
            val priceData = PriceData(price, change24h)

            prefs.edit {
                putLong(Prefs.LAST_API_CALL_TIMESTAMP, System.currentTimeMillis())
                putString(Prefs.CACHED_PRICE_DATA, gson.toJson(priceData))
                apply()
            }

            setWidgetViews(context, views, priceData, currencyInfo, loading = false)

            showDelay(500)

            // makes final call to update the widget with new data
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        override fun onFailure(call: Call, e: IOException) {
            // failed GET request
            Log.w(TAG, "Failed to execute GET request.")
            showDelay(300)
            // if request fails, use cached data
            val cachedDataJson = prefs.getString(Prefs.CACHED_PRICE_DATA, null)
            if (cachedDataJson == null) {
                // if no cached data, show zeros as default values
                setWidgetViews(context, views, PriceData(currentPrice = 0.0, priceChangePercentage24h = 0.0), currencyInfo, loading = false)
            } else {
                val cachedPriceData: PriceData = gson.fromJson(cachedDataJson, PriceData::class.java)
                setWidgetViews(context, views, cachedPriceData, currencyInfo, loading = false)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
            return
        }
    })
}

// PriceData being null and loading are mutually exclusive
fun setWidgetViews(
    context: Context,
    views: RemoteViews,
    priceData: PriceData?,
    currencyInfo: CurrencyInfo,
    loading: Boolean
) {
    if (loading) {
        // loading state
        views.setViewVisibility(R.id.widget_progress_bar, View.VISIBLE)
        views.setViewVisibility(R.id.widget_text_price, View.GONE)
        views.setViewVisibility(R.id.widget_symbol, View.GONE)
        views.setTextViewText(R.id.widget_day_change, "")
    } else {
        // data loaded state
        views.setViewVisibility(R.id.widget_progress_bar, View.GONE)
        views.setViewVisibility(R.id.widget_text_price, View.VISIBLE)
        views.setViewVisibility(R.id.widget_symbol, View.VISIBLE)

        val numberFormatter = NumberFormat.getNumberInstance().apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

        val percentFormatter = NumberFormat.getPercentInstance().apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

        views.setTextViewText(R.id.widget_text_price, numberFormatter.format(priceData!!.currentPrice))
        views.setTextViewText(
            R.id.widget_day_change, percentFormatter.format(priceData.priceChangePercentage24h / 100)
        )
        views.setTextViewText(R.id.widget_iso_code, currencyInfo.isoCode)
        views.setTextViewText(R.id.widget_symbol, currencyInfo.symbol)

        // determine the specific color for the day change view (red or green)
        val isPositive = priceData.priceChangePercentage24h >= 0
        val dayChangeColor = if (isPositive) {
            ContextCompat.getColor(context, R.color.positive_green)
        } else {
            ContextCompat.getColor(context, R.color.negative_red)
        }
        // apply to widget_day_change textView
        views.setTextColor(R.id.widget_day_change, dayChangeColor)
    }
}
