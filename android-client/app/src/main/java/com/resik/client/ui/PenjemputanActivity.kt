package com.resik.client.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.resik.client.ui.screens.PenjemputanScreen
import com.resik.client.ui.theme.ResikTheme

class PenjemputanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResikTheme {
                PenjemputanScreen(
                    onBack = { finish() },
                    onCreate = { Toast.makeText(this, "Penjemputan dibuat! (demo)", Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }
}
