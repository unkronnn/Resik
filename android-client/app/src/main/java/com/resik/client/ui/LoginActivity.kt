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
import com.resik.client.R
import com.resik.client.api.ApiClient
import com.resik.client.model.AuthResponse
import com.resik.client.model.LoginRequest
import com.resik.client.ui.screens.LoginScreen
import com.resik.client.ui.theme.ResikTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(this)

        if (ApiClient.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContent {
            ResikTheme {
                var isLoading by remember { mutableStateOf(false) }
                var error by remember { mutableStateOf<String?>(null) }

                LoginScreen(
                    onLogin = { email, password ->
                        if (email.isBlank() || password.isBlank()) {
                            error = "Isi email dan password"
                            return@LoginScreen
                        }
                        isLoading = true
                        error = null
                        ApiClient.getApi().login(LoginRequest(email, password))
                            .enqueue(object : Callback<AuthResponse> {
                                override fun onResponse(c: Call<AuthResponse>, r: Response<AuthResponse>) {
                                    isLoading = false
                                    if (r.isSuccessful && r.body() != null) {
                                        val auth = r.body()!!
                                        ApiClient.saveToken(auth.token)
                                        ApiClient.saveUserId(auth.userId)
                                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                        finish()
                                    } else {
                                        error = "Login gagal. Periksa email & password."
                                    }
                                }
                                override fun onFailure(c: Call<AuthResponse>, t: Throwable) {
                                    isLoading = false
                                    error = "Koneksi gagal: ${t.message}"
                                }
                            })
                    },
                    onRegister = { startActivity(Intent(this, RegisterActivity::class.java)) },
                    onForgotPassword = { Toast.makeText(this, "Fitur lupa password akan datang", Toast.LENGTH_SHORT).show() },
                    isLoading = isLoading,
                    error = error
                )
            }
        }
    }
}
