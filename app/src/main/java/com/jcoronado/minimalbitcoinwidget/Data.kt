package com.jcoronado.minimalbitcoinwidget

data class Data(
    // all values initialized to null
    var id: String? = null,
    var symbol: String? = null,
    var name: String? = null,
    var image: String? = null,
    var current_price: Double? = null,
    var market_cap: Long? = null,
    var market_cap_rank: Int? = null,
    var fully_diluted_valuation: Long? = null,
    var total_volume: Long? = null,
    var high_24h: Double? = null,
    var low_24h: Double? = null,
    var price_change_24h: Float? = null,
    var price_change_percentage_24h: Float? = null,
    var market_cap_change_24h: Float? = null,
    var market_cap_change_percentage_24h: Float? = null,
    var circulating_supply: Int? = null,
    var total_supply: Int? = null,
    var max_supply: Int? = null,
    var ath: Double? = null,
    var ath_change_percentage: Double? = null,
    var ath_date: String? = null,
    var atl: Double? = null,
    var atl_change_percentage: Double? = null,
    var atl_date: String? = null,
    var roi: String? = null,
    var last_updated: String? = null
) {

    /**
     *  Formats the 24-hour price change information to a string
     *  in the users' locale and adds a '+' symbol if the
     *  change is positive (and '-' symbol if negative)
     *
     *  @return the 24-hour price change percentage as a String
     */
    fun dayChangeString(): String {

        // format change percent with two decimal places
        // and using LOCALE information (by default)

        val changeStr = "%.2f".format(price_change_percentage_24h)

        return if (price_change_percentage_24h!! > 0) {
            "+$changeStr%"
        } else {
            "$changeStr%"
        }
    }

    /**
     * Formats the price information to a string
     * and adds a decimal or comma where needed.
     *
     * @return the current price as a String
     */
    fun priceString(): String {
        var priceString = current_price.toString()
        var length = priceString.length

//        val tempStr = "%.2f".format(current_price)
//
//        println("TEMP STRING: $tempStr")

        // API returns cents with only 1 int if ending in 0
        // example: 12,345.9 (not .90)
        // check to add extra zero(s) if needed

        // string already contains a period
        if (priceString.contains('.')) {
            // if the decimal is followed by two integers, do nothing
            if (priceString[length - 3] == '.') {
                // do nothing
            } else {
                // add a zero to the end
                priceString += "0"
            }
        } else {
            // string contains no period
            // adds extra zeros
            priceString += ".00"
        }

        // update value of length
        length = priceString.length

        var resultString = ""

        if (length > 6) {
            // over $1,000 (for example)
            resultString = "${priceString.subSequence(0, priceString.length - 6)},${
                priceString.subSequence(
                    priceString.length - 6,
                    priceString.length
                )
            }"

            val newLength = resultString.length

            if (length > 9) {
                // over $1,000,000 (for example)
                resultString =
                    "${resultString.subSequence(0, newLength - 10)},${
                        resultString.subSequence(
                            newLength - 10,
                            newLength
                        )
                    }"
            }

        }
        return resultString
    }
}