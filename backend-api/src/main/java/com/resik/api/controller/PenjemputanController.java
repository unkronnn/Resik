package com.resik.api.controller;

import com.resik.api.dto.PenjemputanDtos;
import com.resik.api.entity.Penjemputan;
import com.resik.api.entity.Transaksi;
import com.resik.api.service.PenjemputanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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

    @PutMapping("/{id}/assign/{petugasId}")
    public ResponseEntity<Penjemputan> assignPetugas(@PathVariable Long id, @PathVariable Long petugasId) {
        return ResponseEntity.ok(penjemputanService.assignPetugas(id, petugasId));
    }

    @PostMapping("/timbang")
    public ResponseEntity<Transaksi> timbang(@RequestBody PenjemputanDtos.TimbangRequest req) {
        return ResponseEntity.ok(penjemputanService.timbangSampah(req));
    }
}
