package com.fot.ams;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL fxml = Main.class.getResource("/fxml/AdminDashboard.fxml");

        if (fxml == null) {
            throw new IllegalStateException(
                    "AdminDashboard.fxml not found. Check src/main/resources/fxml/"
            );
        }

        Parent root = FXMLLoader.load(fxml);
        Scene scene = new Scene(root, 1280, 800);

        URL css = Main.class.getResource("/css/admin-dashboard.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle("Academic Management System - Admin");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
