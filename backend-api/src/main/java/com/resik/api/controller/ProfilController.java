package com.resik.api.controller;

import com.resik.api.entity.User;
import com.resik.api.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/profil")
public class ProfilController {

    private final UserRepository userRepo;

    public ProfilController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping
    public ResponseEntity<?> getProfil(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userRepo.findById(userId).orElseThrow();
        return ResponseEntity.ok(user);
    }

    @PutMapping
    public ResponseEntity<User> updateProfil(Authentication auth, @RequestBody Map<String, String> req) {
        Long userId = (Long) auth.getPrincipal();
        User user = userRepo.findById(userId).orElseThrow();

        if (req.containsKey("nama")) user.setNama(req.get("nama"));
        if (req.containsKey("alamat")) user.setAlamat(req.get("alamat"));
        if (req.containsKey("noTelp")) user.setNoTelp(req.get("noTelp"));

        return ResponseEntity.ok(userRepo.save(user));
    }
}
