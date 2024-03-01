package com.jcoronado.minimalbitcoinwidget

import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PriceViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {
    private val apiHelper = ApiHelper()

    var loading = mutableStateOf(false)

    private val _currencyCode = MutableStateFlow<String?>("")
    val currencyCode: Flow<String?> = _currencyCode

    private val _symbol = MutableStateFlow<String?>("")
    val symbol: Flow<String?> = _symbol

    private val _priceValue = MutableStateFlow<Double?>(0.00)
    val priceValue : Flow<Double?> = _priceValue

    private val _changeValue = MutableStateFlow<Double?>(0.00)
    val changeValue : Flow<Double?> = _changeValue

    init {
        fetchLocalValues()
    }

    private fun fetchLocalValues() {
        viewModelScope.launch {
            val storedCurrencyCodeValue = dataStoreManager.getValue(CURRENCY_CODE_KEY, DEFAULT_CURRENCY_CODE)
            _currencyCode.value = storedCurrencyCodeValue
            val storedSymbolValue = dataStoreManager.getValue(SYMBOL_KEY, DEFAULT_SYMBOL)
            _symbol.value = storedSymbolValue
            val storedPriceValue = dataStoreManager.getValue(PRICE_KEY, DEFAULT_PRICE)
            _priceValue.value = storedPriceValue
            val storedChangeValue = dataStoreManager.getValue(CHANGE_KEY, DEFAULT_CHANGE)
            _changeValue.value = storedChangeValue
        }
    }

    fun updateSelectedCurrency(newCurrency: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _currencyCode.value = newCurrency
            dataStoreManager.saveValue(CURRENCY_CODE_KEY, newCurrency)
        }
    }

    fun fetchData() {
        viewModelScope.launch(Dispatchers.IO) {
            loading.value = true
            delay(750)
            apiHelper.fetchDataFromUrl(priceViewModel = this@PriceViewModel, currencyCode = _currencyCode.value ?: DEFAULT_CURRENCY_CODE)
            loading.value = false
        }
    }

    suspend fun updateData(data: PriceData) {
        _priceValue.value = data.bitcoin.price
        _changeValue.value = data.bitcoin.change24h

        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.saveValue(PRICE_KEY, _priceValue.value ?: 0.00)
            dataStoreManager.saveValue(CHANGE_KEY, _changeValue.value ?: 0.00)
        }
    }

    companion object {
        val CURRENCY_CODE_KEY = stringPreferencesKey("currency")
        val SYMBOL_KEY = stringPreferencesKey("symbol")
        val PRICE_KEY = doublePreferencesKey("price")
        val CHANGE_KEY = doublePreferencesKey("change")
        private const val DEFAULT_CURRENCY_CODE = "USD"
        private const val DEFAULT_SYMBOL = "$"
        private const val DEFAULT_PRICE = 0.00
        private const val DEFAULT_CHANGE = 0.00
    }
}