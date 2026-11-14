package com.resik.admin.ui;

import com.resik.admin.config.AppConfig;
import com.resik.admin.service.ApiClient;
import com.resik.admin.ui.design.GlassCanvas;
import com.resik.admin.ui.design.Theme;
import com.resik.admin.ui.design.Ui;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.collections.FXCollections;

import java.util.Map;

/** Liquid-glass login screen: a frosted card floating on the green ambient canvas. */
public class LoginView {

    private final StackPane view;
    private final Label errorLabel;
    private final ComboBox<String> roleCombo;

    public LoginView() {
        view = new StackPane();
        view.getChildren().add(new GlassCanvas());

        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(38, 36, 38, 36));
        card.setMaxWidth(400);
        card.setMinWidth(340);
        card.getStyleClass().add("glass-card-strong");

        StackPane logoPane = new StackPane();
        logoPane.setMinSize(84, 84);
        logoPane.setPrefSize(84, 84);
        logoPane.setMaxSize(84, 84);
        logoPane.setStyle("-fx-background-color: linear-gradient(to bottom right, " + Theme.ACCENT_LIGHT + ", " + Theme.ACCENT_DEEP + ");"
                + " -fx-background-radius: 24;");
        var logoStream = getClass().getResourceAsStream("/resik-logo-large.png");
        if (logoStream != null) {
            ImageView logoView = new ImageView(new Image(logoStream));
            logoView.setFitWidth(54);
            logoView.setFitHeight(54);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
            logoPane.getChildren().add(logoView);
        } else {
            Label fallback = new Label("R");
            fallback.setFont(Theme.fontBold(36));
            fallback.setTextFill(Color.WHITE);
            logoPane.getChildren().add(fallback);
        }

        Label titleLabel = new Label("Resik");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: 800; -fx-text-fill: " + Theme.ACCENT + ";");

        Label subtitleLabel = new Label("Bank Sampah Digital");
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");

        Label roleLabel = new Label("PILIH ROLE");
        roleLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 800; -fx-text-fill: " + Theme.TEXT_SUBTLE + ";");
        roleLabel.setPadding(new Insets(14, 0, 8, 0));

        roleCombo = new ComboBox<>(FXCollections.observableArrayList("ADMIN", "BANK_SAMPAH"));
        roleCombo.setValue("ADMIN");
        roleCombo.setMaxWidth(Double.MAX_VALUE);
        roleCombo.getStyleClass().add("glass-combo");

        Label emailLabel = new Label("Email");
        emailLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_MUTED + ";");

        TextField emailField = Ui.textField("masukkan email");
        emailField.setMaxWidth(Double.MAX_VALUE);

        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        passLabel.setPadding(new Insets(8, 0, 0, 0));

        PasswordField passwordField = Ui.passwordField("masukkan password");
        passwordField.setMaxWidth(Double.MAX_VALUE);

        errorLabel = new Label();
        errorLabel.setTextFill(Color.web(Theme.DANGER));
        errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.DANGER + ";");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Button loginBtn = Ui.button("Masuk", "btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(loginBtn.getStyle() + " -fx-padding: 13 0; -fx-font-size: 15px;");
        loginBtn.setOnAction(e -> handleLogin(emailField.getText(), passwordField.getText()));

        card.getChildren().addAll(
                logoPane,
                titleLabel,
                subtitleLabel,
                roleLabel,
                roleCombo,
                emailLabel, emailField,
                passLabel, passwordField,
                errorLabel,
                loginBtn
        );

        view.getChildren().add(card);
        view.setStyle("-fx-background-color: linear-gradient(to bottom right, " + Theme.CANVAS_TOP + ", " + Theme.CANVAS_BOTTOM + ");");
    }

    private void handleLogin(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            showError("Email tidak boleh kosong");
            return;
        }

        if (AppConfig.DEMO_MODE) {
            AppConfig.authToken = "demo-token";
            AppConfig.userId = 1L;
            AppConfig.userRole = roleCombo.getValue() == null ? "ADMIN" : roleCombo.getValue();
            String selectedRole = AppConfig.userRole;
            if ("BANK_SAMPAH".equals(selectedRole)) {
                AppConfig.userName = "Bank Sampah Resik Pusat";
            } else {
                AppConfig.userName = email.contains("@") ? email.split("@")[0] : "Administrator";
            }
            openDashboard();
        } else {
            try {
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
                Map<String, Object> result = ApiClient.login(email, password);
                AppConfig.authToken = (String) result.get("token");
                AppConfig.userId = ((Number) result.get("userId")).longValue();
                AppConfig.userRole = (String) result.get("role");
                AppConfig.userName = (String) result.get("nama");
                AppConfig.userEmail = email;
                openDashboard();
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "";
                if (msg.contains("Email tidak ditemukan")) {
                    showError("Email tidak ditemukan. Periksa kembali email Anda.");
                } else if (msg.contains("Password salah")) {
                    showError("Password salah. Coba lagi.");
                } else if (msg.contains("401")) {
                    showError("Email atau password salah.");
                } else if (msg.contains("403")) {
                    showError("Akses ditolak oleh server.");
                } else if (msg.contains("Koneksi gagal") || msg.contains("Server tidak")) {
                    showError("Koneksi gagal. Pastikan server berjalan di 43.157.243.87:8081");
                } else {
                    showError("Login gagal. Periksa email & password Anda.");
                }
            }
        }
    }

    private void showError(String text) {
        errorLabel.setText(text);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void openDashboard() {
        Stage stage = (Stage) view.getScene().getWindow();
        DashboardView dashboard = new DashboardView();
        Scene scene = new Scene(dashboard.getView(), 1280, 780);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Resik Admin — " + AppConfig.userName);
        stage.setResizable(true);
        stage.centerOnScreen();
        stage.setMaximized(true);
    }

    public StackPane getView() {
        return view;
    }
}
