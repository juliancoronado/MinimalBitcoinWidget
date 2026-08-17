package com.jcoronado.minimalbitcoinwidget.data

import com.jcoronado.minimalbitcoinwidget.classes.AppConstants
import com.jcoronado.minimalbitcoinwidget.classes.PriceData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PriceRepositoryTest {

    @Test
    fun `mock price data uses default constant values`() {
        val mockData = PriceData(
            currentPrice = AppConstants.DEBUG_MOCK_PRICE_DEFAULT.toDouble(),
            priceChangePercentage24h = AppConstants.DEBUG_MOCK_PERCENT_CHANGE_DEFAULT.toDouble(),
            priceChangePercentage7d = AppConstants.DEBUG_MOCK_PERCENT_CHANGE_DEFAULT.toDouble(),
            priceChangePercentage30d = AppConstants.DEBUG_MOCK_PERCENT_CHANGE_DEFAULT.toDouble()
        )

        assertEquals(52849.10, mockData.currentPrice, 0.01)
        assertEquals(2.03, mockData.priceChangePercentage24h, 0.01)
        assertEquals(2.03, mockData.priceChangePercentage7d, 0.01)
        assertEquals(2.03, mockData.priceChangePercentage30d, 0.01)
    }

    @Test
    fun `price data percentage for interval works correctly`() {
        val priceData = PriceData(
            currentPrice = 95000.0,
            priceChangePercentage24h = 2.5,
            priceChangePercentage7d = -1.2,
            priceChangePercentage30d = 15.0
        )

        assertEquals(2.5, priceData.getPercentageForInterval(0), 0.0)
        assertEquals(-1.2, priceData.getPercentageForInterval(1), 0.0)
        assertEquals(15.0, priceData.getPercentageForInterval(2), 0.0)
    }

    @Test
    fun `price data constructor initializes all fields`() {
        val priceData = PriceData(
            currentPrice = 100000.0,
            priceChangePercentage24h = 1.0,
            priceChangePercentage7d = 2.0,
            priceChangePercentage30d = 3.0
        )
        assertNotNull(priceData)
        assertEquals(100000.0, priceData.currentPrice, 0.0)
        assertEquals(1.0, priceData.priceChangePercentage24h, 0.0)
    }

    @Test
    fun `resource states instantiate correctly`() {
        val success: Resource<String> = Resource.Success("Data")
        val error: Resource<String> = Resource.Error("Failed", cause = RuntimeException("Error"))
        val loading: Resource<String> = Resource.Loading

        assert(success is Resource.Success && success.data == "Data")
        assert(error is Resource.Error && error.message == "Failed" && error.cause != null)
        assert(loading is Resource.Loading)
    }
}
