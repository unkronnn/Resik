package com.resik.api.controller;

import com.resik.api.dto.ChatDtos.*;
import com.resik.api.entity.Chat;
import com.resik.api.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<Chat> send(@RequestBody SendRequest req) {
        return ResponseEntity.ok(chatService.sendMessage(req.userId(), req.pengirim(), req.pesan()));
    }

    @GetMapping("/{userId}")
    public List<Chat> getMessages(@PathVariable Long userId) {
        return chatService.getMessages(userId);
    }
}
