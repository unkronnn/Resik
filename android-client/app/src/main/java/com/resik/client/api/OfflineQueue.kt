package com.resik.client.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

/**
 * Antrean offline petugas Bank Sampah.
 *
 * Saat petugas mengirim timbang dan panggilan jaringan gagal, operasi
 * disimpan di SharedPreferences (JSON via Gson — tidak ada dependency
 * baru) dan dikirim ulang berurutan saat koneksi kembali: saat aplikasi
 * dibuka dan saat kanal /ws/events tersambung kembali (lihat
 * [RealtimeEventHub.addConnectListener]).
 *
 * BATASAN — baca sebelum mengubah perilaku ini:
 * - Tanpa resolusi konflik (last-write-wins): bila penjemputan yang sama
 *   diubah juga dari desktop/admin sementara operasi masih mengantre,
 *   kiriman ulang akan menimpa perubahan itu tanpa peringatan.
 * - Antrean TIDAK bertahan bila data aplikasi dihapus (SharedPreferences
 *   ikut terhapus) atau bila aplikasi di-uninstall.
 * - Satu operasi timbang tidak boleh dikirim dua kali: operasi yang
 *   sedang dikirim ditandai [TimbangOp.inFlight] dan dilewati oleh
 *   pemicu sync lain sampai hasilnya kembali; operasi hanya dihapus
 *   dari antrean bila server menjawab 2xx. Kegagalan validasi server
 *   (4xx, mis. penjemputan sudah SELESAI) TIDAK diantrekan ulang —
 *   operasi dibuang dan dilaporkan sebagai gagal agar tidak mengulang
 *   selamanya.
 */
object OfflineQueue {

    private const val PREFS = "resik_offline"
    private const val KEY_TIMBANG = "timbang_queue"

    private val gson = Gson()

    /** Satu operasi timbang yang tertunda. `details` = [{detailId, beratAktual}]. */
    data class TimbangOp(
        val opId: String,
        val penjemputanId: Long,
        val details: List<TimbangDetailOp>,
        val createdAt: Long,
        val inFlight: Boolean = false,
    )

    data class TimbangDetailOp(val detailId: Long, val beratAktual: Double)

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun load(context: Context): MutableList<TimbangOp> {
        val raw = prefs(context).getString(KEY_TIMBANG, null) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<TimbangOp>>() {}.type
            gson.fromJson<MutableList<TimbangOp>>(raw, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    private fun save(context: Context, ops: List<TimbangOp>) {
        prefs(context).edit().putString(KEY_TIMBANG, gson.toJson(ops)).apply()
    }

    /** Simpan satu operasi timbang ke antrean. Mengembalikan id operasi. */
    fun enqueueTimbang(
        context: Context,
        penjemputanId: Long,
        details: List<TimbangDetailOp>,
    ): String {
        val ops = load(context)
        val op = TimbangOp(
            opId = UUID.randomUUID().toString(),
            penjemputanId = penjemputanId,
            details = details,
            createdAt = System.currentTimeMillis(),
        )
        ops.add(op)
        save(context, ops)
        return op.opId
    }

    fun pendingCount(context: Context): Int = load(context).count { !it.inFlight }

    /** Termasuk yang sedang dikirim; dipakai untuk menampilkan status antrean. */
    fun pendingOps(context: Context): List<TimbangOp> = load(context)

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Kirim ulang antrean berurutan (FIFO). Callback dipanggil sekali di
     * akhir dengan (jumlah berhasil, jumlah gagal permanen). Operasi yang
     * gagal karena jaringan tetap mengantre untuk percobaan berikutnya;
     * operasi yang ditolak server (4xx) dibuang dan dihitung gagal.
     * Aman dipanggil berkali-kali / bersamaan: operasi inFlight dilewati.
     */
    fun syncAll(context: Context, onDone: (synced: Int, failed: Int) -> Unit = { _, _ -> }) {
        if (!isOnline(context)) {
            onDone(0, 0)
            return
        }
        val ops = load(context).filter { !it.inFlight }
        if (ops.isEmpty()) {
            onDone(0, 0)
            return
        }
        syncNext(context, ops, 0, 0, 0, onDone)
    }

    private fun syncNext(
        context: Context,
        ops: List<TimbangOp>,
        index: Int,
        synced: Int,
        failed: Int,
        onDone: (Int, Int) -> Unit,
    ) {
        if (index >= ops.size) {
            onDone(synced, failed)
            return
        }
        val op = ops[index]
        // Tandai inFlight agar pemicu sync lain tidak mengirim op yang sama.
        val marked = load(context).map { if (it.opId == op.opId) it.copy(inFlight = true) else it }
        save(context, marked)

        val body = mapOf<String, Any>(
            "penjemputanId" to op.penjemputanId,
            "details" to op.details.map { d ->
                mapOf<String, Any>("detailId" to d.detailId, "beratAktual" to d.beratAktual)
            },
        )
        ApiClient.getApi().timbangSampah(ApiClient.getAuthHeader(), body)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    c: Call<Map<String, Any>>,
                    r: Response<Map<String, Any>>,
                ) {
                    val remaining = load(context).filter { it.opId != op.opId }
                    if (r.isSuccessful) {
                        save(context, remaining)
                        syncNext(context, ops, index + 1, synced + 1, failed, onDone)
                    } else if (r.code() in 400..499) {
                        // Ditolak server (mis. sudah SELESAI): buang, jangan
                        // antre ulang — mengulang tidak akan mengubah hasil.
                        save(context, remaining)
                        syncNext(context, ops, index + 1, synced, failed + 1, onDone)
                    } else {
                        // 5xx: biarkan mengantre (hapus tanda inFlight saja).
                        save(context, load(context).map {
                            if (it.opId == op.opId) it.copy(inFlight = false) else it
                        })
                        syncNext(context, ops, index + 1, synced, failed, onDone)
                    }
                }

                override fun onFailure(c: Call<Map<String, Any>>, t: Throwable) {
                    // Jaringan gagal lagi: biarkan mengantre (hapus inFlight).
                    save(context, load(context).map {
                        if (it.opId == op.opId) it.copy(inFlight = false) else it
                    })
                    syncNext(context, ops, index + 1, synced, failed, onDone)
                }
            })
    }
}
