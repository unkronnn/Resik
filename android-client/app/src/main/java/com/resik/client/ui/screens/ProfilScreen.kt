package com.resik.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resik.client.ui.theme.*

@Composable
fun ProfilScreen(
    nama: String,
    email: String,
    alamat: String,
    telp: String,
    role: String,
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(GreenDark, GreenPrimary)))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onBack) { Text("←", fontSize = 24.sp, color = BgWhite) }
            }
            Box(
                modifier = Modifier.size(96.dp).clip(CircleShape).background(BgWhite.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) { Text("👤", fontSize = 44.sp) }
            Spacer(Modifier.height(12.dp))
            Text(nama, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BgWhite)
            Text(role, fontSize = 12.sp, color = GreenMint)
        }

        // Info
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Informasi Akun", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(12.dp))

            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BgWhite)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    InfoRow("✉", "Email", email, Color(0xFFE0F2FE))
                    Spacer(Modifier.height(16.dp))
                    InfoRow("📍", "Alamat", alamat, GreenMint)
                    Spacer(Modifier.height(16.dp))
                    InfoRow("📞", "No. Telepon", telp, OrangeLight)
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedLight,
                    contentColor = RedLogout
                )
            ) { Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun InfoRow(icon: String, label: String, value: String, bgColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(bgColor),
            contentAlignment = Alignment.Center
        ) { Text(icon, fontSize = 18.sp) }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = TextSecondary)
            Text(value, fontSize = 14.sp, color = TextPrimary)
        }
    }
}
