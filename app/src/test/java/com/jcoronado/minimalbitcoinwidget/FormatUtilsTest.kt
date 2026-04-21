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
}
