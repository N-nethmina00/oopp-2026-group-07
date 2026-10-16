
package com.fot.ams;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LecturerTestApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                LecturerTestApp.class.getResource(
                        "/fxml/lecturer-dashboard.fxml"
                )
        );

        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 750);

        stage.setTitle("Faculty AMS - Lecturer Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
