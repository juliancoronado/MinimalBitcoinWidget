package com.jcoronado.minimalbitcoinwidget.utils
import java.util.Currency
import java.text.NumberFormat
import java.text.DecimalFormat
import java.util.Locale

data class FormattedPrice(
    val symbol: String,
    val price: String,
    val symbolAtStart: Boolean
)

object FormatUtils {

    /**
     * Splits price and currency symbol, respecting local formatting rules
     * but prioritizing the "Native" symbol (e.g. "$" for MXN instead of "MXN").
     */
    fun formatPriceSeparated(price: Double, currencyCode: String): FormattedPrice {
        val currency = try {
            Currency.getInstance(currencyCode.uppercase(Locale.ROOT))
        } catch (_: Exception) {
            Currency.getInstance("USD") // default / fallback
        }

        // iterate available locales to find one where this currency is the default
        val nativeSymbol = getNativeSymbol(currency)

        // setup the Formatter based on User's DISPLAY Locale (for dots/commas)
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

        // make the formatter to use our "Native" symbol
        if (formatter is DecimalFormat) {
            val symbols = formatter.decimalFormatSymbols
            symbols.currencySymbol = nativeSymbol
            formatter.decimalFormatSymbols = symbols

            // fix spacing (standard Currency format often adds non-breaking spaces)
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
        }

        val fullFormattedString = formatter.format(price)

        // determine symbol position and separate
        val isSymbolAtStart = fullFormattedString.trim().startsWith(nativeSymbol)

        // extract the price by removing the symbol from the string
        val priceOnly = fullFormattedString.replace(nativeSymbol, "").trim()

        return FormattedPrice(
            symbol = nativeSymbol,
            price = priceOnly,
            symbolAtStart = isSymbolAtStart
        )
    }

    private fun getNativeSymbol(currency: Currency): String {
        val textSymbol = currency.getSymbol(Locale.getDefault()) // default fallback

        // if the default symbol is the same as the code (e.g. "MXN"),
        // it means we are getting a disambiguated international code.
        // we want to find the symbol (e.g. "$").
        if (textSymbol == currency.currencyCode) {
            val availableLocales = Locale.getAvailableLocales()
            for (locale in availableLocales) {
                try {
                    // check if this locale's currency is the one we are looking for
                    if (Currency.getInstance(locale) == currency) {
                        // return symbol as displayed in THAT locale.
                        return currency.getSymbol(locale)
                    }
                } catch (_: Exception) {
                    // some locales are not supported by Currency.getInstance(), ignore error
                }
            }
        }
        return textSymbol
    }

    fun formatChange(change: Double): String {
        val formatter = NumberFormat.getPercentInstance(Locale.getDefault()).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return " ${formatter.format(change / 100)}"
    }
}