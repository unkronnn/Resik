package com.resik.client.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resik.client.api.ApiClient
import com.resik.client.ui.theme.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/** Baris bintang 1-5 yang bisa diketuk. */
@Composable
fun RatingStarsInput(nilai: Int, onNilai: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (1..5).forEach { star ->
            Icon(
                imageVector = if (star <= nilai) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = "$star bintang",
                tint = if (star <= nilai) Color(0xFFF59E0B) else TextHint,
                modifier = Modifier.size(36.dp).clickable { onNilai(star) },
            )
        }
    }
}

/** Dialog beri rating: bintang 1-5 + komentar opsional -> POST /api/rating. */
@Composable
fun RatingDialog(
    penjemputanId: Long,
    petugasNama: String?,
    onDismiss: () -> Unit,
    onSuccess: (nilai: Int) -> Unit,
) {
    var nilai by remember { mutableStateOf(5) }
    var komentar by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!sending) onDismiss() },
        confirmButton = {
            Button(
                onClick = {
                    sending = true
                    error = null
                    val body = mapOf<String, Any>(
                        "penjemputanId" to penjemputanId,
                        "nilai" to nilai,
                        "komentar" to komentar,
                    )
                    ApiClient.getApi().kirimRating(ApiClient.getAuthHeader(), body)
                        .enqueue(object : Callback<Map<String, Any>> {
                            override fun onResponse(
                                c: Call<Map<String, Any>>,
                                r: Response<Map<String, Any>>,
                            ) {
                                sending = false
                                if (r.isSuccessful) onSuccess(nilai)
                                else error = "Gagal mengirim rating (${r.code()}). Coba lagi."
                            }

                            override fun onFailure(c: Call<Map<String, Any>>, t: Throwable) {
                                sending = false
                                error = "Tidak terhubung ke server. Coba lagi."
                            }
                        })
                },
                enabled = !sending,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(12.dp),
            ) { Text(if (sending) "Mengirim..." else "Kirim Rating", color = BgWhite) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !sending) { Text("Batal", color = GreenPrimary) }
        },
        title = { Text("Beri Rating", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (petugasNama != null) {
                    Text("Petugas: $petugasNama", fontSize = 13.sp, color = TextSecondary)
                }
                RatingStarsInput(nilai = nilai, onNilai = { nilai = it })
                OutlinedTextField(
                    value = komentar,
                    onValueChange = { komentar = it },
                    placeholder = { Text("Komentar (opsional)", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = BorderLight,
                    ),
                )
                if (error != null) Text(error!!, fontSize = 12.sp, color = RedLogout)
            }
        },
        containerColor = BgWhite,
    )
}

/**
 * Rata-rata rating petugas (GET /api/rating/petugas/{id}).
 * Menampilkan "★ 4.5 (12 ulasan)" atau "Belum ada rating" bila server
 * menjawab jumlah 0 / 404. Bila petugasId tidak diketahui, tidak tampil.
 */
@Composable
fun PetugasRatingRow(petugasId: Long?, petugasNama: String?) {
    if (petugasId == null) return
    var text by remember(petugasId) { mutableStateOf<String?>(null) }
    LaunchedEffect(petugasId) {
        ApiClient.getApi().getRatingPetugas(ApiClient.getAuthHeader(), petugasId)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(c: Call<Map<String, Any>>, r: Response<Map<String, Any>>) {
                    if (r.isSuccessful && r.body() != null) {
                        val body = r.body()!!
                        val rata = (body["rataRata"] as? Number)?.toDouble()
                        val jumlah = (body["jumlah"] as? Number)?.toInt() ?: 0
                        text = if (jumlah > 0 && rata != null) {
                            "★ ${"%.1f".format(rata)} ($jumlah ulasan)"
                        } else {
                            "Belum ada rating"
                        }
                    } else {
                        text = "Belum ada rating"
                    }
                }

                override fun onFailure(c: Call<Map<String, Any>>, t: Throwable) {
                    text = null // offline: sembunyikan, jangan tampilkan angka karangan
                }
            })
    }
    if (text != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                "Rating ${petugasNama ?: "petugas"}: $text",
                fontSize = 12.sp, color = TextSecondary,
            )
        }
    }
}

private enum class RatingCheckState { LOADING, BELUM_DINILAI, SUDAH_DINILAI, TIDAK_TERSEDIA }

/**
 * Bagian rating di detail/struk penjemputan (butir 9):
 * - Cek dulu GET /api/rating/penjemputan/{id}: bila sudah dinilai,
 *   tampilkan nilai yang diberikan dan JANGAN minta dua kali.
 * - Bila belum dinilai (404) dan status SELESAI, tampilkan tombol
 *   "Beri Rating" yang membuka [RatingDialog].
 * - Bila endpoint belum tersedia di server, bagian ini diam-diam
 *   disembunyikan (bukan error yang menghalangi struk).
 */
@Composable
fun RatingStatusSection(
    penjemputanId: Long,
    status: String,
    petugasId: Long?,
    petugasNama: String?,
) {
    var state by remember(penjemputanId) { mutableStateOf(RatingCheckState.LOADING) }
    var nilaiDiberikan by remember(penjemputanId) { mutableStateOf<Int?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    fun checkRating() {
        state = RatingCheckState.LOADING
        ApiClient.getApi().getRatingPenjemputan(ApiClient.getAuthHeader(), penjemputanId)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(c: Call<Map<String, Any>>, r: Response<Map<String, Any>>) {
                    if (r.isSuccessful && r.body() != null) {
                        nilaiDiberikan = (r.body()!!["nilai"] as? Number)?.toInt()
                        state = RatingCheckState.SUDAH_DINILAI
                    } else if (r.code() == 404) {
                        state = RatingCheckState.BELUM_DINILAI
                    } else {
                        state = RatingCheckState.TIDAK_TERSEDIA
                    }
                }

                override fun onFailure(c: Call<Map<String, Any>>, t: Throwable) {
                    state = RatingCheckState.TIDAK_TERSEDIA
                }
            })
    }

    LaunchedEffect(penjemputanId) { checkRating() }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Divider()
        PetugasRatingRow(petugasId = petugasId, petugasNama = petugasNama)
        when (state) {
            RatingCheckState.LOADING -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = GreenPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Memeriksa rating...", fontSize = 12.sp, color = TextHint)
                }
            }
            RatingCheckState.SUDAH_DINILAI -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Rating Anda: ", fontSize = 13.sp, color = TextSecondary)
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= (nilaiDiberikan ?: 0)) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
            RatingCheckState.BELUM_DINILAI -> {
                if (status == "SELESAI") {
                    OutlinedButton(
                        onClick = { showDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary),
                    ) {
                        Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Beri Rating", fontWeight = FontWeight.Bold)
                    }
                }
            }
            RatingCheckState.TIDAK_TERSEDIA -> {
                // Endpoint belum tersedia / offline: jangan halangi struk.
            }
        }
    }

    if (showDialog) {
        RatingDialog(
            penjemputanId = penjemputanId,
            petugasNama = petugasNama,
            onDismiss = { showDialog = false },
            onSuccess = { nilai ->
                nilaiDiberikan = nilai
                state = RatingCheckState.SUDAH_DINILAI
                showDialog = false
            },
        )
    }
}
