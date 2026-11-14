package com.resik.admin.ui.design;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Floating glass application bar: page scope on the left, the account on the right. */
public final class TopBar extends HBox {

    private final HBox left = new HBox(10);
    private final HBox identity = new HBox(10);
    private final HBox right = new HBox(10);

    public TopBar() {
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("topbar");
        setPadding(new Insets(12, 18, 12, 18));
        setMaxWidth(Double.MAX_VALUE);

        left.setAlignment(Pos.CENTER_LEFT);
        right.setAlignment(Pos.CENTER_RIGHT);
        identity.setAlignment(Pos.CENTER_RIGHT);

        right.getChildren().add(identity);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(left, spacer, right);
    }

    public TopBar addLeft(Node component) {
        left.getChildren().add(component);
        return this;
    }

    public TopBar addRight(Node component) {
        right.getChildren().add(component);
        return this;
    }

    /** Replaces the account chip so it follows the signed-in account after a profile edit. */
    public TopBar setIdentity(Node component) {
        identity.getChildren().setAll(component);
        return this;
    }

    /** Builds the account chip shown on the right of the bar. */
    public static HBox accountChip(String name, String role) {
        HBox chip = new HBox(10);
        chip.setAlignment(Pos.CENTER_LEFT);

        var avatar = Ui.avatar(name, 34);

        VBox text = new VBox(1);
        Label nameLabel = new Label(name == null || name.isBlank() ? "Administrator" : name);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT + ";");
        Label roleLabel = new Label(role == null || role.isBlank() ? "ADMIN" : role);
        roleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        text.getChildren().addAll(nameLabel, roleLabel);

        chip.getChildren().addAll(avatar, text);
        return chip;
    }

    /** Builds a compact logout control. */
    public static Button logoutButton(EventHandler<ActionEvent> action) {
        Button button = Ui.button("Keluar", "btn-danger-ghost", action);
        button.setTooltip(new javafx.scene.control.Tooltip("Akhiri sesi dan kembali ke layar masuk"));
        return button;
    }
}
