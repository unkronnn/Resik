package com.resik.api.websocket;

import com.resik.api.entity.Penjemputan;
import com.resik.api.entity.Sampah;
import com.resik.api.entity.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Publishes domain events to the realtime channel ({@code /ws/events}).
 *
 * <p>Services and controllers call the typed methods here instead of talking
 * to {@link RealtimeEventHandler} directly, so the routing rules (who
 * receives what) live in one place. Publishing never throws: a broadcast
 * failure is logged by the handler and must not roll back the business
 * transaction that triggered it.
 */
@Service
public class RealtimeEventPublisher {

    private final RealtimeEventHandler handler;

    public RealtimeEventPublisher(RealtimeEventHandler handler) {
        this.handler = handler;
    }

    /** A masyarakat created a new pickup request. Admins and bank sampah see it appear. */
    public void penjemputanCreated(Penjemputan p) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("penjemputanId", p.getId());
        payload.put("userId", p.getUser() != null ? p.getUser().getId() : null);
        payload.put("tanggalJemput", p.getTanggalJemput() != null ? p.getTanggalJemput().toString() : null);
        payload.put("waktuJemput", p.getWaktuJemput());
        payload.put("alamat", p.getAlamat());
        payload.put("status", p.getStatus() != null ? p.getStatus().name() : null);
        handler.broadcastToAdminsAndBankSampah(
                RealtimeEvent.of(RealtimeEvent.Types.PENJEMPUTAN_CREATED, payload));
    }

    /**
     * A pickup changed status (MENUNGGU / DIJADWALKAN / DIPROSES / SELESAI /
     * DIBATALKAN). Admins, bank sampah, and the owning masyarakat are notified.
     */
    public void penjemputanStatusChanged(Penjemputan p, String statusLama) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("penjemputanId", p.getId());
        payload.put("userId", p.getUser() != null ? p.getUser().getId() : null);
        payload.put("petugasId", p.getPetugas() != null ? p.getPetugas().getId() : null);
        payload.put("status", p.getStatus() != null ? p.getStatus().name() : null);
        payload.put("statusLama", statusLama);
        payload.put("tanggalJemput", p.getTanggalJemput() != null ? p.getTanggalJemput().toString() : null);
        Long userId = p.getUser() != null ? p.getUser().getId() : null;
        handler.broadcastToAdminsBankSampahAndUser(userId,
                RealtimeEvent.of(RealtimeEvent.Types.PENJEMPUTAN_STATUS, payload));
    }

    /**
     * A user's wallet changed. {@code alasan} is one of TIMBANG, TOPUP, TARIK.
     * Only the owner and admins are notified.
     */
    public void walletUpdated(User user, String alasan, BigDecimal perubahan) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", user.getId());
        payload.put("saldoBaru", user.getSaldoDompet());
        payload.put("poinBaru", user.getPoinReward());
        payload.put("perubahan", perubahan);
        payload.put("alasan", alasan);
        handler.broadcastToAdminsAndUser(user.getId(),
                RealtimeEvent.of(RealtimeEvent.Types.WALLET_UPDATED, payload));
    }

    /** A new waste type was added. Everyone sees it in the price list. */
    public void sampahCreated(Sampah s) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sampahId", s.getId());
        payload.put("kategori", s.getKategori());
        payload.put("nama", s.getNama());
        payload.put("hargaPerKg", s.getHargaPerKg());
        handler.broadcastToAll(
                RealtimeEvent.of(RealtimeEvent.Types.SAMPAH_CREATED, payload));
    }

    /** A waste price (or another field) changed. Everyone sees the new price. */
    public void hargaSampahUpdated(Sampah s, BigDecimal hargaLama) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sampahId", s.getId());
        payload.put("kategori", s.getKategori());
        payload.put("nama", s.getNama());
        payload.put("hargaPerKg", s.getHargaPerKg());
        payload.put("hargaLama", hargaLama);
        handler.broadcastToAll(
                RealtimeEvent.of(RealtimeEvent.Types.HARGA_SAMPAH_UPDATED, payload));
    }

    /** A waste type was deleted. Everyone drops it from the price list. */
    public void sampahDeleted(Long sampahId, String nama) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sampahId", sampahId);
        payload.put("nama", nama);
        handler.broadcastToAll(
                RealtimeEvent.of(RealtimeEvent.Types.SAMPAH_DELETED, payload));
    }

    /**
     * A new account registered and awaits verification by an admin (the
     * desktop Verifikasi &amp; Approval flow). Admins only.
     */
    public void accountVerification(User user) {
        accountVerification(user, "MENUNGGU", null);
    }

    /**
     * An admin decided on an account (approve / reject). Same event type as
     * registration, with the new status in the payload so the desktop
     * verification table can update the row in place instead of re-fetching.
     * Admins only, plus the affected user.
     */
    public void accountVerificationDecided(User user, String alasan) {
        accountVerification(user, user.getStatusVerifikasi() != null
                ? user.getStatusVerifikasi().name() : null, alasan);
    }

    private void accountVerification(User user, String status, String alasan) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", user.getId());
        payload.put("nama", user.getNama());
        payload.put("email", user.getEmail());
        payload.put("role", user.getRole());
        payload.put("status", status);
        if (alasan != null) {
            payload.put("alasan", alasan);
        }
        RealtimeEvent event = RealtimeEvent.of(RealtimeEvent.Types.ACCOUNT_VERIFICATION, payload);
        handler.broadcastToAdmins(event);
        if (user.getId() != null) {
            handler.broadcastToUser(user.getId(), event);
        }
    }

    /**
     * Weighed stock changed after a timbang. {@code rincian} is a list of
     * maps with {@code kategori} and {@code beratKg}. Admins and bank sampah
     * update their stock views.
     */
    public void stokUpdated(Long penjemputanId, List<Map<String, Object>> rincian) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("penjemputanId", penjemputanId);
        payload.put("items", rincian != null ? rincian : List.of());
        handler.broadcastToAdminsAndBankSampah(
                RealtimeEvent.of(RealtimeEvent.Types.STOK_UPDATED, payload));
    }

    /** A user's points changed, so the leaderboard order may change. Everyone re-fetches. */
    public void leaderboardUpdated(User user) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", user.getId());
        payload.put("poinBaru", user.getPoinReward());
        handler.broadcastToAll(
                RealtimeEvent.of(RealtimeEvent.Types.LEADERBOARD_UPDATED, payload));
    }

    /** Builds one {kategori, beratKg} stock line from weighed details. */
    public static List<Map<String, Object>> stokLines(Map<String, BigDecimal> beratPerKategori) {
        List<Map<String, Object>> items = new ArrayList<>();
        beratPerKategori.forEach((kategori, berat) -> {
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("kategori", kategori);
            line.put("beratKg", berat);
            items.add(line);
        });
        return items;
    }
}
