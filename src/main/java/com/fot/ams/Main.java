package com.fot.ams;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent loginRoot = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        primaryStage.setTitle("AMS - Login");
        primaryStage.setScene(new Scene(loginRoot));
        primaryStage.show();



        Button loginBtn = (Button) loginRoot.lookup("#btnLogin");

        if (loginBtn != null) {
            loginBtn.setOnAction(event -> {
                try {

                    Parent studentRoot = FXMLLoader.load(getClass().getResource("/fxml/student-layout.fxml"));
                    primaryStage.setTitle("AMS - Student Dashboard");
                    primaryStage.setScene(new Scene(studentRoot, 1100, 680));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}