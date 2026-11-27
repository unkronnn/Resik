package com.resik.api.controller;

import com.resik.api.entity.User;
import com.resik.api.service.AdminVerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Admin account-approval endpoints (desktop Verifikasi &amp; Approval).
 * All endpoints require a valid login JWT (the {@code /api/admin/**} path
 * is not in the permitAll list in SecurityConfig) <em>and</em> role ADMIN,
 * checked here from the JWT authorities.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminVerificationController {

    private final AdminVerificationService verificationService;

    public AdminVerificationController(AdminVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping("/verifikasi")
    public List<Map<String, Object>> listPending(Authentication auth) {
        requireAdmin(auth);
        return verificationService.listPending().stream().map(AdminVerificationController::toPublicMap).toList();
    }

    @PostMapping("/users/{id}/approve")
    public ResponseEntity<Map<String, Object>> approve(@PathVariable Long id, Authentication auth) {
        requireAdmin(auth);
        return ResponseEntity.ok(toPublicMap(verificationService.approve(id)));
    }

    @PostMapping("/users/{id}/reject")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable Long id,
                                                      @RequestBody(required = false) Map<String, String> body,
                                                      Authentication auth) {
        requireAdmin(auth);
        String alasan = body != null ? body.get("alasan") : null;
        return ResponseEntity.ok(toPublicMap(verificationService.reject(id, alasan)));
    }

    /**
     * Never serialise the {@link User} entity directly: it carries the
     * password hash. Only the fields the admin UI needs are exposed.
     */
    static Map<String, Object> toPublicMap(User user) {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", user.getId());
        m.put("nama", user.getNama());
        m.put("email", user.getEmail());
        m.put("role", user.getRole());
        m.put("statusVerifikasi", user.getStatusVerifikasi() != null ? user.getStatusVerifikasi().name() : null);
        m.put("alasanPenolakan", user.getAlasanPenolakan());
        m.put("alamat", user.getAlamat());
        m.put("noTelp", user.getNoTelp());
        m.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        return m;
    }

    private void requireAdmin(Authentication auth) {
        if (auth == null || auth.getAuthorities().stream()
                .noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Akses ditolak: hanya admin");
        }
    }
}
