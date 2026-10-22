package com.resik.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PenjemputanDtos {
    public record CreateRequest(
        LocalDate tanggalJemput,
        String waktuJemput,
        String alamat,
        String catatan,
        BigDecimal latitude,
        BigDecimal longitude,
        List<DetailRequest> details
    ) {}

    public record DetailRequest(
        Long sampahId,
        BigDecimal estimasiBerat
    ) {}

    public record TimbangRequest(
        Long penjemputanId,
        List<TimbangDetail> details
    ) {}

    public record TimbangDetail(
        Long detailId,
        BigDecimal beratAktual
    ) {}
}
