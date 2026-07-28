package com.jcoronado.minimalbitcoinwidget.workers

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.preference.PreferenceManager
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.jcoronado.minimalbitcoinwidget.AppDatabase
import com.jcoronado.minimalbitcoinwidget.DebugLog
import com.jcoronado.minimalbitcoinwidget.classes.AppConstants
import com.jcoronado.minimalbitcoinwidget.classes.Prefs
import com.jcoronado.minimalbitcoinwidget.data.PriceRepository
import com.jcoronado.minimalbitcoinwidget.data.Resource
import com.jcoronado.minimalbitcoinwidget.viewmodels.PriceViewModel
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidget
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidgetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

private const val LOG_TAG = "PriceUpdateWorker"

class PriceUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(LOG_TAG, "Worker started (doWork())")

        val db = AppDatabase.getInstance(applicationContext)
        val dao = db.debugDao()

        dao.insert(DebugLog(message = "PriceWorker: Fetching Data"))

        val repository = PriceRepository(applicationContext)

        if (repository.isMockUiEnabled()) {
            dao.insert(DebugLog(message = "PriceWorker: Mock UI enabled, bypassing fetch"))
            PriceViewModel.refreshWidgetsFromCache(applicationContext)
            return@withContext Result.success()
        }

        if (repository.isCacheFresh()) {
            dao.insert(DebugLog(message = "PriceWorker: Skipping Data Fetch (< 15 mins)"))
            Log.d(LOG_TAG, "Data is fresh, skipping fetch")
            PriceViewModel.refreshWidgetsFromCache(applicationContext)
            return@withContext Result.success()
        }

        val currencyCode = repository.getSelectedCurrency()
        val resource = repository.fetchPrice(currencyCode, force = true)

        when (resource) {
            is Resource.Success -> {
                PriceViewModel.refreshWidgetsFromCache(applicationContext)
                dao.insert(DebugLog(message = "PriceWorker: Data Fetched"))
                Result.success()
            }
            is Resource.Error -> {
                val exception = resource.cause as? Exception ?: Exception(resource.message)
                dao.insert(DebugLog(message = "PriceWorker: Failed - ${resource.message}"))
                Log.e(LOG_TAG, "Worker fetch error: ${resource.message}", exception)
                handleError(exception)
            }
            // Included for Kotlin sealed class branch exhaustiveness. fetchPrice() runs to completion on Dispatchers.IO and only returns Success or Error.
            is Resource.Loading -> Result.retry()
        }
    }

    private suspend fun handleError(e: Exception): Result {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(PriceWidget::class.java)
        var lastValid: PriceWidgetState.Available? = null

        if (glanceIds.isNotEmpty()) {
            val currentState: PriceWidgetState = PriceWidget().getAppWidgetState(context, glanceIds.first())
            lastValid = when (currentState) {
                is PriceWidgetState.Available -> currentState
                is PriceWidgetState.Error -> currentState.lastValidState
                else -> null
            }
        }

        // update glance widgets to error state
        PriceViewModel.updateGlanceWidgets(context, PriceWidgetState.Error(e.message ?: "Unknown Error", lastValid))
        return Result.retry()
    }

    companion object {
        private const val WORK_NAME = "PriceUpdateWork"

        fun enqueue(context: Context, intervalMinutes: Long? = null) {
            val minutesToUse = intervalMinutes ?: run {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val intervalIndex = prefs.getInt(Prefs.REFRESH_INTERVAL, 1)
                when (intervalIndex) {
                    0 -> 60L
                    1 -> 240L
                    2 -> 480L
                    else -> 240L
                }
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<PriceUpdateWorker>(minutesToUse, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val glanceManager = GlanceAppWidgetManager(context)
                val glanceIds = glanceManager.getGlanceIds(PriceWidget::class.java)

                val appWidgetManager = AppWidgetManager.getInstance(context)
                val legacyIds = appWidgetManager.getAppWidgetIds(ComponentName(context, PriceViewModel.LEGACY_WIDGET_WRAPPER_CLASS))

                if (glanceIds.isEmpty() && legacyIds.isEmpty()) {
                    Log.d(LOG_TAG, "No widgets left. Cancelling work.")
                    WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
                }
            }
        }
    }
}
