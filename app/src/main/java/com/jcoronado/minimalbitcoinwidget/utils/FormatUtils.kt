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
        // 1. Get the basic currency object
        val currency = try {
            Currency.getInstance(currencyCode.uppercase())
        } catch (e: Exception) {
            Currency.getInstance("USD") // Fallback
        }

        // 2. THE BIG BRAIN MOVE: Find the "Native" Symbol.
        // If we just ask for the symbol in the default locale, US phones see "MXN".
        // We iterate available locales to find one where this currency is the default.
        // This gives us the symbol as people in that country see it.
        val nativeSymbol = getNativeSymbol(currency)

        // 3. Setup the Formatter based on User's DISPLAY Locale (for dots/commas)
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

        // 4. Force the formatter to use our "Native" symbol
        if (formatter is DecimalFormat) {
            val symbols = formatter.decimalFormatSymbols
            symbols.currencySymbol = nativeSymbol
            formatter.decimalFormatSymbols = symbols

            // Fix wonky spacing (optional, but standard Currency format often adds non-breaking spaces)
            // This ensures we get clean output.
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
        }

        // 5. Generate the full string to analyze positions
        val fullFormattedString = formatter.format(price)

        // 6. Determine position and separate
        // We check if the string starts with the symbol.
        // We trim regular spaces and non-breaking spaces (Char 160)
        val isSymbolAtStart = fullFormattedString.trim().startsWith(nativeSymbol)

        // 7. Extract the amount by removing the symbol from the string
        // We do this via string manipulation rather than raw number formatting
        // to preserve any locale-specific spacing rules that might exist between number and symbol.
        val amountOnly = fullFormattedString.replace(nativeSymbol, "").trim()

        return FormattedPrice(
            symbol = nativeSymbol,
            price = amountOnly,
            symbolAtStart = isSymbolAtStart
        )
    }

    private fun getNativeSymbol(currency: Currency): String {
        // Try to find a locale that 'owns' this currency
        val textSymbol = currency.symbol // default fallback

        // If the default symbol is the same as the code (e.g. "MXN"),
        // it means we are getting a disambiguated international code.
        // We want to try harder to find the symbol (e.g. "$").
        if (textSymbol == currency.currencyCode) {
            val availableLocales = Locale.getAvailableLocales()
            for (locale in availableLocales) {
                try {
                    // Check if this locale's currency is the one we are looking for
                    if (Currency.getInstance(locale) == currency) {
                        // Found it! e.g. We found Locale("es", "MX") for MXN.
                        // Return the symbol as displayed in THAT locale.
                        return currency.getSymbol(locale)
                    }
                } catch (_: Exception) {
                    // Some locales are weird, ignore errors
                }
            }
        }
        return textSymbol
    }

    // Keep your existing change formatter
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