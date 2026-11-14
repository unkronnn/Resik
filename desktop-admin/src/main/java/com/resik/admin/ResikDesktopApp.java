package com.resik.admin;

import com.resik.admin.ui.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ResikDesktopApp extends Application {

    @Override
    public void start(Stage stage) {
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getView(), 520, 720);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        var logoStream = getClass().getResourceAsStream("/resik-logo.png");
        if (logoStream != null) {
            stage.getIcons().add(new Image(logoStream));
        }

        stage.setTitle("Resik — Bank Sampah Digital");
        stage.setScene(scene);
        stage.setMinWidth(420);
        stage.setMinHeight(600);
        stage.setResizable(true);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
