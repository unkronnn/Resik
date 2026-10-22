package com.resik.admin.ui;

import com.resik.admin.config.AppConfig;
import com.resik.admin.model.Penjemputan;
import com.resik.admin.model.Sampah;
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
    private final ObservableList<Penjemputan> penjemputanData = FXCollections.observableArrayList(DummyData.getPenjemputanList());

    public DashboardView() {
        view = new BorderPane();
        view.setTop(createTopBar());
        view.setLeft(createSidebar());
        contentArea = new VBox();
        contentArea.setPadding(new Insets(28, 32, 28, 32));
        contentArea.setStyle("-fx-background-color: #F5F7F5;");
        contentArea.setSpacing(16);
        view.setCenter(contentArea);
        showDashboard();
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
        contentArea.getChildren().addAll(sr, rb);
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
                    new CrudDialog.FormField("alamat", "Alamat Penjemputan", "Jl. Contoh No. 1"),
                    new CrudDialog.FormField("tanggal", "Tanggal (YYYY-MM-DD)", "2026-10-25"),
                    new CrudDialog.FormField("waktu", "Waktu", "09:00-11:00"),
                    new CrudDialog.FormField("catatan", "Catatan", "Jenis sampah dan estimasi")
                ), null
            ).ifPresent(vals -> {
                Penjemputan p = new Penjemputan();
                p.setId((long)(penjemputanData.size() + 1));
                p.setAlamat(vals.get("alamat"));
                try { p.setTanggalJemput(LocalDate.parse(vals.get("tanggal"))); } catch(Exception ex) { p.setTanggalJemput(LocalDate.now()); }
                p.setWaktuJemput(vals.get("waktu"));
                p.setCatatan(vals.get("catatan"));
                p.setStatus("MENUNGGU");
                p.setQrCode("RESIK-" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
                penjemputanData.add(p);
                showPenjemputan();
                CrudDialog.showInfo("Berhasil", "Penjemputan baru ditambahkan.");
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
        t.getColumns().addAll(c1, c2, c3, c4, c5);
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

    // ==================== DATA SAMPAH ====================
    private void showSampah() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Data Sampah", "Kelola jenis sampah dan harga per kilogram"));
        HBar bar = new HBar(10);
        bar.add(createBtn("Tambah Sampah", "#1A5D3A", "white", e -> {
            CrudDialog.showForm("Tambah Jenis Sampah", "Simpan",
                List.of(
                    new CrudDialog.FormField("nama", "Nama Sampah", "Contoh: Botol Plastik"),
                    new CrudDialog.FormField("kategori", "Kategori", "ORGANIK/ANORGANIK/B3"),
                    new CrudDialog.FormField("harga", "Harga per kg (Rp)", "2500"),
                    new CrudDialog.FormField("deskripsi", "Deskripsi", "")
                ), null
            ).ifPresent(vals -> {
                DummyData.addSampah(vals.get("nama"), vals.get("kategori"), vals.get("harga"), vals.get("deskripsi"));
                showSampah();
                CrudDialog.showInfo("Berhasil", "Jenis sampah ditambahkan.");
            });
        }));
        contentArea.getChildren().addAll(bar.get(), new Separator());

        TableView<Sampah> t = createStyledTable();
        t.setItems(FXCollections.observableArrayList(DummyData.getSampahList()));
        TableColumn<Sampah, String> c1 = new TableColumn<>("Nama");
        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNama()));
        TableColumn<Sampah, String> c2 = new TableColumn<>("Kategori");
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getKategori()));
        TableColumn<Sampah, String> c3 = new TableColumn<>("Harga/kg");
        c3.setCellValueFactory(d -> new SimpleStringProperty("Rp " + d.getValue().getHargaPerKg()));
        TableColumn<Sampah, String> c4 = new TableColumn<>("Deskripsi");
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDeskripsi()));
        t.getColumns().addAll(c1, c2, c3, c4);
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
                        DummyData.updateSampah(sel.getId(), vals.get("nama"), vals.get("kategori"), vals.get("harga"), vals.get("deskripsi"));
                        showSampah();
                        CrudDialog.showInfo("Berhasil", "Data sampah diupdate.");
                    });
                }
            });
            return row;
        });
        contentArea.getChildren().add(t);
    }

    // ==================== DOMPET DIGITAL ====================
    private void showDompet() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Dompet Digital", "Saldo dan riwayat transaksi semua pengguna"));
        VBox list = new VBox(6);
        for (Map<String,Object> d : DummyData.getDompetList()) {
            HBox row = new HBox(20);
            row.setStyle("-fx-background-color: white; -fx-padding: 14 18; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 6, 0, 0, 1);");
            row.setAlignment(Pos.CENTER_LEFT);
            Label nm = new Label((String) d.get("nama"));
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
        contentArea.getChildren().add(createPageHeader("Leaderboard", "Ranking kontribusi masyarakat"));
        VBox list = new VBox(6);
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
        HBox dateBox = new HBox(10, dateLabel, datePicker);
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

        Runnable updateTable = () -> {
            LocalDate selected = datePicker.getValue();
            table.setItems(FXCollections.observableArrayList(penjemputanData.stream()
                .filter(p -> p.getTanggalJemput() != null && p.getTanggalJemput().equals(selected))
                .toList()));
        };
        datePicker.valueProperty().addListener((obs,o,n) -> updateTable.run());
        updateTable.run();

        contentArea.getChildren().addAll(dateBox, statsRow, statsCard, table);
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

    // ==================== MANAJEMEN USER ====================
    private void showUsers() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Manajemen User", "Kelola akun masyarakat, petugas, admin — role-based access"));
        HBar bar = new HBar(10);
        bar.add(createBtn("Tambah User", "#1A5D3A", "white", e -> {
            CrudDialog.showForm("Tambah User", "Simpan",
                List.of(
                    new CrudDialog.FormField("nama","Nama Lengkap","Budi Santoso"),
                    new CrudDialog.FormField("email","Email","budi@mail.com"),
                    new CrudDialog.FormField("password","Password","min 6 karakter"),
                    new CrudDialog.FormField("role","Role","MASYARAKAT/PETUGAS/ADMIN"),
                    new CrudDialog.FormField("alamat","Alamat",""),
                    new CrudDialog.FormField("telp","No. Telepon","08xx")
                ), null
            ).ifPresent(vals -> {
                DummyData.addUser(vals.get("nama"), vals.get("email"), vals.get("role"), vals.get("alamat"), vals.get("telp"));
                showUsers();
                CrudDialog.showInfo("Berhasil", "User baru ditambahkan.");
            });
        }));
        contentArea.getChildren().addAll(bar.get(), new Separator());

        TableView<Map<String,Object>> t = createStyledTable();
        t.setItems(FXCollections.observableArrayList(DummyData.getUserList()));
        TableColumn<Map<String,Object>, String> c1 = new TableColumn<>("ID");
        c1.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("id"))));
        TableColumn<Map<String,Object>, String> c2 = new TableColumn<>("Nama");
        c2.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("nama"))));
        TableColumn<Map<String,Object>, String> c3 = new TableColumn<>("Email");
        c3.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("email"))));
        TableColumn<Map<String,Object>, String> c4 = new TableColumn<>("Role");
        c4.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("role"))));
        TableColumn<Map<String,Object>, String> c5 = new TableColumn<>("No. Telp");
        c5.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("telp"))));
        t.getColumns().addAll(c1, c2, c3, c4, c5);
        t.setRowFactory(tv -> {
            TableRow<Map<String,Object>> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount()==2 && !row.isEmpty()) {
                    Map<String,Object> sel = row.getItem();
                    Map<String,String> init = new HashMap<>();
                    init.put("nama",String.valueOf(sel.get("nama"))); init.put("email",String.valueOf(sel.get("email")));
                    init.put("role",String.valueOf(sel.get("role"))); init.put("alamat",String.valueOf(sel.get("alamat")));
                    init.put("telp",String.valueOf(sel.get("telp")));
                    CrudDialog.showForm("Edit User","Update",
                        List.of(
                            new CrudDialog.FormField("nama","Nama",""), new CrudDialog.FormField("email","Email",""),
                            new CrudDialog.FormField("role","Role","MASYARAKAT/PETUGAS/ADMIN"),
                            new CrudDialog.FormField("alamat","Alamat",""), new CrudDialog.FormField("telp","No. Telepon","")
                        ), init
                    ).ifPresent(vals -> {
                        DummyData.updateUser((Long)sel.get("id"), vals.get("nama"), vals.get("email"), vals.get("role"), vals.get("alamat"), vals.get("telp"));
                        showUsers();
                        CrudDialog.showInfo("Berhasil","Data user diupdate.");
                    });
                }
            });
            return row;
        });
        contentArea.getChildren().add(t);
    }

    // ==================== PROFIL ====================
    private void showProfil() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Profil", "Informasi akun administrator"));
        VBox card = new VBox(16);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 28; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 8, 0, 0, 2);");
        Label nm = new Label(AppConfig.userName);
        nm.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 20)); nm.setTextFill(Color.valueOf("#1E293B"));
        Label rl = new Label("Role: " + AppConfig.userRole);
        rl.setFont(Font.font("SF Pro Display", 13)); rl.setTextFill(Color.valueOf("#64748B"));
        Label em = new Label("Email: admin@resik.id");
        em.setFont(Font.font("SF Pro Display", 13)); em.setTextFill(Color.valueOf("#64748B"));
        Label id = new Label("User ID: " + AppConfig.userId);
        id.setFont(Font.font("SF Pro Display", 13)); id.setTextFill(Color.valueOf("#64748B"));
        card.getChildren().addAll(nm, rl, em, id);
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
