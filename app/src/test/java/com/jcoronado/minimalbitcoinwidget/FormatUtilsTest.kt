package com.jcoronado.minimalbitcoinwidget

import com.jcoronado.minimalbitcoinwidget.utils.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example Unit Test for FormatUtils.
 */
class FormatUtilsTest {

    @Test
    fun `formatChange with positive value adds percentage`() {
        val result = FormatUtils.formatChange(1.234)
        // Note: NumberFormat depends on locale, but typically it would be " 1.23%"
        assertEquals(" 1.23%", result)
    }

    @Test
    fun `formatChange with negative value adds minus sign and percentage`() {
        val result = FormatUtils.formatChange(-5.678)
        assertEquals(" -5.68%", result)
    }

    @Test
    fun `formatChange with zero value`() {
        val result = FormatUtils.formatChange(0.0)
        assertEquals(" 0.00%", result)
    }

    @Test
    fun `formatPriceSeparated with mock default price and currency GBP`() {
        // Ensure default mock price (52849.10) formats correctly with default currency (GBP)
        val formatted = FormatUtils.formatPriceSeparated(52849.10, "GBP")
        assertEquals("£", formatted.symbol)
        // We replace any non-breaking space characters to avoid platform differences in test assertion
        val cleanedPrice = formatted.price.replace("\u00A0", " ").replace(" ", "")
        // Expect price digits without formatting space issues
        assertEquals("52,849.10", cleanedPrice)
    }

    @Test
    fun `formatPriceSeparated with fallback for invalid currency code`() {
        // Ensure invalid currency code falls back to USD symbol
        val formatted = FormatUtils.formatPriceSeparated(52849.10, "INVALID_CODE")
        assertEquals("$", formatted.symbol)
    }

    @Test
    fun `formatChange with mock default change percentage`() {
        val result = FormatUtils.formatChange(2.03)
        assertEquals("2.03%", result.trim())
    }

    @Test
    fun `formatPriceSeparated with JPY formats with zero decimal places`() {
        val formatted = FormatUtils.formatPriceSeparated(14500000.0, "JPY")
        assertEquals("¥", formatted.symbol)
        val cleanedPrice = formatted.price.replace("\u00A0", " ").replace(" ", "")
        assertEquals("14,500,000", cleanedPrice)
    }

    @Test
    fun `formatPriceSeparated with BRL formats with R$ symbol`() {
        val formatted = FormatUtils.formatPriceSeparated(540000.0, "BRL")
        assertEquals("R$", formatted.symbol)
    }

    @Test
    fun `formatPriceSeparated with zero price`() {
        val formatted = FormatUtils.formatPriceSeparated(0.0, "USD")
        assertEquals("$", formatted.symbol)
        val cleanedPrice = formatted.price.replace("\u00A0", " ").replace(" ", "")
        assertEquals("0.00", cleanedPrice)
    }
}
