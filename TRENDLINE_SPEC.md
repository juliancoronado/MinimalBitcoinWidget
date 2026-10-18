# Trendline Widget Implementation Specification

This is a temporary file and should not remain in the repo for long.

Created: Oct. 13th, 2026

> **Purpose for AI Agent (Antigravity)**:  
> This specification contains the complete architecture, visual design parameters, and verbatim source code required to re-implement the **Trendline Glance Widget (Expressive)** in Minimal Bitcoin Widget (`MinimalBitcoinWidget`).
>
> An exact working commit of this implementation is also preserved on the git branch:
> `feature/trendline-widget`

---

## 1. Feature & Design Overview

The **Trendline Glance Widget** is a Material 3 Expressive Android home screen widget built with Jetpack Glance that displays the live Bitcoin price, percentage change, and a smooth 7-day sparkline trendline curve.

### Key Visual & UX Design Elements

1. **Hero Price Micro-Typography**:
   - The price separates the integer portion and decimal cents into distinct font sizes (cents are scaled down by ~32%).
   - Clean native currency symbol placement (prefix or suffix) according to the user's active locale.
   - Handled via `FormatUtils.formatPriceParts(...)`.
2. **Tactile Percentage Badge**:
   - A Material 3 pill badge (`cornerRadius(10.dp)`) showing directional arrow token (`↑` or `↓`) and 2-decimal percentage change.
   - Uses semantic container colors (`GlanceTheme.colors.primaryContainer` for gains, `GlanceTheme.colors.errorContainer` for drops).
   - In 2x2 (Medium) layout, it is stacked directly under the price to prevent text truncation.
3. **Dashed Opening Baseline**:
   - A subtle horizontal dashed line (`DashPathEffect`) across the chart indicating the opening/reference price level at the beginning of the period.
   - Rendered using `(Color.alpha(strokeColor) * 0.40f)` so it stays visually subordinate to the primary curve.
4. **Smooth Catmull-Rom Cubic Spline**:
   - Catmull-Rom spline interpolation (`alpha = 0.20f`) with Gaussian 3-point smoothing (`[0.25, 0.50, 0.25]`) preserving endpoint values.
   - Off-screen anti-aliased bitmap rendering using `TrendlineBitmapRenderer`.
   - Subtle vertical linear gradient fill beneath the curve fading to transparent.
5. **Live Glowing Endpoint Dot**:
   - Triple-layer endpoint marker at the latest price:
     - Outer translucent halo (radius 6.0 dp, alpha 0.35)
     - Solid theme-colored core (radius 3.2 dp)
     - Bright white pinpoint center (radius 1.2 dp)
6. **Responsive Layouts**:
   - **Compact (2x1)**: Top metrics with edge-to-edge sparkline below.
   - **Wide-Slim (4x1 / 3x1)**: Left metrics with right-aligned wide sparkline.
   - **Medium (2x2)**: Stacked header, bold price with scaled cents, tactile badge below, and edge-to-edge trendline filling the bottom.
   - **Large (4x2)**: Full-width dashboard with hero typography and large trendline.
7. **Unified Background Updates & Widget Picker Previews**:
   - Synchronized background polling via `PriceUpdateWorker`.
   - Android 15/16 widget picker previews published via `PriceViewModel.updateWidgetPreview`.

---

## 2. File Implementation Manifest

The feature consists of **6 new files** and modifications to **8 existing areas**:

| Action     | Path                                                                                                  | Description                                                                |
| :--------- | :---------------------------------------------------------------------------------------------------- | :------------------------------------------------------------------------- |
| **CREATE** | `app/src/main/java/com/jcoronado/minimalbitcoinwidget/widgets/glance/TrendlinePriceWidget.kt`         | Glance AppWidget UI layout & responsive sizes                              |
| **CREATE** | `app/src/main/java/com/jcoronado/minimalbitcoinwidget/widgets/glance/TrendlinePriceWidgetReceiver.kt` | BroadcastReceiver for Glance widget lifecycle                              |
| **CREATE** | `app/src/main/java/com/jcoronado/minimalbitcoinwidget/widgets/glance/TrendlineBitmapRenderer.kt`      | Off-screen Bitmap renderer (Catmull-Rom spline, dashed baseline, live dot) |
| **CREATE** | `app/src/main/res/xml/trendline_price_widget_info.xml`                                                | AppWidgetProvider XML metadata                                             |
| **CREATE** | `app/src/main/res/xml-v31/trendline_price_widget_info.xml`                                            | AppWidgetProvider XML metadata for Android 12+ (API 31+)                   |
| **CREATE** | `app/src/test/java/com/jcoronado/minimalbitcoinwidget/TrendlineBitmapRendererTest.kt`                 | Unit tests for responsive breakpoints, serialization, and formatting       |
| **MODIFY** | `app/src/main/java/com/jcoronado/minimalbitcoinwidget/utils/FormatUtils.kt`                           | Added `FormattedPriceParts` and `formatPriceParts(...)`                    |
| **MODIFY** | `app/src/main/java/com/jcoronado/minimalbitcoinwidget/widgets/glance/PriceWidgetState.kt`             | Added `sparklinePrices: List<Double> = emptyList()` to `Available` state   |
| **MODIFY** | `app/src/main/AndroidManifest.xml`                                                                    | Registered `TrendlinePriceWidgetReceiver`                                  |
| **MODIFY** | `app/src/main/java/com/jcoronado/minimalbitcoinwidget/viewmodels/PriceViewModel.kt`                   | Added widget preview publication and widget instance updates               |
| **MODIFY** | `app/src/main/java/com/jcoronado/minimalbitcoinwidget/workers/PriceUpdateWorker.kt`                   | Added widget error handling and cancellation checks                        |
| **MODIFY** | `app/src/main/res/values*/strings.xml` (9 locales)                                                    | Added widget picker name and localized descriptions                        |
| **MODIFY** | `AGENTS.md`                                                                                           | Added `TrendlinePriceWidget` to Glance widgets directory list              |

---

## 3. Verbatim Source Code for New Files

### File 1: `TrendlinePriceWidget.kt`

Path: `app/src/main/java/com/jcoronado/minimalbitcoinwidget/widgets/glance/TrendlinePriceWidget.kt`

```kotlin
package com.jcoronado.minimalbitcoinwidget.widgets.glance

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.preference.PreferenceManager
import com.jcoronado.minimalbitcoinwidget.MainActivity
import com.jcoronado.minimalbitcoinwidget.R
import com.jcoronado.minimalbitcoinwidget.classes.AppConstants
import com.jcoronado.minimalbitcoinwidget.classes.Prefs
import com.jcoronado.minimalbitcoinwidget.classes.WidgetColorStyle
import com.jcoronado.minimalbitcoinwidget.classes.WidgetFont
import com.jcoronado.minimalbitcoinwidget.classes.WidgetPriceSize
import com.jcoronado.minimalbitcoinwidget.classes.WidgetTheme
import com.jcoronado.minimalbitcoinwidget.data.PriceRepository
import com.jcoronado.minimalbitcoinwidget.ui.components.PLACEHOLDER_SPARKLINE_PRICES
import com.jcoronado.minimalbitcoinwidget.utils.FormatUtils
import com.jcoronado.minimalbitcoinwidget.utils.TimeInterval

/**
 * Material 3 Expressive Glance Widget displaying Bitcoin price, change metrics,
 * and an edge-to-edge off-screen rendered price trendline (sparkline) across responsive sizes.
 */
class TrendlinePriceWidget : GlanceAppWidget() {

    companion object {
        val COMPACT = DpSize(140.dp, 60.dp)
        val WIDE_SLIM = DpSize(260.dp, 60.dp)
        val MEDIUM = DpSize(160.dp, 140.dp)
        val LARGE = DpSize(260.dp, 140.dp)
    }

    override val sizeMode = SizeMode.Exact

    override val stateDefinition = PriceWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = currentState<PriceWidgetState>()
            val size = LocalSize.current
            WidgetContent(state, size)
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val repository = PriceRepository(context)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val fontKey = prefs.getString(Prefs.SELECTED_WIDGET_FONT, WidgetFont.DEFAULT.key) ?: WidgetFont.DEFAULT.key
        val priceSizeKey = prefs.getString(Prefs.WIDGET_PRICE_SIZE, "default") ?: "default"
        val colorStyleKey = prefs.getString(Prefs.WIDGET_COLOR_STYLE, "dynamic") ?: "dynamic"
        val themeKey = prefs.getString(Prefs.WIDGET_THEME, "system") ?: "system"
        val selectedInterval = prefs.getInt(Prefs.SELECTED_CHANGE_PERCENTAGE, AppConstants.CHANGE_PERCENTAGE_DEFAULT)
        val interval = TimeInterval.fromValue(selectedInterval)

        val previewState: PriceWidgetState.Available = when {
            repository.isMockUiEnabled() -> {
                val mockData = repository.getMockPriceData()
                PriceWidgetState.Available(
                    price = mockData.currentPrice,
                    changePercentage = mockData.getPercentageForInterval(selectedInterval),
                    intervalLabelResId = interval.labelResId,
                    currency = repository.getMockCurrency(),
                    fontKey = fontKey,
                    priceSizeKey = priceSizeKey,
                    colorStyleKey = colorStyleKey,
                    themeKey = themeKey,
                    sparklinePrices = mockData.getSparklineForInterval(selectedInterval)
                )
            }
            else -> {
                val cachedData = repository.getCachedPriceData()
                if (cachedData != null) {
                    PriceWidgetState.Available(
                        price = cachedData.currentPrice,
                        changePercentage = cachedData.getPercentageForInterval(selectedInterval),
                        intervalLabelResId = interval.labelResId,
                        currency = repository.getSelectedCurrency(),
                        fontKey = fontKey,
                        priceSizeKey = priceSizeKey,
                        colorStyleKey = colorStyleKey,
                        themeKey = themeKey,
                        sparklinePrices = cachedData.getSparklineForInterval(selectedInterval)
                    )
                } else {
                    PriceWidgetState.Available(
                        price = 95432.10,
                        changePercentage = 2.45,
                        intervalLabelResId = interval.labelResId,
                        currency = repository.getSelectedCurrency(),
                        fontKey = fontKey,
                        priceSizeKey = priceSizeKey,
                        colorStyleKey = colorStyleKey,
                        themeKey = themeKey,
                        sparklinePrices = PLACEHOLDER_SPARKLINE_PRICES
                    )
                }
            }
        }

        provideContent {
            val size = LocalSize.current
            WidgetContent(previewState, size)
        }
    }

    @SuppressLint("DiscouragedApi")
    @Composable
    private fun WidgetContent(state: PriceWidgetState, size: DpSize) {
        val context = LocalContext.current

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

        val colorStyleKey = when (state) {
            is PriceWidgetState.Available -> state.colorStyleKey
            is PriceWidgetState.Error -> state.colorStyleKey
            is PriceWidgetState.Loading -> {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                prefs.getString(Prefs.WIDGET_COLOR_STYLE, WidgetColorStyle.DYNAMIC.key) ?: WidgetColorStyle.DYNAMIC.key
            }
        }
        val themeKey = when (state) {
            is PriceWidgetState.Available -> state.themeKey
            is PriceWidgetState.Error -> state.themeKey
            is PriceWidgetState.Loading -> {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                prefs.getString(Prefs.WIDGET_THEME, WidgetTheme.SYSTEM.key) ?: WidgetTheme.SYSTEM.key
            }
        }
        val colorStyle = WidgetColorStyle.fromKey(colorStyleKey)
        val widgetTheme = WidgetTheme.fromKey(themeKey)

        val colorProviders = GlanceColorScheme.colorsFor(context, colorStyle, widgetTheme)

        GlanceTheme(colors = colorProviders) {
            val systemCornerRadiusDefined = LocalContext.current.resources
                .getIdentifier("system_app_widget_background_radius", "dimen", "android") != 0

            var backgroundModifier = GlanceModifier.fillMaxSize()
            backgroundModifier = GlanceColorScheme.applyBackground(
                modifier = backgroundModifier,
                colorStyle = colorStyle,
                widgetTheme = widgetTheme,
                systemCornerRadiusDefined = systemCornerRadiusDefined
            )

            backgroundModifier = if (Build.VERSION.SDK_INT >= 31 && systemCornerRadiusDefined) {
                backgroundModifier
                    .appWidgetBackground()
                    .cornerRadius(android.R.dimen.system_app_widget_background_radius)
            } else {
                backgroundModifier.appWidgetBackground()
            }

            Box(
                modifier = backgroundModifier
                    .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
            ) {
                when (state) {
                    is PriceWidgetState.Available -> {
                        ResponsiveAvailableUI(
                            state = state,
                            size = size,
                            typeface = typeface,
                            colorStyle = colorStyle,
                            widgetTheme = widgetTheme
                        )
                    }
                    is PriceWidgetState.Error -> {
                        if (state.lastValidState != null) {
                            ResponsiveAvailableUI(
                                state = state.lastValidState,
                                size = size,
                                typeface = typeface,
                                colorStyle = colorStyle,
                                widgetTheme = widgetTheme,
                                error = true
                            )
                        } else {
                            ErrorUI(state, typeface)
                        }
                    }
                    is PriceWidgetState.Loading -> {
                        Box(
                            modifier = GlanceModifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ResponsiveAvailableUI(
        state: PriceWidgetState.Available,
        size: DpSize,
        typeface: Typeface?,
        colorStyle: WidgetColorStyle,
        widgetTheme: WidgetTheme,
        error: Boolean = false
    ) {
        val width = size.width
        val height = size.height

        when {
            width >= 240.dp && height >= 120.dp -> {
                LargeLayout(state, size, typeface, colorStyle, widgetTheme, error)
            }
            height >= 120.dp -> {
                MediumLayout(state, size, typeface, colorStyle, widgetTheme, error)
            }
            width >= 240.dp -> {
                WideSlimLayout(state, size, typeface, colorStyle, widgetTheme, error)
            }
            else -> {
                CompactLayout(state, size, typeface, colorStyle, widgetTheme, error)
            }
        }
    }

    /**
     * Compact (2x1) layout: Top metrics with edge-to-edge sparkline below.
     */
    @Composable
    private fun CompactLayout(
        state: PriceWidgetState.Available,
        size: DpSize,
        typeface: Typeface?,
        colorStyle: WidgetColorStyle,
        widgetTheme: WidgetTheme,
        error: Boolean
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            Column(
                modifier = GlanceModifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 8.dp)
            ) {
                Header(state.currency, state.intervalLabelResId, typeface)
                Spacer(modifier = GlanceModifier.height(2.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PriceValue(state, typeface, scaleFactor = 0.95f)
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    PriceChange(state.changePercentage, error, typeface)
                }
            }
            Spacer(modifier = GlanceModifier.height(2.dp))
            TrendlineImage(
                prices = state.sparklinePrices,
                isPositive = state.changePercentage >= 0,
                widthDp = size.width,
                heightDp = (size.height - 48.dp).coerceAtLeast(24.dp),
                colorStyle = colorStyle,
                widgetTheme = widgetTheme,
                includeGradient = true,
                strokeWidthDp = 2.0f
            )
        }
    }

    /**
     * Wide-Slim (4x1 / 3x1) layout: Left metrics + Right wide sparkline bleeding to edge.
     */
    @Composable
    private fun WideSlimLayout(
        state: PriceWidgetState.Available,
        size: DpSize,
        typeface: Typeface?,
        colorStyle: WidgetColorStyle,
        widgetTheme: WidgetTheme,
        error: Boolean
    ) {
        Row(
            modifier = GlanceModifier.fillMaxSize().padding(start = 14.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = GlanceModifier.defaultWeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Header(state.currency, state.intervalLabelResId, typeface)
                Spacer(modifier = GlanceModifier.height(2.dp))
                PriceValue(state, typeface, scaleFactor = 1.0f)
                Spacer(modifier = GlanceModifier.height(2.dp))
                PriceChange(state.changePercentage, error, typeface)
            }
            TrendlineImage(
                prices = state.sparklinePrices,
                isPositive = state.changePercentage >= 0,
                widthDp = size.width * 0.52f,
                heightDp = size.height,
                colorStyle = colorStyle,
                widgetTheme = widgetTheme,
                includeGradient = true,
                strokeWidthDp = 2.2f
            )
        }
    }

    /**
     * Medium (2x2) layout: Stacked header, bold price, change pill, and edge-to-edge sparkline sitting directly below.
     */
    @Composable
    private fun MediumLayout(
        state: PriceWidgetState.Available,
        size: DpSize,
        typeface: Typeface?,
        colorStyle: WidgetColorStyle,
        widgetTheme: WidgetTheme,
        error: Boolean
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            Column(
                modifier = GlanceModifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 12.dp)
            ) {
                Header(state.currency, state.intervalLabelResId, typeface)
                Spacer(modifier = GlanceModifier.height(3.dp))
                PriceValue(state, typeface, scaleFactor = 1.05f)
                Spacer(modifier = GlanceModifier.height(3.dp))
                PriceChange(state.changePercentage, error, typeface)
            }

            Spacer(modifier = GlanceModifier.height(3.dp))

            TrendlineImage(
                prices = state.sparklinePrices,
                isPositive = state.changePercentage >= 0,
                widthDp = size.width,
                heightDp = (size.height - 76.dp).coerceAtLeast(44.dp),
                colorStyle = colorStyle,
                widgetTheme = widgetTheme,
                includeGradient = true,
                strokeWidthDp = 2.6f
            )
        }
    }

    /**
     * Large (4x2) layout: Full dashboard with header, bold price & change pill, and edge-to-edge sparkline.
     */
    @Composable
    private fun LargeLayout(
        state: PriceWidgetState.Available,
        size: DpSize,
        typeface: Typeface?,
        colorStyle: WidgetColorStyle,
        widgetTheme: WidgetTheme,
        error: Boolean
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            Column(
                modifier = GlanceModifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp)
            ) {
                Header(state.currency, state.intervalLabelResId, typeface)
                Spacer(modifier = GlanceModifier.height(4.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PriceValue(state, typeface, scaleFactor = 1.25f)
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    PriceChange(state.changePercentage, error, typeface)
                }
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            TrendlineImage(
                prices = state.sparklinePrices,
                isPositive = state.changePercentage >= 0,
                widthDp = size.width,
                heightDp = (size.height - 66.dp).coerceAtLeast(60.dp),
                colorStyle = colorStyle,
                widgetTheme = widgetTheme,
                includeGradient = true,
                strokeWidthDp = 2.6f
            )
        }
    }

    @Composable
    private fun TrendlineImage(
        prices: List<Double>,
        isPositive: Boolean,
        widthDp: Dp,
        heightDp: Dp,
        colorStyle: WidgetColorStyle,
        widgetTheme: WidgetTheme,
        includeGradient: Boolean = true,
        strokeWidthDp: Float = 2.6f
    ) {
        val context = LocalContext.current
        val widthPx = TrendlineBitmapRenderer.dpToPx(context, widthDp.value).toInt().coerceAtLeast(10)
        val heightPx = TrendlineBitmapRenderer.dpToPx(context, heightDp.value).toInt().coerceAtLeast(10)

        val strokeColor = TrendlineBitmapRenderer.resolveTrendColor(
            context = context,
            isPositive = isPositive,
            colorStyle = colorStyle,
            widgetTheme = widgetTheme
        )

        val bitmap = TrendlineBitmapRenderer.createTrendlineBitmap(
            context = context,
            prices = prices,
            strokeColor = strokeColor,
            widthPx = widthPx,
            heightPx = heightPx,
            fillStyle = if (includeGradient) TrendlineFillStyle.SUBTLE else TrendlineFillStyle.NONE,
            strokeWidthDp = strokeWidthDp,
            drawDashedBaseline = true,
            drawLiveDot = true,
            neonGlow = false
        )

        Image(
            provider = ImageProvider(bitmap),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = GlanceModifier.fillMaxWidth().height(heightDp)
        )
    }

    @Composable
    private fun Header(
        currency: String,
        intervalLabel: Int,
        typeface: Typeface?
    ) {
        val context = LocalContext.current
        val headerText = "/ ${currency.uppercase()} ・ ${context.getString(intervalLabel)}"
        val fontSize = WidgetBitmapUtils.getWidgetSecondaryFontSize()

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(R.drawable.rounded_currency_bitcoin_24),
                contentDescription = context.getString(R.string.bitcoin_icon_description),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.secondary),
                modifier = GlanceModifier.size(14.dp)
            )
            Spacer(modifier = GlanceModifier.width(2.dp))
            if (typeface != null) {
                val headerBitmap = WidgetBitmapUtils.createTextBitmap(
                    context = context,
                    text = headerText,
                    fontSizeSp = fontSize,
                    typeface = typeface
                )
                Image(
                    provider = ImageProvider(headerBitmap),
                    contentDescription = headerText,
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.secondary)
                )
            } else {
                Text(
                    text = headerText,
                    style = TextStyle(
                        color = GlanceTheme.colors.secondary,
                        fontSize = fontSize.sp
                    )
                )
            }
        }
    }

    @Composable
    private fun PriceValue(
        state: PriceWidgetState.Available,
        typeface: Typeface?,
        scaleFactor: Float = 1.0f
    ) {
        val context = LocalContext.current
        val parts = FormatUtils.formatPriceParts(state.price, state.currency)
        val priceSize = WidgetPriceSize.fromKey(state.priceSizeKey)
        val (basePriceSp, baseSymbolSp) = WidgetBitmapUtils.getWidgetPriceFontSize(
            parts.integerPart + parts.decimalPart,
            parts.symbol,
            priceSize.scaleFactor * scaleFactor
        )
        val integerFontSize = basePriceSp * 1.08f
        val decimalFontSize = basePriceSp * 0.68f
        val symbolFontSize = baseSymbolSp * 0.92f

        Row(verticalAlignment = Alignment.Bottom) {
            if (parts.symbolAtStart) {
                RenderPriceText(context, parts.symbol, symbolFontSize, typeface)
                Spacer(modifier = GlanceModifier.width(1.5.dp))
                RenderPriceText(context, parts.integerPart, integerFontSize, typeface)
                if (parts.decimalPart.isNotEmpty()) {
                    RenderPriceText(context, parts.decimalPart, decimalFontSize, typeface)
                }
            } else {
                RenderPriceText(context, parts.integerPart, integerFontSize, typeface)
                if (parts.decimalPart.isNotEmpty()) {
                    RenderPriceText(context, parts.decimalPart, decimalFontSize, typeface)
                }
                Spacer(modifier = GlanceModifier.width(2.dp))
                RenderPriceText(context, parts.symbol, symbolFontSize, typeface)
            }
        }
    }

    @Composable
    private fun RenderPriceText(
        context: Context,
        text: String,
        fontSizeSp: Float,
        typeface: Typeface?
    ) {
        if (typeface != null) {
            val bitmap = WidgetBitmapUtils.createTextBitmap(
                context = context,
                text = text,
                fontSizeSp = fontSizeSp,
                typeface = typeface
            )
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = text,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
            )
        } else {
            Text(
                text = text,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = fontSizeSp.sp,
                    fontWeight = androidx.glance.text.FontWeight.Bold
                )
            )
        }
    }

    @SuppressLint("DefaultLocale")
    @Composable
    private fun PriceChange(changePercentage: Double, error: Boolean, typeface: Typeface?) {
        val (containerColor, contentColor, arrowSymbol) = if (changePercentage > 0) {
            Triple(GlanceTheme.colors.primaryContainer, GlanceTheme.colors.onPrimaryContainer, "↑")
        } else if (changePercentage < 0) {
            Triple(GlanceTheme.colors.errorContainer, GlanceTheme.colors.onErrorContainer, "↓")
        } else {
            Triple(GlanceTheme.colors.surfaceVariant, GlanceTheme.colors.onSurfaceVariant, "•")
        }

        val formatPattern = "%.2f%%"
        val changeText = "$arrowSymbol " + String.format(java.util.Locale.ROOT, formatPattern, kotlin.math.abs(changePercentage))

        Box(
            modifier = GlanceModifier
                .background(containerColor)
                .cornerRadius(10.dp)
                .padding(horizontal = 7.dp, vertical = 2.5.dp),
            contentAlignment = Alignment.Center
        ) {
            if (typeface != null) {
                val context = LocalContext.current
                val textBitmap = WidgetBitmapUtils.createTextBitmap(
                    context = context,
                    text = changeText,
                    fontSizeSp = 11.5f,
                    typeface = typeface
                )
                Image(
                    provider = ImageProvider(textBitmap),
                    contentDescription = changeText,
                    colorFilter = ColorFilter.tint(contentColor)
                )
            } else {
                Text(
                    text = changeText,
                    style = TextStyle(
                        color = contentColor,
                        fontSize = 11.5.sp,
                        fontWeight = androidx.glance.text.FontWeight.Bold
                    )
                )
            }
        }
    }

    @Composable
    private fun ErrorUI(state: PriceWidgetState.Error, typeface: Typeface?) {
        val context = LocalContext.current
        val errorLabel = "${context.getString(R.string.error)}:"
        val message = state.message

        Column(
            modifier = GlanceModifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (typeface != null) {
                val labelBitmap = WidgetBitmapUtils.createTextBitmap(
                    context = context,
                    text = errorLabel,
                    fontSizeSp = 12f,
                    typeface = typeface
                )
                val messageBitmap = WidgetBitmapUtils.createTextBitmap(
                    context = context,
                    text = message,
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
                    contentDescription = message,
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
                )
            } else {
                Text(
                    text = errorLabel,
                    style = TextStyle(color = GlanceTheme.colors.error, fontSize = 12.sp)
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = message,
                    style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 12.sp)
                )
            }
        }
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 180, heightDp = 80)
    @Composable
    private fun PreviewCompact() {
        val state = PriceWidgetState.Available(
            price = 95432.10,
            changePercentage = 2.45,
            intervalLabelResId = R.string.interval_24h,
            currency = "USD",
            sparklinePrices = PLACEHOLDER_SPARKLINE_PRICES
        )
        WidgetContent(state, COMPACT)
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 280, heightDp = 80)
    @Composable
    private fun PreviewWideSlim() {
        val state = PriceWidgetState.Available(
            price = 95432.10,
            changePercentage = -1.82,
            intervalLabelResId = R.string.interval_7d,
            currency = "USD",
            sparklinePrices = PLACEHOLDER_SPARKLINE_PRICES
        )
        WidgetContent(state, WIDE_SLIM)
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 180, heightDp = 180)
    @Composable
    private fun PreviewMedium() {
        val state = PriceWidgetState.Available(
            price = 95432.10,
            changePercentage = 3.14,
            intervalLabelResId = R.string.interval_24h,
            currency = "USD",
            sparklinePrices = PLACEHOLDER_SPARKLINE_PRICES
        )
        WidgetContent(state, MEDIUM)
    }

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 300, heightDp = 180)
    @Composable
    private fun PreviewLarge() {
        val state = PriceWidgetState.Available(
            price = 95432.10,
            changePercentage = 2.45,
            intervalLabelResId = R.string.interval_7d,
            currency = "USD",
            sparklinePrices = PLACEHOLDER_SPARKLINE_PRICES
        )
        WidgetContent(state, LARGE)
    }
}
```

---

### File 2: `TrendlinePriceWidgetReceiver.kt`

Path: `app/src/main/java/com/jcoronado/minimalbitcoinwidget/widgets/glance/TrendlinePriceWidgetReceiver.kt`

```kotlin
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
```

---

### File 3: `TrendlineBitmapRenderer.kt`

Path: `app/src/main/java/com/jcoronado/minimalbitcoinwidget/widgets/glance/TrendlineBitmapRenderer.kt`

```kotlin
package com.jcoronado.minimalbitcoinwidget.widgets.glance

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Shader
import android.util.TypedValue
import com.jcoronado.minimalbitcoinwidget.ui.components.PLACEHOLDER_SPARKLINE_PRICES
import com.jcoronado.minimalbitcoinwidget.ui.components.SparklineScaleCalculator

/**
 * Gradient fill style for the trendline chart.
 */
enum class TrendlineFillStyle {
    SUBTLE, // Soft linear gradient fade
    NONE
}

/**
 * High-performance off-screen Bitmap renderer for sparkline trendlines in Glance widgets.
 * Supports smooth Catmull-Rom splines, dashed baselines, live glowing dots, and volumetric gradients.
 */
object TrendlineBitmapRenderer {

    const val DEFAULT_STROKE_WIDTH_DP = 2.5f

    /**
     * Converts DP to physical pixels for the current display metrics.
     */
    fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }

    /**
     * Resolves the trendline line color adhering to MBW theme settings and dynamic colors.
     */
    fun resolveTrendColor(
        context: Context,
        isPositive: Boolean,
        colorStyle: com.jcoronado.minimalbitcoinwidget.classes.WidgetColorStyle,
        widgetTheme: com.jcoronado.minimalbitcoinwidget.classes.WidgetTheme
    ): Int {
        val isDark = when (widgetTheme) {
            com.jcoronado.minimalbitcoinwidget.classes.WidgetTheme.DARK -> true
            com.jcoronado.minimalbitcoinwidget.classes.WidgetTheme.LIGHT -> false
            com.jcoronado.minimalbitcoinwidget.classes.WidgetTheme.SYSTEM -> {
                val nightModeFlags = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }

        return if (colorStyle == com.jcoronado.minimalbitcoinwidget.classes.WidgetColorStyle.SOLID) {
            if (isDark) {
                if (isPositive) 0xFF36B37E.toInt() else 0xFFFF5630.toInt()
            } else {
                if (isPositive) 0xFF00875A.toInt() else 0xFFDE350B.toInt()
            }
        } else {
            val colorRes = if (isDark) {
                if (isPositive) com.jcoronado.minimalbitcoinwidget.R.color.widget_dynamic_dark_primary
                else com.jcoronado.minimalbitcoinwidget.R.color.widget_dynamic_dark_error
            } else {
                if (isPositive) com.jcoronado.minimalbitcoinwidget.R.color.widget_dynamic_light_primary
                else com.jcoronado.minimalbitcoinwidget.R.color.widget_dynamic_light_error
            }
            androidx.core.content.ContextCompat.getColor(context, colorRes)
        }
    }

    /**
     * Backwards-compatible overload for existing calls.
     */
    fun createTrendlineBitmap(
        context: Context,
        prices: List<Double>,
        strokeColor: Int,
        widthPx: Int,
        heightPx: Int,
        includeGradient: Boolean = true,
        strokeWidthDp: Float = DEFAULT_STROKE_WIDTH_DP
    ): Bitmap {
        return createTrendlineBitmap(
            context = context,
            prices = prices,
            strokeColor = strokeColor,
            widthPx = widthPx,
            heightPx = heightPx,
            fillStyle = if (includeGradient) TrendlineFillStyle.SUBTLE else TrendlineFillStyle.NONE,
            strokeWidthDp = strokeWidthDp,
            drawDashedBaseline = false,
            drawLiveDot = false,
            neonGlow = false
        )
    }

    /**
     * Generates an off-screen [Bitmap] with advanced styling:
     * - Smooth Catmull-Rom cubic spline
     * - Optional dashed opening baseline
     * - Optional glowing real-time endpoint dot
     * - Volumetric / subtle / ambient gradient fills
     * - Neon outer stroke glow
     */
    fun createTrendlineBitmap(
        context: Context,
        prices: List<Double>,
        strokeColor: Int,
        widthPx: Int,
        heightPx: Int,
        fillStyle: TrendlineFillStyle = TrendlineFillStyle.SUBTLE,
        strokeWidthDp: Float = DEFAULT_STROKE_WIDTH_DP,
        drawDashedBaseline: Boolean = false,
        drawLiveDot: Boolean = false,
        neonGlow: Boolean = false,
        smoothingRounds: Int = 2
    ): Bitmap {
        val w = widthPx.coerceAtLeast(10)
        val h = heightPx.coerceAtLeast(10)

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val rawPrices = if (prices.size < 2) PLACEHOLDER_SPARKLINE_PRICES else prices

        // Gaussian 3-point smoothing preserving endpoints
        val smoothPrices = if (rawPrices.size < 5) {
            rawPrices
        } else {
            val n = rawPrices.size
            val smoothed = rawPrices.toDoubleArray()
            repeat(smoothingRounds.coerceIn(1, 4)) {
                val temp = smoothed.clone()
                for (i in 1 until n - 1) {
                    smoothed[i] = 0.25 * temp[i - 1] + 0.50 * temp[i] + 0.25 * temp[i + 1]
                }
                smoothed[0] = rawPrices.first()
                smoothed[n - 1] = rawPrices.last()
            }
            smoothed.toList()
        }

        val strokeWidthPx = dpToPx(context, strokeWidthDp)
        val verticalPadding = strokeWidthPx * 1.6f
        val usableHeight = (h.toFloat() - (verticalPadding * 2f)).coerceAtLeast(1f)

        val minPrice = smoothPrices.minOrNull() ?: 0.0
        val maxPrice = smoothPrices.maxOrNull() ?: 1.0

        val points = smoothPrices.indices.map { i ->
            val x = (i.toFloat() / (smoothPrices.size - 1)) * w.toFloat()
            val yFraction = SparklineScaleCalculator.calculateYFraction(
                price = smoothPrices[i],
                minPrice = minPrice,
                maxPrice = maxPrice,
                verticalPaddingFraction = SparklineScaleCalculator.DEFAULT_VERTICAL_PADDING_FRACTION
            )
            val y = h.toFloat() - verticalPadding - (yFraction * usableHeight)
            PointF(x, y)
        }

        // 1. Draw dashed opening reference baseline if requested
        if (drawDashedBaseline && points.isNotEmpty()) {
            val baselineY = points.first().y
            val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = dpToPx(context, 1.2f)
                color = Color.argb(
                    (Color.alpha(strokeColor) * 0.40f).toInt(),
                    Color.red(strokeColor),
                    Color.green(strokeColor),
                    Color.blue(strokeColor)
                )
                pathEffect = DashPathEffect(
                    floatArrayOf(dpToPx(context, 4.5f), dpToPx(context, 3.5f)),
                    0f
                )
            }
            canvas.drawLine(0f, baselineY, w.toFloat(), baselineY, dashPaint)
        }

        // 2. Build Catmull-Rom cubic spline path
        val linePath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points[0].x, points[0].y)
                val n = points.size
                val alpha = 0.20f

                for (i in 0 until n - 1) {
                    val p0 = points[if (i == 0) 0 else i - 1]
                    val p1 = points[i]
                    val p2 = points[i + 1]
                    val p3 = points[if (i + 2 >= n) n - 1 else i + 2]

                    val cp1X = p1.x + (p2.x - p0.x) * alpha
                    val cp1Y = p1.y + (p2.y - p0.y) * alpha

                    val cp2X = p2.x - (p3.x - p1.x) * alpha
                    val cp2Y = p2.y - (p3.y - p1.y) * alpha

                    cubicTo(cp1X, cp1Y, cp2X, cp2Y, p2.x, p2.y)
                }
            }
        }

        // 3. Draw gradient fill under trendline
        if (fillStyle != TrendlineFillStyle.NONE && points.size >= 2) {
            val fillPath = Path(linePath).apply {
                lineTo(w.toFloat(), h.toFloat())
                lineTo(0f, h.toFloat())
                close()
            }

            when (fillStyle) {
                TrendlineFillStyle.SUBTLE -> {
                    val topAlpha = (Color.alpha(strokeColor) * 0.25f).toInt().coerceIn(0, 255)
                    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.FILL
                        shader = LinearGradient(
                            0f, 0f, 0f, h.toFloat(),
                            Color.argb(topAlpha, Color.red(strokeColor), Color.green(strokeColor), Color.blue(strokeColor)),
                            Color.TRANSPARENT,
                            Shader.TileMode.CLAMP
                        )
                    }
                    canvas.drawPath(fillPath, fillPaint)
                }
                TrendlineFillStyle.NONE -> Unit
            }
        }

        // 4. Draw outer neon glow stroke if enabled
        if (neonGlow) {
            val glowStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = strokeWidthPx * 2.4f
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                color = Color.argb(
                    (Color.alpha(strokeColor) * 0.30f).toInt(),
                    Color.red(strokeColor),
                    Color.green(strokeColor),
                    Color.blue(strokeColor)
                )
            }
            canvas.drawPath(linePath, glowStrokePaint)
        }

        // 5. Anti-aliased main stroke line
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthPx
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            this.color = strokeColor
        }
        canvas.drawPath(linePath, strokePaint)

        // 6. Draw glowing live endpoint dot if requested
        if (drawLiveDot && points.isNotEmpty()) {
            val lastPoint = points.last()
            // Pull in slightly from right border to avoid edge-clipping
            val dotX = (lastPoint.x - dpToPx(context, 3.5f)).coerceAtLeast(0f)
            val dotY = lastPoint.y

            // Outer halo
            val haloRadius = if (neonGlow) dpToPx(context, 8.0f) else dpToPx(context, 6.0f)
            val haloAlpha = if (neonGlow) 0.50f else 0.35f
            val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.argb(
                    (Color.alpha(strokeColor) * haloAlpha).toInt(),
                    Color.red(strokeColor),
                    Color.green(strokeColor),
                    Color.blue(strokeColor)
                )
            }
            canvas.drawCircle(dotX, dotY, haloRadius, haloPaint)

            // Solid core
            val coreRadius = dpToPx(context, 3.2f)
            val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = strokeColor
            }
            canvas.drawCircle(dotX, dotY, coreRadius, corePaint)

            // Pinpoint center highlight
            val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.WHITE
            }
            canvas.drawCircle(dotX, dotY, dpToPx(context, 1.2f), pinPaint)
        }

        return bitmap
    }
}
```

---

### File 4: `trendline_price_widget_info.xml`

Path: `app/src/main/res/xml/trendline_price_widget_info.xml`

```xml
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:description="@string/chart_widget_picker_description"
    android:previewImage="@drawable/glance_widget_preview"
    android:minWidth="140dp"
    android:minHeight="120dp"
    android:resizeMode="horizontal|vertical"
    android:minResizeWidth="140dp"
    android:minResizeHeight="60dp"
    android:widgetCategory="home_screen" />
```

---

### File 5: `trendline_price_widget_info.xml` (API 31+)

Path: `app/src/main/res/xml-v31/trendline_price_widget_info.xml`

```xml
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:description="@string/chart_widget_picker_description"
    android:previewLayout="@layout/trendline_widget_preview"
    android:minWidth="140dp"
    android:minHeight="120dp"
    android:resizeMode="horizontal|vertical"
    android:targetCellWidth="2"
    android:targetCellHeight="2"
    android:minResizeWidth="140dp"
    android:minResizeHeight="60dp"
    android:maxResizeWidth="500dp"
    android:maxResizeHeight="300dp"
    android:widgetCategory="home_screen" />
```

---

### File 6: `TrendlineBitmapRendererTest.kt`

Path: `app/src/test/java/com/jcoronado/minimalbitcoinwidget/TrendlineBitmapRendererTest.kt`

```kotlin
package com.jcoronado.minimalbitcoinwidget

import androidx.compose.ui.unit.dp
import com.jcoronado.minimalbitcoinwidget.classes.WidgetColorStyle
import com.jcoronado.minimalbitcoinwidget.classes.WidgetTheme
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidgetState
import com.jcoronado.minimalbitcoinwidget.widgets.glance.TrendlinePriceWidget
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for TrendlinePriceWidget and PriceWidgetState sparkline serialization.
 */
class TrendlineBitmapRendererTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    @Test
    fun `TrendlinePriceWidget responsive breakpoint sizes match specifications`() {
        assertEquals(140.dp, TrendlinePriceWidget.COMPACT.width)
        assertEquals(60.dp, TrendlinePriceWidget.COMPACT.height)

        assertEquals(260.dp, TrendlinePriceWidget.WIDE_SLIM.width)
        assertEquals(60.dp, TrendlinePriceWidget.WIDE_SLIM.height)

        assertEquals(160.dp, TrendlinePriceWidget.MEDIUM.width)
        assertEquals(140.dp, TrendlinePriceWidget.MEDIUM.height)

        assertEquals(260.dp, TrendlinePriceWidget.LARGE.width)
        assertEquals(140.dp, TrendlinePriceWidget.LARGE.height)
    }

    @Test
    fun `PriceWidgetState Available serializes and deserializes sparklinePrices successfully`() {
        val testPrices = listOf(92000.0, 93500.0, 91200.0, 95400.0)
        val state = PriceWidgetState.Available(
            price = 95400.0,
            changePercentage = 3.25,
            intervalLabelResId = R.string.interval_24h,
            currency = "USD",
            fontKey = "app_default",
            priceSizeKey = "default",
            colorStyleKey = "dynamic",
            themeKey = "system",
            sparklinePrices = testPrices
        )

        val encoded = json.encodeToString(PriceWidgetState.serializer(), state)
        assertTrue(encoded.contains("sparklinePrices"))

        val decoded = json.decodeFromString(PriceWidgetState.serializer(), encoded)
        assertTrue(decoded is PriceWidgetState.Available)
        val decodedAvailable = decoded as PriceWidgetState.Available
        assertEquals(testPrices, decodedAvailable.sparklinePrices)
        assertEquals(95400.0, decodedAvailable.price, 0.001)
        assertEquals(3.25, decodedAvailable.changePercentage, 0.001)
    }

    @Test
    fun `PriceWidgetState Available deserializes older JSON missing sparklinePrices with default empty list`() {
        val legacyJson = """
            {
                "type": "com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidgetState.Available",
                "price": 65000.0,
                "changePercentage": -1.5,
                "intervalLabelResId": 2131689500,
                "currency": "USD"
            }
        """.trimIndent()

        val decoded = json.decodeFromString(PriceWidgetState.serializer(), legacyJson)
        assertTrue(decoded is PriceWidgetState.Available)
        val decodedAvailable = decoded as PriceWidgetState.Available
        assertEquals(65000.0, decodedAvailable.price, 0.001)
        assertEquals(-1.5, decodedAvailable.changePercentage, 0.001)
        assertEquals(emptyList<Double>(), decodedAvailable.sparklinePrices)
    }

    @Test
    fun `PriceWidgetState Error retains lastValidState with sparklinePrices`() {
        val validState = PriceWidgetState.Available(
            price = 90000.0,
            changePercentage = 2.0,
            intervalLabelResId = R.string.interval_7d,
            currency = "USD",
            sparklinePrices = listOf(88000.0, 90000.0)
        )
        val errorState = PriceWidgetState.Error(
            message = "Network Error",
            lastValidState = validState
        )

        val encoded = json.encodeToString(PriceWidgetState.serializer(), errorState)
        val decoded = json.decodeFromString(PriceWidgetState.serializer(), encoded)
        assertTrue(decoded is PriceWidgetState.Error)
        val decodedError = decoded as PriceWidgetState.Error
        assertEquals("Network Error", decodedError.message)
        assertEquals(listOf(88000.0, 90000.0), decodedError.lastValidState?.sparklinePrices)
    }

    @Test
    fun `formatPriceParts correctly separates price integer and decimals`() {
        val parts = com.jcoronado.minimalbitcoinwidget.utils.FormatUtils.formatPriceParts(82239.51, "USD")
        assertTrue(parts.integerPart.contains("82"))
        assertTrue(parts.decimalPart.contains("51"))
        assertTrue(parts.symbol.isNotEmpty())
    }

    @Test
    fun `Trendline widget classes instantiate properly`() {
        val expressive = com.jcoronado.minimalbitcoinwidget.widgets.glance.TrendlinePriceWidget()
        assertNotNull(expressive)
    }
}
```

---

## 4. Modifications to Existing Files

### 4.1. `FormatUtils.kt`

Path: `app/src/main/java/com/jcoronado/minimalbitcoinwidget/utils/FormatUtils.kt`

Add data class:

```kotlin
data class FormattedPriceParts(
    val symbol: String,
    val integerPart: String,
    val decimalPart: String,
    val symbolAtStart: Boolean
)
```

Add method to `FormatUtils` object:

```kotlin
    /**
     * Splits price into integer and decimal parts along with currency symbol,
     * respecting locale-specific grouping and decimal separators.
     */
    fun formatPriceParts(price: Double, currencyCode: String): FormattedPriceParts {
        val currency = try {
            Currency.getInstance(currencyCode.uppercase(Locale.ROOT))
        } catch (_: Exception) {
            Currency.getInstance("USD")
        }

        val nativeSymbol = getNativeSymbol(currency)
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        var separator = "."

        if (formatter is DecimalFormat) {
            val symbols = formatter.decimalFormatSymbols
            symbols.currencySymbol = nativeSymbol
            formatter.decimalFormatSymbols = symbols

            val fractionDigits = if (currency.defaultFractionDigits >= 0) {
                currency.defaultFractionDigits
            } else {
                2
            }
            formatter.minimumFractionDigits = fractionDigits
            formatter.maximumFractionDigits = fractionDigits
            separator = symbols.decimalSeparator.toString()
        }

        val fullFormattedString = formatter.format(price)
        val isSymbolAtStart = fullFormattedString.trim().startsWith(nativeSymbol)
        val priceOnly = fullFormattedString.replace(nativeSymbol, "").trim()

        val lastSepIndex = priceOnly.lastIndexOf(separator)
        return if (lastSepIndex != -1) {
            FormattedPriceParts(
                symbol = nativeSymbol,
                integerPart = priceOnly.substring(0, lastSepIndex),
                decimalPart = priceOnly.substring(lastSepIndex),
                symbolAtStart = isSymbolAtStart
            )
        } else {
            FormattedPriceParts(
                symbol = nativeSymbol,
                integerPart = priceOnly,
                decimalPart = "",
                symbolAtStart = isSymbolAtStart
            )
        }
    }
```

---

### 4.2. `PriceWidgetState.kt`

Path: `app/src/main/java/com/jcoronado/minimalbitcoinwidget/widgets/glance/PriceWidgetState.kt`

Add `sparklinePrices: List<Double> = emptyList()` to `PriceWidgetState.Available`:

```kotlin
    @Serializable
    data class Available(
        val price: Double,
        val changePercentage: Double,
        val intervalLabelResId: Int,
        val currency: String,
        val fontKey: String = "app_default",
        val priceSizeKey: String = "default",
        val colorStyleKey: String = "dynamic",
        val themeKey: String = "system",
        val sparklinePrices: List<Double> = emptyList()
    ) : PriceWidgetState()
```

---

### 4.3. `AndroidManifest.xml`

Path: `app/src/main/AndroidManifest.xml`

Declare `TrendlinePriceWidgetReceiver` inside `<application>`:

```xml
        <!-- Glance Trendline Price Widget Code (Expressive) -->
        <receiver
            android:name=".widgets.glance.TrendlinePriceWidgetReceiver"
            android:label="@string/chart_widget_picker_name"
            android:exported="true">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>

            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/trendline_price_widget_info" />
        </receiver>
```

---

### 4.4. `PriceViewModel.kt`

Path: `app/src/main/java/com/jcoronado/minimalbitcoinwidget/viewmodels/PriceViewModel.kt`

1. Import:

```kotlin
import com.jcoronado.minimalbitcoinwidget.widgets.glance.TrendlinePriceWidget
import com.jcoronado.minimalbitcoinwidget.widgets.glance.TrendlinePriceWidgetReceiver
```

2. When instantiating `PriceWidgetState.Available` in `loadInitialData()`:

```kotlin
sparklinePrices = mockData.getSparklineForInterval(selectedInterval)
```

and

```kotlin
sparklinePrices = priceData.getSparklineForInterval(selectedInterval)
```

3. In `updateWidgetPreview(context: Context)`:

```kotlin
manager.setWidgetPreviews(PriceWidgetReceiver::class)
manager.setWidgetPreviews(TrendlinePriceWidgetReceiver::class)
```

4. In `updateGlanceWidgets(context: Context, state: PriceWidgetState)`:

```kotlin
    // Trendline Widget
    val trendlineGlanceIds = manager.getGlanceIds(TrendlinePriceWidget::class.java)
    trendlineGlanceIds.forEach { glanceId ->
        updateAppWidgetState(context, PriceWidgetStateDefinition, glanceId) {
            state
        }
    }
    TrendlinePriceWidget().updateAll(context)
```

---

### 4.5. `PriceUpdateWorker.kt`

Path: `app/src/main/java/com/jcoronado/minimalbitcoinwidget/workers/PriceUpdateWorker.kt`

1. Import:

```kotlin
import com.jcoronado.minimalbitcoinwidget.widgets.glance.TrendlinePriceWidget
```

2. In `handleError(e: Exception)`:

```kotlin
        val trendlineIds = manager.getGlanceIds(TrendlinePriceWidget::class.java)
        ...
        } else if (trendlineIds.isNotEmpty()) {
            val currentState: PriceWidgetState = TrendlinePriceWidget().getAppWidgetState(context, trendlineIds.first())
            lastValid = when (currentState) {
                is PriceWidgetState.Available -> currentState
                is PriceWidgetState.Error -> currentState.lastValidState
                else -> null
            }
        }
```

3. In `cancel(context: Context)`:

```kotlin
        val trendlineIds = glanceManager.getGlanceIds(TrendlinePriceWidget::class.java)
        ...
        if (glanceIds.isEmpty() && trendlineIds.isEmpty() && legacyIds.isEmpty()) {
            Log.d(LOG_TAG, "No widgets left. Cancelling work.")
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
```

---

### 4.6. Localization Across All 9 `strings.xml` Files

1. **`app/src/main/res/values/strings.xml`**:

```xml
    <string name="chart_widget_picker_name" translatable="false">Trendline Widget (Beta)</string>
    <string name="chart_widget_picker_description">Quickly view the current Bitcoin price with trendline</string>
```

2. **`app/src/main/res/values-de/strings.xml`**:

```xml
    <string name="chart_widget_picker_description">Bitcoin-Preis mit Trendlinie und prozentualer Veränderung schnell anzeigen</string>
```

3. **`app/src/main/res/values-es/strings.xml`**:

```xml
    <string name="chart_widget_picker_description">Consulta rápidamente el precio de Bitcoin con línea de tendencia</string>
```

4. **`app/src/main/res/values-fr/strings.xml`**:

```xml
    <string name="chart_widget_picker_description">Consultez rapidement le prix du Bitcoin avec courbe de tendance</string>
```

5. **`app/src/main/res/values-fr-rFR/strings.xml`**:

```xml
    <string name="chart_widget_picker_description">Consultez rapidement le prix du Bitcoin avec courbe de tendance</string>
```

6. **`app/src/main/res/values-it/strings.xml`**:

```xml
    <string name="chart_widget_picker_description">Visualizza rapidamente il prezzo di Bitcoin con linea di tendenza</string>
```

7. **`app/src/main/res/values-pt/strings.xml`**:

```xml
    <string name="chart_widget_picker_description">Veja rapidamente o preço do Bitcoin com linha de tendência</string>
```

8. **`app/src/main/res/values-pt-rBR/strings.xml`**:

```xml
    <string name="chart_widget_picker_description">Veja rapidamente o preço do Bitcoin com linha de tendência</string>
```

9. **`app/src/main/res/values-tr/strings.xml`**:

```xml
    <string name="chart_widget_picker_description">Trend çizgisiyle güncel Bitcoin fiyatını hızla görüntüleyin</string>
```

---

### 4.7. `AGENTS.md`

Path: `AGENTS.md`

Update the widgets directory note under Project Structure:

```markdown
- **`widgets/`**: Contains widget implementations. Divided into Glance widgets (`PriceWidget`, `TrendlinePriceWidget`) and legacy implementations (`LegacyPriceWidget`).
```

---

## 5. Verification Commands

Run unit tests and assemble the debug build:

```bash
./gradlew test assembleDebug
```

Ensure 100% test pass rate with 0 compilation errors.
