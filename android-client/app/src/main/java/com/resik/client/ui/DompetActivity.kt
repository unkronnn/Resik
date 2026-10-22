package com.resik.client.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.resik.client.ui.screens.DompetScreen
import com.resik.client.ui.theme.ResikTheme

class DompetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResikTheme {
                DompetScreen(saldo = "Rp 0", poin = "0", onBack = { finish() })
            }
        }
    }
}
