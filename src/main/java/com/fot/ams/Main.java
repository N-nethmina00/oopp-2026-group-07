package com.fot.ams;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent loginRoot = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        primaryStage.setTitle("AMS - Login");
        primaryStage.setScene(new Scene(loginRoot));
        primaryStage.show();
    }
}