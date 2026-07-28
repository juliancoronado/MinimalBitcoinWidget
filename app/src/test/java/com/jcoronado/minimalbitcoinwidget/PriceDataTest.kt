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
}
