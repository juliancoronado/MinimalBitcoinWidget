package com.jcoronado.minimalbitcoinwidget

data class PriceData(
    var bitcoin: Bitcoin
)


data class Bitcoin(
    var usd: Double,
    var usd_24h_change: Double
)