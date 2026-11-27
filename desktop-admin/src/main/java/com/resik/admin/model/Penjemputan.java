package com.resik.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Penjemputan {
    private Long id;
    private String alamat;
    private LocalDate tanggalJemput;
    private String waktuJemput;
    private String status;
    private String qrCode;
    private String catatan;
    /** Nasabah pemilik penjemputan (objek user dari backend, bila ada). */
    private User user;
    /** Petugas yang ditugaskan (objek user dari backend, bila ada). */
    private User petugas;
    /** URL foto bukti timbang, dari PUT /penjemputan/{id}/foto-bukti. */
    private String fotoBuktiUrl;
    private List<DetailPenjemputan> detailList;
    private Transaksi transaksi;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public LocalDate getTanggalJemput() { return tanggalJemput; }
    public void setTanggalJemput(LocalDate tanggalJemput) { this.tanggalJemput = tanggalJemput; }
    public String getWaktuJemput() { return waktuJemput; }
    public void setWaktuJemput(String waktuJemput) { this.waktuJemput = waktuJemput; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public User getPetugas() { return petugas; }
    public void setPetugas(User petugas) { this.petugas = petugas; }
    public String getFotoBuktiUrl() { return fotoBuktiUrl; }
    public void setFotoBuktiUrl(String fotoBuktiUrl) { this.fotoBuktiUrl = fotoBuktiUrl; }
    public List<DetailPenjemputan> getDetailList() { return detailList; }
    public void setDetailList(List<DetailPenjemputan> detailList) { this.detailList = detailList; }
    public Transaksi getTransaksi() { return transaksi; }
    public void setTransaksi(Transaksi transaksi) { this.transaksi = transaksi; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetailPenjemputan {
        private Long id;
        /**
         * Jenis sampah. Backend mengirim objek Sampah ({nama, hargaPerKg, ...}),
         * sedangkan data lama/dummy memakai String nama — karena itu field ini
         * bertipe Object dan diakses lewat getSampahNama()/getSampahHargaPerKg().
         */
        private Object sampah;
        private BigDecimal estimasiBerat;
        private BigDecimal beratAktual;
        private BigDecimal subtotal;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Object getSampah() { return sampah; }
        public void setSampah(Object sampah) { this.sampah = sampah; }

        /** Nama jenis sampah, baik dari String polos maupun objek {"nama": ...}. */
        public String getSampahNama() {
            if (sampah == null) return null;
            if (sampah instanceof String s) return s;
            if (sampah instanceof java.util.Map<?, ?> m) {
                Object nama = m.get("nama");
                return nama != null ? String.valueOf(nama) : null;
            }
            return String.valueOf(sampah);
        }

        /** Harga/kg dari objek sampah backend ({"hargaPerKg": ...}), bila ada. */
        public BigDecimal getSampahHargaPerKg() {
            if (sampah instanceof java.util.Map<?, ?> m) {
                Object h = m.get("hargaPerKg");
                if (h instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
                if (h != null) {
                    try { return new BigDecimal(String.valueOf(h)); } catch (Exception ignored) { /* fall through */ }
                }
            }
            return null;
        }
        public BigDecimal getEstimasiBerat() { return estimasiBerat; }
        public void setEstimasiBerat(BigDecimal estimasiBerat) { this.estimasiBerat = estimasiBerat; }
        public BigDecimal getBeratAktual() { return beratAktual; }
        public void setBeratAktual(BigDecimal beratAktual) { this.beratAktual = beratAktual; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Transaksi {
        private Long id;
        private BigDecimal totalHarga;
        private String status;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public BigDecimal getTotalHarga() { return totalHarga; }
        public void setTotalHarga(BigDecimal totalHarga) { this.totalHarga = totalHarga; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
