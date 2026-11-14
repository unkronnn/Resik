package com.resik.admin.ui.design;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/** Floating glass navigation with grouped entries, line icons and a single active marker. */
public final class Sidebar extends VBox {

    private final VBox items = new VBox(2);
    private final Map<String, NavItem> entries = new LinkedHashMap<>();
    private final Label statusText = new Label();
    private final Label statusDot = new Label("●");
    private String activeId;

    public Sidebar() {
        super(4);
        setPrefWidth(Theme.SIDEBAR_WIDTH);
        setMinWidth(Theme.SIDEBAR_WIDTH);
        setMaxWidth(Theme.SIDEBAR_WIDTH);
        getStyleClass().add("sidebar-panel");
        setPadding(new Insets(18, 12, 14, 12));

        getChildren().add(brand());

        items.setPadding(new Insets(4, 0, 4, 0));
        var navScroll = Ui.scroll(items);
        navScroll.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(navScroll, Priority.ALWAYS);
        getChildren().add(navScroll);

        getChildren().add(footer());
    }

    private HBox brand() {
        HBox brand = new HBox(12);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setPadding(new Insets(2, 8, 14, 8));

        StackPane logoPane = new StackPane();
        logoPane.setMinSize(40, 40);
        logoPane.setPrefSize(40, 40);
        logoPane.setMaxSize(40, 40);
        logoPane.setStyle("-fx-background-color: linear-gradient(to bottom right, "
                + Theme.ACCENT_LIGHT + ", " + Theme.ACCENT_DEEP + "); -fx-background-radius: 14;");
        var logoStream = getClass().getResourceAsStream("/resik-logo.png");
        if (logoStream != null) {
            ImageView logo = new ImageView(new Image(logoStream));
            logo.setFitWidth(24);
            logo.setFitHeight(24);
            logo.setPreserveRatio(true);
            logoPane.getChildren().add(logo);
        } else {
            Label fallback = new Label("R");
            fallback.setFont(Theme.fontBold(18));
            fallback.setTextFill(Color.WHITE);
            logoPane.getChildren().add(fallback);
        }

        VBox text = new VBox(1);
        Label title = new Label("Resik");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: " + Theme.TEXT + ";");
        Label caption = new Label("Bank Sampah Digital");
        caption.setStyle("-fx-font-size: 11px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        text.getChildren().addAll(title, caption);

        brand.getChildren().addAll(logoPane, text);
        return brand;
    }

    private HBox footer() {
        HBox chip = new HBox(8);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(9, 12, 9, 12));
        chip.setStyle("-fx-background-color: " + Theme.ACCENT_SOFT + ";"
                + " -fx-background-radius: 12;"
                + " -fx-border-color: " + Theme.ACCENT_BORDER + ";"
                + " -fx-border-width: 1;"
                + " -fx-border-radius: 12;");

        statusDot.setStyle("-fx-font-size: 9px; -fx-text-fill: " + Theme.SUCCESS + ";");
        statusText.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_MUTED + ";");

        chip.getChildren().addAll(statusDot, statusText);
        HBox wrapper = new HBox(chip);
        wrapper.setPadding(new Insets(6, 4, 0, 4));
        return wrapper;
    }

    /** Adds a small uppercase group label with a hairline divider. */
    public void addSection(String title) {
        Label label = new Label(title.toUpperCase(Locale.ROOT));
        label.getStyleClass().add("nav-section");
        label.setMaxWidth(Double.MAX_VALUE);
        items.getChildren().add(label);
    }

    /** Adds a navigation entry that calls {@code onSelect} when clicked. */
    public void addItem(String id, String label, Icon icon, Runnable onSelect) {
        NavItem item = new NavItem(label, icon);
        item.setOnMouseClicked(event -> onSelect.run());
        entries.put(id, item);
        items.getChildren().add(item);
    }

    public void select(String id) {
        activeId = id;
        entries.forEach((key, item) -> item.setActive(key.equals(id)));
    }

    public String activeId() { return activeId; }

    public void setStatus(String text, boolean online) {
        statusText.setText(text);
        statusDot.setStyle("-fx-font-size: 9px; -fx-text-fill: "
                + (online ? Theme.SUCCESS : Theme.WARNING) + ";");
    }

    /** A navigation entry that paints its own active state. */
    private static final class NavItem extends HBox {

        private final Region marker = new Region();
        private final Node iconNode;
        private final Label label;

        NavItem(String text, Icon icon) {
            super(10);
            setAlignment(Pos.CENTER_LEFT);
            setPadding(new Insets(9, 12, 9, 12));
            getStyleClass().add("nav-item");
            setMaxWidth(Double.MAX_VALUE);

            marker.setMinWidth(3);
            marker.setPrefWidth(3);
            marker.setMaxWidth(3);
            marker.setMinHeight(18);
            marker.setPrefHeight(18);
            marker.setMaxHeight(18);
            marker.setStyle("-fx-background-color: " + Theme.ACCENT + "; -fx-background-radius: 4;");
            marker.setOpacity(0);

            iconNode = icon.node(18);
            iconNode.getStyleClass().add("nav-icon");

            label = new Label(text);
            label.getStyleClass().add("nav-label");
            label.setMaxWidth(Double.MAX_VALUE);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            getChildren().addAll(marker, iconNode, label, spacer);
        }

        void setActive(boolean active) {
            getStyleClass().remove("nav-item-active");
            if (active) getStyleClass().add("nav-item-active");
            marker.setOpacity(active ? 1 : 0);
            marker.setStyle("-fx-background-color: " + (active ? "#FFFFFF" : Theme.ACCENT) + "; -fx-background-radius: 4;");
        }
    }
}
