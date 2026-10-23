package com.resik.admin.ui;

import com.resik.admin.config.AppConfig;
import com.resik.admin.model.Penjemputan;
import com.resik.admin.model.Sampah;
import com.resik.admin.service.ApiClient;
import com.resik.admin.service.ApiService;
import com.resik.admin.service.DummyData;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class DashboardView {

    private final BorderPane view;
    private final VBox contentArea;
    private VBox activeNav = null;
    private final ObservableList<Penjemputan> penjemputanData = FXCollections.observableArrayList();

    public DashboardView() {
        view = new BorderPane();
        view.setTop(createTopBar());
        view.setLeft(createSidebar());
        contentArea = new VBox();
        contentArea.setPadding(new Insets(28, 32, 28, 32));
        contentArea.setStyle("-fx-background-color: #F5F7F5;");
        contentArea.setSpacing(16);
        view.setCenter(contentArea);
        // Load data from VPS
        loadPenjemputanFromVPS();
        showDashboard();
    }

    private void loadPenjemputanFromVPS() {
        try {
            List<Penjemputan> list = ApiService.getPenjemputan();
            penjemputanData.setAll(list);
        } catch (Exception e) {
            // Fallback to DummyData if VPS unavailable
            penjemputanData.setAll(DummyData.getPenjemputanList());
        }
    }

    private HBox createTopBar() {
        HBox bar = new HBox(14);
        bar.setPadding(new Insets(14, 28, 14, 28));
        bar.setStyle("-fx-background-color: #1A5D3A;");
        bar.setAlignment(Pos.CENTER_LEFT);

        ImageView logoIv = new ImageView(new Image(getClass().getResourceAsStream("/resik-logo.png")));
        logoIv.setFitWidth(30); logoIv.setFitHeight(30); logoIv.setPreserveRatio(true);
        logoIv.setClip(new Circle(15));
        Label title = new Label("Resik");
        title.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 18));
        title.setTextFill(Color.WHITE);
        Label sub = new Label("Bank Sampah Digital");
        sub.setFont(Font.font("SF Pro Display", 12));
        sub.setTextFill(Color.valueOf("#C8E6D0"));
        bar.getChildren().addAll(logoIv, title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getChildren().add(spacer);

        Label un = new Label(AppConfig.userName);
        un.setTextFill(Color.WHITE);
        un.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 13));
        Label rl = new Label(AppConfig.userRole);
        rl.setTextFill(Color.valueOf("#C8E6D0"));
        rl.setFont(Font.font("SF Pro Display", 11));
        VBox userBox = new VBox(un, rl);
        userBox.setAlignment(Pos.CENTER_RIGHT);

        Circle av = new Circle(18);
        av.setFill(Color.valueOf("#2D7A4F"));
        Text ini = new Text(AppConfig.userName.substring(0, 1).toUpperCase());
        ini.setFill(Color.WHITE);
        ini.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 14));
        StackPane avP = new StackPane(av, ini);

        Button out = new Button("Keluar");
        out.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-border-color: #EF4444; -fx-border-width: 1px; -fx-border-radius: 12; -fx-padding: 6 16; -fx-font-size: 12px; -fx-cursor: hand;");
        out.setOnMouseEntered(e -> out.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-border-color: #DC2626; -fx-border-width: 1px; -fx-border-radius: 12; -fx-padding: 6 16; -fx-font-size: 12px;"));
        out.setOnMouseExited(e -> out.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-border-color: #EF4444; -fx-border-width: 1px; -fx-border-radius: 12; -fx-padding: 6 16; -fx-font-size: 12px;"));
        out.setOnAction(e -> {
            AppConfig.authToken = null; AppConfig.userId = null;
            Stage st = (Stage) view.getScene().getWindow();
            LoginView lv = new LoginView();
            Scene sc = new Scene(lv.getView(), 460, 600);
            sc.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            st.setScene(sc); st.setResizable(true); st.centerOnScreen();
        });
        bar.getChildren().addAll(userBox, avP, out);
        return bar;
    }

    private VBox createSidebar() {
        VBox sb = new VBox();
        sb.setPrefWidth(220);
        sb.setStyle("-fx-background-color: #0D4E2C;");
        sb.setPadding(new Insets(16, 10, 16, 10));
        sb.setSpacing(4);
        Label mh = new Label("MENU");
        mh.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 10));
        mh.setTextFill(Color.valueOf("#7BA89A"));
        mh.setStyle("-fx-letter-spacing: 2px;");
        mh.setPadding(new Insets(12, 12, 8, 12));
        VBox nav = new VBox(2);
        nav.getChildren().addAll(
            createNavButton("Dashboard", e -> showDashboard()),
            createNavButton("Penjemputan", e -> showPenjemputan()),
            createNavButton("Penimbangan", e -> showTimbang()),
            createNavButton("Data Sampah", e -> showSampah()),
            createNavButton("Edukasi Sampah", e -> showEdukasi()),
            createNavButton("Chat Client", e -> showChat()),
            createNavButton("Lokasi Bank Sampah", e -> showLokasi()),
            createNavButton("Dompet Digital", e -> showDompet()),
            createNavButton("Leaderboard", e -> showLeaderboard()),
            createNavButton("Laporan Harian", e -> showLaporanHarian()),
            createNavButton("Riwayat Setoran", e -> showRiwayatSetoran()),
            createNavButton("Manajemen User", e -> showUsers()),
            createNavButton("Profil", e -> showProfil()));
        sb.getChildren().addAll(mh, nav);
        return sb;
    }

    private VBox createNavButton(String label, javafx.event.EventHandler<javafx.scene.input.MouseEvent> handler) {
        VBox btn = new VBox();
        String n = "-fx-background-color: transparent; -fx-padding: 10 16; -fx-background-radius: 12; -fx-cursor: hand;";
        String h = "-fx-background-color: rgba(255,255,255,0.08); -fx-padding: 10 16; -fx-background-radius: 12; -fx-cursor: hand;";
        String a = "-fx-background-color: rgba(255,255,255,0.12); -fx-padding: 10 16; -fx-background-radius: 12; -fx-cursor: hand;";
        btn.setStyle(n);
        btn.setOnMouseEntered(e -> { if (btn != activeNav) btn.setStyle(h); });
        btn.setOnMouseExited(e -> { if (btn != activeNav) btn.setStyle(n); });
        btn.setOnMouseClicked(e -> { if (activeNav != null) activeNav.setStyle(n); btn.setStyle(a); activeNav = btn; handler.handle(e); });
        Label t = new Label(label);
        t.setTextFill(Color.valueOf("#C8E6D0"));
        t.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 13));
        btn.getChildren().add(t);
        return btn;
    }

    private VBox createPageHeader(String t, String d) {
        VBox h = new VBox(4);
        Label tl = new Label(t);
        tl.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 24));
        tl.setTextFill(Color.valueOf("#1E293B"));
        Label dl = new Label(d);
        dl.setFont(Font.font("SF Pro Display", 13));
        dl.setTextFill(Color.valueOf("#94A3B8"));
        h.getChildren().addAll(tl, dl);
        return h;
    }

    private <T> TableView<T> createStyledTable() {
        TableView<T> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setStyle("-fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: #E2E8F0; -fx-border-width: 1px;");
        return t;
    }

    // ==================== DASHBOARD ====================
    private void showDashboard() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Dashboard", "Ringkasan operasional bank sampah"));
        Map<String, Integer> s = DummyData.getStats();
        HBox sr = new HBox(16);
        sr.getChildren().addAll(
            createStatCard("Menunggu", s.get("MENUNGGU"), "#F39C12"),
            createStatCard("Dijadwalkan", s.get("DIJADWALKAN"), "#3B82F6"),
            createStatCard("Selesai", s.get("SELESAI"), "#10B981"),
            createStatCard("Total User", s.get("TOTAL_USER"), "#8B5CF6"));

        // Simple bar chart - Setoran per kategori
        VBox chartBox = new VBox(12);
        chartBox.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 6, 0, 0, 1);");
        Label chartTitle = new Label("Setoran per Kategori");
        chartTitle.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 15));
        chartTitle.setTextFill(Color.valueOf("#1E293B"));
        String[] kategori = {"ORGANIK", "ANORGANIK", "B3", "RESIDU"};
        int[] values = {15, 28, 8, 12};
        String[] colors = {"#10B981", "#3B82F6", "#EF4444", "#6B7280"};
        int maxVal = 30;
        VBox chartContent = new VBox(8);
        for (int i = 0; i < kategori.length; i++) {
            HBox bar = new HBox(10);
            bar.setAlignment(Pos.CENTER_LEFT);
            Label label = new Label(kategori[i]);
            label.setPrefWidth(80);
            label.setFont(Font.font("SF Pro Display", 12));
            label.setTextFill(Color.valueOf("#64748B"));
            double barWidth = (values[i] * 1.0 / maxVal) * 400;
            VBox barViz = new VBox();
            barViz.setStyle("-fx-background-color: " + colors[i] + "; -fx-background-radius: 6; -fx-min-width: " + barWidth + "; -fx-pref-height: 24; -fx-max-width: " + barWidth + ";");
            Label valLabel = new Label(" " + values[i] + " setoran");
            valLabel.setFont(Font.font("SF Pro Display", 11));
            valLabel.setTextFill(Color.valueOf("#64748B"));
            bar.getChildren().addAll(label, barViz, valLabel);
            chartContent.getChildren().add(bar);
        }
        chartBox.getChildren().addAll(chartTitle, chartContent);

        VBox rb = new VBox(12);
        Label rt = new Label("Penjemputan Terbaru");
        rt.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 15));
        rt.setTextFill(Color.valueOf("#1E293B"));
        VBox list = new VBox(6);
        for (Penjemputan p : penjemputanData) {
            HBox row = new HBox(20);
            row.setStyle("-fx-background-color: white; -fx-padding: 14 18; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 6, 0, 0, 1);");
            row.setAlignment(Pos.CENTER_LEFT);
            Label idL = new Label("#" + String.format("%03d", p.getId()));
            idL.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 12));
            idL.setTextFill(Color.valueOf("#94A3B8"));
            idL.setPrefWidth(50);
            VBox ab = new VBox(2);
            Label al = new Label(p.getAlamat());
            al.setFont(Font.font("SF Pro Display", 13));
            al.setTextFill(Color.valueOf("#334155"));
            Label cl = new Label(p.getCatatan());
            cl.setFont(Font.font("SF Pro Display", 11));
            cl.setTextFill(Color.valueOf("#94A3B8"));
            ab.getChildren().addAll(al, cl);
            ab.setPrefWidth(300);
            Label tl = new Label(p.getTanggalJemput() != null ? p.getTanggalJemput().toString() : "-");
            tl.setPrefWidth(100);
            tl.setFont(Font.font("SF Pro Display", 12));
            tl.setTextFill(Color.valueOf("#64748B"));
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label sb = createStatusLabel(p.getStatus());
            row.getChildren().addAll(idL, ab, tl, sp, sb);
            list.getChildren().add(row);
        }
        rb.getChildren().addAll(rt, list);
        HBox twoCol = new HBox(16);
        chartBox.setPrefWidth(500);
        rb.setPrefWidth(500);
        HBox.setHgrow(rb, Priority.ALWAYS);
        twoCol.getChildren().addAll(chartBox, rb);
        contentArea.getChildren().addAll(sr, twoCol);
    }

    private VBox createStatCard(String label, int value, String color) {
        VBox c = new VBox(6);
        c.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 20; -fx-padding: 20 24; -fx-pref-width: 160; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);");
        Label v = new Label(String.valueOf(value));
        v.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 32));
        v.setTextFill(Color.WHITE);
        Label l = new Label(label);
        l.setFont(Font.font("SF Pro Display", 12));
        l.setTextFill(Color.valueOf("#E2E8F0"));
        c.getChildren().addAll(v, l);
        return c;
    }

    private Label createStatusLabel(String status) {
        Label sb = new Label(status);
        sb.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 10));
        sb.setPadding(new Insets(4, 10, 4, 10));
        String bg, fg;
        switch (status) {
            case "MENUNGGU": bg="#FEF3C7"; fg="#92400E"; break;
            case "DIJADWALKAN": bg="#DBEAFE"; fg="#1E40AF"; break;
            case "DIPROSES": bg="#F3E8FF"; fg="#6B21A8"; break;
            case "SELESAI": bg="#D1FAE5"; fg="#065F46"; break;
            case "DIBATALKAN": bg="#FEE2E2"; fg="#991B1B"; break;
            default: bg="#F1F5F9"; fg="#475569";
        }
        sb.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-background-radius: 8;");
        return sb;
    }

    // ==================== PENJEMPUTAN (Pilih + Assign Petugas) ====================
    private void showPenjemputan() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Manajemen Penjemputan", "Pilih penjemputan, assign petugas, update status"));

        HBar actionBar = new HBar(10);
        actionBar.add(createBtn("Tambah Penjemputan", "#1A5D3A", "white", e -> {
            CrudDialog.showForm("Tambah Penjemputan", "Simpan",
                List.of(
                    new CrudDialog.FormField("kategori", "Kategori (ORGANIK/ANORGANIK/B3/RESIDU)", "ORGANIK"),
                    new CrudDialog.FormField("estimasiBerat", "Estimasi Berat (kg)", "5"),
                    new CrudDialog.FormField("alamat", "Alamat Penjemputan", "Jl. Contoh No. 1"),
                    new CrudDialog.FormField("tanggal", "Tanggal (YYYY-MM-DD)", LocalDate.now().toString()),
                    new CrudDialog.FormField("waktu", "Waktu", "09:00-11:00"),
                    new CrudDialog.FormField("catatan", "Catatan", "Jenis sampah dan estimasi")
                ), null
            ).ifPresent(vals -> {
                Penjemputan p = new Penjemputan();
                p.setId((long)(penjemputanData.size() + 1));
                p.setAlamat(vals.get("alamat"));
                try { p.setTanggalJemput(LocalDate.parse(vals.get("tanggal"))); } catch(Exception ex) { p.setTanggalJemput(LocalDate.now()); }
                p.setWaktuJemput(vals.get("waktu"));
                String catatan = vals.get("kategori") + " - " + vals.get("estimasiBerat") + "kg | " + vals.get("catatan");
                p.setCatatan(catatan);
                p.setStatus("MENUNGGU");
                p.setQrCode("RESIK-" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
                penjemputanData.add(p);
                showPenjemputan();
                CrudDialog.showInfo("Berhasil", "Penjemputan baru ditambahkan.\nKategori: " + vals.get("kategori") + "\nEstimasi: " + vals.get("estimasiBerat") + "kg");
            });
        }));
        actionBar.add(createBtn("Assign Petugas", "#3B82F6", "white", e -> {
            TableView<Penjemputan> tbl = (TableView) contentArea.getChildren().stream().filter(n -> n instanceof TableView).findFirst().orElse(null);
            if (tbl != null) {
                Penjemputan sel = tbl.getSelectionModel().getSelectedItem();
                if (sel != null && (sel.getStatus().equals("MENUNGGU") || sel.getStatus().equals("DIJADWALKAN"))) {
                    sel.setStatus("DIJADWALKAN");
                    tbl.refresh();
                    CrudDialog.showInfo("Berhasil", "Petugas ditugaskan untuk #" + sel.getId());
                } else {
                    CrudDialog.showInfo("Info", "Pilih penjemputan berstatus MENUNGGU/DIJADWALKAN");
                }
            }
        }));
        actionBar.add(createBtn("Proses", "#8B5CF6", "white", e -> {
            TableView<Penjemputan> tbl = (TableView) contentArea.getChildren().stream().filter(n -> n instanceof TableView).findFirst().orElse(null);
            if (tbl != null) {
                Penjemputan sel = tbl.getSelectionModel().getSelectedItem();
                if (sel != null && sel.getStatus().equals("DIJADWALKAN")) {
                    sel.setStatus("DIPROSES");
                    tbl.refresh();
                    CrudDialog.showInfo("Berhasil", "Penjemputan #" + sel.getId() + " sekarang DIPROSES — lanjut ke Penimbangan");
                }
            }
        }));
        contentArea.getChildren().addAll(actionBar.get(), new Separator());

        TableView<Penjemputan> t = createStyledTable();

        // Search box
        HBox searchBox = new HBox(8);
        searchBox.setPadding(new Insets(0, 0, 8, 0));
        TextField searchField = new TextField();
        searchField.setPromptText("Cari alamat, status, atau catatan...");
        searchField.setStyle("-fx-background-radius: 8; -fx-padding: 8 12; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-pref-width: 400;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                t.setItems(penjemputanData);
            } else {
                ObservableList<Penjemputan> filtered = FXCollections.observableArrayList();
                for (Penjemputan p : penjemputanData) {
                    if ((p.getAlamat() != null && p.getAlamat().toLowerCase().contains(newVal.toLowerCase())) ||
                        (p.getStatus() != null && p.getStatus().toLowerCase().contains(newVal.toLowerCase())) ||
                        (p.getCatatan() != null && p.getCatatan().toLowerCase().contains(newVal.toLowerCase()))) {
                        filtered.add(p);
                    }
                }
                t.setItems(filtered);
            }
        });
        searchBox.getChildren().add(searchField);
        contentArea.getChildren().add(searchBox);
        t.setItems(penjemputanData);
        TableColumn<Penjemputan, String> c1 = new TableColumn<>("ID");
        c1.setCellValueFactory(d -> new SimpleStringProperty(String.format("%03d", d.getValue().getId())));
        TableColumn<Penjemputan, String> c2 = new TableColumn<>("Alamat");
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAlamat()));
        TableColumn<Penjemputan, String> c3 = new TableColumn<>("Tanggal");
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTanggalJemput() != null ? d.getValue().getTanggalJemput().toString() : "-"));
        TableColumn<Penjemputan, String> c4 = new TableColumn<>("Waktu");
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getWaktuJemput()));
        TableColumn<Penjemputan, String> c5 = new TableColumn<>("Status");
        c5.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        TableColumn<Penjemputan, String> c6 = new TableColumn<>("Catatan");
        c6.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCatatan()));
        t.getColumns().addAll(c1, c2, c3, c4, c5, c6);
        contentArea.getChildren().add(t);
    }

    // ==================== PENIMBANGAN (Timbang + Kalkulasi + Bayar + Dompet) ====================
    private void showTimbang() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Penimbangan Sampah", "Input berat aktual, kalkulasi harga, konfirmasi pembayaran, transfer ke dompet"));

        // Filter hanya DIPROSES
        List<Penjemputan> prosesList = new ArrayList<>();
        for (Penjemputan p : penjemputanData) {
            if ("DIPROSES".equals(p.getStatus())) prosesList.add(p);
        }

        if (prosesList.isEmpty()) {
            VBox empty = new VBox(10);
            empty.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 40; -fx-alignment: center;");
            Label e1 = new Label("Tidak ada penjemputan berstatus DIPROSES");
            e1.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 16));
            e1.setTextFill(Color.valueOf("#94A3B8"));
            Label e2 = new Label("Ubah status penjemputan ke DIPROSES dari menu Penjemputan terlebih dahulu");
            e2.setFont(Font.font("SF Pro Display", 12));
            e2.setTextFill(Color.valueOf("#94A3B8"));
            empty.getChildren().addAll(e1, e2);
            contentArea.getChildren().add(empty);
            return;
        }

        ComboBox<Penjemputan> combo = new ComboBox<>(FXCollections.observableArrayList(prosesList));
        combo.setPromptText("Pilih penjemputan untuk ditimbang...");
        combo.setPrefWidth(400);
        combo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Penjemputan p) { return p == null ? "" : "#" + p.getId() + " — " + p.getAlamat(); }
            @Override public Penjemputan fromString(String s) { return null; }
        });

        // Form timbang
        VBox timbangForm = new VBox(12);
        timbangForm.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 24; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 8, 0, 0, 2);");
        timbangForm.setVisible(false);

        Label detailTitle = new Label("Detail Penimbangan");
        detailTitle.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 16));
        detailTitle.setTextFill(Color.valueOf("#1E293B"));

        VBox detailRows = new VBox(8);
        Label totalLabel = new Label("Total: Rp 0");
        totalLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 18));
        totalLabel.setTextFill(Color.valueOf("#1A5D3A"));

        Map<Long, TextField> beratFields = new HashMap<>();
        Map<Long, BigDecimal> hargaMap = new HashMap<>();

        combo.setOnAction(e -> {
            Penjemputan sel = combo.getValue();
            if (sel == null) return;
            detailRows.getChildren().clear();
            beratFields.clear();
            hargaMap.clear();
            List<Sampah> sampahList = DummyData.getSampahList();
            for (int i = 0; i < sel.getDetailList().size(); i++) {
                Penjemputan.DetailPenjemputan dp = sel.getDetailList().get(i);
                Sampah s = sampahList.get(i % sampahList.size());
                hargaMap.put(dp.getId(), s.getHargaPerKg());

                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                Label nama = new Label(s.getNama() + " (Rp " + s.getHargaPerKg() + "/kg)");
                nama.setPrefWidth(250);
                nama.setFont(Font.font("SF Pro Display", 13));
                nama.setTextFill(Color.valueOf("#334155"));
                Label est = new Label("Estimasi: " + dp.getEstimasiBerat() + " kg");
                est.setFont(Font.font("SF Pro Display", 11));
                est.setTextFill(Color.valueOf("#94A3B8"));
                TextField berat = new TextField();
                berat.setPromptText("Berat aktual (kg)");
                berat.setPrefWidth(120);
                berat.setStyle("-fx-background-radius: 14; -fx-border-radius: 14; -fx-padding: 8 12;");
                beratFields.put(dp.getId(), berat);
                Label sub = new Label("Subtotal: Rp 0");
                sub.setFont(Font.font("SF Pro Display", 12));
                sub.setTextFill(Color.valueOf("#64748B"));
                berat.textProperty().addListener((obs, o, nv) -> {
                    try {
                        BigDecimal b = new BigDecimal(nv);
                        BigDecimal subtot = b.multiply(hargaMap.get(dp.getId()));
                        sub.setText("Subtotal: Rp " + subtot);
                        updateTotal(beratFields, hargaMap, totalLabel);
                    } catch(Exception ex) {
                        sub.setText("Subtotal: Rp 0");
                        updateTotal(beratFields, hargaMap, totalLabel);
                    }
                });
                row.getChildren().addAll(nama, est, berat, sub);
                detailRows.getChildren().add(row);
            }
            updateTotal(beratFields, hargaMap, totalLabel);
            timbangForm.setVisible(true);
        });

        // Buttons
        HBox btnRow = new HBox(10);
        btnRow.setPadding(new Insets(8, 0, 0, 0));

        Label calcBtn = createBtn("Kalkulasi Harga", "#F39C12", "white", e -> {
            updateTotal(beratFields, hargaMap, totalLabel);
            CrudDialog.showInfo("Kalkulasi", "Total harga: " + totalLabel.getText());
        });

        Label payBtn = createBtn("Konfirmasi Pembayaran", "#1A5D3A", "white", e -> {
            Penjemputan sel = combo.getValue();
            if (sel == null) return;
            BigDecimal total = calculateTotal(beratFields, hargaMap);
            if (total.compareTo(BigDecimal.ZERO) <= 0) {
                CrudDialog.showInfo("Error", "Masukkan berat aktual terlebih dahulu");
                return;
            }
            // Update details
            for (Penjemputan.DetailPenjemputan dp : sel.getDetailList()) {
                TextField f = beratFields.get(dp.getId());
                if (f != null) {
                    try {
                        BigDecimal b = new BigDecimal(f.getText());
                        dp.setBeratAktual(b);
                        dp.setSubtotal(b.multiply(hargaMap.get(dp.getId())));
                    } catch(Exception ex) {}
                }
            }
            // Create transaksi
            Penjemputan.Transaksi t = new Penjemputan.Transaksi();
            t.setId(sel.getId());
            t.setTotalHarga(total);
            t.setStatus("BERHASIL");
            sel.setTransaksi(t);
            // Update status = SELESAI
            sel.setStatus("SELESAI");
            // Transfer ke dompet (update DummyData)
            DummyData.addTransactionToUser(sel, total);
            CrudDialog.showInfo("Berhasil", "Pembayaran Rp " + total + " dikonfirmasi.\nSaldo dompet user telah diupdate.\nStatus penjemputan: SELESAI");
            penjemputanData.set(penjemputanData.indexOf(sel), sel);
            showTimbang();
        });

        btnRow.getChildren().addAll(calcBtn, payBtn);
        timbangForm.getChildren().addAll(detailTitle, detailRows, totalLabel, btnRow);

        contentArea.getChildren().addAll(combo, timbangForm);
    }

    private void updateTotal(Map<Long, TextField> fields, Map<Long, BigDecimal> harga, Label totalLabel) {
        totalLabel.setText("Total: Rp " + calculateTotal(fields, harga));
    }

    private BigDecimal calculateTotal(Map<Long, TextField> fields, Map<Long, BigDecimal> harga) {
        BigDecimal total = BigDecimal.ZERO;
        for (var entry : fields.entrySet()) {
            try {
                BigDecimal b = new BigDecimal(entry.getValue().getText());
                BigDecimal h = harga.get(entry.getKey());
                if (h != null) total = total.add(b.multiply(h));
            } catch(Exception ex) {}
        }
        return total;
    }

    // ==================== DATA SAMPAH (VPS CRUD) ====================
    private void showSampah() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Data Sampah", "Kelola jenis sampah dan harga per kilogram (VPS)"));
        HBar bar = new HBar(10);
        bar.add(createBtn("Tambah Sampah", "#1A5D3A", "white", e -> {
            CrudDialog.showForm("Tambah Jenis Sampah", "Simpan",
                List.of(
                    new CrudDialog.FormField("nama", "Nama Sampah", "Contoh: Botol Plastik"),
                    new CrudDialog.FormField("kategori", "Kategori", "ORGANIK/ANORGANIK/B3/RESIDU"),
                    new CrudDialog.FormField("harga", "Harga per kg (Rp)", "2500"),
                    new CrudDialog.FormField("deskripsi", "Deskripsi", "")
                ), null
            ).ifPresent(vals -> {
                try {
                    Sampah s = new Sampah();
                    s.setNama(vals.get("nama"));
                    s.setKategori(vals.get("kategori"));
                    s.setHargaPerKg(new BigDecimal(vals.get("harga")));
                    s.setDeskripsi(vals.get("deskripsi"));
                    s.setIsActive(true);
                    ApiService.createSampah(s);
                    showSampah();
                    CrudDialog.showInfo("Berhasil", "Jenis sampah ditambahkan ke VPS.");
                } catch (Exception ex) {
                    CrudDialog.showInfo("Error", "Gagal tambah: " + ex.getMessage());
                }
            });
        }));
        bar.add(createBtn("Refresh", "#3B82F6", "white", e -> showSampah()));
        contentArea.getChildren().addAll(bar.get(), new Separator());

        TableView<Sampah> t = createStyledTable();
        List<Sampah> sampahList = ApiService.getSampah();
        if (sampahList.isEmpty()) sampahList = DummyData.getSampahList();
        t.setItems(FXCollections.observableArrayList(sampahList));
        TableColumn<Sampah, String> c1 = new TableColumn<>("Nama");
        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNama()));
        TableColumn<Sampah, String> c2 = new TableColumn<>("Kategori");
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getKategori()));
        TableColumn<Sampah, String> c3 = new TableColumn<>("Harga/kg");
        c3.setCellValueFactory(d -> new SimpleStringProperty("Rp " + d.getValue().getHargaPerKg()));
        TableColumn<Sampah, String> c4 = new TableColumn<>("Deskripsi");
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDeskripsi()));
        t.getColumns().addAll(c1, c2, c3, c4);

        // Context menu for delete
        ContextMenu cm = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Hapus Sampah");
        deleteItem.setOnAction(e -> {
            Sampah sel = t.getSelectionModel().getSelectedItem();
            if (sel != null) {
                try {
                    ApiService.deleteSampah(sel.getId());
                    showSampah();
                    CrudDialog.showInfo("Berhasil", "Sampah dihapus dari VPS.");
                } catch (Exception ex) {
                    CrudDialog.showInfo("Error", "Gagal hapus: " + ex.getMessage());
                }
            }
        });
        cm.getItems().add(deleteItem);
        t.setContextMenu(cm);

        t.setRowFactory(tv -> {
            TableRow<Sampah> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !row.isEmpty()) {
                    Sampah sel = row.getItem();
                    Map<String,String> init = new HashMap<>();
                    init.put("nama", sel.getNama()); init.put("kategori", sel.getKategori());
                    init.put("harga", sel.getHargaPerKg().toString()); init.put("deskripsi", sel.getDeskripsi());
                    CrudDialog.showForm("Edit Sampah", "Update",
                        List.of(
                            new CrudDialog.FormField("nama","Nama",""), new CrudDialog.FormField("kategori","Kategori",""),
                            new CrudDialog.FormField("harga","Harga/kg",""), new CrudDialog.FormField("deskripsi","Deskripsi","")
                        ), init
                    ).ifPresent(vals -> {
                        try {
                            sel.setNama(vals.get("nama"));
                            sel.setKategori(vals.get("kategori"));
                            sel.setHargaPerKg(new BigDecimal(vals.get("harga")));
                            sel.setDeskripsi(vals.get("deskripsi"));
                            ApiService.updateSampah(sel.getId(), sel);
                            showSampah();
                            CrudDialog.showInfo("Berhasil", "Data sampah diupdate di VPS.");
                        } catch (Exception ex) {
                            CrudDialog.showInfo("Error", "Gagal update: " + ex.getMessage());
                        }
                    });
                }
            });
            return row;
        });
        contentArea.getChildren().add(t);
    }

    // ==================== DOMPET DIGITAL (VPS) ====================
    private void showDompet() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Dompet Digital", "Saldo dan riwayat transaksi semua pengguna (VPS)"));
        VBox list = new VBox(6);
        List<Map<String,Object>> dompetList = ApiService.getAllDompet();
        if (dompetList.isEmpty()) dompetList = DummyData.getDompetList();
        for (Map<String,Object> d : dompetList) {
            HBox row = new HBox(20);
            row.setStyle("-fx-background-color: white; -fx-padding: 14 18; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 6, 0, 0, 1);");
            row.setAlignment(Pos.CENTER_LEFT);
            Label nm = new Label(String.valueOf(d.get("nama")));
            nm.setFont(Font.font("SF Pro Display", 14)); nm.setTextFill(Color.valueOf("#334155")); nm.setPrefWidth(250);
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            Label sd = new Label((String) d.get("saldo"));
            sd.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 14)); sd.setTextFill(Color.valueOf("#1A5D3A")); sd.setPrefWidth(120);
            Label pi = new Label((String) d.get("poin"));
            pi.setFont(Font.font("SF Pro Display", 13)); pi.setTextFill(Color.valueOf("#F39C12"));
            row.getChildren().addAll(nm, sp, sd, pi);
            list.getChildren().add(row);
        }
        contentArea.getChildren().add(list);
    }

    // ==================== LEADERBOARD ====================
    private void showLeaderboard() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Leaderboard", "Ranking kontribusi masyarakat (VPS)"));
        VBox list = new VBox(6);
        List<Map<String, Object>> lbData = ApiService.getLeaderboard();
        if (lbData.isEmpty()) {
            // Fallback
            String[] names = {"Syukron Maulana","Zain Ghathfaan","Riri Syafala","Ahmad Fauzi","Siti Nurhaliza"};
            int[] poins = {320,280,250,180,120};
            for (int i=0; i<names.length; i++) {
                HBox row = new HBox(16);
                row.setStyle("-fx-background-color: white; -fx-padding: 14 18; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 6, 0, 0, 1);");
                row.setAlignment(Pos.CENTER_LEFT);
                Label rank = new Label(String.valueOf(i+1));
                rank.setPrefWidth(30);
                rank.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 16));
                rank.setTextFill(i==0?Color.valueOf("#F39C12"):i==1?Color.valueOf("#94A3B8"):i==2?Color.valueOf("#CD7F32"):Color.valueOf("#94A3B8"));
                Label nm = new Label(names[i]); nm.setPrefWidth(250);
                nm.setFont(Font.font("SF Pro Display", 14)); nm.setTextFill(Color.valueOf("#334155"));
                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                Label pi = new Label(poins[i]+" poin");
                pi.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 13)); pi.setTextFill(Color.valueOf("#1A5D3A"));
                row.getChildren().addAll(rank, nm, sp, pi);
                list.getChildren().add(row);
            }
        } else {
            for (Map<String, Object> item : lbData) {
                int rank = item.get("rank") != null ? ((Number) item.get("rank")).intValue() : 0;
                String nama = String.valueOf(item.getOrDefault("nama", "?"));
                Object poinObj = item.getOrDefault("poin", 0);
                String poinStr = poinObj instanceof Number ? ((Number) poinObj).intValue() + " poin" : "0 poin";
                Object saldoObj = item.getOrDefault("saldo", 0);
                String saldoStr = saldoObj instanceof Number ? "Rp " + ((Number) saldoObj).doubleValue() : "Rp 0";

                HBox row = new HBox(16);
                row.setStyle("-fx-background-color: white; -fx-padding: 14 18; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 6, 0, 0, 1);");
                row.setAlignment(Pos.CENTER_LEFT);
                Label rankL = new Label(String.valueOf(rank));
                rankL.setPrefWidth(30);
                rankL.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 16));
                rankL.setTextFill(rank==1?Color.valueOf("#F39C12"):rank==2?Color.valueOf("#94A3B8"):rank==3?Color.valueOf("#CD7F32"):Color.valueOf("#94A3B8"));
                Label nm = new Label(nama); nm.setPrefWidth(250);
                nm.setFont(Font.font("SF Pro Display", 14)); nm.setTextFill(Color.valueOf("#334155"));
                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                Label pi = new Label(poinStr + " | " + saldoStr);
                pi.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 13)); pi.setTextFill(Color.valueOf("#1A5D3A"));
                row.getChildren().addAll(rankL, nm, sp, pi);
                list.getChildren().add(row);
            }
        }
        contentArea.getChildren().add(list);
    }

    // ==================== LAPORAN HARIAN (Statistik per hari) ====================
    private void showLaporanHarian() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Laporan Harian", "Statistik setoran dan pembayaran per hari"));

        // Filter per hari
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle("-fx-background-radius: 14; -fx-border-radius: 14; -fx-padding: 8 12;");
        Label dateLabel = new Label("Pilih tanggal:");
        dateLabel.setFont(Font.font("SF Pro Display", 13)); dateLabel.setTextFill(Color.valueOf("#64748B"));
        HBox dateBox = new HBox(10, dateLabel, datePicker,
            createBtn("Export ke File", "#1A5D3A", "white", e -> {
                LocalDate selected = datePicker.getValue();
                java.io.File file = new java.io.File(System.getProperty("user.home"), "Desktop/Laporan-Resik-" + selected + ".txt");
                try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
                    pw.println("=====================================");
                    pw.println("  LAPORAN HARIAN RESIK");
                    pw.println("  Tanggal: " + selected);
                    pw.println("=====================================");
                    pw.println();
                    int totalSetoran = 0; BigDecimal totalPembayaran = BigDecimal.ZERO; double totalKg = 0;
                    for (Penjemputan p : penjemputanData) {
                        if (p.getTanggalJemput() != null && p.getTanggalJemput().equals(selected) && "SELESAI".equals(p.getStatus())) {
                            totalSetoran++;
                            if (p.getTransaksi() != null) totalPembayaran = totalPembayaran.add(p.getTransaksi().getTotalHarga());
                            if (p.getDetailList() != null) for (var d : p.getDetailList()) if (d.getBeratAktual()!=null) totalKg += d.getBeratAktual().doubleValue();
                            pw.println("ID: #" + p.getId() + " | Alamat: " + p.getAlamat() + " | Status: " + p.getStatus());
                        }
                    }
                    pw.println();
                    pw.println("Total Setoran Selesai: " + totalSetoran);
                    pw.println("Total Pembayaran: Rp " + totalPembayaran);
                    pw.println(String.format("Total Sampah: %.1f kg", totalKg));
                    pw.println(String.format("Estimasi CO2 Reduction: %.1f kg", totalKg * 2));
                    pw.println();
                    pw.println("=====================================");
                    CrudDialog.showInfo("Berhasil", "Laporan di-export ke:\n" + file.getAbsolutePath());
                } catch (Exception ex) {
                    CrudDialog.showInfo("Error", "Gagal export: " + ex.getMessage());
                }
            }));
        dateBox.setAlignment(Pos.CENTER_LEFT);

        // Stat cards
        HBox statsRow = new HBox(16);
        VBox statsCard = new VBox(8);
        statsCard.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 24; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 8, 0, 0, 2);");

        Runnable updateStats = () -> {
            LocalDate selected = datePicker.getValue();
            statsCard.getChildren().clear();
            int totalSetoran = 0; BigDecimal totalPembayaran = BigDecimal.ZERO; double totalKg = 0;
            for (Penjemputan p : penjemputanData) {
                if (p.getTanggalJemput() != null && p.getTanggalJemput().equals(selected) && "SELESAI".equals(p.getStatus())) {
                    totalSetoran++;
                    if (p.getTransaksi() != null) totalPembayaran = totalPembayaran.add(p.getTransaksi().getTotalHarga());
                    if (p.getDetailList() != null) for (var d : p.getDetailList()) if (d.getBeratAktual()!=null) totalKg += d.getBeratAktual().doubleValue();
                }
            }
            Label t1 = new Label("Setoran Selesai: " + totalSetoran);
            t1.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 14)); t1.setTextFill(Color.valueOf("#334155"));
            Label t2 = new Label("Total Pembayaran: Rp " + totalPembayaran);
            t2.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 14)); t2.setTextFill(Color.valueOf("#1A5D3A"));
            Label t3 = new Label(String.format("Total Sampah: %.1f kg", totalKg));
            t3.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 14)); t3.setTextFill(Color.valueOf("#F39C12"));
            Label t4 = new Label(String.format("Estimasi CO2 Reduction: %.1f kg", totalKg * 2));
            t4.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 14)); t4.setTextFill(Color.valueOf("#10B981"));
            statsCard.getChildren().addAll(t1, t2, t3, t4);
        };
        datePicker.valueProperty().addListener((obs,o,n) -> updateStats.run());
        updateStats.run();

        // Table of transactions for selected date
        TableView<Penjemputan> table = createStyledTable();
        TableColumn<Penjemputan, String> tc1 = new TableColumn<>("ID");
        tc1.setCellValueFactory(d -> new SimpleStringProperty("#"+d.getValue().getId()));
        TableColumn<Penjemputan, String> tc2 = new TableColumn<>("Alamat");
        tc2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAlamat()));
        TableColumn<Penjemputan, String> tc3 = new TableColumn<>("Status");
        tc3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        TableColumn<Penjemputan, String> tc4 = new TableColumn<>("Total");
        tc4.setCellValueFactory
(d -> new SimpleStringProperty(d.getValue().getTransaksi() != null ? "Rp "+d.getValue().getTransaksi().getTotalHarga() : "-"));
        table.getColumns().addAll(tc1, tc2, tc3, tc4);

        // Chart container
        VBox chartBox = new VBox(12);
        chartBox.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 6, 0, 0, 1);");
        chartBox.setPadding(new Insets(20));

        Runnable updateTable = () -> {
            LocalDate selected = datePicker.getValue();
            List<Penjemputan> dayItems = penjemputanData.stream()
                .filter(p -> p.getTanggalJemput() != null && p.getTanggalJemput().equals(selected))
                .toList();
            table.setItems(FXCollections.observableArrayList(dayItems));
            updateCharts(dayItems, chartBox);
        };
        datePicker.valueProperty().addListener((obs,o,n) -> updateTable.run());
        updateTable.run();

        contentArea.getChildren().addAll(dateBox, statsRow, statsCard, table, chartBox);
    }

    private void updateCharts(List<Penjemputan> dayItems, VBox chartBox) {
        chartBox.getChildren().clear();

        // Title
        Label chartTitle = new Label("Grafik Setoran per Kategori");
        chartTitle.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 16));
        chartTitle.setTextFill(Color.valueOf("#1E293B"));
        chartBox.getChildren().add(chartTitle);

        // Count per kategori from catatan field
        Map<String, Integer> kategoriCount = new LinkedHashMap<>();
        kategoriCount.put("ORGANIK", 0);
        kategoriCount.put("ANORGANIK", 0);
        kategoriCount.put("B3", 0);
        kategoriCount.put("RESIDU", 0);

        for (Penjemputan p : dayItems) {
            String catatan = p.getCatatan() != null ? p.getCatatan().toUpperCase() : "";
            for (String kat : kategoriCount.keySet()) {
                if (catatan.contains(kat)) {
                    kategoriCount.put(kat, kategoriCount.get(kat) + 1);
                    break;
                }
            }
        }

        // If no data, show all statuses
        if (dayItems.isEmpty()) {
            Label empty = new Label("Belum ada data setoran untuk tanggal ini.");
            empty.setFont(Font.font("SF Pro Display", 13));
            empty.setTextFill(Color.valueOf("#94A3B8"));
            chartBox.getChildren().add(empty);
            return;
        }

        // Bar chart
        String[] kategori = {"ORGANIK", "ANORGANIK", "B3", "RESIDU"};
        String[] colors = {"#10B981", "#3B82F6", "#EF4444", "#6B7280"};
        int maxVal = 1;
        for (int v : kategoriCount.values()) if (v > maxVal) maxVal = v;

        VBox barChart = new VBox(8);
        for (int i = 0; i < kategori.length; i++) {
            int val = kategoriCount.get(kategori[i]);
            HBox bar = new HBox(10);
            bar.setAlignment(Pos.CENTER_LEFT);
            Label label = new Label(kategori[i]);
            label.setPrefWidth(80);
            label.setFont(Font.font("SF Pro Display", 12));
            label.setTextFill(Color.valueOf("#64748B"));
            double barWidth = (val * 1.0 / maxVal) * 350;
            if (barWidth < 4) barWidth = 4;
            VBox barViz = new VBox();
            barViz.setStyle("-fx-background-color: " + colors[i] + "; -fx-background-radius: 6; -fx-min-width: " + barWidth + "; -fx-pref-height: 28; -fx-max-width: " + barWidth + ";");
            Label valLabel = new Label(" " + val + " setoran");
            valLabel.setFont(Font.font("SF Pro Display", 11));
            valLabel.setTextFill(Color.valueOf("#64748B"));
            bar.getChildren().addAll(label, barViz, valLabel);
            barChart.getChildren().add(bar);
        }
        chartBox.getChildren().add(barChart);

        // Pie chart (simple visual using colored circles)
        Separator sep = new Separator();
        sep.setPadding(new Insets(8, 0, 8, 0));
        chartBox.getChildren().add(sep);

        Label pieTitle = new Label("Distribusi Status Penjemputan");
        pieTitle.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 14));
        pieTitle.setTextFill(Color.valueOf("#1E293B"));
        chartBox.getChildren().add(pieTitle);

        Map<String, Integer> statusCount = new LinkedHashMap<>();
        for (Penjemputan p : dayItems) {
            String st = p.getStatus() != null ? p.getStatus() : "UNKNOWN";
            statusCount.put(st, statusCount.getOrDefault(st, 0) + 1);
        }

        HBox pieRow = new HBox(16);
        pieRow.setPadding(new Insets(8, 0, 0, 0));
        for (Map.Entry<String, Integer> entry : statusCount.entrySet()) {
            String status = entry.getKey();
            int count = entry.getValue();
            String bg, fg;
            switch (status) {
                case "MENUNGGU": bg="#FEF3C7"; fg="#92400E"; break;
                case "DIJADWALKAN": bg="#DBEAFE"; fg="#1E40AF"; break;
                case "DIPROSES": bg="#F3E8FF"; fg="#6B21A8"; break;
                case "SELESAI": bg="#D1FAE5"; fg="#065F46"; break;
                case "DIBATALKAN": bg="#FEE2E2"; fg="#991B1B"; break;
                default: bg="#F1F5F9"; fg="#475569";
            }
            VBox statusBox = new VBox(4);
            statusBox.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 12; -fx-padding: 12 16;");
            Label stLabel = new Label(status);
            stLabel.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 11));
            stLabel.setTextFill(Color.valueOf(fg));
            Label stCount = new Label(count + " setoran");
            stCount.setFont(Font.font("SF Pro Display", 10));
            stCount.setTextFill(Color.valueOf(fg));
            statusBox.getChildren().addAll(stLabel, stCount);
            pieRow.getChildren().add(statusBox);
        }
        chartBox.getChildren().add(pieRow);
    }

    // ==================== RIWAYAT SETORAN ====================
    private void showRiwayatSetoran() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Riwayat Setoran & Pembayaran", "Semua transaksi yang telah selesai"));

        TableView<Penjemputan> t = createStyledTable();
        List<Penjemputan> selesai = penjemputanData.stream().filter(p -> "SELESAI".equals(p.getStatus())).toList();
        t.setItems(FXCollections.observableArrayList(selesai));
        TableColumn<Penjemputan, String> c1 = new TableColumn<>("ID");
        c1.setCellValueFactory(d -> new SimpleStringProperty("#"+d.getValue().getId()));
        TableColumn<Penjemputan, String> c2 = new TableColumn<>("Alamat");
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAlamat()));
        TableColumn<Penjemputan, String> c3 = new TableColumn<>("Tanggal");
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTanggalJemput()!=null?d.getValue().getTanggalJemput().toString():"-"));
        TableColumn<Penjemputan, String> c4 = new TableColumn<>("Total Bayar");
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTransaksi()!=null?"Rp "+d.getValue().getTransaksi().getTotalHarga():"-"));
        TableColumn<Penjemputan, String> c5 = new TableColumn<>("Status");
        c5.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTransaksi()!=null?d.getValue().getTransaksi().getStatus():"-"));
        t.getColumns().addAll(c1, c2, c3, c4, c5);

        // Summary
        BigDecimal grandTotal = BigDecimal.ZERO;
        for (Penjemputan p : selesai) {
            if (p.getTransaksi() != null) grandTotal = grandTotal.add(p.getTransaksi().getTotalHarga());
        }
        Label summary = new Label("Total semua pembayaran: Rp " + grandTotal + " | Total setoran: " + selesai.size());
        summary.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 16));
        summary.setTextFill(Color.valueOf("#1A5D3A"));
        summary.setPadding(new Insets(12, 0, 0, 0));

        contentArea.getChildren().addAll(t, summary);
    }

    // ==================== MANAJEMEN USER (VPS) ====================
    private void showUsers() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Manajemen User", "Kelola akun masyarakat, petugas, admin (VPS)"));
        HBar bar = new HBar(10);
        bar.add(createBtn("Refresh", "#3B82F6", "white", e -> showUsers()));
        contentArea.getChildren().addAll(bar.get(), new Separator());

        // Get users from VPS leaderboard (has userId, nama, poin, saldo)
        List<Map<String, Object>> vpsUsers = ApiService.getLeaderboard();
        List<Map<String,Object>> userList = new ArrayList<>();
        for (Map<String, Object> u : vpsUsers) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.get("userId"));
            m.put("nama", u.getOrDefault("nama", "?"));
            m.put("email", "-");
            m.put("role", "MASYARAKAT");
            m.put("telp", "-");
            m.put("saldo", u.getOrDefault("saldo", 0));
            m.put("poin", u.getOrDefault("poin", 0));
            userList.add(m);
        }
        if (userList.isEmpty()) userList = DummyData.getUserList();

        TableView<Map<String,Object>> t = createStyledTable();
        t.setItems(FXCollections.observableArrayList(userList));
        TableColumn<Map<String,Object>, String> c1 = new TableColumn<>("ID");
        c1.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("id"))));
        TableColumn<Map<String,Object>, String> c2 = new TableColumn<>("Nama");
        c2.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("nama"))));
        TableColumn<Map<String,Object>, String> c3 = new TableColumn<>("Role");
        c3.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("role"))));
        TableColumn<Map<String,Object>, String> c4 = new TableColumn<>("Saldo");
        c4.setCellValueFactory(d -> {
            Object s = d.getValue().get("saldo");
            return new SimpleStringProperty(s instanceof Number ? "Rp " + ((Number) s).doubleValue() : "-");
        });
        TableColumn<Map<String,Object>, String> c5 = new TableColumn<>("Poin");
        c5.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("poin"))));
        t.getColumns().addAll(c1, c2, c3, c4, c5);
        contentArea.getChildren().add(t);
    }

    // ==================== EDUKASI SAMPAH (VPS) ====================
    private void showEdukasi() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Edukasi Sampah", "Panduan kategori dan harga sampah (VPS)"));

        // Load from VPS
        List<Sampah> sampahList = ApiService.getSampah();
        if (sampahList.isEmpty()) sampahList = DummyData.getSampahList();

        // Convert to display format
        String[][] data = new String[sampahList.size()][4];
        for (int i = 0; i < sampahList.size(); i++) {
            Sampah s = sampahList.get(i);
            data[i][0] = s.getNama();
            data[i][1] = s.getKategori();
            data[i][2] = s.getDeskripsi() != null ? s.getDeskripsi() : "";
            data[i][3] = "Rp " + s.getHargaPerKg() + "/kg";
        }

        HBar filterBar = new HBar(8);
        String[] filters = {"Semua", "ORGANIK", "ANORGANIK", "B3", "RESIDU"};
        for (String f : filters) {
            String bg = f.equals("ORGANIK") ? "#10B981" : f.equals("ANORGANIK") ? "#3B82F6" : f.equals("B3") ? "#EF4444" : f.equals("RESIDU") ? "#6B7280" : "#1A5D3A";
            filterBar.add(createBtn(f, bg, "white", e -> {
                contentArea.getChildren().removeIf(n -> n instanceof TableView);
                TableView<String[]> t = createStyledTable();
                ObservableList<String[]> filtered = FXCollections.observableArrayList();
                for (String[] row : data) {
                    if (f.equals("Semua") || row[1].equals(f)) filtered.add(row);
                }
                t.setItems(filtered);
                TableColumn<String[], String> c1 = new TableColumn<>("Jenis Sampah");
                c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
                TableColumn<String[], String> c2 = new TableColumn<>("Kategori");
                c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
                TableColumn<String[], String> c3 = new TableColumn<>("Deskripsi");
                c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
                TableColumn<String[], String> c4 = new TableColumn<>("Harga");
                c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
                t.getColumns().addAll(c1, c2, c3, c4);
                contentArea.getChildren().add(t);
            }));
        }
        contentArea.getChildren().addAll(filterBar.get(), new Separator());

        TableView<String[]> t = createStyledTable();
        t.setItems(FXCollections.observableArrayList(Arrays.asList(data)));
        TableColumn<String[], String> c1 = new TableColumn<>("Jenis Sampah");
        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        TableColumn<String[], String> c2 = new TableColumn<>("Kategori");
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        TableColumn<String[], String> c3 = new TableColumn<>("Deskripsi");
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        TableColumn<String[], String> c4 = new TableColumn<>("Harga");
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
        t.getColumns().addAll(c1, c2, c3, c4);
        contentArea.getChildren().add(t);
    }

    // ==================== CHAT CLIENT (WhatsApp-style, Real-time WebSocket) ====================
    private javafx.animation.Timeline chatTimeline = null;
    private com.resik.admin.service.ChatWebSocketClient chatWsClient = null;
    private String currentChatUserId = null;
    private final Map<String, String> clientNames = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, String> clientPreviews = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, String> clientTimes = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, Boolean> clientHasUnread = new java.util.concurrent.ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private void showChat() {
        contentArea.getChildren().clear();

        // ---- WhatsApp-style container ----
        HBox chatRoot = new HBox();
        chatRoot.setStyle("-fx-background-color: #ECE5DD; -fx-background-radius: 16;");
        HBox.setHgrow(chatRoot, Priority.ALWAYS);
        VBox.setVgrow(chatRoot, Priority.ALWAYS);

        // ===== LEFT PANEL: Conversation list (like WhatsApp chat list) =====
        VBox leftPanel = new VBox();
        leftPanel.setPrefWidth(340);
        leftPanel.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 16 0 0 16; -fx-border-color: #E2E8F0; -fx-border-width: 0 1px 0 0;");

        // Green header
        HBox leftHeader = new HBox(10);
        leftHeader.setPadding(new Insets(16, 16, 12, 16));
        leftHeader.setStyle("-fx-background-color: #1A5D3A; -fx-background-radius: 16 0 0 0;");
        leftHeader.setAlignment(Pos.CENTER_LEFT);

        Label statusDot = new Label("●");
        statusDot.setFont(Font.font(16));
        statusDot.setTextFill(Color.valueOf("#FBBF24"));

        Label headerTitle = new Label("Percakapan");
        headerTitle.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 18));
        headerTitle.setTextFill(Color.WHITE);
        Region hdrSpacer = new Region();
        HBox.setHgrow(hdrSpacer, Priority.ALWAYS);
        Label refreshChatBtn = createBtn("⟳", "#2D7A4F", "white", e -> loadClientList());
        refreshChatBtn.setFont(Font.font(18));
        leftHeader.getChildren().addAll(statusDot, headerTitle, hdrSpacer, refreshChatBtn);

        // Conversation list
        ListView<String> clientList = new ListView<>();
        clientList.setId("clientList");
        clientList.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(clientList, Priority.ALWAYS);

        // Custom cell factory — WhatsApp style
        clientList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                // Parse: "userId|nama|preview|time|hasUnread"
                String[] parts = item.split("\\|", -1);
                String uid = parts.length > 0 ? parts[0] : "?";
                String nama = parts.length > 1 ? parts[1] : "User #" + uid;
                String preview = parts.length > 2 ? parts[2] : "";
                String time = parts.length > 3 ? parts[3] : "";
                boolean unread = parts.length > 4 && "1".equals(parts[4]);

                HBox row = new HBox(12);
                row.setPadding(new Insets(10, 14, 10, 14));
                row.setAlignment(Pos.CENTER_LEFT);

                Circle avatar = new Circle(24);
                avatar.setFill(Color.valueOf("#1A5D3A"));
                Label initial = new Label(nama.substring(0, 1).toUpperCase());
                initial.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 18));
                initial.setTextFill(Color.WHITE);
                StackPane avatarPane = new StackPane(avatar, initial);

                VBox textCol = new VBox(4);
                HBox.setHgrow(textCol, Priority.ALWAYS);
                Label nameLbl = new Label(nama);
                nameLbl.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 15));
                nameLbl.setTextFill(Color.valueOf("#1E293B"));
                Label previewLbl = new Label(preview);
                previewLbl.setFont(Font.font("SF Pro Display", 13));
                previewLbl.setTextFill(Color.valueOf("#64748B"));
                textCol.getChildren().addAll(nameLbl, previewLbl);

                VBox rightCol = new VBox(6);
                rightCol.setAlignment(Pos.CENTER_RIGHT);
                Label timeLbl = new Label(time);
                timeLbl.setFont(Font.font("SF Pro Display", 11));
                timeLbl.setTextFill(Color.valueOf("#94A3B8"));
                if (unread) {
                    Circle badge = new Circle(9);
                    badge.setFill(Color.valueOf("#22C55E"));
                    Label badgeDot = new Label("•");
                    badgeDot.setFont(Font.font(10));
                    badgeDot.setTextFill(Color.WHITE);
                    StackPane badgePane = new StackPane(badge, badgeDot);
                    rightCol.getChildren().addAll(timeLbl, badgePane);
                } else {
                    rightCol.getChildren().add(timeLbl);
                }

                row.getChildren().addAll(avatarPane, textCol, rightCol);
                setGraphic(row);
                setText(null);
                setStyle("-fx-background-color: " + (isSelected() ? "#F0F9F4" : (getIndex() % 2 == 0 ? "#FAFAFA" : "#FFFFFF")) + "; -fx-border-color: #F1F5F9; -fx-border-width: 0 0 1px 0;");
            }
        });

        leftPanel.getChildren().addAll(leftHeader, clientList);

        // ===== RIGHT PANEL: Chat conversation area =====
        VBox rightPanel = new VBox();
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        rightPanel.setStyle("-fx-background-color: #ECE5DD;");

        // Chat header bar (green, like WhatsApp)
        HBox chatHeaderBar = new HBox(12);
        chatHeaderBar.setId("chatHeaderBar");
        chatHeaderBar.setPadding(new Insets(12, 20, 12, 20));
        chatHeaderBar.setStyle("-fx-background-color: #1A5D3A;");
        chatHeaderBar.setAlignment(Pos.CENTER_LEFT);

        Circle chatAvatar = new Circle(20);
        chatAvatar.setFill(Color.valueOf("#2D7A4F"));
        Label chatAvatarInit = new Label("?");
        chatAvatarInit.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 16));
        chatAvatarInit.setTextFill(Color.WHITE);
        StackPane chatAvatarPane = new StackPane(chatAvatar, chatAvatarInit);

        VBox chatTitleBox = new VBox(2);
        Label chatName = new Label("Pilih percakapan");
        chatName.setId("chatName");
        chatName.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 17));
        chatName.setTextFill(Color.WHITE);
        Label chatStatus = new Label("");
        chatStatus.setId("chatStatus");
        chatStatus.setFont(Font.font("SF Pro Display", 12));
        chatStatus.setTextFill(Color.valueOf("#C8E6D0"));
        chatTitleBox.getChildren().addAll(chatName, chatStatus);

        Region hdrSpacer2 = new Region();
        HBox.setHgrow(hdrSpacer2, Priority.ALWAYS);
        chatHeaderBar.getChildren().addAll(chatAvatarPane, chatTitleBox, hdrSpacer2);

        // Messages area — ListView with custom chat bubbles
        ListView<HBox> chatList = new ListView<>();
        chatList.setId("chatList");
        chatList.setStyle("-fx-background-color: #ECE5DD; -fx-border-color: transparent;");
        VBox.setVgrow(chatList, Priority.ALWAYS);
        chatList.setCellFactory(lv -> new ListCell<HBox>() {
            @Override
            protected void updateItem(HBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: #ECE5DD; -fx-padding: 2 8;");
                } else {
                    setGraphic(item);
                    setStyle("-fx-background-color: #ECE5DD; -fx-padding: 2 8;");
                }
            }
        });

        // Input row
        HBox inputRow = new HBox(8);
        inputRow.setId("inputRow");
        inputRow.setPadding(new Insets(10, 20, 10, 20));
        inputRow.setStyle("-fx-background-color: #F0F0F0;");
        inputRow.setAlignment(Pos.CENTER);

        TextField msgField = new TextField();
        msgField.setId("msgField");
        msgField.setPromptText("Pilih percakapan dulu...");
        msgField.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 24; -fx-padding: 12 18; -fx-border-color: transparent; -fx-border-radius: 24; -fx-font-size: 15px;");
        msgField.setDisable(true);
        HBox.setHgrow(msgField, Priority.ALWAYS);
        msgField.setOnAction(e -> doAdminSend(msgField));

        Label sendBtn = createBtn("➤ Kirim", "#1A5D3A", "white", e -> doAdminSend(msgField));
        sendBtn.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 15));
        sendBtn.setStyle("-fx-background-color: #1A5D3A; -fx-text-fill: white; -fx-background-radius: 24; -fx-padding: 10 20; -fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;");

        inputRow.getChildren().addAll(msgField, sendBtn);
        rightPanel.getChildren().addAll(chatHeaderBar, chatList, inputRow);

        chatRoot.getChildren().addAll(leftPanel, rightPanel);
        contentArea.getChildren().add(chatRoot);

        // ---- Client selection handler ----
        clientList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String[] parts = newVal.split("\\|", -1);
            String uid = parts[0];
            String nama = clientNames.getOrDefault(uid, "User #" + uid);

            currentChatUserId = uid;
            msgField.setDisable(false);
            msgField.setPromptText("Ketik balasan...");
            chatName.setText(nama);
            chatStatus.setText("Online");
            chatAvatarInit.setText(nama.substring(0, 1).toUpperCase());

            clientHasUnread.put(uid, false);
            loadClientList();
            loadChatMessages(uid);
            markMessagesRead(uid);
        });

        loadClientList();

        // Auto-select first conversation
        javafx.application.Platform.runLater(() -> {
            if (clientList.getItems() != null && !clientList.getItems().isEmpty()) {
                clientList.getSelectionModel().select(0);
            }
        });

        connectChatWebSocket(statusDot, chatStatus);
    }

    private void connectChatWebSocket(Label statusDot, Label chatStatus) {
        if (chatWsClient != null) {
            chatWsClient.disconnect();
            chatWsClient = null;
        }

        chatWsClient = new com.resik.admin.service.ChatWebSocketClient(
            AppConfig.API_BASE_URL,
            msg -> javafx.application.Platform.runLater(() -> {
                String type = String.valueOf(msg.getOrDefault("type", ""));
                if ("NEW_MESSAGE".equals(type)) {
                    Object userIdObj = msg.get("userId");
                    String msgUserId = userIdObj != null ? String.valueOf(userIdObj) : "";
                    String sender = String.valueOf(msg.getOrDefault("pengirim", ""));
                    String pesan = String.valueOf(msg.getOrDefault("pesan", ""));
                    Object createdObj = msg.get("createdAt");
                    String createdAt = createdObj != null ? String.valueOf(createdObj) : "";
                    String time = createdAt.length() > 16 ? createdAt.substring(11, 16) : createdAt;

                    clientPreviews.put(msgUserId, pesan.length() > 30 ? pesan.substring(0, 27) + "..." : pesan);
                    clientTimes.put(msgUserId, time);

                    if (!"ADMIN".equals(sender) && !msgUserId.equals(currentChatUserId)) {
                        clientHasUnread.put(msgUserId, true);
                    }

                    loadClientList();

                    if (msgUserId.equals(currentChatUserId)) {
                        ListView<HBox> chatList = findChatBubbleList();
                        if (chatList != null) {
                            boolean fromAdmin = "ADMIN".equals(sender);
                            HBox bubble = createChatBubble(pesan, time, fromAdmin);
                            chatList.getItems().add(bubble);
                            chatList.scrollTo(chatList.getItems().size() - 1);
                        }
                        if (!"ADMIN".equals(sender)) {
                            markMessagesRead(msgUserId);
                        }
                    } else {
                        chatStatus.setText("Pesan baru!");
                    }
                }
            }),
            () -> javafx.application.Platform.runLater(() -> {
                statusDot.setTextFill(Color.valueOf("#22C55E"));
                if (currentChatUserId != null) chatStatus.setText("Online");
            }),
            () -> javafx.application.Platform.runLater(() -> {
                statusDot.setTextFill(Color.valueOf("#EF4444"));
                chatStatus.setText("Menghubungkan...");
            })
        );

        Thread wsThread = new Thread(() -> chatWsClient.connect());
        wsThread.setDaemon(true);
        wsThread.setName("ws-chat-client");
        wsThread.start();

        if (chatTimeline != null) chatTimeline.stop();
        chatTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(5), e -> {
                loadClientList();
                if (currentChatUserId != null && !currentChatUserId.isEmpty()) {
                    loadChatMessages(currentChatUserId);
                }
            })
        );
        chatTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        chatTimeline.play();
    }

    @SuppressWarnings("unchecked")
    private void loadClientList() {
        ListView<String> clientList = (ListView<String>) contentArea.getChildren().stream()
            .filter(n -> n instanceof HBox)
            .map(n -> (HBox) n)
            .flatMap(hb -> hb.getChildren().stream())
            .filter(n -> n instanceof VBox)
            .map(n -> (VBox) n)
            .flatMap(vb -> vb.getChildren().stream())
            .filter(n -> n instanceof ListView && "clientList".equals(n.getId()))
            .findFirst()
            .map(n -> (ListView<String>) n)
            .orElse(null);
        if (clientList == null) return;

        try {
            List<Map<String, Object>> latestChats = ApiClient.getList("/chat/latest");
            ObservableList<String> items = FXCollections.observableArrayList();
            for (Map<String, Object> m : latestChats) {
                String userId;
                String userDisplay;
                Object userObj = m.get("user");
                if (userObj instanceof Map) {
                    Map<String, Object> userMap = (Map<String, Object>) userObj;
                    userId = String.valueOf(userMap.getOrDefault("id", m.get("userId")));
                    userDisplay = String.valueOf(userMap.getOrDefault("nama", "User #" + userId));
                } else {
                    userId = m.get("userId") != null ? String.valueOf(m.get("userId")) : "?";
                    userDisplay = "User #" + userId;
                }
                clientNames.put(userId, userDisplay);

                String sender = String.valueOf(m.getOrDefault("pengirim", "?"));
                String preview = String.valueOf(m.getOrDefault("pesan", ""));
                if (preview.length() > 30) preview = preview.substring(0, 27) + "...";
                String time = String.valueOf(m.getOrDefault("createdAt", ""));
                if (time.length() > 16) time = time.substring(11, 16);

                clientPreviews.put(userId, preview);
                clientTimes.put(userId, time);

                Boolean isRead = (Boolean) m.get("isRead");
                boolean unread = "USER".equals(sender) && (isRead == null || !isRead);
                if (unread) clientHasUnread.put(userId, true);

                items.add(userId + "|" + userDisplay + "|" + preview + "|" + time + "|" + (clientHasUnread.getOrDefault(userId, false) ? "1" : "0"));
            }
            String selected = currentChatUserId;
            clientList.setItems(items);
            if (selected != null) {
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).startsWith(selected + "|")) {
                        clientList.getSelectionModel().select(i);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // Silent fail
        }
    }

    @SuppressWarnings("unchecked")
    private void loadChatMessages(String userId) {
        ListView<HBox> chatList = findChatBubbleList();
        if (chatList == null) return;
        try {
            List<Map<String, Object>> messages = ApiClient.getList("/chat/" + userId);
            ObservableList<HBox> items = FXCollections.observableArrayList();
            for (Map<String, Object> m : messages) {
                String sender = String.valueOf(m.getOrDefault("pengirim", "?"));
                String pesan = String.valueOf(m.getOrDefault("pesan", ""));
                String time = String.valueOf(m.getOrDefault("createdAt", ""));
                if (time.length() > 16) time = time.substring(11, 16);
                boolean fromAdmin = "ADMIN".equals(sender);
                items.add(createChatBubble(pesan, time, fromAdmin));
            }
            chatList.setItems(items);
            if (!items.isEmpty()) {
                chatList.scrollTo(items.size() - 1);
            }
        } catch (Exception e) {
            if (chatList.getItems() == null || chatList.getItems().isEmpty()) {
                HBox errBubble = createChatBubble("Gagal memuat pesan: " + e.getMessage(), "", false);
                chatList.setItems(FXCollections.observableArrayList(errBubble));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private ListView<HBox> findChatBubbleList() {
        return contentArea.getChildren().stream()
            .filter(n -> n instanceof HBox)
            .map(n -> (HBox) n)
            .flatMap(hb -> hb.getChildren().stream())
            .filter(n -> n instanceof VBox)
            .map(n -> (VBox) n)
            .flatMap(vb -> vb.getChildren().stream())
            .filter(n -> n instanceof ListView && "chatList".equals(n.getId()))
            .findFirst()
            .map(n -> (ListView<HBox>) n)
            .orElse(null);
    }

    /**
     * Creates a WhatsApp-style chat bubble.
     * Admin messages: right-aligned, green background.
     * Client messages: left-aligned, white background.
     */
    private HBox createChatBubble(String pesan, String time, boolean fromAdmin) {
        HBox row = new HBox();
        row.setPadding(new Insets(6, 16, 6, 16));

        // Bubble container
        VBox bubble = new VBox(2);
        bubble.setMaxWidth(420);
        bubble.setPadding(new Insets(10, 14, 8, 14));

        if (fromAdmin) {
            // Admin (me) — right side, green bubble
            row.setAlignment(Pos.CENTER_RIGHT);
            bubble.setStyle("-fx-background-color: #DCF8C6; -fx-background-radius: 12 12 4 12;");
        } else {
            // Client — left side, white bubble
            row.setAlignment(Pos.CENTER_LEFT);
            bubble.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12 12 12 4;");
        }

        Label msgLbl = new Label(pesan);
        msgLbl.setFont(Font.font("SF Pro Display", 15));
        msgLbl.setTextFill(Color.valueOf("#1E293B"));
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(380);

        HBox metaRow = new HBox();
        metaRow.setAlignment(Pos.CENTER_RIGHT);
        Label timeLbl = new Label(time);
        timeLbl.setFont(Font.font("SF Pro Display", 11));
        timeLbl.setTextFill(Color.valueOf("#94A3B8"));
        if (fromAdmin) {
            Label tick = new Label(" ✓✓");
            tick.setFont(Font.font("SF Pro Display", 11));
            tick.setTextFill(Color.valueOf("#34B7F1"));
            metaRow.getChildren().addAll(timeLbl, tick);
        } else {
            metaRow.getChildren().add(timeLbl);
        }

        bubble.getChildren().addAll(msgLbl, metaRow);
        row.getChildren().add(bubble);
        return row;
    }

    private void doAdminSend(TextField msgField) {
        String uid = currentChatUserId;
        String msg = msgField.getText().trim();
        if (uid == null || uid.isEmpty() || msg.isEmpty()) {
            CrudDialog.showInfo("Info", "Pilih percakapan dan ketik pesan.");
            return;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("userId", Long.parseLong(uid));
            body.put("pengirim", "ADMIN");
            body.put("pesan", msg);
            ApiClient.post("/chat", body, Map.class);
            msgField.clear();
            loadChatMessages(uid);
        } catch (Exception ex) {
            CrudDialog.showInfo("Error", "Gagal kirim: " + ex.getMessage());
        }
    }

    private void markMessagesRead(String userId) {
        try {
            ApiClient.put("/chat/" + userId + "/read", null, Void.class);
        } catch (Exception ignored) {
        }
    }

    // ==================== LOKASI BANK SAMPAH (VPS CRUD) ====================
    private void showLokasi() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Lokasi Bank Sampah", "Kelola lokasi bank sampah (VPS)"));

        HBar bar = new HBar(10);
        bar.add(createBtn("Tambah Lokasi", "#1A5D3A", "white", e -> {
            CrudDialog.showForm("Tambah Lokasi", "Simpan",
                List.of(
                    new CrudDialog.FormField("nama", "Nama", "Bank Sampah Resik..."),
                    new CrudDialog.FormField("alamat", "Alamat", "Jl. ..."),
                    new CrudDialog.FormField("jam", "Jam Operasional", "08:00-17:00"),
                    new CrudDialog.FormField("telepon", "Telepon", "0812-...")
                ), null
            ).ifPresent(vals -> {
                try {
                    ApiService.createLokasi(vals.get("nama"), vals.get("alamat"), vals.get("jam"), vals.get("telepon"));
                    showLokasi();
                    CrudDialog.showInfo("Berhasil", "Lokasi ditambahkan ke VPS.");
                } catch (Exception ex) {
                    CrudDialog.showInfo("Error", "Gagal tambah: " + ex.getMessage());
                }
            });
        }));
        bar.add(createBtn("Refresh", "#3B82F6", "white", e -> showLokasi()));
        contentArea.getChildren().addAll(bar.get(), new Separator());

        List<Map<String, Object>> lokasiList = ApiService.getLokasi();
        String[][] lokasiData;
        if (lokasiList.isEmpty()) {
            lokasiData = new String[][] {
                {"Bank Sampah Resik Pusat", "Jl. Merdeka No. 45, Jakarta Pusat", "08:00-17:00", "0812-3456-7890"},
                {"Bank Sampah Resik Selatan", "Jl. Margonda Raya No. 10, Depok", "07:00-16:00", "0813-9876-5432"},
                {"Bank Sampah Resik Timur", "Jl. Raya Bekasi KM 20, Bekasi", "08:00-16:00", "0815-1111-2222"},
                {"Bank Sampah Resik Barat", "Jl. Daan Mogot No. 88, Tangerang", "09:00-17:00", "0817-3333-4444"}
            };
        } else {
            lokasiData = new String[lokasiList.size()][4];
            for (int i = 0; i < lokasiList.size(); i++) {
                Map<String, Object> l = lokasiList.get(i);
                lokasiData[i][0] = String.valueOf(l.getOrDefault("nama", ""));
                lokasiData[i][1] = String.valueOf(l.getOrDefault("alamat", ""));
                lokasiData[i][2] = String.valueOf(l.getOrDefault("jamOperasional", ""));
                lokasiData[i][3] = String.valueOf(l.getOrDefault("telepon", ""));
            }
        }

        TableView<String[]> t = createStyledTable();
        t.setItems(FXCollections.observableArrayList(Arrays.asList(lokasiData)));
        TableColumn<String[], String> c1 = new TableColumn<>("Nama");
        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        TableColumn<String[], String> c2 = new TableColumn<>("Alamat");
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        TableColumn<String[], String> c3 = new TableColumn<>("Jam Operasional");
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        TableColumn<String[], String> c4 = new TableColumn<>("Telepon");
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
        t.getColumns().addAll(c1, c2, c3, c4);

        ContextMenu cm = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Hapus Lokasi");
        deleteItem.setOnAction(e -> {
            String[] sel = t.getSelectionModel().getSelectedItem();
            if (sel != null) {
                for (Map<String, Object> l : lokasiList) {
                    if (String.valueOf(l.get("nama")).equals(sel[0])) {
                        try {
                            ApiService.deleteLokasi(((Number) l.get("id")).longValue());
                            showLokasi();
                            CrudDialog.showInfo("Berhasil", "Lokasi dihapus dari VPS.");
                        } catch (Exception ex) {
                            CrudDialog.showInfo("Error", "Gagal hapus: " + ex.getMessage());
                        }
                        return;
                    }
                }
            }
        });
        cm.getItems().add(deleteItem);
        t.setContextMenu(cm);
        contentArea.getChildren().add(t);

        VBox info = new VBox(8);
        info.setPadding(new Insets(16, 0, 0, 0));
        Label infoLabel = new Label("Lokasi tersimpan di VPS. Data sinkron antara Android dan Desktop.");
        infoLabel.setFont(Font.font("SF Pro Display", 13));
        infoLabel.setTextFill(Color.valueOf("#64748B"));
        info.getChildren().add(infoLabel);
        contentArea.getChildren().add(info);
    }

    // ==================== PROFIL ====================
    private void showProfil() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Profil", "Informasi akun administrator"));

        VBox card = new VBox(16);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 28; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 8, 0, 0, 2);");

        GridPane grid = new GridPane();
        grid.setHgap(16); grid.setVgap(12);

        TextField namaField = new TextField(AppConfig.userName);
        namaField.setStyle("-fx-background-radius: 8; -fx-padding: 8 12; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");
        TextField emailField = new TextField(AppConfig.userEmail != null ? AppConfig.userEmail : "admin@resik.id");
        emailField.setStyle("-fx-background-radius: 8; -fx-padding: 8 12; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");
        TextField telpField = new TextField("0812-0000-0000");
        telpField.setStyle("-fx-background-radius: 8; -fx-padding: 8 12; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");
        TextArea alamatArea = new TextArea("Jl. Resik Admin No. 1, Jakarta");
        alamatArea.setStyle("-fx-background-radius: 8; -fx-padding: 8 12; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");
        alamatArea.setPrefRowCount(2);

        grid.add(new Label("Nama:"), 0, 0); grid.add(namaField, 1, 0);
        grid.add(new Label("Email:"), 0, 1); grid.add(emailField, 1, 1);
        grid.add(new Label("No. Telepon:"), 0, 2); grid.add(telpField, 1, 2);
        grid.add(new Label("Alamat:"), 0, 3); grid.add(alamatArea, 1, 3);

        Label roleLabel = new Label("Role: " + AppConfig.userRole);
        roleLabel.setFont(Font.font("SF Pro Display", 13)); roleLabel.setTextFill(Color.valueOf("#64748B"));
        Label idLabel = new Label("User ID: " + AppConfig.userId);
        idLabel.setFont(Font.font("SF Pro Display", 13)); idLabel.setTextFill(Color.valueOf("#64748B"));

        HBar actionBar = new HBar(10);
        actionBar.add(createBtn("Simpan Perubahan", "#1A5D3A", "white", e -> {
            AppConfig.userName = namaField.getText().trim();
            AppConfig.userEmail = emailField.getText().trim();
            CrudDialog.showInfo("Berhasil", "Profil berhasil diperbarui.");
            showProfil();
        }));
        actionBar.add(createBtn("Upload Foto", "#3B82F6", "white", e -> {
            CrudDialog.showInfo("Info", "Fitur upload foto akan tersedia di versi mendatang.");
        }));

        card.getChildren().addAll(grid, roleLabel, idLabel, actionBar.get());
        contentArea.getChildren().add(card);
    }

    // ==================== HELPER: Button & HBar ====================
    private Label createBtn(String text, String bg, String fg, javafx.event.EventHandler<javafx.scene.input.MouseEvent> handler) {
        Label btn = new Label(text);
        btn.setStyle("-fx-background-color: "+bg+"; -fx-text-fill: "+fg+"; -fx-background-radius: 12; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: 500;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: "+bg+"; -fx-text-fill: "+fg+"; -fx-background-radius: 12; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: 500; -fx-opacity: 0.85;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: "+bg+"; -fx-text-fill: "+fg+"; -fx-background-radius: 12; -fx-padding: 8 16; -fx-font-size: 13px; -fx-cursor: hand; -fx-font-weight: 500;"));
        btn.setOnMouseClicked(handler);
        return btn;
    }

    private static class HBar {
        private final HBox box;
        HBar(int spacing) { box = new HBox(spacing); box.setPadding(new Insets(0,0,8,0)); }
        void add(javafx.scene.Node n) { box.getChildren().add(n); }
        HBox get() { return box; }
    }

    public BorderPane getView() { return view; }
}
