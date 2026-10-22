-- RESIK Database Schema - Initial Migration

CREATE TYPE user_role AS ENUM ('MASYARAKAT', 'PETUGAS', 'ADMIN');
CREATE TYPE pickup_status AS ENUM ('MENUNGGU', 'DIJADWALKAN', 'DIPROSES', 'SELESAI', 'DIBATALKAN');
CREATE TYPE transaction_status AS ENUM ('PENDING', 'BERHASIL', 'GAGAL');
CREATE TYPE notification_status AS ENUM ('TERKIRIM', 'DIBACA');

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    nama VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    alamat TEXT,
    no_telp VARCHAR(20),
    role user_role NOT NULL DEFAULT 'MASYARAKAT',
    nip VARCHAR(50),
    shift VARCHAR(50),
    area_tugas VARCHAR(255),
    saldo_dompet DECIMAL(15,2) DEFAULT 0,
    poin_reward INT DEFAULT 0,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sampah (
    id BIGSERIAL PRIMARY KEY,
    kategori VARCHAR(50) NOT NULL,
    nama VARCHAR(255) NOT NULL,
    harga_per_kg DECIMAL(10,2) NOT NULL,
    deskripsi TEXT,
    icon_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE penjemputan (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    petugas_id BIGINT REFERENCES users(id),
    tanggal_jemput DATE NOT NULL,
    waktu_jemput VARCHAR(50),
    alamat TEXT NOT NULL,
    catatan TEXT,
    status pickup_status NOT NULL DEFAULT 'MENUNGGU',
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    qr_code VARCHAR(100) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE detail_penjemputan (
    id BIGSERIAL PRIMARY KEY,
    penjemputan_id BIGINT NOT NULL REFERENCES penjemputan(id) ON DELETE CASCADE,
    sampah_id BIGINT NOT NULL REFERENCES sampah(id),
    estimasi_berat DECIMAL(10,2),
    berat_aktual DECIMAL(10,2),
    subtotal DECIMAL(15,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transaksi (
    id BIGSERIAL PRIMARY KEY,
    penjemputan_id BIGINT NOT NULL REFERENCES penjemputan(id),
    total_harga DECIMAL(15,2) NOT NULL,
    tanggal TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status transaction_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE dompet_digital (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    saldo DECIMAL(15,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE dompet_transaksi (
    id BIGSERIAL PRIMARY KEY,
    dompet_id BIGINT NOT NULL REFERENCES dompet_digital(id),
    jenis VARCHAR(20) NOT NULL,
    jumlah DECIMAL(15,2) NOT NULL,
    deskripsi VARCHAR(500),
    transaksi_id BIGINT REFERENCES transaksi(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notifikasi (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    judul VARCHAR(255) NOT NULL,
    pesan TEXT NOT NULL,
    status notification_status NOT NULL DEFAULT 'TERKIRIM',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE chat (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    petugas_id BIGINT REFERENCES users(id),
    pengirim VARCHAR(20) NOT NULL,
    pesan TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_penjemputan_user ON penjemputan(user_id);
CREATE INDEX idx_penjemputan_status ON penjemputan(status);
CREATE INDEX idx_penjemputan_petugas ON penjemputan(petugas_id);
CREATE INDEX idx_detail_penjemputan_pickup ON detail_penjemputan(penjemputan_id);
CREATE INDEX idx_notifikasi_user ON notifikasi(user_id);
CREATE INDEX idx_chat_user ON chat(user_id);

INSERT INTO users (nama, email, password, role, nip, shift, area_tugas) VALUES
('Admin RESIK', 'admin@resik.id', '$2a$10$NZ5Q3JkQ4JkQ4JkQ4JkQ4eQ4JkQ4JkQ4JkQ4JkQ4JkQ4JkQ4JkQ4', 'ADMIN', 'ADM001', 'PAGI', 'ALL');

INSERT INTO sampah (kategori, nama, harga_per_kg, deskripsi) VALUES
('ORGANIK', 'Sampah Makanan', 1000, 'Sisa makanan dan sisa dapur'),
('ORGANIK', 'Daun & Rumput Kering', 500, 'Daun kering, rumput, dan tanaman'),
('ANORGANIK', 'Plastik Botol', 2500, 'Botol plastik bekas minuman'),
('ANORGANIK', 'Kardus/Karton', 1500, 'Kardus dan karton bekas'),
('ANORGANIK', 'Kaleng Aluminium', 5000, 'Kaleng minuman ringan'),
('ANORGANIK', 'Besi/Logam', 3000, 'Besi dan logam bekas'),
('ANORGANIK', 'Kertas', 1500, 'Kertas bekas dan koran'),
('ANORGANIK', 'Botol Kaca', 2000, 'Botol kaca bening dan berwarna'),
('B3', 'Baterai Bekas', 3000, 'Baterai bekas dan aki'),
('B3', 'Elektronik Bekas', 5000, 'Komponen elektronik rusak');
