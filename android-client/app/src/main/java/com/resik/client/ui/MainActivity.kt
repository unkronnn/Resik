package com.resik.client.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.resik.client.api.ApiClient
import com.resik.client.api.FcmTokenSync
import com.resik.client.api.OfflineQueue
import com.resik.client.api.RealtimeEventHub
import com.resik.client.api.RealtimeEventType
import com.resik.client.ui.screens.*
import com.resik.client.ui.theme.ResikTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {

    private var ownUserId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var userName by mutableStateOf("Loading...")
        var saldo by mutableStateOf("Rp 0")
        var poin by mutableStateOf("0")
        var role by mutableStateOf("")

        fun loadMe() {
            ApiClient.getApi().getMe(ApiClient.getAuthHeader())
                .enqueue(object : Callback<Map<String, Any>> {
                    override fun onResponse(c: Call<Map<String, Any>>, r: Response<Map<String, Any>>) {
                        if (r.isSuccessful && r.body() != null) {
                            val user = r.body()!!
                            userName = user["nama"]?.toString() ?: "User"
                            saldo = "Rp ${user["saldoDompet"] ?: 0}"
                            poin = (user["poinReward"] ?: 0).toString()
                            role = user["role"]?.toString() ?: "MASYARAKAT"
                            ownUserId = (user["id"] as? Number)?.toLong()
                                ?: ApiClient.getUserId()
                            // Realtime: one shared /ws/events connection for the
                            // whole app. Saldo/poin below update live when the
                            // backend broadcasts WALLET_UPDATED for this user.
                            RealtimeEventHub.connect(ownUserId, role)
                            // FCM (persiapan, butir 4): kirim token ke server
                            // bila Firebase terinisialisasi; bila belum ada
                            // google-services.json, gagal diam-diam.
                            FcmTokenSync.sync(this@MainActivity)
                            // Antrean offline petugas (butir 7): kirim ulang
                            // saat aplikasi dibuka.
                            OfflineQueue.syncAll(this@MainActivity)
                        }
                    }
                    override fun onFailure(c: Call<Map<String, Any>>, t: Throwable) {
                        userName = "Error loading"
                    }
                })
        }

        loadMe()

        // Live wallet: a timbang / topup / tarik anywhere (desktop, another
        // device) updates this screen's saldo and poin without reopening it.
        RealtimeEventHub.addListener("main") { event ->
            if (event.type == RealtimeEventType.WALLET_UPDATED &&
                event.payloadLong("userId") == ownUserId
            ) {
                loadMe()
            }
        }

        // Antrean offline (butir 7): kirim ulang saat /ws/events
        // tersambung kembali (koneksi pulih).
        RealtimeEventHub.addConnectListener("main") {
            OfflineQueue.syncAll(this@MainActivity)
        }

        setContent {
            ResikTheme {
                if (role.equals("BANK_SAMPAH", ignoreCase = true)) {
                    BankSampahHomeScreen(
                        userName = userName,
                        onLogout = {
                            ApiClient.logout()
                            startActivity(Intent(this, WelcomeActivity::class.java))
                            finish()
                        },
                        onNavChat = { startActivity(Intent(this, ChatActivity::class.java)) },
                        onNavProfil = { startActivity(Intent(this, ProfilActivity::class.java)) },
                        onNavRiwayat = { startActivity(Intent(this, RiwayatActivity::class.java)) },
                    )
                } else {
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
                        onNavMisi = { startActivity(Intent(this, MisiActivity::class.java)) },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        RealtimeEventHub.removeListener("main")
        RealtimeEventHub.removeConnectListener("main")
        RealtimeEventHub.disconnect()
        super.onDestroy()
    }
}
