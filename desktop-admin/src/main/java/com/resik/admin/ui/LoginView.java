package com.resik.admin.ui;

import com.resik.admin.config.AppConfig;
import com.resik.admin.service.ApiClient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.Map;

public class LoginView {

    private final VBox view;
    private final Label errorLabel;

    public LoginView() {
        view = new VBox(15);
        view.setAlignment(Pos.CENTER);
        view.setPadding(new Insets(40));
        view.setStyle("-fx-background-color: #1a5d3a;");

        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 40;");
        card.setMaxWidth(320);
        card.setMaxHeight(450);

        Label logoLabel = new Label("♻️ RESIK");
        logoLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        logoLabel.setTextFill(Color.valueOf("#1a5d3a"));

        Label subtitle = new Label("Bank Sampah Digital");
        subtitle.setFont(Font.font("System", 14));
        subtitle.setTextFill(Color.GRAY);

        Label adminLabel = new Label("Admin / Petugas Login");
        adminLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        adminLabel.setTextFill(Color.valueOf("#333333"));
        adminLabel.setPadding(new Insets(10, 0, 10, 0));

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setStyle("-fx-padding: 10; -fx-background-radius: 8; -fx-border-radius: 8;");
        emailField.setPrefWidth(280);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-padding: 10; -fx-background-radius: 8; -fx-border-radius: 8;");
        passwordField.setPrefWidth(280);

        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setFont(Font.font("System", 12));
        errorLabel.setVisible(false);

        Button loginBtn = new Button("LOGIN");
        loginBtn.setStyle("-fx-background-color: #1a5d3a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 0; -fx-background-radius: 8; -fx-cursor: hand;");
        loginBtn.setPrefWidth(280);
        loginBtn.setOnAction(e -> handleLogin(emailField.getText(), passwordField.getText()));

        Label hint = new Label("Default: admin@resik.id / admin123");
        hint.setFont(Font.font("System", 10));
        hint.setTextFill(Color.LIGHTGRAY);

        card.getChildren().addAll(logoLabel, subtitle, new Separator(), adminLabel,
                emailField, passwordField, errorLabel, loginBtn, hint);

        view.getChildren().add(card);
    }

    private void handleLogin(String email, String password) {
        try {
            errorLabel.setVisible(false);
            Map<String, Object> result = ApiClient.login(email, password);

            AppConfig.authToken = (String) result.get("token");
            AppConfig.userId = ((Number) result.get("userId")).longValue();
            AppConfig.userRole = (String) result.get("role");
            AppConfig.userName = (String) result.get("nama");

            Stage stage = (Stage) view.getScene().getWindow();
            DashboardView dashboard = new DashboardView();
            Scene scene = new Scene(dashboard.getView(), 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("RESIK Admin — " + AppConfig.userName);
            stage.setResizable(true);
            stage.setMaximized(true);
        } catch (Exception e) {
            errorLabel.setText(e.getMessage().contains("Login gagal") ? e.getMessage() : "Koneksi gagal. Pastikan server berjalan.");
            errorLabel.setVisible(true);
        }
    }

    public VBox getView() {
        return view;
    }
}
