package com.resik.admin.ui;

import com.resik.admin.config.AppConfig;
import com.resik.admin.model.Penjemputan;
import com.resik.admin.model.Sampah;
import com.resik.admin.service.DummyData;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

public class DashboardView {

    private final BorderPane view;
    private final VBox contentArea;
    private VBox activeNav = null;

    public DashboardView() {
        view = new BorderPane();
        view.setTop(createTopBar());
        view.setLeft(createSidebar());
        contentArea = new VBox();
        contentArea.setPadding(new Insets(28, 32, 28, 32));
        contentArea.setStyle("-fx-background-color: #f1f5f9;");
        contentArea.setSpacing(16);
        view.setCenter(contentArea);
        showDashboard();
    }

    private HBox createTopBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(14, 28, 14, 28));
        bar.setStyle("-fx-background-color: #0d6e4e;");
        bar.setAlignment(Pos.CENTER_LEFT);
        HBox brand = new HBox(10);
        brand.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(14);
        dot.setFill(Color.WHITE);
        Text r = new Text("R");
        r.setFill(Color.valueOf("#0d6e4e"));
        r.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 14));
        StackPane logo = new StackPane(dot, r);
        Label title = new Label("RESIK");
        title.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 18));
        title.setTextFill(Color.WHITE);
        Label sub = new Label("Bank Sampah Digital");
        sub.setFont(Font.font("SF Pro Display", 12));
        sub.setTextFill(Color.valueOf("#a7d4c4"));
        brand.getChildren().addAll(logo, title, sub);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox userSection = new HBox(12);
        userSection.setAlignment(Pos.CENTER_RIGHT);
        VBox userBox = new VBox();
        userBox.setAlignment(Pos.CENTER_RIGHT);
        Label un = new Label(AppConfig.userName);
        un.setTextFill(Color.WHITE);
        un.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 13));
        Label rl = new Label(AppConfig.userRole);
        rl.setTextFill(Color.valueOf("#a7d4c4"));
        rl.setFont(Font.font("SF Pro Display", 11));
        userBox.getChildren().addAll(un, rl);
        Circle av = new Circle(18);
        av.setFill(Color.valueOf("#1a8a68"));
        Text ini = new Text(AppConfig.userName.substring(0, 1).toUpperCase());
        ini.setFill(Color.WHITE);
        ini.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 14));
        StackPane avP = new StackPane(av, ini);
        Separator sep = new Separator();
        sep.setOrientation(javafx.geometry.Orientation.VERTICAL);
        sep.setPrefHeight(30);
        Button out = new Button("Keluar");
        out.setStyle("-fx-background-color: transparent; -fx-text-fill: #e57373; -fx-border-color: #e57373; -fx-border-width: 1px; -fx-border-radius: 6; -fx-padding: 6 16; -fx-font-size: 12px; -fx-cursor: hand;");
        out.setOnMouseEntered(e -> out.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-border-color: #c0392b; -fx-border-width: 1px; -fx-border-radius: 6; -fx-padding: 6 16; -fx-font-size: 12px;"));
        out.setOnMouseExited(e -> out.setStyle("-fx-background-color: transparent; -fx-text-fill: #e57373; -fx-border-color: #e57373; -fx-border-width: 1px; -fx-border-radius: 6; -fx-padding: 6 16; -fx-font-size: 12px;"));
        out.setOnAction(e -> { AppConfig.authToken = null; AppConfig.userId = null;
            Stage st = (Stage) view.getScene().getWindow();
            LoginView lv = new LoginView();
            Scene sc = new Scene(lv.getView(), 400, 520);
            sc.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            st.setScene(sc); st.setResizable(false);
        });
        userSection.getChildren().addAll(userBox, avP, sep, out);
        bar.getChildren().addAll(brand, spacer, userSection);
        return bar;
    }

    private VBox createSidebar() {
        VBox sb = new VBox();
        sb.setPrefWidth(220);
        sb.setStyle("-fx-background-color: #0f4d3a;");
        sb.setPadding(new Insets(16, 10, 16, 10));
        sb.setSpacing(4);
        Label mh = new Label("MENU");
        mh.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 10));
        mh.setTextFill(Color.valueOf("#5e9484"));
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
            createNavButton("Laporan", e -> showLaporan()),
            createNavButton("Chat", e -> showChat()),
            createNavButton("Manajemen User", e -> showUsers()),
            createNavButton("Profil", e -> showProfil()));
        sb.getChildren().addAll(mh, nav);
        return sb;
    }

    private VBox createNavButton(String label, javafx.event.EventHandler<javafx.scene.input.MouseEvent> handler) {
        VBox btn = new VBox();
        String n = "-fx-background-color: transparent; -fx-padding: 10 16; -fx-background-radius: 8; -fx-cursor: hand;";
        String h = "-fx-background-color: rgba(255,255,255,0.08); -fx-padding: 10 16; -fx-background-radius: 8; -fx-cursor: hand;";
        String a = "-fx-background-color: rgba(255,255,255,0.12); -fx-padding: 10 16; -fx-background-radius: 8; -fx-cursor: hand;";
        btn.setStyle(n);
        btn.setOnMouseEntered(e -> { if (btn != activeNav) btn.setStyle(h); });
        btn.setOnMouseExited(e -> { if (btn != activeNav) btn.setStyle(n); });
        btn.setOnMouseClicked(e -> {
            if (activeNav != null) activeNav.setStyle(n);
            btn.setStyle(a); activeNav = btn; handler.handle(e);
        });
        Label t = new Label(label);
        t.setTextFill(Color.valueOf("#cbd5e1"));
        t.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 13));
        btn.getChildren().add(t);
        return btn;
    }

    private VBox createPageHeader(String t, String d) {
        VBox h = new VBox(4);
        Label tl = new Label(t);
        tl.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 24));
        tl.setTextFill(Color.valueOf("#1e293b"));
        Label dl = new Label(d);
        dl.setFont(Font.font("SF Pro Display", 13));
        dl.setTextFill(Color.valueOf("#94a3b8"));
        h.getChildren().addAll(tl, dl);
        return h;
    }

    private <T> TableView<T> createStyledTable() {
        TableView<T> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return t;
    }

    private void showDashboard() {
        contentArea.getChildren().clear();
        VBox header = createPageHeader("Dashboard", "Ringkasan operasional bank sampah");
        Map<String, Integer> s = DummyData.getStats();
        HBox sr = new HBox(16);
        sr.getChildren().addAll(
            createStatCard("Menunggu", s.get("MENUNGGU"), "#f59e0b"),
            createStatCard("Dijadwalkan", s.get("DIJADWALKAN"), "#3b82f6"),
            createStatCard("Selesai", s.get("SELESAI"), "#10b981"),
            createStatCard("Total Pengguna", s.get("TOTAL_USER"), "#8b5cf6"));
        VBox rb = new VBox(12);
        Label rt = new Label("Penjemputan Terbaru");
        rt.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 15));
        rt.setTextFill(Color.valueOf("#1e293b"));
        VBox list = new VBox(6);
        for (Penjemputan p : DummyData.getPenjemputanList()) {
            HBox row = new HBox(20);
            row.setStyle("-fx-background-color: white; -fx-padding: 14 18; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 6, 0, 0, 1);");
            row.setAlignment(Pos.CENTER_LEFT);
            Label idL = new Label("#" + String.format("%03d", p.getId()));
            idL.setPrefWidth(50);
            idL.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 12));
            idL.setTextFill(Color.valueOf("#94a3b8"));
            VBox ab = new VBox(2);
            Label al = new Label(p.getAlamat());
            al.setFont(Font.font("SF Pro Display", 13));
            al.setTextFill(Color.valueOf("#334155"));
            Label cl = new Label(p.getCatatan());
            cl.setFont(Font.font("SF Pro Display", 11));
            cl.setTextFill(Color.valueOf("#94a3b8"));
            ab.getChildren().addAll(al, cl);
            ab.setPrefWidth(300);
            Label tl = new Label(p.getTanggalJemput() != null ? p.getTanggalJemput().toString() : "-");
            tl.setPrefWidth(100);
            tl.setFont(Font.font("SF Pro Display", 12));
            tl.setTextFill(Color.valueOf("#64748b"));
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label sb = new Label(p.getStatus());
            sb.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 10));
            sb.setPadding(new Insets(4, 10, 4, 10));
            String bg = switch (p.getStatus()) {
                case "MENUNGGU" -> "#fef3c7"; case "DIJADWALKAN" -> "#dbeafe";
                case "DIPROSES" -> "#f3e8ff"; case "SELESAI" -> "#d1fae5";
                case "DIBATALKAN" -> "#fee2e2"; default -> "#f1f5f9";};
            String fg = switch (p.getStatus()) {
                case "MENUNGGU" -> "#92400e"; case "DIJADWALKAN" -> "#1e40af";
                case "DIPROSES" -> "#6b21a8"; case "SELESAI" -> "#065f46";
                case "DIBATALKAN" -> "#991b1b"; default -> "#475569";};
            sb.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-background-radius: 4;");
            row.getChildren().addAll(idL, ab, tl, sp, sb);
            list.getChildren().add(row);
        }
        rb.getChildren().addAll(rt, list);
        contentArea.getChildren().addAll(header, sr, rb);
    }

    private VBox createStatCard(String label, int value, String color) {
        VBox c = new VBox(6);
        c.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 12; -fx-padding: 20 24; -fx-pref-width: 160; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");
        Label v = new Label(String.valueOf(value));
        v.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 32));
        v.setTextFill(Color.WHITE);
        Label l = new Label(label);
        l.setFont(Font.font("SF Pro Display", 12));
        l.setTextFill(Color.valueOf("#e2e8f0"));
        c.getChildren().addAll(v, l);
        return c;
    }

    private void showPenjemputan() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Manajemen Penjemputan", "Kelola semua permintaan penjemputan sampah"));

        HBox actionBar = CrudDialog.createActionBar(
            e -> {
                Optional<Map<String, String>> result = CrudDialog.showForm(
                    "Tambah Penjemputan", "Simpan",
                    List.of(
                        new CrudDialog.FormField("alamat", "Alamat Penjemputan", "Jl. Contoh No. 1"),
                        new CrudDialog.FormField("tanggal", "Tanggal Jemput (YYYY-MM-DD)", "2026-10-25"),
                        new CrudDialog.FormField("waktu", "Waktu Jemput", "09:00-11:00"),
                        new CrudDialog.FormField("catatan", "Catatan", "Jenis sampah dan estimasi berat")
                    ), null
                );
                result.ifPresent(vals -> {
                    CrudDialog.showInfo("Berhasil", "Penjemputan baru telah ditambahkan (demo mode).");
                    showPenjemputan();
                });
            },
            null,
            e -> { CrudDialog.showInfo("Info", "Klik dua kali pada baris untuk melihat detail. Untuk hapus, pilih baris lalu konfirmasi."); }
        );
        contentArea.getChildren().addAll(actionBar, new Separator());

        TableView<Penjemputan> t = createStyledTable();
        TableColumn<Penjemputan, String> c1 = new TableColumn<>("ID");
        c1.setCellValueFactory(d -> new SimpleStringProperty(String.format("%03d", d.getValue().getId())));
        TableColumn<Penjemputan, String> c2 = new TableColumn<>("Alamat");
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAlamat()));
        TableColumn<Penjemputan, String> c3 = new TableColumn<>("Tanggal");
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTanggalJemput() != null ? d.getValue().getTanggalJemput().toString() : "-"));
        TableColumn<Penjemputan, String> c4 = new TableColumn<>("Waktu");
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getWaktuJemput() != null ? d.getValue().getWaktuJemput() : "-"));
        TableColumn<Penjemputan, String> c5 = new TableColumn<>("Status");
        c5.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        t.getColumns().addAll(c1, c2, c3, c4, c5);
        t.getItems().addAll(DummyData.getPenjemputanList());
        contentArea.getChildren().add(t);
    }

    private void showTimbang() {
        contentArea.getChildren().clear();
        VBox h = createPageHeader("Penimbangan Sampah", "Input berat aktual dan kalkulasi harga otomatis");
        VBox ic = new VBox(10);
        ic.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 24; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);");
        Label i = new Label("Pilih penjemputan dengan status DIPROSES untuk melakukan penimbangan.\nSistem akan menghitung subtotal otomatis berdasarkan berat aktual dikali harga per kg.");
        i.setFont(Font.font("SF Pro Display", 14));
        i.setTextFill(Color.valueOf("#475569"));
        i.setWrapText(true);
        ic.getChildren().add(i);
        contentArea.getChildren().addAll(h, ic);
    }

    private void showSampah() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Data Sampah", "Kelola jenis sampah dan harga per kilogram"));

        HBox actionBar = CrudDialog.createActionBar(
            e -> {
                Optional<Map<String, String>> result = CrudDialog.showForm(
                    "Tambah Jenis Sampah", "Simpan",
                    List.of(
                        new CrudDialog.FormField("nama", "Nama Sampah", "Contoh: Botol Plastik"),
                        new CrudDialog.FormField("kategori", "Kategori", "ORGANIK / ANORGANIK / B3"),
                        new CrudDialog.FormField("harga", "Harga per kg (Rp)", "Contoh: 2500"),
                        new CrudDialog.FormField("deskripsi", "Deskripsi", "Keterangan singkat")
                    ), null
                );
                result.ifPresent(vals -> {
                    DummyData.addSampah(vals.get("nama"), vals.get("kategori"), vals.get("harga"), vals.get("deskripsi"));
                    showSampah();
                    CrudDialog.showInfo("Berhasil", "Jenis sampah baru telah ditambahkan.");
                });
            },
            null,
            e -> { CrudDialog.showInfo("Info", "Klik dua kali pada baris untuk mengedit. Untuk hapus, klik tombol Hapus lalu konfirmasi."); }
        );
        contentArea.getChildren().addAll(actionBar, new Separator());

        TableView<Sampah> t = createStyledTable();
        TableColumn<Sampah, String> c1 = new TableColumn<>("Nama");
        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNama()));
        TableColumn<Sampah, String> c2 = new TableColumn<>("Kategori");
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getKategori()));
        TableColumn<Sampah, String> c3 = new TableColumn<>("Harga / kg");
        c3.setCellValueFactory(d -> new SimpleStringProperty("Rp " + d.getValue().getHargaPerKg()));
        TableColumn<Sampah, String> c4 = new TableColumn<>("Deskripsi");
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDeskripsi()));
        t.getColumns().addAll(c1, c2, c3, c4);
        t.getItems().addAll(DummyData.getSampahList());

        t.setRowFactory(tv -> {
            TableRow<Sampah> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Sampah sel = row.getItem();
                    Map<String, String> init = new HashMap<>();
                    init.put("nama", sel.getNama());
                    init.put("kategori", sel.getKategori());
                    init.put("harga", sel.getHargaPerKg().toString());
                    init.put("deskripsi", sel.getDeskripsi());
                    CrudDialog.showForm("Edit Jenis Sampah", "Update",
                        List.of(
                            new CrudDialog.FormField("nama", "Nama Sampah", ""),
                            new CrudDialog.FormField("kategori", "Kategori", ""),
                            new CrudDialog.FormField("harga", "Harga per kg (Rp)", ""),
                            new CrudDialog.FormField("deskripsi", "Deskripsi", "")
                        ), init
                    ).ifPresent(vals -> {
                        DummyData.updateSampah(sel.getId(), vals.get("nama"), vals.get("kategori"), vals.get("harga"), vals.get("deskripsi"));
                        showSampah();
                        CrudDialog.showInfo("Berhasil", "Data sampah telah diperbarui.");
                    });
                }
            });
            return row;
        });

        contentArea.getChildren().add(t);
    }

    private void showLaporan() {
        contentArea.getChildren().clear();
        VBox header = createPageHeader("Laporan Transaksi", "Laporan harian, bulanan, dan tahunan");
        HBox exportButtons = new HBox(12);
        Button pdf = new Button("Export PDF");
        pdf.setStyle("-fx-background-color: #0d6e4e; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 10 20; -fx-font-size: 13px; -fx-cursor: hand;");
        Button xls = new Button("Export Excel");
        xls.setStyle("-fx-background-color: white; -fx-text-fill: #0d6e4e; -fx-border-color: #0d6e4e; -fx-border-width: 1px; -fx-background-radius: 6; -fx-padding: 10 20; -fx-font-size: 13px; -fx-cursor: hand;");
        exportButtons.getChildren().addAll(pdf, xls);

        VBox lingCard = new VBox(12);
        lingCard.setStyle("-fx-background-color: #0d6e4e; -fx-background-radius: 12; -fx-padding: 24; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");
        Label lingTitle = new Label("Laporan Lingkungan");
        lingTitle.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 16));
        lingTitle.setTextFill(Color.WHITE);

        Map<String, Object> l = DummyData.getLaporanLingkungan();
        HBox statsRow = new HBox(24);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        for (var entry : l.entrySet()) {
            VBox s = new VBox(4);
            Label val = new Label(String.valueOf(entry.getValue()));
            val.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 18));
            val.setTextFill(Color.WHITE);
            Label lbl = new Label(entry.getKey());
            lbl.setFont(Font.font("SF Pro Display", 11));
            lbl.setTextFill(Color.valueOf("#a7d4c4"));
            s.getChildren().addAll(val, lbl);
            statsRow.getChildren().add(s);
        }
        lingCard.getChildren().addAll(lingTitle, statsRow);

        VBox ic = new VBox(10);
        ic.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 24; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);");
        Label info = new Label("Laporan transaksi harian, bulanan, dan tahunan akan ditampilkan di sini.\nGunakan tombol di atas untuk mengekspor laporan.");
        info.setFont(Font.font("SF Pro Display", 14));
        info.setTextFill(Color.valueOf("#475569"));
        info.setWrapText(true);
        ic.getChildren().add(info);
        contentArea.getChildren().addAll(header, exportButtons, lingCard, ic);
    }

    private void showChat() {
        contentArea.getChildren().clear();
        VBox h = createPageHeader("Chat", "Percakapan dengan masyarakat");
        VBox ic = new VBox(10);
        ic.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 24; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);");
        Label i = new Label("Daftar percakapan dengan masyarakat akan ditampilkan di sini.");
        i.setFont(Font.font("SF Pro Display", 14));
        i.setTextFill(Color.valueOf("#475569"));
        i.setWrapText(true);
        ic.getChildren().add(i);
        contentArea.getChildren().addAll(h, ic);
    }

    private void showDompet() {
        contentArea.getChildren().clear();
        VBox header = createPageHeader("Dompet Digital", "Saldo dan riwayat transaksi semua pengguna");

        VBox list = new VBox(6);
        for (Map<String, Object> d : DummyData.getDompetList()) {
            HBox row = new HBox(20);
            row.setStyle("-fx-background-color: white; -fx-padding: 14 18; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 6, 0, 0, 1);");
            row.setAlignment(Pos.CENTER_LEFT);
            Label nm = new Label((String) d.get("nama"));
            nm.setFont(Font.font("SF Pro Display", 14));
            nm.setTextFill(Color.valueOf("#334155"));
            nm.setPrefWidth(250);
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label sd = new Label((String) d.get("saldo"));
            sd.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 14));
            sd.setTextFill(Color.valueOf("#0d6e4e"));
            sd.setPrefWidth(120);
            Label pi = new Label((String) d.get("poin"));
            pi.setFont(Font.font("SF Pro Display", 13));
            pi.setTextFill(Color.valueOf("#f59e0b"));
            row.getChildren().addAll(nm, sp, sd, pi);
            list.getChildren().add(row);
        }
        contentArea.getChildren().addAll(header, list);
    }

    private void showLeaderboard() {
        contentArea.getChildren().clear();
        VBox header = createPageHeader("Leaderboard", "Ranking kontribusi masyarakat");
        VBox list = new VBox(6);
        String[] names = {"Syukron Maulana", "Zain Ghathfaan", "Riri Syafala", "Ahmad Fauzi", "Siti Nurhaliza"};
        int[] poins = {320, 280, 250, 180, 120};
        for (int idx = 0; idx < names.length; idx++) {
            HBox row = new HBox(16);
            row.setStyle("-fx-background-color: white; -fx-padding: 14 18; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 6, 0, 0, 1);");
            row.setAlignment(Pos.CENTER_LEFT);
            Label rank = new Label(String.valueOf(idx + 1));
            rank.setPrefWidth(30);
            rank.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 16));
            rank.setTextFill(idx == 0 ? Color.valueOf("#f59e0b") : idx == 1 ? Color.valueOf("#94a3b8") : idx == 2 ? Color.valueOf("#cd7f32") : Color.valueOf("#94a3b8"));
            Label nm = new Label(names[idx]);
            nm.setFont(Font.font("SF Pro Display", 14));
            nm.setTextFill(Color.valueOf("#334155"));
            nm.setPrefWidth(250);
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label pi = new Label(poins[idx] + " poin");
            pi.setFont(Font.font("SF Pro Display", FontWeight.MEDIUM, 13));
            pi.setTextFill(Color.valueOf("#0d6e4e"));
            row.getChildren().addAll(rank, nm, sp, pi);
            list.getChildren().add(row);
        }
        contentArea.getChildren().addAll(header, list);
    }

    private void showProfil() {
        contentArea.getChildren().clear();
        VBox header = createPageHeader("Profil", "Informasi akun administrator");
        VBox card = new VBox(16);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 28; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 8, 0, 0, 2);");
        Label nm = new Label(AppConfig.userName);
        nm.setFont(Font.font("SF Pro Display", FontWeight.BOLD, 20));
        nm.setTextFill(Color.valueOf("#1e293b"));
        Label rl = new Label("Role: " + AppConfig.userRole);
        rl.setFont(Font.font("SF Pro Display", 13));
        rl.setTextFill(Color.valueOf("#64748b"));
        Label em = new Label("Email: admin@resik.id");
        em.setFont(Font.font("SF Pro Display", 13));
        em.setTextFill(Color.valueOf("#64748b"));
        Label id = new Label("User ID: " + AppConfig.userId);
        id.setFont(Font.font("SF Pro Display", 13));
        id.setTextFill(Color.valueOf("#64748b"));
        card.getChildren().addAll(nm, rl, em, id);
        contentArea.getChildren().addAll(header, card);
    }

    private void showUsers() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createPageHeader("Manajemen User", "Kelola data semua pengguna"));

        HBox actionBar = CrudDialog.createActionBar(
            e -> {
                Optional<Map<String, String>> result = CrudDialog.showForm(
                    "Tambah User Baru", "Simpan",
                    List.of(
                        new CrudDialog.FormField("nama", "Nama Lengkap", "Contoh: Budi Santoso"),
                        new CrudDialog.FormField("email", "Email", "Contoh: budi@mail.com"),
                        new CrudDialog.FormField("password", "Password", "Minimal 6 karakter"),
                        new CrudDialog.FormField("role", "Role", "MASYARAKAT / PETUGAS / ADMIN"),
                        new CrudDialog.FormField("alamat", "Alamat", "Alamat lengkap"),
                        new CrudDialog.FormField("telp", "No. Telepon", "08xxxxxxxxxx")
                    ), null
                );
                result.ifPresent(vals -> {
                    DummyData.addUser(vals.get("nama"), vals.get("email"), vals.get("role"), vals.get("alamat"), vals.get("telp"));
                    showUsers();
                    CrudDialog.showInfo("Berhasil", "User baru telah ditambahkan.");
                });
            },
            null,
            e -> { CrudDialog.showInfo("Info", "Klik dua kali pada baris untuk mengedit data user."); }
        );
        contentArea.getChildren().addAll(actionBar, new Separator());

        TableView<Map<String, Object>> table = createStyledTable();
        TableColumn<Map<String, Object>, String> c1 = new TableColumn<>("ID");
        c1.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("id"))));
        TableColumn<Map<String, Object>, String> c2 = new TableColumn<>("Nama");
        c2.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("nama"))));
        TableColumn<Map<String, Object>, String> c3 = new TableColumn<>("Email");
        c3.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("email"))));
        TableColumn<Map<String, Object>, String> c4 = new TableColumn<>("Role");
        c4.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("role"))));
        TableColumn<Map<String, Object>, String> c5 = new TableColumn<>("No. Telp");
        c5.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("telp"))));
        table.getColumns().addAll(c1, c2, c3, c4, c5);
        table.getItems().addAll(DummyData.getUserList());

        table.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Map<String, Object> sel = row.getItem();
                    Map<String, String> init = new HashMap<>();
                    init.put("nama", String.valueOf(sel.get("nama")));
                    init.put("email", String.valueOf(sel.get("email")));
                    init.put("role", String.valueOf(sel.get("role")));
                    init.put("alamat", String.valueOf(sel.get("alamat")));
                    init.put("telp", String.valueOf(sel.get("telp")));
                    CrudDialog.showForm("Edit User", "Update",
                        List.of(
                            new CrudDialog.FormField("nama", "Nama Lengkap", ""),
                            new CrudDialog.FormField("email", "Email", ""),
                            new CrudDialog.FormField("role", "Role", "MASYARAKAT / PETUGAS / ADMIN"),
                            new CrudDialog.FormField("alamat", "Alamat", ""),
                            new CrudDialog.FormField("telp", "No. Telepon", "")
                        ), init
                    ).ifPresent(vals -> {
                        DummyData.updateUser((Long) sel.get("id"), vals.get("nama"), vals.get("email"), vals.get("role"), vals.get("alamat"), vals.get("telp"));
                        showUsers();
                        CrudDialog.showInfo("Berhasil", "Data user telah diperbarui.");
                    });
                }
            });
            return row;
        });

        contentArea.getChildren().add(table);
    }

    public BorderPane getView() {
        return view;
    }
}
