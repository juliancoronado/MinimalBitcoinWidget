package com.jcoronado.minimalbitcoinwidget

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BTCPriceViewModel(private val apiHelper: ApiHelper, private val btcDataStoreManager: BtcDataStoreManager) : ViewModel() {
    var priceValue by mutableDoubleStateOf(0.00) // price state
    var changeValue by mutableDoubleStateOf(0.00) // change state
    var loading = mutableStateOf(false) // loading state
    var error = mutableStateOf("")

    private suspend fun fetchData() {
        apiHelper.fetchDataFromUrl(this)
    }

    fun fetchDataAsync() {
        viewModelScope.launch(Dispatchers.IO) {
            loading.value = true
            fetchData()
            delay(1500)
            loading.value = false
        }
    }

    suspend fun updateData(data: PriceData) {
        priceValue = data.bitcoin.usd
        changeValue = data.bitcoin.usd_24h_change

        viewModelScope.launch(Dispatchers.IO) {
            btcDataStoreManager.storeValue(PRICE_KEY, priceValue)
            btcDataStoreManager.storeValue(CHANGE_KEY, changeValue)
        }
    }

    fun printStoredValues() {
        viewModelScope.launch(Dispatchers.IO) {
            val storedPrice = btcDataStoreManager.getValue(PRICE_KEY)
            val storedChange = btcDataStoreManager.getValue(CHANGE_KEY)

            println(storedPrice.toString())
            println(storedChange.toString())
        }
    }


}