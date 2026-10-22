# Product Requirements Document (PRD)
## Bank Sampah Digital — RESIK

---

## 1. Informasi Umum

| Field | Keterangan |
|-------|-----------|
| **Nama Proyek** | RESIK — Bank Sampah Digital |
| **Platform** | Desktop (petugas) & Mobile (pengguna) |
| **Bahasa Pemrograman** | Java (Apache NetBeans) |
| **Paradigma** | Object-Oriented Programming (OOP) |
| **Tahap** | UTS — Prototype & Rencana |
| **Target Penyelesaian** | UAS — Aplikasi Lengkap |

---

## 2. Latar Belakang

Sampah merupakan masalah yang terus berkepanjangan di Indonesia. Berdasarkan data KLHK, Indonesia menghasilkan jutaan ton sampah setiap tahun, namun masih banyak masyarakat yang belum memahami cara pemilahan sampah yang benar. Masalah utama yang ditemukan:

1. **Minimnya Edukasi Pemilahan Sampah** — Masyarakat sering tidak mengetahui jenis sampah yang dapat didaur ulang, dijual, atau dibuang, sehingga pencampuran sampah organik dan anorganik masih terjadi.
2. **Belum Adanya Nilai Ekonomis dari Sampah** — Banyak masyarakat tidak menyadari bahwa sampah yang dipilah dengan baik memiliki nilai jual dan dapat menjadi sumber penghasilan tambahan.
3. **Proses Penyetoran yang Tidak Terstruktur** — Masyarakat yang ingin menyetor sampah sering bingung ke mana harus menyetor, bagaimana prosesnya, dan berapa nilai yang akan didapat.
4. **Kurangnya Transparansi dan Tracking** — Tidak ada sistem yang memungkinkan masyarakat melacak riwayat setoran sampah mereka beserta pendapatan yang diperoleh.

---

## 3. Alasan Proyek Perlu Dikembangkan

Proyek **RESIK (Bank Sampah Digital)** perlu dikembangkan karena:

- **Mendigitalisasi proses bank sampah** yang selama ini berjalan manual dan tidak terstruktur.
- **Memberikan edukasi pemilahan sampah** secara langsung kepada masyarakat melalui aplikasi.
- **Memberikan nilai ekonomis** dari limbah yang dikelola dengan sistem penimbangan dan pembayaran yang transparan.
- **Meningkatkan partisipasi masyarakat** dalam pengelolaan sampah melalui sistem reward yang jelas dan mudah diakses.
- **Memberikan tools bagi pengelola bank sampah** untuk mengelola operasional secara efisien dan terdokumentasi.

---

## 4. Tujuan Pengembangan Aplikasi

1. Menyediakan platform digital yang menghubungkan masyarakat (penyetor) dengan pengelola bank sampah (verifikator).
2. Memberikan edukasi pemilahan sampah kepada masyarakat melalui informasi jenis-jenis sampah dan nilainya.
3. Memfasilitasi proses penjadwalan penjemputan sampah dari rumah masyarakat.
4. Menyediakan sistem penimbangan dan kalkulasi harga sampah yang transparan.
5. Mengintegrasikan dompet digital untuk pembayaran hasil setoran sampah.
6. Menyediakan dashboard riwayat transaksi untuk tracking dan transparansi.
7. Mendorong masyarakat untuk aktif memilah dan menyetor sampah melalui sistem reward.

---

## 5. Target Pengguna

### 5.1 Pengguna Masyarakat (Penyetor Sampah)
- Masyarakat umum yang ingin menyetorkan sampah rumah tangga.
- Usia 17+ tahun, melek teknologi (menggunakan smartphone).
- Ingin mendapat penghasilan tambahan dari sampah.
- Ingin belajar memilah sampah dengan benar.

### 5.2 Pengelola Bank Sampah (Verifikator/Petugas)
- Operator dan petugas bank sampah.
- Bertanggung jawab atas verifikasi, penimbangan, dan pembayaran.
- Menggunakan aplikasi desktop untuk mengelola operasional.
- Membutuhkan tools untuk tracking dan reporting.

---

## 6. Fitur Utama

### F1. Registrasi & Manajemen Pengguna
- Pendaftaran akun masyarakat (nama, alamat, nomor telepon).
- Login & logout (masyarakat dan petugas).
- Manajemen profil pengguna.
- Role-based access (masyarakat vs petugas).

### F2. Edukasi Pemilahan Sampah
- Daftar kategori sampah (organik, anorganik, B3).
- Daftar jenis sampah dengan harga per kilogram.
- Panduan pemilahan sampah yang benar.
- Tips & edukasi daur ulang.

### F3. Penjadwalan Jemput Sampah
- Masyarakat memilih jenis & estimasi berat sampah.
- Pemilihan jadwal penjemputan (tanggal & waktu).
- Pemilihan alamat penjemputan.
- Status penjemputan (menunggu, dijadwalkan, selesai, dibatalkan).
- Notifikasi status penjemputan.

### F4. Sistem Penimbangan
- Petugas melakukan penimbangan sampah saat jemput.
- Input berat aktual per jenis sampah.
- Kalkulasi otomatis total harga berdasarkan harga per kg.
- Verifikasi hasil timbangan oleh masyarakat.

### F5. Dompet Digital & Pembayaran
- Saldo dompet digital untuk setiap masyarakat.
- Pembayaran otomatis hasil setoran ke dompet.
- Riwayat top-up dan penarikan.
- Penarikan saldo ke rekening/e-wallet.

### F6. Dashboard Riwayat Transaksi
- Riwayat setoran sampah (tanggal, jenis, berat, harga).
- Riwayat pembayaran.
- Statistik setoran (total sampah, total pendapatan).
- Grafik kontribusi lingkungan.

### F7. Manajemen Operasional (Petugas — Desktop)
- Dashboard operasional bank sampah.
- Daftar penjemputan terjadwal.
- Manajemen harga sampah per kg.
- Laporan transaksi harian/bulanan.
- Manajemen data masyarakat.

---

## 7. Alur Penggunaan Aplikasi

### 7.1 Alur Masyarakat (Mobile App)

```
Registrasi Akun → Login
    → Lihat Edukasi Sampah (opsional)
    → Buat Penjemputan
        → Pilih jenis sampah
        → Estimasi berat
        → Pilih jadwal & alamat
        → Konfirmasi penjemputan
    → Tunggu penjemputan oleh petugas
    → Petugas menimbang sampah
    → Masyarakat verifikasi hasil timbangan
    → Pembayaran otomatis ke dompet digital
    → Lihat riwayat transaksi di dashboard
    → Tarik saldo (opsional)
```

### 7.2 Alur Petugas (Desktop App)

```
Login Petugas → Dashboard Operasional
    → Lihat daftar penjemputan terjadwal
    → Pilih penjemputan untuk diproses
    → Datang ke lokasi & timbang sampah
    → Input berat aktual per jenis sampah
    → Sistem kalkulasi total harga
    → Konfirmasi pembayaran
    → Sistem transfer ke dompet masyarakat
    → Update status penjemputan = selesai
    → Lihat laporan transaksi
```

---

## 8. Rencana Penggunaan Konsep OOP

### 8.1 Class & Object

| Class | Deskripsi | Attribute Utama |
|-------|-----------|-----------------|
| `User` | Abstract class untuk pengguna | `id`, `nama`, `email`, `password`, `alamat`, `noTelp` |
| `Masyarakat` | Penyetor sampah (extends User) | `saldoDompet`, `poinReward`, `riwayatSetoran` |
| `Petugas` | Pengelola bank sampah (extends User) | `nip`, `shift`, `areaTugas` |
| `Sampah` | Representasi jenis sampah | `id`, `kategori`, `nama`, `hargaPerKg`, `deskripsi` |
| `Penjemputan` | Permintaan jemput sampah | `id`, `masyarakat`, `tanggal`, `status`, `alamat`, `daftarSampah` |
| `DetailPenjemputan` | Detail sampah per penjemputan | `penjemputan`, `sampah`, `estimasiBerat`, `beratAktual`, `subtotal` |
| `Transaksi` | Transaksi pembayaran | `id`, `penjemputan`, `totalHarga`, `tanggal`, `status` |
| `DompetDigital` | Dompet masyarakat | `id`, `masyarakat`, `saldo`, `riwayatTransaksi` |
| `Notifikasi` | Notifikasi sistem | `id`, `penerima`, `pesan`, `tanggal`, `statusBaca` |

### 8.2 Attribute & Method

**Contoh: Class `Masyarakat`**
```java
public class Masyarakat extends User {
    private double saldoDompet;
    private int poinReward;
    private List<Penjemputan> riwayatSetoran;

    // Constructor
    public Masyarakat(String nama, String email, String password, 
                      String alamat, String noTelp) { ... }

    // Method
    public Penjemputan buatPenjemputan(List<Sampah> sampah, ...) { ... }
    public void verifikasiTimbangan(Penjemputan p) { ... }
    public void tarikSaldo(double jumlah) { ... }
    public List<Transaksi> getRiwayatTransaksi() { ... }
}
```

### 8.3 Constructor

Setiap class utama akan memiliki **constructor** untuk inisialisasi object:
- `User`: constructor dengan parameter dasar (nama, email, password).
- `Sampah`: constructor dengan kategori, nama, harga per kg.
- `Penjemputan`: constructor dengan masyarakat, tanggal, alamat, daftar sampah.

### 8.4 Encapsulation

Semua attribute menggunakan **access modifier `private`** dan diakses melalui **getter & setter**:
```java
private double saldoDompet;

public double getSaldoDompet() { return saldoDompet; }
public void setSaldoDompet(double saldo) { 
    // validasi
    this.saldoDompet = saldo; 
}
```

### 8.5 Inheritance

```
User (abstract)
  ├── Masyarakat
  └── Petugas
```
- `Masyarakat` dan `Petugas` **mewarisi** attribute dan method dari `User`.
- `User` memiliki method abstract `getRole()` yang di-override oleh subclass.

### 8.6 Polymorphism

- **Method overriding**: `getRole()` di-override oleh `Masyarakat` (return "MASYARAKAT") dan `Petugas` (return "PETUGAS").
- **Method overloading**: `buatPenjemputan()` dapat menerima parameter berbeda (dengan/without estimasi berat).
```java
// Overloading
public Penjemputan buatPenjemputan(List<Sampah> sampah, LocalDate tanggal) { ... }
public Penjemputan buatPenjemputan(List<Sampah> sampah, LocalDate tanggal, String catatan) { ... }

// Overriding
@Override
public String getRole() { return "MASYARAKAT"; }
```

### 8.7 Relasi Antar Class

```
Masyarakat 1───* Penjemputan
Penjemputan 1───* DetailPenjemputan
DetailPenjemputan *───1 Sampah
Penjemputan 1───1 Transaksi
Masyarakat 1───1 DompetDigital
Petugas 1───* Penjemputan (sebagai verifikator)
```

---

## 9. Rencana Tahapan Pengembangan

### Tahap 1 — Persiapan & Desain (Pra-UTS)
- [x] Penyusunan PRD.
- [x] Desain wireframe & flow (Figma).
- [x] Rancangan struktur class & OOP.
- [ ] Setup project di Apache NetBeans.
- [ ] Rancangan database.

### Tahap 2 — Prototype (UTS)
- [ ] Implementasi class dasar (User, Masyarakat, Petugas, Sampah).
- [ ] Implementasi constructor, encapsulation, inheritance, polymorphism.
- [ ] Prototype UI registrasi & login.
- [ ] Prototype UI dashboard masyarakat (tampilan dasar).
- [ ] Prototype satu alur fungsi: registrasi → buat penjemputan.
- [ ] Demo prototype.

### Tahap 3 — Pengembangan Inti (Pasca-UTS → UAS)
- [ ] Implementasi database (MySQL/SQLite).
- [ ] Fitur penjadwalan jemput lengkap.
- [ ] Sistem penimbangan & kalkulasi harga.
- [ ] Dompet digital & pembayaran.
- [ ] Dashboard riwayat transaksi.
- [ ] Edukasi pemilahan sampah.
- [ ] Notifikasi status.

### Tahap 4 — Aplikasi Desktop Petugas
- [ ] Dashboard operasional.
- [ ] Manajemen penjemputan.
- [ ] Input penimbangan.
- [ ] Laporan transaksi.
- [ ] Manajemen harga sampah.

### Tahap 5 — Integrasi & Polish (Menuju UAS)
- [ ] Integrasi mobile & desktop.
- [ ] Fitur tambahan (lihat bagian 11).
- [ ] Testing & bug fixing.
- [ ] Finalisasi UI/UX.
- [ ] Dokumentasi & presentasi UAS.

---

## 10. Pembagian Tugas Anggota

| Anggota | Tugas Utama | Detail |
|---------|-------------|--------|
| **Syukron Maulana (250111032)** | Arsitektur & Backend Core | Rancangan class diagram, implementasi class dasar (User, Masyarakat, Petugas), inheritance & polymorphism, koneksi database |
| **Zain Ghathfaan Kiesa (250111035)** | Fitur Transaksi & Dompet | Implementasi class Penjemputan, DetailPenjemputan, Transaksi, DompetDigital, sistem penimbangan & kalkulasi harga |
| **Riri Syafala Ghofaro Jamal (250111027)** | UI/UX & Edukasi | Desain & implementasi UI (mobile & desktop), fitur edukasi sampah, dashboard riwayat, notifikasi |

---

## 11. Fitur Tambahan & Target UAS

### Fitur Tambahan yang Direncanakan:
1. **Sistem Poin Reward** — masyarakat mendapat poin setiap setoran, dapat ditukar dengan voucher.
2. **Leaderboard Kontribusi** — ranking masyarakat berdasarkan jumlah sampah yang disetorkan.
3. **Edukasi Interaktif** — kuis pemilahan sampah dengan reward poin.
4. **Notifikasi Push** — pengingat jadwal penjemputan & update status.
5. **Laporan Lingkungan** — statistik dampak lingkungan (kg sampah diselamatkan, estimasi CO2 reduction).
6. **Multi-Cabang Bank Sampah** — dukungan untuk multiple lokasi bank sampah.
7. **Export Laporan (PDF/Excel)** — laporan transaksi untuk masyarakat dan petugas.

### Hasil Akhir yang Diharapkan untuk UAS:
- Aplikasi mobile (masyarakat) dan desktop (petugas) yang **fully functional**.
- Database terintegrasi.
- Minimal 5 alur fungsi yang berjalan end-to-end.
- Dokumentasi lengkap (class diagram, ERD, user manual).
- Presentasi final & demo.

---

## 12. Referensi

- Figma Wireframe: [Mid-Fidelity Wireframe Flow RESIK](https://www.figma.com/proto/3vUsR0p8O4QV9MqWRYgC8Y/Mid-Fidelity-Wireframe-Flow-RESIK?node-id=1-2)
- Data sampah KLHK (Sistem Informasi Pengelolaan Sampah Nasional).
- Referensi OOP: Java Documentation & Apache NetBeans.

---

*Document Version: 1.0 | Last Updated: Oktober 2026*
*Proyek: RESIK — Bank Sampah Digital | Mata Kuliah: PBO | UTS*
