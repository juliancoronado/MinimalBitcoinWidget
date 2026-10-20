package com.jcoronado.minimalbitcoinwidget

import androidx.compose.ui.unit.dp
import com.jcoronado.minimalbitcoinwidget.classes.WidgetColorStyle
import com.jcoronado.minimalbitcoinwidget.classes.WidgetTheme
import com.jcoronado.minimalbitcoinwidget.widgets.glance.ChartPriceWidget
import com.jcoronado.minimalbitcoinwidget.widgets.glance.PriceWidgetState
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for ChartPriceWidget and PriceWidgetState sparkline serialization.
 */
class ChartBitmapRendererTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    @Test
    fun `ChartPriceWidget responsive breakpoint sizes match specifications`() {
        assertEquals(140.dp, ChartPriceWidget.COMPACT.width)
        assertEquals(60.dp, ChartPriceWidget.COMPACT.height)

        assertEquals(260.dp, ChartPriceWidget.WIDE_SLIM.width)
        assertEquals(60.dp, ChartPriceWidget.WIDE_SLIM.height)

        assertEquals(180.dp, ChartPriceWidget.MEDIUM.width)
        assertEquals(184.dp, ChartPriceWidget.MEDIUM.height)

        assertEquals(260.dp, ChartPriceWidget.LARGE.width)
        assertEquals(184.dp, ChartPriceWidget.LARGE.height)
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
        assertNotNull(decodedError.lastValidState)
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
    fun `Chart widget classes instantiate properly`() {
        val expressive = com.jcoronado.minimalbitcoinwidget.widgets.glance.ChartPriceWidget()
        assertNotNull(expressive)
    }

    @Test
    fun `PreviewWidgetType entries contain valid string resources for Price and Chart`() {
        val entries = com.jcoronado.minimalbitcoinwidget.screens.PreviewWidgetType.entries
        assertEquals(2, entries.size)
        assertEquals(R.string.widget_preview_price, com.jcoronado.minimalbitcoinwidget.screens.PreviewWidgetType.PRICE.labelResId)
        assertEquals(R.string.widget_preview_chart, com.jcoronado.minimalbitcoinwidget.screens.PreviewWidgetType.CHART.labelResId)
    }

    @Test
    fun `isDarkTheme correctly evaluates explicit DARK and LIGHT widget theme settings`() {
        val darkResult = com.jcoronado.minimalbitcoinwidget.widgets.glance.ChartBitmapRenderer.isDarkTheme(null, WidgetTheme.DARK)
        val lightResult = com.jcoronado.minimalbitcoinwidget.widgets.glance.ChartBitmapRenderer.isDarkTheme(null, WidgetTheme.LIGHT)
        assertTrue(darkResult)
        org.junit.Assert.assertFalse(lightResult)
    }

    @Test
    fun `PriceWidgetState Available with 30D interval and empty sparklinePrices serializes properly`() {
        val state30d = PriceWidgetState.Available(
            price = 98500.0,
            changePercentage = 14.2,
            intervalLabelResId = R.string.interval_30d,
            currency = "USD",
            sparklinePrices = emptyList()
        )

        val encoded = json.encodeToString(PriceWidgetState.serializer(), state30d)
        val decoded = json.decodeFromString(PriceWidgetState.serializer(), encoded)
        assertTrue(decoded is PriceWidgetState.Available)
        val available = decoded as PriceWidgetState.Available
        assertEquals(R.string.interval_30d, available.intervalLabelResId)
        assertTrue(available.sparklinePrices.isEmpty())
    }

    @Test
    fun `downsamplePrices preserves small series without modification`() {
        val smallList = List(25) { it * 100.0 }
        val result = com.jcoronado.minimalbitcoinwidget.widgets.glance.ChartBitmapRenderer.downsamplePrices(smallList)
        assertEquals(25, result.size)
        assertEquals(smallList, result)
    }

    @Test
    fun `downsamplePrices downsamples 168-point 7D series to 35 points strictly preserving endpoints`() {
        val largeList = List(168) { i -> 80000.0 + (i * 25.0) }
        val result = com.jcoronado.minimalbitcoinwidget.widgets.glance.ChartBitmapRenderer.downsamplePrices(largeList, maxPoints = 42, targetPoints = 35)
        assertEquals(35, result.size)
        assertEquals(largeList.first(), result.first(), 0.0001)
        assertEquals(largeList.last(), result.last(), 0.0001)
    }
}
