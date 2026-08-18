package com.jcoronado.minimalbitcoinwidget.widgets.glance

import kotlinx.serialization.Serializable

@Serializable
sealed class PriceWidgetState {
    @Serializable
    data object Loading : PriceWidgetState()

    @Serializable
    data class Available(
        val price: Double,
        val changePercentage: Double,
        val intervalLabelResId: Int,
        val currency: String,
        val fontKey: String = "google_sans_rounded",
        val boldPrice: Boolean = false
    ) : PriceWidgetState()

    @Serializable
    data class Error(
        val message: String,
        val lastValidState: Available? = null,
        val fontKey: String = "google_sans_rounded",
        val boldPrice: Boolean = false
    ) : PriceWidgetState()
}
