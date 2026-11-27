package com.resik.client.api

/**
 * One event from the backend realtime channel ({@code /ws/events}).
 *
 * Wire format (same envelope as the chat channel, with a payload object):
 * ```
 * {"type":"PENJEMPUTAN_STATUS","timestamp":"2026-11-26T14:55:00.123Z",
 *  "payload":{"penjemputanId":12,"userId":7,"status":"SELESAI","statusLama":"DIPROSES"}}
 * ```
 *
 * Unknown types must be ignored so the server can add new event types
 * without breaking older app versions.
 */
data class RealtimeEvent(
    val type: String,
    val timestamp: String?,
    val payload: Map<String, Any?>
) {
    @Suppress("UNCHECKED_CAST")
    fun payloadString(key: String): String? = payload[key]?.toString()

    fun payloadLong(key: String): Long? = when (val v = payload[key]) {
        is Number -> v.toLong()
        is String -> v.toLongOrNull()
        else -> null
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any>): RealtimeEvent? {
            val type = map["type"]?.toString() ?: return null
            val payload = (map["payload"] as? Map<String, Any>) ?: emptyMap()
            return RealtimeEvent(type, map["timestamp"]?.toString(), payload)
        }
    }
}

/** Event type constants. Keep in sync with the backend RealtimeEvent.Types. */
object RealtimeEventType {
    /** A new pickup request was created. */
    const val PENJEMPUTAN_CREATED = "PENJEMPUTAN_CREATED"
    /** A pickup changed status (MENUNGGU/DIJADWALKAN/DIPROSES/SELESAI/DIBATALKAN). */
    const val PENJEMPUTAN_STATUS = "PENJEMPUTAN_STATUS"
    /** A user's wallet balance / points changed. */
    const val WALLET_UPDATED = "WALLET_UPDATED"
    /** A waste type was created. */
    const val SAMPAH_CREATED = "SAMPAH_CREATED"
    /** A waste price changed. */
    const val HARGA_SAMPAH_UPDATED = "HARGA_SAMPAH_UPDATED"
    /** A waste type was deleted. */
    const val SAMPAH_DELETED = "SAMPAH_DELETED"
    /** A new account registered and awaits verification. */
    const val ACCOUNT_VERIFICATION = "ACCOUNT_VERIFICATION"
    /** Weighed stock changed after a timbang. */
    const val STOK_UPDATED = "STOK_UPDATED"
    /** A user's points changed, so the leaderboard order may change. */
    const val LEADERBOARD_UPDATED = "LEADERBOARD_UPDATED"
}
