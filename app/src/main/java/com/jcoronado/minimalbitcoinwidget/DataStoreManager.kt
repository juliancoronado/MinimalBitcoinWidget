package com.jcoronado.minimalbitcoinwidget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreManager(context: Context) {
    private val dataStore: DataStore<Preferences> = context.dataStore

    suspend fun <T> getValue(key: Preferences.Key<T>, defaultValue: T) : T {

        val storedValue = dataStore.data.map {preferences ->
            preferences[key]
        }.first()

        return if (storedValue == null) {
            this.saveValue(key, defaultValue)
            defaultValue
        } else {
            storedValue
        }
    }

    suspend fun <T> saveValue(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }
}