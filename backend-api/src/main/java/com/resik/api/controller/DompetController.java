package com.resik.api.controller;

import com.resik.api.entity.User;
import com.resik.api.repository.UserRepository;
import com.resik.api.repository.TransaksiRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/dompet")
public class DompetController {

    private final UserRepository userRepo;
    private final TransaksiRepository transaksiRepo;

    public DompetController(UserRepository userRepo, TransaksiRepository transaksiRepo) {
        this.userRepo = userRepo;
        this.transaksiRepo = transaksiRepo;
    }

    @GetMapping("/saldo")
    public ResponseEntity<Map<String, Object>> getSaldo(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userRepo.findById(userId).orElseThrow();

        Map<String, Object> res = new HashMap<>();
        res.put("saldo", user.getSaldoDompet());
        res.put("poinReward", user.getPoinReward());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/riwayat")
    public List<?> getRiwayat(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return transaksiRepo.findByPenjemputanUserId(userId);
    }

    @GetMapping("/all")
    public List<Map<String, Object>> getAllSaldo() {
        List<User> users = userRepo.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> m = new HashMap<>();
            m.put("userId", u.getId());
            m.put("nama", u.getNama());
            m.put("saldo", u.getSaldoDompet());
            m.put("poin", u.getPoinReward());
            result.add(m);
        }
        return result;
    }

    @PutMapping("/topup/{userId}")
    public ResponseEntity<Map<String, Object>> topup(@PathVariable Long userId, @RequestBody Map<String, BigDecimal> req) {
        User user = userRepo.findById(userId).orElseThrow();
        BigDecimal jumlah = req.get("jumlah");
        user.setSaldoDompet(user.getSaldoDompet().add(jumlah));
        userRepo.save(user);

        Map<String, Object> res = new HashMap<>();
        res.put("status", "BERHASIL");
        res.put("saldoBaru", user.getSaldoDompet());
        return ResponseEntity.ok(res);
    }

    @PutMapping("/tarik")
    public ResponseEntity<Map<String, Object>> tarik(Authentication auth, @RequestBody Map<String, BigDecimal> req) {
        Long userId = (Long) auth.getPrincipal();
        User user = userRepo.findById(userId).orElseThrow();
        BigDecimal jumlah = req.get("jumlah");

        if (user.getSaldoDompet().compareTo(jumlah) < 0) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "GAGAL");
            err.put("pesan", "Saldo tidak mencukupi");
            return ResponseEntity.badRequest().body(err);
        }

        user.setSaldoDompet(user.getSaldoDompet().subtract(jumlah));
        userRepo.save(user);

        Map<String, Object> res = new HashMap<>();
        res.put("status", "BERHASIL");
        res.put("saldoBaru", user.getSaldoDompet());
        return ResponseEntity.ok(res);
    }
}
