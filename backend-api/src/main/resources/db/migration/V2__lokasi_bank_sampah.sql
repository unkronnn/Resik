-- Lokasi Bank Sampah table
CREATE TABLE IF NOT EXISTS lokasi_bank_sampah (
    id BIGSERIAL PRIMARY KEY,
    nama VARCHAR(255) NOT NULL,
    alamat VARCHAR(500) NOT NULL,
    jam_operasional VARCHAR(100),
    telepon VARCHAR(50),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Seed data
INSERT INTO lokasi_bank_sampah (nama, alamat, jam_operasional, telepon, latitude, longitude) VALUES
('Bank Sampah Resik Pusat', 'Jl. Merdeka No. 45, Jakarta Pusat', '08:00-17:00', '0812-3456-7890', -6.1950, 106.8230),
('Bank Sampah Resik Selatan', 'Jl. Margonda Raya No. 10, Depok', '07:00-16:00', '0813-9876-5432', -6.3720, 106.8320),
('Bank Sampah Resik Timur', 'Jl. Raya Bekasi KM 20, Bekasi', '08:00-16:00', '0815-1111-2222', -6.2340, 106.9750),
('Bank Sampah Resik Barat', 'Jl. Daan Mogot No. 88, Tangerang', '09:00-17:00', '0817-3333-4444', -6.1710, 106.6430);
