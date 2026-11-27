# Realtime Events — sinkronisasi Admin, Bank Sampah, dan Masyarakat

Selain chat (yang sudah realtime lewat `/ws/chat` sejak awal), backend
menyediakan kanal event umum **`/ws/events`** supaya semua perubahan data
penting tersiar ke semua peran yang sedang online, tanpa refresh manual.

Kanal ini memakai pola yang sama persis seperti chat: plain
`TextWebSocketHandler` Spring (bukan STOMP). Sejak batch all-features,
**kedua kanal wajib JWT saat handshake** (lihat `docs/SECURITY.md`):
token login dari `POST /api/auth/login` dikirim sebagai
`?token=<jwt>` (atau header `Authorization: Bearer <jwt>`).
`userId` dan `role` diambil dari klaim token — query param `?role=` /
`?userId=` yang lama diabaikan.

## Cara berlangganan

```
ws://<host>:8081/ws/events?token=<jwt dari login>
ws://<host>:8081/ws/chat?token=<jwt dari login>
```

Peran (dari token) menentukan apa yang diterima:

| Peran (dari token) | Menerima |
|---|---|
| `ADMIN` | Semua event |
| `PETUGAS` | Penjemputan, stok, harga sampah, leaderboard |
| `MASYARAKAT` | Event broadcast + event yang ditujukan ke `userId` sendiri (mis. saldo sendiri) |

Client yang tidak mengirim token valid akan ditolak saat handshake.
Karena token berlaku 24 jam, client harus reconnect dengan token baru
setelah login ulang.

Client boleh mengirim `{"type":"ping"}` sebagai keepalive; server menjawab
`{"type":"pong"}`. Semua client yang sudah ada melakukan reconnect otomatis
setelah 3 detik dan ping tiap 30 detik.

## Envelope event

Setiap pesan adalah JSON dengan tiga field:

```json
{
  "type": "PENJEMPUTAN_STATUS",
  "timestamp": "2026-11-26T14:55:00.123Z",
  "payload": { "...": "..." }
}
```

Client harus mengabaikan `type` yang tidak dikenal, supaya server bisa
menambah tipe event baru tanpa merusak aplikasi lama.

## Tipe event

### `PENJEMPUTAN_CREATED` — masyarakat membuat permintaan jemput

```json
{"type": "PENJEMPUTAN_CREATED", "timestamp": "...",
 "payload": {"penjemputanId": 12, "userId": 7,
             "tanggalJemput": "2026-11-27", "waktuJemput": "09:00-11:00",
             "alamat": "Jl. Merdeka 45", "status": "MENUNGGU"}}
```

Penerima: admin, bank sampah. Dipicu di
`PenjemputanService.createPenjemputan`.

### `PENJEMPUTAN_STATUS` — status penjemputan berubah

```json
{"type": "PENJEMPUTAN_STATUS", "timestamp": "...",
 "payload": {"penjemputanId": 12, "userId": 7, "petugasId": 3,
             "status": "SELESAI", "statusLama": "DIJADWALKAN",
             "tanggalJemput": "2026-11-27"}}
```

`status` salah satu dari `MENUNGGU`, `DIJADWALKAN`, `DIPROSES`, `SELESAI`,
`DIBATALKAN`. Penerima: admin, bank sampah, dan masyarakat pemilik.
Dipicu di `PenjemputanService.assignPetugas` (menjadi `DIJADWALKAN`),
`PenjemputanService.timbangSampah` (menjadi `SELESAI`),
`PenjemputanService.batalkanPenjemputan` (menjadi `DIBATALKAN`),
`PenjemputanService.jadwalUlang` (tanggal/waktu baru, status tetap), dan
`PenjemputanService.ubahStatus` (endpoint desktop `PUT /{id}/status`).

### `WALLET_UPDATED` — saldo / poin dompet berubah

```json
{"type": "WALLET_UPDATED", "timestamp": "...",
 "payload": {"userId": 7, "saldoBaru": 15500, "poinBaru": 15,
             "perubahan": 15500, "alasan": "TIMBANG"}}
```

`alasan` salah satu dari `TIMBANG`, `TOPUP`, `TARIK` (`perubahan` negatif
untuk `TARIK`). Penerima: pemilik dompet dan admin. Dipicu di
`PenjemputanService.timbangSampah` dan `DompetController.topup` /
`DompetController.tarik`.

### `SAMPAH_CREATED` / `HARGA_SAMPAH_UPDATED` / `SAMPAH_DELETED` — data sampah berubah

```json
{"type": "HARGA_SAMPAH_UPDATED", "timestamp": "...",
 "payload": {"sampahId": 4, "kategori": "ANORGANIK", "nama": "Kardus",
             "hargaPerKg": 1800, "hargaLama": 1500}}
```

`SAMPAH_CREATED` dan `SAMPAH_DELETED` (`payload`: `sampahId`, `nama`)
punya bentuk payload yang sama minus harga. Penerima: semua peran.
Dipicu di `SampahController.create` / `update` / `delete`.

### `ACCOUNT_VERIFICATION` — akun baru menunggu verifikasi / keputusan admin

```json
{"type": "ACCOUNT_VERIFICATION", "timestamp": "...",
 "payload": {"userId": 21, "nama": "Budi", "email": "budi@mail.com",
             "role": "MASYARAKAT", "status": "MENUNGGU"}}
```

Penerima: admin (dan user yang bersangkutan saat ada keputusan).
Dipicu di `AuthService.register` (status `MENUNGGU`, untuk mengisi
halaman desktop Verifikasi & Approval) dan di
`AdminVerificationService.approve` / `.reject` (status `DISETUJUI` /
`DITOLAK`, payload `alasan` ikut bila ada) — tipe event yang sama, jadi
tabel desktop bisa memperbarui barisnya di tempat tanpa re-fetch.

### `STOK_UPDATED` — stok hasil timbang berubah

```json
{"type": "STOK_UPDATED", "timestamp": "...",
 "payload": {"penjemputanId": 12,
             "items": [{"kategori": "ANORGANIK", "beratKg": 6.2},
                       {"kategori": "ORGANIK", "beratKg": 1.5}]}}
```

Penerima: admin, bank sampah. Dipicu di
`PenjemputanService.timbangSampah`.

### `LEADERBOARD_UPDATED` — poin berubah, urutan leaderboard bisa berubah

```json
{"type": "LEADERBOARD_UPDATED", "timestamp": "...",
 "payload": {"userId": 7, "poinBaru": 15}}
```

Penerima: semua peran. Client cukup re-fetch `GET /api/leaderboard`.
Dipicu di `PenjemputanService.timbangSampah`.

## Yang berubah di client

### Android (`android-client`)

- `api/RealtimeEvent.kt` — model envelope + konstanta tipe event.
- `api/EventWebSocketClient.kt` — client WebSocket (pola sama seperti
  `ChatWebSocketClient`) + `RealtimeEventHub`, satu koneksi per proses yang
  dipakai semua layar. `MainActivity` menghubungkannya setelah login.
- `api/ResikApi.java` — endpoint re-fetch baru: `dompet/saldo`,
  `dompet/riwayat`, `leaderboard`.
- `MainActivity` — saldo/poin beranda ikut `WALLET_UPDATED`.
- `DompetActivity` — saldo asli dari server (sebelumnya hardcode "Rp 0"),
  ikut `WALLET_UPDATED`.
- `OtherScreens.kt` — `PenjemputanScreen`, `RiwayatScreen`, dan
  `LeaderboardScreen` memuat data asli dari server dan me-refresh otomatis
  saat event terkait tiba. Kalau server tidak terjangkau, tetap tampil data
  lokal/dummy seperti sebelumnya (fallback, bukan layar kosong).
- Chat tidak diubah: tetap `/ws/chat` + polling 5 detik.

### Desktop (`desktop-admin`)

- `service/RealtimeEventClient.java` — client WebSocket (pola sama seperti
  `ChatWebSocketClient`), terhubung sekali saat aplikasi dibuka.
- `ui/DashboardView.java` — `connectEventWebSocket()` + `onRealtimeEvent()`.
  Aturannya: update data dulu, lalu render ulang hanya halaman yang sedang
  dibuka (dilacak lewat `currentView`). Tabel penjemputan terikat ke
  `penjemputanData`, jadi barisnya berubah langsung saat event tiba.
  Tombol "Refresh" manual di Data Sampah dihapus, diganti label Live.
- Chat tidak diubah: tetap `/ws/chat` + polling 15 detik.

## Yang belum realtime (batas yang diketahui)

- Tidak ada push notification saat aplikasi tertutup (butuh FCM — kode
  server-side sudah disiapkan, lihat `docs/FCM_SETUP.md`, tetapi belum
  aktif sampai Syukron menyediakan proyek Firebase + service-account).
- Aksi desktop "Tugaskan Petugas" dan "Proses Penjemputan" masih mengubah
  status lokal saja (tidak memanggil API), jadi tidak memicu event ke
  client lain. Endpointnya sudah siap (`PUT /api/penjemputan/{id}/assign/{petugasId}`
  dan `PUT /api/penjemputan/{id}/status`); yang kurang adalah pemanggilan
  API-nya dari desktop.
- Layar verifikasi desktop masih memakai data dummy; endpoint
  `GET /api/admin/verifikasi` sudah tersedia untuk menggantikannya.
