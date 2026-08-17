package com.jcoronado.minimalbitcoinwidget

import com.jcoronado.minimalbitcoinwidget.ui.components.SparklineScaleCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SparklineGraphTest {

    @Test
    fun `tiny price movement minus 0 point 20 percent in JPY expands across visual height with padding`() {
        val currentPrice = 9901380.88
        val startPrice = 9921223.33 // -0.20% move over 24h
        val minPrice = currentPrice
        val maxPrice = startPrice

        val startYFraction = SparklineScaleCalculator.calculateYFraction(startPrice, minPrice, maxPrice)
        val endYFraction = SparklineScaleCalculator.calculateYFraction(currentPrice, minPrice, maxPrice)

        // Midpoint Y fraction is exact 0.5 (center)
        val midYFraction = (startYFraction + endYFraction) / 2f
        assertEquals(0.5f, midYFraction, 0.001f)

        // Dynamic auto-scaling expands even tiny 0.20% movements to span the padded visual range (~83.33% of canvas)
        val heightDiff = startYFraction - endYFraction
        assertEquals(0.8333f, heightDiff, 0.005f)

        assertEquals(0.9167f, startYFraction, 0.005f)
        assertEquals(0.0833f, endYFraction, 0.005f)
    }

    @Test
    fun `price movement scales dynamically across canvas regardless of percentage`() {
        val minPrice = 100000.0
        val maxPrice = 102500.0 // +2.5% move

        val minYFraction = SparklineScaleCalculator.calculateYFraction(minPrice, minPrice, maxPrice)
        val maxYFraction = SparklineScaleCalculator.calculateYFraction(maxPrice, minPrice, maxPrice)

        val heightDiff = maxYFraction - minYFraction
        assertEquals(0.8333f, heightDiff, 0.005f)
        assertEquals(0.0833f, minYFraction, 0.005f)
        assertEquals(0.9167f, maxYFraction, 0.005f)
    }

    @Test
    fun `custom vertical padding fraction scales correctly`() {
        val minPrice = 90000.0
        val maxPrice = 94500.0 // +5.0% difference

        // Zero padding allows min and max to hit exact canvas edges (0.0 and 1.0)
        val minYFraction = SparklineScaleCalculator.calculateYFraction(minPrice, minPrice, maxPrice, verticalPaddingFraction = 0.0)
        val maxYFraction = SparklineScaleCalculator.calculateYFraction(maxPrice, minPrice, maxPrice, verticalPaddingFraction = 0.0)

        assertEquals(0.0f, minYFraction, 0.001f)
        assertEquals(1.0f, maxYFraction, 0.001f)
    }

    @Test
    fun `flat price change produces centered Y fraction at 0 point 5`() {
        val minPrice = 50000.0
        val maxPrice = 50000.0 // 0% change

        val yFraction = SparklineScaleCalculator.calculateYFraction(minPrice, minPrice, maxPrice)

        assertEquals(0.5f, yFraction, 0.001f)
    }

    @Test
    fun `placeholder sparkline prices list is non empty and has distinct min max`() {
        val prices = com.jcoronado.minimalbitcoinwidget.ui.components.PLACEHOLDER_SPARKLINE_PRICES
        assertTrue(prices.size >= 2)
        val minPrice = prices.minOrNull() ?: 0.0
        val maxPrice = prices.maxOrNull() ?: 0.0
        assertTrue(maxPrice > minPrice)
    }
}
