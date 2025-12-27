package com.jcoronado.minimalbitcoinwidget.widgets

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jcoronado.minimalbitcoinwidget.CurrencyInfo
import com.jcoronado.minimalbitcoinwidget.MainActivity
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.classes.Api
import com.jcoronado.minimalbitcoinwidget.classes.AppConstants
import com.jcoronado.minimalbitcoinwidget.classes.Prefs
import com.jcoronado.minimalbitcoinwidget.classes.PriceData
import com.jcoronado.minimalbitcoinwidget.getCurrencyInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.NumberFormat

// TODO - convert this "Test" into the main widget
// TODO - make sure that different currencies and locale formatting displays correctly
// TODO - make sure to handle "error" states properly (network failure, etc)
// TODO - tweak theme and colors to match Material Widgets better (lighter text on background)
// TODO - remove legacy widget and code in future release

class TestWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val priceData = withContext(Dispatchers.IO) { loadData(context) }
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val currency = prefs.getString(Prefs.SELECTED_CURRENCY, AppConstants.CURRENCY_DEFAULT)
        val currencyInfo = getCurrencyInfo(currency)

        provideContent {
            WidgetContent(priceData, currencyInfo)
        }
    }

    private fun loadData(context: Context): PriceData {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val lastApiCallTime = prefs.getLong(Prefs.LAST_API_CALL_TIMESTAMP, 0L)
        val currentTime = System.currentTimeMillis()
        val gson = Gson()

        // Check if cached data is still valid
        if (currentTime - lastApiCallTime < AppConstants.CACHE_DURATION_MILLIS) {
            val cachedDataJson = prefs.getString(Prefs.CACHED_PRICE_DATA, null)
            if (cachedDataJson != null) {
                return gson.fromJson(cachedDataJson, PriceData::class.java)
            }
        }

        // Cache is stale or missing, fetch from network
        val currency = prefs.getString(Prefs.SELECTED_CURRENCY, AppConstants.CURRENCY_DEFAULT)
            ?: AppConstants.CURRENCY_DEFAULT
        val url = Api.COINGECKO_API_URL + currency
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body.string()
                val type = object : TypeToken<Map<String, Map<String, Double>>>() {}.type
                val data: Map<String, Map<String, Double>> = gson.fromJson(body, type)
                val priceDataJson = data["bitcoin"]

                if (priceDataJson != null) {
                    val price = priceDataJson[currency] ?: 0.0
                    val change24h = priceDataJson["${currency}_24h_change"] ?: 0.0
                    val priceData = PriceData(price, change24h)

                    // Update cache
                    prefs.edit {
                        putLong(Prefs.LAST_API_CALL_TIMESTAMP, System.currentTimeMillis())
                        putString(Prefs.CACHED_PRICE_DATA, gson.toJson(priceData))
                    }
                    return priceData
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // fallback to cache if network call fails, otherwise return zeros (app first installed but no cached data yet)
        val cachedDataJson = prefs.getString(Prefs.CACHED_PRICE_DATA, null)
        return if (cachedDataJson != null) {
            gson.fromJson(cachedDataJson, PriceData::class.java)
        } else {
            PriceData(0.0, 0.0)
        }
    }

    @Composable
    private fun WidgetContent(priceData: PriceData, currencyInfo: CurrencyInfo) {
        val openAppIntent = Intent(LocalContext.current, MainActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // pass reset_nav to AppNavigation via extras
            putExtra("reset_nav", true)
        }
        GlanceTheme(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) GlanceTheme.colors
            else GlanceColorScheme.colors
        ) {
            Scaffold {
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(vertical = 10.dp)
                        .clickable(actionStartActivity(openAppIntent)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TODO - clean up this logic (like currencyInfo variable)
                    TitleUI(currencyInfo)
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    PriceUi(priceData, currencyInfo)
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    // TODO - don't divide here, do that when formatting the string
                    ChangeUI(changePercentage = priceData.priceChangePercentage24h / 100)
                }
            }
        }
    }

    @Composable
    private fun TitleUI(currencyInfo: CurrencyInfo) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.rounded_currency_bitcoin_24),
                contentDescription = "Bitcoin Icon",
                colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
                modifier = GlanceModifier.size(14.dp).padding(top = 1.dp)
            )
            Text(
                "/ ${currencyInfo.isoCode}", style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp
                )
            )
        }
    }

    @Composable
    private fun PriceUi(priceData: PriceData, currencyInfo: CurrencyInfo) {
        val numberFormatter = NumberFormat.getNumberInstance().apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

        val fontSize = if (priceData.currentPrice >= 100000) 24.sp else 26.sp

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = currencyInfo.symbol, style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 18.sp
                )
            )
            Spacer(modifier = GlanceModifier.width(4.dp))
            Text(
                text = numberFormatter.format(priceData.currentPrice), style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = fontSize
                )
            )
        }
    }

    @Composable
    private fun ChangeUI(changePercentage: Double) {
        val (iconRes, color) = if (changePercentage >= 0) {
            R.drawable.rounded_trending_up_24 to GlanceTheme.colors.tertiary
        } else {
            R.drawable.rounded_trending_down_24 to GlanceTheme.colors.error
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color),
                modifier = GlanceModifier.size(16.dp).padding(top = 2.dp)
            )
            Spacer(modifier = GlanceModifier.width(4.dp))
            Text(
                text = String.format("%.2f%%", changePercentage * 100), style = TextStyle(
                    color = GlanceTheme.colors.secondary,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp
                )
            )
        }
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 100)
    @Composable
    private fun WidgetPreviewDataPos() {
        WidgetContent(
            PriceData(currentPrice = 84123.34, priceChangePercentage24h = 2.3),
            CurrencyInfo("$", "USD")
        )
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 100)
    @Composable
    private fun WidgetPreviewDataNeg() {
        WidgetContent(
            PriceData(currentPrice = 84123.78, priceChangePercentage24h = -1.67),
            CurrencyInfo("$", "USD")
        )
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 100)
    @Composable
    private fun WidgetPreviewDataLargePos() {
        WidgetContent(
            PriceData(currentPrice = 184123.78, priceChangePercentage24h = 0.57),
            CurrencyInfo("$", "USD")
        )
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 100)
    @Composable
    private fun WidgetPreviewDataLargeNeg() {
        WidgetContent(
            PriceData(currentPrice = 184123.78, priceChangePercentage24h = -2.47),
            CurrencyInfo("$", "USD")
        )
    }
}
