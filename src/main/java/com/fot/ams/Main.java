package com.fot.ams;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/fxml/admin-dashboard.fxml")
        );

        Parent root = loader.load();

        stage.setTitle("Academic Management System");
        stage.setScene(new Scene(root, 1200, 750));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}