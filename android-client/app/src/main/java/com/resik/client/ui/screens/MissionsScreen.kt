package com.resik.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resik.client.api.ApiClient
import com.resik.client.model.Penjemputan
import com.resik.client.ui.theme.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Misi & Badge (butir 10).
 *
 * Misi dihitung SEPENUHNYA dari data yang sudah ada — tidak ada endpoint
 * baru dan tidak ada poin bonus baru dari server:
 * - GET /api/user/me -> poin (poinReward)
 * - GET /api/penjemputan/my -> jumlah setoran SELESAI dan total kg
 *   (jumlah beratAktual tiap detail; bila belum ditimbang, pakai
 *   estimasiBerat sebagai fallback tampilan progres).
 *
 * Definisi misi tetap (konstanta di bawah). Selesainya misi hanya
 * pemberitahuan di UI dan nyambung ke poin/leaderboard yang sudah ada;
 * tidak ada hadiah poin tambahan yang diberikan oleh misi ini.
 */
data class Misi(
    val id: String,
    val judul: String,
    val deskripsi: String,
    val target: Double,
    val satuan: String,
    val icon: ImageVector,
)

val MISI_DEFINITIONS: List<Misi> = listOf(
    Misi("setor-pertama", "Setor Pertama", "Selesaikan 1 penjemputan sampah.", 1.0, "setoran", Icons.Filled.LocalShipping),
    Misi("pejuang-10kg", "Pejuang 10 Kg", "Setorkan total 10 kg sampah.", 10.0, "kg", Icons.Filled.FitnessCenter),
    Misi("langganan-hijau", "Langganan Hijau", "Selesaikan 5 penjemputan sampah.", 5.0, "setoran", Icons.Filled.Eco),
    Misi("kolektor-100-poin", "Kolektor 100 Poin", "Kumpulkan 100 poin reward.", 100.0, "poin", Icons.Filled.EmojiEvents),
)

data class MisiProgress(val misi: Misi, val progres: Double) {
    val selesai: Boolean get() = progres >= misi.target
    val labelProgres: String get() {
        val p = if (misi.satuan == "kg") {
            "${"%.1f".format(progres.coerceAtMost(misi.target)).trimEnd('0').trimEnd('.')}/${misi.target.toInt()} kg"
        } else {
            "${progres.toInt()}/${misi.target.toInt()} ${misi.satuan}"
        }
        return p
    }
}

/** Hitung progres tiap misi dari data yang sudah ada (murni, mudah dites). */
fun hitungMisi(poin: Int, setoranSelesai: Int, totalKg: Double): List<MisiProgress> =
    MISI_DEFINITIONS.map { misi ->
        val progres = when (misi.id) {
            "setor-pertama" -> setoranSelesai.toDouble()
            "pejuang-10kg" -> totalKg
            "langganan-hijau" -> setoranSelesai.toDouble()
            "kolektor-100-poin" -> poin.toDouble()
            else -> 0.0
        }
        MisiProgress(misi, progres)
    }

/** Total kg dari penjemputan SELESAI: beratAktual, fallback estimasiBerat. */
fun totalKgSetoranSelesai(penjemputan: List<Penjemputan>): Double =
    penjemputan.filter { it.status == "SELESAI" }
        .flatMap { it.detailList ?: emptyList() }
        .sumOf { (it.beratAktual ?: it.estimasiBerat)?.toDouble() ?: 0.0 }

@Composable
fun MisiScreen(onBack: () -> Unit) {
    var poin by remember { mutableStateOf<Int?>(null) }
    var setoranSelesai by remember { mutableStateOf<Int?>(null) }
    var totalKg by remember { mutableStateOf<Double?>(null) }
    var loading by remember { mutableStateOf(true) }
    var offline by remember { mutableStateOf(false) }

    fun load() {
        loading = true
        offline = false
        ApiClient.getApi().getMe(ApiClient.getAuthHeader())
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(c: Call<Map<String, Any>>, r: Response<Map<String, Any>>) {
                    poin = if (r.isSuccessful && r.body() != null) {
                        (r.body()!!["poinReward"] as? Number)?.toInt() ?: 0
                    } else 0
                    ApiClient.getApi().getMyPenjemputan(ApiClient.getAuthHeader())
                        .enqueue(object : Callback<List<Penjemputan>> {
                            override fun onResponse(
                                c2: Call<List<Penjemputan>>,
                                r2: Response<List<Penjemputan>>,
                            ) {
                                val list = if (r2.isSuccessful) r2.body() ?: emptyList() else emptyList()
                                setoranSelesai = list.count { it.status == "SELESAI" }
                                totalKg = totalKgSetoranSelesai(list)
                                loading = false
                            }

                            override fun onFailure(c2: Call<List<Penjemputan>>, t: Throwable) {
                                offline = true
                                loading = false
                            }
                        })
                }

                override fun onFailure(c: Call<Map<String, Any>>, t: Throwable) {
                    offline = true
                    loading = false
                }
            })
    }

    LaunchedEffect(Unit) { load() }

    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {
        GradientHeader("Misi & Badge", onBack)
        // `return` tidak boleh dipakai di dalam lambda Column (bukan inline),
        // jadi cabang loading / konten ditulis sebagai if-else.
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else {
            if (offline) {
                Text(
                    "Tidak terhubung ke server — progres misi memakai data terakhir yang berhasil dimuat.",
                    fontSize = 12.sp, color = TextSecondary,
                    modifier = Modifier.padding(16.dp),
                )
            }
            val progress = hitungMisi(poin ?: 0, setoranSelesai ?: 0, totalKg ?: 0.0)
        val selesaiCount = progress.count { it.selesai }
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BgWhite)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(GreenMint),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(26.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("$selesaiCount dari ${progress.size} misi selesai", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Poin kamu: ${poin ?: 0} • Setoran selesai: ${setoranSelesai ?: 0} • Total: ${"%.1f".format(totalKg ?: 0.0)} kg", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
            Text(
                "Misi dihitung dari riwayat setoran dan poinmu. Tidak ada poin bonus baru dari misi — poin tetap berasal dari timbang sampah dan tampil di Leaderboard.",
                fontSize = 12.sp, color = TextHint,
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(progress) { p -> MisiCard(p) }
            }
        }
        }
    }
}

@Composable
private fun MisiCard(p: MisiProgress) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (p.selesai) GreenMint else BgWhite),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                    .background(if (p.selesai) GreenPrimary else BgLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (p.selesai) Icons.Filled.CheckCircle else p.misi.icon,
                    contentDescription = null,
                    tint = if (p.selesai) BgWhite else GreenPrimary,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(p.misi.judul, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    if (p.selesai) {
                        Spacer(Modifier.width(6.dp))
                        Text("SELESAI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                    }
                }
                Text(p.misi.deskripsi, fontSize = 12.sp, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = (p.progres / p.misi.target).toFloat().coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = GreenPrimary,
                    trackColor = BorderLight,
                )
                Spacer(Modifier.height(4.dp))
                Text(p.labelProgres, fontSize = 11.sp, color = TextHint)
            }
        }
    }
}
