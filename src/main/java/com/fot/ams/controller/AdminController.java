package com.fot.ams.controller;

import com.fot.ams.util.DBConnection;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class AdminController {

    @FXML
    private BorderPane mainPane;

    @FXML
    private Label lblUserCount;

    @FXML
    private Label lblCourseCount;

    @FXML
    private Label lblDepartmentCount;

    @FXML
    private Label lblPendingMedicalCount;

    @FXML
    private Label lblDashboardStatus;

    @FXML
    private Button btnRefresh;

    private Node dashboardContent;
    private boolean loadingDashboard;

    @FXML
    private void initialize() {
        if (mainPane != null
                && lblUserCount != null
                && lblCourseCount != null
                && lblDepartmentCount != null
                && lblPendingMedicalCount != null
                && lblDashboardStatus != null
                && btnRefresh != null) {
            dashboardContent = mainPane.getCenter();
            refreshDashboard();
        }
    }

    @FXML
    private void showDashboard() {
        if (dashboardContent == null) {
            loadPage("adminDashboard.fxml");
            return;
        }

        mainPane.setCenter(dashboardContent);
        refreshDashboard();
    }

    @FXML
    private void refreshDashboard() {
        if (dashboardContent == null || loadingDashboard) {
            return;
        }

        loadingDashboard = true;
        btnRefresh.setDisable(true);
        clearCounts();

        lblDashboardStatus.setTextFill(Color.web("#526174"));
        lblDashboardStatus.setText("Loading dashboard...");

        Task<long[]> task = new Task<>() {
            @Override
            protected long[] call() throws SQLException {
                return fetchDashboardCounts();
            }
        };

        task.setOnSucceeded(event -> {
            long[] counts = task.getValue();

            lblUserCount.setText(Long.toString(counts[0]));
            lblCourseCount.setText(Long.toString(counts[1]));
            lblDepartmentCount.setText(Long.toString(counts[2]));
            lblPendingMedicalCount.setText(Long.toString(counts[3]));

            String updatedTime = LocalTime.now().format(
                    DateTimeFormatter.ofPattern("hh:mm:ss a")
            );

            lblDashboardStatus.setTextFill(Color.web("#526174"));
            lblDashboardStatus.setText("Last updated: " + updatedTime);

            loadingDashboard = false;
            btnRefresh.setDisable(false);
        });

        task.setOnFailed(event -> {
            clearCounts();

            Throwable error = task.getException();
            String message = error == null || error.getMessage() == null
                    ? "Unknown database error."
                    : error.getMessage();

            lblDashboardStatus.setTextFill(Color.web("#C62828"));
            lblDashboardStatus.setText(
                    "Unable to load dashboard: " + message
                            + " Click Refresh to try again."
            );

            loadingDashboard = false;
            btnRefresh.setDisable(false);
        });

        Thread thread = new Thread(task, "admin-dashboard-loader");
        thread.setDaemon(true);
        thread.start();
    }

    private long[] fetchDashboardCounts() throws SQLException {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM `user`) AS user_count,
                    (SELECT COUNT(*) FROM `course`) AS course_count,
                    (SELECT COUNT(*) FROM `department`) AS department_count,
                    (SELECT COUNT(*) FROM `medical`
                     WHERE `status` = ?) AS pending_medical_count
                """;

        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                throw new SQLException("Database connection is unavailable.");
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, "Pending");
                statement.setQueryTimeout(15);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new SQLException("No dashboard data was returned.");
                    }

                    return new long[]{
                            resultSet.getLong("user_count"),
                            resultSet.getLong("course_count"),
                            resultSet.getLong("department_count"),
                            resultSet.getLong("pending_medical_count")
                    };
                }
            }
        }
    }

    private void clearCounts() {
        lblUserCount.setText("—");
        lblCourseCount.setText("—");
        lblDepartmentCount.setText("—");
        lblPendingMedicalCount.setText("—");
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

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
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

                borderPane.setCenter(null);
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