package com.resik.api.controller;

import com.resik.api.entity.Sampah;
import com.resik.api.repository.SampahRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/sampah")
public class SampahController {

    private final SampahRepository sampahRepo;

    public SampahController(SampahRepository sampahRepo) {
        this.sampahRepo = sampahRepo;
    }

    @GetMapping
    public List<Sampah> getAll() {
        return sampahRepo.findByIsActiveTrue();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sampah> getById(@PathVariable Long id) {
        return sampahRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/kategori/{kategori}")
    public List<Sampah> getByKategori(@PathVariable String kategori) {
        return sampahRepo.findByKategori(kategori);
    }

    @PostMapping
    public Sampah create(@RequestBody Sampah sampah) {
        return sampahRepo.save(sampah);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sampah> update(@PathVariable Long id, @RequestBody Sampah req) {
        return sampahRepo.findById(id).map(s -> {
            s.setKategori(req.getKategori());
            s.setNama(req.getNama());
            s.setHargaPerKg(req.getHargaPerKg());
            s.setDeskripsi(req.getDeskripsi());
            s.setIsActive(req.getIsActive());
            return ResponseEntity.ok(sampahRepo.save(s));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sampahRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
