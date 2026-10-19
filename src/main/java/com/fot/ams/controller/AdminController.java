
package com.fot.ams.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
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
        try {
            URL url = getClass().getResource("/fxml/" + fileName);

            if (url == null) {
                System.out.println("FXML file not found: " + fileName);
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            BorderPane page = loader.load();
            Node body = page.getCenter();

            if (body != null) {
                mainPane.setCenter(body);
            }

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
    }
}
