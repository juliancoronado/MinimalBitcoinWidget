package com.example.btcpricewidget

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

    // returns string on the 24 hour change including the + or - symbol
    fun change24h(): String {

        val change = "%.2f".format(price_change_percentage_24h).toDouble()

        return if (change > 0) {
            // positive change
            "+$change%"
        } else {
            // negative change
            "$change%"
        }
    }

    // returns string with price adjusted with two decimal places (USD cents)
    fun price(): String {
        var tempPrice = current_price.toString()
        val n = tempPrice.length

        // API returns cents with only 1 int if ending in 0
        // example: 12,345.9 (not .90)
        // check to add extra zero if needed

        if (tempPrice[n - 3] == '.') {
            // do nothing
        } else {
            // adds extra 0
            tempPrice += "0"
        }

        var returnStr = ""

        if (tempPrice.length > 6) {
            returnStr = "$${tempPrice.subSequence(0, tempPrice.length - 6)},${
                tempPrice.subSequence(
                    tempPrice.length - 6,
                    tempPrice.length
                )
            }"
        }

        return returnStr
    }
}