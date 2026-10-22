package com.resik.api.service;

import com.resik.api.entity.*;
import com.resik.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PenjemputanService {

    private final PenjemputanRepository penjemputanRepo;
    private final DetailPenjemputanRepository detailRepo;
    private final SampahRepository sampahRepo;
    private final UserRepository userRepo;
    private final TransaksiRepository transaksiRepo;
    private final NotifikasiRepository notifRepo;

    public PenjemputanService(PenjemputanRepository penjemputanRepo,
                              DetailPenjemputanRepository detailRepo,
                              SampahRepository sampahRepo,
                              UserRepository userRepo,
                              TransaksiRepository transaksiRepo,
                              NotifikasiRepository notifRepo) {
        this.penjemputanRepo = penjemputanRepo;
        this.detailRepo = detailRepo;
        this.sampahRepo = sampahRepo;
        this.userRepo = userRepo;
        this.transaksiRepo = transaksiRepo;
        this.notifRepo = notifRepo;
    }

    @Transactional
    public Penjemputan createPenjemputan(Long userId, PenjemputanDtos.CreateRequest req) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        Penjemputan p = new Penjemputan();
        p.setUser(user);
        p.setTanggalJemput(req.tanggalJemput());
        p.setWaktuJemput(req.waktuJemput());
        p.setAlamat(req.alamat());
        p.setCatatan(req.catatan());
        p.setLatitude(req.latitude());
        p.setLongitude(req.longitude());
        p.setQrCode("RESIK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        p.setStatus(Penjemputan.PenjemputanStatus.MENUNGGU);

        p = penjemputanRepo.save(p);

        for (PenjemputanDtos.DetailRequest dr : req.details()) {
            Sampah s = sampahRepo.findById(dr.sampahId())
                    .orElseThrow(() -> new RuntimeException("Sampah tidak ditemukan"));
            DetailPenjemputan dp = new DetailPenjemputan();
            dp.setPenjemputan(p);
            dp.setSampah(s);
            dp.setEstimasiBerat(dr.estimasiBerat());
            detailRepo.save(dp);
            p.getDetailList().add(dp);
        }

        Notifikasi n = new Notifikasi();
        n.setUser(user);
        n.setJudul("Penjemputan Dibuat");
        n.setPesan("Penjemputan sampah Anda telah dijadwalkan pada " + req.tanggalJemput());
        notifRepo.save(n);

        return p;
    }

    public List<Penjemputan> getByUser(Long userId) {
        return penjemputanRepo.findByUserId(userId);
    }

    public List<Penjemputan> getByStatus(Penjemputan.PenjemputanStatus status) {
        return penjemputanRepo.findByStatus(status);
    }

    public List<Penjemputan> getAll() {
        return penjemputanRepo.findAll();
    }

    @Transactional
    public Penjemputan assignPetugas(Long penjemputanId, Long petugasId) {
        Penjemputan p = penjemputanRepo.findById(penjemputanId)
                .orElseThrow(() -> new RuntimeException("Penjemputan tidak ditemukan"));
        User petugas = userRepo.findById(petugasId)
                .orElseThrow(() -> new RuntimeException("Petugas tidak ditemukan"));

        p.setPetugas(petugas);
        p.setStatus(Penjemputan.PenjemputanStatus.DIJADWALKAN);
        p = penjemputanRepo.save(p);

        Notifikasi n = new Notifikasi();
        n.setUser(p.getUser());
        n.setJudul("Petugas Ditugaskan");
        n.setPesan("Petugas akan datang menjemput sampah Anda pada " + p.getTanggalJemput());
        notifRepo.save(n);

        return p;
    }

    @Transactional
    public Transaksi timbangSampah(PenjemputanDtos.TimbangRequest req) {
        Penjemputan p = penjemputanRepo.findById(req.penjemputanId())
                .orElseThrow(() -> new RuntimeException("Penjemputan tidak ditemukan"));

        p.setStatus(Penjemputan.PenjemputanStatus.DIPROSES);

        BigDecimal total = BigDecimal.ZERO;

        for (PenjemputanDtos.TimbangDetail td : req.details()) {
            DetailPenjemputan dp = detailRepo.findById(td.detailId())
                    .orElseThrow(() -> new RuntimeException("Detail tidak ditemukan"));
            dp.setBeratAktual(td.beratAktual());
            BigDecimal subtotal = td.beratAktual().multiply(dp.getSampah().getHargaPerKg());
            dp.setSubtotal(subtotal);
            detailRepo.save(dp);
            total = total.add(subtotal);
        }

        p = penjemputanRepo.save(p);

        Transaksi t = new Transaksi();
        t.setPenjemputan(p);
        t.setTotalHarga(total);
        t.setStatus(Transaksi.TransaksiStatus.BERHASIL);
        t = transaksiRepo.save(t);

        p.setStatus(Penjemputan.PenjemputanStatus.SELESAI);
        p.setTransaksi(t);
        penjemputanRepo.save(p);

        User masyarakat = p.getUser();
        masyarakat.setSaldoDompet(masyarakat.getSaldoDompet().add(total));
        masyarakat.setPoinReward(masyarakat.getPoinReward() + total.intValue() / 1000);
        userRepo.save(masyarakat);

        Notifikasi n = new Notifikasi();
        n.setUser(masyarakat);
        n.setJudul("Setoran Selesai");
        n.setPesan("Setoran sampah Anda selesai. Total: Rp" + total + " telah masuk ke dompet digital.");
        notifRepo.save(n);

        return t;
    }
}
