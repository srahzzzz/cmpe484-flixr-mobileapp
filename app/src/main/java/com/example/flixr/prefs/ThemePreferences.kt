package com.example.flixr.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "flixr_theme")

class ThemePreferences(private val context: Context) {
    private val key = stringPreferencesKey("theme_mode")

    val themeMode: Flow<ThemeMode> =
        context.themeDataStore.data.map { prefs ->
            when (prefs[key]) {
                "LIGHT" -> ThemeMode.LIGHT
                "DARK" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { it[key] = mode.name }
    }
}
