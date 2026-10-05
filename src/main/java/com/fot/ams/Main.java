package com.fot.ams;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        URL view = Main.class.getResource(
                "/fxml/technical-officer-dashboard.fxml"
        );

        if (view == null) {
            throw new IOException("Dashboard FXML file was not found.");
        }

        FXMLLoader loader = new FXMLLoader(view);
        Scene scene = new Scene(loader.load(), 1000, 650);

        stage.setTitle("Academic Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}