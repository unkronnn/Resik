package com.resik.admin.ui.design;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 * Monochrome 24x24 line icons (Lucide-style stroke glyphs) rendered with an
 * {@link SVGPath}. Each icon scales cleanly to the requested size and can be
 * coloured either by CSS (navigation) or by an inline stroke colour (chips).
 */
public enum Icon {

    DASHBOARD("M3 3h8v9H3zM13 3h8v5h-8zM13 12h8v9h-8zM3 16h8v5H3z"),
    PICKUP("M14 18V6a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v11a1 1 0 0 0 1 1h2"
            + " M15 18H9"
            + " M19 18h2a1 1 0 0 0 1-1v-3.65a1 1 0 0 0-.22-.62l-3.48-4.35A1 1 0 0 0 17.52 8H14"
            + " M5 18a2 2 0 1 0 4 0a2 2 0 1 0-4 0"
            + " M15 18a2 2 0 1 0 4 0a2 2 0 1 0-4 0"),
    TIMBANG("M12 3v3M7 6h10M4 21h16M6 21l2.5-8h7L18 21M12 9a2.5 2.5 0 0 0-2.5 2.5"),
    SAMPAH("M3 6h18"
            + " M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"
            + " M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"
            + " M10 11v6M14 11v6"),
    EDUKASI("M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"
            + " M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"),
    LOKASI("M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"
            + " M9 10a3 3 0 1 0 6 0a3 3 0 1 0-6 0"),
    CHAT("M7.9 20A9 9 0 1 0 4 16.1L2 22z"),
    WALLET("M21 12V7H5a2 2 0 0 1 0-4h14v4"
            + " M3 5v14a2 2 0 0 0 2 2h16v-5"
            + " M18 12a2 2 0 0 0 0 4h4v-4Z"),
    LEADERBOARD("M6 9H4.5a2.5 2.5 0 0 1 0-5H6"
            + " M18 9h1.5a2.5 2.5 0 0 0 0-5H18"
            + " M4 22h16"
            + " M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"
            + " M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"
            + " M18 2H6v7a6 6 0 0 0 12 0V2Z"),
    LAPORAN("M3 3v18h18M18 17V9M13 17V5M8 17v-3"),
    RIWAYAT("M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"
            + " M3 3v5h5M12 7v5l4 2"),
    USERS("M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"
            + " M5 7a4 4 0 1 0 8 0a4 4 0 1 0-8 0"
            + " M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"),
    PROFIL("M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2"
            + " M8 7a4 4 0 1 0 8 0a4 4 0 1 0-8 0");

    private final String path;

    Icon(String path) {
        this.path = path;
    }

    public String path() { return path; }

    /** Creates a scalable stroke icon whose colour is driven by CSS (see style.css). */
    public Node node(double size) {
        return node(size, null);
    }

    /** Creates a scalable stroke icon with an inline stroke colour. */
    public Node node(double size, Color color) {
        SVGPath glyph = new SVGPath();
        glyph.setContent(path);
        glyph.setFill(Color.TRANSPARENT);
        glyph.setStroke(color == null ? Color.web(Theme.TEXT_MUTED) : color);
        glyph.setStrokeWidth(1.9);
        glyph.setStrokeLineCap(StrokeLineCap.ROUND);
        glyph.setStrokeLineJoin(StrokeLineJoin.ROUND);
        glyph.getStyleClass().add("icon-path");

        double scale = size / 24.0;
        glyph.setScaleX(scale);
        glyph.setScaleY(scale);

        StackPane host = new StackPane(glyph);
        host.setMinSize(size, size);
        host.setPrefSize(size, size);
        host.setMaxSize(size, size);
        host.getStyleClass().add("icon-host");
        return host;
    }
}
