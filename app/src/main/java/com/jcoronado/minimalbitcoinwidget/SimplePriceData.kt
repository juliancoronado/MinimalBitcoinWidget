package com.jcoronado.minimalbitcoinwidget

/**
 * A simplified data class to hold only the required information from the API response.
 */
data class PriceData(
    val currentPrice: Double,
    val priceChangePercentage24h: Double
)