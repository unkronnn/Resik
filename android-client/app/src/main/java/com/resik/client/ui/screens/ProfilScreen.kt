package com.resik.client.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resik.client.R
import com.resik.client.ui.theme.*

@Composable
fun ProfilScreen(
    nama: String, email: String, alamat: String, telp: String, role: String,
    onBack: () -> Unit, onLogout: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(GreenDark, GreenPrimary))).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = BgWhite) }
            }
            Image(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = "Resik Logo",
                modifier = Modifier.size(88.dp).clip(CircleShape).background(BgWhite).padding(8.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(nama, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BgWhite)
            Text(role, fontSize = 12.sp, color = GreenMint)
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Text("Informasi Akun", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(12.dp))

            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BgWhite)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    InfoRow(Icons.Filled.Email, "Email", email, Color(0xFFE0F2FE), Color(0xFF3B82F6))
                    Spacer(Modifier.height(16.dp))
                    InfoRow(Icons.Filled.LocationOn, "Alamat", alamat, GreenMint, GreenPrimary)
                    Spacer(Modifier.height(16.dp))
                    InfoRow(Icons.Filled.Phone, "No. Telepon", telp, OrangeLight, OrangeAccent)
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedLight, contentColor = RedLogout)
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String, bgColor: Color, iconTint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(bgColor),
            contentAlignment = Alignment.Center
        ) { Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = TextSecondary)
            Text(value, fontSize = 14.sp, color = TextPrimary)
        }
    }
}
