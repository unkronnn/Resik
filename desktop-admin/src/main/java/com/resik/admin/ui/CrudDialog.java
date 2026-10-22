package com.resik.admin.ui;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.*;

/**
 * Reusable dialog for CRUD operations (Add/Edit/Delete)
 */
public class CrudDialog {

    /**
     * Show add/edit dialog with form fields
     * Returns Map of field values, or empty if cancelled
     */
    public static Optional<Map<String, String>> showForm(String title, String submitText,
                                                          List<FormField> fields,
                                                          Map<String, String> initialValues) {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        // Build form
        VBox form = new VBox(12);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color: white;");

        Label header = new Label(title);
        header.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 16));
        header.setTextFill(Color.valueOf("#1E293B"));
        header.setPadding(new Insets(0, 0, 8, 0));

        Map<String, TextField> fieldMap = new HashMap<>();

        for (FormField field : fields) {
            VBox fieldBox = new VBox(4);
            Label label = new Label(field.label);
            label.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 12));
            label.setTextFill(Color.valueOf("#64748B"));

            TextField input = new TextField();
            input.setPromptText(field.placeholder);
            input.setStyle("-fx-background-color: #F8FAF8; -fx-background-radius: 14; -fx-border-color: #E2E8F0; -fx-border-width: 1px; -fx-border-radius: 6; -fx-padding: 8 12; -fx-font-size: 13px;");
            if (initialValues != null && initialValues.containsKey(field.key)) {
                input.setText(initialValues.get(field.key));
            }
            fieldMap.put(field.key, input);

            fieldBox.getChildren().addAll(label, input);
            form.getChildren().add(fieldBox);
        }

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setStyle("-fx-background-color: white; -fx-background-radius: 20;");

        ButtonType submitBtn = new ButtonType(submitText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtn, cancelBtn);

        // Style buttons
        dialog.getDialogPane().lookupButton(submitBtn).setStyle("-fx-background-color: #1A5D3A; -fx-text-fill: white; -fx-background-radius: 14; -fx-padding: 8 20; -fx-font-size: 13px;");
        dialog.getDialogPane().lookupButton(cancelBtn).setStyle("-fx-background-color: #F5F7F5; -fx-text-fill: #64748B; -fx-background-radius: 14; -fx-padding: 8 20; -fx-font-size: 13px;");

        dialog.setResultConverter(button -> {
            if (button == submitBtn) {
                Map<String, String> result = new HashMap<>();
                for (FormField field : fields) {
                    result.put(field.key, fieldMap.get(field.key).getText());
                }
                return result;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * Show confirmation dialog for delete
     */
    public static boolean showDeleteConfirm(String itemName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText(null);
        alert.setContentText("Yakin ingin menghapus \"" + itemName + "\"?\nTindakan ini tidak dapat dibatalkan.");

        alert.getDialogPane().setStyle("-fx-background-color: white; -fx-background-radius: 20;");
        alert.getDialogPane().lookupButton(ButtonType.OK).setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-background-radius: 14; -fx-padding: 8 20; -fx-font-size: 13px;");
        alert.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle("-fx-background-color: #F5F7F5; -fx-text-fill: #64748B; -fx-background-radius: 14; -fx-padding: 8 20; -fx-font-size: 13px;");

        return alert.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }

    /**
     * Show info toast
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle("-fx-background-color: white; -fx-background-radius: 20;");
        alert.showAndWait();
    }

    /**
     * Create a styled action button bar (Tambah, Edit, Hapus)
     */
    public static HBox createActionBar(javafx.event.EventHandler<javafx.scene.input.MouseEvent> onAdd,
                                        javafx.event.EventHandler<javafx.scene.input.MouseEvent> onEdit,
                                        javafx.event.EventHandler<javafx.scene.input.MouseEvent> onDelete) {
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(0, 0, 8, 0));

        if (onAdd != null) {
            Label addBtn = createActionLabel("Tambah", "#1A5D3A", "white", e -> onAdd.handle(e));
            bar.getChildren().add(addBtn);
        }
        if (onEdit != null) {
            Label editBtn = createActionLabel("Edit", "#3b82f6", "white", e -> onEdit.handle(e));
            bar.getChildren().add(editBtn);
        }
        if (onDelete != null) {
            Label delBtn = createActionLabel("Hapus", "#ef4444", "white", e -> onDelete.handle(e));
            bar.getChildren().add(delBtn);
        }

        return bar;
    }

    private static Label createActionLabel(String text, String bg, String fg,
                                           javafx.event.EventHandler<javafx.scene.input.MouseEvent> handler) {
        Label btn = new Label(text);
        btn.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-background-radius: 14; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: 500;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-background-radius: 14; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: 500; -fx-opacity: 0.85;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-background-radius: 14; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: 500;"));
        btn.setOnMouseClicked(handler);
        return btn;
    }

    public static class FormField {
        public final String key;
        public final String label;
        public final String placeholder;

        public FormField(String key, String label, String placeholder) {
            this.key = key;
            this.label = label;
            this.placeholder = placeholder;
        }
    }
}
