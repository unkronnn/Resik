-- All-features batch (backend): account verification, proof photos,
-- FCM device tokens, pickup ratings.
--
-- Matches the JPA entities exactly (ddl-auto: validate):
--   User.statusVerifikasi / alasanPenolakan
--   Penjemputan.fotoBuktiUrl
--   DeviceToken, Rating
--
-- Existing accounts are grandfathered to DISETUJUI: the verification
-- field did not exist before this migration, so every account created
-- earlier is treated as already approved. New rows default to MENUNGGU
-- (entity default + column default below).

CREATE TYPE status_verifikasi AS ENUM ('MENUNGGU', 'DISETUJUI', 'DITOLAK');

ALTER TABLE users
    ADD COLUMN status_verifikasi status_verifikasi NOT NULL DEFAULT 'MENUNGGU',
    ADD COLUMN alasan_penolakan TEXT;

UPDATE users SET status_verifikasi = 'DISETUJUI';

ALTER TABLE penjemputan
    ADD COLUMN foto_bukti_url VARCHAR(500);

CREATE TABLE device_token (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    token VARCHAR(500) NOT NULL UNIQUE,
    platform VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_device_token_user ON device_token(user_id);

CREATE TABLE rating (
    id BIGSERIAL PRIMARY KEY,
    penjemputan_id BIGINT NOT NULL UNIQUE REFERENCES penjemputan(id),
    pemberi_rating_id BIGINT NOT NULL REFERENCES users(id),
    petugas_id BIGINT NOT NULL REFERENCES users(id),
    nilai INT NOT NULL CHECK (nilai BETWEEN 1 AND 5),
    komentar TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_rating_petugas ON rating(petugas_id);
