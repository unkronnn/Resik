package com.resik.client.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.resik.client.api.ApiClient
import com.resik.client.model.AuthResponse
import com.resik.client.model.RegisterRequest
import com.resik.client.ui.screens.RegisterScreen
import com.resik.client.ui.theme.ResikTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResikTheme {
                var isLoading by remember { mutableStateOf(false) }
                var error by remember { mutableStateOf<String?>(null) }

                RegisterScreen(
                    onRegister = { nama, email, password, alamat, telp ->
                        if (nama.isBlank() || email.isBlank() || password.isBlank()) {
                            error = "Isi semua field wajib"
                            return@RegisterScreen
                        }
                        isLoading = true
                        error = null
                        ApiClient.getApi().register(RegisterRequest(nama, email, password, alamat, telp))
                            .enqueue(object : Callback<AuthResponse> {
                                override fun onResponse(c: Call<AuthResponse>, r: Response<AuthResponse>) {
                                    isLoading = false
                                    if (r.isSuccessful && r.body() != null) {
                                        val auth = r.body()!!
                                        ApiClient.saveToken(auth.token)
                                        ApiClient.saveUserId(auth.userId)
                                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                                        finish()
                                    } else {
                                        error = "Email sudah terdaftar"
                                    }
                                }
                                override fun onFailure(c: Call<AuthResponse>, t: Throwable) {
                                    isLoading = false
                                    error = "Koneksi gagal"
                                }
                            })
                    },
                    onLogin = { finish() },
                    isLoading = isLoading,
                    error = error
                )
            }
        }
    }
}
