package com.resik.client.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.resik.client.ui.screens.*
import com.resik.client.ui.theme.ResikTheme

class PenjemputanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResikTheme {
                PenjemputanScreen(onBack = { finish() })
            }
        }
    }
}
