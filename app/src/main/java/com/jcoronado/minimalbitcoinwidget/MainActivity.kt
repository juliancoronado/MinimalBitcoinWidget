package com.jcoronado.minimalbitcoinwidget

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jcoronado.minimalbitcoinwidget.screens.AppNavigation
import com.jcoronado.minimalbitcoinwidget.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    private var intentState by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        intentState = intent
        setContent {
            AppTheme {
                AppNavigation(intent = intentState)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // when the widget is clicked while app is open, this is triggered
        intentState = intent
    }
}