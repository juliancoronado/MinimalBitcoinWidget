package com.jcoronado.minimalbitcoinwidget.widgets.glance

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.preference.PreferenceManager
import com.jcoronado.minimalbitcoinwidget.MainActivity
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.classes.Prefs
import com.jcoronado.minimalbitcoinwidget.classes.WidgetFont
import com.jcoronado.minimalbitcoinwidget.utils.FormatUtils

class PriceWidget : GlanceAppWidget() {

    override val stateDefinition = PriceWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = currentState<PriceWidgetState>()
            WidgetContent(state)
        }
    }

    @SuppressLint( "DiscouragedApi")
    @Composable
    private fun WidgetContent(state: PriceWidgetState) {
        val context = LocalContext.current
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val fontKey = when (state) {
            is PriceWidgetState.Available -> state.fontKey
            is PriceWidgetState.Error -> state.fontKey
            is PriceWidgetState.Loading -> {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                prefs.getString(Prefs.SELECTED_WIDGET_FONT, WidgetFont.DEFAULT.key) ?: WidgetFont.DEFAULT.key
            }
        }
        val widgetFont = WidgetFont.fromKey(fontKey)
        val typeface = WidgetBitmapUtils.getTypeface(context, widgetFont)
        val boldPrice = when (state) {
            is PriceWidgetState.Available -> state.boldPrice
            is PriceWidgetState.Error -> state.boldPrice
            is PriceWidgetState.Loading -> {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                prefs.getBoolean(Prefs.WIDGET_PRICE_BOLD, true)
            }
        }
        val priceTypeface = WidgetBitmapUtils.getTypeface(context, widgetFont, isBold = boldPrice)

        GlanceTheme(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                GlanceTheme.colors
            } else {
                GlanceColorScheme.colors
            }
        ) {
            var backgroundModifier = GlanceModifier.fillMaxSize()
            
            // Check if the device's launcher provides a system-wide widget corner radius (Android 12+)
            val systemCornerRadiusDefined = context.resources
                .getIdentifier("system_app_widget_background_radius", "dimen", "android") != 0

            backgroundModifier = if (Build.VERSION.SDK_INT >= 31 && systemCornerRadiusDefined) {
                // On Android 12+ (API 31 and above):
                // We use the system-provided corner radius to match other widgets on the homescreen.
                // GlanceTheme.colors.widgetBackground automatically handles Material You dynamic colors.
                backgroundModifier
                    .background(GlanceTheme.colors.widgetBackground)
                    .appWidgetBackground()
                    .cornerRadius(android.R.dimen.system_app_widget_background_radius)
            } else {
                // On Android 11 and lower (API 30 and below):
                // The .cornerRadius() modifier does not work. We must use an XML shape drawable.
                // R.drawable.glance_widget_bg has a hardcoded 16dp radius and uses standard 
                // light/dark mode colors defined in values/glance_colors.xml and values-night/glance_colors.xml.
                backgroundModifier
                    .background(ImageProvider(R.drawable.glance_widget_bg))
                    .appWidgetBackground()
            }

            Box(modifier = backgroundModifier) {
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp)
                        .clickable(actionStartActivity(openAppIntent)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isMonospaced = (widgetFont == WidgetFont.GOOGLE_SANS_CODE)
                    when (state) {
                        is PriceWidgetState.Available -> {
                            AvailableUI(state, GlanceModifier.defaultWeight(), typeface, priceTypeface, isMonospaced)
                        }

                        is PriceWidgetState.Error -> {
                            if (state.lastValidState != null) {
                                AvailableUI(
                                    state.lastValidState,
                                    GlanceModifier.defaultWeight(),
                                    typeface,
                                    priceTypeface,
                                    isMonospaced,
                                    error = true
                                )
                            } else {
                                ErrorUI(state, typeface)
                            }
                        }

                        is PriceWidgetState.Loading -> {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun AvailableUI(
        state: PriceWidgetState.Available,
        modifier: GlanceModifier,
        typeface: Typeface?,
        priceTypeface: Typeface?,
        isMonospaced: Boolean,
        error: Boolean = false
    ) {
        Header(state.currency, state.intervalLabelResId, typeface, isMonospaced)
        Spacer(modifier)
        PriceValue(state, priceTypeface, isMonospaced)
        Spacer(modifier)
        PriceChange(state.changePercentage, error, typeface, isMonospaced)
    }

    @Composable
    private fun ErrorUI(state: PriceWidgetState.Error, typeface: Typeface?) {
        val context = LocalContext.current
        val errorLabel = "${context.getString(R.string.error)}:"
        val labelBitmap = WidgetBitmapUtils.createTextBitmap(
            context = context,
            text = errorLabel,
            fontSizeSp = 12f,
            typeface = typeface
        )
        val messageBitmap = WidgetBitmapUtils.createTextBitmap(
            context = context,
            text = state.message,
            fontSizeSp = 12f,
            typeface = typeface
        )
        Image(
            provider = ImageProvider(labelBitmap),
            contentDescription = errorLabel,
            colorFilter = ColorFilter.tint(GlanceTheme.colors.error)
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Image(
            provider = ImageProvider(messageBitmap),
            contentDescription = state.message,
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
        )
    }

    @Composable
    private fun Header(currency: String, intervalLabel: Int, typeface: Typeface?, isMonospaced: Boolean = false) {
        val context = LocalContext.current
        val headerText = "/ ${currency.uppercase()} ・ ${context.getString(intervalLabel)}"
        val fontSize = WidgetBitmapUtils.getWidgetSecondaryFontSize(isMonospaced)
        val headerBitmap = WidgetBitmapUtils.createTextBitmap(
            context = context,
            text = headerText,
            fontSizeSp = fontSize,
            typeface = typeface
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.rounded_currency_bitcoin_24),
                contentDescription = context.getString(R.string.bitcoin_icon_description),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.secondary),
                modifier = GlanceModifier.size(14.dp)
            )
            Spacer(modifier = GlanceModifier.width(2.dp))
            Image(
                provider = ImageProvider(headerBitmap),
                contentDescription = headerText,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.secondary)
            )
        }
    }

    @Composable
    private fun PriceValue(state: PriceWidgetState.Available, typeface: Typeface?, isMonospaced: Boolean = false) {
        val context = LocalContext.current
        val priceData = FormatUtils.formatPriceSeparated(state.price, state.currency)
        val (priceFontSize, symbolFontSize) = WidgetBitmapUtils.getWidgetPriceFontSize(state.price, isMonospaced)

        val symbolBitmap = WidgetBitmapUtils.createTextBitmap(
            context = context,
            text = priceData.symbol,
            fontSizeSp = symbolFontSize,
            typeface = typeface,
            isBold = state.boldPrice
        )

        val priceBitmap = WidgetBitmapUtils.createTextBitmap(
            context = context,
            text = priceData.price,
            fontSizeSp = priceFontSize,
            typeface = typeface,
            isBold = state.boldPrice
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (priceData.symbolAtStart) {
                // symbol on left
                Image(
                    provider = ImageProvider(symbolBitmap),
                    contentDescription = priceData.symbol,
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
                )
                Spacer(modifier = GlanceModifier.width(2.dp))
                Image(
                    provider = ImageProvider(priceBitmap),
                    contentDescription = priceData.price,
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
                )
            } else {
                // symbol on right (Euros in some regions)
                Image(
                    provider = ImageProvider(priceBitmap),
                    contentDescription = priceData.price,
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
                )
                Spacer(modifier = GlanceModifier.width(2.dp))
                Image(
                    provider = ImageProvider(symbolBitmap),
                    contentDescription = priceData.symbol,
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
                )
            }
        }
    }

    @SuppressLint("DefaultLocale")
    @Composable
    private fun PriceChange(changePercentage: Double, error: Boolean, typeface: Typeface?, isMonospaced: Boolean = false) {
        val context = LocalContext.current
        val (iconRes, color, iconDesc) = if (changePercentage > 0) {
            Triple(
                R.drawable.rounded_trending_up_24,
                GlanceTheme.colors.primary,
                context.getString(R.string.trending_up_icon_description)
            )
        } else if (changePercentage < 0) {
            Triple(
                R.drawable.rounded_trending_down_24,
                GlanceTheme.colors.error,
                context.getString(R.string.trending_down_icon_description)
            )
        } else {
            Triple(
                R.drawable.rounded_trending_flat_24,
                GlanceTheme.colors.secondary,
                context.getString(R.string.trending_flat_icon_description)
            )
        }

        val formatPattern = "%.2f%%"
        val changeText = String.format(formatPattern, changePercentage)
        val fontSize = WidgetBitmapUtils.getWidgetSecondaryFontSize(isMonospaced)
        val changeBitmap = WidgetBitmapUtils.createTextBitmap(
            context = context,
            text = changeText,
            fontSizeSp = fontSize,
            typeface = typeface
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = iconDesc,
                colorFilter = ColorFilter.tint(color),
                modifier = GlanceModifier.size(16.dp).padding(top = 2.dp)
            )
            Spacer(modifier = GlanceModifier.width(4.dp))
            Image(
                provider = ImageProvider(changeBitmap),
                contentDescription = changeText,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.secondary)
            )
            if (error) {
                Spacer(modifier = GlanceModifier.width(4.dp))
                Box(
                    modifier = GlanceModifier.size(3.dp).background(
                        GlanceTheme.colors.error
                    ).cornerRadius(32.dp), content = {})
            }
        }
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 100)
    @Composable
    private fun WidgetPreviewDataPos() {
        val state = PriceWidgetState.Available(
            price = 123456.78,
            changePercentage = 1.23,
            intervalLabelResId = R.string.interval_24h,
            currency = "USD",
        )
        WidgetContent(state)
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 100)
    @Composable
    private fun WidgetPreviewDataNeg() {
        val state = PriceWidgetState.Available(
            price = 123456.78,
            changePercentage = -2.34,
            intervalLabelResId = R.string.interval_7d,
            currency = "USD",
        )
        WidgetContent(state)
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 100)
    @Composable
    private fun WidgetPreviewDataZero() {
        val state = PriceWidgetState.Available(
            price = 123456.78,
            changePercentage = 0.00,
            intervalLabelResId = R.string.interval_24h,
            currency = "USD",
        )
        WidgetContent(state)
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 100)
    @Composable
    private fun WidgetPreviewDataLargePos() {
        val state = PriceWidgetState.Available(
            price = 1234560.78,
            changePercentage = 3.45,
            intervalLabelResId = R.string.interval_30d,
            currency = "USD",
        )
        WidgetContent(state)
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 100)
    @Composable
    private fun WidgetPreviewDataLargeNeg() {
        val state = PriceWidgetState.Available(
            price = 1234560.78,
            changePercentage = -1.23,
            intervalLabelResId = R.string.interval_24h,
            currency = "USD"
        )
        WidgetContent(state)
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 100)
    @Composable
    private fun WidgetPreviewPosError() {
        // error but with a valid previous state
        val lastValidState = PriceWidgetState.Available(
            price = 123456.78,
            changePercentage = 2.34,
            intervalLabelResId = R.string.interval_24h,
            currency = "USD"
        )
        val state = PriceWidgetState.Error(
            lastValidState = lastValidState, message = "Error Message"
        )
        WidgetContent(state)
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 250, heightDp = 100)
    @Composable
    private fun WidgetPreviewNegError() {
        val lastValidState = null
        // no last valid state
        val state = PriceWidgetState.Error(
            lastValidState = lastValidState, message = "Price data could not be fetched"
        )
        WidgetContent(state)
    }
}