package com.resik.client.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class Penjemputan {
    @SerializedName("id")
    private Long id;
    @SerializedName("alamat")
    private String alamat;
    @SerializedName("tanggalJemput")
    private String tanggalJemput;
    @SerializedName("waktuJemput")
    private String waktuJemput;
    @SerializedName("status")
    private String status;
    @SerializedName("qrCode")
    private String qrCode;
    @SerializedName("catatan")
    private String catatan;
    @SerializedName("latitude")
    private BigDecimal latitude;
    @SerializedName("longitude")
    private BigDecimal longitude;
    /** URL relatif foto bukti timbangan, mis. "/api/penjemputan/{id}/foto". */
    @SerializedName("fotoBuktiUrl")
    private String fotoBuktiUrl;
    /** Nasabah pemilik penjemputan (nested object dari backend, bisa null). */
    @SerializedName("user")
    private UserRef user;
    /** Petugas yang menangani (nested object dari backend, bisa null). */
    @SerializedName("petugas")
    private UserRef petugas;
    @SerializedName("detailList")
    private List<Detail> detailList;

    public Long getId() { return id; }
    public String getAlamat() { return alamat; }
    public String getTanggalJemput() { return tanggalJemput; }
    public String getWaktuJemput() { return waktuJemput; }
    public String getStatus() { return status; }
    public String getQrCode() { return qrCode; }
    public String getCatatan() { return catatan; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public String getFotoBuktiUrl() { return fotoBuktiUrl; }
    public UserRef getUser() { return user; }
    public UserRef getPetugas() { return petugas; }
    public List<Detail> getDetailList() { return detailList; }

    /** Referensi user minimal (id + nama) dari objek nested backend. */
    public static class UserRef {
        @SerializedName("id")
        private Long id;
        @SerializedName("nama")
        private String nama;

        public Long getId() { return id; }
        public String getNama() { return nama; }
    }

    public static class Detail {
        @SerializedName("id")
        private Long id;
        @SerializedName("sampah")
        private SampahRef sampah;
        @SerializedName("estimasiBerat")
        private BigDecimal estimasiBerat;
        @SerializedName("beratAktual")
        private BigDecimal beratAktual;
        @SerializedName("subtotal")
        private BigDecimal subtotal;

        public Long getId() { return id; }
        public SampahRef getSampah() { return sampah; }
        public BigDecimal getEstimasiBerat() { return estimasiBerat; }
        public BigDecimal getBeratAktual() { return beratAktual; }
        public BigDecimal getSubtotal() { return subtotal; }
    }

    /** Referensi sampah minimal (id + nama + harga/kg) dari objek nested backend. */
    public static class SampahRef {
        @SerializedName("id")
        private Long id;
        @SerializedName("nama")
        private String nama;
        @SerializedName("hargaPerKg")
        private BigDecimal hargaPerKg;

        public Long getId() { return id; }
        public String getNama() { return nama; }
        public BigDecimal getHargaPerKg() { return hargaPerKg; }
    }
}
