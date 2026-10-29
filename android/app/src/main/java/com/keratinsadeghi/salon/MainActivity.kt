package com.keratinsadeghi.salon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.keratinsadeghi.salon.ui.AppRoot
import com.keratinsadeghi.salon.ui.KeratinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KeratinTheme {
                AppRoot()
            }
        }
    }
}
