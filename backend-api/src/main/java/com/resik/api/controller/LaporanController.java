package com.resik.api.controller;

import com.resik.api.repository.TransaksiRepository;
import com.resik.api.repository.PenjemputanRepository;
import com.resik.api.repository.UserRepository;
import com.resik.api.entity.Transaksi;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/laporan")
public class LaporanController {

    private final TransaksiRepository transaksiRepo;
    private final PenjemputanRepository penjemputanRepo;
    private final UserRepository userRepo;

    public LaporanController(TransaksiRepository transaksiRepo,
                             PenjemputanRepository penjemputanRepo,
                             UserRepository userRepo) {
        this.transaksiRepo = transaksiRepo;
        this.penjemputanRepo = penjemputanRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/lingkungan")
    public Map<String, Object> getLaporanLingkungan() {
        List<Transaksi> all = transaksiRepo.findAll();
        BigDecimal totalKg = BigDecimal.ZERO;
        BigDecimal totalPembayaran = BigDecimal.ZERO;

        for (Transaksi t : all) {
            if (t.getPenjemputan() != null && t.getPenjemputan().getDetailList() != null) {
                for (var d : t.getPenjemputan().getDetailList()) {
                    if (d.getBeratAktual() != null) {
                        totalKg = totalKg.add(d.getBeratAktual());
                    }
                }
            }
            totalPembayaran = totalPembayaran.add(t.getTotalHarga());
        }

        // Estimasi CO2 reduction: 1 kg sampah anorganik = ~2 kg CO2
        BigDecimal co2Reduction = totalKg.multiply(new BigDecimal("2"));

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("totalSampahKg", totalKg);
        res.put("totalPembayaran", totalPembayaran);
        res.put("estimasiCO2Reduction", co2Reduction);
        res.put("totalTransaksi", all.size());
        res.put("totalUser", userRepo.findAll().size());
        res.put("totalPenjemputan", penjemputanRepo.findAll().size());

        // Estimasi pohon yang diselamatkan (1 pohon = ~20 kg CO2/tahun)
        if (co2Reduction.compareTo(BigDecimal.ZERO) > 0) {
            res.put("estimasiPohon", co2Reduction.divide(new BigDecimal("20"), 0, java.math.RoundingMode.HALF_UP));
        } else {
            res.put("estimasiPohon", 0);
        }

        return res;
    }

    @GetMapping("/transaksi")
    public List<Transaksi> getAllTransaksi() {
        return transaksiRepo.findAll();
    }
}
