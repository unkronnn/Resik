package com.resik.api.controller;

import com.resik.api.entity.Sampah;
import com.resik.api.repository.SampahRepository;
import com.resik.api.websocket.RealtimeEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/sampah")
public class SampahController {

    private final SampahRepository sampahRepo;
    private final RealtimeEventPublisher eventPublisher;

    public SampahController(SampahRepository sampahRepo, RealtimeEventPublisher eventPublisher) {
        this.sampahRepo = sampahRepo;
        this.eventPublisher = eventPublisher;
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
        Sampah saved = sampahRepo.save(sampah);
        // Realtime: everyone sees the new waste type in the price list.
        eventPublisher.sampahCreated(saved);
        return saved;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sampah> update(@PathVariable Long id, @RequestBody Sampah req) {
        return sampahRepo.findById(id).map(s -> {
            BigDecimal hargaLama = s.getHargaPerKg();
            s.setKategori(req.getKategori());
            s.setNama(req.getNama());
            s.setHargaPerKg(req.getHargaPerKg());
            s.setDeskripsi(req.getDeskripsi());
            s.setIsActive(req.getIsActive());
            Sampah saved = sampahRepo.save(s);
            // Realtime: everyone sees the new price immediately.
            eventPublisher.hargaSampahUpdated(saved, hargaLama);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        String nama = sampahRepo.findById(id).map(Sampah::getNama).orElse(null);
        sampahRepo.deleteById(id);
        // Realtime: everyone drops the waste type from the price list.
        eventPublisher.sampahDeleted(id, nama);
        return ResponseEntity.noContent().build();
    }
}
