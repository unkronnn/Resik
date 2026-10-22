package com.resik.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
fun GradientHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(GreenDark, GreenPrimary)))
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
            Text("←", fontSize = 24.sp, color = BgWhite)
        }
        Spacer(Modifier.width(8.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BgWhite)
    }
}

@Composable
fun SimpleScreen(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        GradientHeader(title, onBack)
        content()
    }
}

@Composable
fun EdukasiScreen(onBack: () -> Unit) {
    SimpleScreen("Edukasi Sampah", onBack) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                FilterChip("Semua", true)
                Spacer(Modifier.width(8.dp))
                FilterChip("Organik", false)
                Spacer(Modifier.width(8.dp))
                FilterChip("Anorganik", false)
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, active: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (active) GreenPrimary else BgLight)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(label, fontSize = 13.sp, color = if (active) BgWhite else TextSecondary, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun DompetScreen(saldo: String, poin: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(GreenDark, GreenPrimary)))
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) { Text("←", fontSize = 24.sp, color = BgWhite) }
                Spacer(Modifier.width(8.dp))
                Text("Dompet Digital", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BgWhite)
            }
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(BgWhite.copy(alpha = 0.15f)).padding(24.dp)
            ) {
                Column {
                    Text("Total Saldo", fontSize = 13.sp, color = GreenMint)
                    Text(saldo, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = BgWhite)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎯", fontSize = 18.sp)
                        Spacer(Modifier.width(6.dp))
                        Text("$poin poin", fontSize = 14.sp, color = OrangeAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Text("Riwayat Transaksi", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(20.dp))
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Belum ada transaksi", fontSize = 14.sp, color = TextHint)
        }
    }
}

@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        GradientHeader("Leaderboard", onBack)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🏆", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text("Leaderboard Kontribusi", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Data akan dimuat dari server", fontSize = 13.sp, color = TextHint)
            }
        }
    }
}

@Composable
fun ChatScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        GradientHeader("Chat dengan Admin", onBack)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chat akan dimuat dari server", fontSize = 14.sp, color = TextHint)
        }
    }
}

@Composable
fun PenjemputanScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        GradientHeader("Penjemputan Sampah", onBack)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Daftar penjemputan akan dimuat", fontSize = 14.sp, color = TextHint)
        }
    }
}

@Composable
fun RiwayatScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        GradientHeader("Riwayat Transaksi", onBack)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Riwayat akan dimuat dari server", fontSize = 14.sp, color = TextHint)
        }
    }
}

@Composable
fun MapScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        GradientHeader("Lokasi Bank Sampah", onBack)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Map akan ditampilkan di sini", fontSize = 14.sp, color = TextHint)
        }
    }
}

@Composable
fun QrScannerScreen(onBack: () -> Unit, onScan: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(GreenDark)) {
        Row(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) { Text("←", fontSize = 24.sp, color = BgWhite) }
            Spacer(Modifier.width(8.dp))
            Text("Scan QR Code", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BgWhite)
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(240.dp).clip(RoundedCornerShape(20.dp)).background(BgWhite.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📷", fontSize = 56.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Arahkan kamera ke QR Code", fontSize = 13.sp, color = GreenMint)
                    }
                }
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onScan,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BgWhite)
                ) { Text("Mulai Scan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenPrimary) }
            }
        }
    }
}
