package com.resik.admin.ui;

import com.resik.admin.config.AppConfig;
import com.resik.admin.model.Penjemputan;
import com.resik.admin.model.Sampah;
import com.resik.admin.service.ApiClient;
import com.resik.admin.service.ApiService;
import com.resik.admin.service.DummyData;
import com.resik.admin.ui.design.EmptyState;
import com.resik.admin.ui.design.GlassCanvas;
import com.resik.admin.ui.design.Icon;
import com.resik.admin.ui.design.PageHeader;
import com.resik.admin.ui.design.Sidebar;
import com.resik.admin.ui.design.StatCard;
import com.resik.admin.ui.design.Theme;
import com.resik.admin.ui.design.TopBar;
import com.resik.admin.ui.design.Ui;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.stage.StageStyle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** Liquid-glass admin dashboard: floating sidebar, floating topbar and frosted pages. */
public class DashboardView {

    private final StackPane view;
    private final ScrollPane contentScroll;
    private final VBox contentArea;
    private final Sidebar sidebar;
    private final TopBar topBar;
    private final ObservableList<Penjemputan> penjemputanData = FXCollections.observableArrayList();
    private String leaderboardPeriod = "Bulanan";
    private String sampahFilter = "Semua";
    private String eduFilter = "Semua";
    private String dompetMode = "Masyarakat";
    private String laporanBank = "Semua Bank Sampah";
    private int unreadChat = 16;
    private Label chatUnreadLabel;
    private StackPane chatBubbleWrap;
    private String chartRange = "Mingguan";
    private final ObservableList<EdukasiArticle> articles = FXCollections.observableArrayList();

    public DashboardView() {
        view = new StackPane();
        view.getChildren().add(new GlassCanvas());

        BorderPane shell = new BorderPane();
        shell.setStyle("-fx-background-color: transparent;");

        sidebar = createSidebar();
        BorderPane.setMargin(sidebar, new Insets(16, 6, 16, 16));
        shell.setLeft(sidebar);

        topBar = createTopBar();

        contentArea = new VBox(16);
        contentArea.setPadding(new Insets(22, 26, 22, 26));
        contentScroll = Ui.scroll(contentArea);
        VBox.setVgrow(contentScroll, Priority.ALWAYS);

        VBox centre = new VBox(14);
        centre.setPadding(new Insets(16, 16, 16, 6));
        centre.getChildren().addAll(topBar, contentScroll);
        shell.setCenter(centre);

        view.getChildren().add(shell);

        javafx.scene.Node chatBubble = chatBubbleButton();
        StackPane.setAlignment(chatBubble, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(chatBubble, new Insets(0, 24, 24, 0));
        view.getChildren().add(chatBubble);

        loadPenjemputanFromVPS();
        showDashboard();
        connectEventWebSocket();
    }

    private void loadPenjemputanFromVPS() {
        try {
            List<Penjemputan> list = ApiService.getPenjemputan();
            penjemputanData.setAll(list);
        } catch (Exception e) {
            penjemputanData.setAll(DummyData.getPenjemputanList());
        }
    }

    /* ---------------- realtime events (/ws/events) ---------------- */

    /**
     * Opens the shared realtime event channel. Unlike the chat channel, this
     * one is connected once for the whole desktop session (not per popup) and
     * drives live updates: a pickup created on Android appears in the
     * penjemputan table, a price edited here reaches Android instantly, and
     * a timbang refreshes wallet, stock and leaderboard views.
     */
    private void connectEventWebSocket() {
        if (eventWsClient != null) {
            eventWsClient.disconnect();
            eventWsClient = null;
        }
        String role = AppConfig.userRole == null ? "ADMIN" : AppConfig.userRole;
        eventWsClient = new com.resik.admin.service.RealtimeEventClient(
                AppConfig.API_BASE_URL,
                role,
                msg -> javafx.application.Platform.runLater(() -> onRealtimeEvent(msg)),
                () -> System.out.println("[realtime] connected to /ws/events"),
                () -> System.out.println("[realtime] disconnected, retrying...")
        );
        Thread wsThread = new Thread(() -> eventWsClient.connect());
        wsThread.setDaemon(true);
        wsThread.setName("ws-events");
        wsThread.start();
    }

    /**
     * Handles one realtime event. The rule is: update the shared data first,
     * then re-render only the view that is currently open. Views that are not
     * open re-fetch from the server when navigated to, so they are fresh too.
     * Unknown event types are ignored so the server can add types later.
     */
    @SuppressWarnings("unchecked")
    private void onRealtimeEvent(Map<String, Object> msg) {
        String type = String.valueOf(msg.getOrDefault("type", ""));
        switch (type) {
            case "PENJEMPUTAN_CREATED":
            case "PENJEMPUTAN_STATUS":
                // The penjemputan table is bound to penjemputanData, so
                // reloading the list updates the visible rows live. The
                // status-count chips refresh on next navigation.
                new Thread(() -> loadPenjemputanFromVPS()).start();
                if ("dashboard".equals(currentView)) {
                    new Thread(() -> {
                        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                        javafx.application.Platform.runLater(this::showDashboard);
                    }).start();
                }
                break;
            case "SAMPAH_CREATED":
            case "HARGA_SAMPAH_UPDATED":
            case "SAMPAH_DELETED":
                if ("sampah".equals(currentView)) showSampah();
                break;
            case "WALLET_UPDATED":
                if ("dompet".equals(currentView)) showDompet();
                break;
            case "LEADERBOARD_UPDATED":
                if ("leaderboard".equals(currentView)) showLeaderboard();
                break;
            case "STOK_UPDATED":
                if ("stok".equals(currentView)) showStokSampah();
                break;
            case "ACCOUNT_VERIFICATION":
                if ("verifikasi".equals(currentView)) showVerifikasi();
                break;
            default:
                // Ignore unknown types (forward compatibility).
                break;
        }
    }

    /* ---------------- shell ---------------- */

    private Sidebar createSidebar() {
        String role = AppConfig.userRole == null ? "ADMIN" : AppConfig.userRole.toUpperCase();
        Sidebar sb = new Sidebar();
        sb.addSection("Ringkasan");
        sb.addItem("dashboard", "Dashboard", Icon.DASHBOARD, this::showDashboard);

        sb.addSection("Operasional");
        if ("BANK_SAMPAH".equals(role)) {
            sb.addItem("penjemputan", "Penjemputan", Icon.PICKUP, this::showPenjemputan);
            sb.addItem("jadwal", "Jadwal Penjemputan", Icon.RIWAYAT, this::showJadwal);
            sb.addItem("timbangan", "Input Timbangan", Icon.TIMBANG, this::showTimbangan);
            sb.addItem("stok", "Stok Sampah", Icon.SAMPAH, this::showStokSampah);
        } else {
            sb.addItem("penjemputan", "Penjemputan", Icon.PICKUP, this::showPenjemputan);
            sb.addItem("jadwal", "Jadwal Penjemputan", Icon.RIWAYAT, this::showJadwal);
            sb.addItem("verifikasi", "Verifikasi & Approval", Icon.USERS, this::showVerifikasi);
            sb.addItem("bankmanage", "Manajemen Bank Sampah", Icon.LOKASI, this::showManajemenBank);
        }

        sb.addSection("Data");
        sb.addItem("sampah", "Data Sampah", Icon.SAMPAH, this::showSampah);
        sb.addItem("edukasi", "Edukasi Sampah", Icon.EDUKASI, this::showEdukasi);
        sb.addItem("lokasi", "Lokasi Bank Sampah", Icon.LOKASI, this::showLokasi);

        sb.addSection("Komunikasi");
        sb.addItem("chat", "Chat Client", Icon.CHAT, this::showChatPopup);

        sb.addSection("Keuangan");
        sb.addItem("dompet", "Dompet Digital", Icon.WALLET, this::showDompet);
        sb.addItem("leaderboard", "Leaderboard", Icon.LEADERBOARD, this::showLeaderboard);
        if ("ADMIN".equals(role)) {
            sb.addItem("laporan", "Laporan Harian", Icon.LAPORAN, this::showLaporanHarian);
        } else {
            sb.addItem("laporanbank", "Laporan Bank", Icon.LAPORAN, this::showLaporanBank);
        }
        sb.addItem("riwayat", "Riwayat Setoran", Icon.RIWAYAT, this::showRiwayatSetoran);

        sb.addSection("Sistem");
        if ("ADMIN".equals(role)) {
            sb.addItem("users", "Manajemen User", Icon.USERS, this::showUsers);
            sb.addItem("audit", "Audit Log", Icon.RIWAYAT, this::showAuditLog);
        }
        sb.addItem("profil", "Profil", Icon.PROFIL, this::showProfil);

        sb.select("dashboard");
        sb.getChildren().add(accountFab());
        return sb;
    }

    private TopBar createTopBar() {
        TopBar bar = new TopBar();
        String role = AppConfig.userRole == null ? "ADMIN" : AppConfig.userRole.toUpperCase();
        bar.addLeft(Ui.muted("Bank Sampah Digital"));
        bar.addLeft(Ui.badge(role, Theme.ACCENT, Theme.ACCENT_SOFT, Theme.ACCENT_BORDER));
        return bar;
    }

    private javafx.scene.Node chatBubbleButton() {
        Button btn = Ui.button("Pesan", "btn-primary", e -> showChatPopup());
        btn.setStyle("-fx-background-color: #1A5D3A; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10 16; -fx-font-weight: 800;");
        Circle dot = new Circle(9, Color.web("#DC2626"));
        chatUnreadLabel = new Label(String.valueOf(unreadChat));
        chatUnreadLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: 800;");
        StackPane badge = new StackPane(dot, chatUnreadLabel);
        chatBubbleWrap = new StackPane(btn, badge);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(-4, -4, 0, 0));
        return chatBubbleWrap;
    }

    private void pulseChatBubble() {
        if (chatBubbleWrap == null) return;
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(180), chatBubbleWrap);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.12);
        st.setToY(1.12);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    private javafx.scene.Node accountFab() {
        String role = AppConfig.userRole == null ? "ADMIN" : AppConfig.userRole.toUpperCase();
        String displayRole = role.equals("BANK_SAMPAH") ? "Bank Sampah" : role.equals("MASYARAKAT") ? "Masyarakat" : "Admin";
        String name = AppConfig.userName == null ? "Administrator" : AppConfig.userName;

        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 12, 10, 12));
        card.setStyle("-fx-background-color: #F9FBF9; -fx-background-radius: 14;"
                + " -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 14;"
                + " -fx-cursor: hand;");

        StackPane avatar = new StackPane();
        avatar.setMinSize(36, 36);
        avatar.setPrefSize(36, 36);
        avatar.setMaxSize(36, 36);
        avatar.setStyle("-fx-background-color: #1A5D3A; -fx-background-radius: 18;");
        Label initial = new Label(name.substring(0, 1).toUpperCase());
        initial.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: white;");
        avatar.getChildren().add(initial);

        VBox info = new VBox(1);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT + ";");
        Label roleLbl = new Label(displayRole);
        roleLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        info.getChildren().addAll(nameLbl, roleLbl);

        Label chevron = new Label("›");
        chevron.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: " + Theme.TEXT_MUTED + ";");

        card.getChildren().addAll(avatar, info, chevron);

        javafx.scene.control.ContextMenu menu = new javafx.scene.control.ContextMenu();
        javafx.scene.control.MenuItem profile = new javafx.scene.control.MenuItem("Profil Saya");
        profile.setOnAction(e -> showProfil());
        javafx.scene.control.MenuItem settings = new javafx.scene.control.MenuItem("Pengaturan Akun");
        settings.setOnAction(e -> showProfil());
        javafx.scene.control.MenuItem logout = new javafx.scene.control.MenuItem("Keluar");
        logout.setOnAction(e -> logout());
        menu.getItems().addAll(profile, settings, new javafx.scene.control.SeparatorMenuItem(), logout);

        card.setOnMouseClicked(e -> menu.show(card, javafx.geometry.Side.TOP, 0, 8));
        return card;
    }

    private void logout() {
        AppConfig.authToken = null;
        AppConfig.userId = null;
        Stage stage = (Stage) view.getScene().getWindow();
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getView(), 520, 720);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setResizable(true);
        stage.centerOnScreen();
    }

    private PageHeader pageHeader(String title, String description) {
        return new PageHeader(title, description);
    }

    private <T> TableView<T> styledTable() {
        return Ui.table();
    }

    private static String todayLabel() {
        LocalDate today = LocalDate.now();
        String day = today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("id"));
        String month = today.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("id"));
        return day + ", " + today.getDayOfMonth() + " " + month + " " + today.getYear();
    }

    /* ---------------- DASHBOARD ---------------- */

    private void showDashboard() {
        currentView = "dashboard";
        contentArea.getChildren().clear();

        PageHeader header = new PageHeader("Dashboard", "Ringkasan operasional bank sampah", todayLabel());
        contentArea.getChildren().add(header);

        Map<String, Integer> s = DummyData.getStats();
        StatCard menunggu = new StatCard("Menunggu", String.valueOf(s.getOrDefault("MENUNGGU", 0)), Icon.PICKUP, Theme.TINT_AMBER);
        menunggu.setHint("butuh penjadwalan");
        StatCard dijadwalkan = new StatCard("Dijadwalkan", String.valueOf(s.getOrDefault("DIJADWALKAN", 0)), Icon.TIMBANG, Theme.TINT_BLUE);
        dijadwalkan.setHint("siap dijemput");
        StatCard selesai = new StatCard("Selesai", String.valueOf(s.getOrDefault("SELESAI", 0)), Icon.SAMPAH, Theme.TINT_GREEN);
        selesai.setHint("setoran berhasil");
        StatCard totalUser = new StatCard("Total User", String.valueOf(s.getOrDefault("TOTAL_USER", 0)), Icon.USERS, Theme.TINT_VIOLET);
        totalUser.setHint("masyarakat aktif");

        HBox statsRow = new HBox(14);
        HBox.setHgrow(menunggu, Priority.ALWAYS);
        HBox.setHgrow(dijadwalkan, Priority.ALWAYS);
        HBox.setHgrow(selesai, Priority.ALWAYS);
        HBox.setHgrow(totalUser, Priority.ALWAYS);
        statsRow.getChildren().addAll(menunggu, dijadwalkan, selesai, totalUser);
        contentArea.getChildren().add(statsRow);

        VBox chartBox = Ui.card();
        chartBox.getChildren().add(Ui.cardHeader("Tren Setoran per Kategori", "Pilih rentang waktu untuk melihat tren setoran."));

        HBox rangeBar = new HBox(8);
        for (String range : new String[]{"Harian", "Mingguan", "Bulanan"}) {
            Button rangeBtn = Ui.button(range, range.equals(chartRange) ? "btn-primary" : "btn-secondary", e -> {
                chartRange = range;
                showDashboard();
            });
            rangeBar.getChildren().add(rangeBtn);
        }
        chartBox.getChildren().add(rangeBar);

        LineChart<String, Number> categoryChart = createCategoryLineChart();
        categoryChart.setPrefHeight(240);
        chartBox.getChildren().add(categoryChart);

        VBox recentBox = Ui.card();
        recentBox.getChildren().add(Ui.cardHeader("Penjemputan Terbaru", "Permintaan penjemputan terbaru dari masyarakat."));
        VBox list = new VBox(8);
        for (Penjemputan p : penjemputanData) {
            HBox row = Ui.rowCard(16);
            Label idL = new Label("#" + String.format("%03d", p.getId()));
            idL.setPrefWidth(52);
            idL.setStyle("-fx-font-size: 12px; -fx-font-weight: 800; -fx-text-fill: " + Theme.TEXT_SUBTLE + ";");
            VBox ab = new VBox(2);
            HBox.setHgrow(ab, Priority.ALWAYS);
            Label al = new Label(p.getAlamat());
            al.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: " + Theme.TEXT + ";");
            Label cl = new Label(p.getCatatan());
            cl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + Theme.TEXT_SUBTLE + ";");
            ab.getChildren().addAll(al, cl);
            Label tl = new Label(p.getTanggalJemput() != null ? p.getTanggalJemput().toString() : "-");
            tl.setPrefWidth(96);
            tl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
            Label statusBadge = Ui.statusBadge(p.getStatus());
            row.getChildren().addAll(idL, ab, tl, statusBadge);
            list.getChildren().add(row);
        }
        recentBox.getChildren().add(list);

        HBox twoCol = new HBox(16);
        HBox.setHgrow(chartBox, Priority.ALWAYS);
        HBox.setHgrow(recentBox, Priority.ALWAYS);
        chartBox.setPrefWidth(460);
        recentBox.setPrefWidth(460);
        twoCol.getChildren().addAll(chartBox, recentBox);
        contentArea.getChildren().add(twoCol);
    }

    /* ---------------- PENJEMPUTAN ---------------- */

    private void showPenjemputan() {
        currentView = "penjemputan";
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pageHeader("Manajemen Penjemputan", "Pilih penjemputan, tugaskan petugas, dan perbarui status."));

        TableView<Penjemputan> table = styledTable();
        table.setItems(penjemputanData);

        HBox actionBar = new HBox(10);
        actionBar.getChildren().add(Ui.button("Tambah Penjemputan", "btn-primary", e -> {
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
                p.setId((long) (penjemputanData.size() + 1));
                p.setAlamat(vals.get("alamat"));
                try { p.setTanggalJemput(LocalDate.parse(vals.get("tanggal"))); } catch (Exception ex) { p.setTanggalJemput(LocalDate.now()); }
                p.setWaktuJemput(vals.get("waktu"));
                String catatan = vals.get("kategori") + " - " + vals.get("estimasiBerat") + "kg | " + vals.get("catatan");
                p.setCatatan(catatan);
                p.setStatus("MENUNGGU");
                p.setQrCode("RESIK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                penjemputanData.add(p);
                showPenjemputan();
                CrudDialog.showInfo("Berhasil", "Penjemputan baru ditambahkan.\nKategori: " + vals.get("kategori") + "\nEstimasi: " + vals.get("estimasiBerat") + "kg");
            });
        }));
        actionBar.getChildren().add(Ui.button("Tugaskan Petugas", "btn-secondary", e -> {
            Penjemputan sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && ("MENUNGGU".equals(sel.getStatus()) || "DIJADWALKAN".equals(sel.getStatus()))) {
                sel.setStatus("DIJADWALKAN");
                table.refresh();
                CrudDialog.showInfo("Berhasil", "Petugas ditugaskan untuk #" + sel.getId());
            } else {
                CrudDialog.showInfo("Info", "Pilih penjemputan berstatus MENUNGGU/DIJADWALKAN");
            }
        }));
        actionBar.getChildren().add(Ui.button("Proses Penjemputan", "btn-secondary", e -> {
            Penjemputan sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && "DIJADWALKAN".equals(sel.getStatus())) {
                sel.setStatus("DIPROSES");
                table.refresh();
                CrudDialog.showInfo("Berhasil", "Penjemputan #" + sel.getId() + " sekarang DIPROSES");
            } else {
                CrudDialog.showInfo("Info", "Pilih penjemputan berstatus DIJADWALKAN untuk diproses");
            }
        }));
        actionBar.getChildren().add(Ui.button("Detail", "btn-secondary", e -> {
            Penjemputan sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                CrudDialog.showInfo("Info", "Pilih salah satu penjemputan terlebih dahulu");
                return;
            }
            showPenjemputanDetail(sel);
        }));
        contentArea.getChildren().add(actionBar);

        TextField searchField = Ui.textField("Cari alamat, status, atau catatan...");
        searchField.setPrefWidth(420);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                table.setItems(penjemputanData);
            } else {
                ObservableList<Penjemputan> filtered = FXCollections.observableArrayList();
                for (Penjemputan p : penjemputanData) {
                    if ((p.getAlamat() != null && p.getAlamat().toLowerCase().contains(newVal.toLowerCase())) ||
                        (p.getStatus() != null && p.getStatus().toLowerCase().contains(newVal.toLowerCase())) ||
                        (p.getCatatan() != null && p.getCatatan().toLowerCase().contains(newVal.toLowerCase()))) {
                        filtered.add(p);
                    }
                }
                table.setItems(filtered);
            }
        });
        contentArea.getChildren().add(searchField);

        int menunggu = 0, dijadwalkan = 0, diproses = 0, selesai = 0;
        for (Penjemputan p : penjemputanData) {
            if ("MENUNGGU".equals(p.getStatus())) menunggu++;
            else if ("DIJADWALKAN".equals(p.getStatus())) dijadwalkan++;
            else if ("DIPROSES".equals(p.getStatus())) diproses++;
            else if ("SELESAI".equals(p.getStatus())) selesai++;
        }
        HBox counts = new HBox(14);
        counts.setAlignment(Pos.CENTER_LEFT);
        counts.getChildren().add(countChip("#D97706", menunggu + " Menunggu"));
        counts.getChildren().add(countChip("#2563EB", dijadwalkan + " Dijadwalkan"));
        counts.getChildren().add(countChip("#7C3AED", diproses + " Diproses"));
        counts.getChildren().add(countChip("#059669", selesai + " Selesai"));
        contentArea.getChildren().add(counts);

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
        table.getColumns().addAll(c1, c2, c3, c4, c5, c6);

        contentArea.getChildren().add(table);
    }

    /** Opens a glass dialog with the full pickup detail, order list and a Google Maps action. */
    private void showPenjemputanDetail(Penjemputan p) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detail Penjemputan");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStyleClass().add("glass-dialog");

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));

        Label idLabel = new Label("#" + String.format("%03d", p.getId()) + " · " + p.getStatus());
        idLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: " + Theme.TEXT + ";");

        Label alamat = new Label(p.getAlamat());
        alamat.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.TEXT + ";");
        Label jadwal = new Label((p.getTanggalJemput() != null ? p.getTanggalJemput() : "-") + " · " + p.getWaktuJemput());
        jadwal.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        Label catatan = new Label(p.getCatatan());
        catatan.setWrapText(true);
        catatan.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");

        VBox mapCard = new VBox(8);
        mapCard.setPadding(new Insets(12));
        mapCard.setStyle("-fx-background-color: #F5F7F5; -fx-background-radius: 14; -fx-border-color: " + Theme.BORDER + "; -fx-border-radius: 14;");
        Label mapTitle = new Label("Lokasi penjemputan");
        mapTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT + ";");
        Label mapHint = new Label("Lihat titik alamat di Google Maps");
        mapHint.setStyle("-fx-font-size: 11px; -fx-text-fill: " + Theme.TEXT_SUBTLE + ";");
        Button mapsBtn = Ui.button("Buka di Google Maps", "btn-secondary", e -> {
            try {
                String query = java.net.URLEncoder.encode(p.getAlamat(), java.nio.charset.StandardCharsets.UTF_8);
                java.awt.Desktop.getDesktop().browse(new java.net.URI("https://www.google.com/maps/search/?api=1&query=" + query));
            } catch (Exception ex) {
                CrudDialog.showInfo("Info", "Tidak dapat membuka browser: " + ex.getMessage());
            }
        });
        mapCard.getChildren().addAll(mapTitle, mapHint, mapsBtn);

        VBox orderBox = new VBox(6);
        Label orderTitle = new Label("Orderan");
        orderTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT + ";");
        orderBox.getChildren().add(orderTitle);
        if (p.getDetailList() == null || p.getDetailList().isEmpty()) {
            Label empty = new Label("Belum ada rincian sampah.");
            empty.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.TEXT_SUBTLE + ";");
            orderBox.getChildren().add(empty);
        } else {
            List<Sampah> refs = DummyData.getSampahList();
            for (Penjemputan.DetailPenjemputan d : p.getDetailList()) {
                Sampah s = refs.get((int) ((d.getId() - 1) % refs.size()));
                Label row = new Label("• " + s.getNama() + " — estimasi " + d.getEstimasiBerat() + " kg"
                        + (d.getBeratAktual() != null ? ", aktual " + d.getBeratAktual() + " kg" : ""));
                row.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
                orderBox.getChildren().add(row);
            }
        }

        if (p.getTransaksi() != null) {
            Label trx = new Label("Pembayaran: Rp " + p.getTransaksi().getTotalHarga() + " · " + p.getTransaksi().getStatus());
            trx.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: " + Theme.ACCENT + ";");
            root.getChildren().add(trx);
        }

        root.getChildren().addAll(idLabel, alamat, jadwal, catatan, mapCard, orderBox);
        dialog.getDialogPane().setContent(root);

        ButtonType closeBtn = new ButtonType("Tutup", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeBtn);
        Button close = (Button) dialog.getDialogPane().lookupButton(closeBtn);
        if (close != null) close.getStyleClass().addAll("btn", "btn-secondary");
        dialog.showAndWait();
    }

    /* ---------------- JADWAL PENJEMPUTAN ---------------- */

    private void showJadwal() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pageHeader("Jadwal Penjemputan", "Penjemputan terurut berdasarkan tanggal dan waktu."));

        Map<LocalDate, List<Penjemputan>> grouped = new TreeMap<>();
        for (Penjemputan p : penjemputanData) {
            if (p.getTanggalJemput() == null) continue;
            grouped.computeIfAbsent(p.getTanggalJemput(), k -> new ArrayList<>()).add(p);
        }

        if (grouped.isEmpty()) {
            contentArea.getChildren().add(new EmptyState("Belum ada jadwal", "Tambahkan penjemputan terlebih dahulu."));
            return;
        }

        VBox list = new VBox(12);
        for (var entry : grouped.entrySet()) {
            VBox dayCard = Ui.card();
            dayCard.getChildren().add(Ui.cardHeader(entry.getKey().toString() + " · " + dayName(entry.getKey()),
                    entry.getValue().size() + " penjemputan"));
            for (Penjemputan p : entry.getValue()) {
                HBox row = Ui.rowCard(14);

                Label waktu = new Label(p.getWaktuJemput() != null ? p.getWaktuJemput() : "-");
                waktu.setPrefWidth(96);
                waktu.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_MUTED + ";");

                VBox main = new VBox(2);
                HBox.setHgrow(main, Priority.ALWAYS);
                Label alamat = new Label(p.getAlamat());
                alamat.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: " + Theme.TEXT + ";");
                Label catatan = new Label(p.getCatatan());
                catatan.setStyle("-fx-font-size: 11px; -fx-text-fill: " + Theme.TEXT_SUBTLE + ";");
                main.getChildren().addAll(alamat, catatan);

                Label petugas = new Label("DIJADWALKAN".equals(p.getStatus()) ? "Petugas: Andi" : "Petugas: belum ada");
                petugas.setPrefWidth(140);
                petugas.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");

                Label status = Ui.statusBadge(p.getStatus());
                row.getChildren().addAll(waktu, main, petugas, status);
                dayCard.getChildren().add(row);
            }
            list.getChildren().add(dayCard);
        }
        contentArea.getChildren().add(list);
    }

    private String dayName(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("id"));
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
            } catch (Exception ex) { }
        }
        return total;
    }

    /** Builds a crypto-style multi-line chart for setoran per category. */
    private LineChart<String, Number> createCategoryLineChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Setoran");
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(true);
        chart.setAnimated(true);
        chart.setCreateSymbols(true);
        chart.getStyleClass().add("glass-chart");

        String[] days;
        int[][] data;
        if ("Harian".equals(chartRange)) {
            days = new String[]{"Pagi", "Siang", "Sore", "Malam"};
            data = new int[][]{
                    {1, 2, 3, 2},
                    {2, 3, 4, 2},
                    {0, 1, 0, 1},
                    {0, 1, 1, 1}
            };
        } else if ("Bulanan".equals(chartRange)) {
            days = new String[]{"Minggu 1", "Minggu 2", "Minggu 3", "Minggu 4"};
            data = new int[][]{
                    {4, 6, 8, 10},
                    {6, 9, 11, 13},
                    {1, 2, 1, 2},
                    {2, 3, 3, 4}
            };
        } else {
            days = new String[]{"07", "08", "09", "10", "11", "12", "13"};
            data = new int[][]{
                    {3, 4, 2, 5, 6, 5, 8},
                    {5, 6, 7, 6, 9, 8, 11},
                    {0, 1, 2, 0, 1, 1, 2},
                    {1, 2, 1, 3, 2, 4, 3}
            };
        }
        String[] categories = {"ORGANIK", "ANORGANIK", "B3", "RESIDU"};
        for (int i = 0; i < categories.length; i++) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(categories[i]);
            for (int d = 0; d < days.length; d++) {
                XYChart.Data<String, Number> point = new XYChart.Data<>(days[d], data[i][d]);
                series.getData().add(point);
                final String name = categories[i];
                point.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        javafx.scene.control.Tooltip.install(newNode,
                                new javafx.scene.control.Tooltip(name + " · " + point.getXValue() + " — " + point.getYValue() + " setoran"));
                    }
                });
            }
            chart.getData().add(series);
        }
        return chart;
    }

    /* ---------------- DATA SAMPAH ---------------- */

    private void showSampah() {
        currentView = "sampah";
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pageHeader("Data Sampah", "Kelola jenis sampah dan harga per kilogram (VPS)."));

        List<Sampah> all = ApiService.getSampah();
        if (all.isEmpty()) all = DummyData.getSampahList();
        List<Sampah> filtered = "Semua".equals(sampahFilter) ? new ArrayList<>(all)
                : all.stream().filter(s -> sampahFilter.equals(s.getKategori())).toList();

        TableView<Sampah> table = styledTable();
        table.setItems(FXCollections.observableArrayList(filtered));

        HBox bar = new HBox(10);
        bar.getChildren().add(Ui.button("Tambah Sampah", "btn-primary", e -> {
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
        bar.getChildren().add(Ui.button("Edit", "btn-secondary", e -> {
            Sampah sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                CrudDialog.showInfo("Info", "Pilih data sampah terlebih dahulu");
                return;
            }
            editSampah(sel);
        }));
        bar.getChildren().add(Ui.button("Hapus", "btn-danger-ghost", e -> {
            Sampah sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                CrudDialog.showInfo("Info", "Pilih data sampah terlebih dahulu");
                return;
            }
            if (!CrudDialog.showDeleteConfirm(sel.getNama())) return;
            try {
                ApiService.deleteSampah(sel.getId());
                showSampah();
                CrudDialog.showInfo("Berhasil", "Sampah dihapus dari VPS.");
            } catch (Exception ex) {
                CrudDialog.showInfo("Error", "Gagal hapus: " + ex.getMessage());
            }
        }));
        // No manual Refresh button: this table now updates itself whenever
        // the server broadcasts SAMPAH_CREATED / HARGA_SAMPAH_UPDATED /
        // SAMPAH_DELETED over /ws/events (see onRealtimeEvent).
        bar.getChildren().add(Ui.muted("Live: harga berubah otomatis saat ada update."));
        contentArea.getChildren().add(bar);

        HBox filterBar = new HBox(8);
        String[] categories = {"Semua", "ORGANIK", "ANORGANIK", "B3", "RESIDU"};
        for (String category : categories) {
            String variant = category.equals(sampahFilter) ? "btn-primary" : "btn-secondary";
            filterBar.getChildren().add(Ui.button(category, variant, e -> {
                sampahFilter = category;
                showSampah();
            }));
        }
        contentArea.getChildren().add(filterBar);

        TableColumn<Sampah, String> c1 = new TableColumn<>("Nama");
        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNama()));
        TableColumn<Sampah, String> c2 = new TableColumn<>("Kategori");
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getKategori()));
        TableColumn<Sampah, String> c3 = new TableColumn<>("Harga/kg");
        c3.setCellValueFactory(d -> new SimpleStringProperty("Rp " + d.getValue().getHargaPerKg()));
        TableColumn<Sampah, String> c4 = new TableColumn<>("Deskripsi");
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDeskripsi()));
        table.getColumns().addAll(c1, c2, c3, c4);

        table.setRowFactory(tv -> {
            TableRow<Sampah> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !row.isEmpty()) {
                    editSampah(row.getItem());
                }
            });
            return row;
        });

        contentArea.getChildren().add(table);
    }

    private void editSampah(Sampah sel) {
        Map<String, String> init = new HashMap<>();
        init.put("nama", sel.getNama());
        init.put("kategori", sel.getKategori());
        init.put("harga", sel.getHargaPerKg().toString());
        init.put("deskripsi", sel.getDeskripsi());
        CrudDialog.showForm("Edit Sampah", "Update",
                List.of(
                        new CrudDialog.FormField("nama", "Nama", ""),
                        new CrudDialog.FormField("kategori", "Kategori", ""),
                        new CrudDialog.FormField("harga", "Harga/kg", ""),
                        new CrudDialog.FormField("deskripsi", "Deskripsi", "")
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

    /* ---------------- EDUKASI SAMPAH ---------------- */

    private void showEdukasi() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pageHeader("Edukasi Sampah", "Artikel dan panduan pemilahan sampah."));

        if (articles.isEmpty()) {
            articles.add(new EdukasiArticle("Cara Memilah Sampah Organik", "ORGANIK", "https://placehold.co/600x300/1A5D3A/FFF?text=Organik",
                    "Pisahkan sisa makanan, daun, dan kulit buah dari sampah lainnya. Simpan di wadah tertutup agar tidak berbau."));
            articles.add(new EdukasiArticle("Botol Plastik Jadi Cuan", "ANORGANIK", "https://placehold.co/600x300/2563EB/FFF?text=Anorganik",
                    "Cuci dan keringkan botol plastik sebelum disetor. Harga lebih tinggi untuk plastik bersih dan terpilah."));
            articles.add(new EdukasiArticle("Cara Aman Membuang Baterai Bekas", "B3", "https://placehold.co/600x300/DC2626/FFF?text=B3",
                    "Baterai bekas jangan dibuang ke saluran air. Kumpulkan dalam wadah kering dan serahkan ke bank sampah."));
        }

        HBox bar = new HBox(10);
        bar.getChildren().add(Ui.button("Upload Artikel", "btn-primary", e -> showArticleForm(null)));
        contentArea.getChildren().add(bar);

        HBox filterBar = new HBox(8);
        String[] filters = {"Semua", "ORGANIK", "ANORGANIK", "B3", "RESIDU"};
        for (String f : filters) {
            String variant = f.equals(eduFilter) ? "btn-primary" : "btn-secondary";
            filterBar.getChildren().add(Ui.button(f, variant, e -> {
                eduFilter = f;
                showEdukasi();
            }));
        }
        contentArea.getChildren().add(filterBar);

        VBox list = new VBox(12);
        for (EdukasiArticle a : articles) {
            if (!"Semua".equals(eduFilter) && !eduFilter.equals(a.category)) continue;
            VBox card = Ui.card();

            HBox head = new HBox(10);
            head.setAlignment(Pos.CENTER_LEFT);
            Label title = new Label(a.title);
            title.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT + ";");
            Label badge = Ui.badge(a.category, new Theme.StatusStyle(Theme.ACCENT, Theme.ACCENT_SOFT, Theme.ACCENT_BORDER));
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            head.getChildren().addAll(title, spacer, badge);

            Label image = new Label(a.image);
            image.setStyle("-fx-font-size: 11px; -fx-text-fill: " + Theme.TEXT_SUBTLE + ";");
            Label excerpt = new Label(a.content);
            excerpt.setWrapText(true);
            excerpt.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");

            HBox actions = new HBox(8);
            actions.getChildren().add(Ui.button("Edit", "btn-secondary", ev -> showArticleForm(a)));
            actions.getChildren().add(Ui.button("Hapus", "btn-danger-ghost", ev -> {
                articles.remove(a);
                showEdukasi();
            }));

            card.getChildren().addAll(head, image, excerpt, actions);
            list.getChildren().add(card);
        }
        if (articles.isEmpty()) {
            list.getChildren().add(new EmptyState("Belum ada artikel", "Upload artikel pertama untuk mulai mengedukasi masyarakat."));
        }
        contentArea.getChildren().add(list);
    }

    private void showArticleForm(EdukasiArticle existing) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Upload Artikel" : "Edit Artikel");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStyleClass().add("glass-dialog");

        VBox form = new VBox(8);
        form.setPadding(new Insets(20));

        TextField titleField = Ui.textField("Judul artikel");
        TextField categoryField = Ui.textField("Kategori (ORGANIK/ANORGANIK/B3/RESIDU)");
        TextField imageField = Ui.textField("URL gambar sampul");
        TextArea contentAreaField = new TextArea();
        contentAreaField.setPromptText("Isi artikel");
        contentAreaField.setPrefRowCount(5);
        contentAreaField.getStyleClass().add("input-area");

        if (existing != null) {
            titleField.setText(existing.title);
            categoryField.setText(existing.category);
            imageField.setText(existing.image);
            contentAreaField.setText(existing.content);
        }

        form.getChildren().addAll(
                new Label("Judul"), titleField,
                new Label("Kategori"), categoryField,
                new Label("Gambar"), imageField,
                new Label("Isi"), contentAreaField);

        dialog.getDialogPane().setContent(form);

        ButtonType saveBtn = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, cancelBtn);
        Button save = (Button) dialog.getDialogPane().lookupButton(saveBtn);
        Button cancel = (Button) dialog.getDialogPane().lookupButton(cancelBtn);
        if (save != null) save.getStyleClass().addAll("btn", "btn-primary");
        if (cancel != null) cancel.getStyleClass().addAll("btn", "btn-ghost");

        dialog.setResultConverter(btn -> btn == saveBtn ? (Void) null : null);
        dialog.showAndWait().ifPresent(ok -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) return;
            String category = categoryField.getText().trim().toUpperCase();
            if (category.isEmpty()) category = "UMUM";
            if (existing == null) {
                articles.add(new EdukasiArticle(title, category, imageField.getText(), contentAreaField.getText()));
            } else {
                articles.set(articles.indexOf(existing), new EdukasiArticle(title, category, imageField.getText(), contentAreaField.getText()));
            }
            showEdukasi();
        });
    }

    /* ---------------- DOMPET DIGITAL ---------------- */

    private void showDompet() {
        currentView = "dompet";
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pageHeader("Dompet Digital", "Saldo, poin, dan transaksi per tipe akun."));

        ComboBox<String> mode = Ui.comboBox();
        mode.setItems(FXCollections.observableArrayList("Masyarakat", "Bank Sampah", "Admin"));
        mode.setValue(dompetMode);
        mode.setPrefWidth(220);
        mode.setOnAction(e -> {
            dompetMode = mode.getValue();
            showDompet();
        });
        contentArea.getChildren().add(mode);

        VBox list = new VBox(8);

        if ("Masyarakat".equals(dompetMode)) {
            List<Map<String, Object>> dompetList = ApiService.getAllDompet();
            if (dompetList.isEmpty()) dompetList = DummyData.getDompetList();
            for (Map<String, Object> d : dompetList) {
                HBox row = Ui.rowCard(20);
                Label nm = new Label(String.valueOf(d.get("nama")));
                nm.setPrefWidth(260);
                nm.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + Theme.TEXT + ";");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Label sd = new Label(String.valueOf(d.get("saldo")));
                sd.setPrefWidth(120);
                sd.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: " + Theme.ACCENT + ";");
                Label pi = new Label(String.valueOf(d.get("poin")));
                pi.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: " + Theme.WARNING + ";");
                row.getChildren().addAll(nm, spacer, sd, pi);
                list.getChildren().add(row);
            }
        } else if ("Bank Sampah".equals(dompetMode)) {
            String[][] banks = {
                    {"Bank Sampah Resik Pusat", "BCA 8820 1234 5678", "Rp 4.250.000"},
                    {"Bank Sampah Resik Selatan", "Mandiri 1300 9876 5432", "Rp 2.180.000"},
                    {"Bank Sampah Resik Timur", "BRI 0021 4567 8901", "Rp 1.930.000"}
            };
            for (String[] b : banks) {
                HBox row = Ui.rowCard(20);
                Label nm = new Label(b[0]);
                nm.setPrefWidth(240);
                nm.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + Theme.TEXT + ";");
                Label rek = new Label(b[1]);
                rek.setPrefWidth(220);
                rek.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Label sd = new Label(b[2]);
                sd.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: " + Theme.ACCENT + ";");
                row.getChildren().addAll(nm, rek, spacer, sd);
                list.getChildren().add(row);
            }
        } else {
            HBox row = Ui.rowCard(20);
            Label nm = new Label("Admin Resik");
            nm.setPrefWidth(240);
            nm.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + Theme.TEXT + ";");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label sd = new Label("Payroll: Rp 8.360.000");
            sd.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: " + Theme.ACCENT + ";");
            row.getChildren().addAll(nm, spacer, sd);
            list.getChildren().add(row);

            Label trxTitle = Ui.heading("Transaksi terakhir");
            list.getChildren().add(trxTitle);
            String[][] trx = {
                    {"Setoran masyarakat", "2026-10-20", "Rp 15.500"},
                    {"Pencairan Bank Sampah Pusat", "2026-10-19", "-Rp 1.000.000"},
                    {"Setoran masyarakat", "2026-10-18", "Rp 22.500"}
            };
            for (String[] t : trx) {
                HBox r = Ui.rowCard(16);
                Label n = new Label(t[0]);
                HBox.setHgrow(n, Priority.ALWAYS);
                n.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.TEXT + ";");
                Label d = new Label(t[1]);
                d.setPrefWidth(110);
                d.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.TEXT_SUBTLE + ";");
                Label v = new Label(t[2]);
                v.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT + ";");
                r.getChildren().addAll(n, d, v);
                list.getChildren().add(r);
            }
        }

        contentArea.getChildren().add(list);
    }

    /* ---------------- LEADERBOARD ---------------- */

    private void showLeaderboard() {
        currentView = "leaderboard";
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pageHeader("Leaderboard", "Ranking kontribusi masyarakat (VPS)."));

        HBox periodBar = new HBox(8);
        String[] periods = {"Harian", "Mingguan", "Bulanan", "Tahunan"};
        for (String period : periods) {
            String variant = leaderboardPeriod.equals(period) ? "btn-primary" : "btn-secondary";
            periodBar.getChildren().add(Ui.button(period, variant, e -> {
                leaderboardPeriod = period;
                showLeaderboard();
            }));
        }
        contentArea.getChildren().add(periodBar);

        int factor = switch (leaderboardPeriod) {
            case "Harian" -> 1;
            case "Mingguan" -> 7;
            case "Tahunan" -> 365;
            default -> 30;
        };

        VBox list = new VBox(8);
        List<Map<String, Object>> lbData = ApiService.getLeaderboard();
        if (lbData.isEmpty()) {
            String[] names = {"Syukron Maulana", "Zain Ghathfaan", "Riri Syafala", "Ahmad Fauzi", "Siti Nurhaliza"};
            int[] poins = {320, 280, 250, 180, 120};
            for (int i = 0; i < names.length; i++) {
                HBox row = Ui.rowCard(16);
                Label rank = new Label(String.valueOf(i + 1));
                rank.setPrefWidth(30);
                rank.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: "
                        + (i == 0 ? "#D97706" : i == 1 ? "#8A928E" : i == 2 ? "#B45309" : "#8A928E") + ";");
                Label nm = new Label(names[i]);
                nm.setPrefWidth(250);
                nm.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + Theme.TEXT + ";");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Label pi = new Label((poins[i] * factor / 30) + " poin");
                pi.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: " + Theme.ACCENT + ";");
                row.getChildren().addAll(rank, nm, spacer, pi);
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

                HBox row = Ui.rowCard(16);
                Label rankL = new Label(String.valueOf(rank));
                rankL.setPrefWidth(30);
                rankL.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: "
                        + (rank == 1 ? "#F39C12" : rank == 2 ? "#94A3B8" : rank == 3 ? "#CD7F32" : "#94A3B8") + ";");
                Label nm = new Label(nama);
                nm.setPrefWidth(250);
                nm.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + Theme.TEXT + ";");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Label pi = new Label(poinStr + " | " + saldoStr);
                pi.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: " + Theme.ACCENT + ";");
                row.getChildren().addAll(rankL, nm, spacer, pi);
                list.getChildren().add(row);
            }
        }
        contentArea.getChildren().add(list);
    }

    /* ---------------- LAPORAN HARIAN ---------------- */

    private void showLaporanHarian() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pageHeader("Laporan Harian", "Statistik setoran dan pembayaran per hari."));

        ComboBox<String> bankCombo = Ui.comboBox();
        bankCombo.setItems(FXCollections.observableArrayList(
                "Semua Bank Sampah", "Resik Pusat", "Resik Selatan", "Resik Timur", "Resik Barat"));
        bankCombo.setValue(laporanBank);
        bankCombo.setPrefWidth(240);
        bankCombo.setOnAction(e -> {
            laporanBank = bankCombo.getValue();
            showLaporanHarian();
        });
        contentArea.getChildren().add(bankCombo);

        DatePicker datePicker = Ui.datePicker();
        datePicker.setValue(LocalDate.now());
        Label dateLabel = new Label("Pilih tanggal:");
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");

        HBox dateBox = new HBox(10);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        dateBox.getChildren().addAll(dateLabel, datePicker);
        dateBox.getChildren().add(Ui.button("Export CSV", "btn-primary", e -> {
            LocalDate selected = datePicker.getValue();
            java.io.File file = new java.io.File(System.getProperty("user.home"), "Desktop/Laporan-Resik-" + selected + ".csv");
            try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
                pw.println("Tanggal,ID,Alamat,Status,Total Pembayaran");
                int totalSetoran = 0;
                BigDecimal totalPembayaran = BigDecimal.ZERO;
                double totalKg = 0;
                for (Penjemputan p : penjemputanData) {
                    if (p.getTanggalJemput() != null && p.getTanggalJemput().equals(selected) && "SELESAI".equals(p.getStatus()) && matchesBank(p)) {
                        totalSetoran++;
                        BigDecimal total = p.getTransaksi() != null ? p.getTransaksi().getTotalHarga() : BigDecimal.ZERO;
                        totalPembayaran = totalPembayaran.add(total);
                        if (p.getDetailList() != null) for (var d : p.getDetailList()) if (d.getBeratAktual() != null) totalKg += d.getBeratAktual().doubleValue();
                        pw.println(selected + "," + String.format("#%03d", p.getId()) + ",\"" + p.getAlamat() + "\"," + p.getStatus() + ",Rp " + total);
                    }
                }
                pw.println();
                pw.println("Total Setoran," + totalSetoran);
                pw.println("Total Pembayaran,Rp " + totalPembayaran);
                pw.println("Total Sampah (kg)," + String.format("%.1f", totalKg));
                pw.println("Estimasi CO2 Reduction (kg)," + String.format("%.1f", totalKg * 2));
                CrudDialog.showInfo("Berhasil", "Laporan CSV di-export ke:\n" + file.getAbsolutePath());
            } catch (Exception ex) {
                CrudDialog.showInfo("Error", "Gagal export: " + ex.getMessage());
            }
        }));
        dateBox.getChildren().add(Ui.button("Export PDF", "btn-secondary", e -> exportLaporanPdf(datePicker.getValue())));
        contentArea.getChildren().add(dateBox);

        VBox statsCard = Ui.card();

        Runnable updateStats = () -> {
            LocalDate selected = datePicker.getValue();
            statsCard.getChildren().clear();
            int totalSetoran = 0;
            BigDecimal totalPembayaran = BigDecimal.ZERO;
            double totalKg = 0;
            for (Penjemputan p : penjemputanData) {
                if (p.getTanggalJemput() != null && p.getTanggalJemput().equals(selected) && "SELESAI".equals(p.getStatus()) && matchesBank(p)) {
                    totalSetoran++;
                    if (p.getTransaksi() != null) totalPembayaran = totalPembayaran.add(p.getTransaksi().getTotalHarga());
                    if (p.getDetailList() != null) for (var d : p.getDetailList()) if (d.getBeratAktual() != null) totalKg += d.getBeratAktual().doubleValue();
                }
            }
            Label t1 = new Label("Setoran Selesai: " + totalSetoran);
            t1.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + Theme.TEXT + ";");
            Label t2 = new Label("Total Pembayaran: Rp " + totalPembayaran);
            t2.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + Theme.ACCENT + ";");
            Label t3 = new Label(String.format("Total Sampah: %.1f kg", totalKg));
            t3.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + Theme.WARNING + ";");
            Label t4 = new Label(String.format("Estimasi CO2 Reduction: %.1f kg", totalKg * 2));
            t4.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: " + Theme.SUCCESS + ";");
            statsCard.getChildren().addAll(t1, t2, t3, t4);
        };
        datePicker.valueProperty().addListener((obs, o, n) -> updateStats.run());
        updateStats.run();
        contentArea.getChildren().add(statsCard);

        TableView<Penjemputan> table = styledTable();
        TableColumn<Penjemputan, String> tc1 = new TableColumn<>("ID");
        tc1.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getId()));
        TableColumn<Penjemputan, String> tc2 = new TableColumn<>("Alamat");
        tc2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAlamat()));
        TableColumn<Penjemputan, String> tc3 = new TableColumn<>("Status");
        tc3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        TableColumn<Penjemputan, String> tc4 = new TableColumn<>("Total");
        tc4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTransaksi() != null ? "Rp " + d.getValue().getTransaksi().getTotalHarga() : "-"));
        table.getColumns().addAll(tc1, tc2, tc3, tc4);

        VBox chartBox = Ui.card();

        Runnable updateTable = () -> {
            LocalDate selected = datePicker.getValue();
            List<Penjemputan> dayItems = penjemputanData.stream()
                    .filter(p -> p.getTanggalJemput() != null && p.getTanggalJemput().equals(selected) && matchesBank(p))
                    .toList();
            table.setItems(FXCollections.observableArrayList(dayItems));
            updateCharts(dayItems, chartBox);
        };
        datePicker.valueProperty().addListener((obs, o, n) -> updateTable.run());
        updateTable.run();

        HBox reportCols = new HBox(16);
        HBox.setHgrow(table, Priority.ALWAYS);
        HBox.setHgrow(chartBox, Priority.ALWAYS);
        table.setPrefWidth(480);
        chartBox.setPrefWidth(400);
        reportCols.getChildren().addAll(table, chartBox);
        contentArea.getChildren().add(reportCols);
    }

    private HBox countChip(String color, String text) {
        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 9px;");
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        HBox chip = new HBox(6, dot, label);
        chip.setAlignment(Pos.CENTER_LEFT);
        return chip;
    }

    private String namaMasyarakat(Penjemputan p) {
        Map<Long, String> names = Map.of(
                1L, "Budi Santoso",
                2L, "Siti Nurhaliza",
                3L, "Ahmad Fauzi",
                4L, "Rina Marlina",
                5L, "Dewi Anggraini",
                6L, "Andi Saputra",
                7L, "Maya Puspita",
                8L, "Rizky Ramadhan");
        return names.getOrDefault(p.getId(), "Masyarakat #" + p.getId());
    }

    private boolean matchesBank(Penjemputan p) {
        if ("Semua Bank Sampah".equals(laporanBank)) return true;
        String[] banks = {"Resik Pusat", "Resik Selatan", "Resik Timur", "Resik Barat"};
        int idx = (int) ((p.getId() - 1) % banks.length);
        return laporanBank.equals(banks[idx]);
    }

    private void updateCharts(List<Penjemputan> dayItems, VBox chartBox) {
        chartBox.getChildren().clear();

        Label chartTitle = new Label("Grafik Setoran per Kategori");
        chartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: " + Theme.TEXT + ";");
        chartBox.getChildren().add(chartTitle);

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

        if (dayItems.isEmpty()) {
            Label empty = new Label("Belum ada data setoran untuk tanggal ini.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.TEXT_SUBTLE + ";");
            chartBox.getChildren().add(empty);
            return;
        }

        LineChart<String, Number> trendChart = createCategoryLineChart();
        trendChart.setPrefHeight(210);
        chartBox.getChildren().add(trendChart);

        Separator sep = new Separator();
        sep.setPadding(new Insets(8, 0, 8, 0));
        chartBox.getChildren().add(sep);

        Label pieTitle = new Label("Distribusi Status Penjemputan");
        pieTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: " + Theme.TEXT + ";");
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
            Theme.StatusStyle style = Theme.statusOf(status);
            VBox statusBox = new VBox(4);
            statusBox.setStyle("-fx-background-color: " + style.background() + ";"
                    + " -fx-background-radius: 12;"
                    + " -fx-border-color: " + style.border() + ";"
                    + " -fx-border-width: 1;"
                    + " -fx-border-radius: 12;"
                    + " -fx-padding: 12 16;");
            Label stLabel = new Label(status);
            stLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 800; -fx-text-fill: " + style.foreground() + ";");
            Label stCount = new Label(count + " setoran");
            stCount.setStyle("-fx-font-size: 10px; -fx-text-fill: " + style.foreground() + ";");
            statusBox.getChildren().addAll(stLabel, stCount);
            pieRow.getChildren().add(statusBox);
        }
        chartBox.getChildren().add(pieRow);
    }

    /* ---------------- RIWAYAT SETORAN ---------------- */

    private void showRiwayatSetoran() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pageHeader("Riwayat Setoran & Pembayaran", "Semua transaksi yang telah selesai."));

        TableView<Penjemputan> table = styledTable();
        List<Penjemputan> selesai = penjemputanData.stream().filter(p -> "SELESAI".equals(p.getStatus())).toList();
        table.setItems(FXCollections.observableArrayList(selesai));

        TableColumn<Penjemputan, String> c1 = new TableColumn<>("ID");
        c1.setCellValueFactory(d -> new SimpleStringProperty("#" + d.getValue().getId()));
        TableColumn<Penjemputan, String> cNama = new TableColumn<>("Nama Masyarakat");
        cNama.setCellValueFactory(d -> new SimpleStringProperty(namaMasyarakat(d.getValue())));
        TableColumn<Penjemputan, String> c2 = new TableColumn<>("Alamat");
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAlamat()));
        TableColumn<Penjemputan, String> c3 = new TableColumn<>("Tanggal");
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTanggalJemput() != null ? d.getValue().getTanggalJemput().toString() : "-"));
        TableColumn<Penjemputan, String> c4 = new TableColumn<>("Total Bayar");
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTransaksi() != null ? "Rp " + d.getValue().getTransaksi().getTotalHarga() : "-"));
        TableColumn<Penjemputan, String> c5 = new TableColumn<>("Status");
        c5.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTransaksi() != null ? d.getValue().getTransaksi().getStatus() : "-"));
        table.getColumns().addAll(c1, cNama, c2, c3, c4, c5);

        BigDecimal grandTotal = BigDecimal.ZERO;
        for (Penjemputan p : selesai) {
            if (p.getTransaksi() != null) grandTotal = grandTotal.add(p.getTransaksi().getTotalHarga());
        }
        Label summary = new Label("Total semua pembayaran: Rp " + grandTotal + " | Total setoran: " + selesai.size());
        summary.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: " + Theme.ACCENT + ";");
        summary.setPadding(new Insets(12, 0, 0, 0));

        contentArea.getChildren().addAll(table, summary);
    }

    /* ---------------- MANAJEMEN USER ---------------- */

    private void showUsers() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pageHeader("Manajemen User", "Kelola akun masyarakat, petugas, dan admin (VPS)."));

        HBox bar = new HBox(10);
        bar.getChildren().add(Ui.button("Tambah User", "btn-primary", e -> {
            CrudDialog.showForm("Tambah User", "Simpan",
                    List.of(
                            new CrudDialog.FormField("nama", "Nama Lengkap", "Nama sesuai KTP"),
                            new CrudDialog.FormField("email", "Email", "nama@mail.com"),
                            new CrudDialog.FormField("role", "Role", "MASYARAKAT/PETUGAS/ADMIN"),
                            new CrudDialog.FormField("telp", "No. Telepon", "08xx-xxxx-xxxx"),
                            new CrudDialog.FormField("alamat", "Alamat", "Alamat lengkap"),
                            new CrudDialog.FormField("ktp", "Nomor KTP", "16 digit NIK"),
                            new CrudDialog.FormField("foto", "Foto Bank Sampah", "Pilih file foto...")
                    ), null
            ).ifPresent(vals -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", System.currentTimeMillis() % 1000);
                m.put("nama", vals.get("nama"));
                m.put("email", vals.get("email"));
                m.put("role", vals.get("role").toUpperCase());
                m.put("telp", vals.get("telp"));
                m.put("alamat", vals.get("alamat"));
                m.put("ktp", vals.get("ktp"));
                m.put("foto", vals.get("foto"));
                m.put("saldo", "0");
                m.put("poin", "0");
                DummyData.getUserList().add(m);
                showUsers();
                CrudDialog.showInfo("Berhasil", "User baru ditambahkan.");
            });
        }));
        bar.getChildren().add(Ui.button("Upload Foto", "btn-secondary", e -> pilihFileGambar("Upload Foto KTP atau Bank Sampah")));
        bar.getChildren().add(Ui.button("Refresh", "btn-secondary", e -> showUsers()));
        contentArea.getChildren().addAll(bar, new Separator());

        List<Map<String, Object>> vpsUsers = ApiService.getLeaderboard();
        List<Map<String, Object>> userList = new ArrayList<>();
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

        TableView<Map<String, Object>> table = styledTable();
        table.setItems(FXCollections.observableArrayList(userList));
        TableColumn<Map<String, Object>, String> c1 = new TableColumn<>("ID");
        c1.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("id"))));
        TableColumn<Map<String, Object>, String> c2 = new TableColumn<>("Nama");
        c2.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("nama"))));
        TableColumn<Map<String, Object>, String> c3 = new TableColumn<>("Role");
        c3.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("role"))));
        TableColumn<Map<String, Object>, String> c4 = new TableColumn<>("Saldo");
        c4.setCellValueFactory(d -> {
            Object s = d.getValue().get("saldo");
            return new SimpleStringProperty(s instanceof Number ? "Rp " + ((Number) s).doubleValue() : "-");
        });
        TableColumn<Map<String, Object>, String> c5 = new TableColumn<>("Poin");
        c5.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("poin"))));
        table.getColumns().addAll(c1, c2, c3, c4, c5);
        contentArea.getChildren().add(table);
    }

    /* ---------------- CHAT CLIENT ---------------- */

    /** Opens the chat as a two-pane popup in the bottom-left corner. */
    private void showChatPopup() {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Pesan");

        HBox chatRoot = new HBox();
        chatRoot.setMinSize(780, 500);
        chatRoot.setPrefSize(780, 500);
        chatRoot.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16;");

        VBox leftPanel = new VBox();
        leftPanel.setPrefWidth(310);
        leftPanel.setMinWidth(310);
        leftPanel.setStyle("-fx-background-color: white; -fx-background-radius: 16 0 0 16; -fx-border-color: #E5E7EB; -fx-border-width: 0 1 0 0;");

        HBox leftHeader = new HBox(10);
        leftHeader.setPadding(new Insets(14, 16, 12, 16));
        leftHeader.setStyle("-fx-background-color: " + Theme.ACCENT + "; -fx-background-radius: 16 0 0 0;");
        leftHeader.setAlignment(Pos.CENTER_LEFT);
        Label statusDot = new Label("●");
        statusDot.setStyle("-fx-text-fill: #FBBF24; -fx-font-size: 12px;");
        Label headerTitle = new Label("Pesan");
        headerTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: white;");
        Region hs = new Region();
        HBox.setHgrow(hs, Priority.ALWAYS);
        leftHeader.getChildren().addAll(statusDot, headerTitle, hs);

        popupClientList = new ListView<>();
        popupClientList.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(popupClientList, Priority.ALWAYS);
        popupClientList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                String[] parts = item.split("\\|", -1);
                String uid = parts.length > 0 ? parts[0] : "?";
                String nama = parts.length > 1 ? parts[1] : "User #" + uid;
                String preview = parts.length > 2 ? parts[2] : "";
                String time = parts.length > 3 ? parts[3] : "";
                boolean unread = parts.length > 4 && "1".equals(parts[4]);

                HBox row = new HBox(10);
                row.setPadding(new Insets(10, 14, 10, 14));
                row.setAlignment(Pos.CENTER_LEFT);

                Circle avatar = new Circle(22);
                avatar.setFill(Color.web(Theme.ACCENT_LIGHT));
                Label initial = new Label(nama.substring(0, 1).toUpperCase());
                initial.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: white;");
                StackPane avatarPane = new StackPane(avatar, initial);

                VBox textCol = new VBox(3);
                HBox.setHgrow(textCol, Priority.ALWAYS);
                Label nameLbl = new Label(nama);
                nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT + ";");
                Label previewLbl = new Label(preview);
                previewLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
                textCol.getChildren().addAll(nameLbl, previewLbl);

                VBox rightCol = new VBox(5);
                rightCol.setAlignment(Pos.CENTER_RIGHT);
                Label timeLbl = new Label(time);
                timeLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + Theme.TEXT_SUBTLE + ";");
                if (unread) {
                    Circle badge = new Circle(9);
                    badge.setFill(Color.web("#22C55E"));
                    Label badgeLbl = new Label("•");
                    badgeLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: white;");
                    StackPane badgePane = new StackPane(badge, badgeLbl);
                    rightCol.getChildren().addAll(timeLbl, badgePane);
                } else {
                    rightCol.getChildren().add(timeLbl);
                }

                row.getChildren().addAll(avatarPane, textCol, rightCol);
                setGraphic(row);
                setText(null);
                setStyle("-fx-background-color: " + (isSelected() ? "#F0F9F4" : "white") + "; -fx-border-color: #F1F5F9; -fx-border-width: 0 0 1px 0;");
            }
        });
        leftPanel.getChildren().addAll(leftHeader, popupClientList);

        VBox rightPanel = new VBox();
        rightPanel.setStyle("-fx-background-color: #F5F7F5; -fx-background-radius: 0 16 16 0;");
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        HBox chatHeader = new HBox(10);
        chatHeader.setPadding(new Insets(12, 18, 12, 18));
        chatHeader.setStyle("-fx-background-color: " + Theme.ACCENT + "; -fx-background-radius: 0 16 0 0;");
        chatHeader.setAlignment(Pos.CENTER_LEFT);

        Circle chatAvatar = new Circle(18);
        chatAvatar.setFill(Color.web(Theme.ACCENT_LIGHT));
        Label chatAvatarInit = new Label("?");
        chatAvatarInit.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: white;");
        StackPane chatAvatarPane = new StackPane(chatAvatar, chatAvatarInit);

        VBox chatTitleBox = new VBox(2);
        Label chatName = new Label("Pilih percakapan");
        chatName.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: white;");
        Label chatStatus = new Label("");
        chatStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #E2E8F0;");
        chatTitleBox.getChildren().addAll(chatName, chatStatus);

        Region hs2 = new Region();
        HBox.setHgrow(hs2, Priority.ALWAYS);
        chatHeader.getChildren().addAll(chatAvatarPane, chatTitleBox, hs2);

        popupChatList = new ListView<>();
        popupChatList.setStyle("-fx-background-color: #F5F7F5; -fx-border-color: transparent;");
        VBox.setVgrow(popupChatList, Priority.ALWAYS);
        popupChatList.setCellFactory(lv -> new ListCell<HBox>() {
            @Override
            protected void updateItem(HBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: #F5F7F5; -fx-padding: 2 8;");
                } else {
                    setGraphic(item);
                    setStyle("-fx-background-color: #F5F7F5; -fx-padding: 2 8;");
                }
            }
        });

        HBox inputRow = new HBox(8);
        inputRow.setPadding(new Insets(10, 16, 10, 16));
        inputRow.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 1 0 0 0;");
        inputRow.setAlignment(Pos.CENTER);

        popupMsgField = Ui.textField("Ketik pesan...");
        popupMsgField.setDisable(true);
        HBox.setHgrow(popupMsgField, Priority.ALWAYS);
        popupMsgField.setOnAction(e -> doAdminSend(popupMsgField));
        Button sendBtn = Ui.button("Kirim", "btn-primary", e -> doAdminSend(popupMsgField));
        inputRow.getChildren().addAll(popupMsgField, sendBtn);

        rightPanel.getChildren().addAll(chatHeader, popupChatList, inputRow);
        chatRoot.getChildren().addAll(leftPanel, rightPanel);

        Scene scene = new Scene(chatRoot);
        stage.setScene(scene);
        javafx.geometry.Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMaxX() - 780 - 24);
        stage.setY(bounds.getMaxY() - 572);
        stage.show();

        popupClientList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String[] parts = newVal.split("\\|", -1);
            String uid = parts[0];
            String nama = clientNames.getOrDefault(uid, "User #" + uid);
            currentChatUserId = uid;
            popupMsgField.setDisable(false);
            popupMsgField.setPromptText("Ketik balasan...");
            chatName.setText(nama);
            chatStatus.setText("Online");
            chatAvatarInit.setText(nama.substring(0, 1).toUpperCase());
            clientHasUnread.put(uid, false);
            new Thread(() -> {
                loadChatMessagesAsync(uid);
                markMessagesRead(uid);
            }).start();
        });

        new Thread(() -> {
            loadClientListAsync(popupClientList);
            javafx.application.Platform.runLater(() -> {
                if (popupClientList.getItems() != null && !popupClientList.getItems().isEmpty()) {
                    popupClientList.getSelectionModel().select(0);
                }
            });
        }).start();

        connectChatWebSocket(statusDot, chatStatus);
    }

    private javafx.animation.Timeline chatTimeline = null;
    private com.resik.admin.service.ChatWebSocketClient chatWsClient = null;
    private com.resik.admin.service.RealtimeEventClient eventWsClient = null;
    /** Id of the view currently shown (dashboard, penjemputan, sampah, dompet,
     *  leaderboard, verifikasi, stok, ...). Event-driven refreshes only
     *  re-render the view that is actually open. */
    private String currentView = "dashboard";
    private String currentChatUserId = null;
    private ListView<String> popupClientList;
    private ListView<HBox> popupChatList;
    private TextField popupMsgField;
    private final AtomicBoolean chatPollingInProgress = new AtomicBoolean(false);
    private final Map<String, String> clientNames = new ConcurrentHashMap<>();
    private final Map<String, String> clientPreviews = new ConcurrentHashMap<>();
    private final Map<String, String> clientTimes = new ConcurrentHashMap<>();
    private final Map<String, Boolean> clientHasUnread = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private void showChat() {
        contentArea.getChildren().clear();

        HBox chatRoot = new HBox();
        chatRoot.setStyle("-fx-background-radius: 18;");
        HBox.setHgrow(chatRoot, Priority.ALWAYS);
        VBox.setVgrow(chatRoot, Priority.ALWAYS);

        VBox leftPanel = new VBox();
        leftPanel.setPrefWidth(340);
        leftPanel.setStyle("-fx-background-color: rgba(255,255,255,0.96); -fx-background-radius: 18 0 0 18; -fx-border-color: rgba(255,255,255,0.9); -fx-border-width: 1 0 1 1;");

        HBox leftHeader = new HBox(10);
        leftHeader.setPadding(new Insets(16, 16, 12, 16));
        leftHeader.setStyle("-fx-background-color: linear-gradient(to right, #0D4E2C, #1A5D3A); -fx-background-radius: 18 0 0 0;");
        leftHeader.setAlignment(Pos.CENTER_LEFT);

        Label statusDot = new Label("●");
        statusDot.setStyle("-fx-font-size: 16px; -fx-text-fill: #FBBF24;");
        Label headerTitle = new Label("Percakapan");
        headerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: white;");
        Region hdrSpacer = new Region();
        HBox.setHgrow(hdrSpacer, Priority.ALWAYS);
        Button refreshChatBtn = Ui.button("⟳", "btn-secondary", e -> new Thread(() -> loadClientListAsync(findClientList())).start());
        refreshChatBtn.setFont(Theme.fontBold(14));
        leftHeader.getChildren().addAll(statusDot, headerTitle, hdrSpacer, refreshChatBtn);

        ListView<String> clientList = new ListView<>();
        clientList.setId("clientList");
        clientList.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(clientList, Priority.ALWAYS);

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
                avatar.setFill(Color.web(Theme.ACCENT_LIGHT));
                Label initial = new Label(nama.substring(0, 1).toUpperCase());
                initial.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: white;");
                StackPane avatarPane = new StackPane(avatar, initial);

                VBox textCol = new VBox(4);
                HBox.setHgrow(textCol, Priority.ALWAYS);
                Label nameLbl = new Label(nama);
                nameLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT + ";");
                Label previewLbl = new Label(preview);
                previewLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
                textCol.getChildren().addAll(nameLbl, previewLbl);

                VBox rightCol = new VBox(6);
                rightCol.setAlignment(Pos.CENTER_RIGHT);
                Label timeLbl = new Label(time);
                timeLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + Theme.TEXT_SUBTLE + ";");
                if (unread) {
                    Circle badge = new Circle(9);
                    badge.setFill(Color.web("#22C55E"));
                    Label badgeDot = new Label("•");
                    badgeDot.setStyle("-fx-font-size: 10px; -fx-text-fill: white;");
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

        VBox rightPanel = new VBox();
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        rightPanel.setStyle("-fx-background-color: rgba(232,241,236,0.92); -fx-background-radius: 0 18 18 0; -fx-border-color: rgba(255,255,255,0.9); -fx-border-width: 1 1 1 0;");

        HBox chatHeaderBar = new HBox(12);
        chatHeaderBar.setId("chatHeaderBar");
        chatHeaderBar.setPadding(new Insets(12, 20, 12, 20));
        chatHeaderBar.setStyle("-fx-background-color: linear-gradient(to right, #0D4E2C, #1A5D3A); -fx-background-radius: 0 18 0 0;");
        chatHeaderBar.setAlignment(Pos.CENTER_LEFT);

        Circle chatAvatar = new Circle(20);
        chatAvatar.setFill(Color.web(Theme.ACCENT_LIGHT));
        Label chatAvatarInit = new Label("?");
        chatAvatarInit.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: white;");
        StackPane chatAvatarPane = new StackPane(chatAvatar, chatAvatarInit);

        VBox chatTitleBox = new VBox(2);
        Label chatName = new Label("Pilih percakapan");
        chatName.setId("chatName");
        chatName.setStyle("-fx-font-size: 17px; -fx-font-weight: 800; -fx-text-fill: white;");
        Label chatStatus = new Label("");
        chatStatus.setId("chatStatus");
        chatStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: #E2E8F0;");
        chatTitleBox.getChildren().addAll(chatName, chatStatus);

        Region hdrSpacer2 = new Region();
        HBox.setHgrow(hdrSpacer2, Priority.ALWAYS);
        chatHeaderBar.getChildren().addAll(chatAvatarPane, chatTitleBox, hdrSpacer2);

        ListView<HBox> chatList = new ListView<>();
        chatList.setId("chatList");
        chatList.setStyle("-fx-background-color: rgba(232,241,236,0.92); -fx-border-color: transparent;");
        VBox.setVgrow(chatList, Priority.ALWAYS);
        chatList.setCellFactory(lv -> new ListCell<HBox>() {
            @Override
            protected void updateItem(HBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setStyle("-fx-background-color: rgba(232,241,236,0.92); -fx-padding: 2 8;");
                } else {
                    setGraphic(item);
                    setStyle("-fx-background-color: rgba(232,241,236,0.92); -fx-padding: 2 8;");
                }
            }
        });

        HBox inputRow = new HBox(8);
        inputRow.setId("inputRow");
        inputRow.setPadding(new Insets(10, 20, 10, 20));
        inputRow.setStyle("-fx-background-color: rgba(255,255,255,0.9);");
        inputRow.setAlignment(Pos.CENTER);

        TextField msgField = Ui.textField("Pilih percakapan dulu...");
        msgField.setId("msgField");
        msgField.setDisable(true);
        msgField.setStyle("-fx-background-color: white; -fx-background-radius: 24; -fx-border-radius: 24; -fx-padding: 12 18; -fx-border-color: rgba(20,60,40,0.12); -fx-font-size: 15px;");
        HBox.setHgrow(msgField, Priority.ALWAYS);
        msgField.setOnAction(e -> doAdminSend(msgField));

        Button sendBtn = Ui.button("Kirim", "btn-primary", e -> doAdminSend(msgField));
        sendBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #2D7A4F, #1A5D3A); -fx-text-fill: white; -fx-background-radius: 24; -fx-padding: 12 20; -fx-font-size: 15px; -fx-font-weight: 800; -fx-cursor: hand;");

        inputRow.getChildren().addAll(msgField, sendBtn);
        rightPanel.getChildren().addAll(chatHeaderBar, chatList, inputRow);

        chatRoot.getChildren().addAll(leftPanel, rightPanel);
        contentArea.getChildren().add(chatRoot);

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
            final String finalUid = uid;
            new Thread(() -> {
                loadChatMessagesAsync(finalUid);
                markMessagesRead(finalUid);
            }).start();
        });

        new Thread(() -> {
            loadClientListAsync(clientList);
            javafx.application.Platform.runLater(() -> {
                if (clientList.getItems() != null && !clientList.getItems().isEmpty()) {
                    clientList.getSelectionModel().select(0);
                }
            });
        }).start();

        connectChatWebSocket(statusDot, chatStatus);
    }

    private void connectChatWebSocket(Label statusDot, Label chatStatus) {
        if (chatWsClient != null) { chatWsClient.disconnect(); chatWsClient = null; }

        chatWsClient = new com.resik.admin.service.ChatWebSocketClient(
                AppConfig.API_BASE_URL,
                msg -> javafx.application.Platform.runLater(() -> {
                    String type = String.valueOf(msg.getOrDefault("type", ""));
                    if ("NEW_MESSAGE".equals(type)) {
                        String msgUserId = msg.get("userId") != null ? String.valueOf(msg.get("userId")) : "";
                        String sender = String.valueOf(msg.getOrDefault("pengirim", ""));
                        String pesan = String.valueOf(msg.getOrDefault("pesan", ""));
                        String createdAt = msg.get("createdAt") != null ? String.valueOf(msg.get("createdAt")) : "";
                        String time = createdAt.length() > 16 ? createdAt.substring(11, 16) : createdAt;
                        clientPreviews.put(msgUserId, pesan.length() > 30 ? pesan.substring(0, 27) + "..." : pesan);
                        clientTimes.put(msgUserId, time);
                        if (!"ADMIN".equals(sender) && !msgUserId.equals(currentChatUserId)) clientHasUnread.put(msgUserId, true);
                        java.awt.Toolkit.getDefaultToolkit().beep();
                        pulseChatBubble();
                        new Thread(() -> loadClientListAsync(findClientList())).start();
                        if (msgUserId.equals(currentChatUserId)) {
                            ListView<HBox> chatList = findChatBubbleList();
                            if (chatList != null) {
                                HBox bubble = createChatBubble(pesan, time, "ADMIN".equals(sender));
                                chatList.getItems().add(bubble);
                                chatList.scrollTo(chatList.getItems().size() - 1);
                            }
                            if (!"ADMIN".equals(sender)) new Thread(() -> markMessagesRead(msgUserId)).start();
                        } else {
                            chatStatus.setText("Pesan baru dari " + clientNames.getOrDefault(msgUserId, "User #" + msgUserId));
                        }
                    }
                }),
                () -> javafx.application.Platform.runLater(() -> { statusDot.setStyle("-fx-font-size: 16px; -fx-text-fill: #22C55E;"); if (currentChatUserId != null) chatStatus.setText("Online"); }),
                () -> javafx.application.Platform.runLater(() -> { statusDot.setStyle("-fx-font-size: 16px; -fx-text-fill: #EF4444;"); chatStatus.setText("Menghubungkan..."); })
        );
        Thread wsThread = new Thread(() -> chatWsClient.connect());
        wsThread.setDaemon(true);
        wsThread.setName("ws-chat");
        wsThread.start();

        if (chatTimeline != null) chatTimeline.stop();
        chatTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(15), e -> {
                    if (chatPollingInProgress.compareAndSet(false, true)) {
                        new Thread(() -> {
                            try {
                                loadClientListAsync(findClientList());
                                if (currentChatUserId != null) loadChatMessagesAsync(currentChatUserId);
                            } finally {
                                chatPollingInProgress.set(false);
                            }
                        }).start();
                    }
                })
        );
        chatTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        chatTimeline.play();
    }

    @SuppressWarnings("unchecked")
    private ListView<String> findClientList() {
        if (popupClientList != null) return popupClientList;
        return contentArea.getChildren().stream()
                .filter(n -> n instanceof HBox).map(n -> (HBox) n)
                .flatMap(hb -> hb.getChildren().stream())
                .filter(n -> n instanceof VBox).map(n -> (VBox) n)
                .flatMap(vb -> vb.getChildren().stream())
                .filter(n -> n instanceof ListView && "clientList".equals(n.getId()))
                .findFirst().map(n -> (ListView<String>) n).orElse(null);
    }

    @SuppressWarnings("unchecked")
    private void loadClientListAsync(ListView<String> clientList) {
        if (clientList == null) return;
        try {
            List<Map<String, Object>> latestChats = ApiClient.getList("/chat/latest");
            ObservableList<String> items = FXCollections.observableArrayList();
            for (Map<String, Object> m : latestChats) {
                String userId;
                String userDisplay;
                Object userObj = m.get("user");
                if (userObj instanceof Map) {
                    Map<String, Object> um = (Map<String, Object>) userObj;
                    userId = String.valueOf(um.getOrDefault("id", m.get("userId")));
                    userDisplay = String.valueOf(um.getOrDefault("nama", "User #" + userId));
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
            final ObservableList<String> finalItems = items;
            javafx.application.Platform.runLater(() -> {
                int selIdx = clientList.getSelectionModel().getSelectedIndex();
                clientList.getItems().setAll(finalItems);
                if (selIdx >= 0 && selIdx < finalItems.size()) {
                    clientList.getSelectionModel().select(selIdx);
                }
                long unread = clientHasUnread.values().stream().filter(Boolean::booleanValue).count();
                if (chatUnreadLabel != null) {
                    chatUnreadLabel.setText(String.valueOf(unread));
                }
            });
        } catch (Exception e) {
            System.err.println("[Chat] loadClientListAsync error: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadChatMessagesAsync(String userId) {
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
                items.add(createChatBubble(pesan, time, "ADMIN".equals(sender)));
            }
            javafx.application.Platform.runLater(() -> {
                chatList.setItems(items);
                if (!items.isEmpty()) chatList.scrollTo(items.size() - 1);
            });
        } catch (Exception e) {
            javafx.application.Platform.runLater(() -> {
                if (chatList.getItems() == null || chatList.getItems().isEmpty())
                    chatList.setItems(FXCollections.observableArrayList(createChatBubble("Gagal memuat pesan: " + e.getMessage(), "", false)));
            });
        }
    }

    @SuppressWarnings("unchecked")
    private ListView<HBox> findChatBubbleList() {
        if (popupChatList != null) return popupChatList;
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

    private HBox createChatBubble(String pesan, String time, boolean fromAdmin) {
        HBox row = new HBox();
        row.setPadding(new Insets(6, 16, 6, 16));

        VBox bubble = new VBox(2);
        bubble.setMaxWidth(420);
        bubble.setPadding(new Insets(10, 14, 8, 14));

        Label msgLbl = new Label(pesan);
        msgLbl.setFont(Theme.font(15));
        msgLbl.setWrapText(true);
        msgLbl.setMaxWidth(380);

        HBox metaRow = new HBox();
        metaRow.setAlignment(Pos.CENTER_RIGHT);
        Label timeLbl = new Label(time);
        timeLbl.setFont(Theme.font(11));

        if (fromAdmin) {
            row.setAlignment(Pos.CENTER_RIGHT);
            bubble.setStyle("-fx-background-color: linear-gradient(to bottom, #2D7A4F, #1A5D3A); -fx-background-radius: 14 14 4 14;");
            msgLbl.setTextFill(Color.WHITE);
            timeLbl.setTextFill(Color.web("rgba(255,255,255,0.78)"));
            Label tick = new Label(" ✓✓");
            tick.setFont(Theme.font(11));
            tick.setTextFill(Color.web("#A7F3D0"));
            metaRow.getChildren().addAll(timeLbl, tick);
        } else {
            row.setAlignment(Pos.CENTER_LEFT);
            bubble.setStyle("-fx-background-color: rgba(255,255,255,0.96); -fx-background-radius: 14 14 14 4;");
            msgLbl.setTextFill(Color.web(Theme.TEXT));
            timeLbl.setTextFill(Color.web(Theme.TEXT_SUBTLE));
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
        final String finalUid = uid;
        final String finalMsg = msg;
        msgField.clear();
        new Thread(() -> {
            try {
                Map<String, Object> body = new HashMap<>();
                body.put("userId", Long.parseLong(finalUid));
                body.put("pengirim", "ADMIN");
                body.put("pesan", finalMsg);
                ApiClient.post("/chat", body, Map.class);
                loadChatMessagesAsync(finalUid);
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() ->
                        CrudDialog.showInfo("Error", "Gagal kirim: " + ex.getMessage()));
            }
        }).start();
    }

    private void markMessagesRead(String userId) {
        try {
            ApiClient.put("/chat/" + userId + "/read", null, Void.class);
        } catch (Exception ignored) { }
    }

    /* ---------------- LOKASI BANK SAMPAH ---------------- */

    private void showLokasi() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pageHeader("Lokasi Bank Sampah", "Kelola lokasi bank sampah (VPS)."));

        HBox bar = new HBox(10);
        bar.getChildren().add(Ui.button("Tambah Lokasi", "btn-primary", e -> {
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
        bar.getChildren().add(Ui.button("Detail", "btn-secondary", e -> {
            TableView<String[]> tbl = (TableView<String[]>) contentArea.getChildren().stream()
                    .filter(n -> n instanceof TableView).findFirst().orElse(null);
            if (tbl == null) return;
            String[] sel = tbl.getSelectionModel().getSelectedItem();
            if (sel == null) {
                CrudDialog.showInfo("Info", "Pilih lokasi terlebih dahulu");
                return;
            }
            showLokasiDetail(sel);
        }));
        bar.getChildren().add(Ui.button("Edit", "btn-secondary", e -> {
            TableView<String[]> tbl = (TableView<String[]>) contentArea.getChildren().stream()
                    .filter(n -> n instanceof TableView).findFirst().orElse(null);
            if (tbl == null) return;
            String[] sel = tbl.getSelectionModel().getSelectedItem();
            if (sel == null) {
                CrudDialog.showInfo("Info", "Pilih lokasi terlebih dahulu");
                return;
            }
            Map<String, String> init = new HashMap<>();
            init.put("nama", sel[0]);
            init.put("alamat", sel[1]);
            init.put("jam", sel[2]);
            init.put("telepon", sel[3]);
            CrudDialog.showForm("Edit Lokasi", "Simpan",
                    List.of(
                            new CrudDialog.FormField("nama", "Nama", ""),
                            new CrudDialog.FormField("alamat", "Alamat", ""),
                            new CrudDialog.FormField("jam", "Jam Operasional", ""),
                            new CrudDialog.FormField("telepon", "Telepon", ""),
                            new CrudDialog.FormField("rekening", "Rekening Pencairan", "BCA 8820 1234 5678"),
                            new CrudDialog.FormField("foto", "Foto Bank Sampah", "Pilih file foto...")
                    ), init
            ).ifPresent(vals -> {
                sel[0] = vals.get("nama");
                sel[1] = vals.get("alamat");
                sel[2] = vals.get("jam");
                sel[3] = vals.get("telepon");
                tbl.refresh();
                CrudDialog.showInfo("Berhasil", "Lokasi diperbarui.");
            });
        }));
        bar.getChildren().add(Ui.button("Refresh", "btn-secondary", e -> showLokasi()));
        contentArea.getChildren().addAll(bar, new Separator());

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

        TableView<String[]> table = styledTable();
        table.setItems(FXCollections.observableArrayList(Arrays.asList(lokasiData)));
        TableColumn<String[], String> c1 = new TableColumn<>("Nama");
        c1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        TableColumn<String[], String> c2 = new TableColumn<>("Alamat");
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        TableColumn<String[], String> c3 = new TableColumn<>("Jam Operasional");
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        TableColumn<String[], String> c4 = new TableColumn<>("Telepon");
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
        table.getColumns().addAll(c1, c2, c3, c4);

        ContextMenu cm = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Hapus Lokasi");
        deleteItem.setOnAction(e -> {
            String[] sel = table.getSelectionModel().getSelectedItem();
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
        table.setContextMenu(cm);
        contentArea.getChildren().add(table);

        Label infoLabel = new Label("Lokasi tersimpan di VPS. Data sinkron antara Android dan Desktop.");
        infoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        infoLabel.setPadding(new Insets(12, 0, 0, 0));
        contentArea.getChildren().add(infoLabel);
    }

    /* ---------------- PROFIL ---------------- */

    private void showProfil() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pageHeader("Profil", "Informasi akun administrator."));

        VBox card = Ui.card();

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        TextField namaField = Ui.textField();
        namaField.setText(AppConfig.userName);
        TextField emailField = Ui.textField();
        emailField.setText(AppConfig.userEmail != null ? AppConfig.userEmail : "admin@resik.id");
        TextField telpField = Ui.textField();
        telpField.setText("0812-0000-0000");
        TextArea alamatArea = new TextArea("Jl. Resik Admin No. 1, Jakarta");
        alamatArea.getStyleClass().add("input-area");
        alamatArea.setPrefRowCount(2);

        Label namaLabel = new Label("Nama:");
        namaLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        Label emailLabel = new Label("Email:");
        emailLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        Label telpLabel = new Label("No. Telepon:");
        telpLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        Label alamatLabel = new Label("Alamat:");
        alamatLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: " + Theme.TEXT_MUTED + ";");

        grid.add(namaLabel, 0, 0); grid.add(namaField, 1, 0);
        grid.add(emailLabel, 0, 1); grid.add(emailField, 1, 1);
        grid.add(telpLabel, 0, 2); grid.add(telpField, 1, 2);
        grid.add(alamatLabel, 0, 3); grid.add(alamatArea, 1, 3);
        GridPane.setHgrow(namaField, Priority.ALWAYS);
        GridPane.setHgrow(emailField, Priority.ALWAYS);
        GridPane.setHgrow(telpField, Priority.ALWAYS);
        GridPane.setHgrow(alamatArea, Priority.ALWAYS);

        Label roleLabel = new Label("Role: " + AppConfig.userRole);
        roleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        Label idLabel = new Label("User ID: " + AppConfig.userId);
        idLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");

        HBox actionBar = new HBox(10);
        actionBar.getChildren().add(Ui.button("Simpan Perubahan", "btn-primary", e -> {
            AppConfig.userName = namaField.getText().trim();
            AppConfig.userEmail = emailField.getText().trim();
            CrudDialog.showInfo("Berhasil", "Profil berhasil diperbarui.");
            showProfil();
        }));
        actionBar.getChildren().add(Ui.button("Upload Foto", "btn-secondary", e ->
                CrudDialog.showInfo("Info", "Fitur upload foto akan tersedia di versi mendatang.")));

        card.getChildren().addAll(grid, roleLabel, idLabel, actionBar);
        contentArea.getChildren().add(card);
    }

    /** Opens complete bank sampah information including payout account. */
    private void showLokasiDetail(String[] row) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detail Bank Sampah");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStyleClass().add("glass-dialog");

        VBox box = new VBox(8);
        box.setPadding(new Insets(20));

        Label name = new Label(row[0]);
        name.setStyle("-fx-font-size: 17px; -fx-font-weight: 800; -fx-text-fill: " + Theme.TEXT + ";");
        Label alamat = new Label(row[1]);
        alamat.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.TEXT + ";");
        Label jam = new Label("Jam: " + row[2]);
        jam.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        Label telp = new Label("Telepon: " + row[3]);
        telp.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.TEXT_MUTED + ";");
        Label rek = new Label("Rekening pencairan: BCA 8820 1234 5678 a.n. " + row[0]);
        rek.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.ACCENT + ";");
        Label foto = new Label("Foto: https://placehold.co/600x300/1A5D3A/FFF?text=Bank%20Sampah");
        foto.setStyle("-fx-font-size: 11px; -fx-text-fill: " + Theme.TEXT_SUBTLE + ";");

        box.getChildren().addAll(name, alamat, jam, telp, rek, foto);
        dialog.getDialogPane().setContent(box);

        ButtonType closeBtn = new ButtonType("Tutup", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeBtn);
        Button close = (Button) dialog.getDialogPane().lookupButton(closeBtn);
        if (close != null) close.getStyleClass().addAll("btn", "btn-secondary");
        dialog.showAndWait();
    }

    private void exportLaporanPdf(LocalDate selected) {
        java.io.File file = new java.io.File(System.getProperty("user.home"), "Desktop/Laporan-Resik-" + selected + ".pdf");
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
            com.lowagie.text.Document doc = new com.lowagie.text.Document();
            com.lowagie.text.pdf.PdfWriter.getInstance(doc, fos);
            doc.open();
            doc.add(new com.lowagie.text.Paragraph("LAPORAN HARIAN RESIK"));
            doc.add(new com.lowagie.text.Paragraph("Tanggal: " + selected));
            doc.add(new com.lowagie.text.Paragraph(" "));
            int totalSetoran = 0;
            BigDecimal totalPembayaran = BigDecimal.ZERO;
            double totalKg = 0;
            for (Penjemputan p : penjemputanData) {
                if (p.getTanggalJemput() != null && p.getTanggalJemput().equals(selected) && "SELESAI".equals(p.getStatus()) && matchesBank(p)) {
                    totalSetoran++;
                    BigDecimal total = p.getTransaksi() != null ? p.getTransaksi().getTotalHarga() : BigDecimal.ZERO;
                    totalPembayaran = totalPembayaran.add(total);
                    if (p.getDetailList() != null) for (var d : p.getDetailList()) if (d.getBeratAktual() != null) totalKg += d.getBeratAktual().doubleValue();
                    doc.add(new com.lowagie.text.Paragraph(String.format("#%03d | %s | %s | Rp %s", p.getId(), p.getAlamat(), p.getStatus(), total)));
                }
            }
            doc.add(new com.lowagie.text.Paragraph(" "));
            doc.add(new com.lowagie.text.Paragraph("Total Setoran: " + totalSetoran));
            doc.add(new com.lowagie.text.Paragraph("Total Pembayaran: Rp " + totalPembayaran));
            doc.add(new com.lowagie.text.Paragraph(String.format("Total Sampah: %.1f kg", totalKg)));
            doc.add(new com.lowagie.text.Paragraph(String.format("Estimasi CO2 Reduction: %.1f kg", totalKg * 2)));
            doc.close();
            CrudDialog.showInfo("Berhasil", "Laporan PDF di-export ke:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            CrudDialog.showInfo("Error", "Gagal export PDF: " + ex.getMessage());
        }
    }

    private void pilihFileGambar(String judul) {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle(judul);
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Gambar (PNG, JPG, JPEG)", "*.png", "*.jpg", "*.jpeg"));
        java.io.File file = fc.showOpenDialog(view.getScene().getWindow());
        if (file == null) return;
        try {
            java.nio.file.Path dir = java.nio.file.Paths.get("uploads");
            java.nio.file.Files.createDirectories(dir);
            java.nio.file.Path target = dir.resolve(System.currentTimeMillis() + "-" + file.getName());
            java.nio.file.Files.copy(file.toPath(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            CrudDialog.showInfo("Upload berhasil", "File tersimpan di:\n" + target.toAbsolutePath());
        } catch (Exception ex) {
            CrudDialog.showInfo("Error", "Gagal menyimpan file: " + ex.getMessage());
        }
    }

    private TableView<String[]> quickTable(String[] headers, String[][] rows) {
        TableView<String[]> table = styledTable();
        for (int i = 0; i < headers.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(headers[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(
                    idx < data.getValue().length ? data.getValue()[idx] : ""));
            table.getColumns().add(col);
        }
        table.setItems(FXCollections.observableArrayList(Arrays.asList(rows)));
        return table;
    }

    private void showVerifikasi() {
        currentView = "verifikasi";
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pageHeader("Verifikasi & Approval", "Approve akun Bank Sampah dan masyarakat sebelum aktif."));

        contentArea.getChildren().add(Ui.heading("Bank Sampah"));
        TableView<String[]> bank = quickTable(
                new String[]{"ID", "Nama Bank Sampah", "KTP", "Foto Bank", "Rekening", "Status"},
                new String[][]{
                        {"1", "Bank Sampah Resik Pusat", "3201234567890001", "Ada", "BCA 8820 1234 5678", "MENUNGGU"},
                        {"2", "Bank Sampah Resik Selatan", "3201234567890002", "Ada", "Mandiri 1300 9876 5432", "MENUNGGU"},
                        {"3", "Bank Sampah Resik Timur", "3201234567890003", "Belum", "BRI 0021 4567 8901", "DITOLAK"}
                });
        contentArea.getChildren().add(bank);

        HBox bar = new HBox(10);
        bar.getChildren().add(Ui.button("Preview", "btn-secondary", e -> {
            String[] sel = bank.getSelectionModel().getSelectedItem();
            if (sel == null) {
                CrudDialog.showInfo("Info", "Pilih akun bank sampah terlebih dahulu.");
                return;
            }
            CrudDialog.showInfo("Detail " + sel[1],
                    "KTP: " + sel[2] + "\nFoto Bank: " + sel[3] + "\nRekening Pencairan: " + sel[4] + "\nStatus: " + sel[5]);
        }));
        bar.getChildren().add(Ui.button("Upload KTP/Foto", "btn-secondary", e -> pilihFileGambar("Upload KTP atau Foto Bank Sampah")));
        bar.getChildren().add(Ui.button("Approve", "btn-primary", e -> {
            String[] sel = bank.getSelectionModel().getSelectedItem();
            if (sel == null) {
                CrudDialog.showInfo("Info", "Pilih akun bank sampah terlebih dahulu.");
                return;
            }
            try {
                ApiService.post("/admin/verifikasi/bank/" + sel[0] + "/approve", Map.of("status", "AKTIF"));
            } catch (Exception ignored) {
                // offline demo — tetap update lokal
            }
            sel[5] = "AKTIF";
            bank.refresh();
            CrudDialog.showInfo("Approval", "Akun " + sel[1] + " disetujui dan sekarang aktif.");
        }));
        bar.getChildren().add(Ui.button("Tolak", "btn-danger-ghost", e -> {
            String[] sel = bank.getSelectionModel().getSelectedItem();
            if (sel == null) {
                CrudDialog.showInfo("Info", "Pilih akun bank sampah terlebih dahulu.");
                return;
            }
            try {
                ApiService.post("/admin/verifikasi/bank/" + sel[0] + "/reject", Map.of("status", "DITOLAK"));
            } catch (Exception ignored) {
                // offline demo — tetap update lokal
            }
            sel[5] = "DITOLAK";
            bank.refresh();
            CrudDialog.showInfo("Approval", "Akun " + sel[1] + " ditolak.");
        }));
        contentArea.getChildren().add(bar);

        contentArea.getChildren().add(Ui.heading("Masyarakat"));
        TableView<String[]> users = quickTable(
                new String[]{"ID", "Nama", "KTP", "Email", "Status"},
                new String[][]{
                        {"1", "Budi Santoso", "3201234567890004", "budi@gmail.com", "AKTIF"},
                        {"2", "Siti Nurhaliza", "3201234567890005", "siti@gmail.com", "MENUNGGU"}
                });
        contentArea.getChildren().add(users);
    }

    private void showManajemenBank() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pageHeader("Manajemen Bank Sampah", "Status aktif, rating, batas layanan, dan komisi platform."));
        TableView<String[]> table = quickTable(
                new String[]{"Nama", "Rating", "Batas Layanan/hari", "Komisi", "Status"},
                new String[][]{
                        {"Bank Sampah Resik Pusat", "4.8", "20", "5%", "AKTIF"},
                        {"Bank Sampah Resik Selatan", "4.5", "15", "5%", "AKTIF"},
                        {"Bank Sampah Resik Timur", "3.9", "10", "7%", "NONAKTIF"}
                });
        contentArea.getChildren().add(table);
        HBox bar = new HBox(10);
        bar.getChildren().add(Ui.button("Edit Komisi", "btn-secondary", e -> {
            String[] sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                CrudDialog.showInfo("Info", "Pilih bank sampah terlebih dahulu.");
                return;
            }
            CrudDialog.showForm("Edit Komisi — " + sel[0], "Simpan",
                    List.of(
                            new CrudDialog.FormField("komisi", "Komisi (%)", sel[3]),
                            new CrudDialog.FormField("batas", "Batas Layanan/hari", sel[2])
                    ), null
            ).ifPresent(vals -> {
                sel[3] = vals.get("komisi");
                sel[2] = vals.get("batas");
                table.refresh();
                CrudDialog.showInfo("Berhasil", "Komisi dan batas layanan diperbarui.");
            });
        }));
        bar.getChildren().add(Ui.button("Nonaktifkan", "btn-danger-ghost", e -> {
            String[] sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                CrudDialog.showInfo("Info", "Pilih bank sampah terlebih dahulu.");
                return;
            }
            sel[4] = "AKTIF".equals(sel[4]) ? "NONAKTIF" : "AKTIF";
            table.refresh();
        }));
        contentArea.getChildren().add(bar);
    }

    private void showAuditLog() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pageHeader("Audit Log", "Riwayat aksi admin: siapa, kapan, dan apa yang diubah."));
        TableView<String[]> table = quickTable(
                new String[]{"Waktu", "Admin", "Aksi", "Detail"},
                new String[][]{
                        {"20 Okt 2026 09:12", "Administrator", "APPROVE", "Approve Bank Sampah Resik Pusat"},
                        {"20 Okt 2026 08:47", "Administrator", "UPDATE", "Ubah harga ORGANIK menjadi Rp 1.000"},
                        {"19 Okt 2026 16:03", "Administrator", "LOGIN", "Login berhasil dari 192.168.1.10"},
                        {"19 Okt 2026 15:21", "Administrator", "REJECT", "Tolak Bank Sampah Resik Timur"}
                });
        contentArea.getChildren().add(table);
    }

    private void showTimbangan() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pageHeader("Input Timbangan", "Konfirmasi hasil timbang dan upload foto bukti."));
        TableView<String[]> table = quickTable(
                new String[]{"ID", "Nama Masyarakat", "Kategori", "Estimasi", "Hasil Timbang", "Status"},
                new String[][]{
                        {"#004", "Rina Marlina", "ORGANIK", "2 kg", "2,4 kg", "SELESAI"},
                        {"#005", "Dewi Anggraini", "ANORGANIK", "3 kg", "2,8 kg", "DIPROSES"},
                        {"#006", "Andi Saputra", "B3", "1 kg", "-", "MENUNGGU"}
                });
        contentArea.getChildren().add(table);
        HBox bar = new HBox(10);
        bar.getChildren().add(Ui.button("Input Hasil", "btn-primary", e -> {
            String[] sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                CrudDialog.showInfo("Info", "Pilih data penjemputan terlebih dahulu.");
                return;
            }
            CrudDialog.showForm("Input Hasil Timbangan", "Simpan",
                    List.of(
                            new CrudDialog.FormField("hasil", "Hasil Timbang (kg)", "-".equals(sel[4]) ? "" : sel[4]),
                            new CrudDialog.FormField("foto", "Foto Bukti", "Pilih file foto bukti timbangan...")
                    ), null
            ).ifPresent(vals -> {
                sel[4] = vals.get("hasil");
                sel[5] = "SELESAI";
                table.refresh();
                CrudDialog.showInfo("Berhasil", "Timbangan tersimpan dan saldo masyarakat bertambah.");
            });
        }));
        bar.getChildren().add(Ui.button("Upload Foto Bukti", "btn-secondary", e -> CrudDialog.showInfo("Info", "Pilih file foto bukti timbangan.")));
        contentArea.getChildren().add(bar);
    }

    private void showStokSampah() {
        currentView = "stok";
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pageHeader("Stok Sampah", "Akumulasi kg per kategori, siap jual ke pengepul."));
        TableView<String[]> table = quickTable(
                new String[]{"Kategori", "Total kg", "Harga beli/kg", "Harga jual/kg", "Margin"},
                new String[][]{
                        {"ORGANIK", "1.240", "Rp 1.000", "Rp 1.400", "Rp 496.000"},
                        {"ANORGANIK", "860", "Rp 2.500", "Rp 3.200", "Rp 602.000"},
                        {"B3", "40", "Rp 500", "Rp 800", "Rp 12.000"},
                        {"RESIDU", "310", "Rp 300", "Rp 500", "Rp 62.000"}
                });
        contentArea.getChildren().add(table);
        contentArea.getChildren().add(Ui.button("Jual ke Pengepul", "btn-primary", e -> CrudDialog.showInfo("Info", "Pilih pengepul dan buat nota penjualan.")));
    }

    private void showLaporanBank() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pageHeader("Laporan Bank", "Performa bank sampah per periode."));
        TableView<String[]> table = quickTable(
                new String[]{"Periode", "Setoran", "Total kg", "Pendapatan", "Pencairan"},
                new String[][]{
                        {"Hari ini", "4", "18 kg", "Rp 42.500", "-"},
                        {"Minggu ini", "22", "96 kg", "Rp 231.000", "Rp 200.000"},
                        {"Bulan ini", "87", "410 kg", "Rp 1.102.000", "Rp 1.000.000"}
                });
        contentArea.getChildren().add(table);
    }

    public StackPane getView() {
        return view;
    }

    private static final class EdukasiArticle {
        final String title;
        final String category;
        final String image;
        final String content;

        EdukasiArticle(String title, String category, String image, String content) {
            this.title = title;
            this.category = category;
            this.image = image;
            this.content = content;
        }
    }
}
