package com.fot.ams;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.fot.ams.util.DBConnection;
import java.sql.Connection;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/fxml/login.fxml")
        );

        Parent root = loader.load();

        stage.setTitle("Academic Management System");
        stage.setScene(new Scene(root, 1200, 750));
        stage.show();
    }

    public static void main(String[] args) {

        try (Connection connection = DBConnection.getStudentConnection()) {
            System.out.println("Database Connected Successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        launch(args);
    }
}