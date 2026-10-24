<div align="center">

<img src="assets/Resik.png" width="140" height="140" alt="Resik Logo">

# Resik — Bank Sampah Digital

**Platform digital pengelolaan sampah yang menghubungkan masyarakat dengan bank sampah**

[![Release](https://img.shields.io/github/v/release/unkronnn/resik?style=for-the-badge&label=Latest%20Release)](https://github.com/unkronnn/resik/releases)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Windows%20%7C%20macOS-blue?style=for-the-badge)](#-download)
[![Backend](https://img.shields.io/badge/Backend-Spring%20Boot%203.2.5-6DB33F?style=for-the-badge)](#-tech-stack)
[![Database](https://img.shields.io/badge/Database-PostgreSQL%2016-336791?style=for-the-badge)](#-tech-stack)

</div>

---

## Tentang Resik

**Resik** (Resik = Bersih) adalah aplikasi **Bank Sampah Digital** yang dibangun untuk mengatasi dua permasalahan utama dalam pengelolaan sampah di Indonesia:

1. **Minimnya edukasi pemilahan sampah** — masyarakat belum memahami kategori sampah dan nilainya.
2. **Tidak adanya platform terpusat** — belum ada sistem yang menghubungkan masyarakat dengan bank sampah secara digital.

Resik menghubungkan masyarakat sebagai penyetor sampah dengan pengelola bank sampah sebagai verifikator, sehingga seluruh proses — dari penjemputan, penimbangan, hingga pembayaran — tercatat secara digital dan transparan.

### Tiga Pilar Resik
| Pilar | Deskripsi |
|-------|-----------|
| **Edukasi** | Panduan kategori sampah (Organik, Anorganik, B3, Residu) dan harga per kg |
| **Transparansi** | Riwayat transaksi real-time dan dompet digital |
| **Insentif** | Pembayaran langsung ke dompet digital dan leaderboard kontribusi |

---

## Fitur Utama

### Aplikasi Mobile (Android)

| Fitur | Deskripsi |
|-------|-----------|
| Registrasi & Login | Pendaftaran akun, login, profil, role-based access |
| Penjemputan Sampah | Pilih kategori (Organik/Anorganik/B3/Residu), estimasi berat, tanggal, waktu, alamat GPS |
| Riwayat Setoran | Histori transaksi dengan status real-time |
| Dompet Digital | Total saldo dan riwayat transaksi pembayaran |
| Edukasi Sampah | Panduan kategori dan harga per kg |
| Leaderboard | Peringkat kontribusi pengguna |
| Chat Admin | Komunikasi langsung dengan admin via WebSocket |
| Lokasi Bank Sampah | Denah lokasi bank sampah terdekat |
| Scan QR Code | Pemindaian kode QR untuk verifikasi |
| Profil Lengkap | Upload foto, edit data (nama, email, no. telp, alamat) |

### Aplikasi Desktop (Admin/Petugas)

| Fitur | Deskripsi |
|-------|-----------|
| Dashboard | Statistik real-time: total penjemputan, sampah, transaksi, pengguna aktif |
| Manajemen Penjemputan | Form dengan kategori & estimasi berat, assign petugas, update status |
| Penimbangan | Timbang → Kalkulasi harga → Konfirmasi bayar → Transfer dompet → Selesai |
| Data Sampah | CRUD jenis sampah dan harga per kg |
| Edukasi Sampah | 12 jenis sampah dengan 4 kategori filter |
| Chat Client | Komunikasi dengan client via WebSocket real-time |
| Lokasi Bank Sampah | Kelola lokasi bank sampah |
| Dompet Digital | Monitoring saldo semua pengguna |
| Leaderboard | Peringkat kontribusi masyarakat |
| Laporan Harian | Statistik per hari dengan date picker |
| Riwayat Setoran | Histori semua transaksi |
| Manajemen User | CRUD data pengguna |
| Profil Admin | Edit informasi akun admin |

---

## Tech Stack

| Komponen | Teknologi |
|----------|-----------|
| **Backend API** | Spring Boot 3.2.5, Java 17, PostgreSQL 16, Flyway, JWT (jjwt 0.12.5), BCrypt |
| **Android Client** | Kotlin, Jetpack Compose, Material 3, MVVM, Coroutines |
| **Desktop Admin** | Java 17, JavaFX 21, Jackson, WebSocket Client |
| **Deployment** | Docker Compose pada VPS (43.157.243.87:8081) |
| **Build Tools** | Maven (Backend + Desktop), Gradle (Android), NSIS (Windows Installer), jpackage (macOS DMG) |
| **CI/CD** | GitHub Actions (auto-deploy ke VPS on push) |

---

## Arsitektur Sistem

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
│        Backend API (Spring Boot)     │
│  Port: 8081  │  VPS: 43.157.243.87  │
└──────────────────┬──────────────────┘
                   │
                   ▼
┌─────────────────────────────────────┐
│       PostgreSQL 16 (Docker)         │
│       Port: 5433                     │
└─────────────────────────────────────┘
```

### Penerapan OOP

Aplikasi ini menerapkan konsep Pemrograman Berorientasi Objek secara menyeluruh:

- **Inheritance** — Kelas `User` (abstract) dengan strategi `SINGLE_TABLE`, tiga subclass: `Admin`, `Petugas`, `Masyarakat`
- **Encapsulation** — Semua entitas menggunakan modifier `private` dengan getter/setter publik
- **Polymorphism** — Method `getRole()` di-override oleh setiap subclass, enum untuk status
- **Composition & Association** — Relasi `@OneToMany`, `@OneToOne`, `@ManyToOne` antar entitas

---

## Struktur Project

```
resik/
├── backend-api/              # Spring Boot REST API + PostgreSQL
├── desktop-admin/            # JavaFX Desktop App (Admin/Petugas)
├── android-client/           # Android App (Kotlin + Compose)
├── docs/
│   ├── Laporan-Resik-PBO.pdf         # User Manual (laporan PBO)
│   ├── laporan-resik.html            # HTML source laporan
│   ├── PRD-Bank-Sampah-Digital.md   # Product Requirements Document
│   ├── android-preview.html          # Preview UI Android
│   ├── presentasi/                   # Slide presentasi (HTML + PDF)
│   ├── ui-ux/                        # Desain UI/UX (Figma exports)
│   ├── screenshots-android/          # Screenshot aplikasi Android
│   └── screenshots-desktop/          # Screenshot aplikasi Desktop
├── assets/                   # Logo & gambar (Resik, UHB)
├── scripts/                  # Build & deploy scripts
├── .github/workflows/        # CI/CD (auto-deploy ke VPS)
├── docker-compose.yml        # VPS deployment config
├── deploy.sh                 # VPS deployment script
└── README.md
```

---

## Download

| Platform | File | Kebutuhan Sistem |
|----------|------|-------------------|
| **Android** | `Resik-Android-v3.6.apk` | Android 7.0+ (API 24) |
| **macOS** | `Resik-Admin-macOS-v3.6.dmg` | macOS 11+ (Apple Silicon / Intel) |
| **Windows** | `Resik-Admin-Setup-v3.6.exe` | Windows 10/11 (x64), installer bundle JRE 17 + JavaFX 21 |
| **Cross-platform** | `Resik-Admin-v3.6.jar` | Java 17+ + JavaFX 21 |
| **Backend** | `Resik-API-v3.6.jar` | Java 17+, PostgreSQL 16, Docker |

> Download semua release assets di [GitHub Releases](https://github.com/unkronnn/resik/releases).

---

## Akun Demo (Production VPS)

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin2@resik.id` | `admin123` |
| Masyarakat | `test@resik.id` | `test123` |

---

## Alur Penggunaan

### Masyarakat (Android)

1. **Registrasi/Login** — Buat akun atau login dengan akun demo
2. **Pilih Kategori Sampah** — Organik, Anorganik, B3, atau Residu
3. **Isi Form Penjemputan** — Estimasi berat, tanggal, waktu, alamat (GPS otomatis)
4. **Tunggu Penjemputan** — Petugas dijadwalkan dan datang sesuai jadwal
5. **Penimbangan** — Petugas menimbang berat aktual sampah
6. **Verifikasi & Pembayaran** — Sistem kalkulasi harga (berat × harga/kg), transfer ke dompet digital
7. **Riwayat** — Lihat histori transaksi di menu Riwayat

### Admin (Desktop)

1. **Login** — Masuk dengan akun admin
2. **Dashboard** — Pantau statistik operasional
3. **Penjemputan** — Assign petugas ke penjemputan, update status
4. **Penimbangan** — Input berat aktual → kalkulasi harga → konfirmasi bayar → selesai
5. **Laporan** — Cetak laporan harian dengan filter tanggal
6. **Chat** — Komunikasi real-time dengan client

### State Machine Penjemputan

```
MENUNGGU → DIJADWALKAN → DIPROSES → SELESAI
                                    ↘ DIBATALKAN
```

---

## Build dari Source

### Backend API
```bash
cd backend-api
./mvnw clean package -DskipTests
# Output: target/resik-api-1.0.0.jar
```

### Android APK
```bash
cd android-client
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### Desktop Admin (JAR)
```bash
cd desktop-admin
mvn package -DskipTests
# Output: target/resik-desktop-1.0.0.jar
```

### Desktop Admin (macOS DMG)
```bash
cd desktop-admin
mvn package -DskipTests
jpackage --name "Resik Admin" --app-version 3.6.0 --input target \
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

Backend akan berjalan di `http://43.157.243.87:8081/api`

---

## Tim Pengembang

| Nama | NIM | Peran |
|------|-----|-------|
| Syukron Maulana | 250111032 | Ketua Kelompok — Full Stack Developer (Backend, Android, Desktop, Deployment) |
| Muhamad Dafa Nuruzzufar Al Mubarok | 250111018 | Anggota — Dokumentasi & Penyusunan Laporan |
| Riri Syafala Ghofaro Jamal | 250111027 | Anggota — Review Dokumentasi & Pengujian Aplikasi |
| Zain Ghathfaan Kiesa | 250111035 | Anggota — Quality Assurance & Presentasi |

**Kontak:** s.ssyukronmaulana@gmail.com

---

## License

MIT License — bebas digunakan untuk keperluan edukasi dan non-komersial.

---

<div align="center">

**Resik — Bersihkan Bumi, Mulai dari Sampahmu**

</div>
