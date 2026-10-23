package com.resik.api.controller;

import com.resik.api.entity.LokasiBankSampah;
import com.resik.api.repository.LokasiBankSampahRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/lokasi")
public class LokasiController {

    private final LokasiBankSampahRepository repo;

    public LokasiController(LokasiBankSampahRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<LokasiBankSampah> getAll() {
        return repo.findByIsActiveTrue();
    }

    @GetMapping("/all")
    public List<LokasiBankSampah> getAllAdmin() {
        return repo.findAll();
    }

    @PostMapping
    public LokasiBankSampah create(@RequestBody LokasiBankSampah lokasi) {
        return repo.save(lokasi);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LokasiBankSampah> update(@PathVariable Long id, @RequestBody LokasiBankSampah req) {
        return repo.findById(id).map(l -> {
            l.setNama(req.getNama());
            l.setAlamat(req.getAlamat());
            l.setJamOperasional(req.getJamOperasional());
            l.setTelepon(req.getTelepon());
            l.setLatitude(req.getLatitude());
            l.setLongitude(req.getLongitude());
            l.setIsActive(req.getIsActive());
            return ResponseEntity.ok(repo.save(l));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
