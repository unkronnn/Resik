package com.resik.admin.ui;

import com.resik.admin.config.AppConfig;
import com.resik.admin.service.ApiClient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.Map;

public class LoginView {

    private final VBox view;
    private final Label errorLabel;

    public LoginView() {
        view = new VBox();
        view.setAlignment(Pos.CENTER);
        view.setStyle("-fx-background-color: #0d6e4e;");

        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(48, 44, 48, 44));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 8);");
        card.setMaxWidth(360);

        Circle logoCircle = new Circle(32);
        logoCircle.setFill(Color.valueOf("#0d6e4e"));
        Text logoText = new Text("R");
        logoText.setFill(Color.WHITE);
        logoText.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 28));
        StackPane logoPane = new StackPane(logoCircle, logoText);
        logoPane.setPadding(new Insets(0, 0, 8, 0));

        Label titleLabel = new Label("RESIK");
        titleLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.valueOf("#0d6e4e"));

        Label subtitleLabel = new Label("Bank Sampah Digital");
        subtitleLabel.setFont(Font.font("SF Pro Display", 14));
        subtitleLabel.setTextFill(Color.valueOf("#94a3b8"));

        Label roleLabel = new Label("ADMINISTRATOR");
        roleLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 11));
        roleLabel.setTextFill(Color.valueOf("#64748b"));
        roleLabel.setStyle("-fx-letter-spacing: 2px;");
        roleLabel.setPadding(new Insets(16, 0, 4, 0));

        Label emailLabel = new Label("Email");
        emailLabel.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 12));
        emailLabel.setTextFill(Color.valueOf("#475569"));

        TextField emailField = new TextField();
        emailField.setPromptText("Masukkan email");
        emailField.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
        emailField.setPrefWidth(280);
        emailField.focusedProperty().addListener((obs, old, isFocus) -> {
            if (isFocus) {
                emailField.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #0d6e4e; -fx-border-width: 1.5px; -fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
            } else {
                emailField.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
            }
        });

        Label passLabel = new Label("Password");
        passLabel.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 12));
        passLabel.setTextFill(Color.valueOf("#475569"));
        passLabel.setPadding(new Insets(8, 0, 0, 0));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Masukkan password");
        passwordField.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
        passwordField.setPrefWidth(280);
        passwordField.focusedProperty().addListener((obs, old, isFocus) -> {
            if (isFocus) {
                passwordField.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #0d6e4e; -fx-border-width: 1.5px; -fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
            } else {
                passwordField.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px;");
            }
        });

        errorLabel = new Label();
        errorLabel.setTextFill(Color.valueOf("#dc2626"));
        errorLabel.setFont(Font.font("SF Pro Display", 12));
        errorLabel.setVisible(false);

        Button loginBtn = new Button("MASUK");
        loginBtn.setStyle("-fx-background-color: #0d6e4e; -fx-text-fill: white; -fx-font-weight: 600; -fx-padding: 12 0; -fx-background-radius: 8; -fx-font-size: 14px; -fx-letter-spacing: 1px;");
        loginBtn.setPrefWidth(280);
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle("-fx-background-color: #0a5840; -fx-text-fill: white; -fx-font-weight: 600; -fx-padding: 12 0; -fx-background-radius: 8; -fx-font-size: 14px; -fx-letter-spacing: 1px;"));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle("-fx-background-color: #0d6e4e; -fx-text-fill: white; -fx-font-weight: 600; -fx-padding: 12 0; -fx-background-radius: 8; -fx-font-size: 14px; -fx-letter-spacing: 1px;"));
        loginBtn.setOnAction(e -> handleLogin(emailField.getText(), passwordField.getText()));

        Label hint = new Label("Mode Demo — Masukkan email apa pun untuk masuk");
        hint.setFont(Font.font("SF Pro Display", 11));
        hint.setTextFill(Color.valueOf("#94a3b8"));

        card.getChildren().addAll(
                logoPane,
                titleLabel,
                subtitleLabel,
                new Separator(),
                roleLabel,
                emailLabel, emailField,
                passLabel, passwordField,
                errorLabel,
                loginBtn,
                hint
        );

        view.getChildren().add(card);
    }

    private void handleLogin(String email, String password) {
        if (AppConfig.DEMO_MODE) {
            if (email == null || email.trim().isEmpty()) {
                errorLabel.setText("Email tidak boleh kosong");
                errorLabel.setVisible(true);
                return;
            }
            AppConfig.authToken = "demo-token";
            AppConfig.userId = 1L;
            AppConfig.userRole = "ADMIN";
            AppConfig.userName = email.contains("@") ? email.split("@")[0] : "Administrator";

            Stage stage = (Stage) view.getScene().getWindow();
            DashboardView dashboard = new DashboardView();
            Scene scene = new Scene(dashboard.getView(), 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("RESIK Admin — " + AppConfig.userName);
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

                Stage stage = (Stage) view.getScene().getWindow();
                DashboardView dashboard = new DashboardView();
                Scene scene = new Scene(dashboard.getView(), 1200, 700);
                scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle("RESIK Admin — " + AppConfig.userName);
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
