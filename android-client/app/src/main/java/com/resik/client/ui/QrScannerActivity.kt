package com.resik.client.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.resik.client.ui.screens.QrScannerScreen
import com.resik.client.ui.theme.ResikTheme

class QrScannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResikTheme {
                QrScannerScreen(
                    onBack = { finish() },
                    onScan = { Toast.makeText(this, "Scan dimulai", Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }
}
