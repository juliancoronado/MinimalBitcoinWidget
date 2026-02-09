package com.jcoronado.minimalbitcoinwidget.classes

import com.google.gson.annotations.SerializedName

/**
 * A data class to hold the Bitcoin price data from the CoinGecko markets API.
 */
data class PriceData(
    @SerializedName("current_price")
    val currentPrice: Double,
    @SerializedName("price_change_percentage_24h_in_currency")
    val priceChangePercentage24h: Double,
    @SerializedName("price_change_percentage_7d_in_currency")
    val priceChangePercentage7d: Double,
    @SerializedName("price_change_percentage_30d_in_currency")
    val priceChangePercentage30d: Double
)
