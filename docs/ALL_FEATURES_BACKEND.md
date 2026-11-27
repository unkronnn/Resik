# All Features — Backend (batch `feat/all-features`)

Daftar endpoint baru dan perubahan backend dari batch ini. Semua endpoint
di bawah memakai JWT yang sama dengan REST lain (`Authorization: Bearer
<jwt>`); bila tidak disebut lain, tanpa token yang valid jawabannya
`401`, dan otorisasi yang gagal `403`. Error memakai
`ResponseStatusException` Spring: body `{"status": ..., "error": ...,
"message": "...", "path": ...}` (format default Spring).

Dokumen terkait: `docs/REALTIME_EVENTS.md` (event + auth WebSocket),
`docs/FCM_SETUP.md` (aktivasi FCM), `docs/RATING.md` (desain rating),
`docs/SECURITY.md` (yang masih terbuka). Migrasi DB:
`backend-api/src/main/resources/db/migration/V3__all_features.sql`.

## 0. Keamanan WebSocket

`JwtHandshakeInterceptor` memvalidasi JWT saat handshake `/ws/chat` dan
`/ws/events`. Client mengirim `?token=<jwt>` (atau header
`Authorization: Bearer <jwt>`). `userId`/`role` diambil dari klaim token;
`?role=` / `?userId=` diabaikan. **Client wajib** menambahkan token ke URL
WebSocket — tanpa token, handshake ditolak. Detail: `docs/SECURITY.md`.

## 1. Batalkan + jadwalkan ulang penjemputan

### `PUT /api/penjemputan/{id}/batal` — body kosong

Hanya pemilik penjemputan, `ADMIN`, atau `PETUGAS`. Status menjadi
`DIBATALKAN`. Ditolak (`400`) bila status sudah `SELESAI`/`DIBATALKAN`.
Broadcast `PENJEMPUTAN_STATUS` + push FCM ke pemilik.

```http
PUT /api/penjemputan/12/batal
Authorization: Bearer <jwt>
```

```json
// 200 — Penjemputan (status DIBATALKAN)
{"id": 12, "status": "DIBATALKAN", "tanggalJemput": "2026-11-28",
 "waktuJemput": "09:00", "...": "..."}
```

### `PUT /api/penjemputan/{id}/jadwal` — ubah tanggal/waktu

Hanya pemilik penjemputan, `ADMIN`, atau `PETUGAS`. Status tidak berubah
(tanggal/waktu baru ikut dalam payload event). Ditolak (`400`) bila status
sudah `SELESAI`/`DIBATALKAN`, atau bila `tanggalJemput` dan `waktuJemput`
keduanya kosong. Broadcast `PENJEMPUTAN_STATUS` + push FCM ke pemilik.

```http
PUT /api/penjemputan/12/jadwal
Authorization: Bearer <jwt>
Content-Type: application/json

{"tanggalJemput": "2026-11-28", "waktuJemput": "09:00"}
```

```json
// 200 — Penjemputan (tanggal/waktu baru)
{"id": 12, "status": "MENUNGGU", "tanggalJemput": "2026-11-28",
 "waktuJemput": "09:00", "...": "..."}
```

### `PUT /api/penjemputan/{id}/status` — ubah status (desktop)

Hanya `ADMIN` / `PETUGAS`. Transisi valid: `MENUNGGU → DIJADWALKAN /
DIBATALKAN`, `DIJADWALKAN → DIPROSES / DIBATALKAN`, `DIPROSES →
DIBATALKAN`. Mengirim status yang sama adalah no-op yang idempotent
(`200`). Ditolak (`400`): transisi tidak valid, keluar dari `SELESAI` /
`DIBATALKAN`, atau target `SELESAI` — penjemputan selesai **hanya** lewat
`POST /api/penjemputan/timbang` (yang membuat `Transaksi` dan mengkredit
dompet; status `SELESAI` tanpa transaksi akan inkonsisten). Broadcast
`PENJEMPUTAN_STATUS` + push FCM ke pemilik.

```http
PUT /api/penjemputan/12/status
Authorization: Bearer <jwt>
Content-Type: application/json

{"status": "DIPROSES"}
```

```json
// 200 — Penjemputan (status DIPROSES)
{"id": 12, "status": "DIPROSES", "...": "..."}
```

### `GET /api/penjemputan/{id}` — detail lengkap untuk struk

```http
GET /api/penjemputan/12
Authorization: Bearer <jwt>
```

```json
// 200 — Penjemputan dengan detailList, transaksi, petugas, fotoBuktiUrl
{"id": 12, "status": "SELESAI", "tanggalJemput": "2026-11-28",
 "waktuJemput": "09:00", "alamat": "Jl. Merdeka 45",
 "user": {"id": 7, "nama": "Budi", "...": "..."},
 "petugas": {"id": 3, "nama": "Sari", "...": "..."},
 "detailList": [{"id": 31, "estimasiBerat": 5.0, "beratAktual": 6.2,
                 "subtotal": 15500,
                 "sampah": {"id": 4, "nama": "Plastik Botol", "hargaPerKg": 2500}}],
 "transaksi": {"id": 9, "totalHarga": 15500, "status": "BERHASIL"},
 "fotoBuktiUrl": "/api/penjemputan/12/foto"}
```

Catatan serialisasi: `DetailPenjemputan.penjemputan` dan
`Transaksi.penjemputan` diberi `@JsonIgnore` — tanpa itu Jackson
berputar `Penjemputan → detailList/transaksi → penjemputan → …` dan
endpoint di atas mengembalikan 500. Arah maju (penjemputan → detail →
sampah, penjemputan → transaksi) tetap terserialisasi penuh; relasi
lazy yang diserialisasi memakai
`@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` seperti
`Chat` sebelumnya. `PenjemputanService.getById` menginisialisasi
eksplisit tiap `detail.sampah` di dalam transaksi (bukan hanya
`detailList`), supaya `nama` + `hargaPerKg` tiap baris detail
benar-benar terkirim untuk struk — tidak bergantung pada open-in-view
saat serialisasi.

## 2. Approve / tolak akun

`User.statusVerifikasi`: `MENUNGGU` (default untuk register baru) /
`DISETUJUI` / `DITOLAK`. Akun yang sudah ada sebelum migrasi V3 diangkat
menjadi `DISETUJUI`. Login menolak akun `DITOLAK` (`401`, pesan
`Akun ditolak oleh admin: <alasan>`); akun `MENUNGGU` tetap bisa login
dan statusnya terlihat di `AuthResponse.statusVerifikasi` serta
`GET /api/user/me`. Register tetap broadcast `ACCOUNT_VERIFICATION`
(status `MENUNGGU`).

### `GET /api/admin/verifikasi` — daftar akun menunggu (admin saja)

```http
GET /api/admin/verifikasi
Authorization: Bearer <jwt admin>
```

```json
// 200
[{"id": 21, "nama": "Budi", "email": "budi@mail.com", "role": "MASYARAKAT",
  "statusVerifikasi": "MENUNGGU", "alasanPenolakan": null,
  "alamat": "Jl. Merdeka 45", "noTelp": "0812...", "createdAt": "2026-11-26T10:00:00"}]
```

### `POST /api/admin/users/{id}/approve` — tanpa body (admin saja)

```http
POST /api/admin/users/21/approve
Authorization: Bearer <jwt admin>
```

```json
// 200 — sama seperti satu entri daftar di atas, dengan:
{"id": 21, "statusVerifikasi": "DISETUJUI", "alasanPenolakan": null, "...": "..."}
```

### `POST /api/admin/users/{id}/reject` — body opsional (admin saja)

```http
POST /api/admin/users/21/reject
Authorization: Bearer <jwt admin>
Content-Type: application/json

{"alasan": "Dokumen tidak valid"}
```

```json
// 200
{"id": 21, "statusVerifikasi": "DITOLAK", "alasanPenolakan": "Dokumen tidak valid", "...": "..."}
```

Approve/reject broadcast `ACCOUNT_VERIFICATION` dengan status baru
(`DISETUJUI`/`DITOLAK`, `alasan` ikut bila ada) ke admin dan ke user
yang bersangkutan. Respons sengaja bukan entitas `User` mentah (hash
password tidak boleh terserialisasi).

## 4. FCM server-side (persiapan — BELUM aktif)

**FCM belum mengirim apa pun** sampai Syukron menyediakan proyek
Firebase + service-account; lihat `docs/FCM_SETUP.md` untuk langkah
persisnya. Tanpa service-account, semua kirim adalah no-op berlog
peringatan. Dependency: `com.google.firebase:firebase-admin:9.4.0`
(hanya dependency, tanpa file kredensial di repo).

### `POST /api/notifikasi/token` — daftarkan token perangkat (upsert)

```http
POST /api/notifikasi/token
Authorization: Bearer <jwt>
Content-Type: application/json

{"token": "eXaMpLeToKeN...", "platform": "android"}
```

```json
// 200
{"id": 5, "userId": 7, "platform": "android", "updatedAt": "2026-11-26T15:00:00"}
```

Token yang sama didaftarkan ulang hanya memperbarui baris yang ada
(unik per token).

### `GET /api/notifikasi/token?userId=7` — daftar token satu user

Hanya pemilik token atau admin. Token mentahnya **tidak** dikembalikan
(token adalah kredensial perangkat).

```json
// 200
[{"id": 5, "userId": 7, "platform": "android", "updatedAt": "2026-11-26T15:00:00"}]
```

Pengiriman dipakai di `PenjemputanService`: penjemputan dijadwalkan
(`assignPetugas`), dibatalkan, dijadwalkan ulang, status berubah, dan
timbangan selesai — semuanya ke pemilik penjemputan, via
`FcmNotificationService.sendToUser(...)`.

## 6. Foto bukti timbangan

Foto disimpan di `Penjemputan.fotoBuktiUrl` (dipilih atas `Transaksi`:
foto adalah bukti atas satu penjemputan, struk dibangun dari
penjemputan, dan foto bisa diunggah setelah timbang selesai; satu
penjemputan punya tepat satu transaksi sehingga tidak ambigu). File
tersimpan di `uploads/bukti/{id}.{jpg|png|webp}` di bawah direktori
kerja backend (bisa diubah lewat env `RESIK_UPLOADS_DIR`); yang
disimpan di DB hanya URL penyajiannya.

### `PUT /api/penjemputan/{id}/foto-bukti` — upload Base64

Batas 5 MB setelah decode (`413` bila lewat). `contentType` harus
`image/jpeg`, `image/png`, atau `image/webp` (`400` bila lain).
Prefix `data:image/...;base64,` ditoleransi. Upload ulang menggantikan
foto lama (termasuk file lama dengan ekstensi berbeda).

```http
PUT /api/penjemputan/12/foto-bukti
Authorization: Bearer <jwt>
Content-Type: application/json

{"fotoBase64": "/9j/4AAQSkZJRgABAQAAAQABAAD...", "contentType": "image/jpeg"}
```

```json
// 200
{"fotoBuktiUrl": "/api/penjemputan/12/foto"}
```

### `GET /api/penjemputan/{id}/foto` — sajikan byte foto

```http
GET /api/penjemputan/12/foto
```

```http
// 200 — Content-Type: image/jpeg (sesuai ekstensi tersimpan), body = byte file
// 404 — penjemputan tidak ada, belum punya foto, atau file tidak ditemukan
```

## 9. Rating bank sampah (rating petugas)

Desain lengkap: `docs/RATING.md`. Yang dinilai adalah **petugas**
yang melakukan penjemputan (`penjemputan.petugas`, diambil server dari
penjemputan — body tidak membawa `petugasId`), bukan lokasi bank
sampah: `Penjemputan` tidak punya relasi ke `LokasiBankSampah`,
sehingga lokasi tidak dapat dinilai dari sebuah penjemputan tanpa
mengarang kaitan.

### `POST /api/rating` — beri rating

Hanya pemilik penjemputan; penjemputan harus `SELESAI`; satu rating
per penjemputan.

```http
POST /api/rating
Authorization: Bearer <jwt>
Content-Type: application/json

{"penjemputanId": 12, "nilai": 5, "komentar": "Cepat dan ramah"}
```

```json
// 201
{"id": 4, "penjemputanId": 12, "pemberiRatingId": 7, "petugasId": 3,
 "nilai": 5, "komentar": "Cepat dan ramah", "createdAt": "2026-11-26T15:30:00"}
// 400 — penjemputan belum SELESAI, atau nilai di luar 1-5
// 403 — pemberi bukan pemilik penjemputan
// 409 — penjemputan ini sudah pernah dinilai
```

### `GET /api/rating/petugas/{petugasId}` — ringkasan rating petugas

```http
GET /api/rating/petugas/3
Authorization: Bearer <jwt>
```

```json
// 200 — rataRata dibulatkan 1 desimal; 0.0 bila belum ada rating
{"petugasId": 3, "rataRata": 4.5, "jumlah": 12}
```

### `GET /api/rating/penjemputan/{id}` — rating satu penjemputan

```http
GET /api/rating/penjemputan/12
Authorization: Bearer <jwt>
```

```json
// 200 — bentuk sama seperti respons POST; 404 bila belum ada rating
{"id": 4, "penjemputanId": 12, "pemberiRatingId": 7, "petugasId": 3,
 "nilai": 5, "komentar": "Cepat dan ramah", "createdAt": "2026-11-26T15:30:00"}
```

## Tindak lanjut koordinator (penggabungan `feat/all-features`)

- Client WebSocket kini mengirim token: Android (`EventWebSocketClient`,
  chat di `OtherScreens`) dan desktop (`RealtimeEventClient`,
  `ChatWebSocketClient`) menambahkan `?token=<jwt>` dari token login yang
  tersimpan (`ApiClient.getToken()` / `AppConfig.authToken`). Tanpa token,
  handshake ditolak — ini yang disyaratkan `JwtHandshakeInterceptor`.
  Mode demo desktop (`AppConfig.authToken = "demo-token"`) akan ditolak
  handshake-nya; itu perilaku yang benar.
- `User.password` diberi `@JsonIgnore`: hash password tidak lagi ikut
  terserialisasi di `Penjemputan.user`/`petugas` dan `GET /api/user/all`.
  Aman karena tidak ada endpoint yang menerima `@RequestBody User`
  (register/login memakai DTO).
