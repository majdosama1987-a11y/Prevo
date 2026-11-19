package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.storeDataStore: DataStore<Preferences> by preferencesDataStore(name = "store_settings")

data class StoreSettings(
    val currencySymbol: String = "د.أ",
    val storeName: String = "Prevo"
)

class StorePreferences(private val context: Context) {
    private object Keys {
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val STORE_NAME = stringPreferencesKey("store_name")
    }

    val settingsFlow: Flow<StoreSettings> = context.storeDataStore.data.map { prefs ->
        StoreSettings(
            currencySymbol = prefs[Keys.CURRENCY_SYMBOL] ?: "د.أ",
            storeName = prefs[Keys.STORE_NAME] ?: "Prevo"
        )
    }

    suspend fun updateCurrency(symbol: String) {
        context.storeDataStore.edit { prefs ->
            prefs[Keys.CURRENCY_SYMBOL] = symbol
        }
    }

    suspend fun updateStoreName(name: String) {
        context.storeDataStore.edit { prefs ->
            prefs[Keys.STORE_NAME] = name.trim()
        }
    }
}
