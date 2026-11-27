# Fitur Baru Android — feat/all-features (Nov 2026)

Dokumen ini merangkum pekerjaan agen Android untuk paket fitur
"bangun SEMUA yang disarankan": batal/jadwal ulang, FCM, struk digital,
foto bukti, mode offline petugas, rute optimal, rating, dan misi/badge.

> **Status kontrak (diperbarui saat penulisan selesai, 2026-11-26):**
> agen backend SUDAH membuat endpoint-endpoint ini selama pekerjaan
> Android berjalan (dicek langsung di `backend-api/`): `PUT batal`,
> `PUT jadwal`, `GET penjemputan/{id}`, `PUT foto-bukti` (+ `GET foto`),
> `RatingController` (POST + 2 GET), `DeviceTokenController`
> (`POST notifikasi/token`), entitas `Rating`/`DeviceToken`,
> `fotoBuktiUrl` di `Penjemputan`, dan `docs/FCM_SETUP.md`. Semuanya
> cocok dengan kontrak — tidak ada penyimpangan (lihat "Pemeriksaan
> kontrak" di bawah). Yang belum terverifikasi adalah perilaku
> runtime-nya (butuh deploy + uji end-to-end).

## 1. Batalkan + Jadwalkan Ulang — `ui/screens/OtherScreens.kt`
- Daftar penjemputan (`PenjemputanScreen`): kartu bisa diketuk untuk
  struk; bila status `MENUNGGU`/`DIJADWALKAN` muncul tombol **Batalkan**
  (dialog konfirmasi) dan **Jadwalkan Ulang** (DatePicker + TimePicker).
- `PUT penjemputan/{id}/batal` (body kosong), `PUT penjemputan/{id}/jadwal`
  `{"tanggalJemput","waktuJemput"}`. Setelah berhasil, refresh dari
  server; event `PENJEMPUTAN_STATUS` dari `RealtimeEventHub` juga
  me-refresh daftar otomatis (pola refresh-on-event yang sudah ada).
- Item yang hanya tersimpan lokal (belum ada di server) akan dijawab
  404 dan ditampilkan sebagai pesan, bukan crash.

## 4. FCM Token (persiapan, tanpa kredensial) — `api/FcmTokenSync.kt`
- Dependency `firebase-messaging:23.4.0` ditambahkan di
  `app/build.gradle` **SAJA** — plugin `google-services` sengaja TIDAK
  dipakai karena gagal build bila `google-services.json` tidak ada.
- `MainActivity` memanggil `FcmTokenSync.sync()` setelah login:
  `FirebaseMessaging.getInstance().token` di dalam try/catch; bila
  Firebase belum terinisialisasi, gagal diam-diam dan aplikasi lanjut
  normal. Bila token didapat, dikirim ke `POST notifikasi/token`
  `{"token","platform":"android"}`.
- **FCM BELUM AKTIF** sampai user menyediakan `google-services.json`.
  Langkah aktivasi lengkap ada di `docs/FCM_SETUP.md` (agen backend).
  Tidak ada push yang diterima/ditampilkan sebelum itu.

## 5. Struk Digital — `ui/screens/ReceiptDialog.kt`
- Ketuk satu riwayat di `RiwayatScreen` (atau satu penjemputan di
  `PenjemputanScreen`) → memuat `GET penjemputan/{id}` dan membuka
  struk: nama nasabah, tanggal, petugas, tiap jenis sampah (nama, kg,
  harga/kg, total), total bayar, status, foto bukti bila ada.
- Parsing defensif: `user`/`petugas` bisa objek nested atau field datar;
  field hilang ditampilkan "-" — struk tidak crash.
- Tombol **Bagikan via WhatsApp**: `Intent.ACTION_SEND` teks struk,
  package `com.whatsapp`, fallback ke chooser bila WhatsApp tidak
  terpasang. Format teks polos ada di `buildReceiptText()`.

## 6. Foto Bukti di Riwayat — `ReceiptDialog.kt`, `BankSampahHomeScreen.kt`
- Foto dimuat dari `fotoBuktiUrl` (`/api/penjemputan/{id}/foto`) dengan
  Coil (`coil-compose` sudah ada di `build.gradle` — tidak ada
  dependency baru).
- Upload oleh petugas: di dialog Timbang, tombol "Tambah Foto Bukti"
  (galeri) → downscale maks. 1024px, JPEG 80, Base64 → setelah timbang
  berhasil, `PUT penjemputan/{id}/foto-bukti`
  `{"fotoBase64","contentType":"image/jpeg"}`.

## 7. Mode Offline Petugas — `api/OfflineQueue.kt`
- Antrean di SharedPreferences + Gson (tanpa dependency baru). Bila
  timbang gagal karena jaringan, operasi disimpan dan status
  **"Menunggu sync: N timbang"** tampil di Beranda Bank Sampah.
- Sync ulang berurutan (FIFO) saat aplikasi dibuka dan saat kanal
  `/ws/events` tersambung kembali (`RealtimeEventHub.addConnectListener`).
  Operasi dihapus hanya bila server menjawab 2xx; 4xx dibuang (ditolak
  server, mengulang tak berguna); 5xx/gagal jaringan tetap mengantre.
- Batasan (juga tertulis di komentar kode): tanpa resolusi konflik
  (last-write-wins); antrean hilang bila data aplikasi dihapus; operasi
  yang sedang dikirim ditandai `inFlight` agar tidak terkirim dua kali.

## 8. Rute Penjemputan Optimal — `ui/screens/RouteUtil.kt`
- Beranda Bank Sampah kini memuat `GET penjemputan` dan menampilkan
  jemputan **hari ini** (bukan lagi daftar dummy) — alamat/koordinat
  hanya dari API, tidak ada yang dikarang.
- Urutan: nearest-neighbor (haversine) dari titik paling awal bila ada
  koordinat; titik tanpa koordinat diurutkan sesudahnya menurut
  waktuJemput lalu alamat; bila tidak ada koordinat sama sekali,
  urutan waktu lalu alamat.
- Daftar bernomor urut rute + tombol **Buka Rute di Maps** (URL
  `https://www.google.com/maps/dir/?api=1&origin=...&destination=...&waypoints=...`
  dari alamat/koordinat asli). Bila tidak ada satu pun alamat/koordinat,
  tombol nonaktif dengan penjelasan.

## 9. Rating — `ui/screens/RatingUi.kt`
- Di struk penjemputan `SELESAI`: cek dulu `GET rating/penjemputan/{id}`.
  Sudah dinilai → tampilkan nilai yang diberikan, tidak minta dua kali.
  Belum (404) → tombol **Beri Rating** (bintang 1–5 + komentar opsional)
  → `POST rating` `{"penjemputanId","nilai","komentar"}`.
- Rata-rata petugas `GET rating/petugas/{id}` (`{"petugasId","rataRata","jumlah"}`)
  ditampilkan di struk ("★ 4.5 (12 ulasan)" / "Belum ada rating").

## 10. Misi & Badge — `ui/screens/MissionsScreen.kt`, `ui/MisiActivity.kt`
- Layar sendiri (`MisiActivity`), dijangkau dari Beranda (menu "Misi")
  dan Profil (tombol "Misi & Badge").
- Dihitung dari data yang sudah ada: `GET user/me` (poin),
  `GET penjemputan/my` (jumlah setoran `SELESAI`, total kg dari
  `beratAktual`, fallback `estimasiBerat`). Definisi tetap:
  - **Setor Pertama** — 1 setoran SELESAI
  - **Pejuang 10 Kg** — total ≥ 10 kg
  - **Langganan Hijau** — 5 setoran SELESAI
  - **Kolektor 100 Poin** — poin ≥ 100
- Progres tiap misi (mis. 3/5 setoran) + status selesai/belum.
  **Tidak ada poin bonus baru dari server** — dinyatakan di UI dan
  komentar kode; poin tetap dari timbang dan tampil di Leaderboard.

## Pemeriksaan kontrak vs backend (dilakukan 2026-11-26)
Semua cocok, tanpa penyimpangan:
- `PUT penjemputan/{id}/batal` (body kosong), `PUT penjemputan/{id}/jadwal`
  `{"tanggalJemput","waktuJemput"}` — ada di `PenjemputanController`.
  Batal hanya diizinkan bila belum `SELESAI`/`DIBATALKAN` — konsisten
  dengan UI yang hanya menampilkan tombol untuk `MENUNGGU`/`DIJADWALKAN`.
- `GET penjemputan/{id}` mengembalikan `Penjemputan` lengkap;
  `getById()` menginisialisasi `detailList`, `user`, `petugas`,
  `transaksi`. Catatan: `detail.sampah` tidak diinisialisasi eksplisit —
  serialisasinya bergantung pada open-in-view; parser struk Android
  sudah defensif (nama/harga hilang → "-"), tapi perlu dicek di uji
  end-to-end apakah nama + harga/kg per baris benar-benar terkirim.
- `PUT penjemputan/{id}/foto-bukti` `{"fotoBase64","contentType"}` →
  `{"fotoBuktiUrl":"/api/penjemputan/{id}/foto"}`, batas 5 MB setelah
  decode — foto Android di-downscale (maks. 1024px, JPEG 80) agar aman.
- `POST /api/rating` `{"penjemputanId","nilai","komentar"}` (petugas
  diambil server dari penjemputan); `GET rating/petugas/{id}` →
  `{"petugasId","rataRata","jumlah"}`; `GET rating/penjemputan/{id}` →
  404 bila belum dinilai, map berisi `nilai` bila sudah.
- `POST /api/notifikasi/token` `{"token","platform"}` (upsert per token)
  — ada di `DeviceTokenController`; `docs/FCM_SETUP.md` sudah dibuat
  agen backend (status: FCM belum aktif sampai `google-services.json`
  + service-account disediakan user).
- `POST penjemputan/timbang` yang sudah ada tidak berubah
  (`{"penjemputanId","details":[{"detailId","beratAktual"}]}`).

## Yang masih butuh dicocokkan (setelah backend selesai)
1. Uji end-to-end: apakah `GET penjemputan/{id}` benar-benar mengirim
   `detailList[].sampah.{nama,hargaPerKg}` (lihat catatan di atas).
2. `docs/FCM_SETUP.md` dari agen backend + `google-services.json` dari
   user sebelum FCM diaktifkan.
3. Uji end-to-end (butuh Android SDK/Gradle, server + PostgreSQL, deploy
   VPS): batal/jadwal, timbang offline → sync, rute Maps, rating,
   struk + foto, misi.

## Verifikasi yang dilakukan (VM ini, tanpa Android SDK)
- `kotlinc` 1.9.22 dipakai untuk pemeriksaan sintaks file Kotlin yang
  diubah (referensi Android/Compose tentu tidak ter-resolve — itu wajar
  di VM ini; hanya error sintaks murni yang dicari).
- `javac` (JDK 17, UTF-8) mengkompilasi penuh `ResikApi.java` +
  `Penjemputan.java` + model lain dengan Retrofit/Gson di classpath:
  **berhasil, tanpa error**.
- Pemeriksaan kotlinc menemukan 2 bug nyata yang sudah diperbaiki:
  (1) `ReceiptDialog` memakai `DetailRow` yang `private` di file
  `OtherScreens.kt` → diganti `StrukDetailRow` lokal;
  (2) `return` di dalam lambda `Column` di `MisiScreen` (tidak legal)
  → ditulis ulang sebagai if/else. Setelah perbaikan, tidak ada lagi
  error selain knock-on dari classpath yang tidak ada (referensi
  Android/Compose/Retrofit yang belum ter-resolve).
- `RouteUtil.kt` (logika rute, murni Kotlin) dikompilasi bersih dengan
  kotlinc.
- Tidak ada referensi ke endpoint/field di luar kontrak; tidak ada
  perubahan di `backend-api/` atau `desktop-admin/`; tidak ada push,
  deploy, atau perubahan secret/keystore/URL API/harga.
