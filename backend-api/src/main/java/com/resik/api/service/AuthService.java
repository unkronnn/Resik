package com.resik.api.service;

import com.resik.api.dto.AuthDtos.*;
import com.resik.api.entity.*;
import com.resik.api.repository.*;
import com.resik.api.security.JwtUtil;
import com.resik.api.websocket.RealtimeEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RealtimeEventPublisher eventPublisher;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, RealtimeEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new RuntimeException("Email sudah terdaftar");
        }

        User user;
        String role = req.role() != null ? req.role().toUpperCase() : "MASYARAKAT";

        switch (role) {
            case "PETUGAS" -> user = new Petugas();
            case "ADMIN" -> user = new Admin();
            default -> user = new Masyarakat();
        }

        user.setNama(req.nama());
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setAlamat(req.alamat());
        user.setNoTelp(req.noTelp());

        user = userRepository.save(user);

        // Realtime: admins see the new account in Verifikasi & Approval immediately.
        // Note: the backend has no approve endpoint yet; the desktop Approve
        // button currently only updates its local table, so no
        // "account approved" event is emitted anywhere until that endpoint exists.
        eventPublisher.accountVerification(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getNama(), user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("Email tidak ditemukan"));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new RuntimeException("Password salah");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getNama(), user.getEmail(), user.getRole());
    }
}
