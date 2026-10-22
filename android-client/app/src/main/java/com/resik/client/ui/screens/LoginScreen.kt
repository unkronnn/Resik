package com.resik.client.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.resik.client.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resik.client.ui.theme.*

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .padding(horizontal = 28.dp)
            .padding(top = 60.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = "RESIK Logo",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(BgWhite)
                .padding(8.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text("RESIK", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
        Text("Bank Sampah Digital", fontSize = 13.sp, color = TextSecondary)
        Spacer(Modifier.height(32.dp))

        // Email
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Email", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("masukkan email", fontSize = 14.sp, color = TextHint) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = BorderLight,
                )
            )
        }

        // Password
        Spacer(Modifier.height(14.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Password", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("masukkan password", fontSize = 14.sp, color = TextHint) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(if (passwordVisible) "🙈" else "👁", fontSize = 16.sp)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = BorderLight,
                )
            )
        }

        // Forgot password
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onForgotPassword) {
                Text("Lupa Password?", fontSize = 13.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        }

        // Error
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error, fontSize = 13.sp, color = RedLogout)
        }

        // Login button
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = BgWhite,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Masuk", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Register link
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.Center) {
            Text("Belum punya akun? ", fontSize = 14.sp, color = TextSecondary)
            TextButton(onClick = onRegister, contentPadding = PaddingValues(0.dp)) {
                Text("Daftar di sini", fontSize = 14.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
