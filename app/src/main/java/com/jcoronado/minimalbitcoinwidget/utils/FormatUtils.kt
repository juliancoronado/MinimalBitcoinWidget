package com.jcoronado.minimalbitcoinwidget.utils

import java.text.NumberFormat

object FormatUtils {
    fun formatPrice(price: Double): String {

        val formatter = NumberFormat.getNumberInstance().apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

        return formatter.format(price)
    }

    fun getCurrencySymbolFromCode(currencyCode: String): String {
        val currency = java.util.Currency.getInstance(currencyCode.uppercase())
        return currency.symbol
    }

    fun formatChange(change: Double): String {
        val formatter = NumberFormat.getPercentInstance().apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return if (change > 0) {
            " +${formatter.format(change / 100)}"
        } else {
            " ${formatter.format(change / 100)}"
        }
    }
}
