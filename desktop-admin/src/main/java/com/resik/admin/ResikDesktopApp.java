package com.resik.admin;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.resik.admin.ui.LoginView;

public class ResikDesktopApp extends Application {

    @Override
    public void start(Stage stage) {
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getView(), 400, 500);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setTitle("RESIK — Bank Sampah Digital (Admin)");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
