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
    val priceChangePercentage30d: Double,
    @SerializedName("sparkline_in_7d")
    val sparklineIn7d: SparklineData? = null
) {
    data class SparklineData(
        @SerializedName("price")
        val prices: List<Double> = emptyList()
    )
    /**
     * Returns the price change percentage for the given time interval index.
     * Index mappings:
     * - 0: 24h
     * - 1: 7d
     * - 2: 30d
     */
    fun getPercentageForInterval(index: Int): Double {
        return when (index) {
            0 -> priceChangePercentage24h
            1 -> priceChangePercentage7d
            2 -> priceChangePercentage30d
            else -> priceChangePercentage24h
        }
    }

    /**
     * Returns the sliced and trend-normalized sparkline price list for the given time interval index.
     * Index mappings:
     * - 0: 24h (last 25 points, covering 24 hourly intervals)
     * - 1: 7d (all points)
     * - 2: 30d (empty list, as CoinGecko only provides 7d sparklines)
     */
    fun getSparklineForInterval(index: Int): List<Double> {
        val rawPrices = sparklineIn7d?.prices ?: return emptyList()
        if (rawPrices.isEmpty()) return emptyList()

        val slice = when (index) {
            0 -> if (rawPrices.size >= 25) rawPrices.takeLast(25) else rawPrices
            1 -> rawPrices
            2 -> return emptyList()
            else -> if (rawPrices.size >= 25) rawPrices.takeLast(25) else rawPrices
        }

        if (slice.size < 2) return slice

        val percentageChange = getPercentageForInterval(index)
        val denom = 1.0 + (percentageChange / 100.0)
        if (denom <= 0.0) return slice

        val targetStartPrice = currentPrice / denom
        val targetEndPrice = currentPrice

        val n = slice.size
        val r0 = slice.first()
        val rEnd = slice.last()

        return List(n) { i ->
            val t = i.toDouble() / (n - 1)
            val rawBaseline = r0 + t * (rEnd - r0)
            val targetBaseline = targetStartPrice + t * (targetEndPrice - targetStartPrice)
            if (rawBaseline <= 0.0) {
                targetBaseline
            } else {
                val ratio = slice[i] / rawBaseline
                targetBaseline * ratio
            }
        }
    }
}
