package com.jcoronado.minimalbitcoinwidget.classes

/**
 * Represents the UI state for the Bitcoin price widget.
 *
 * @property price The current price of Bitcoin.
 * @property percentageChange The percentage change in price over a specific period.
 * @property changeIntervalLabel The label for the percentage change period (e.g., "24H", "7D", "30D").
 * @property selectedCurrency The currency code currently selected for displaying the price.
 * @property isLoading Indicates whether the price data is currently being fetched.
 * @property errorMessage An optional error message to display if the data fetch fails.
 */
data class PriceUiState(
    val price: Double = 0.0,
    val percentageChange: Double = 0.0,
    val changeIntervalLabel: String = "24H", // default value
    val selectedCurrency : String = AppConstants.CURRENCY_DEFAULT,
    val lastUpdated: Long = 0L,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
