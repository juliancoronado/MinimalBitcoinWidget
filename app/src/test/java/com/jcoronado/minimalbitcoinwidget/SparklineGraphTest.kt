package com.jcoronado.minimalbitcoinwidget

import com.jcoronado.minimalbitcoinwidget.ui.components.SparklineScaleCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SparklineGraphTest {

    @Test
    fun `tiny price movement minus 0 point 20 percent in JPY produces 4 percent visual height difference`() {
        val currentPrice = 9901380.88
        val startPrice = 9921223.33 // -0.20% move over 24h
        val minPrice = currentPrice
        val maxPrice = startPrice

        val startYFraction = SparklineScaleCalculator.calculateYFraction(startPrice, minPrice, maxPrice, minVisualRangePct = 0.05)
        val endYFraction = SparklineScaleCalculator.calculateYFraction(currentPrice, minPrice, maxPrice, minVisualRangePct = 0.05)

        // Midpoint Y fraction is exact 0.5 (center)
        val midYFraction = (startYFraction + endYFraction) / 2f
        assertEquals(0.5f, midYFraction, 0.001f)

        // Height difference between start and end should be exactly 0.20% / 5.0% = 4% of canvas height (52% vs 48%)
        val heightDiff = startYFraction - endYFraction
        assertEquals(0.04f, heightDiff, 0.005f)

        assertEquals(0.52f, startYFraction, 0.005f)
        assertEquals(0.48f, endYFraction, 0.005f)
    }

    @Test
    fun `2 point 5 percent price movement produces 50 percent visual height difference`() {
        val minPrice = 100000.0
        val maxPrice = 102500.0 // +2.5% move

        val minYFraction = SparklineScaleCalculator.calculateYFraction(minPrice, minPrice, maxPrice, minVisualRangePct = 0.05)
        val maxYFraction = SparklineScaleCalculator.calculateYFraction(maxPrice, minPrice, maxPrice, minVisualRangePct = 0.05)

        val heightDiff = maxYFraction - minYFraction
        assertEquals(0.50f, heightDiff, 0.005f)
        assertEquals(0.25f, minYFraction, 0.005f)
        assertEquals(0.75f, maxYFraction, 0.005f)
    }

    @Test
    fun `5 percent or greater price movement maxes out to 100 percent visual range`() {
        val minPrice = 90000.0
        val maxPrice = 94500.0 // +5.0% difference

        val minYFraction = SparklineScaleCalculator.calculateYFraction(minPrice, minPrice, maxPrice, minVisualRangePct = 0.05)
        val maxYFraction = SparklineScaleCalculator.calculateYFraction(maxPrice, minPrice, maxPrice, minVisualRangePct = 0.05)

        assertEquals(0.0f, minYFraction, 0.001f)
        assertEquals(1.0f, maxYFraction, 0.001f)
    }

    @Test
    fun `flat price change produces centered Y fraction at 0 point 5`() {
        val minPrice = 50000.0
        val maxPrice = 50000.0 // 0% change

        val yFraction = SparklineScaleCalculator.calculateYFraction(minPrice, minPrice, maxPrice, minVisualRangePct = 0.05)

        assertEquals(0.5f, yFraction, 0.001f)
    }
}
