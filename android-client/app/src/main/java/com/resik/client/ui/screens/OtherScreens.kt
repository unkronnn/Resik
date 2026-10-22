package com.resik.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resik.client.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ===== Data models untuk local state =====
data class ChatMsg(val text: String, val fromMe: Boolean, val time: String)
data class PenjemputanItem(val id: Int, val alamat: String, val tanggal: String, val status: String, val catatan: String)
data class TransaksiItem(val id: Int, val tanggal: String, val jumlah: String, val status: String, val jenis: String)
data class EdukasiItem(val judul: String, val kategori: String, val deskripsi: String, val icon: String)

// ===== Dummy data (match dengan admin desktop) =====
val dummyPenjemputan = listOf(
    PenjemputanItem(1, "Jl. Merdeka No. 45", "2026-10-22", "MENUNGGU", "Sampah rumah tangga campuran"),
    PenjemputanItem(2, "Jl. Sudirman No. 12", "2026-10-22", "DIJADWALKAN", "Botol plastik dan kardus"),
    PenjemputanItem(3, "Jl. Asia Afrika No. 88", "2026-10-21", "DIPROSES", "Menunggu timbangan"),
    PenjemputanItem(4, "Jl. Dipatiukur No. 7", "2026-10-20", "SELESAI", "Selesai dibayar Rp 15.500"),
    PenjemputanItem(5, "Jl. Cihampelas No. 100", "2026-10-20", "SELESAI", "Selesai dibayar Rp 8.000"),
)
val dummyTransaksi = listOf(
    TransaksiItem(1, "2026-10-20", "Rp 15.500", "BERHASIL", "Setor Sampah"),
    TransaksiItem(2, "2026-10-20", "Rp 8.000", "BERHASIL", "Setor Sampah"),
    TransaksiItem(3, "2026-10-19", "Rp 22.500", "BERHASIL", "Setor Sampah"),
)
val dummyEdukasi = listOf(
    EdukasiItem("Sampah Organik", "ORGANIK", "Sisa makanan, daun, rumput kering. Dapat diolah jadi kompos.", "🍃"),
    EdukasiItem("Plastik Botol", "ANORGANIK", "Botol plastik bekas. Harga Rp 2.500/kg. Bersihkan sebelum disetor.", "🍾"),
    EdukasiItem("Kardus/Karton", "ANORGANIK", "Kardus dan karton bekas. Harga Rp 1.500/kg. Lipat untuk hemat tempat.", "📦"),
    EdukasiItem("Kaleng Aluminium", "ANORGANIK", "Kaleng minuman. Harga Rp 5.000/kg. Paling berharga!", "🥫"),
    EdukasiItem("Baterai Bekas", "B3", "Baterai dan aki bekas. Harus dipisahkan, jangan campur dengan sampah lain.", "🔋"),
)
val leaderboardData = listOf(
    "🥇 Syukron Maulana — 320 poin" to "#F39C12",
    "🥈 Zain Ghathfaan — 280 poin" to "#94A3B8",
    "🥉 Riri Syafala — 250 poin" to "#CD7F32",
    "4. Ahmad Fauzi — 180 poin" to "#94A3B8",
    "5. Siti Nurhaliza — 120 poin" to "#94A3B8",
)

// ===== Shared header =====
@Composable
fun GradientHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(GreenDark, GreenPrimary))).padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) { Text("←", fontSize = 24.sp, color = BgWhite) }
        Spacer(Modifier.width(8.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BgWhite)
    }
}

// ===== CHAT SCREEN — berfungsi dengan local state =====
@Composable
fun ChatScreen(onBack: () -> Unit) {
    val messages = remember { mutableStateListOf(
        ChatMsg("Selamat datang di Resik! Ada yang bisa kami bantu?", false, "09:00"),
    )}
    var input by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(GreenDark, GreenPrimary))).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) { Text("←", fontSize = 24.sp, color = BgWhite) }
            Spacer(Modifier.width(8.dp))
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(BgWhite.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Text("👨", fontSize = 22.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Admin Resik", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BgWhite)
                Text("Online", fontSize = 11.sp, color = GreenMint)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp).background(BgWhite),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Ketik pesan...", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = BorderLight)
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(GreenPrimary).clickable {
                    if (input.isNotBlank()) {
                        val now = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                        messages.add(ChatMsg(input.trim(), true, now))
                        input = ""
                        // Auto-reply dari admin
                        val reply = when {
                            input.contains("jemput", ignoreCase = true) -> "Untuk penjemputan, silakan buka menu Jemput Sampah di home ya. Petugas akan datang sesuai jadwal."
                            input.contains("harga", ignoreCase = true) -> "Harga sampah: Plastik Rp 2.500/kg, Kardus Rp 1.500/kg, Kaleng Rp 5.000/kg. Lihat menu Edukasi untuk detail."
                            input.contains("saldo", ignoreCase = true) || input.contains("dompet", ignoreCase = true) -> "Saldo dan poin Anda bisa dilihat di menu Dompet Digital. Saldo otomatis masuk setelah setoran selesai ditimbang."
                            else -> "Terima kasih atas pesan Anda! Admin akan segera membalas. Untuk bantuan cepat, cek menu Edukasi."
                        }
                        messages.add(ChatMsg(reply, false, SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())))
                    }
                },
                contentAlignment = Alignment.Center
            ) { Text("➤", fontSize = 20.sp, color = BgWhite) }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMsg) {
    val isMe = msg.fromMe
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ))
                .background(if (isMe) GreenPrimary else BgWhite)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 280.dp)
        ) {
            Column {
                Text(msg.text, fontSize = 14.sp, color = if (isMe) BgWhite else TextPrimary)
                Text(msg.time, fontSize = 10.sp, color = if (isMe) GreenMint else TextHint)
            }
        }
    }
}

// ===== PENJEMPUTAN SCREEN — list dengan status badge =====
@Composable
fun PenjemputanScreen(onBack: () -> Unit, onCreate: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        GradientHeader("Penjemputan Sampah", onBack)
        Button(
            onClick = onCreate,
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
        ) { Text("+ Jemput Sampah Sekarang", color = BgWhite, fontWeight = FontWeight.Bold) }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dummyPenjemputan) { item ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BgWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("#${String.format("%03d", item.id)}", fontSize = 12.sp, color = TextHint, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            StatusBadge(item.status)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(item.alamat, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text(item.tanggal, fontSize = 12.sp, color = TextSecondary)
                        Spacer(Modifier.height(4.dp))
                        Text(item.catatan, fontSize = 12.sp, color = TextHint)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bg, fg) = when(status) {
        "MENUNGGU" -> OrangeLight to TextPrimary
        "DIJADWALKAN" -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
        "DIPROSES" -> Color(0xFFF3E8FF) to Color(0xFF6B21A8)
        "SELESAI" -> GreenMint to GreenPrimary
        "DIBATALKAN" -> RedLight to RedLogout
        else -> BgLight to TextSecondary
    }
    Box(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp)
    ) { Text(status, fontSize = 10.sp, color = fg, fontWeight = FontWeight.Bold) }
}

// ===== RIWAYAT SCREEN — transaksi selesai =====
@Composable
fun RiwayatScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        GradientHeader("Riwayat Transaksi", onBack)
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dummyTransaksi) { item ->
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BgWhite)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(GreenMint), contentAlignment = Alignment.Center) {
                            Text("💰", fontSize = 20.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.jenis, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text(item.tanggal, fontSize = 12.sp, color = TextHint)
                        }
                        Text(item.jumlah, fontSize = 16.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ===== DOMPET SCREEN =====
@Composable
fun DompetScreen(saldo: String, poin: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        Column(
            modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(GreenDark, GreenPrimary))).padding(24.dp)
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
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dummyTransaksi) { item ->
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = BgWhite)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.jenis, fontSize = 13.sp, color = TextPrimary)
                            Text(item.tanggal, fontSize = 11.sp, color = TextHint)
                        }
                        Text("+${item.jumlah}", fontSize = 14.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ===== LEADERBOARD SCREEN =====
@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        GradientHeader("Leaderboard", onBack)
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            leaderboardData.forEachIndexed { i, (text, color) ->
                Card(
                    shape = RoundedCornerShape(if (i < 3) 20.dp else 16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (i == 0) GreenMint else BgWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text, fontSize = 14.sp, color = TextPrimary, fontWeight = if (i < 3) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

// ===== EDUKASI SCREEN =====
@Composable
fun EdukasiScreen(onBack: () -> Unit) {
    var filter by remember { mutableStateOf("Semua") }
    val filters = listOf("Semua", "ORGANIK", "ANORGANIK", "B3")
    val filtered = if (filter == "Semua") dummyEdukasi else dummyEdukasi.filter { it.kategori == filter }

    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        GradientHeader("Edukasi Sampah", onBack)
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            filters.forEach { f ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (filter == f) GreenPrimary else BgWhite)
                        .clickable { filter = f }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) { Text(f, fontSize = 12.sp, color = if (
filter == f) BgWhite else TextSecondary, fontWeight = if (filter == f) FontWeight.Bold else FontWeight.Normal) }
            }
        }
        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered) { item ->
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BgWhite)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(GreenMint), contentAlignment = Alignment.Center) {
                            Text(item.icon, fontSize = 24.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.judul, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text(item.kategori, fontSize = 10.sp, color = TextHint)
                            Spacer(Modifier.height(4.dp))
                            Text(item.deskripsi, fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

// ===== MAP SCREEN =====
@Composable
fun MapScreen(onBack: () -> Unit) {
    val locations = listOf("Bank Sampah Resik Pusat — Jl. Merdeka 45" to "0.5 km", "Bank Sampah Sukajadi — Jl. Sukajadi 12" to "1.2 km", "Bank Sampah Coblong — Jl. Dipatiukur 7" to "2.1 km")
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        GradientHeader("Lokasi Bank Sampah", onBack)
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            locations.forEach { (name, dist) ->
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BgWhite)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFFCE7F3)), contentAlignment = Alignment.Center) {
                            Text("📍", fontSize = 20.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, fontSize = 13.sp, color = TextPrimary)
                            Text("Jarak dari lokasi Anda", fontSize = 11.sp, color = TextHint)
                        }
                        Text(dist, fontSize = 12.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ===== QR SCANNER SCREEN =====
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
                Box(modifier = Modifier.size(240.dp).clip(RoundedCornerShape(20.dp)).background(BgWhite.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📷", fontSize = 56.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Arahkan kamera ke QR Code", fontSize = 13.sp, color = GreenMint)
                    }
                }
                Spacer(Modifier.height(32.dp))
                Button(onClick = onScan, shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = BgWhite)) {
                    Text("Mulai Scan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                }
            }
        }
    }
}
