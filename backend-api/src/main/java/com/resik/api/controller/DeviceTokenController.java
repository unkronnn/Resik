package com.resik.api.controller;

import com.resik.api.entity.DeviceToken;
import com.resik.api.entity.User;
import com.resik.api.repository.DeviceTokenRepository;
import com.resik.api.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FCM device-token registration. The Android app calls
 * {@code POST /api/notifikasi/token} after login (and whenever Firebase
 * refreshes its token) so the server can push to the device when the app
 * is closed. Registration is an upsert keyed by the token itself.
 */
@RestController
@RequestMapping("/api/notifikasi/token")
public class DeviceTokenController {

    private final DeviceTokenRepository deviceTokenRepo;
    private final UserRepository userRepo;

    public DeviceTokenController(DeviceTokenRepository deviceTokenRepo, UserRepository userRepo) {
        this.deviceTokenRepo = deviceTokenRepo;
        this.userRepo = userRepo;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> registerToken(@RequestBody Map<String, String> body,
                                                             Authentication auth) {
        Long userId = requireUserId(auth);
        String token = body != null ? body.get("token") : null;
        String platform = body != null ? body.get("platform") : null;
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "token harus diisi");
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

        DeviceToken dt = deviceTokenRepo.findByToken(token).orElseGet(DeviceToken::new);
        dt.setUser(user);
        dt.setToken(token);
        dt.setPlatform(platform != null ? platform : "android");
        dt = deviceTokenRepo.save(dt);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("id", dt.getId());
        res.put("userId", userId);
        res.put("platform", dt.getPlatform());
        res.put("updatedAt", dt.getUpdatedAt() != null ? dt.getUpdatedAt().toString() : null);
        return ResponseEntity.ok(res);
    }

    /**
     * Lists a user's registered tokens. For use by the push sender and by
     * admins debugging delivery. A caller may only list their own tokens,
     * unless they are an admin.
     */
    @GetMapping
    public List<Map<String, Object>> listTokens(@RequestParam Long userId, Authentication auth) {
        Long callerId = requireUserId(auth);
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!callerId.equals(userId) && !admin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Akses ditolak: hanya pemilik token atau admin");
        }
        return deviceTokenRepo.findByUserId(userId).stream()
                .map(dt -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", dt.getId());
                    m.put("userId", dt.getUser().getId());
                    m.put("platform", dt.getPlatform());
                    m.put("updatedAt", dt.getUpdatedAt() != null ? dt.getUpdatedAt().toString() : null);
                    return m;
                })
                .toList();
    }

    private Long requireUserId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login diperlukan");
        }
        return userId;
    }
}
