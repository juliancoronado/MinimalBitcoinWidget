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
 * and an edge-to-edge off-screen rendered price chart (sparkline) across responsive sizes.
 */
class ChartPriceWidget : GlanceAppWidget() {

    companion object {
        val COMPACT = DpSize(140.dp, 60.dp)
        val WIDE_SLIM = DpSize(260.dp, 60.dp)
        val MEDIUM = DpSize(180.dp, 184.dp)
        val LARGE = DpSize(260.dp, 184.dp)
    }

    override val sizeMode = SizeMode.Exact

    override val previewSizeMode = SizeMode.Single

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
                        price = 52849.10,
                        changePercentage = 2.45,
                        intervalLabelResId = interval.labelResId,
                        currency = repository.getSelectedCurrency(),
                        fontKey = fontKey,
                        priceSizeKey = priceSizeKey,
                        colorStyleKey = colorStyleKey,
                        themeKey = themeKey,
                        sparklinePrices = if (selectedInterval == 2) emptyList() else PLACEHOLDER_SPARKLINE_PRICES
                    )
                }
            }
        }

        provideContent {
            WidgetContent(previewState, MEDIUM)
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
     * Compact (2x1) layout: Top metrics with edge-to-edge sparkline below,
     * or centered price info when chart data is unavailable.
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
        val is30d = state.intervalLabelResId == R.string.interval_30d
        val isUnavailable = is30d || state.sparklinePrices.size < 2

        if (isUnavailable) {
            Column(
                modifier = GlanceModifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(GlanceModifier.defaultWeight())
                Header(state.currency, state.intervalLabelResId, typeface)
                Spacer(modifier = GlanceModifier.height(3.dp))
                PriceValue(state, typeface, scaleFactor = 0.95f)
                Spacer(modifier = GlanceModifier.height(3.dp))
                PriceChange(state.changePercentage, error, typeface)
                Spacer(GlanceModifier.defaultWeight())
            }
        } else {
            Column(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 8.dp)
                ) {
                    Header(state.currency, state.intervalLabelResId, typeface)
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    PriceValue(state, typeface, scaleFactor = 0.95f)
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    PriceChange(state.changePercentage, error, typeface)
                }
                Spacer(modifier = GlanceModifier.height(2.dp))

                ChartImage(
                    prices = state.sparklinePrices,
                    isPositive = state.changePercentage >= 0,
                    widthDp = size.width,
                    heightDp = (size.height - 76.dp).coerceAtLeast(20.dp),
                    colorStyle = colorStyle,
                    widgetTheme = widgetTheme,
                    includeGradient = false,
                    strokeWidthDp = 2.0f,
                    isUnavailable = false,
                    typeface = typeface,
                    modifier = GlanceModifier.defaultWeight().fillMaxWidth()
                )
            }
        }
    }

    /**
     * Wide-Slim (4x1 / 3x1) layout: Left metrics + Right wide sparkline bleeding to edge,
     * or a balanced two-column ticker when chart data is unavailable.
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
        val is30d = state.intervalLabelResId == R.string.interval_30d
        val isUnavailable = is30d || state.sparklinePrices.size < 2

        if (isUnavailable) {
            Row(
                modifier = GlanceModifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Header(state.currency, state.intervalLabelResId, typeface)
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    PriceChange(state.changePercentage, error, typeface)
                }
                PriceValue(state, typeface, scaleFactor = 1.15f)
            }
        } else {
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

                ChartImage(
                    prices = state.sparklinePrices,
                    isPositive = state.changePercentage >= 0,
                    widthDp = size.width * 0.52f,
                    heightDp = size.height,
                    colorStyle = colorStyle,
                    widgetTheme = widgetTheme,
                    includeGradient = false,
                    strokeWidthDp = 2.2f,
                    isUnavailable = false,
                    typeface = typeface,
                    modifier = GlanceModifier.width(size.width * 0.52f).height(size.height)
                )
            }
        }
    }

    /**
     * Medium (2x2) layout: Stacked header, bold price, change pill, and edge-to-edge sparkline below,
     * or centered, well-proportioned price info when chart data is unavailable.
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
        val is30d = state.intervalLabelResId == R.string.interval_30d
        val isUnavailable = is30d || state.sparklinePrices.size < 2

        if (isUnavailable) {
            Column(
                modifier = GlanceModifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(GlanceModifier.defaultWeight())
                Header(state.currency, state.intervalLabelResId, typeface)
                Spacer(modifier = GlanceModifier.height(10.dp))
                PriceValue(state, typeface, scaleFactor = 1.0f)
                Spacer(modifier = GlanceModifier.height(10.dp))
                PriceChange(state.changePercentage, error, typeface)
                Spacer(GlanceModifier.defaultWeight())
            }
        } else {
            Column(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 12.dp)
                ) {
                    Header(state.currency, state.intervalLabelResId, typeface)
                    Spacer(modifier = GlanceModifier.height(3.dp))
                    PriceValue(state, typeface, scaleFactor = 1.0f)
                    Spacer(modifier = GlanceModifier.height(3.dp))
                    PriceChange(state.changePercentage, error, typeface)
                }

                Spacer(modifier = GlanceModifier.height(3.dp))

                ChartImage(
                    prices = state.sparklinePrices,
                    isPositive = state.changePercentage >= 0,
                    widthDp = size.width,
                    heightDp = (size.height - 86.dp).coerceAtLeast(44.dp),
                    colorStyle = colorStyle,
                    widgetTheme = widgetTheme,
                    includeGradient = false,
                    strokeWidthDp = 2.6f,
                    isUnavailable = false,
                    typeface = typeface,
                    modifier = GlanceModifier.defaultWeight().fillMaxWidth()
                )
            }
        }
    }

    /**
     * Large (4x2) layout: Full dashboard with header & pill row, large price, and edge-to-edge sparkline,
     * or spacious header & pill row with prominent centered price when chart data is unavailable.
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
        val is30d = state.intervalLabelResId == R.string.interval_30d
        val isUnavailable = is30d || state.sparklinePrices.size < 2

        if (isUnavailable) {
            Column(
                modifier = GlanceModifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Header(state.currency, state.intervalLabelResId, typeface)
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    PriceChange(state.changePercentage, error, typeface)
                }
                Spacer(GlanceModifier.defaultWeight())
                PriceValue(state, typeface, scaleFactor = 1.35f)
                Spacer(GlanceModifier.defaultWeight())
            }
        } else {
            Column(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp)
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Header(state.currency, state.intervalLabelResId, typeface)
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        PriceChange(state.changePercentage, error, typeface)
                    }
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    PriceValue(state, typeface, scaleFactor = 1.20f)
                }

                Spacer(modifier = GlanceModifier.height(4.dp))

                ChartImage(
                    prices = state.sparklinePrices,
                    isPositive = state.changePercentage >= 0,
                    widthDp = size.width,
                    heightDp = (size.height - 76.dp).coerceAtLeast(48.dp),
                    colorStyle = colorStyle,
                    widgetTheme = widgetTheme,
                    includeGradient = false,
                    strokeWidthDp = 2.6f,
                    isUnavailable = false,
                    typeface = typeface,
                    modifier = GlanceModifier.defaultWeight().fillMaxWidth()
                )
            }
        }
    }

    @Composable
    private fun ChartImage(
        prices: List<Double>,
        isPositive: Boolean,
        widthDp: Dp,
        heightDp: Dp,
        colorStyle: WidgetColorStyle,
        widgetTheme: WidgetTheme,
        includeGradient: Boolean = false,
        strokeWidthDp: Float = 2.6f,
        isUnavailable: Boolean = false,
        unavailableMessage: String? = null,
        typeface: Typeface? = null,
        modifier: GlanceModifier = GlanceModifier.fillMaxWidth().height(heightDp)
    ) {
        val context = LocalContext.current
        val widthPx = ChartBitmapRenderer.dpToPx(context, widthDp.value).toInt().coerceAtLeast(10)
        val heightPx = ChartBitmapRenderer.dpToPx(context, heightDp.value).toInt().coerceAtLeast(10)

        val unavailable = isUnavailable || prices.size < 2

        val bitmap = if (unavailable) {
            ChartBitmapRenderer.createUnavailableChartBitmap(
                context = context,
                widthPx = widthPx,
                heightPx = heightPx,
                widgetTheme = widgetTheme,
                message = unavailableMessage ?: context.getString(R.string.chart_unavailable_30d),
                typeface = typeface
            )
        } else {
            val strokeColor = ChartBitmapRenderer.resolveTrendColor(
                context = context,
                isPositive = isPositive,
                colorStyle = colorStyle,
                widgetTheme = widgetTheme
            )

            ChartBitmapRenderer.createChartBitmap(
                context = context,
                prices = prices,
                strokeColor = strokeColor,
                widthPx = widthPx,
                heightPx = heightPx,
                fillStyle = if (includeGradient) ChartFillStyle.SUBTLE else ChartFillStyle.NONE,
                strokeWidthDp = strokeWidthDp,
                drawDashedBaseline = true,
                drawLiveDot = false,
                neonGlow = false
            )
        }

        Image(
            provider = ImageProvider(bitmap),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = modifier
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
        val priceData = FormatUtils.formatPriceSeparated(state.price, state.currency)
        val priceSize = WidgetPriceSize.fromKey(state.priceSizeKey)
        val (priceFontSize, symbolFontSize) = WidgetBitmapUtils.getWidgetPriceFontSize(
            priceData,
            priceSize.scaleFactor * scaleFactor
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (priceData.symbolAtStart) {
                RenderPriceText(context, priceData.symbol, symbolFontSize, typeface)
                Spacer(modifier = GlanceModifier.width(2.dp))
                RenderPriceText(context, priceData.price, priceFontSize, typeface)
            } else {
                RenderPriceText(context, priceData.price, priceFontSize, typeface)
                Spacer(modifier = GlanceModifier.width(2.dp))
                RenderPriceText(context, priceData.symbol, symbolFontSize, typeface)
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
                    fontSize = fontSizeSp.sp
                )
            )
        }
    }

    @SuppressLint("DefaultLocale")
    @Composable
    private fun PriceChange(changePercentage: Double, error: Boolean, typeface: Typeface?) {
        val context = LocalContext.current
        val (containerColor, contentColor) = if (changePercentage > 0) {
            Pair(GlanceTheme.colors.primaryContainer, GlanceTheme.colors.onPrimaryContainer)
        } else if (changePercentage < 0) {
            Pair(GlanceTheme.colors.errorContainer, GlanceTheme.colors.onErrorContainer)
        } else {
            Pair(GlanceTheme.colors.surfaceVariant, GlanceTheme.colors.onSurfaceVariant)
        }

        val (iconRes, iconDesc) = if (changePercentage > 0) {
            Pair(
                R.drawable.rounded_trending_up_24,
                context.getString(R.string.trending_up_icon_description)
            )
        } else if (changePercentage < 0) {
            Pair(
                R.drawable.rounded_trending_down_24,
                context.getString(R.string.trending_down_icon_description)
            )
        } else {
            Pair(
                R.drawable.rounded_trending_flat_24,
                context.getString(R.string.trending_flat_icon_description)
            )
        }

        val formatPattern = "%.2f%%"
        val changeText = String.format(formatPattern, changePercentage)

        Box(
            modifier = GlanceModifier
                .background(containerColor)
                .cornerRadius(10.dp)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    provider = ImageProvider(iconRes),
                    contentDescription = iconDesc,
                    colorFilter = ColorFilter.tint(contentColor),
                    modifier = GlanceModifier.size(13.dp).padding(top = 1.dp)
                )
                Spacer(modifier = GlanceModifier.width(3.dp))
                if (typeface != null) {
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
                            fontSize = 11.5.sp
                        )
                    )
                }
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
            price = 52849.10,
            changePercentage = 2.45,
            intervalLabelResId = R.string.interval_7d,
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

    @Suppress("unused")
    @OptIn(ExperimentalGlancePreviewApi::class)
    @Preview(widthDp = 180, heightDp = 180)
    @Composable
    private fun PreviewMediumUnavailable30D() {
        val state = PriceWidgetState.Available(
            price = 95432.10,
            changePercentage = 5.21,
            intervalLabelResId = R.string.interval_30d,
            currency = "USD",
            sparklinePrices = emptyList()
        )
        WidgetContent(state, MEDIUM)
    }
}
