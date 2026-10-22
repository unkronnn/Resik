package com.resik.api.controller;

import com.resik.api.entity.Notifikasi;
import com.resik.api.repository.NotifikasiRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifikasi")
public class NotifikasiController {

    private final NotifikasiRepository notifRepo;

    public NotifikasiController(NotifikasiRepository notifRepo) {
        this.notifRepo = notifRepo;
    }

    @GetMapping
    public List<Notifikasi> getMy(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return notifRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @PutMapping("/{id}/read")
    public Notifikasi markAsRead(@PathVariable Long id) {
        Notifikasi n = notifRepo.findById(id).orElseThrow();
        n.setStatus(Notifikasi.NotifikasiStatus.DIBACA);
        return notifRepo.save(n);
    }
}
