package com.resik.api.websocket;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Envelope for all realtime events broadcast over {@code /ws/events}.
 *
 * <p>Every event is a JSON object with exactly three top-level fields:
 *
 * <pre>{@code
 * {
 *   "type": "PENJEMPUTAN_STATUS",
 *   "timestamp": "2026-11-26T14:55:00.123Z",
 *   "payload": { "penjemputanId": 12, "status": "SELESAI", ... }
 * }
 * }</pre>
 *
 * <p>Clients subscribe to the channel and switch on {@code type}. Unknown
 * types must be ignored so the server can add new event types without
 * breaking older clients.
 */
public class RealtimeEvent {

    /** Event type constants. Keep in sync with the clients. */
    public static final class Types {
        /** A new pickup request was created (payload: penjemputanId, userId, tanggalJemput). */
        public static final String PENJEMPUTAN_CREATED = "PENJEMPUTAN_CREATED";
        /** A pickup changed status (payload: penjemputanId, userId, petugasId, status, statusLama). */
        public static final String PENJEMPUTAN_STATUS = "PENJEMPUTAN_STATUS";
        /** A user's wallet balance / points changed (payload: userId, saldoBaru, poinBaru, perubahan, alasan). */
        public static final String WALLET_UPDATED = "WALLET_UPDATED";
        /** A waste type was created (payload: sampahId, kategori, nama, hargaPerKg). */
        public static final String SAMPAH_CREATED = "SAMPAH_CREATED";
        /** A waste price (or other field) changed (payload: sampahId, kategori, nama, hargaPerKg, hargaLama). */
        public static final String HARGA_SAMPAH_UPDATED = "HARGA_SAMPAH_UPDATED";
        /** A waste type was deleted (payload: sampahId, nama). */
        public static final String SAMPAH_DELETED = "SAMPAH_DELETED";
        /** A new account registered and awaits verification (payload: userId, nama, email, role, status). */
        public static final String ACCOUNT_VERIFICATION = "ACCOUNT_VERIFICATION";
        /** Weighed stock changed after a timbang (payload: penjemputanId, items: [{kategori, beratKg}]). */
        public static final String STOK_UPDATED = "STOK_UPDATED";
        /** A user's points changed, so the leaderboard order may change (payload: userId, poinBaru). */
        public static final String LEADERBOARD_UPDATED = "LEADERBOARD_UPDATED";

        private Types() {
        }
    }

    private final String type;
    private final String timestamp;
    private final Map<String, Object> payload;

    public RealtimeEvent(String type, Map<String, Object> payload) {
        this.type = type;
        this.timestamp = Instant.now().toString();
        this.payload = payload != null ? payload : Map.of();
    }

    public String getType() {
        return type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    /** Serializes this event to the wire format: {type, timestamp, payload}. */
    public Map<String, Object> toWireMap() {
        Map<String, Object> wire = new LinkedHashMap<>();
        wire.put("type", type);
        wire.put("timestamp", timestamp);
        wire.put("payload", payload);
        return wire;
    }

    public static RealtimeEvent of(String type, Map<String, Object> payload) {
        return new RealtimeEvent(type, payload);
    }
}
