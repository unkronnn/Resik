package com.resik.api.service;

import com.resik.api.entity.Penjemputan;
import com.resik.api.entity.Rating;
import com.resik.api.entity.User;
import com.resik.api.repository.PenjemputanRepository;
import com.resik.api.repository.RatingRepository;
import com.resik.api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ratings of completed pickups (see {@code docs/RATING.md} for the design
 * choice: what is rated is the petugas who did the pickup, not the
 * bank-sampah location — a pickup has no location id to rate).
 */
@Service
public class RatingService {

    private final RatingRepository ratingRepo;
    private final PenjemputanRepository penjemputanRepo;
    private final UserRepository userRepo;

    public RatingService(RatingRepository ratingRepo,
                         PenjemputanRepository penjemputanRepo,
                         UserRepository userRepo) {
        this.ratingRepo = ratingRepo;
        this.penjemputanRepo = penjemputanRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public Rating beriRating(Long penjemputanId, Long pemberiId, int nilai, String komentar) {
        if (nilai < 1 || nilai > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nilai harus 1 sampai 5");
        }
        Penjemputan p = penjemputanRepo.findById(penjemputanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Penjemputan tidak ditemukan"));
        if (p.getUser() == null || !p.getUser().getId().equals(pemberiId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Akses ditolak: hanya pemilik penjemputan yang dapat menilai");
        }
        if (p.getStatus() != Penjemputan.PenjemputanStatus.SELESAI) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Penjemputan belum selesai (status: " + p.getStatus() + ")");
        }
        if (p.getPetugas() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Penjemputan ini tidak memiliki petugas untuk dinilai");
        }
        if (ratingRepo.existsByPenjemputanId(penjemputanId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Penjemputan ini sudah pernah dinilai");
        }
        User pemberi = userRepo.findById(pemberiId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User tidak ditemukan"));

        Rating r = new Rating();
        r.setPenjemputan(p);
        r.setPemberiRating(pemberi);
        // The petugas is taken from the pickup, never from the request body.
        r.setPetugas(p.getPetugas());
        r.setNilai(nilai);
        r.setKomentar(komentar);
        return ratingRepo.save(r);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> ringkasanPetugas(Long petugasId) {
        Double rata = ratingRepo.averageByPetugasId(petugasId);
        long jumlah = ratingRepo.countByPetugasId(petugasId);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("petugasId", petugasId);
        // One decimal, e.g. 4.5; 0.0 when the petugas has no ratings yet.
        res.put("rataRata", Math.round((rata != null ? rata : 0.0) * 10.0) / 10.0);
        res.put("jumlah", jumlah);
        return res;
    }

    @Transactional(readOnly = true)
    public Rating ratingPenjemputan(Long penjemputanId) {
        return ratingRepo.findByPenjemputanId(penjemputanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Penjemputan ini belum memiliki rating"));
    }
}
