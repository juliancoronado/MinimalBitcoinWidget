package com.jcoronado.minimalbitcoinwidget.classes

import com.jcoronado.minimalbitcoinwidget.R

/**
 * Represents the UI state for the Bitcoin price widget.
 *
 * @property price The current price of Bitcoin.
 * @property percentageChange The percentage change in price over a specific period.
 * @property changeIntervalLabelResId The string resource ID for the percentage change period label (e.g., "24H", "7D").
 * @property selectedCurrency The currency code currently selected for displaying the price.
 * @property lastUpdated The timestamp of the last successful price update.
 * @property isLoading Indicates whether the price data is currently being fetched.
 * @property errorMessage An optional error message to display if the data fetch fails.
 */
data class PriceUiState(
    val price: Double = 0.0,
    val percentageChange: Double = 0.0,
    val changeIntervalLabelResId: Int = R.string.interval_24h, // default value
    val selectedCurrency : String = AppConstants.CURRENCY_DEFAULT,
    val lastUpdated: Long = 0L,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
