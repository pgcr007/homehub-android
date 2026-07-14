package com.homehub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.homehub.app.ui.navigation.HomeHubNavHost
import com.homehub.app.ui.theme.HomeHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeHubTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeHubNavHost()
                }
            }
        }
    }
}