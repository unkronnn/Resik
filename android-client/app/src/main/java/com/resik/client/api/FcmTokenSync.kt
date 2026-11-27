package com.resik.client.api

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Persiapan FCM (Firebase Cloud Messaging) — token saja, tanpa push aktif.
 *
 * Alur: ambil token perangkat via FirebaseMessaging, lalu kirim ke
 * POST /api/notifikasi/token {"token","platform":"android"} (upsert).
 *
 * PENTING — FCM BELUM AKTIF sampai user menyediakan google-services.json:
 * tanpa file itu Firebase tidak terinisialisasi dan
 * FirebaseMessaging.getInstance() melempar IllegalStateException.
 * Semua kegagalan di sini ditangkap diam-diam (try/catch Throwable) dan
 * aplikasi lanjut berjalan normal tanpa push. JANGAN menambahkan plugin
 * google-services ke build.gradle sebelum file itu ada — plugin tersebut
 * gagal build bila google-services.json tidak ditemukan.
 * Langkah aktivasi lengkap ada di docs/FCM_SETUP.md (dibuat agen backend).
 */
object FcmTokenSync {

    /**
     * Coba ambil token FCM dan kirim ke server. Aman dipanggil kapan saja
     * (mis. saat MainActivity dibuka): tidak pernah melempar, tidak pernah
     * memblokir UI, dan tidak melakukan apa pun bila pengguna belum login
     * (endpoint token butuh auth) atau bila Firebase belum terinisialisasi.
     */
    fun sync(context: Context) {
        if (!ApiClient.isLoggedIn()) return
        try {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    if (!token.isNullOrBlank()) sendToken(token)
                }
                .addOnFailureListener {
                    // Firebase belum terinisialisasi / belum ada
                    // google-services.json — FCM belum aktif, abaikan.
                }
        } catch (t: Throwable) {
            // Firebase belum terinisialisasi (belum ada google-services.json)
            // atau Play Services tidak tersedia: FCM belum aktif, lanjut
            // aplikasi normal tanpa push.
        }
    }

    private fun sendToken(token: String) {
        val body = mapOf<String, Any>("token" to token, "platform" to "android")
        try {
            ApiClient.getApi().kirimFcmToken(ApiClient.getAuthHeader(), body)
                .enqueue(object : Callback<Map<String, Any>> {
                    override fun onResponse(
                        c: Call<Map<String, Any>>,
                        r: Response<Map<String, Any>>,
                    ) {
                        // Token tersimpan (upsert). Tidak ada aksi lanjutan.
                    }

                    override fun onFailure(c: Call<Map<String, Any>>, t: Throwable) {
                        // Server belum menyediakan endpoint / offline:
                        // token akan dikirim ulang saat aplikasi dibuka lagi.
                    }
                })
        } catch (t: Throwable) {
            // Jangan pernah mengganggu jalannya aplikasi karena FCM.
        }
    }
}
