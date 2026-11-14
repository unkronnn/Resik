package com.resik.admin.ui.design;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/** Factory methods for the shared Resik components. */
public final class Ui {

    private Ui() { }

    /* ---------- surfaces ---------- */

    public static VBox card() {
        return card("glass-card");
    }

    public static VBox cardStrong() {
        return card("glass-card-strong");
    }

    public static VBox card(String styleClass) {
        VBox box = new VBox(14);
        box.setPadding(new Insets(20));
        box.getStyleClass().add(styleClass);
        return box;
    }

    /* ---------- layout helpers ---------- */

    public static HBox row() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    public static HBox row(double spacing) {
        HBox box = new HBox(spacing);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    /** A frosted horizontal row used for list entries inside cards. */
    public static HBox rowCard() {
        return rowCard(20);
    }

    public static HBox rowCard(double spacing) {
        HBox box = new HBox(spacing);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(14, 18, 14, 18));
        box.getStyleClass().add("glass-card");
        return box;
    }

    public static Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    public static Region vSpacer() {
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    /* ---------- text ---------- */

    public static Label title(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("page-title");
        return label;
    }

    public static Label subtitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("page-subtitle");
        return label;
    }

    public static Label eyebrow(String text) {
        Label label = new Label(text == null ? "" : text.toUpperCase(java.util.Locale.ROOT));
        label.getStyleClass().add("page-eyebrow");
        return label;
    }

    public static Label heading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("card-title");
        return label;
    }

    public static Label muted(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("label-muted");
        return label;
    }

    public static Label subtle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("label-subtle");
        return label;
    }

    /** The heading block used at the top of a card. */
    public static VBox cardHeader(String title, String description) {
        VBox head = new VBox(3);
        head.getChildren().add(heading(title));
        if (description != null && !description.isBlank()) {
            head.getChildren().add(subtle(description));
        }
        return head;
    }

    /* ---------- controls ---------- */

    public static Button button(String text, String variant, EventHandler<ActionEvent> action) {
        Button button = new Button(text);
        button.getStyleClass().addAll("btn", variant);
        if (action != null) button.setOnAction(action);
        return button;
    }

    public static Button button(String text, String variant) {
        return button(text, variant, null);
    }

    public static TextField textField() {
        TextField field = new TextField();
        field.getStyleClass().add("input");
        return field;
    }

    public static TextField textField(String prompt) {
        TextField field = textField();
        field.setPromptText(prompt);
        return field;
    }

    public static PasswordField passwordField(String prompt) {
        PasswordField field = new PasswordField();
        field.getStyleClass().add("input");
        field.setPromptText(prompt);
        return field;
    }

    public static <T> ComboBox<T> comboBox() {
        ComboBox<T> combo = new ComboBox<>();
        combo.setPrefHeight(Theme.CONTROL_HEIGHT);
        combo.setStyle("-fx-background-color: rgba(255,255,255,0.94); -fx-background-radius: 14;"
                + " -fx-border-color: rgba(20,60,40,0.16); -fx-border-width: 1; -fx-border-radius: 14;"
                + " -fx-padding: 6 12; -fx-font-size: 13px; -fx-text-fill: " + Theme.TEXT + ";");
        return combo;
    }

    public static DatePicker datePicker() {
        DatePicker picker = new DatePicker();
        picker.setPrefHeight(Theme.CONTROL_HEIGHT);
        picker.setStyle("-fx-background-color: rgba(255,255,255,0.94); -fx-background-radius: 14;"
                + " -fx-border-color: rgba(20,60,40,0.16); -fx-border-width: 1; -fx-border-radius: 14;"
                + " -fx-padding: 6 12; -fx-font-size: 13px;");
        return picker;
    }

    public static ScrollPane scroll(Parent content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.getStyleClass().add("scroll-glass");
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scroll;
    }

    public static ScrollPane scrollTransparent(Parent content) {
        return scroll(content);
    }

    public static <T> TableView<T> table() {
        TableView<T> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("table-glass");
        return table;
    }

    /* ---------- badges & status ---------- */

    public static Label badge(String text, Theme.StatusStyle style) {
        Label badge = new Label(text == null ? "" : text);
        badge.getStyleClass().add("badge");
        badge.setStyle("-fx-background-color: " + style.background() + ";"
                + " -fx-text-fill: " + style.foreground() + ";"
                + " -fx-background-radius: 10;"
                + " -fx-border-color: " + style.border() + ";"
                + " -fx-border-width: 1;"
                + " -fx-border-radius: 10;"
                + " -fx-padding: 4 10;"
                + " -fx-font-size: 10px;"
                + " -fx-font-weight: 700;");
        return badge;
    }

    public static Label statusBadge(String status) {
        return badge(status, Theme.statusOf(status));
    }

    public static Label badge(String text, String foreground, String background, String border) {
        return badge(text, new Theme.StatusStyle(foreground, background, border));
    }

    /* ---------- avatar ---------- */

    public static StackPane avatar(String name, double size) {
        Circle circle = new Circle(size / 2);
        circle.setFill(Color.web(Theme.ACCENT_LIGHT));
        Label initials = new Label(initials(name));
        initials.setFont(Theme.fontBold(size * 0.42));
        initials.setTextFill(Color.WHITE);
        StackPane pane = new StackPane(circle, initials);
        pane.setMinSize(size, size);
        pane.setPrefSize(size, size);
        pane.setMaxSize(size, size);
        return pane;
    }

    /* ---------- empty state ---------- */

    public static VBox emptyState(String title, String description) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(48, 24, 48, 24));

        StackPane glyph = new StackPane(Icon.PROFIL.node(26, Color.web(Theme.ACCENT_LIGHT)));
        glyph.setMinSize(54, 54);
        glyph.setPrefSize(54, 54);
        glyph.setMaxSize(54, 54);
        glyph.setStyle("-fx-background-color: " + Theme.ACCENT_SOFT + "; -fx-background-radius: 18;");

        Label titleLabel = heading(title);
        titleLabel.getStyleClass().remove("card-title");
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        Label descLabel = subtle(description);
        descLabel.setWrapText(true);
        descLabel.setAlignment(Pos.CENTER);

        box.getChildren().addAll(glyph, titleLabel, descLabel);
        return box;
    }

    /* ---------- helpers ---------- */

    public static String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase(java.util.Locale.ROOT);
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1))
                .toUpperCase(java.util.Locale.ROOT);
    }

    public static void grow(Node node, Priority priority) {
        HBox.setHgrow(node, priority);
    }
}
