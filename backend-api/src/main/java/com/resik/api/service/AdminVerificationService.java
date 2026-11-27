package com.resik.api.service;

import com.resik.api.entity.User;
import com.resik.api.repository.UserRepository;
import com.resik.api.websocket.RealtimeEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Admin account-approval flow (desktop Verifikasi &amp; Approval).
 * Only callers with role ADMIN may approve or reject; the role check lives
 * in {@code AdminVerificationController}.
 */
@Service
public class AdminVerificationService {

    private final UserRepository userRepo;
    private final RealtimeEventPublisher eventPublisher;

    public AdminVerificationService(UserRepository userRepo,
                                    RealtimeEventPublisher eventPublisher) {
        this.userRepo = userRepo;
        this.eventPublisher = eventPublisher;
    }

    /** Accounts still waiting for an admin decision. */
    public List<User> listPending() {
        return userRepo.findByStatusVerifikasi(User.StatusVerifikasi.MENUNGGU);
    }

    @Transactional
    public User approve(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User tidak ditemukan"));
        user.setStatusVerifikasi(User.StatusVerifikasi.DISETUJUI);
        user.setAlasanPenolakan(null);
        user = userRepo.save(user);
        // Realtime: admins (and the user) see the decision immediately.
        eventPublisher.accountVerificationDecided(user, null);
        return user;
    }

    @Transactional
    public User reject(Long userId, String alasan) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User tidak ditemukan"));
        user.setStatusVerifikasi(User.StatusVerifikasi.DITOLAK);
        user.setAlasanPenolakan(alasan);
        user = userRepo.save(user);
        // Realtime: admins (and the user) see the decision immediately.
        eventPublisher.accountVerificationDecided(user, alasan);
        return user;
    }
}
