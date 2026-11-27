package com.resik.api.controller;

import com.resik.api.dto.PenjemputanDtos;
import com.resik.api.entity.Penjemputan;
import com.resik.api.entity.Transaksi;
import com.resik.api.service.PenjemputanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/penjemputan")
public class PenjemputanController {

    private final PenjemputanService penjemputanService;

    public PenjemputanController(PenjemputanService penjemputanService) {
        this.penjemputanService = penjemputanService;
    }

    @PostMapping
    public ResponseEntity<Penjemputan> create(@RequestBody PenjemputanDtos.CreateRequest req, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(penjemputanService.createPenjemputan(userId, req));
    }

    @GetMapping("/my")
    public List<Penjemputan> getMy(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return penjemputanService.getByUser(userId);
    }

    @GetMapping
    public List<Penjemputan> getAll() {
        return penjemputanService.getAll();
    }

    @GetMapping("/status/{status}")
    public List<Penjemputan> getByStatus(@PathVariable String status) {
        return penjemputanService.getByStatus(Penjemputan.PenjemputanStatus.valueOf(status.toUpperCase()));
    }

    /** Full pickup detail (detailList, transaksi, petugas, fotoBuktiUrl) for the receipt. */
    @GetMapping("/{id}")
    public ResponseEntity<Penjemputan> getById(@PathVariable Long id) {
        return ResponseEntity.ok(penjemputanService.getById(id));
    }

    @PutMapping("/{id}/assign/{petugasId}")
    public ResponseEntity<Penjemputan> assignPetugas(@PathVariable Long id, @PathVariable Long petugasId) {
        return ResponseEntity.ok(penjemputanService.assignPetugas(id, petugasId));
    }

    @PostMapping("/timbang")
    public ResponseEntity<Transaksi> timbang(@RequestBody PenjemputanDtos.TimbangRequest req) {
        return ResponseEntity.ok(penjemputanService.timbangSampah(req));
    }

    /** Cancel a pickup: owner, admin, or petugas. Body kosong. */
    @PutMapping("/{id}/batal")
    public ResponseEntity<Penjemputan> batal(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(penjemputanService.batalkanPenjemputan(
                id, requireUserId(auth), callerRole(auth)));
    }

    /** Reschedule a pickup: owner, admin, or petugas. */
    @PutMapping("/{id}/jadwal")
    public ResponseEntity<Penjemputan> jadwal(@PathVariable Long id,
                                              @RequestBody PenjemputanDtos.JadwalRequest req,
                                              Authentication auth) {
        return ResponseEntity.ok(penjemputanService.jadwalUlang(
                id, req.tanggalJemput(), req.waktuJemput(), requireUserId(auth), callerRole(auth)));
    }

    /** Generic status change for the desktop admin (assign/process). Staff only. */
    @PutMapping("/{id}/status")
    public ResponseEntity<Penjemputan> ubahStatus(@PathVariable Long id,
                                                  @RequestBody PenjemputanDtos.StatusRequest req,
                                                  Authentication auth) {
        return ResponseEntity.ok(penjemputanService.ubahStatus(
                id, req.status(), requireUserId(auth), callerRole(auth)));
    }

    /**
     * Upload the weighing-proof photo (Base64 JSON, max 5 MB decoded).
     * The file is stored under {@code uploads/bukti/} on the backend host.
     */
    @PutMapping("/{id}/foto-bukti")
    public ResponseEntity<Map<String, String>> fotoBukti(@PathVariable Long id,
                                                         @RequestBody PenjemputanDtos.FotoBuktiRequest req,
                                                         Authentication auth) {
        requireUserId(auth);
        String url = penjemputanService.simpanFotoBukti(id, req.fotoBase64(), req.contentType());
        return ResponseEntity.ok(Map.of("fotoBuktiUrl", url));
    }

    /** Serve the stored weighing-proof photo bytes. */
    @GetMapping("/{id}/foto")
    public ResponseEntity<byte[]> foto(@PathVariable Long id) {
        PenjemputanService.FotoBukti foto = penjemputanService.bacaFotoBukti(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(foto.contentType()))
                .body(foto.bytes());
    }

    /**
     * The {@code /api/penjemputan/**} path is permitAll in SecurityConfig
     * (pre-existing), so the filter may leave us without an Authentication;
     * endpoints that need a caller enforce login here instead of assuming it.
     */
    private Long requireUserId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login diperlukan");
        }
        return userId;
    }

    private String callerRole(Authentication auth) {
        if (auth == null) return "";
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replaceFirst("^ROLE_", ""))
                .findFirst().orElse("");
    }
}
