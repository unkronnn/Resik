package com.resik.client.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.resik.client.api.ApiClient
import com.resik.client.ui.screens.*
import com.resik.client.ui.theme.ResikTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var userName by mutableStateOf("Loading...")
        var saldo by mutableStateOf("Rp 0")
        var poin by mutableStateOf("0")

        ApiClient.getApi().getMe(ApiClient.getAuthHeader())
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(c: Call<Map<String, Any>>, r: Response<Map<String, Any>>) {
                    if (r.isSuccessful && r.body() != null) {
                        val user = r.body()!!
                        userName = user["nama"]?.toString() ?: "User"
                        saldo = "Rp ${user["saldoDompet"] ?: 0}"
                        poin = (user["poinReward"] ?: 0).toString()
                    }
                }
                override fun onFailure(c: Call<Map<String, Any>>, t: Throwable) {
                    userName = "Error loading"
                }
            })

        setContent {
            ResikTheme {
                HomeScreen(
                    userName = userName,
                    saldo = saldo,
                    poin = poin,
                    onLogout = {
                        ApiClient.logout()
                        startActivity(Intent(this, WelcomeActivity::class.java))
                        finish()
                    },
                    onNavEdukasi = { startActivity(Intent(this, EdukasiActivity::class.java)) },
                    onNavPenjemputan = { startActivity(Intent(this, PenjemputanActivity::class.java)) },
                    onNavRiwayat = { startActivity(Intent(this, RiwayatActivity::class.java)) },
                    onNavChat = { startActivity(Intent(this, ChatActivity::class.java)) },
                    onNavMap = { startActivity(Intent(this, MapActivity::class.java)) },
                    onNavQr = { startActivity(Intent(this, QrScannerActivity::class.java)) },
                    onNavDompet = { startActivity(Intent(this, DompetActivity::class.java)) },
                    onNavLeaderboard = { startActivity(Intent(this, LeaderboardActivity::class.java)) },
                    onNavProfil = { startActivity(Intent(this, ProfilActivity::class.java)) },
                )
            }
        }
    }
}
