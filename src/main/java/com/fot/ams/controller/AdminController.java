
package com.fot.ams.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

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

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText("You will be redirected to the login page.");

        java.util.Optional<javafx.scene.control.ButtonType> result =
                alert.showAndWait();

        if (result.isPresent()
                && result.get() == javafx.scene.control.ButtonType.OK) {
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        try {
            URL url = getClass().getResource("/fxml/login.fxml");

            if (url == null) {
                showError(
                        "FXML File Not Found",
                        "Cannot find /fxml/login.fxml"
                );
                return;
            }

            Parent root = FXMLLoader.load(url);

            Stage stage = (Stage) mainPane.getScene().getWindow();

            stage.setScene(new Scene(root, 1000, 600));
            stage.setTitle("AMS - Login");
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            showError(
                    "Unable to Open Login Page",
                    e.getClass().getSimpleName() + ": " + e.getMessage()
            );
        }
    }

    private void loadPage(String fileName) {
        URL url = getClass().getResource("/fxml/" + fileName);

        if (url == null) {
            showError(
                    "FXML File Not Found",
                    "Cannot find /fxml/" + fileName
            );
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(url);
            Node page = loader.load();

            if (page instanceof BorderPane borderPane) {
                Node content = borderPane.getCenter();

                if (content == null) {
                    showError(
                            "Page Error",
                            fileName + " has no center content."
                    );
                    return;
                }

                mainPane.setCenter(content);
            } else {
                mainPane.setCenter(page);
            }

        } catch (Exception e) {
            showError(
                    "Unable to Open Page",
                    fileName + "\n"
                            + e.getClass().getSimpleName()
                            + ": " + e.getMessage()
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
