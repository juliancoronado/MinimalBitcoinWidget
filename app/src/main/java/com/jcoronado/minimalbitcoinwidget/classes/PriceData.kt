package com.jcoronado.minimalbitcoinwidget.classes

/**
 * A simplified data class to hold only the required information from the API response.
 *
 * @property currentPrice The current price of Bitcoin in the selected currency.
 * @property priceChangePercentage24h The price change percentage in the last 24 hours.
 */
data class PriceData(
    val currentPrice: Double,
    val priceChangePercentage24h: Double
)
