package com.jcoronado.minimalbitcoinwidget.widgets.glance

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jcoronado.minimalbitcoinwidget.workers.PriceUpdateWorker

class PriceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PriceWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d("TestWidgetReceiver", "onEnabled called")
        PriceUpdateWorker.enqueue(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d("TestWidgetReceiver", "onUpdate called")

        PriceUpdateWorker.enqueue(context)

        val workManager = WorkManager.getInstance(context)
        val oneTimeRequest = OneTimeWorkRequestBuilder<PriceUpdateWorker>().build()
        workManager.enqueue(oneTimeRequest)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d("TestWidgetReceiver", "onDisabled called")
        PriceUpdateWorker.cancel(context)
    }
}
