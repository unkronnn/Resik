package com.resik.client.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.resik.client.ui.screens.*
import com.resik.client.ui.theme.ResikTheme

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResikTheme {
                ChatScreen(onBack = { finish() })
            }
        }
    }
}
