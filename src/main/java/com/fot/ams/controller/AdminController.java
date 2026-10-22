package com.fot.ams.controller;

<<<<<<< HEAD
public class AdminController {
}
=======
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;

import java.net.URL;

public class AdminController {

    @FXML
    private BorderPane mainPane;

    @FXML
    private void showDashboard() {
        loadPage("adminDashboard.fxml");
    }

    @FXML
    private void showUsers() {
        loadPage("adminUserManagement.fxml");
    }

    @FXML
    private void showCourses() {
        loadPage("adminCourseManagement.fxml");
    }

    @FXML
    private void showNotices() {
        loadPage("adminNoticeManagement.fxml");
    }


    @FXML
    private void showTimetables() {
        loadPage("adminTimetableManagement.fxml");
    }

    private void loadPage(String fileName) {
        URL url = getClass().getResource("/fxml/" + fileName);

        if (url == null) {
            showError("FXML File Not Found", "Cannot find /fxml/" + fileName);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(url);
            Node page = loader.load();

            if (page instanceof BorderPane borderPane) {
                Node content = borderPane.getCenter();

                if (content == null) {
                    showError("Page Error", fileName + " has no center content.");
                    return;
                }

                mainPane.setCenter(content);
            } else {
                mainPane.setCenter(page);
            }

        } catch (Exception e) {
            showError(
                    "Unable to Open Page",
                    fileName + "\n" + e.getClass().getSimpleName() + ": " + e.getMessage()
            );
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
>>>>>>> 152347c62e0478a2d3c845c988f642bcf6272926
