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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resik.client.ui.theme.*

@Composable
fun RegisterScreen(
    onRegister: (String, String, String, String, String) -> Unit,
    onLogin: () -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var alamat by remember { mutableStateOf("") }
    var telp by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .padding(horizontal = 28.dp)
            .padding(top = 48.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = "RESIK Logo",
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(BgWhite)
                .padding(6.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text("Buat Akun Baru", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Daftar untuk mulai berkontribusi", fontSize = 14.sp, color = TextSecondary)
        Spacer(Modifier.height(28.dp))

        val fields = listOf(
            "Nama Lengkap" to { v: String -> nama = v },
            "Email" to { v: String -> email = v },
            "Password" to { v: String -> password = v },
            "Alamat" to { v: String -> alamat = v },
            "No. Telepon" to { v: String -> telp = v },
        )

        fields.forEachIndexed { i, (label, setter) ->
            Column(modifier = Modifier.fillMaxWidth()) {
                if (i > 0) Spacer(Modifier.height(14.dp))
                Text(label, fontSize = 13.sp, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = when(i) { 0 -> nama; 1 -> email; 2 -> password; 3 -> alamat; else -> telp },
                    onValueChange = setter,
                    placeholder = { Text("masukkan ${label.lowercase()}", fontSize = 14.sp, color = TextHint) },
                    singleLine = true,
                    visualTransformation = if (i == 2) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = when(i) { 1 -> KeyboardType.Email; 2 -> KeyboardType.Password; 4 -> KeyboardType.Phone; else -> KeyboardType.Text }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = BorderLight,
                    )
                )
            }
        }

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error, fontSize = 13.sp, color = RedLogout)
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { onRegister(nama, email, password, alamat, telp) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BgWhite, strokeWidth = 2.dp)
            else Text("Daftar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.Center) {
            Text("Sudah punya akun? ", fontSize = 14.sp, color = TextSecondary)
            TextButton(onClick = onLogin, contentPadding = PaddingValues(0.dp)) {
                Text("Masuk di sini", fontSize = 14.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
