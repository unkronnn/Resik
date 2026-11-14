package com.resik.admin.ui.design;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * Paints the soft green liquid-glass canvas behind every screen: a mint-to-sage gradient
 * plus ambient radial blooms of green light that give the translucent surfaces depth.
 */
public final class GlassCanvas extends Pane {

    private final Rectangle gradient = new Rectangle();
    private final Circle bloomA = bloom(Color.web("#2D7A4F"));
    private final Circle bloomB = bloom(Color.web("#1A5D3A"));
    private final Circle bloomC = bloom(Color.web("#059669"));
    private final Circle bloomD = bloom(Color.web("#2D7A4F"));

    public GlassCanvas() {
        setMouseTransparent(true);
        gradient.setMouseTransparent(true);
        getChildren().addAll(gradient, bloomA, bloomB, bloomC, bloomD);
    }

    private static Circle bloom(Color color) {
        Circle circle = new Circle();
        circle.setMouseTransparent(true);
        circle.setFill(new RadialGradient(0, 0, 0.5, 0.5, 1, true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Theme.alpha(color, 0.14)),
                new Stop(0.55, Theme.alpha(color, 0.07)),
                new Stop(1, Theme.alpha(color, 0))));
        return circle;
    }

    @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();
        if (width <= 0 || height <= 0) return;

        gradient.setWidth(width);
        gradient.setHeight(height);
        gradient.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(Theme.CANVAS_TOP)),
                new Stop(1, Color.web(Theme.CANVAS_BOTTOM))));

        double maxDim = Math.max(width, height);
        placeBloom(bloomA, width * 0.06, height * 0.05, maxDim * 0.42);
        placeBloom(bloomB, width * 0.94, height * 0.08, maxDim * 0.40);
        placeBloom(bloomC, width * 0.42, height * 0.94, maxDim * 0.38);
        placeBloom(bloomD, width * 0.82, height * 0.72, maxDim * 0.30);
    }

    private static void placeBloom(Circle bloom, double centerX, double centerY, double radius) {
        bloom.setCenterX(centerX);
        bloom.setCenterY(centerY);
        bloom.setRadius(radius);
    }
}
