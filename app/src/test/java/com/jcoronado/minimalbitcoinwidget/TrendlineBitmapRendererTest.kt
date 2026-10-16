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

        assertEquals(180.dp, TrendlinePriceWidget.MEDIUM.width)
        assertEquals(184.dp, TrendlinePriceWidget.MEDIUM.height)

        assertEquals(260.dp, TrendlinePriceWidget.LARGE.width)
        assertEquals(184.dp, TrendlinePriceWidget.LARGE.height)
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
    fun `Trendline widget classes instantiate properly`() {
        val expressive = com.jcoronado.minimalbitcoinwidget.widgets.glance.TrendlinePriceWidget()
        assertNotNull(expressive)
    }

    @Test
    fun `PreviewWidgetType entries contain valid string resources for Price and Trendline`() {
        val entries = com.jcoronado.minimalbitcoinwidget.screens.PreviewWidgetType.entries
        assertEquals(2, entries.size)
        assertEquals(R.string.widget_preview_price, com.jcoronado.minimalbitcoinwidget.screens.PreviewWidgetType.PRICE.labelResId)
        assertEquals(R.string.widget_preview_trendline, com.jcoronado.minimalbitcoinwidget.screens.PreviewWidgetType.TRENDLINE.labelResId)
    }
}
