package com.jcoronado.minimalbitcoinwidget.minimalbitcoinwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

// New import.
import es.antonborri.home_widget.HomeWidgetPlugin

/**
 * Implementation of App Widget functionality.
 */
class NewsWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            // Get reference to SharedPreferences
            val widgetData = HomeWidgetPlugin.getData(context)
            val views = RemoteViews(context.packageName, R.layout.news_widget).apply {

                val title = widgetData.getString("title", null)
                setTextViewText(R.id.title, title ?: "No title set")

                val description = widgetData.getString("description", null)
                setTextViewText(R.id.description, description ?: "No description set")
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}