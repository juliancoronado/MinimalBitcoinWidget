package com.jcoronado.minimalbitcoinwidget.widgets.legacy

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.classes.Api
import com.jcoronado.minimalbitcoinwidget.classes.AppConstants
import com.jcoronado.minimalbitcoinwidget.classes.Prefs
import com.jcoronado.minimalbitcoinwidget.classes.PriceData
import com.jcoronado.minimalbitcoinwidget.utils.TimeInterval
import com.jcoronado.minimalbitcoinwidget.workers.PriceUpdateWorker
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

private const val TAG = "Legacy Price Widget"

fun getCurrencyInfo(currency: String?): CurrencyInfo {
    return when (currency?.lowercase()) {
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

data class CurrencyInfo(val symbol: String, val isoCode: String)

open class LegacyPriceWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray
    ) {
        // start periodic work
        PriceUpdateWorker.enqueue(context)

        // trigger immediate one time update
        val workManager = WorkManager.getInstance(context)
        val oneTimeRequest = OneTimeWorkRequestBuilder<PriceUpdateWorker>().build()
        workManager.enqueue(oneTimeRequest)

        // update all available widgets
        for (appWidgetId in appWidgetIds) {
            Log.d(TAG, "Updating widget with ID: $appWidgetId")
            drawWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "onEnabled called")
        PriceUpdateWorker.enqueue(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "onDisabled called")
        PriceUpdateWorker.cancel(context)
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
    val views = RemoteViews(context.packageName, R.layout.legacy_price_widget)

    // set widgets to display loading state
    setWidgetViews(context, views, null, CurrencyInfo("", ""), loading = true)

    // refresh widget UI
    appWidgetManager.updateAppWidget(appWidgetId, views) // continues after this

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
        val currency = prefs.getString(Prefs.SELECTED_CURRENCY, AppConstants.CURRENCY_DEFAULT)
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
    val currency = prefs.getString(Prefs.SELECTED_CURRENCY, AppConstants.CURRENCY_DEFAULT)
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
                    setWidgetViews(
                        context, views, PriceData(
                            currentPrice = 0.0, priceChangePercentage24h = 0.0,
                            priceChangePercentage7d = 0.0,
                            priceChangePercentage30d = 0.0,
                        ), currencyInfo, loading = false
                    )
                } else {
                    val cachedPriceData: PriceData =
                        gson.fromJson(cachedDataJson, PriceData::class.java)
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
            val type = object : TypeToken<List<PriceData>>() {}.type
            val data: List<PriceData> = gson.fromJson(body, type)

            if (data.isEmpty()) return
            val priceData = data[0]

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
                setWidgetViews(
                    context, views, PriceData(
                        currentPrice = 0.0,
                        priceChangePercentage24h = 0.0,
                        priceChangePercentage7d = 0.0,
                        priceChangePercentage30d = 0.0
                    ), currencyInfo, loading = false
                )
            } else {
                val cachedPriceData: PriceData =
                    gson.fromJson(cachedDataJson, PriceData::class.java)
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
        views.setTextViewText(R.id.widget_change_label, "")
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

        views.setTextViewText(
            R.id.widget_text_price, numberFormatter.format(priceData!!.currentPrice)
        )

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val selectedInterval = prefs.getInt(Prefs.SELECTED_CHANGE_PERCENTAGE, 0)
        
        val percentage = when (selectedInterval) {
            0 -> priceData.priceChangePercentage24h
            1 -> priceData.priceChangePercentage7d
            2 -> priceData.priceChangePercentage30d
            else -> priceData.priceChangePercentage24h
        }

        val interval = TimeInterval.fromValue(selectedInterval)

        views.setTextViewText(
            R.id.widget_day_change,
            percentFormatter.format(percentage / 100)
        )
        
        views.setTextViewText(R.id.widget_change_label, context.getString(interval.labelResId))

        views.setTextViewText(R.id.widget_iso_code, currencyInfo.isoCode)
        views.setTextViewText(R.id.widget_symbol, currencyInfo.symbol)

        // determine the specific color for the day change view (red or green)
        val isPositive = percentage >= 0
        val dayChangeColor = if (isPositive) {
            ContextCompat.getColor(context, R.color.positive_green)
        } else {
            ContextCompat.getColor(context, R.color.negative_red)
        }
        // apply to widget_day_change textView
        views.setTextColor(R.id.widget_day_change, dayChangeColor)
    }
}
