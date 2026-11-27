package com.resik.api.service;

import com.resik.api.entity.User;
import com.resik.api.entity.Sampah;
import com.resik.api.entity.Penjemputan;
import com.resik.api.entity.DetailPenjemputan;
import com.resik.api.entity.Transaksi;
import com.resik.api.entity.Notifikasi;
import com.resik.api.dto.PenjemputanDtos;
import com.resik.api.repository.PenjemputanRepository;
import com.resik.api.repository.DetailPenjemputanRepository;
import com.resik.api.repository.SampahRepository;
import com.resik.api.repository.UserRepository;
import com.resik.api.repository.TransaksiRepository;
import com.resik.api.repository.NotifikasiRepository;
import com.resik.api.websocket.RealtimeEventPublisher;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class PenjemputanService {

    private final PenjemputanRepository penjemputanRepo;
    private final DetailPenjemputanRepository detailRepo;
    private final SampahRepository sampahRepo;
    private final UserRepository userRepo;
    private final TransaksiRepository transaksiRepo;
    private final NotifikasiRepository notifRepo;
    private final RealtimeEventPublisher eventPublisher;
    private final FcmNotificationService fcmService;

    /** Batas ukuran foto bukti timbangan setelah decode Base64 (5 MB). */
    public static final long MAX_FOTO_BYTES = 5L * 1024 * 1024;

    /** Direktori penyimpanan foto bukti, relatif terhadap direktori kerja backend. */
    private final Path uploadsDir;

    public PenjemputanService(PenjemputanRepository penjemputanRepo,
                              DetailPenjemputanRepository detailRepo,
                              SampahRepository sampahRepo,
                              UserRepository userRepo,
                              TransaksiRepository transaksiRepo,
                              NotifikasiRepository notifRepo,
                              RealtimeEventPublisher eventPublisher,
                              FcmNotificationService fcmService,
                              @Value("${resik.uploads-dir:uploads}") String uploadsDir) {
        this.penjemputanRepo = penjemputanRepo;
        this.detailRepo = detailRepo;
        this.sampahRepo = sampahRepo;
        this.userRepo = userRepo;
        this.transaksiRepo = transaksiRepo;
        this.notifRepo = notifRepo;
        this.eventPublisher = eventPublisher;
        this.fcmService = fcmService;
        this.uploadsDir = Paths.get(uploadsDir);
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

        // Realtime: admins and bank sampah see the new request immediately.
        eventPublisher.penjemputanCreated(p);

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

        String statusLama = p.getStatus() != null ? p.getStatus().name() : null;
        p.setPetugas(petugas);
        p.setStatus(Penjemputan.PenjemputanStatus.DIJADWALKAN);
        p = penjemputanRepo.save(p);

        Notifikasi n = new Notifikasi();
        n.setUser(p.getUser());
        n.setJudul("Petugas Ditugaskan");
        n.setPesan("Petugas akan datang menjemput sampah Anda pada " + p.getTanggalJemput());
        notifRepo.save(n);

        // Realtime: masyarakat, admin, and bank sampah see DIJADWALKAN immediately.
        eventPublisher.penjemputanStatusChanged(p, statusLama);
        // Push (no-op until FCM is configured — see docs/FCM_SETUP.md).
        fcmService.sendToUser(p.getUser().getId(), "Penjemputan Dijadwalkan",
                "Petugas akan datang menjemput sampah Anda pada " + p.getTanggalJemput(),
                Map.of("type", "PENJEMPUTAN_STATUS", "penjemputanId", String.valueOf(p.getId())));

        return p;
    }

    /**
     * Full pickup detail for the receipt (struk): associations are
     * initialised here so serialisation does not depend on open-in-view.
     * Every detail line's {@code sampah} is initialised explicitly too:
     * the receipt needs the waste name and price/kg per line, and
     * {@code DetailPenjemputan.sampah} is a lazy proxy that
     * {@code Hibernate.initialize(detailList)} alone does not load.
     */
    @Transactional(readOnly = true)
    public Penjemputan getById(Long penjemputanId) {
        Penjemputan p = penjemputanRepo.findById(penjemputanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Penjemputan tidak ditemukan"));
        Hibernate.initialize(p.getDetailList());
        for (DetailPenjemputan dp : p.getDetailList()) {
            Hibernate.initialize(dp.getSampah());
            if (dp.getSampah() != null) {
                // Touch the exact fields the receipt renders, so they are
                // loaded inside this transaction, not during serialisation.
                dp.getSampah().getNama();
                dp.getSampah().getHargaPerKg();
            }
        }
        Hibernate.initialize(p.getUser());
        if (p.getPetugas() != null) {
            Hibernate.initialize(p.getPetugas());
        }
        if (p.getTransaksi() != null) {
            Hibernate.initialize(p.getTransaksi());
        }
        return p;
    }

    /**
     * Cancel a pickup. Allowed for the owning masyarakat or for staff
     * (ADMIN / PETUGAS). A pickup that is already SELESAI or DIBATALKAN
     * cannot be cancelled.
     */
    @Transactional
    public Penjemputan batalkanPenjemputan(Long penjemputanId, Long callerId, String callerRole) {
        Penjemputan p = penjemputanRepo.findById(penjemputanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Penjemputan tidak ditemukan"));
        requireOwnerOrStaff(p, callerId, callerRole);
        if (p.getStatus() == Penjemputan.PenjemputanStatus.SELESAI
                || p.getStatus() == Penjemputan.PenjemputanStatus.DIBATALKAN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Penjemputan dengan status " + p.getStatus() + " tidak dapat dibatalkan");
        }

        String statusLama = p.getStatus() != null ? p.getStatus().name() : null;
        p.setStatus(Penjemputan.PenjemputanStatus.DIBATALKAN);
        p = penjemputanRepo.save(p);

        Notifikasi n = new Notifikasi();
        n.setUser(p.getUser());
        n.setJudul("Penjemputan Dibatalkan");
        n.setPesan("Penjemputan sampah Anda pada " + p.getTanggalJemput() + " telah dibatalkan.");
        notifRepo.save(n);

        eventPublisher.penjemputanStatusChanged(p, statusLama);
        fcmService.sendToUser(p.getUser().getId(), "Penjemputan Dibatalkan",
                "Penjemputan sampah Anda pada " + p.getTanggalJemput() + " telah dibatalkan.",
                Map.of("type", "PENJEMPUTAN_STATUS", "penjemputanId", String.valueOf(p.getId())));
        return p;
    }

    /**
     * Reschedule a pickup (change date and/or time). Allowed for the owning
     * masyarakat or for staff (ADMIN / PETUGAS). The status itself is not
     * changed; a PENJEMPUTAN_STATUS event is still broadcast because the
     * payload carries the new {@code tanggalJemput}. Rejected when the
     * pickup is already SELESAI or DIBATALKAN.
     */
    @Transactional
    public Penjemputan jadwalUlang(Long penjemputanId, LocalDate tanggalJemput, String waktuJemput,
                                   Long callerId, String callerRole) {
        Penjemputan p = penjemputanRepo.findById(penjemputanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Penjemputan tidak ditemukan"));
        requireOwnerOrStaff(p, callerId, callerRole);
        if (p.getStatus() == Penjemputan.PenjemputanStatus.SELESAI
                || p.getStatus() == Penjemputan.PenjemputanStatus.DIBATALKAN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Penjemputan dengan status " + p.getStatus() + " tidak dapat dijadwalkan ulang");
        }
        if (tanggalJemput == null && (waktuJemput == null || waktuJemput.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "tanggalJemput atau waktuJemput harus diisi");
        }

        String statusLama = p.getStatus() != null ? p.getStatus().name() : null;
        if (tanggalJemput != null) {
            p.setTanggalJemput(tanggalJemput);
        }
        if (waktuJemput != null && !waktuJemput.isBlank()) {
            p.setWaktuJemput(waktuJemput);
        }
        p = penjemputanRepo.save(p);

        Notifikasi n = new Notifikasi();
        n.setUser(p.getUser());
        n.setJudul("Jadwal Penjemputan Diubah");
        n.setPesan("Penjemputan sampah Anda dijadwalkan ulang pada " + p.getTanggalJemput()
                + (p.getWaktuJemput() != null ? " pukul " + p.getWaktuJemput() : ""));
        notifRepo.save(n);

        eventPublisher.penjemputanStatusChanged(p, statusLama);
        fcmService.sendToUser(p.getUser().getId(), "Jadwal Penjemputan Diubah",
                "Penjemputan sampah Anda dijadwalkan ulang pada " + p.getTanggalJemput(),
                Map.of("type", "PENJEMPUTAN_STATUS", "penjemputanId", String.valueOf(p.getId())));
        return p;
    }

    /**
     * Generic status change for the desktop admin (Tugaskan / Proses).
     * Staff only (ADMIN / PETUGAS). Valid forward transitions:
     * MENUNGGU → DIJADWALKAN / DIBATALKAN, DIJADWALKAN → DIPROSES /
     * DIBATALKAN, DIPROSES → DIBATALKAN. Setting the same status again is
     * an idempotent no-op. SELESAI is deliberately not a valid target:
     * completing a pickup must go through {@link #timbangSampah}, which
     * creates the Transaksi and credits the wallet — a bare status flip
     * would leave a SELESAI pickup with no transaction and no payment.
     * Nothing may move out of SELESAI or DIBATALKAN.
     */
    @Transactional
    public Penjemputan ubahStatus(Long penjemputanId, String statusBaru, Long callerId, String callerRole) {
        Penjemputan p = penjemputanRepo.findById(penjemputanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Penjemputan tidak ditemukan"));
        if (!isStaff(callerRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Akses ditolak: hanya admin atau petugas");
        }
        Penjemputan.PenjemputanStatus target;
        try {
            target = Penjemputan.PenjemputanStatus.valueOf(statusBaru.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Status tidak valid: " + statusBaru);
        }
        if (target == Penjemputan.PenjemputanStatus.SELESAI) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Status SELESAI hanya dapat dicapai lewat timbang (/api/penjemputan/timbang)");
        }
        Penjemputan.PenjemputanStatus sekarang = p.getStatus();
        if (target == sekarang) {
            return p; // idempotent no-op
        }
        if (!isValidTransition(sekarang, target)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Transisi status tidak valid: " + sekarang + " -> " + target);
        }

        String statusLama = sekarang != null ? sekarang.name() : null;
        p.setStatus(target);
        p = penjemputanRepo.save(p);

        Notifikasi n = new Notifikasi();
        n.setUser(p.getUser());
        n.setJudul("Status Penjemputan Berubah");
        n.setPesan("Penjemputan sampah Anda kini berstatus " + target);
        notifRepo.save(n);

        eventPublisher.penjemputanStatusChanged(p, statusLama);
        fcmService.sendToUser(p.getUser().getId(), "Status Penjemputan Berubah",
                "Penjemputan sampah Anda kini berstatus " + target,
                Map.of("type", "PENJEMPUTAN_STATUS", "penjemputanId", String.valueOf(p.getId())));
        return p;
    }

    private boolean isValidTransition(Penjemputan.PenjemputanStatus dari,
                                      Penjemputan.PenjemputanStatus ke) {
        return switch (dari) {
            case MENUNGGU -> ke == Penjemputan.PenjemputanStatus.DIJADWALKAN
                    || ke == Penjemputan.PenjemputanStatus.DIBATALKAN;
            case DIJADWALKAN -> ke == Penjemputan.PenjemputanStatus.DIPROSES
                    || ke == Penjemputan.PenjemputanStatus.DIBATALKAN;
            case DIPROSES -> ke == Penjemputan.PenjemputanStatus.DIBATALKAN;
            case SELESAI, DIBATALKAN -> false;
        };
    }

    private boolean isStaff(String callerRole) {
        return "ADMIN".equalsIgnoreCase(callerRole) || "PETUGAS".equalsIgnoreCase(callerRole);
    }

    private void requireOwnerOrStaff(Penjemputan p, Long callerId, String callerRole) {
        boolean owner = p.getUser() != null && p.getUser().getId().equals(callerId);
        if (!owner && !isStaff(callerRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Akses ditolak: hanya pemilik penjemputan, admin, atau petugas");
        }
    }

    /**
     * Stores the weighing-proof photo for a pickup. Accepts a Base64 image
     * (a {@code data:image/...;base64,} prefix is tolerated), at most 5 MB
     * after decoding. The file is written to
     * {@code uploads/bukti/{penjemputanId}.{jpg|png|webp}} under the
     * backend working directory; only the serving URL is stored on the
     * entity ({@code /api/penjemputan/{id}/foto}), never the raw bytes.
     * Re-uploading replaces the previous photo (including a stale file
     * with a different extension).
     *
     * @return the {@code fotoBuktiUrl} to store/return to the caller.
     */
    @Transactional
    public String simpanFotoBukti(Long penjemputanId, String fotoBase64, String contentType) {
        Penjemputan p = penjemputanRepo.findById(penjemputanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Penjemputan tidak ditemukan"));
        if (fotoBase64 == null || fotoBase64.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fotoBase64 harus diisi");
        }
        String ext = switch (contentType != null ? contentType.toLowerCase() : "") {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "contentType tidak didukung (gunakan image/jpeg, image/png, atau image/webp): " + contentType);
        };
        String clean = fotoBase64.strip();
        int comma = clean.indexOf(',');
        if (clean.startsWith("data:") && comma > 0) {
            clean = clean.substring(comma + 1);
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(clean);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fotoBase64 bukan Base64 yang valid");
        }
        if (bytes.length > MAX_FOTO_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "Foto melebihi batas 5 MB (" + bytes.length + " byte)");
        }
        try {
            Path dir = uploadsDir.resolve("bukti");
            Files.createDirectories(dir);
            try (var stream = Files.list(dir)) {
                for (Path old : stream.filter(f -> f.getFileName().toString().startsWith(penjemputanId + ".")).toList()) {
                    Files.deleteIfExists(old);
                }
            }
            Files.write(dir.resolve(penjemputanId + "." + ext), bytes);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Gagal menyimpan foto bukti: " + e.getMessage());
        }
        String url = "/api/penjemputan/" + penjemputanId + "/foto";
        p.setFotoBuktiUrl(url);
        penjemputanRepo.save(p);
        return url;
    }

    /** Loads a stored proof photo for serving. */
    @Transactional(readOnly = true)
    public FotoBukti bacaFotoBukti(Long penjemputanId) {
        Penjemputan p = penjemputanRepo.findById(penjemputanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Penjemputan tidak ditemukan"));
        if (p.getFotoBuktiUrl() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Penjemputan ini belum memiliki foto bukti");
        }
        Path dir = uploadsDir.resolve("bukti");
        try (var stream = Files.list(dir)) {
            for (Path file : stream.filter(f -> f.getFileName().toString().startsWith(penjemputanId + ".")).toList()) {
                String name = file.getFileName().toString();
                String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
                String contentType = switch (ext) {
                    case "png" -> "image/png";
                    case "webp" -> "image/webp";
                    default -> "image/jpeg";
                };
                return new FotoBukti(Files.readAllBytes(file), contentType);
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Gagal membaca foto bukti: " + e.getMessage());
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File foto bukti tidak ditemukan");
    }

    /** A stored proof photo: raw bytes plus the content type to serve it with. */
    public record FotoBukti(byte[] bytes, String contentType) {
    }

    @Transactional
    public Transaksi timbangSampah(PenjemputanDtos.TimbangRequest req) {
        Penjemputan p = penjemputanRepo.findById(req.penjemputanId())
                .orElseThrow(() -> new RuntimeException("Penjemputan tidak ditemukan"));

        String statusLama = p.getStatus() != null ? p.getStatus().name() : null;
        p.setStatus(Penjemputan.PenjemputanStatus.DIPROSES);

        BigDecimal total = BigDecimal.ZERO;
        Map<String, BigDecimal> beratPerKategori = new HashMap<>();

        for (PenjemputanDtos.TimbangDetail td : req.details()) {
            DetailPenjemputan dp = detailRepo.findById(td.detailId())
                    .orElseThrow(() -> new RuntimeException("Detail tidak ditemukan"));
            dp.setBeratAktual(td.beratAktual());
            BigDecimal subtotal = td.beratAktual().multiply(dp.getSampah().getHargaPerKg());
            dp.setSubtotal(subtotal);
            detailRepo.save(dp);
            total = total.add(subtotal);
            beratPerKategori.merge(dp.getSampah().getKategori(), td.beratAktual(), BigDecimal::add);
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

        // Realtime: pickup is SELESAI, wallet + points grew, stock grew,
        // and the leaderboard order may have changed. One timbang, four events.
        eventPublisher.penjemputanStatusChanged(p, statusLama);
        eventPublisher.walletUpdated(masyarakat, "TIMBANG", total);
        eventPublisher.stokUpdated(p.getId(), RealtimeEventPublisher.stokLines(beratPerKategori));
        eventPublisher.leaderboardUpdated(masyarakat);
        // Push (no-op until FCM is configured — see docs/FCM_SETUP.md).
        fcmService.sendToUser(masyarakat.getId(), "Setoran Selesai",
                "Setoran sampah Anda selesai. Total: Rp" + total + " telah masuk ke dompet digital.",
                Map.of("type", "PENJEMPUTAN_STATUS", "penjemputanId", String.valueOf(p.getId())));

        return t;
    }
}
