# FCM Setup — aktivasi push notification (belum aktif)

**Status: FCM BELUM aktif.** Backend sudah berisi semua kode server-side
(dependency `firebase-admin`, `FcmNotificationService`, entitas
`DeviceToken`, endpoint registrasi token, dan pemanggilan kirim di
`PenjemputanService`), tetapi **tidak ada satu pun push yang akan terkirim**
sampai langkah-langkah di bawah ini dilakukan oleh Syukron. Tanpa
service-account, service berjalan sebagai no-op: setiap percobaan kirim
hanya menulis log peringatan dan mengembalikan hasil "not configured",
tanpa exception — alur penjemputan dan timbangan tidak pernah gagal
karena FCM belum disiapkan.

## Yang harus disediakan Syukron

### 1. Proyek Firebase

1. Buka [Firebase Console](https://console.firebase.google.com/) dan buat
   proyek baru (atau pakai proyek yang sudah ada), mis. `resik-app`.
2. Di proyek itu, buka **Project Settings → Cloud Messaging** dan pastikan
   **Firebase Cloud Messaging API (V1)** dalam keadaan Enabled. (FCM
   legacy API tidak dipakai; Admin SDK memakai HTTP v1.)

### 2. Aplikasi Android di proyek Firebase

1. Di **Project Settings → Your apps**, tambah aplikasi Android.
2. Package name harus sama persis dengan `applicationId` di
   `android-client` (cek `android-client/app/build.gradle`).
3. Unduh file **`google-services.json`** yang dihasilkan.
4. Letakkan file itu di **`android-client/app/google-services.json`**
   (sejajar `build.gradle` modul app). **Jangan** meletakkannya di repo
   backend dan jangan membuat file palsu — file ini berisi identitas
   proyek Firebase yang asli.
5. Pastikan plugin `com.google.gms.google-services` sudah diterapkan di
   Gradle Android (bagian client, bukan backend). Tanpa plugin ini,
   `google-services.json` tidak dibaca dan `FirebaseMessaging` tidak
   mendapat token.

### 3. Service-account JSON (kunci server)

1. Di Firebase Console, buka **Project Settings → Service accounts**.
2. Klik **Generate new private key** → unduh file JSON-nya. File ini
   adalah **kredensial server yang sangat sensitif**: siapa pun yang
   memegangnya dapat mengirim push atas nama proyek.
3. Simpan file itu **di luar repo**, misalnya di VPS pada
   `/etc/resik/firebase-service-account.json`. **Jangan commit file ini
   ke Git, jangan simpan di `backend-api/src/main/resources/`, dan jangan
   membagikannya lewat chat.**
4. Beri izin baca hanya untuk user yang menjalankan backend
   (`chmod 600`).

### 4. Properti yang disetel di backend

Setel salah satu dari dua cara ini (cukup satu; properti Spring
didahulukan atas env bila keduanya diisi):

```yaml
# backend-api/src/main/resources/application.yml (atau application-prod.yml)
fcm:
  service-account-path: /etc/resik/firebase-service-account.json
```

atau sebagai environment variable saat menjalankan backend:

```bash
export FCM_SERVICE_ACCOUNT_PATH=/etc/resik/firebase-service-account.json
# atau: export RESIK_UPLOADS_DIR=/var/resik/uploads
java -jar resik-api.jar
```

Bila path kosong atau file tidak ditemukan, backend tetap berjalan
normal dan FCM tetap no-op (cek log startup: `FCM not configured ...`).

### 5. Langkah aktivasi

1. Deploy backend dengan service-account yang sudah ditempatkan dan
   properti/env di atas terisi.
2. Jalankan aplikasi Android, login, dan pastikan aplikasi memanggil
   `POST /api/notifikasi/token` dengan token FCM perangkat
   (endpoint sudah tersedia; bagian client yang memanggilnya belum
   dibuat — lihat "Yang masih kurang" di bawah).
3. Cek log backend: baris `FCM configured from service-account file at ...`
   menandakan inisialisasi berhasil.
4. Uji kirim: buat penjemputan lalu tugaskan petugas / selesaikan
   timbangan, dan pastikan notifikasi push tiba di perangkat pemilik
   penjemputan. Bila tidak tiba, cek: token terdaftar
   (`GET /api/notifikasi/token?userId=<id>` sebagai admin), file
   service-account benar, dan API FCM v1 enabled.

## Cara kerja kode (yang sudah jadi)

| Komponen | Lokasi | Fungsi |
|---|---|---|
| Dependency | `backend-api/pom.xml` | `com.google.firebase:firebase-admin:9.4.0` |
| Service | `service/FcmNotificationService.java` | Inisialisasi malas `FirebaseApp` hanya bila file service-account ada; `sendToToken(...)` dan `sendToUser(...)` mengembalikan result, tidak pernah throw |
| Entitas + repo | `entity/DeviceToken.java`, `repository/DeviceTokenRepository.java` | Satu baris per token perangkat (unik), `findByUserId` untuk pengiriman |
| Endpoint | `controller/DeviceTokenController.java` | `POST /api/notifikasi/token` (upsert), `GET /api/notifikasi/token?userId=` (pemilik token atau admin) |
| Pemakaian | `service/PenjemputanService.java` | Push ke pemilik saat penjemputan dijadwalkan (`assignPetugas`), dibatalkan, dijadwalkan ulang, status berubah, dan saat timbangan selesai |

Payload push memakai `data` berisi `type= PENJEMPUTAN_STATUS` dan
`penjemputanId`, supaya client bisa membuka layar yang tepat saat
notifikasi diketuk (bukan hanya menampilkan teks).

## Yang masih kurang (di luar backend ini)

- **Client Android**: mengambil token via `FirebaseMessaging.getInstance().token`
  dan memanggil `POST /api/notifikasi/token` setelah login (dan saat token
  di-refresh). Tanpa ini, server tidak punya token untuk dikirimi.
- **Token kedaluwarsa**: bila FCM menolak token (`UNREGISTERED`), token
  tersebut sebaiknya dihapus dari `device_token`. Saat ini service hanya
  menghitung kegagalan; penghapusan otomatis belum diimplementasikan.
- **Rotasi kunci**: bila service-account bocor, revoke key-nya di Google
  Cloud Console dan generate key baru, lalu ganti file di server.
