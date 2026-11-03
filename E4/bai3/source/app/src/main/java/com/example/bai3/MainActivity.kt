package com.example.bai3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.bai3.navigation.AppNavGraph
import com.example.bai3.ui.theme.Bai3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Bai3Theme(darkTheme = false, dynamicColor = false) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.White
                ) { innerPadding ->
                    // Toàn bộ điều hướng nằm trong AppNavGraph
                    AppNavGraph(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
