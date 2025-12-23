package com.jcoronado.minimalbitcoinwidget.classes

data class PriceUiState(
    val price: Double = 0.0,
    val percentageChange: Double = 0.0,
    val selectedCurrency : String = AppConstants.CURRENCY_DEFAULT,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)