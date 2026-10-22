package com.resik.api.dto;

public class ChatDtos {
    public record SendRequest(
        Long userId,
        String pengirim,
        String pesan
    ) {}
}
