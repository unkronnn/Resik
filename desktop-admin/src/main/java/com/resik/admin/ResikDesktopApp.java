package com.resik.admin;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.resik.admin.ui.LoginView;

public class ResikDesktopApp extends Application {

    @Override
    public void start(Stage stage) {
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getView(), 460, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        var logoStream = getClass().getResourceAsStream("/resik-logo.png");
        if (logoStream != null) {
            stage.getIcons().add(new Image(logoStream));
        }

        stage.setTitle("Resik — Bank Sampah Digital");
        stage.setScene(scene);
        stage.setMinWidth(400);
        stage.setMinHeight(560);
        stage.setResizable(true);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
