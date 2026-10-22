package com.resik.api.dto;

public class AuthDtos {
    public record RegisterRequest(
        String nama,
        String email,
        String password,
        String alamat,
        String noTelp,
        String role
    ) {}

    public record LoginRequest(
        String email,
        String password
    ) {}

    public record AuthResponse(
        String token,
        Long userId,
        String nama,
        String email,
        String role
    ) {}
}
