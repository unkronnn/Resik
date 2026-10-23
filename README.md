<div align="center">

<img src="scripts/assets/Resik.png" width="120" height="120" alt="Resik Logo">

# Resik — Bank Sampah Digital

### Platform digital pengelolaan sampah yang menghubungkan masyarakat dengan bank sampah

[![Release](https://img.shields.io/github/v/release/unkronnn/resik?style=for-the-badge&label=Release)](https://github.com/unkronnn/resik/releases)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Windows%20%7C%20macOS-blue?style=for-the-badge)](#download)

</div>

---

## Tentang Resik

**Resik** (Resik = Bersih) adalah aplikasi Bank Sampah Digital yang mengatasi minimnya edukasi pemilahan sampah dan memberi nilai ekonomis dari limbah yang dikelola. Aplikasi ini menghubungkan masyarakat sebagai penyetor sampah dengan pengelola bank sampah sebagai verifikator.

### Target Pengguna
- **Masyarakat Umum** — sebagai penyetor sampah yang mendapatkan pembayaran
- **Pengelola/Petugas Bank Sampah** — sebagai verifikator dan pengelola transaksi

---

## Fitur Utama

### Aplikasi Android (Client)
| Fitur | Deskripsi |
|-------|-----------|
| Registrasi & Login | Pendaftaran akun, login, profil, role-based access |
| Penjemputan Sampah | Pilih kategori (Organik/Anorganik/B3/Residu), estimasi berat, tanggal, waktu, alamat GPS |
| Riwayat Setoran | Histori transaksi dengan status real-time |
| Dompet Digital | Total saldo dan riwayat transaksi pembayaran |
| Edukasi Sampah | Panduan kategori dan harga per kg |
| Leaderboard | Peringkat kontribusi pengguna |
| Chat Admin | Komunikasi langsung dengan admin via VPS |
| Lokasi Bank Sampah | Denah lokasi bank sampah terdekat |
| Scan QR Code | Pemindaian kode QR untuk verifikasi |
| Profil Lengkap | Upload foto, edit data (nama, email, no. telp, alamat) |

### Aplikasi Desktop (Admin)
| Fitur | Deskripsi |
|-------|-----------|
| Dashboard | Statistik real-time dan akses cepat |
| Manajemen Penjemputan | Form dengan kategori & estimasi berat, assign petugas, update status |
| Penimbangan | Timbang → Kalkulasi harga → Konfirmasi bayar → Transfer dompet → Selesai |
| Data Sampah | CRUD jenis sampah dan harga |
| Edukasi Sampah | 12 jenis sampah dengan 4 kategori filter |
| Chat Client | Komunikasi dengan client via VPS |
| Lokasi Bank Sampah | Kelola lokasi bank sampah |
| Dompet Digital | Monitoring saldo pengguna |
| Leaderboard | Peringkat kontribusi |
| Laporan Harian | Statistik per hari dengan date picker |
| Riwayat Setoran | Histori semua transaksi |
| Manajemen User | CRUD data pengguna |
| Profil | Edit informasi akun admin |

---

## Tech Stack

| Komponen | Teknologi |
|----------|-----------|
| Backend API | Spring Boot 3.2.5 + PostgreSQL 16 + Flyway + JWT |
| Android Client | Kotlin + Jetpack Compose + Material 3 + Material Icons |
| Desktop Admin | Java + JavaFX 21 + Jackson |
| Deployment | Docker Compose pada VPS (43.157.243.87:8081) |
| Build Tools | Maven (Desktop), Gradle (Android), NSIS (Windows Installer) |

---

## Arsitektur

```
┌─────────────────┐     ┌─────────────────┐
│  Android Client  │     │  Desktop Admin  │
│  (Kotlin/Compose)│     │  (Java/JavaFX)   │
└────────┬────────┘     └────────┬─────────┘
         │                       │
         │      HTTP / REST      │
         │   (JSON + JWT Auth)   │
         ▼                       ▼
┌─────────────────────────────────────┐
│        Backend API (Spring Boot)    │
│  Port: 8081  │  VPS: 43.157.243.87  │
└──────────────────┬──────────────────┘
                   │
                   ▼
┌─────────────────────────────────────┐
│       PostgreSQL 16 (Docker)         │
│       Port: 5433                     │
└─────────────────────────────────────┘
```

---

## Struktur Project

```
resik/
├── backend-api/          # Spring Boot REST API + PostgreSQL
├── desktop-admin/        # JavaFX Desktop App (Admin/Petugas)
├── android-client/       # Android App (Kotlin + Compose)
├── docs/                 # PRD & dokumentasi presentasi
├── scripts/              # Build & deploy scripts
├── .github/workflows/    # CI/CD (auto-deploy ke VPS)
├── docker-compose.yml    # VPS deployment config
├── deploy.sh             # VPS deployment script
└── README.md
```

---

## Download

| Platform | File | Kebutuhan |
|----------|------|-----------|
| Android | `Resik-Android-v3.1.apk` | Android 7.0+ (API 24) |
| macOS | `Resik-Admin-macOS-v3.1.dmg` | macOS 11+ (Apple Silicon) |
| Windows | `Resik-Admin-Setup.exe` | Windows 10/11 (x64) |

> **Catatan:** Windows installer sudah bundle JRE 17 + JavaFX 21, tidak perlu install Java.

---

## Akun Demo (Production VPS)

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin2@resik.id` | `admin123` |
| Masyarakat | `test@resik.id` | `test123` |

---

## Alur Penggunaan

### Client (Android)
1. **Registrasi/Login** — Buat akun atau login
2. **Pilih Kategori Sampah** — Organik, Anorganik, B3, atau Residu
3. **Estimasi Berat & Isi Form** — Pilih tanggal, waktu, dan alamat penjemputan (GPS)
4. **Tunggu Penjemputan** — Petugas datang sesuai jadwal
5. **Penimbangan** — Petugas menimbang berat aktual
6. **Verifikasi & Pembayaran** — Sistem kalkulasi harga, transfer ke dompet digital
7. **Riwayat** — Lihat histori transaksi di menu Riwayat

### Admin (Desktop)
1. **Login** — Masuk dengan akun admin
2. **Dashboard** — Pantau statistik
3. **Penjemputan** — Assign petugas, update status proses
4. **Penimbangan** — Input berat aktual → kalkulasi → bayar → selesai
5. **Laporan** — Cetak laporan harian
6. **Chat** — Komunikasi dengan client

---

## Build dari Source

### Backend API
```bash
cd backend-api
./mvnw clean package -DskipTests
docker compose up -d
```

### Android APK
```bash
cd android-client
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### Desktop (macOS DMG)
```bash
cd desktop-admin
mvn package -DskipTests
jpackage --name "Resik Admin" --app-version 3.0.0 --input target \
  --main-jar resik-desktop-1.0.0.jar --main-class com.resik.admin.Main \
  --module-path lib/javafx-mac/javafx-sdk-21.0.5/lib \
  --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base \
  --type dmg --dest target/
```

---

## Deployment ke VPS

```bash
# Di VPS (Linux x86_64)
git clone https://github.com/unkronnn/resik.git /opt/resik
cd /opt/resik
bash deploy.sh
```

Backend akan jalan di `http://43.157.243.87:8081/api`

---

## Tim Pengembang

| Nama | Role |
|------|------|
| Syukron Maulana | Full-Stack Developer |

---

## License

MIT License — bebas digunakan untuk keperluan edukasi dan non-komersial.

---

<div align="center">

**Resik — Bersihkan Bumi, Mulai dari Sampahmu**

</div>
