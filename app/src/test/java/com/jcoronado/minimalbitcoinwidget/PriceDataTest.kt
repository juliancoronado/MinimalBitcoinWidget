package com.jcoronado.minimalbitcoinwidget

import com.jcoronado.minimalbitcoinwidget.classes.PriceData
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for PriceData.
 */
class PriceDataTest {

    private val priceData = PriceData(
        currentPrice = 90000.0,
        priceChangePercentage24h = 1.5,
        priceChangePercentage7d = -5.2,
        priceChangePercentage30d = 12.8
    )

    @Test
    fun `getPercentageForInterval with index 0 returns 24h change`() {
        val result = priceData.getPercentageForInterval(0)
        assertEquals(1.5, result, 0.0)
    }

    @Test
    fun `getPercentageForInterval with index 1 returns 7d change`() {
        val result = priceData.getPercentageForInterval(1)
        assertEquals(-5.2, result, 0.0)
    }

    @Test
    fun `getPercentageForInterval with index 2 returns 30d change`() {
        val result = priceData.getPercentageForInterval(2)
        assertEquals(12.8, result, 0.0)
    }

    @Test
    fun `getPercentageForInterval with invalid index returns 24h change default`() {
        val resultUnder = priceData.getPercentageForInterval(-1)
        assertEquals(1.5, resultUnder, 0.0)

        val resultOver = priceData.getPercentageForInterval(3)
        assertEquals(1.5, resultOver, 0.0)
    }

    @Test
    fun `getSparklineForInterval returns expected sliced lists for 24h 7d and 30d`() {
        val mockPrices = List(168) { i -> 50000.0 + i }
        val sparklineData = priceData.copy(
            sparklineIn7d = PriceData.SparklineData(prices = mockPrices)
        )

        // 24h (index 0) returns last 25 points
        val sparkline24h = sparklineData.getSparklineForInterval(0)
        assertEquals(25, sparkline24h.size)
        assertEquals(priceData.currentPrice, sparkline24h.last(), 0.001)

        // 7d (index 1) returns full 168 points
        val sparkline7d = sparklineData.getSparklineForInterval(1)
        assertEquals(168, sparkline7d.size)

        // 30d (index 2) returns empty list
        val sparkline30d = sparklineData.getSparklineForInterval(2)
        assertEquals(0, sparkline30d.size)
    }

    @Test
    fun `getSparklineForInterval with negative 24h change ensures end price is lower than start price`() {
        // Raw prices increase from 60000 to 65000 (going UP)
        val rawPrices = List(168) { i -> 60000.0 + (i * 30.0) }
        val testData = PriceData(
            currentPrice = 63494.88,
            priceChangePercentage24h = -0.30,
            priceChangePercentage7d = 1.0,
            priceChangePercentage30d = 5.0,
            sparklineIn7d = PriceData.SparklineData(prices = rawPrices)
        )

        val sparkline = testData.getSparklineForInterval(0)
        val expectedStart = 63494.88 / (1.0 - 0.003)

        assertEquals(25, sparkline.size)
        assertEquals(63494.88, sparkline.last(), 0.001)
        assertEquals(expectedStart, sparkline.first(), 0.001)
        org.junit.Assert.assertTrue("Right end of negative trendline must be lower than left end", sparkline.last() < sparkline.first())
    }

    @Test
    fun `getSparklineForInterval with positive 24h change ensures end price is higher than start price`() {
        // Raw prices decrease from 70000 to 60000 (going DOWN)
        val rawPrices = List(168) { i -> 70000.0 - (i * 60.0) }
        val testData = PriceData(
            currentPrice = 65000.00,
            priceChangePercentage24h = 5.00,
            priceChangePercentage7d = 2.0,
            priceChangePercentage30d = 10.0,
            sparklineIn7d = PriceData.SparklineData(prices = rawPrices)
        )

        val sparkline = testData.getSparklineForInterval(0)
        val expectedStart = 65000.00 / 1.05

        assertEquals(25, sparkline.size)
        assertEquals(65000.00, sparkline.last(), 0.001)
        assertEquals(expectedStart, sparkline.first(), 0.001)
        org.junit.Assert.assertTrue("Right end of positive trendline must be higher than left end", sparkline.last() > sparkline.first())
    }

    @Test
    fun `getSparklineForInterval preserves intermediate price peaks and valleys`() {
        // Create a series with a prominent peak in the middle
        val rawPrices = MutableList(25) { 100.0 }
        rawPrices[12] = 150.0 // Peak in the middle

        val testData = PriceData(
            currentPrice = 100.0,
            priceChangePercentage24h = 0.0,
            priceChangePercentage7d = 0.0,
            priceChangePercentage30d = 0.0,
            sparklineIn7d = PriceData.SparklineData(prices = rawPrices)
        )

        val sparkline = testData.getSparklineForInterval(0)
        assertEquals(100.0, sparkline.first(), 0.001)
        assertEquals(100.0, sparkline.last(), 0.001)
        assertEquals(150.0, sparkline[12], 0.001)
        org.junit.Assert.assertTrue("Peak in middle must remain higher than endpoints", sparkline[12] > sparkline.first() && sparkline[12] > sparkline.last())
    }

    @Test
    fun `getSparklineForInterval handles null sparkline gracefully`() {
        val sparkline = priceData.getSparklineForInterval(0)
        assertEquals(0, sparkline.size)
    }
}
