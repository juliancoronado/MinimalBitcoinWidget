package com.jcoronado.minimalbitcoinwidget

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class ApiHelper {

    suspend fun fetchDataFromUrl(btcPriceViewModel: BTCPriceViewModel) {
        val url = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd&include_24hr_change=true&precision=2"

        withContext(Dispatchers.IO) {
            btcPriceViewModel.error.value = "" // reset error value before new api call
            val client = OkHttpClient()

            val request = Request.Builder().url(url).get().build()

            try {
                val response = client.newCall(request).execute()

                if (response.code == 200) {
                    val responseBody = response.body?.string()
                    Log.i("API Response", responseBody!!)
                    val priceData = Gson().fromJson(responseBody, PriceData::class.java)
                    btcPriceViewModel.updateData(priceData)
                } else if (response.code == 429) {
                    btcPriceViewModel.error.value = "Too many requests, try again later"
                }
                response.close()
            } catch (e: Exception) {
                Log.e("CoinGecko Error", e.toString())
            }
        }
    }
}