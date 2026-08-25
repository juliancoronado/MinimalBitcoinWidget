package com.jcoronado.minimalbitcoinwidget

import com.jcoronado.minimalbitcoinwidget.utils.FormattedPrice
import com.jcoronado.minimalbitcoinwidget.widgets.glance.WidgetBitmapUtils
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for WidgetBitmapUtils font sizing calculations and overloads.
 */
class WidgetBitmapUtilsTest {

    @Test
    fun `getWidgetPriceFontSize with standard length (11 or less) returns 24sp price and 16sp symbol`() {
        // 1 char symbol + 6 char price = 7 total
        val (priceSp, symbolSp) = WidgetBitmapUtils.getWidgetPriceFontSize("500.00", "$")
        assertEquals(24f, priceSp, 0.0f)
        assertEquals(16f, symbolSp, 0.0f)

        // 1 char symbol + 8 char price = 9 total
        val (pSp9, sSp9) = WidgetBitmapUtils.getWidgetPriceFontSize("9,450.00", "$")
        assertEquals(24f, pSp9, 0.0f)
        assertEquals(16f, sSp9, 0.0f)

        // 1 char symbol + 9 char price = 10 total (standard 5-digit USD e.g. $65,432.10)
        val (pSp10, sSp10) = WidgetBitmapUtils.getWidgetPriceFontSize("65,432.10", "$")
        assertEquals(24f, pSp10, 0.0f)
        assertEquals(16f, sSp10, 0.0f)

        // 1 char symbol + 10 char price = 11 total (e.g. JPY ¥12,788,187 or 6-digit USD $100,000.00)
        val (pSp11, sSp11) = WidgetBitmapUtils.getWidgetPriceFontSize("12,788,187", "¥")
        assertEquals(24f, pSp11, 0.0f)
        assertEquals(16f, sSp11, 0.0f)
    }

    @Test
    fun `getWidgetPriceFontSize with length (12 to 13) returns 21sp price and 15sp symbol`() {
        // 2 char symbol + 10 char price = 12 total (e.g. R$ 540.000,00)
        val (pSp12, sSp12) = WidgetBitmapUtils.getWidgetPriceFontSize("540.000,00", "R$")
        assertEquals(21f, pSp12, 0.0f)
        assertEquals(15f, sSp12, 0.0f)

        // 1 char symbol + 12 char price = 13 total (e.g. $1,250,000.00)
        val (pSp13, sSp13) = WidgetBitmapUtils.getWidgetPriceFontSize("1,250,000.00", "$")
        assertEquals(21f, pSp13, 0.0f)
        assertEquals(15f, sSp13, 0.0f)
    }

    @Test
    fun `getWidgetPriceFontSize with long length (14 to 15) returns 18sp price and 14sp symbol`() {
        // 1 char symbol + 13 char price = 14 total
        val (pSp14, sSp14) = WidgetBitmapUtils.getWidgetPriceFontSize("14,500,000.00", "¥")
        assertEquals(18f, pSp14, 0.0f)
        assertEquals(14f, sSp14, 0.0f)

        // 1 char symbol + 14 char price = 15 total (e.g. $100,000,000.00)
        val (pSp15, sSp15) = WidgetBitmapUtils.getWidgetPriceFontSize("100,000,000.00", "$")
        assertEquals(18f, pSp15, 0.0f)
        assertEquals(14f, sSp15, 0.0f)
    }

    @Test
    fun `getWidgetPriceFontSize with extra long length (16 or more) returns 15sp price and 12sp symbol`() {
        // 2 char symbol + 14 char price = 16 total
        val (pSp16, sSp16) = WidgetBitmapUtils.getWidgetPriceFontSize("100.000.000,00", "R$")
        assertEquals(15f, pSp16, 0.0f)
        assertEquals(12f, sSp16, 0.0f)
    }

    @Test
    fun `getWidgetPriceFontSize with FormattedPrice object`() {
        val formattedPrice = FormattedPrice(
            symbol = "$",
            price = "65,432.10",
            symbolAtStart = true
        )
        val (priceSp, symbolSp) = WidgetBitmapUtils.getWidgetPriceFontSize(formattedPrice)
        assertEquals(24f, priceSp, 0.0f)
        assertEquals(16f, symbolSp, 0.0f)
    }

    @Test
    fun `getWidgetPriceFontSize with raw Double price and currency code`() {
        // 500.0 in USD -> $500.00 (7 chars total) -> 24sp / 16sp
        val (priceSp1, symbolSp1) = WidgetBitmapUtils.getWidgetPriceFontSize(500.0, "USD")
        assertEquals(24f, priceSp1, 0.0f)
        assertEquals(16f, symbolSp1, 0.0f)

        // 65000.0 in USD -> $65,000.00 (10 chars total) -> 24sp / 16sp
        val (priceSp2, symbolSp2) = WidgetBitmapUtils.getWidgetPriceFontSize(65000.0, "USD")
        assertEquals(24f, priceSp2, 0.0f)
        assertEquals(16f, symbolSp2, 0.0f)

        // 14500000.0 in JPY -> ¥14,500,000 (11 chars total) -> 24sp / 16sp
        val (priceSp3, symbolSp3) = WidgetBitmapUtils.getWidgetPriceFontSize(14500000.0, "JPY")
        assertEquals(24f, priceSp3, 0.0f)
        assertEquals(16f, symbolSp3, 0.0f)
    }

    @Test
    fun `getWidgetSecondaryFontSize returns 12sp`() {
        assertEquals(12f, WidgetBitmapUtils.getWidgetSecondaryFontSize(), 0.0f)
    }

    @Test
    fun `getWidgetPriceFontSize with empty strings edge case`() {
        val (priceSp, symbolSp) = WidgetBitmapUtils.getWidgetPriceFontSize("", "")
        assertEquals(24f, priceSp, 0.0f)
        assertEquals(16f, symbolSp, 0.0f)
    }

    @Test
    fun `getWidgetPriceFontSize with zero price`() {
        val (priceSp, symbolSp) = WidgetBitmapUtils.getWidgetPriceFontSize(0.0, "USD")
        // 0.0 in USD -> $0.00 (5 chars) -> 24sp / 16sp
        assertEquals(24f, priceSp, 0.0f)
        assertEquals(16f, symbolSp, 0.0f)
    }
}
