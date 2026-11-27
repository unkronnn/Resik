package com.resik.api.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.resik.api.entity.DeviceToken;
import com.resik.api.repository.DeviceTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

/**
 * Server-side FCM push sender.
 *
 * <p><strong>Preparation only — FCM is NOT active yet.</strong> The
 * Firebase app is initialised lazily, and only when a service-account
 * file exists at the path configured by {@code fcm.service-account-path}
 * (or the {@code FCM_SERVICE_ACCOUNT_PATH} environment variable). Until
 * the user provides that file (see {@code docs/FCM_SETUP.md}), every send
 * is a no-op: it logs a warning once and returns a "not configured"
 * result instead of throwing, so pickup / weighing flows never fail
 * because push is unavailable.
 *
 * <p>No credential file is bundled with the repo and none is fabricated:
 * the service-account JSON must come from the user's own Firebase project.
 */
@Service
public class FcmNotificationService {

    private static final Logger log = LoggerFactory.getLogger(FcmNotificationService.class);

    private final DeviceTokenRepository deviceTokenRepo;
    private final String serviceAccountPath;

    private volatile boolean initAttempted = false;
    private volatile boolean configured = false;

    public FcmNotificationService(DeviceTokenRepository deviceTokenRepo,
                                  @Value("${fcm.service-account-path:${FCM_SERVICE_ACCOUNT_PATH:}}") String serviceAccountPath) {
        this.deviceTokenRepo = deviceTokenRepo;
        this.serviceAccountPath = serviceAccountPath != null ? serviceAccountPath.trim() : "";
    }

    /** True once Firebase was successfully initialised with a service account. */
    public boolean isConfigured() {
        ensureInitialized();
        return configured;
    }

    /**
     * Sends a push notification to a single FCM registration token.
     * Never throws: when FCM is not configured, or the send fails, the
     * failure is returned as a {@link SendResult} and logged.
     */
    public SendResult sendToToken(String token, String title, String body, Map<String, String> data) {
        ensureInitialized();
        if (!configured) {
            return SendResult.notConfigured();
        }
        if (token == null || token.isBlank()) {
            return SendResult.failed("empty token");
        }
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .putAllData(data != null ? data : Map.of())
                    .build();
            String messageId = FirebaseMessaging.getInstance().send(message);
            return SendResult.sent(messageId);
        } catch (FirebaseMessagingException e) {
            log.warn("FCM send failed for token {}...: {}", token.substring(0, Math.min(12, token.length())), e.getMessage());
            return SendResult.failed(e.getMessage());
        }
    }

    /**
     * Sends a push notification to every registered device of one user
     * (used for pickup scheduled / completed and weighing completed).
     * Never throws; per-device failures are counted, not raised.
     */
    public BatchResult sendToUser(Long userId, String title, String body, Map<String, String> data) {
        ensureInitialized();
        if (!configured) {
            log.warn("FCM not configured: skipping push to user {} (title: {})", userId, title);
            return new BatchResult(0, 0, 0, false);
        }
        List<DeviceToken> tokens = deviceTokenRepo.findByUserId(userId);
        int sent = 0;
        int failed = 0;
        for (DeviceToken dt : tokens) {
            SendResult r = sendToToken(dt.getToken(), title, body, data);
            if (r.success()) {
                sent++;
            } else {
                failed++;
            }
        }
        return new BatchResult(tokens.size(), sent, failed, true);
    }

    /**
     * Lazily initialises {@link FirebaseApp} on first use, only if the
     * configured service-account file exists. Runs at most once.
     */
    private synchronized void ensureInitialized() {
        if (initAttempted) {
            return;
        }
        initAttempted = true;
        if (serviceAccountPath.isEmpty()) {
            log.warn("FCM not configured: fcm.service-account-path / FCM_SERVICE_ACCOUNT_PATH is not set. "
                    + "Push notifications are disabled; see docs/FCM_SETUP.md.");
            return;
        }
        File file = new File(serviceAccountPath);
        if (!file.isFile()) {
            log.warn("FCM not configured: service-account file not found at {}. Push notifications are disabled.", serviceAccountPath);
            return;
        }
        try (FileInputStream in = new FileInputStream(file)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(in))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            configured = true;
            log.info("FCM configured from service-account file at {}", serviceAccountPath);
        } catch (Exception e) {
            log.warn("FCM initialisation failed ({}). Push notifications are disabled.", e.getMessage());
        }
    }

    /** Outcome of a single-token send. */
    public record SendResult(boolean configured, boolean success, String messageId, String error) {
        static SendResult notConfigured() {
            return new SendResult(false, false, null, "FCM not configured");
        }

        static SendResult sent(String messageId) {
            return new SendResult(true, true, messageId, null);
        }

        static SendResult failed(String error) {
            return new SendResult(true, false, null, error);
        }
    }

    /** Outcome of a send to all of one user's devices. */
    public record BatchResult(int attempted, int sent, int failed, boolean configured) {
    }
}
