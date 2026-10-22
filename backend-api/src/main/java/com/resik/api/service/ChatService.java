package com.resik.api.service;

import com.resik.api.entity.*;
import com.resik.api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ChatService {

    private final ChatRepository chatRepo;
    private final UserRepository userRepo;

    public ChatService(ChatRepository chatRepo, UserRepository userRepo) {
        this.chatRepo = chatRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public Chat sendMessage(Long userId, String pengirim, String pesan) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        Chat chat = new Chat();
        chat.setUser(user);
        chat.setPengirim(pengirim);
        chat.setPesan(pesan);
        return chatRepo.save(chat);
    }

    public List<Chat> getMessages(Long userId) {
        return chatRepo.findByUserIdOrderByCreatedAtAsc(userId);
    }
}
