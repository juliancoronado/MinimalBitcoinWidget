package com.jcoronado.minimalbitcoinwidget

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class ApiHelper {

    suspend fun fetchDataFromUrl(priceViewModel: PriceViewModel, currencyCode : String) {
        println(currencyCode.lowercase())
        val url = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=${currencyCode.lowercase()}&include_24hr_change=true&precision=2"

        withContext(Dispatchers.IO) {
            val client = OkHttpClient()

            val request = Request.Builder().url(url).get().build()

            try {
                val response = client.newCall(request).execute()

                if (response.code == 200) {
                    val responseBody = response.body?.string()
                    Log.i("API Response", responseBody!!)
                    val gson = GsonBuilder()
                        .registerTypeAdapter(PriceData::class.java, PriceDataDeserializer(currencyCode.lowercase()))
                        .create()
                    val priceData = gson.fromJson(responseBody, PriceData::class.java)
                    priceViewModel.updateData(priceData)
                } else if (response.code == 429) {
                    print("Too many requests, try again later")
                }
                response.close()
            } catch (e: Exception) {
                Log.e("CoinGecko Error", e.toString())
            }
        }
    }
}