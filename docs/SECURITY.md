# Keamanan backend Resik — yang sudah diperbaiki dan yang masih terbuka

## Yang diperbaiki di batch ini

### 1. WebSocket kini wajib JWT (task 0)

- `websocket/JwtHandshakeInterceptor.java` memvalidasi token login
  (JWT yang sama dengan REST, lewat `JwtUtil` yang sama) saat handshake
  `/ws/chat` dan `/ws/events`. Token dikirim lewat query param
  `?token=<jwt>` (satu-satunya cara di browser WebSocket) atau header
  `Authorization: Bearer <jwt>`. Handshake ditolak bila token tidak ada,
  kedaluwarsa, atau tanda tangannya tidak valid.
- `userId` dan `role` diambil **dari klaim token**, bukan dari query
  param. Query param `?role=` / `?userId=` yang lama sekarang
  **diabaikan** — client tidak lagi bisa menyamar sebagai user lain atau
  sebagai admin dengan mengedit URL. `ChatWebSocketHandler` dan
  `RealtimeEventHandler` membaca identitas dari atribut sesi yang diisi
  interceptor; sesi tanpa identitas ditutup (`POLICY_VIOLATION`).
- **Dampak client (wajib)**: Android `ChatWebSocketClient` /
  `EventWebSocketClient` dan desktop `ChatWebSocketClient` /
  `RealtimeEventClient` harus menambahkan `?token=<jwt dari login>` ke
  URL WebSocket. Tanpa token, koneksi ditolak. Token login berlaku 24 jam
  (`jwt.expiration`), jadi client harus reconnect dengan token baru
  setelah login ulang / token refresh.

### 2. Endpoint baru menegakkan otorisasi di controller

- Batal / jadwal ulang penjemputan: pemilik penjemputan, `ADMIN`, atau
  `PETUGAS`. Selain itu `403`.
- Ubah status penjemputan (`PUT /{id}/status`): `ADMIN` / `PETUGAS` saja.
- Approve / reject akun dan daftar verifikasi: `ADMIN` saja
  (dicek dari authorities JWT).
- Rating: hanya pemilik penjemputan (`403` bila bukan).
- Token FCM: pemilik token sendiri atau admin.
- Endpoint verifikasi admin tidak pernah mengembalikan entitas `User`
  mentah (hash password ikut terserialisasi di `UserController.getAllUsers`
  yang lama); hanya field publik yang dikembalikan.

### 3. Login menolak akun yang ditolak admin

`AuthService.login` menolak akun `statusVerifikasi = DITOLAK` dengan
`401`. Akun `MENUNGGU` tetap bisa login (keputusan yang didokumentasikan
di `AuthService`): verifikasi mengatur daftar approval admin, bukan
login itu sendiri — supaya alur register-lalu-login yang sudah dipakai
client tidak rusak. Statusnya terlihat client lewat field
`statusVerifikasi` di `AuthResponse` dan `GET /api/user/me`.

## Yang masih terbuka (belum diperbaiki, perlu keputusan user)

### A. `SecurityConfig` masih `permitAll` untuk sebagian besar path API

`/api/penjemputan/**`, `/api/dompet/**`, `/api/chat/**`,
`/api/sampah/**`, `/api/lokasi/**`, `/api/leaderboard`, dan `/ws/**`
masih `permitAll` — warisan konfigurasi lama. Artinya filter JWT tidak
memblokir request tanpa token di path itu; endpoint-endpoint yang
membutuhkan identitas menegakkannya sendiri di controller
(`PenjemputanController.requireUserId`, dan lain-lain), dan endpoint
lama yang tidak mengecek (mis. `GET /api/penjemputan`,
`POST /api/penjemputan/timbang`, `PUT /api/penjemputan/{id}/assign/{petugasId}`,
`GET /api/user/all`) **masih bisa dipanggil tanpa login**. Ini sengaja
tidak diubah di batch ini karena mengubahnya dapat mematikan client lama
yang memanggil endpoint itu tanpa token — tetapi ini adalah lubang
terbesar yang tersisa. Langkah yang disarankan: setelah client dipastikan
mengirim token di semua panggilan, persempit `permitAll` hanya ke
`/api/auth/**` dan endpoint yang memang publik, lalu uji ulang.

### B. Token JWT di query string WebSocket bisa tercatat di log

Token di URL handshake (`wss://.../ws/events?token=...`) dapat muncul di
access log / proxy log. Mitigasi saat ini: wajib `wss://` di produksi dan
masa berlaku token 24 jam. Perbaikan lanjutan: autentikasi lewat pesan
pertama setelah koneksi (bukan URL), atau token WebSocket berumur pendek
yang terpisah dari token REST.

### C. Secret JWT dan CORS

- `jwt.secret` dibaca dari `application.yml` / env. Pastikan nilai
  produksinya panjang dan acak dan **tidak sama** dengan nilai default
  di repo; bila secret bocor, semua token yang pernah diterbitkan harus
  dianggap kompromi (tidak ada daftar pencabutan token).
- CORS mengizinkan semua origin (`allowedOriginPatterns: *`) dengan
  kredensial. Untuk produksi, batasi ke origin aplikasi yang sebenarnya.

### D. Foto bukti tersimpan di disk lokal, disajikan tanpa otorisasi

`GET /api/penjemputan/{id}/foto` menyajikan file tanpa memeriksa siapa
pemanggilnya (siapa pun yang tahu id penjemputan bisa mengunduh foto),
dan file tersimpan di disk lokal backend (`uploads/bukti/`, bisa
diubah lewat `RESIK_UPLOADS_DIR`) — bukan di object storage. Bila
backend di-deploy dengan beberapa replika, foto hanya ada di replika
yang menerima upload. Untuk produksi: batasi GET foto ke pemilik /
petugas / admin, dan pindahkan penyimpanan ke object storage (S3/dsb).

### E. Tidak ada pencabutan token / rate limiting

Token JWT yang sudah diterbitkan tetap valid sampai kedaluwarsa meskipun
password diganti atau akun ditolak setelah login (akun `DITOLAK` baru
ditolak saat login berikutnya). Tidak ada rate limiting pada
`/api/auth/login` maupun endpoint lain.

### F. FCM

Lihat `docs/FCM_SETUP.md`. Service-account JSON adalah kredensial
server: simpan di luar repo dengan `chmod 600`, jangan pernah commit.
