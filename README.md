# RESIK — Bank Sampah Digital (Monorepo)

> **RESIK** (Resik = Bersih) adalah aplikasi Bank Sampah Digital yang menghubungkan masyarakat (penyetor sampah) dengan pengelola bank sampah (petugas/admin).

## 📁 Struktur Project

```
resik/
├── backend-api/          # Spring Boot REST API + PostgreSQL
├── desktop-admin/        # JavaFX Desktop App (Admin/Petugas)
├── android-client/       # Android App (Java) untuk Masyarakat
├── docs/                 # PRD & dokumentasi
├── scripts/              # Script presentasi
├── Dockerfile            # Docker build untuk Railway deploy
├── railway.json          # Railway deployment config
└── README.md
```

## 🛠 Tech Stack

| Komponen | Technology |
|----------|-----------|
| Backend API | Java Spring Boot 3.2 + PostgreSQL |
| Auth | JWT (JSON Web Token) |
| Database Migration | Flyway |
| Desktop Admin | JavaFX 21 |
| Android Client | Java + Retrofit + Material Design |
| Deploy | Railway (auto-deploy on push) |
| Container | Docker |

## ✨ Fitur

### Backend API
- ✅ JWT Auth (Register & Login)
- ✅ Manajemen Sampah (CRUD)
- ✅ Penjadwalan Penjemputan
- ✅ Sistem Penimbangan & Kalkulasi Harga
- ✅ Dompet Digital & Pembayaran Otomatis
- ✅ Notifikasi
- ✅ Chat Admin-User
- ✅ Role-based Access (Masyarakat, Petugas, Admin)

### Desktop Admin (JavaFX)
- ✅ Login Petugas/Admin
- ✅ Dashboard Operasional (stats cards)
- ✅ Manajemen Penjemputan (table view)
- ✅ Penimbangan Sampah
- ✅ Manajemen Data Sampah
- ✅ Laporan Transaksi (Export PDF/Excel)
- ✅ Chat dengan Masyarakat

### Android Client (Java)
- ✅ Registrasi & Login
- ✅ Dashboard (saldo dompet, poin reward)
- ✅ Edukasi Pemilahan Sampah
- ✅ Penjemputan Saya (list)
- ✅ Riwayat Transaksi
- ✅ Chat dengan Admin
- ✅ Peta Lokasi Bank Sampah
- ✅ QR Code Scanner

## 🚀 Deploy ke Railway

Backend API auto-deploy saat push ke `main` branch.

**Environment Variables di Railway:**
```
DATABASE_URL=postgresql://...
DATABASE_USER=postgres
DATABASE_PASSWORD=...
JWT_SECRET=your-256-bit-secret
```

## 👥 Tim

| Anggota | NIM | Tugas |
|---------|-----|-------|
| Syukron Maulana | 250111032 | Arsitektur & Backend Core |
| Zain Ghathfaan Kiesa | 250111035 | Fitur Transaksi & Dompet |
| Riri Syafala Ghofaro Jamal | 250111027 | UI/UX & Edukasi |

## 📜 Lisensi

Project untuk mata kuliah PBO — UTS & UAS.
