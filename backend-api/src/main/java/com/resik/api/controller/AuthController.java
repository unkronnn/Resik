package com.resik.api.controller;

import com.resik.api.dto.AuthDtos.*;
import com.resik.api.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        String msg = ex.getMessage();
        if (msg != null && (msg.contains("Email") || msg.contains("Password"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg != null ? msg : "Terjadi kesalahan"));
    }
}
