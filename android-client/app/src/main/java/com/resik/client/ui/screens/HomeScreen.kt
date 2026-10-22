package com.resik.client.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resik.client.ui.theme.*

data class MenuItem(val label: String, val icon: String, val bgColor: Color, val onClick: () -> Unit)

@Composable
fun HomeScreen(
    userName: String,
    saldo: String,
    poin: String,
    onLogout: () -> Unit,
    onNavEdukasi: () -> Unit,
    onNavPenjemputan: () -> Unit,
    onNavRiwayat: () -> Unit,
    onNavChat: () -> Unit,
    onNavMap: () -> Unit,
    onNavQr: () -> Unit,
    onNavDompet: () -> Unit,
    onNavLeaderboard: () -> Unit,
    onNavProfil: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        // Gradient header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(GreenDark, GreenPrimary)))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Selamat Datang 👋", fontSize = 14.sp, color = GreenMint)
                    Text(userName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BgWhite)
                }
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(RedLogout.copy(alpha = 0.15f)).clickable { onLogout() },
                    contentAlignment = Alignment.Center
                ) { Text("⏻", fontSize = 22.sp, color = RedLogout) }
            }
            Spacer(Modifier.height(20.dp))

            // Balance card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgWhite)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Saldo", fontSize = 13.sp, color = TextSecondary)
                    Text(saldo, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                }
                Box(modifier = Modifier.width(1.dp).height(48.dp).background(BorderLight))
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text("Poin", fontSize = 13.sp, color = TextSecondary)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(poin, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)
                        Text(" pts", fontSize = 14.sp, color = OrangeAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Menu grid
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Menu", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(16.dp))

            val menus = listOf(
                MenuItem("Jemput\nSampah", "🚚", GreenMint, onNavPenjemputan),
                MenuItem("Edukasi", "📚", OrangeLight, onNavEdukasi),
                MenuItem("Riwayat", "📋", Color(0xFFE0F2FE), onNavRiwayat),
                MenuItem("Dompet", "💰", Color(0xFFFEF3C7), onNavDompet),
                MenuItem("Lokasi", "📍", Color(0xFFFCE7F3), onNavMap),
                MenuItem("Scan QR", "📷", Color(0xFFE0E7FF), onNavQr),
                MenuItem("Leaderboard", "🏆", Color(0xFFFEF3C7), onNavLeaderboard),
                MenuItem("Chat", "💬", GreenMint, onNavChat),
            )

            menus.chunked(2).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    row.forEach { item ->
                        MenuCard(item, modifier = Modifier.weight(1f).padding(horizontal = 3.dp))
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun MenuCard(item: MenuItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable { item.onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BgWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(item.bgColor),
                contentAlignment = Alignment.Center
            ) { Text(item.icon, fontSize = 24.sp) }
            Spacer(Modifier.height(8.dp))
            Text(item.label, fontSize = 12.sp, color = TextPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
