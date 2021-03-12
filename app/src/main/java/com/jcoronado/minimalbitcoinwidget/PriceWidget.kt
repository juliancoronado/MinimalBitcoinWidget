package com.jcoronado.minimalbitcoinwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

/**
 * Implementation of App Widget functionality.
 */

// var data will hold the information received from the HTTP Request
var data = Data()
// val TAG for Log.i() calls
private val TAG = "Widget"

class PriceWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {

    // create remote view
    val views = RemoteViews(context.packageName, R.layout.price_widget)

    // set textview's text to loading string
    views.setTextViewText(R.id.widget_text_price, "Loading...")
    views.setTextViewText(R.id.widget_day_change, "Loading...")

    // line 52 does not work
    // views.setTextColor(R.id.widget_day_change, R.attr.appWidgetTextColor)

    // first update call to set loading text
    appWidgetManager.updateAppWidget(appWidgetId, views) // continues after this

    // refresh button
    val intentUpdate = Intent(context, PriceWidget::class.java)
    intentUpdate.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

    val idArray = intArrayOf(appWidgetId)
    intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray)

    val pendingUpdate = PendingIntent.getBroadcast(
        context,
        appWidgetId,
        intentUpdate,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    Log.i(TAG, "Refresh button pressed.")
    views.setOnClickPendingIntent(R.id.widget_refresh_button, pendingUpdate)

    // function call to fetch data from HTTP GET request
    fetchData(appWidgetManager, appWidgetId, views, context)
}

fun fetchData(
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    views: RemoteViews,
    context: Context
) {

    // current CoinGecko url to send GET request
    val url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&ids=bitcoin"

    // OkHttp
    val request = Request.Builder().url(url).build()

    val client = OkHttpClient()

    client.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            // successful GET request
            Log.i(TAG, "GET request successful.")

            // converts response into string
            val body = response.body?.string()

            // extracts object from JSON
            val tempList: Array<Data> = Gson().fromJson(body, Array<Data>::class.java)
            data = tempList[0]

            // post execute here
            // update widget with new data
            views.setTextViewText(R.id.widget_text_price, data.price())
            views.setTextViewText(R.id.widget_day_change, data.change24h())

            if (data.change24h().contains('+')) {
                // green color
                views.setTextColor(
                    R.id.widget_day_change,
                    ContextCompat.getColor(context, R.color.positive_green)
                )
            } else {
                // red color
                views.setTextColor(
                    R.id.widget_day_change,
                    ContextCompat.getColor(context, R.color.negative_red)
                )
            }

            // makes final call to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)

        }

        override fun onFailure(call: Call, e: IOException) {
            // failed GET request
            Log.i(TAG, "Failed to execute GET request.")
        }
    })
}