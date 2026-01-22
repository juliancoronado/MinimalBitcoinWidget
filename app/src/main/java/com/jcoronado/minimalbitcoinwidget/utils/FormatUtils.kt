package com.jcoronado.minimalbitcoinwidget.utils
import java.text.NumberFormat

object FormatUtils {
    fun formatPrice(price: Double, selectedCurrency: String): String {
        val formatter = NumberFormat.getCurrencyInstance().apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
            currency = java.util.Currency.getInstance(selectedCurrency.uppercase())
        }
        return formatter.format(price)
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
