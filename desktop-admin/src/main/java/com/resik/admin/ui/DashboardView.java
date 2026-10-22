package com.resik.admin.ui;

import com.resik.admin.config.AppConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class DashboardView {

    private final BorderPane view;
    private final VBox contentArea;

    public DashboardView() {
        view = new BorderPane();

        HBox topBar = createTopBar();
        VBox sidebar = createSidebar();

        contentArea = new VBox();
        contentArea.setPadding(new Insets(20));
        contentArea.setStyle("-fx-background-color: #f4f6f4;");

        showDashboard();

        view.setTop(topBar);
        view.setLeft(sidebar);
        view.setCenter(contentArea);
    }

    private HBox createTopBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setStyle("-fx-background-color: #1a5d3a;");
        bar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("♻️ RESIK — Bank Sampah Digital");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLabel = new Label("👤 " + AppConfig.userName + " (" + AppConfig.userRole + ")");
        userLabel.setTextFill(Color.WHITE);
        userLabel.setFont(Font.font("System", 13));

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> {
            AppConfig.authToken = null;
            AppConfig.userId = null;
            Stage stage = (Stage) view.getScene().getWindow();
            LoginView loginView = new LoginView();
            Scene scene = new Scene(loginView.getView(), 400, 500);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
            stage.setResizable(false);
        });

        bar.getChildren().addAll(title, spacer, userLabel, new Label("  "), logoutBtn);
        return bar;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #2d7a4f;");
        sidebar.setPadding(new Insets(15));
        sidebar.setSpacing(5);

        String btnStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15; -fx-cursor: hand; -fx-pref-width: 190; -fx-background-radius: 8;";
        String btnHover = "-fx-background-color: #1a5d3a; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15; -fx-cursor: hand; -fx-pref-width: 190; -fx-background-radius: 8;";

        Button dashBtn = createNavButton("📊 Dashboard", btnStyle, btnHover);
        dashBtn.setOnAction(e -> showDashboard());

        Button pickupBtn = createNavButton("🚚 Penjemputan", btnStyle, btnHover);
        pickupBtn.setOnAction(e -> showPenjemputan());

        Button timbangBtn = createNavButton("⚖️ Penimbangan", btnStyle, btnHover);
        timbangBtn.setOnAction(e -> showTimbang());

        Button sampahBtn = createNavButton("🗑️ Data Sampah", btnStyle, btnHover);
        sampahBtn.setOnAction(e -> showSampah());

        Button laporanBtn = createNavButton("📋 Laporan", btnStyle, btnHover);
        laporanBtn.setOnAction(e -> showLaporan());

        Button chatBtn = createNavButton("💬 Chat", btnStyle, btnHover);
        chatBtn.setOnAction(e -> showChat());

        sidebar.getChildren().addAll(dashBtn, pickupBtn, timbangBtn, sampahBtn, laporanBtn, chatBtn);
        return sidebar;
    }

    private Button createNavButton(String text, String normal, String hover) {
        Button btn = new Button(text);
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }

    private void showDashboard() {
        contentArea.getChildren().clear();

        Label title = new Label("Dashboard Operasional");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));

        HBox stats = new HBox(20);
        stats.setPadding(new Insets(20, 0, 20, 0));

        stats.getChildren().addAll(
                createStatCard("Menunggu", "0", "#f39c12"),
                createStatCard("Dijadwalkan", "0", "#3498db"),
                createStatCard("Selesai", "0", "#27ae60"),
                createStatCard("Total User", "0", "#8e44ad")
        );

        contentArea.getChildren().addAll(title, new Separator(), stats);

        try {
            com.resik.admin.model.Penjemputan[] all =
                    com.resik.admin.service.ApiClient.get("/penjemputan",
                            com.resik.admin.model.Penjemputan[].class);
            Label recentTitle = new Label("Penjemputan Terbaru");
            recentTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
            recentTitle.setPadding(new Insets(10, 0, 10, 0));

            VBox list = new VBox(5);
            for (int i = 0; i < Math.min(all.length, 10); i++) {
                var p = all[i];
                HBox row = new HBox(15);
                row.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 8;");
                row.getChildren().addAll(
                        new Label("#" + p.getId()),
                        new Label(p.getAlamat() != null ? p.getAlamat() : ""),
                        new Label(p.getTanggalJemput() != null ? p.getTanggalJemput().toString() : ""),
                        new Label(p.getStatus() != null ? p.getStatus() : "")
                );
                list.getChildren().add(row);
            }
            contentArea.getChildren().addAll(recentTitle, list);
        } catch (Exception e) {
            Label err = new Label("⚠️ Tidak dapat memuat data. Pastikan server berjalan.\n" + e.getMessage());
            err.setTextFill(Color.RED);
            contentArea.getChildren().add(err);
        }
    }

    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10; -fx-padding: 20;");
        card.setPrefWidth(150);
        Label val = new Label(value);
        val.setFont(Font.font("System", FontWeight.BOLD, 28));
        val.setTextFill(Color.WHITE);
        Label lbl = new Label(label);
        lbl.setTextFill(Color.WHITE);
        card.getChildren().addAll(val, lbl);
        return card;
    }

    private void showPenjemputan() {
        contentArea.getChildren().clear();
        Label title = new Label("🚚 Manajemen Penjemputan");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        contentArea.getChildren().add(title);

        try {
            com.resik.admin.model.Penjemputan[] all =
                    com.resik.admin.service.ApiClient.get("/penjemputan",
                            com.resik.admin.model.Penjemputan[].class);

            TableView<com.resik.admin.model.Penjemputan> table = new TableView<>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            TableColumn<com.resik.admin.model.Penjemputan, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                    String.valueOf(d.getValue().getId())));

            TableColumn<com.resik.admin.model.Penjemputan, String> alamatCol = new TableColumn<>("Alamat");
            alamatCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                    d.getValue().getAlamat()));

            TableColumn<com.resik.admin.model.Penjemputan, String> tglCol = new TableColumn<>("Tanggal");
            tglCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                    d.getValue().getTanggalJemput() != null ? d.getValue().getTanggalJemput().toString() : "-"));

            TableColumn<com.resik.admin.model.Penjemputan, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                    d.getValue().getStatus()));

            table.getColumns().addAll(idCol, alamatCol, tglCol, statusCol);
            table.getItems().addAll(all);

            contentArea.getChildren().add(table);
        } catch (Exception e) {
            contentArea.getChildren().add(new Label("Error: " + e.getMessage()));
        }
    }

    private void showTimbang() {
        contentArea.getChildren().clear();
        Label title = new Label("⚖️ Penimbangan Sampah");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        contentArea.getChildren().add(title);

        Label info = new Label("Pilih penjemputan yang status DIPROSES untuk ditimbang.\nFitur penimbangan input berat aktual dan kalkulasi harga otomatis.");
        info.setWrapText(true);
        contentArea.getChildren().add(info);
    }

    private void showSampah() {
        contentArea.getChildren().clear();
        Label title = new Label("🗑️ Manajemen Data Sampah");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        contentArea.getChildren().add(title);

        try {
            com.resik.admin.model.Sampah[] all =
                    com.resik.admin.service.ApiClient.get("/sampah",
                            com.resik.admin.model.Sampah[].class);

            TableView<com.resik.admin.model.Sampah> table = new TableView<>();
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            TableColumn<com.resik.admin.model.Sampah, String> namaCol = new TableColumn<>("Nama");
            namaCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNama()));

            TableColumn<com.resik.admin.model.Sampah, String> katCol = new TableColumn<>("Kategori");
            katCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getKategori()));

            TableColumn<com.resik.admin.model.Sampah, String> hargaCol = new TableColumn<>("Harga/kg");
            hargaCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                    "Rp " + d.getValue().getHargaPerKg()));

            table.getColumns().addAll(namaCol, katCol, hargaCol);
            table.getItems().addAll(all);
            contentArea.getChildren().add(table);
        } catch (Exception e) {
            contentArea.getChildren().add(new Label("Error: " + e.getMessage()));
        }
    }

    private void showLaporan() {
        contentArea.getChildren().clear();
        Label title = new Label("📋 Laporan Transaksi");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        contentArea.getChildren().add(title);

        Button exportPdf = new Button("📄 Export PDF");
        Button exportExcel = new Button("📊 Export Excel");
        HBox btns = new HBox(10, exportPdf, exportExcel);
        btns.setPadding(new Insets(10, 0, 10, 0));
        contentArea.getChildren().add(btns);

        Label info = new Label("Laporan transaksi harian, bulanan, dan tahunan akan ditampilkan di sini.");
        contentArea.getChildren().add(info);
    }

    private void showChat() {
        contentArea.getChildren().clear();
        Label title = new Label("💬 Chat dengan Masyarakat");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        contentArea.getChildren().add(title);

        Label info = new Label("Daftar chat aktif dengan masyarakat akan ditampilkan di sini.");
        contentArea.getChildren().add(info);
    }

    public BorderPane getView() {
        return view;
    }
}
