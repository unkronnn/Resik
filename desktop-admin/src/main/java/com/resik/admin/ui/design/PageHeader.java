package com.resik.admin.ui.design;

import javafx.scene.layout.VBox;

/** Page title, supporting description and an optional eyebrow label. */
public final class PageHeader extends VBox {

    private final javafx.scene.control.Label eyebrow = Ui.eyebrow("");
    private final javafx.scene.control.Label heading;
    private final javafx.scene.control.Label description;

    public PageHeader(String title, String description) {
        this(title, description, null);
    }

    public PageHeader(String title, String description, String eyebrowText) {
        super(4);
        heading = Ui.title(title == null ? "" : title);
        this.description = Ui.subtitle(description == null ? "" : description);
        eyebrow.setVisible(false);
        eyebrow.setManaged(false);
        getChildren().addAll(eyebrow, heading, this.description);
        if (eyebrowText != null && !eyebrowText.isBlank()) setEyebrow(eyebrowText);
    }

    public void setTitle(String text) {
        heading.setText(text == null ? "" : text);
    }

    public void setDescription(String text) {
        description.setText(text == null ? "" : text);
    }

    public void setEyebrow(String text) {
        String value = text == null ? "" : text.trim();
        eyebrow.setText(value);
        eyebrow.setVisible(!value.isEmpty());
        eyebrow.setManaged(!value.isEmpty());
    }
}
