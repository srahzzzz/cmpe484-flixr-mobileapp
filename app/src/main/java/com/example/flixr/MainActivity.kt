package com.example.flixr

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flixr.prefs.ThemeMode
import com.example.flixr.prefs.ThemePreferences
import com.example.flixr.ui.FlixrApp
import com.example.flixr.ui.theme.FlixrTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        Log.d("FirebaseTest", "Firebase Connected Successfully!")

        enableEdgeToEdge()

        setTheme(R.style.Theme_Flixr)
        val themePreferences = ThemePreferences(applicationContext)
        setContent {
            val themeMode by themePreferences.themeMode.collectAsStateWithLifecycle(ThemeMode.SYSTEM)
            val darkTheme =
                when (themeMode) {
                    ThemeMode.DARK -> true
                    ThemeMode.LIGHT -> false
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }
            FlixrTheme(
                darkTheme = darkTheme,
                dynamicColor = false,
            ) {
                FlixrApp(themePreferences = themePreferences)
            }
        }
    }
}
