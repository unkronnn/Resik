package com.resik.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resik.client.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class ChatMsg(val text: String, val fromMe: Boolean, val time: String)
data class PenjemputanItem(val id: Int, val alamat: String, val tanggal: String, val status: String, val catatan: String)
data class TransaksiItem(val id: Int, val tanggal: String, val jumlah: String, val status: String, val jenis: String)
data class EdukasiItem(val judul: String, val kategori: String, val deskripsi: String, val icon: ImageVector)

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
    EdukasiItem("Sampah Organik", "ORGANIK", "Sisa makanan, daun, rumput kering. Dapat diolah jadi kompos.", Icons.Filled.Eco),
    EdukasiItem("Plastik Botol", "ANORGANIK", "Botol plastik bekas. Harga Rp 2.500/kg. Bersihkan sebelum disetor.", Icons.Filled.Recycling),
    EdukasiItem("Kardus/Karton", "ANORGANIK", "Kardus dan karton bekas. Harga Rp 1.500/kg. Lipat untuk hemat tempat.", Icons.Filled.Inventory2),
    EdukasiItem("Kaleng Aluminium", "ANORGANIK", "Kaleng minuman. Harga Rp 5.000/kg. Paling berharga!", Icons.Filled.Recycling),
    EdukasiItem("Baterai Bekas", "B3", "Baterai dan aki bekas. Harus dipisahkan, jangan campur dengan sampah lain.", Icons.Filled.Warning),
)
val leaderboardData = listOf(
    Triple("Syukron Maulana", "320 poin", 1),
    Triple("Zain Ghathfaan", "280 poin", 2),
    Triple("Riri Syafala", "250 poin", 3),
    Triple("Ahmad Fauzi", "180 poin", 4),
    Triple("Siti Nurhaliza", "120 poin", 5),
)

@Composable
fun GradientHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(GreenDark, GreenPrimary))).padding(horizontal = 8.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = BgWhite) }
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BgWhite)
    }
}

// ===== CHAT SCREEN =====
@Composable
fun ChatScreen(onBack: () -> Unit) {
    val messages = remember { mutableStateListOf(
        ChatMsg("Selamat datang di Resik! Ada yang bisa kami bantu?", false, "09:00"),
    )}
    var input by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(GreenDark, GreenPrimary))).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = BgWhite) }
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(BgWhite.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = BgWhite, modifier = Modifier.size(24.dp))
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
            items(messages) { msg -> ChatBubble(msg) }
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
                        val reply = when {
                            input.contains("jemput", ignoreCase = true) -> "Untuk penjemputan, buka menu Jemput Sampah. Petugas akan datang sesuai jadwal."
                            input.contains("harga", ignoreCase = true) -> "Harga: Plastik Rp 2.500/kg, Kardus Rp 1.500/kg, Kaleng Rp 5.000/kg."
                            input.contains("saldo", ignoreCase = true) || input.contains("dompet", ignoreCase = true) -> "Saldo bisa dilihat di Dompet Digital. Saldo masuk otomatis setelah setoran selesai."
                            else -> "Terima kasih! Admin akan segera membalas."
                        }
                        input = ""
                        messages.add(ChatMsg(reply, false, SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())))
                    }
                },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Send, contentDescription = "Send", tint = BgWhite, modifier = Modifier.size(20.dp)) }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMsg) {
    val isMe = msg.fromMe
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isMe) 16.dp else 4.dp, bottomEnd = if (isMe) 4.dp else 16.dp))
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

// ===== PENJEMPUTAN SCREEN =====
@Composable
fun PenjemputanScreen(onBack: () -> Unit, onCreate: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        GradientHeader("Penjemputan Sampah", onBack)
        Button(
            onClick = onCreate,
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Jemput Sampah Sekarang", color = BgWhite, fontWeight = FontWeight.Bold)
        }
        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dummyPenjemputan) { item ->
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BgWhite)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("#${String.format("%03d", item.id)}", fontSize = 12.sp, color = TextHint, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            StatusBadge(item.status)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(item.alamat, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = TextHint, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(item.tanggal, fontSize = 12.sp, color = TextSecondary)
                        }
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
    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(status, fontSize = 10.sp, color = fg, fontWeight = FontWeight.Bold)
    }
}

// ===== RIWAYAT SCREEN =====
@Composable
fun RiwayatScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        GradientHeader("Riwayat Transaksi", onBack)
        LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dummyTransaksi) { item ->
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BgWhite)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(GreenMint), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Paid, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
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
        Column(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(GreenDark, GreenPrimary))).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = BgWhite) }
                Text("Dompet Digital", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BgWhite)
            }
            Spacer(Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(BgWhite.copy(alpha = 0.15f)).padding(24.dp)) {
                Column {
                    Text("Total Saldo", fontSize = 13.sp, color = GreenMint)
                    Text(saldo, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = BgWhite)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("$poin poin", fontSize = 14.sp, color = OrangeAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Text("Riwayat Transaksi", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(20.dp))
        LazyColumn(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dummyTransaksi) { item ->
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = BgWhite)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
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
            leaderboardData.forEachIndexed { i, (nama, pts, rank) ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = if (i == 0) GreenMint else BgWhite)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        val medalIcon = when(rank) { 1 -> Icons.Filled.EmojiEvents; 2 -> Icons.Filled.EmojiEvents; 3 -> Icons.Filled.EmojiEvents; else -> Icons.Filled.Person }
                        val medalColor = when(rank) { 1 -> Color(0xFFF39C12); 2 -> Color(0xFF94A3B8); 3 -> Color(0xFFCD7F32); else -> TextHint }
                        Icon(medalIcon, contentDescription = null, tint = medalColor, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(nama, fontSize = 14.sp, color = TextPrimary, fontWeight = if (i < 3) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                        Text(pts, fontSize = 14.sp, color = if (i < 3) GreenPrimary else TextSecondary, fontWeight = FontWeight.Bold)
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
                ) { Text(f, fontSize = 12.sp, color = if (filter == f) BgWhite else TextSecondary, fontWeight = if (filter == f) FontWeight.Bold else FontWeight.Normal) }
            }
        }
        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered) { item ->
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BgWhite)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(GreenMint), contentAlignment = Alignment.Center) {
                            Icon(item.icon, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(24.dp))
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
    val locations = listOf(
        Triple("Bank Sampah Resik Pusat", "Jl. Merdeka 45, Bandung", "0.5 km"),
        Triple("Bank Sampah Sukajadi", "Jl. Sukajadi 12, Bandung", "1.2 km"),
        Triple("Bank Sampah Coblong", "Jl. Dipatiukur 7, Bandung", "2.1 km"),
    )
    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        GradientHeader("Lokasi Bank Sampah", onBack)
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            locations.forEach { (name, addr, dist) ->
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BgWhite)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFFCE7F3)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text(addr, fontSize = 11.sp, color = TextHint)
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
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = BgWhite) }
            Text("Scan QR Code", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BgWhite)
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(240.dp).clip(RoundedCornerShape(20.dp)).background(BgWhite.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = BgWhite, modifier = Modifier.size(64.dp))
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
