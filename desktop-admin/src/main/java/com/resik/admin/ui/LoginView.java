package com.resik.admin.ui;

import com.resik.admin.config.AppConfig;
import com.resik.admin.service.ApiClient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
        view = new VBox();
        view.setAlignment(Pos.CENTER);
        view.setStyle("-fx-background-color: #F5F7F5;");

        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40, 36, 40, 36));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 20, 0, 0, 4);");
        card.setMaxWidth(400);
        card.setMinWidth(320);

        ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/resik-logo-large.png")));
        logoView.setFitWidth(80);
        logoView.setFitHeight(80);
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);
        StackPane logoPane = new StackPane(logoView);
        logoPane.setPadding(new Insets(0, 0, 4, 0));

        Label titleLabel = new Label("Resik");
        titleLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 26));
        titleLabel.setTextFill(Color.valueOf("#1A5D3A"));

        Label subtitleLabel = new Label("Bank Sampah Digital");
        subtitleLabel.setFont(Font.font("SF Pro Display", 14));
        subtitleLabel.setTextFill(Color.valueOf("#64748B"));

        Label roleLabel = new Label("ADMINISTRATOR");
        roleLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 11));
        roleLabel.setTextFill(Color.valueOf("#94A3B8"));
        roleLabel.setStyle("-fx-letter-spacing: 2px;");
        roleLabel.setPadding(new Insets(16, 0, 8, 0));

        Label emailLabel = new Label("Email");
        emailLabel.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 13));
        emailLabel.setTextFill(Color.valueOf("#64748B"));

        TextField emailField = new TextField();
        emailField.setPromptText("masukkan email");
        emailField.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(emailField, Priority.SOMETIMES);
        emailField.setStyle("-fx-background-color: #F8FAF8; -fx-background-radius: 14; -fx-border-color: #D1D5DB; -fx-border-width: 1px; -fx-border-radius: 14; -fx-padding: 12 16; -fx-font-size: 14px; -fx-text-fill: #1E293B;");
        emailField.focusedProperty().addListener((obs, old, isFocus) -> {
            if (isFocus) {
                emailField.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-border-color: #1A5D3A; -fx-border-width: 1.5px; -fx-border-radius: 14; -fx-padding: 12 16; -fx-font-size: 14px; -fx-text-fill: #1E293B;");
            } else {
                emailField.setStyle("-fx-background-color: #F8FAF8; -fx-background-radius: 14; -fx-border-color: #D1D5DB; -fx-border-width: 1px; -fx-border-radius: 14; -fx-padding: 12 16; -fx-font-size: 14px; -fx-text-fill: #1E293B;");
            }
        });

        Label passLabel = new Label("Password");
        passLabel.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 13));
        passLabel.setTextFill(Color.valueOf("#64748B"));
        passLabel.setPadding(new Insets(8, 0, 0, 0));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("masukkan password");
        passwordField.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(passwordField, Priority.SOMETIMES);
        passwordField.setStyle("-fx-background-color: #F8FAF8; -fx-background-radius: 14; -fx-border-color: #D1D5DB; -fx-border-width: 1px; -fx-border-radius: 14; -fx-padding: 12 16; -fx-font-size: 14px; -fx-text-fill: #1E293B;");
        passwordField.focusedProperty().addListener((obs, old, isFocus) -> {
            if (isFocus) {
                passwordField.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-border-color: #1A5D3A; -fx-border-width: 1.5px; -fx-border-radius: 14; -fx-padding: 12 16; -fx-font-size: 14px; -fx-text-fill: #1E293B;");
            } else {
                passwordField.setStyle("-fx-background-color: #F8FAF8; -fx-background-radius: 14; -fx-border-color: #D1D5DB; -fx-border-width: 1px; -fx-border-radius: 14; -fx-padding: 12 16; -fx-font-size: 14px; -fx-text-fill: #1E293B;");
            }
        });

        errorLabel = new Label();
        errorLabel.setTextFill(Color.valueOf("#DC2626"));
        errorLabel.setFont(Font.font("SF Pro Display", 12));
        errorLabel.setVisible(false);

        Button loginBtn = new Button("Masuk");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-background-color: #1A5D3A; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 14 0; -fx-background-radius: 16; -fx-font-size: 16px;");
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle("-fx-background-color: #0D4E2C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 14 0; -fx-background-radius: 16; -fx-font-size: 16px;"));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle("-fx-background-color: #1A5D3A; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 14 0; -fx-background-radius: 16; -fx-font-size: 16px;"));
        loginBtn.setOnAction(e -> handleLogin(emailField.getText(), passwordField.getText()));

        card.getChildren().addAll(
                logoPane,
                titleLabel,
                subtitleLabel,
                roleLabel,
                emailLabel, emailField,
                passLabel, passwordField,
                errorLabel,
                loginBtn
        );

        view.getChildren().add(card);
    }

    private void handleLogin(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            errorLabel.setText("Email tidak boleh kosong");
            errorLabel.setVisible(true);
            return;
        }

        if (AppConfig.DEMO_MODE) {
            AppConfig.authToken = "demo-token";
            AppConfig.userId = 1L;
            AppConfig.userRole = "ADMIN";
            AppConfig.userName = email.contains("@") ? email.split("@")[0] : "Administrator";

            Stage stage = (Stage) view.getScene().getWindow();
            DashboardView dashboard = new DashboardView();
            Scene scene = new Scene(dashboard.getView(), 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Resik Admin — " + AppConfig.userName);
            stage.setResizable(true);
            stage.setMaximized(true);
        } else {
            try {
                errorLabel.setVisible(false);
                Map<String, Object> result = ApiClient.login(email, password);
                AppConfig.authToken = (String) result.get("token");
                AppConfig.userId = ((Number) result.get("userId")).longValue();
                AppConfig.userRole = (String) result.get("role");
                AppConfig.userName = (String) result.get("nama");
                AppConfig.userEmail = email;

                Stage stage = (Stage) view.getScene().getWindow();
                DashboardView dashboard = new DashboardView();
                Scene scene = new Scene(dashboard.getView(), 1200, 700);
                scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle("Resik Admin — " + AppConfig.userName);
                stage.setResizable(true);
                stage.setMaximized(true);
            } catch (Exception e) {
                errorLabel.setText("Koneksi gagal. Pastikan server berjalan.");
                errorLabel.setVisible(true);
            }
        }
    }

    public VBox getView() {
        return view;
    }
}
