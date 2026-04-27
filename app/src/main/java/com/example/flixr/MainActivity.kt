package com.example.flixr

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.flixr.ui.FlixrApp
import com.example.flixr.ui.theme.FlixrTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase is also auto-initialized via `FirebaseInitProvider` when google-services.json is present.
        // Calling initializeApp() is still fine: it makes startup explicit and matches coursework-style setup.
        FirebaseApp.initializeApp(this)
        Log.d("FirebaseTest", "Firebase Connected Successfully!")

        enableEdgeToEdge()

        // Switch away from the launch theme right before we draw Compose.
        // This lets Android display the launch image window background during startup.
        setTheme(R.style.Theme_Flixr)
        setContent {
            // `FlixrApp` owns navigation between Auth screens and the signed-in Home screen.
            FlixrTheme {
                FlixrApp()
            }
        }
    }
}