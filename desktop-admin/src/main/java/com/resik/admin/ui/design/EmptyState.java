package com.resik.admin.ui.design;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** A soft empty-state block shown when a page or card has no data to display. */
public final class EmptyState extends VBox {

    public EmptyState(String title, String description) {
        super(10);
        setAlignment(Pos.CENTER);
        setPadding(new javafx.geometry.Insets(40, 20, 40, 20));

        StackPane glyph = new StackPane(Icon.SAMPAH.node(26, javafx.scene.paint.Color.web(Theme.ACCENT_LIGHT)));
        glyph.setMinSize(54, 54);
        glyph.setPrefSize(54, 54);
        glyph.setMaxSize(54, 54);
        glyph.setStyle("-fx-background-color: " + Theme.ACCENT_SOFT + "; -fx-background-radius: 18;");

        Label titleLabel = new Label(title == null ? "" : title);
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT + ";");
        Label descLabel = new Label(description == null ? "" : description);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.TEXT_SUBTLE + ";");
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setMaxWidth(420);

        getChildren().addAll(glyph, titleLabel, descLabel);
    }
}
