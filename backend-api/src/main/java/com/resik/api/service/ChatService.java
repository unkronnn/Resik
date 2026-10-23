package com.resik.api.service;

import com.resik.api.entity.*;
import com.resik.api.repository.*;
import com.resik.api.websocket.ChatWebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ChatService {

    private final ChatRepository chatRepo;
    private final UserRepository userRepo;
    private final ChatWebSocketHandler webSocketHandler;

    public ChatService(ChatRepository chatRepo, UserRepository userRepo, ChatWebSocketHandler webSocketHandler) {
        this.chatRepo = chatRepo;
        this.userRepo = userRepo;
        this.webSocketHandler = webSocketHandler;
    }

    @Transactional
    public Chat sendMessage(Long userId, String pengirim, String pesan) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        Chat chat = new Chat();
        chat.setUser(user);
        chat.setPengirim(pengirim);
        chat.setPesan(pesan);
        Chat saved = chatRepo.save(chat);

        // Push to admin and user via WebSocket for real-time updates
        webSocketHandler.broadcastNewMessage(saved);

        return saved;
    }

    public List<Chat> getMessages(Long userId) {
        return chatRepo.findByUserIdOrderByCreatedAtAsc(userId);
    }

    /**
     * Returns the latest message from each user — used by the desktop admin
     * to build its conversation list automatically.
     */
    public List<Chat> getLatestPerUser() {
        return chatRepo.findLatestPerUser();
    }

    /**
     * Mark all messages from a user as read (admin has seen them).
     */
    @Transactional
    public void markAsRead(Long userId) {
        List<Chat> messages = chatRepo.findByUserIdOrderByCreatedAtAsc(userId);
        for (Chat c : messages) {
            if ("USER".equals(c.getPengirim()) && (c.getIsRead() == null || !c.getIsRead())) {
                c.setIsRead(true);
            }
        }
        chatRepo.saveAll(messages);
    }
}

