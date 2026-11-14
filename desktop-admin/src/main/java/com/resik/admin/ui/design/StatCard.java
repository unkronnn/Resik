package com.resik.admin.ui.design;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * A frosted summary tile with a caption, a large value and a quiet hint. A small tinted icon
 * chip carries the semantic colour of the metric.
 */
public final class StatCard extends VBox {

    private final Label value;
    private final Label hint;

    public StatCard(String caption, String initialValue) {
        this(caption, initialValue, Icon.DASHBOARD, Theme.TINT_GREEN);
    }

    public StatCard(String caption, String initialValue, Icon icon, Color tint) {
        super(10);
        setPadding(new Insets(18));
        getStyleClass().add("glass-card");

        HBox head = new HBox(10);
        head.setAlignment(Pos.CENTER_LEFT);

        StackPane chip = new StackPane(icon.node(16, Color.web(Theme.TEXT_MUTED)));
        chip.setMinSize(36, 36);
        chip.setPrefSize(36, 36);
        chip.setMaxSize(36, 36);
        chip.setStyle("-fx-background-color: #F0F2F0;"
                + " -fx-background-radius: 10;"
                + " -fx-border-color: #E5E7EB;"
                + " -fx-border-width: 1;"
                + " -fx-border-radius: 10;");

        Label captionLabel = Ui.muted(caption);
        captionLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        head.getChildren().addAll(chip, captionLabel);

        value = new Label(initialValue == null ? "—" : initialValue);
        value.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: " + Theme.TEXT + ";");

        hint = Ui.subtle("");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: " + Theme.TEXT_SUBTLE + ";");
        hint.setVisible(false);
        hint.setManaged(false);

        getChildren().addAll(head, value, hint);
    }

    public void setValue(String text) {
        value.setText(text == null || text.isBlank() ? "—" : text);
    }

    public void setHint(String text) {
        hint.setText(text == null ? "" : text);
        boolean visible = !hint.getText().isEmpty();
        hint.setVisible(visible);
        hint.setManaged(visible);
    }
}
