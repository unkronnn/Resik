package com.resik.admin.service;

import com.resik.admin.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Data dummy untuk demo offline tanpa backend.
 */
public class DummyData {

    public static List<Sampah> getSampahList() {
        List<Sampah> list = new ArrayList<>();
        list.add(createSampah(1L, "ORGANIK", "Sampah Makanan", new BigDecimal("1000"), "Sisa makanan dan sisa dapur", true));
        list.add(createSampah(2L, "ORGANIK", "Daun & Rumput Kering", new BigDecimal("500"), "Daun kering, rumput, dan tanaman", true));
        list.add(createSampah(3L, "ANORGANIK", "Plastik Botol", new BigDecimal("2500"), "Botol plastik bekas minuman", true));
        list.add(createSampah(4L, "ANORGANIK", "Kardus/Karton", new BigDecimal("1500"), "Kardus dan karton bekas", true));
        list.add(createSampah(5L, "ANORGANIK", "Kaleng Aluminium", new BigDecimal("5000"), "Kaleng minuman ringan", true));
        list.add(createSampah(6L, "ANORGANIK", "Besi/Logam", new BigDecimal("3000"), "Besi dan logam bekas", true));
        list.add(createSampah(7L, "ANORGANIK", "Kertas", new BigDecimal("1500"), "Kertas bekas dan koran", true));
        list.add(createSampah(8L, "ANORGANIK", "Botol Kaca", new BigDecimal("2000"), "Botol kaca bening dan berwarna", true));
        list.add(createSampah(9L, "B3", "Baterai Bekas", new BigDecimal("3000"), "Baterai bekas dan aki", true));
        list.add(createSampah(10L, "B3", "Elektronik Bekas", new BigDecimal("5000"), "Komponen elektronik rusak", true));
        return list;
    }

    private static Sampah createSampah(Long id, String kat, String nama, BigDecimal harga, String desc, Boolean active) {
        Sampah s = new Sampah();
        s.setId(id); s.setKategori(kat); s.setNama(nama);
        s.setHargaPerKg(harga); s.setDeskripsi(desc); s.setIsActive(active);
        return s;
    }

    public static List<Penjemputan> getPenjemputanList() {
        List<Penjemputan> list = new ArrayList<>();

        list.add(createPenjemputan(1L, "Jl. Merdeka No. 45, Bandung", LocalDate.of(2026, 10, 22), "09:00-11:00", "MENUNGGU", "RESIK-A1B2C3D4", "Sampah rumah tangga campuran"));
        list.add(createPenjemputan(2L, "Jl. Sudirman No. 12, Bandung", LocalDate.of(2026, 10, 22), "13:00-15:00", "DIJADWALKAN", "RESIK-E5F6G7H8", "Botol plastik dan kardus"));
        list.add(createPenjemputan(3L, "Jl. Asia Afrika No. 88, Bandung", LocalDate.of(2026, 10, 21), "09:00-11:00", "DIPROSES", "RESIK-I9J0K1L2", "Menunggu timbangan"));
        list.add(createPenjemputan(4L, "Jl. Dipatiukur No. 7, Bandung", LocalDate.of(2026, 10, 20), "10:00-12:00", "SELESAI", "RESIK-M3N4O5P6", "Selesai dibayar Rp 15.500"));
        list.add(createPenjemputan(5L, "Jl. Cihampelas No. 100, Bandung", LocalDate.of(2026, 10, 20), "14:00-16:00", "SELESAI", "RESIK-Q7R8S9T0", "Selesai dibayar Rp 8.000"));
        list.add(createPenjemputan(6L, "Jl. Dago No. 25, Bandung", LocalDate.of(2026, 10, 19), "09:00-11:00", "SELESAI", "RESIK-U1V2W3X4", "Selesai dibayar Rp 22.500"));
        list.add(createPenjemputan(7L, "Jl. Riau No. 50, Bandung", LocalDate.of(2026, 10, 18), "13:00-15:00", "DIBATALKAN", "RESIK-Y5Z6A7B8", "Dibatalkan oleh user"));
        list.add(createPenjemputan(8L, "Jl. Ahmad Yani No. 33, Bandung", LocalDate.of(2026, 10, 23), "09:00-11:00", "MENUNGGU", "RESIK-C9D0E1F2", "Sampah organik"));

        return list;
    }

    @SuppressWarnings("unchecked")
    private static Penjemputan createPenjemputan(Long id, String alamat, LocalDate tgl, String waktu, String status, String qr, String catatan) {
        Penjemputan p = new Penjemputan();
        p.setId(id); p.setAlamat(alamat); p.setTanggalJemput(tgl);
        p.setWaktuJemput(waktu); p.setStatus(status); p.setQrCode(qr); p.setCatatan(catatan);

        List<Penjemputan.DetailPenjemputan> details = new ArrayList<>();
        Penjemputan.DetailPenjemputan d1 = new Penjemputan.DetailPenjemputan();
        d1.setId(id * 10);
        d1.setEstimasiBerat(new BigDecimal("2.5"));
        d1.setBeratAktual(status.equals("SELESAI") ? new BigDecimal("2.3") : null);
        d1.setSubtotal(status.equals("SELESAI") ? new BigDecimal("5750") : null);
        details.add(d1);

        if (status.equals("SELESAI")) {
            Penjemputan.Transaksi t = new Penjemputan.Transaksi();
            t.setId(id);
            t.setTotalHarga(new BigDecimal("5750"));
            t.setStatus("BERHASIL");
            p.setTransaksi(t);
        }

        p.setDetailList(details);
        return p;
    }

    public static Map<String, Integer> getStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("MENUNGGU", 2);
        stats.put("DIJADWALKAN", 1);
        stats.put("DIPROSES", 1);
        stats.put("SELESAI", 3);
        stats.put("DIBATALKAN", 1);
        stats.put("TOTAL_USER", 47);
        return stats;
    }
}
