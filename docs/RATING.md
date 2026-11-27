# Rating Bank Sampah — pilihan desain

## Apa yang dinilai: petugas yang melakukan penjemputan

Satu rating menilai **petugas** (`User` ber-role `PETUGAS`) yang
menjemputkan sampah pada satu penjemputan, bukan lokasi bank sampah
(`LokasiBankSampah`) dan bukan bank sampah sebagai institusi. Pemberi
rating adalah masyarakat pemilik penjemputan itu.

## Kenapa bukan lokasi bank sampah

`Penjemputan` tidak punya relasi apa pun ke `LokasiBankSampah`: tidak ada
kolom `lokasi_id` di tabel `penjemputan`, tidak ada field lokasi di
entitas, dan alur penjemputan (buat → tugaskan petugas → timbang) tidak
pernah memilih lokasi. Menilai "bank sampah" dari sebuah penjemputan
tidak mungkin dilakukan dengan data yang ada tanpa mengarang kaitan
penjemputan ↔ lokasi. Satu-satunya aktor yang terikat langsung pada
penjemputan dan bisa dinilai secara jujur adalah `penjemputan.petugas`.

Konsekuensinya:

- Rating menjawab pertanyaan "bagaimana kinerja petugas ini?", yang
  memang dibutuhkan untuk memilah petugas dan untuk layar profil
  petugas di desktop.
- Bila nanti penjemputan dikaitkan ke lokasi bank sampah (mis. kolom
  `lokasi_id` + pemilihan lokasi saat membuat penjemputan), rating per
  lokasi bisa ditambahkan sebagai entitas terpisah tanpa mengubah
  entitas ini — `Rating` yang sekarang tetap valid sebagai rating
  petugas.
- Nama endpoint tetap `/api/rating` (bukan `/api/rating/lokasi`) supaya
  tidak mengklaim sesuatu yang tidak diukur.

## Aturan

| Aturan | Alasan |
|---|---|
| Hanya pemilik penjemputan yang boleh menilai | Mencegah rating palsu dari pengguna lain |
| Penjemputan harus berstatus `SELESAI` | Yang dinilai adalah layanan yang sudah benar-benar terjadi, bukan janji jemput |
| Petugas diambil dari `penjemputan.petugas` di server, bukan dari body request | Body tidak membawa `petugasId`, jadi caller tidak bisa menilai user sembarang |
| Satu rating per penjemputan (unique constraint + cek `exists`) | Mencegah satu layanan dinilai berulang kali; tidak ada edit rating — bila salah, admin bisa hapus barisnya langsung di DB |
| `nilai` integer 1–5, `komentar` opsional | Skala bintang standar; komentar bebas |
| Penjemputan tanpa petugas tidak bisa dinilai | Tidak ada subjek rating |

## Endpoint

- `POST /api/rating` — body `{"penjemputanId": 12, "nilai": 5, "komentar": "..."}`.
  Mengembalikan `201` + rating yang dibuat. `409` bila penjemputan itu
  sudah pernah dinilai, `400` bila belum `SELESAI` atau `nilai` di luar
  1–5, `403` bila pemberi bukan pemilik.
- `GET /api/rating/petugas/{petugasId}` — `{"petugasId": 3, "rataRata": 4.5, "jumlah": 12}`.
  `rataRata` dibulatkan ke 1 desimal; `0.0` dan `jumlah: 0` bila petugas
  belum punya rating.
- `GET /api/rating/penjemputan/{id}` — rating untuk penjemputan itu, atau
  `404` bila belum ada.

## Skema

```sql
CREATE TABLE rating (
    id BIGSERIAL PRIMARY KEY,
    penjemputan_id BIGINT NOT NULL UNIQUE REFERENCES penjemputan(id),
    pemberi_rating_id BIGINT NOT NULL REFERENCES users(id),
    petugas_id BIGINT NOT NULL REFERENCES users(id),
    nilai INT NOT NULL CHECK (nilai BETWEEN 1 AND 5),
    komentar TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

(Migrasi `backend-api/src/main/resources/db/migration/V3__all_features.sql`.)
