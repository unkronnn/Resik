package com.resik.client.model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    @SerializedName("detailList")
    private List<Detail> detailList;

    public Long getId() { return id; }
    public String getAlamat() { return alamat; }
    public String getTanggalJemput() { return tanggalJemput; }
    public String getWaktuJemput() { return waktuJemput; }
    public String getStatus() { return status; }
    public String getQrCode() { return qrCode; }
    public List<Detail> getDetailList() { return detailList; }

    public static class Detail {
        @SerializedName("id")
        private Long id;
        @SerializedName("estimasiBerat")
        private BigDecimal estimasiBerat;
        @SerializedName("beratAktual")
        private BigDecimal beratAktual;
        @SerializedName("subtotal")
        private BigDecimal subtotal;
    }
}
