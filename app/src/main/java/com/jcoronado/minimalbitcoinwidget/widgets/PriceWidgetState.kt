package com.jcoronado.minimalbitcoinwidget.widgets

import kotlinx.serialization.Serializable

@Serializable
sealed class PriceWidgetState {
    @Serializable
    data object Loading : PriceWidgetState()

    @Serializable
    data class Available(
        val price: Double,
        val changePercentage: Double,
        val currency: String,
        val symbol: String
    ) : PriceWidgetState()

    @Serializable
    data class Error(val message: String?) : PriceWidgetState()
}
