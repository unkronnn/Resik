package com.resik.admin.ui.design;

import java.util.Locale;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Design tokens for the Resik desktop admin application.
 *
 * <p>The palette follows a green "liquid glass" direction: a soft mint-to-sage canvas,
 * layered translucent white surfaces, luminous hairline edges and a deep green accent.
 * Text colours stay fully opaque so readability never depends on the translucency of the
 * surface below.</p>
 */
public final class Theme {

    /* ---------- canvas ---------- */
    public static final String CANVAS_TOP = "#F4FAF6";
    public static final String CANVAS_BOTTOM = "#DCEFE3";

    /* ---------- glass surfaces ---------- */
    public static final String GLASS = "rgba(255,255,255,0.84)";
    public static final String GLASS_CARD = "rgba(255,255,255,0.93)";
    public static final String GLASS_SOLID = "rgba(255,255,255,0.97)";
    public static final String GLASS_TABLE = "rgba(255,255,255,0.90)";
    public static final String GLASS_OUTLINE = "rgba(255,255,255,0.96)";

    /* ---------- lines ---------- */
    public static final String BORDER = "rgba(20,60,40,0.12)";
    public static final String BORDER_STRONG = "rgba(20,60,40,0.20)";

    /* ---------- text ---------- */
    public static final String TEXT = "#142B21";
    public static final String TEXT_MUTED = "#5F7A6E";
    public static final String TEXT_SUBTLE = "#8AA697";
    public static final String TEXT_ON_ACCENT = "#FFFFFF";

    /* ---------- accent ---------- */
    public static final String ACCENT = "#1A5D3A";
    public static final String ACCENT_DEEP = "#0D4E2C";
    public static final String ACCENT_LIGHT = "#2D7A4F";
    public static final String ACCENT_SOFT = "#E8F5EE";
    public static final String ACCENT_BORDER = "#C7E3D3";

    /* ---------- status ---------- */
    public static final String SUCCESS = "#059669";
    public static final String SUCCESS_SOFT = "#E7F7F0";
    public static final String SUCCESS_BORDER = "#A7E8C9";

    public static final String WARNING = "#D97706";
    public static final String WARNING_SOFT = "#FFF7E8";
    public static final String WARNING_BORDER = "#F8DFA8";

    public static final String DANGER = "#DC2626";
    public static final String DANGER_SOFT = "#FEF1F1";
    public static final String DANGER_BORDER = "#F5C6C6";

    public static final String INFO = "#2563EB";
    public static final String INFO_SOFT = "#EDF4FE";
    public static final String INFO_BORDER = "#C5D9F8";

    public static final String VIOLET = "#6D28D9";
    public static final String VIOLET_SOFT = "#F3E8FF";
    public static final String VIOLET_BORDER = "#DDD0F5";

    public static final String NEUTRAL = "#64748B";
    public static final String NEUTRAL_SOFT = "#F1F5F9";
    public static final String NEUTRAL_BORDER = "#E2E8F0";

    /* ---------- accents used by summary icon chips ---------- */
    public static final Color TINT_GREEN = Color.web("#059669");
    public static final Color TINT_SKY = Color.web("#0284C7");
    public static final Color TINT_TEAL = Color.web("#0D9488");
    public static final Color TINT_VIOLET = Color.web("#7C3AED");
    public static final Color TINT_AMBER = Color.web("#D97706");
    public static final Color TINT_ROSE = Color.web("#E11D48");
    public static final Color TINT_BLUE = Color.web("#3B82F6");
    public static final Color TINT_SLATE = Color.web("#64748B");

    /* ---------- typography ---------- */
    public static final String FAMILY = "SF Pro Display";

    public static Font font(double size) {
        return Font.font(FAMILY, FontWeight.NORMAL, size);
    }

    public static Font fontMedium(double size) {
        return Font.font(FAMILY, FontWeight.MEDIUM, size);
    }

    public static Font fontSemiBold(double size) {
        return Font.font(FAMILY, FontWeight.SEMI_BOLD, size);
    }

    public static Font fontBold(double size) {
        return Font.font(FAMILY, FontWeight.BOLD, size);
    }

    /* ---------- shape ---------- */
    public static final int RADIUS = 20;
    public static final int RADIUS_SM = 12;
    public static final int RADIUS_LG = 24;
    public static final int CONTROL_HEIGHT = 40;
    public static final int SIDEBAR_WIDTH = 244;

    private Theme() { }

    /** Returns a copy of the colour with a different alpha channel. */
    public static Color alpha(Color color, double alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /** Formats a JavaFX colour as {@code rgba(r,g,b,a)} for CSS. */
    public static String rgba(Color color, double alpha) {
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return String.format(Locale.ROOT, "rgba(%d,%d,%d,%.2f)", r, g, b, alpha);
    }

    /** Returns the solid hex representation of a JavaFX colour. */
    public static String hex(Color color) {
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return String.format(Locale.ROOT, "#%02X%02X%02X", r, g, b);
    }

    /** Resolves a status keyword to readable colours for a badge. */
    public static StatusStyle statusOf(String status) {
        if (status == null) return neutral();
        String value = status.toUpperCase(Locale.ROOT);
        return switch (value) {
            case "SELESAI", "BERHASIL", "ACTIVE", "APPROVED", "SUKSES" ->
                    new StatusStyle(SUCCESS, SUCCESS_SOFT, SUCCESS_BORDER);
            case "MENUNGGU", "PENDING", "BELUM", "MENUNGGU KONFIRMASI" ->
                    new StatusStyle(WARNING, WARNING_SOFT, WARNING_BORDER);
            case "DIJADWALKAN", "TERJADWAL", "DALAM PROSES" ->
                    new StatusStyle(INFO, INFO_SOFT, INFO_BORDER);
            case "DIPROSES" ->
                    new StatusStyle(VIOLET, VIOLET_SOFT, VIOLET_BORDER);
            case "DIBATALKAN", "REJECTED", "GAGAL", "EXPIRED", "DITOLAK" ->
                    new StatusStyle(DANGER, DANGER_SOFT, DANGER_BORDER);
            default -> neutral();
        };
    }

    public static StatusStyle neutral() {
        return new StatusStyle(NEUTRAL, NEUTRAL_SOFT, NEUTRAL_BORDER);
    }

    public record StatusStyle(String foreground, String background, String border) {
        public StatusStyle(String foreground, String background) {
            this(foreground, background, foreground);
        }
    }
}
