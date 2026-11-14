package com.resik.admin.ui;

import com.resik.admin.ui.design.Theme;
import com.resik.admin.ui.design.Ui;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Reusable glass-styled dialogs for CRUD operations. */
public class CrudDialog {

    public static Optional<Map<String, String>> showForm(String title, String submitText,
                                                          List<FormField> fields,
                                                          Map<String, String> initialValues) {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStyleClass().add("glass-dialog");

        VBox form = new VBox(12);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color: transparent;");

        Label header = new Label(title);
        header.setStyle("-fx-font-size: 17px; -fx-font-weight: 800; -fx-text-fill: " + Theme.TEXT + ";");
        header.setPadding(new Insets(0, 0, 8, 0));

        Map<String, TextField> fieldMap = new HashMap<>();

        for (FormField field : fields) {
            VBox fieldBox = new VBox(5);
            Label label = new Label(field.label);
            label.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_MUTED + ";");

            TextField input = Ui.textField(field.placeholder);
            if (initialValues != null && initialValues.containsKey(field.key)) {
                input.setText(initialValues.get(field.key));
            }
            fieldMap.put(field.key, input);

            fieldBox.getChildren().addAll(label, input);
            form.getChildren().add(fieldBox);
        }

        dialog.getDialogPane().setContent(form);

        ButtonType submitBtn = new ButtonType(submitText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtn, cancelBtn);

        Button submit = (Button) dialog.getDialogPane().lookupButton(submitBtn);
        Button cancel = (Button) dialog.getDialogPane().lookupButton(cancelBtn);
        if (submit != null) submit.getStyleClass().addAll("btn", "btn-primary");
        if (cancel != null) cancel.getStyleClass().addAll("btn", "btn-ghost");

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

    public static boolean showDeleteConfirm(String itemName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText(null);
        alert.setContentText("Yakin ingin menghapus \"" + itemName + "\"?\nTindakan ini tidak dapat dibatalkan.");
        alert.getDialogPane().getStyleClass().add("glass-dialog");

        Button ok = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        Button cancel = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (ok != null) ok.getStyleClass().addAll("btn", "btn-danger");
        if (cancel != null) cancel.getStyleClass().addAll("btn", "btn-ghost");

        return alert.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("glass-dialog");

        Button ok = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (ok != null) ok.getStyleClass().addAll("btn", "btn-primary");
        alert.showAndWait();
    }

    /** Creates a styled action button bar. */
    public static HBox createActionBar(EventHandler<ActionEvent> onAdd,
                                        EventHandler<ActionEvent> onEdit,
                                        EventHandler<ActionEvent> onDelete) {
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(0, 0, 8, 0));

        if (onAdd != null) bar.getChildren().add(Ui.button("Tambah", "btn-primary", onAdd));
        if (onEdit != null) bar.getChildren().add(Ui.button("Edit", "btn-blue", onEdit));
        if (onDelete != null) bar.getChildren().add(Ui.button("Hapus", "btn-danger", onDelete));
        return bar;
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
