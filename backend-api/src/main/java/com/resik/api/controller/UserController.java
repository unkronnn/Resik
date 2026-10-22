package com.resik.api.controller;

import com.resik.api.entity.User;
import com.resik.api.repository.UserRepository;
import com.resik.api.repository.TransaksiRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepo;
    private final TransaksiRepository transaksiRepo;

    public UserController(UserRepository userRepo, TransaksiRepository transaksiRepo) {
        this.userRepo = userRepo;
        this.transaksiRepo = transaksiRepo;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userRepo.findById(userId).orElseThrow();
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("nama", user.getNama());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("saldoDompet", user.getSaldoDompet());
        response.put("poinReward", user.getPoinReward());
        response.put("alamat", user.getAlamat());
        response.put("noTelp", user.getNoTelp());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transaksi")
    public List<?> getMyTransaksi(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return transaksiRepo.findByPenjemputanUserId(userId);
    }

    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
}
