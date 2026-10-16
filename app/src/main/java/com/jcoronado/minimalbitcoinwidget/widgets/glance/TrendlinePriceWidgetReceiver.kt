package com.jcoronado.minimalbitcoinwidget.widgets.glance

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jcoronado.minimalbitcoinwidget.workers.PriceUpdateWorker

class TrendlinePriceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TrendlinePriceWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d("TrendlineWidgetReceiver", "onEnabled called")
        PriceUpdateWorker.enqueue(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d("TrendlineWidgetReceiver", "onUpdate called")

        PriceUpdateWorker.enqueue(context)

        val workManager = WorkManager.getInstance(context)
        val oneTimeRequest = OneTimeWorkRequestBuilder<PriceUpdateWorker>().build()
        workManager.enqueue(oneTimeRequest)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d("TrendlineWidgetReceiver", "onDisabled called")
        PriceUpdateWorker.cancel(context)
    }
}
