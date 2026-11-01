package com.fot.ams.controller;

import com.fot.ams.model.User;
import com.fot.ams.util.DBConnection;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AdminController {

    @FXML private BorderPane mainPane;

    @FXML private Label lblUserCount;
    @FXML private Label lblCourseCount;
    @FXML private Label lblDepartmentCount;
    @FXML private Label lblPendingMedicalCount;
    @FXML private Label lblDashboardStatus;

    @FXML private Button btnRefresh;
    @FXML private Button dashboardBtn;
    @FXML private Button usersBtn;
    @FXML private Button coursesBtn;
    @FXML private Button noticesBtn;
    @FXML private Button timetablesBtn;
    @FXML private Button accountBtn;

    private Node dashboardContent;
    private String loggedInUserId;
    private boolean loadingDashboard;
    private Button selectedMenuButton;
    private Timeline menuAnimation;

    @FXML
    private void initialize() {
        dashboardContent = mainPane.getCenter();
        selectMenu(dashboardBtn, false);
    }

    public void setLoggedInUser(User user) {
        if (user == null || !"Admin".equals(user.getRole())) {
            throw new IllegalArgumentException(
                    "A valid admin account is required."
            );
        }

        loggedInUserId = user.getUserId();
        refreshDashboard();
    }

    private void selectMenu(Button target, boolean animate) {
        if (target == null || target == selectedMenuButton) {
            return;
        }

        if (menuAnimation != null) {
            menuAnimation.stop();
        }

        Button[] buttons = {
                dashboardBtn,
                usersBtn,
                coursesBtn,
                noticesBtn,
                timetablesBtn,
                accountBtn
        };

        for (Button button : buttons) {
            if (button != null) {
                button.getStyleClass().removeAll("selected");
                button.setTranslateX(0);
                button.setScaleX(1);
                button.setScaleY(1);
                button.setOpacity(1);
            }
        }

        target.getStyleClass().add("selected");
        selectedMenuButton = target;

        if (!animate) {
            return;
        }

        menuAnimation = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(target.translateXProperty(), -5),
                        new KeyValue(target.scaleXProperty(), 0.96),
                        new KeyValue(target.scaleYProperty(), 0.96),
                        new KeyValue(target.opacityProperty(), 0.65)
                ),
                new KeyFrame(
                        Duration.millis(140),
                        new KeyValue(
                                target.translateXProperty(),
                                3,
                                Interpolator.EASE_OUT
                        ),
                        new KeyValue(
                                target.scaleXProperty(),
                                1,
                                Interpolator.EASE_OUT
                        ),
                        new KeyValue(
                                target.scaleYProperty(),
                                1,
                                Interpolator.EASE_OUT
                        ),
                        new KeyValue(
                                target.opacityProperty(),
                                1,
                                Interpolator.EASE_OUT
                        )
                ),
                new KeyFrame(
                        Duration.millis(240),
                        new KeyValue(
                                target.translateXProperty(),
                                0,
                                Interpolator.EASE_BOTH
                        )
                )
        );

        menuAnimation.play();
    }

    @FXML
    private void showDashboard() {
        mainPane.setCenter(dashboardContent);
        selectMenu(dashboardBtn, true);
        refreshDashboard();
    }

    @FXML
    private void showUsers() {
        openPage("adminUserManagement.fxml", usersBtn);
    }

    @FXML
    private void showCourses() {
        openPage("adminCourseManagement.fxml", coursesBtn);
    }

    @FXML
    private void showNotices() {
        openPage("adminNoticeManagement.fxml", noticesBtn);
    }

    @FXML
    private void showTimetables() {
        openPage("adminTimetableManagement.fxml", timetablesBtn);
    }

    @FXML
    private void showAccount() {
        openPage("adminAccount.fxml", accountBtn);
    }

    private void openPage(String fileName, Button button) {
        if (loggedInUserId == null || loggedInUserId.isBlank()) {
            showError(
                    "Session Required",
                    "Please log in again."
            );
            return;
        }

        try {
            URL resource = getClass().getResource(
                    "/fxml/" + fileName
            );

            if (resource == null) {
                throw new IllegalStateException(
                        "Cannot find /fxml/" + fileName
                );
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Node page = loader.load();

            Object controller = loader.getController();

            if (controller instanceof AdminAccountController account) {
                account.setUserId(loggedInUserId);
            }

            if (page instanceof BorderPane borderPane) {
                Node content = borderPane.getCenter();

                if (content == null) {
                    throw new IllegalStateException(
                            fileName + " has no center content."
                    );
                }

                borderPane.setCenter(null);
                mainPane.setCenter(content);

            } else {
                mainPane.setCenter(page);
            }

            selectMenu(button, true);

        } catch (Exception e) {
            showError(
                    "Unable to Open Page",
                    fileName + "\n"
                            + e.getClass().getSimpleName()
                            + ": " + e.getMessage()
            );
        }
    }

    @FXML
    private void refreshDashboard() {
        if (loadingDashboard || loggedInUserId == null) {
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

            lblDashboardStatus.setTextFill(Color.web("#526174"));

            lblDashboardStatus.setText(
                    "Last updated: "
                            + LocalTime.now().format(
                            DateTimeFormatter.ofPattern(
                                    "hh:mm:ss a"
                            )
                    )
            );

            loadingDashboard = false;
            btnRefresh.setDisable(false);
        });

        task.setOnFailed(event -> {
            clearCounts();

            Throwable error = task.getException();

            lblDashboardStatus.setTextFill(Color.web("#C62828"));

            lblDashboardStatus.setText(
                    "Unable to load dashboard: "
                            + (error == null
                            ? "Unknown database error."
                            : error.getMessage())
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
                throw new SQLException(
                        "Database connection is unavailable."
                );
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, "Pending");
                statement.setQueryTimeout(15);

                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new SQLException(
                                "No dashboard data was returned."
                        );
                    }

                    return new long[]{
                            result.getLong("user_count"),
                            result.getLong("course_count"),
                            result.getLong("department_count"),
                            result.getLong("pending_medical_count")
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
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to log out?");
        alert.setContentText(
                "You will be redirected to the login page."
        );

        if (alert.showAndWait()
                .orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            URL resource = getClass().getResource(
                    "/fxml/login.fxml"
            );

            if (resource == null) {
                throw new IllegalStateException(
                        "Cannot find /fxml/login.fxml"
                );
            }

            Parent root = FXMLLoader.load(resource);

            Stage stage = (Stage) mainPane
                    .getScene()
                    .getWindow();

            if (menuAnimation != null) {
                menuAnimation.stop();
            }

            loggedInUserId = null;

            stage.setScene(new Scene(root, 1000, 600));
            stage.setTitle("AMS - Login");
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            showError(
                    "Unable to Open Login Page",
                    e.getMessage()
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