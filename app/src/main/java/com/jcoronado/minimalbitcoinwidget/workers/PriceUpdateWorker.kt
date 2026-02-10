package com.jcoronado.minimalbitcoinwidget.workers

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
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
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidget
import com.jcoronado.minimalbitcoinwidget.widgets.legacy.getCurrencyInfo
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidgetState
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidgetStateDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

        dao.insert(DebugLog(message = "Worker Started: Fetching Data"))

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
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
                    
                    val selectedInterval = prefs.getInt(Prefs.SELECTED_CHANGE_PERCENTAGE, 0)
                    val percentage = when (selectedInterval) {
                        0 -> priceData.priceChangePercentage24h
                        1 -> priceData.priceChangePercentage7d
                        2 -> priceData.priceChangePercentage30d
                        else -> priceData.priceChangePercentage24h
                    }

                    val intervalLabel = when (selectedInterval) {
                        0 -> "24H"
                        1 -> "7D"
                        2 -> "30D"
                        else -> "24H"
                    }

                    val symbol = getCurrencyInfo(currencyCode).symbol

                    val currentTime =
                        SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())

                    val newState = PriceWidgetState.Available(
                        price = priceData.currentPrice,
                        changePercentage = percentage,
                        intervalLabel = intervalLabel,
                        currency = currencyCode,
                        symbol = symbol,
                        lastUpdated = currentTime,
                        debug = AppConstants.WIDGET_DEBUG_MODE
                    )

                    // update shared prefs cache for the MainActivity display
                    prefs.edit {
                        putLong(Prefs.LAST_API_CALL_TIMESTAMP, System.currentTimeMillis())
                        putString(Prefs.CACHED_PRICE_DATA, gson.toJson(priceData))
                    }

                    // update widgets
                    updateWidgets(newState)

                    dao.insert(DebugLog(message = "Worker Success: Data fetched"))

                    Result.success()
                } else {
                    Result.retry()
                }
            } else if (response.code == 429) {
                // rate limit error - retry with backoff
                Result.retry()
            } else {
                // other error - display error state
                throw Exception("Server error: ${response.code}")
            }
        } catch (e: IOException) {
            dao.insert(DebugLog(message = "Worker Failed (Network Error): ${e.localizedMessage}"))
            // network timeout or no connection - retry with backoff
            Log.e(LOG_TAG, "Network error", e)
            return@withContext handleError(e)
        } catch (e: Exception) {
            dao.insert(DebugLog(message = "Worker Failed (Other Error): ${e.localizedMessage}"))
            // other error
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

        updateWidgets(PriceWidgetState.Error(e.message ?: "Unknown Error" ,  lastValid))
        return Result.retry()
    }
    private suspend fun updateWidgets(state: PriceWidgetState) {
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(PriceWidget::class.java)
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, PriceWidgetStateDefinition, glanceId) {
                state
            }
        }
        Log.d(LOG_TAG, "Updating widgets using .updateAll()")
        // notify widgets to redraw
        PriceWidget().updateAll(context)
    }

    companion object {
        private const val WORK_NAME = "PriceUpdateWork"

        fun enqueue(context: Context, intervalMinutes: Long? = null) {
            val minutesToUse = intervalMinutes ?: run {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val intervalIndex = prefs.getInt(Prefs.REFRESH_INTERVAL, 0)
                when (intervalIndex) {
                    0 -> 30L
                    1 -> 60L
                    2 -> 240L
                    else -> 30L
                }
            }

            Log.d(LOG_TAG, "Enqueuing work with interval: $minutesToUse minutes")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<PriceUpdateWorker>(minutesToUse, TimeUnit.MINUTES)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
