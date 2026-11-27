package com.resik.api.controller;

import com.resik.api.entity.Rating;
import com.resik.api.service.RatingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pickup ratings. The petugas is always taken from the pickup on the
 * server side — the request body carries no petugas id, so a caller
 * cannot rate an arbitrary user.
 */
@RestController
@RequestMapping("/api/rating")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> beriRating(@RequestBody Map<String, Object> body,
                                                          Authentication auth) {
        Long pemberiId = requireUserId(auth);
        if (body == null || body.get("penjemputanId") == null || body.get("nilai") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "penjemputanId dan nilai harus diisi");
        }
        Long penjemputanId = Long.valueOf(body.get("penjemputanId").toString());
        int nilai = Integer.parseInt(body.get("nilai").toString());
        String komentar = body.get("komentar") != null ? body.get("komentar").toString() : null;
        Rating r = ratingService.beriRating(penjemputanId, pemberiId, nilai, komentar);
        return ResponseEntity.status(HttpStatus.CREATED).body(toMap(r));
    }

    @GetMapping("/petugas/{petugasId}")
    public Map<String, Object> ringkasanPetugas(@PathVariable Long petugasId) {
        return ratingService.ringkasanPetugas(petugasId);
    }

    @GetMapping("/penjemputan/{id}")
    public Map<String, Object> ratingPenjemputan(@PathVariable Long id) {
        return toMap(ratingService.ratingPenjemputan(id));
    }

    private Map<String, Object> toMap(Rating r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("penjemputanId", r.getPenjemputan().getId());
        m.put("pemberiRatingId", r.getPemberiRating().getId());
        m.put("petugasId", r.getPetugas().getId());
        m.put("nilai", r.getNilai());
        m.put("komentar", r.getKomentar());
        m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
        return m;
    }

    private Long requireUserId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login diperlukan");
        }
        return userId;
    }
}
