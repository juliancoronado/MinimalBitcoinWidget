package com.jcoronado.minimalbitcoinwidget.workers

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.edit
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jcoronado.minimalbitcoinwidget.AppDatabase
import com.jcoronado.minimalbitcoinwidget.DebugLog
import com.jcoronado.minimalbitcoinwidget.classes.Api
import com.jcoronado.minimalbitcoinwidget.classes.AppConstants
import com.jcoronado.minimalbitcoinwidget.classes.Prefs
import com.jcoronado.minimalbitcoinwidget.classes.PriceData
import com.jcoronado.minimalbitcoinwidget.viewmodels.PriceViewModel
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidget
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidgetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
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

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        
        val lastApiCallTime = prefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, 0L)
        val currentTime = System.currentTimeMillis()
        val cacheDuration = 15 * 60 * 1000L // 15 minutes

        if (currentTime - lastApiCallTime < cacheDuration) {
            dao.insert(DebugLog(message = "PriceWorker: Skipping Data Fetch (< 15 mins)"))
            Log.d(LOG_TAG, "Data is fresh, skipping fetch")
            PriceViewModel.refreshWidgetsFromCache(context)
            return@withContext Result.success()
        }

        val currencyCode = prefs.getString(Prefs.SELECTED_CURRENCY, AppConstants.CURRENCY_DEFAULT)
            ?: AppConstants.CURRENCY_DEFAULT

        val gson = Gson()
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
        val url = Api.COINGECKO_API_URL + currencyCode
        val request = Request.Builder().url(url).build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body.string()

                val type = object : TypeToken<List<PriceData>>() {}.type
                val dataList: List<PriceData> = gson.fromJson(body, type)

                if (dataList.isNotEmpty()) {
                    val priceData = dataList[0]
                    
                    // update shared prefs cache for the MainActivity display
                    prefs.edit {
                        putLong(Prefs.LAST_API_CALL_TIMESTAMP, System.currentTimeMillis())
                        putString(Prefs.CACHED_PRICE_DATA, gson.toJson(priceData))
                    }

                    // update widgets using the helper in PriceViewModel
                    PriceViewModel.refreshWidgetsFromCache(context)

                    dao.insert(DebugLog(message = "PriceWorker: Data Fetched"))

                    Result.success()
                } else {
                    Result.retry()
                }
            } else if (response.code == 429) {
                Result.retry()
            } else {
                throw Exception("Error: ${response.code}")
            }
        } catch (e: IOException) {
            dao.insert(DebugLog(message = "PriceWorker: Failed (Network) - ${e.localizedMessage}"))
            Log.e(LOG_TAG, "Network error", e)
            return@withContext handleError(e)
        } catch (e: Exception) {
            dao.insert(DebugLog(message = "PriceWorker: Failed (Other) - ${e.localizedMessage}"))
            Log.e(LOG_TAG, "Fatal error", e)
            return@withContext handleError(e)
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
