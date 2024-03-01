package com.jcoronado.minimalbitcoinwidget

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

data class PriceData(
    var bitcoin: Bitcoin
)

data class Bitcoin(
    var price: Double,
    var change24h: Double
)

class PriceDataDeserializer(private val currencyCode: String) : JsonDeserializer<PriceData> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PriceData {
        val jsonObject = json!!.asJsonObject
        val bitcoinObject = jsonObject.getAsJsonObject("bitcoin")
        val price = bitcoinObject.getAsJsonPrimitive(currencyCode).asDouble
        val change24h = bitcoinObject.getAsJsonPrimitive("${currencyCode}_24h_change").asDouble
        val bitcoin = Bitcoin(price = price, change24h = change24h)
        return PriceData(bitcoin = bitcoin)
    }
}