package com.pingbridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pingbridge.ui.screens.ConfigScreen
import com.pingbridge.ui.theme.One4AllTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            One4AllTheme {
                ConfigScreen()
            }
        }
    }
}
