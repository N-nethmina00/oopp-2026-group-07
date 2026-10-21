package com.fot.ams;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
<<<<<<< HEAD
import javafx.scene.control.Button;
=======
import com.fot.ams.util.DBConnection;
import java.sql.Connection;


>>>>>>> 152347c62e0478a2d3c845c988f642bcf6272926

public class Main extends Application {

    @Override
<<<<<<< HEAD
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
=======
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/fxml/adminDashboard.fxml")
        );

        Parent root = loader.load();

        stage.setTitle("Academic Management System");
        stage.setScene(new Scene(root, 1200, 750));
        stage.show();
>>>>>>> 152347c62e0478a2d3c845c988f642bcf6272926
    }

    public static void main(String[] args) {

        try (Connection connection = DBConnection.getConnection()) {
            System.out.println("Database Connected Successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        launch(args);
    }
}