package com.resik.client.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.resik.client.api.ApiClient
import com.resik.client.ui.screens.ProfilScreen
import com.resik.client.ui.theme.ResikTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var nama by mutableStateOf("Loading...")
        var email by mutableStateOf("-")
        var alamat by mutableStateOf("-")
        var telp by mutableStateOf("-")
        var role by mutableStateOf("MASYARAKAT")

        ApiClient.getApi().getMe(ApiClient.getAuthHeader())
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(c: Call<Map<String, Any>>, r: Response<Map<String, Any>>) {
                    if (r.isSuccessful && r.body() != null) {
                        val u = r.body()!!
                        nama = u["nama"]?.toString() ?: "User"
                        email = u["email"]?.toString() ?: "-"
                        alamat = u["alamat"]?.toString() ?: "-"
                        telp = u["noTelp"]?.toString() ?: "-"
                        role = u["role"]?.toString() ?: "MASYARAKAT"
                    }
                }
                override fun onFailure(c: Call<Map<String, Any>>, t: Throwable) { nama = "Error" }
            })

        setContent {
            ResikTheme {
                ProfilScreen(
                    nama = nama, email = email, alamat = alamat, telp = telp, role = role,
                    onBack = { finish() },
                    onLogout = {
                        ApiClient.logout()
                        startActivity(Intent(this, WelcomeActivity::class.java))
                        finishAffinity()
                    }
                )
            }
        }
    }
}
