package com.jcoronado.minimalbitcoinwidget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class BtcDataStoreManager(context: Context) {
    private val dataStore: DataStore<Preferences> = context.dataStore

    suspend fun getValue(key: Preferences.Key<Double>) : Double {
        return dataStore.data.map { preferences ->
            preferences[key] ?: 0.00
        }.first()
    }

    suspend fun storeValue(key: Preferences.Key<Double>, value: Double) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }
}