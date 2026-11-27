package com.resik.client.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.resik.client.api.ApiClient
import com.resik.client.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

/** Satu baris sampah pada struk. */
data class StrukBaris(
    val nama: String,
    val beratKg: Double?,
    val hargaPerKg: Long?,
    val subtotal: Long?,
)

data class PenjemputanDetailData(
    val id: Long?,
    val namaNasabah: String,
    val tanggal: String,
    val waktu: String,
    val alamat: String,
    val petugasNama: String?,
    val petugasId: Long?,
    val status: String,
    val baris: List<StrukBaris>,
    val totalBayar: Long?,
    val fotoBuktiUrl: String?,
)

/** Ubah fotoBuktiUrl relatif ("/api/penjemputan/{id}/foto") menjadi URL absolut. */
fun resolveFotoUrl(fotoBuktiUrl: String?): String? {
    if (fotoBuktiUrl.isNullOrBlank()) return null
    if (fotoBuktiUrl.startsWith("http://") || fotoBuktiUrl.startsWith("https://")) return fotoBuktiUrl
    val base = ApiClient.getBaseUrl().removeSuffix("/")
    // base ".../api" -> origin "..." + "/api/penjemputan/{id}/foto"
    val origin = base.removeSuffix("/api")
    return origin + fotoBuktiUrl
}

@Suppress("UNCHECKED_CAST")
private fun asMap(value: Any?): Map<String, Any>? = value as? Map<String, Any>

private fun num(value: Any?): Double? = when (value) {
    is Number -> value.toDouble()
    is String -> value.toDoubleOrNull()
    else -> null
}

private fun longNum(value: Any?): Long? = num(value)?.toLong()

private fun str(value: Any?): String? = value?.toString()?.takeIf { it.isNotBlank() }

/**
 * Parse GET /api/penjemputan/{id} secara defensif: backend bisa mengirim
 * user/petugas sebagai objek nested ({id, nama}) atau field datar
 * (userNama/petugasNama); detailList berisi sampah nested atau nama datar.
 * Field yang tidak ada dibiarkan kosong ("-") — struk tidak boleh crash
 * karena satu field hilang.
 */
@Suppress("UNCHECKED_CAST")
fun parsePenjemputanDetail(map: Map<String, Any>): PenjemputanDetailData {
    val userMap = asMap(map["user"])
    val petugasMap = asMap(map["petugas"])
    val transaksiMap = asMap(map["transaksi"])
    val details = (map["detailList"] as? List<*>)?.mapNotNull { it as? Map<String, Any> } ?: emptyList()
    val baris = details.map { d ->
        val sampahMap = asMap(d["sampah"])
        val nama = str(sampahMap?.get("nama")) ?: str(d["nama"]) ?: str(d["namaSampah"]) ?: "-"
        val harga = longNum(sampahMap?.get("hargaPerKg")) ?: longNum(d["hargaPerKg"])
        val berat = num(d["beratAktual"]) ?: num(d["estimasiBerat"])
        StrukBaris(nama, berat, harga, longNum(d["subtotal"]))
    }
    val total = longNum(transaksiMap?.get("totalHarga"))
        ?: baris.mapNotNull { it.subtotal }.takeIf { it.isNotEmpty() }?.sum()
    return PenjemputanDetailData(
        id = longNum(map["id"]),
        namaNasabah = str(userMap?.get("nama")) ?: str(map["userNama"]) ?: str(map["nama"]) ?: "-",
        tanggal = str(map["tanggalJemput"]) ?: "-",
        waktu = str(map["waktuJemput"]) ?: "-",
        alamat = str(map["alamat"]) ?: "-",
        petugasNama = str(petugasMap?.get("nama")) ?: str(map["petugasNama"]),
        petugasId = longNum(petugasMap?.get("id")) ?: longNum(map["petugasId"]),
        status = str(map["status"]) ?: "-",
        baris = baris,
        totalBayar = total,
        fotoBuktiUrl = str(map["fotoBuktiUrl"]),
    )
}

fun formatRupiah(nilai: Long?): String {
    if (nilai == null) return "-"
    return "Rp " + NumberFormat.getInstance(Locale("id", "ID")).format(nilai)
}

private fun formatKg(berat: Double?): String =
    if (berat == null) "-" else "${"%.2f".format(berat).trimEnd('0').trimEnd('.')} kg"

/**
 * Teks struk polos yang rapi — dipakai untuk tampilan dan untuk dibagikan
 * via WhatsApp (Intent ACTION_SEND). Tidak ada poin bonus atau field
 * yang dikarang: semua angka berasal dari detail penjemputan server.
 */
fun buildReceiptText(d: PenjemputanDetailData): String {
    val sb = StringBuilder()
    sb.appendLine("====== STRUK RESIK ======")
    sb.appendLine("Bank Sampah Digital")
    sb.appendLine("------------------------------")
    sb.appendLine("Nasabah  : ${d.namaNasabah}")
    sb.appendLine("Tanggal  : ${d.tanggal} ${d.waktu}")
    sb.appendLine("Petugas  : ${d.petugasNama ?: "-"}")
    sb.appendLine("Alamat   : ${d.alamat}")
    sb.appendLine("Status   : ${d.status}")
    sb.appendLine("------------------------------")
    if (d.baris.isEmpty()) {
        sb.appendLine("(belum ada rincian sampah)")
    } else {
        d.baris.forEach { b ->
            sb.appendLine(b.nama)
            val harga = if (b.hargaPerKg != null) " @ ${formatRupiah(b.hargaPerKg)}/kg" else ""
            sb.appendLine("  ${formatKg(b.beratKg)}$harga = ${formatRupiah(b.subtotal)}")
        }
    }
    sb.appendLine("------------------------------")
    sb.appendLine("TOTAL BAYAR: ${formatRupiah(d.totalBayar)}")
    sb.appendLine("====== Terima kasih ======")
    sb.append("Sampah terpilah, bumi terjaga.")
    return sb.toString()
}

/** Bagikan teks struk via WhatsApp; bila WhatsApp tidak terpasang, buka chooser. */
fun shareReceiptViaWhatsApp(context: Context, receiptText: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, receiptText)
        setPackage("com.whatsapp")
    }
    try {
        context.startActivity(send)
    } catch (e: ActivityNotFoundException) {
        val chooser = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, receiptText)
        }
        context.startActivity(Intent.createChooser(chooser, "Bagikan struk"))
    }
}

/**
 * Dialog struk digital: rincian sampah (nama, kg, harga/kg, total),
 * total bayar, status, foto bukti timbangan (bila ada, dimuat dari
 * GET penjemputan/{id}/foto via fotoBuktiUrl dengan Coil), rating
 * petugas, dan tombol "Bagikan via WhatsApp".
 */
@Composable
fun StrukDialog(detail: PenjemputanDetailData, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row {
                TextButton(onClick = { shareReceiptViaWhatsApp(context, buildReceiptText(detail)) }) {
                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Bagikan via WhatsApp", color = GreenPrimary)
                }
                TextButton(onClick = onDismiss) { Text("Tutup", color = GreenPrimary) }
            }
        },
        title = { Text("Struk Digital", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StrukDetailRow("Nasabah", detail.namaNasabah)
                StrukDetailRow("Tanggal", "${detail.tanggal} ${detail.waktu}")
                StrukDetailRow("Petugas", detail.petugasNama ?: "-")
                StrukDetailRow("Alamat", detail.alamat)
                StrukDetailRow("Status", detail.status)
                Divider()
                if (detail.baris.isEmpty()) {
                    Text("Belum ada rincian sampah.", fontSize = 13.sp, color = TextHint)
                } else {
                    detail.baris.forEach { b ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(b.nama, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                Text(
                                    "${formatKg(b.beratKg)}" +
                                        (if (b.hargaPerKg != null) " @ ${formatRupiah(b.hargaPerKg)}/kg" else ""),
                                    fontSize = 12.sp, color = TextHint,
                                )
                            }
                            Text(formatRupiah(b.subtotal), fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Divider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Bayar", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(formatRupiah(detail.totalBayar), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                }
                // Foto bukti timbangan (butir 6): hanya bila server
                // mengirim fotoBuktiUrl; dimuat dengan Coil (sudah ada di
                // build.gradle sebagai coil-compose).
                val fotoUrl = resolveFotoUrl(detail.fotoBuktiUrl)
                if (fotoUrl != null) {
                    Text("Foto Bukti Timbangan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    AsyncImage(
                        model = fotoUrl,
                        contentDescription = "Foto bukti timbangan",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)),
                    )
                }
                // Rating petugas (butir 9): hanya untuk penjemputan SELESAI.
                if (detail.id != null) {
                    RatingStatusSection(
                        penjemputanId = detail.id,
                        status = detail.status,
                        petugasId = detail.petugasId,
                        petugasNama = detail.petugasNama,
                    )
                }
            }
        },
        containerColor = BgWhite,
    )
}

/** Baris label–nilai pada struk. (DetailRow di OtherScreens.kt bersifat private file, jadi struk punya salinannya sendiri.) */
@Composable
private fun StrukDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextHint)
        Text(value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}
