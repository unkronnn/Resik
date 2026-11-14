package com.resik.client.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resik.client.ui.theme.*

data class BankPickup(
    val id: String,
    val nama: String,
    val alamat: String,
    val waktu: String,
    val status: String,
    val lat: Double,
    val lng: Double
)

@Composable
fun BankSampahHomeScreen(
    userName: String,
    onLogout: () -> Unit,
    onNavChat: () -> Unit,
    onNavProfil: () -> Unit,
    onNavRiwayat: () -> Unit,
) {
    val context = LocalContext.current
    val pickups = remember {
        listOf(
            BankPickup("#004", "Rina Marlina", "Jl. Dipatiukur No. 7, Bandung", "09:00-11:00", "DIJADWALKAN", -6.8898, 107.6160),
            BankPickup("#005", "Dewi Anggraini", "Jl. Asia Afrika No. 88, Bandung", "13:00-15:00", "DIPROSES", -6.9210, 107.6070),
            BankPickup("#006", "Andi Saputra", "Jl. Merdeka No. 45, Bandung", "15:30-17:00", "MENUNGGU", -6.9147, 107.6098),
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(GreenDark, GreenPrimary)))
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Bank Sampah", fontSize = 13.sp, color = GreenMint)
                    Text(userName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BgWhite)
                }
                IconButton(onClick = onLogout, modifier = Modifier.size(40.dp).clip(CircleShape).background(RedLogout.copy(alpha = 0.15f))) {
                    Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = RedLogout, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BgWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(20.dp))
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Penjemputan Hari Ini", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("${pickups.size} penjemputan", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                    Text("Buka Google Maps untuk navigasi & tracking realtime", fontSize = 11.sp, color = TextHint)
                }
            }
        }

        Text("Daftar Penjemputan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(20.dp))

        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            pickups.forEach { p ->
                PickupCard(
                    p = p,
                    onNavigate = {
                        val uri = Uri.parse("google.navigation:q=${p.lat},${p.lng}&mode=d")
                        val mapsUri = Uri.parse("https://maps.google.com/?daddr=${p.lat},${p.lng}")
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                            .onFailure { context.startActivity(Intent(Intent.ACTION_VIEW, mapsUri)) }
                    }
                )
            }
        }

        BottomNav(
            activeTab = "home",
            onHome = { },
            onRiwayat = onNavRiwayat,
            onChat = onNavChat,
            onProfil = onNavProfil,
        )
    }
}

@Composable
fun PickupCard(p: BankPickup, onNavigate: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BgWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(18.dp))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(p.id, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextHint)
                Spacer(Modifier.width(8.dp))
                Text(p.nama, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                StatusPill(p.status)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(p.alamat, fontSize = 13.sp, color = TextSecondary)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Schedule, contentDescription = null, tint = TextHint, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(p.waktu, fontSize = 12.sp, color = TextHint)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onNavigate,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                contentPadding = PaddingValues(vertical = 10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Navigasi via Google Maps", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatusPill(status: String) {
    val (bg, fg) = when (status) {
        "SELESAI" -> GreenPale to GreenPrimary
        "DIPROSES" -> Color(0xFFF3E8FF) to Color(0xFF6D28D9)
        "DIJADWALKAN" -> Color(0xFFEDF4FE) to Color(0xFF2563EB)
        else -> Color(0xFFFFF7E8) to Color(0xFFD97706)
    }
    Box(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}
