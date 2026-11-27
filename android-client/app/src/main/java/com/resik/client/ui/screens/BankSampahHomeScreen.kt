package com.resik.client.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.resik.client.api.ApiClient
import com.resik.client.api.OfflineQueue
import com.resik.client.api.RealtimeEventHub
import com.resik.client.api.RealtimeEventType
import com.resik.client.model.Penjemputan
import com.resik.client.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Satu jemputan untuk Beranda Bank Sampah. Semua field berasal dari
 * GET /api/penjemputan — tidak ada alamat atau koordinat yang dikarang.
 * lat/lng boleh null (penjemputan lama / tanpa GPS).
 */
data class BankPickup(
    val id: Long,
    val nama: String,
    val alamat: String,
    val tanggal: String,
    val waktu: String,
    val status: String,
    val lat: Double?,
    val lng: Double?,
    val details: List<Penjemputan.Detail> = emptyList(),
)

private fun Penjemputan.toBankPickup(): BankPickup {
    val nama = user?.nama ?: "-"
    return BankPickup(
        id = id ?: 0L,
        nama = nama,
        alamat = alamat ?: "-",
        tanggal = tanggalJemput ?: "-",
        waktu = waktuJemput ?: "-",
        status = status ?: "MENUNGGU",
        lat = latitude?.toDouble(),
        lng = longitude?.toDouble(),
        details = detailList ?: emptyList(),
    )
}

@Composable
fun BankSampahHomeScreen(
    userName: String,
    onLogout: () -> Unit,
    onNavChat: () -> Unit,
    onNavProfil: () -> Unit,
    onNavRiwayat: () -> Unit,
) {
    val context = LocalContext.current
    var pickups by remember { mutableStateOf<List<BankPickup>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var refreshTick by remember { mutableStateOf(0) }
    var timbangTarget by remember { mutableStateOf<BankPickup?>(null) }
    var pendingSync by remember { mutableStateOf(OfflineQueue.pendingCount(context)) }
    var syncMessage by remember { mutableStateOf<String?>(null) }

    fun loadFromServer() {
        ApiClient.getApi().getAllPenjemputan(ApiClient.getAuthHeader())
            .enqueue(object : Callback<List<Penjemputan>> {
                override fun onResponse(c: Call<List<Penjemputan>>, r: Response<List<Penjemputan>>) {
                    loading = false
                    if (r.isSuccessful && r.body() != null) {
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        pickups = r.body()!!
                            .filter { (it.tanggalJemput ?: "") == today }
                            .filter { it.status != "SELESAI" && it.status != "DIBATALKAN" }
                            .map { it.toBankPickup() }
                        loadError = null
                    } else {
                        loadError = "Gagal memuat penjemputan (${r.code()})."
                    }
                }
                override fun onFailure(c: Call<List<Penjemputan>>, t: Throwable) {
                    loading = false
                    loadError = "Tidak terhubung ke server — menampilkan data terakhir."
                }
            })
    }

    fun syncOfflineQueue() {
        OfflineQueue.syncAll(context) { synced, failed ->
            pendingSync = OfflineQueue.pendingCount(context)
            syncMessage = when {
                synced > 0 && failed == 0 -> "$synced timbang berhasil disinkronkan."
                synced > 0 -> "$synced timbang disinkronkan, $failed gagal (ditolak server)."
                failed > 0 -> "$failed timbang ditolak server dan dihapus dari antrean."
                else -> null
            }
            if (synced > 0) refreshTick++
        }
    }

    DisposableEffect(Unit) {
        RealtimeEventHub.addListener("bank-home") { event ->
            if (event.type == RealtimeEventType.PENJEMPUTAN_CREATED ||
                event.type == RealtimeEventType.PENJEMPUTAN_STATUS
            ) {
                refreshTick++
            }
        }
        // Saat kanal /ws/events tersambung kembali, koneksi sudah pulih:
        // kirim ulang antrean offline (butir 7).
        RealtimeEventHub.addConnectListener("bank-home") { syncOfflineQueue() }
        onDispose {
            RealtimeEventHub.removeListener("bank-home")
            RealtimeEventHub.removeConnectListener("bank-home")
        }
    }

    LaunchedEffect(refreshTick) { loadFromServer() }

    // Saat aplikasi dibuka: coba kirim ulang antrean offline (butir 7).
    LaunchedEffect(Unit) {
        pendingSync = OfflineQueue.pendingCount(context)
        syncOfflineQueue()
    }

    // Rute optimal (butir 8): nearest-neighbor bila ada koordinat, bila
    // tidak ada pakai urutan waktuJemput lalu alamat (lihat RouteUtil.kt).
    val orderedPickups = remember(pickups) {
        orderRoute(pickups.map {
            RouteStop(it.id, it.nama, it.alamat, it.waktu, it.lat, it.lng)
        }).mapNotNull { stop -> pickups.firstOrNull { it.id == stop.id } }
    }
    val mapsUrl = remember(orderedPickups) {
        buildMapsDirectionsUrl(orderedPickups.map {
            RouteStop(it.id, it.nama, it.alamat, it.waktu, it.lat, it.lng)
        })
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
                    Text("${orderedPickups.size} penjemputan", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                    Text("Diurutkan sebagai rute optimal. Buka Google Maps untuk navigasi.", fontSize = 11.sp, color = TextHint)
                }
            }
            // Status antrean offline (butir 7).
            if (pendingSync > 0) {
                Spacer(Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7E8)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CloudOff, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Menunggu sync: $pendingSync timbang", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Akan dikirim otomatis saat koneksi kembali.", fontSize = 11.sp, color = TextSecondary)
                        }
                        TextButton(onClick = { syncOfflineQueue() }) {
                            Text("Sinkronkan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                        }
                    }
                }
            }
            if (syncMessage != null) {
                Spacer(Modifier.height(8.dp))
                Text(syncMessage!!, fontSize = 12.sp, color = BgWhite)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Rute Penjemputan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (mapsUrl != null) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl)))
                    }
                },
                enabled = mapsUrl != null,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Buka Rute di Maps", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (mapsUrl == null && orderedPickups.isNotEmpty()) {
            Text(
                "Rute Maps tidak tersedia: tidak ada satu pun alamat/koordinat dari server.",
                fontSize = 12.sp, color = TextSecondary,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp),
            )
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else if (orderedPickups.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(20.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = TextHint, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Tidak ada penjemputan hari ini.", fontSize = 14.sp, color = TextSecondary)
                    if (loadError != null) Text(loadError!!, fontSize = 12.sp, color = TextHint)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(orderedPickups) { index, p ->
                    PickupCard(
                        p = p,
                        routeNumber = index + 1,
                        onNavigate = {
                            val uri = if (p.lat != null && p.lng != null) {
                                Uri.parse("google.navigation:q=${p.lat},${p.lng}&mode=d")
                            } else {
                                Uri.parse("google.navigation:q=${Uri.encode(p.alamat)}&mode=d")
                            }
                            val mapsUri = if (p.lat != null && p.lng != null) {
                                Uri.parse("https://maps.google.com/?daddr=${p.lat},${p.lng}")
                            } else {
                                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(p.alamat)}")
                            }
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                                .onFailure { context.startActivity(Intent(Intent.ACTION_VIEW, mapsUri)) }
                        },
                        onTimbang = { timbangTarget = p },
                    )
                }
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

    // Dialog timbang petugas (butir 7, memakai POST penjemputan/timbang
    // yang sudah ada) + foto bukti opsional (PUT foto-bukti).
    timbangTarget?.let { target ->
        TimbangDialog(
            pickup = target,
            onDismiss = { timbangTarget = null },
            onDone = { refreshTick++ },
            onQueuedOffline = { pendingSync = OfflineQueue.pendingCount(context) },
        )
    }
}

@Composable
fun PickupCard(p: BankPickup, routeNumber: Int, onNavigate: () -> Unit, onTimbang: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BgWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, BorderLight, RoundedCornerShape(18.dp))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(GreenPrimary),
                    contentAlignment = Alignment.Center,
                ) { Text("$routeNumber", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BgWhite) }
                Spacer(Modifier.width(8.dp))
                Text("#${String.format("%03d", p.id)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextHint)
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
                Text("${p.tanggal} • ${p.waktu}", fontSize = 12.sp, color = TextHint)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onNavigate,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Navigasi", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onTimbang,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Scale, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Timbang", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Dialog timbang petugas. Mengirim POST /api/penjemputan/timbang
 * {"penjemputanId","details":[{"detailId","beratAktual"}]}.
 * Bila jaringan gagal, operasi disimpan ke antrean offline (butir 7)
 * dan TIDAK hilang — status "Menunggu sync" tampil di beranda.
 * Foto bukti timbangan bersifat opsional; bila dipilih, dikirim setelah
 * timbang berhasil via PUT penjemputan/{id}/foto-bukti.
 */
@Composable
private fun TimbangDialog(
    pickup: BankPickup,
    onDismiss: () -> Unit,
    onDone: () -> Unit,
    onQueuedOffline: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val berat = remember { mutableStateMapOf<Long, String>() }
    var fotoBase64 by remember { mutableStateOf<String?>(null) }
    var fotoName by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val base64 = withContext(Dispatchers.IO) { encodePhotoBase64(context, uri) }
            if (base64 != null) {
                fotoBase64 = base64
                fotoName = "Foto bukti dipilih."
            } else {
                message = "Foto tidak dapat dibaca."
            }
        }
    }

    fun buildDetails(): List<OfflineQueue.TimbangDetailOp> =
        pickup.details.mapNotNull { d ->
            val kg = berat[d.id]?.toDoubleOrNull()
            if (d.id != null && kg != null && kg > 0) {
                OfflineQueue.TimbangDetailOp(detailId = d.id, beratAktual = kg)
            } else null
        }

    fun uploadFotoBukti() {
        val base64 = fotoBase64 ?: return
        val body = mapOf<String, Any>("fotoBase64" to base64, "contentType" to "image/jpeg")
        ApiClient.getApi().uploadFotoBukti(ApiClient.getAuthHeader(), pickup.id, body)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(c: Call<Map<String, Any>>, r: Response<Map<String, Any>>) {}
                override fun onFailure(c: Call<Map<String, Any>>, t: Throwable) {}
            })
    }

    fun submit() {
        val details = buildDetails()
        if (details.isEmpty()) {
            message = "Isi berat aktual minimal satu jenis sampah."
            return
        }
        busy = true
        message = null
        val body = mapOf<String, Any>(
            "penjemputanId" to pickup.id,
            "details" to details.map { d ->
                mapOf<String, Any>("detailId" to d.detailId, "beratAktual" to d.beratAktual)
            },
        )
        ApiClient.getApi().timbangSampah(ApiClient.getAuthHeader(), body)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(c: Call<Map<String, Any>>, r: Response<Map<String, Any>>) {
                    busy = false
                    if (r.isSuccessful) {
                        uploadFotoBukti()
                        onDone()
                        onDismiss()
                    } else {
                        // Ditolak server (4xx): jangan antrekan — mengulang
                        // tidak akan mengubah hasil.
                        message = "Timbang ditolak server (${r.code()}). Periksa berat yang diisi."
                    }
                }

                override fun onFailure(c: Call<Map<String, Any>>, t: Throwable) {
                    busy = false
                    // Jaringan gagal: simpan ke antrean offline (butir 7).
                    OfflineQueue.enqueueTimbang(context, pickup.id, details)
                    onQueuedOffline()
                    message = "Offline — timbang disimpan ke antrean dan akan dikirim otomatis saat koneksi kembali."
                }
            })
    }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        confirmButton = {
            Button(
                onClick = { submit() },
                enabled = !busy,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(12.dp),
            ) { Text(if (busy) "Mengirim..." else "Kirim Timbang", color = BgWhite) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) { Text("Batal", color = GreenPrimary) }
        },
        title = { Text("Timbang — #${pickup.id} ${pickup.nama}", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (pickup.details.isEmpty()) {
                    Text("Penjemputan ini belum punya rincian sampah dari server.", fontSize = 13.sp, color = TextSecondary)
                }
                pickup.details.forEach { d ->
                    val namaSampah = d.sampah?.nama ?: "Sampah #${d.id}"
                    val estimasi = d.estimasiBerat?.toDouble()
                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = BgLight)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(namaSampah, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                Text(
                                    if (estimasi != null) "Estimasi: $estimasi kg" else "Tanpa estimasi",
                                    fontSize = 12.sp, color = TextHint,
                                )
                            }
                            OutlinedTextField(
                                value = berat[d.id] ?: "",
                                onValueChange = { v ->
                                    if (d.id != null) berat[d.id] = v.filter { c -> c.isDigit() || c == '.' }
                                },
                                placeholder = { Text("kg", fontSize = 12.sp) },
                                modifier = Modifier.width(90.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GreenPrimary,
                                    unfocusedBorderColor = BorderLight,
                                ),
                            )
                        }
                    }
                }
                OutlinedButton(
                    onClick = { photoPicker.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary),
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(fotoName ?: "Tambah Foto Bukti (opsional)", fontSize = 12.sp)
                }
                if (message != null) Text(message!!, fontSize = 12.sp, color = TextSecondary)
            }
        },
        containerColor = BgWhite,
    )
}

/**
 * Baca foto dari galeri, perkecil ke sisi terpanjang 1024px, kompres
 * JPEG kualitas 80, kembalikan Base64. Mengembalikan null bila gagal.
 * Downscale menjaga ukuran request tetap wajar untuk PUT foto-bukti.
 */
private fun encodePhotoBase64(context: android.content.Context, uri: Uri): String? {
    return try {
        val input = context.contentResolver.openInputStream(uri) ?: return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(input, null, bounds)
        input.close()
        val maxSide = maxOf(bounds.outWidth, bounds.outHeight)
        var sample = 1
        while (maxSide / sample > 1024) sample *= 2
        val input2 = context.contentResolver.openInputStream(uri) ?: return null
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = BitmapFactory.decodeStream(input2, null, opts)
        input2.close()
        if (bitmap == null) return null
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    } catch (e: Exception) {
        null
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
