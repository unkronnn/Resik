package com.resik.client.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.resik.client.api.ApiClient
import com.resik.client.api.RealtimeEventHub
import com.resik.client.api.RealtimeEventType
import com.resik.client.ui.screens.DompetScreen
import com.resik.client.ui.theme.ResikTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DompetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var saldo by remember { mutableStateOf("Rp 0") }
            var poin by remember { mutableStateOf("0") }
            val ownUserId = ApiClient.getUserId()

            fun loadSaldo() {
                ApiClient.getApi().getSaldo(ApiClient.getAuthHeader())
                    .enqueue(object : Callback<Map<String, Any>> {
                        override fun onResponse(c: Call<Map<String, Any>>, r: Response<Map<String, Any>>) {
                            if (r.isSuccessful && r.body() != null) {
                                saldo = "Rp ${r.body()!!["saldo"] ?: 0}"
                                poin = (r.body()!!["poinReward"] ?: 0).toString()
                            }
                        }
                        override fun onFailure(c: Call<Map<String, Any>>, t: Throwable) {
                            // Offline: keep the last shown values.
                        }
                    })
            }

            // Live: re-fetch when the wallet changes anywhere (a timbang on
            // desktop, a topup, a tarik). No full reload, just the numbers.
            DisposableEffect(Unit) {
                RealtimeEventHub.addListener("dompet") { event ->
                    if (event.type == RealtimeEventType.WALLET_UPDATED &&
                        event.payloadLong("userId") == ownUserId
                    ) {
                        loadSaldo()
                    }
                }
                onDispose { RealtimeEventHub.removeListener("dompet") }
            }

            LaunchedEffect(Unit) { loadSaldo() }

            ResikTheme {
                DompetScreen(saldo = saldo, poin = poin, onBack = { finish() })
            }
        }
    }
}
