package com.fot.ams.controller;

// Provides the logged-in user details passed from the login screen.
import com.fot.ams.model.User;
// Provides the method used to connect to the application database.
import com.fot.ams.util.DBConnection;

// These JavaFX classes create a small animation when a menu button is selected.
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
// Lets the dashboard query the database in the background without freezing the UI
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
// JDBC types used to connect to the database and read query results
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// Used to show when the dashboard was last refreshed
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// Controls the admin dashboard, navigation, counts, and logout
public class AdminController {

    // Main layout: pages are displayed in its center area
    @FXML private BorderPane mainPane;

    // Dashboard labels display totals and loading/error status
    @FXML private Label lblUserCount;
    @FXML private Label lblCourseCount;
    @FXML private Label lblNoticeCount;
    @FXML private Label lblTimetableCount;
    @FXML private Label lblDashboardStatus;

    // Buttons connected to the refresh action and sidebar navigation
    @FXML private Button btnRefresh;
    @FXML private Button dashboardBtn;
    @FXML private Button usersBtn;
    @FXML private Button coursesBtn;
    @FXML private Button noticesBtn;
    @FXML private Button timetablesBtn;
    @FXML private Button accountBtn;

    // Keep the original dashboard view so it can be shown again after navigation
    private Node dashboardContent;
    // Remember which admin is logged in, for session checks and the account page
    private String loggedInUserId;
    // Prevent starting multiple dashboard queries at the same time
    private boolean loadingDashboard;
    // Track the active menu item and its animation.
    private Button selectedMenuButton;
    private Timeline menuAnimation;

    // JavaFX calls this after loading the FXML and connecting its controls
    @FXML
    private void initialize() {
        // Save the dashboard page before other pages replace it
        dashboardContent = mainPane.getCenter();
        selectMenu(dashboardBtn, false);
    }

    // Called after login to pass the authenticated user into this controller
    public void setLoggedInUser(User user) {
        // Only a valid Admin account should use the admin dashboard
        if (user == null || !"Admin".equals(user.getRole())) {
            throw new IllegalArgumentException(
                    "A valid admin account is required."
            );
        }

        // Save the admin ID and load the dashboard totals
        loggedInUserId = user.getUserId();
        refreshDashboard();
    }

    // Clear the old selection, select the new menu button, and optionally animate it
    private void selectMenu(Button target, boolean animate) {
        if (target == null || target == selectedMenuButton) {
            return;
        }

        if (menuAnimation != null) {
            menuAnimation.stop();
        }

        // Group the sidebar buttons so all can be reset consistently
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

        // Apply the CSS style used to show which page is active
        target.getStyleClass().add("selected");
        selectedMenuButton = target;

        if (!animate) {
            return;
        }

        // Create a short movement and scale animation for visual feedback
        menuAnimation = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(
                                target.translateXProperty(),
                                -5
                        ),
                        new KeyValue(
                                target.scaleXProperty(),
                                0.96
                        ),
                        new KeyValue(
                                target.scaleYProperty(),
                                0.96
                        ),
                        new KeyValue(
                                target.opacityProperty(),
                                0.65
                        )
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

    // Return to the saved dashboard view when its sidebar button is clicked
    @FXML
    private void showDashboard() {
        mainPane.setCenter(dashboardContent);
        selectMenu(dashboardBtn, true);
        refreshDashboard();
    }

    // Open the user-management page
    @FXML
    private void showUsers() {
        openPage(
                "adminUserManagement.fxml",
                usersBtn
        );
    }

    // Open the course-management page
    @FXML
    private void showCourses() {
        openPage(
                "adminCourseManagement.fxml",
                coursesBtn
        );
    }

    // Open the notice-management page
    @FXML
    private void showNotices() {
        openPage(
                "adminNoticeManagement.fxml",
                noticesBtn
        );
    }

    // Open the timetable-management page
    @FXML
    private void showTimetables() {
        openPage(
                "adminTimetableManagement.fxml",
                timetablesBtn
        );
    }

    // Open the admin account page
    @FXML
    private void showAccount() {
        openPage(
                "adminAccount.fxml",
                accountBtn
        );
    }

    // Load an FXML page, show it inside the main layout, and mark its menu button
    private void openPage(String fileName, Button button) {
        // Do not open admin pages if the admin session is missing
        if (loggedInUserId == null || loggedInUserId.isBlank()) {
            showError(
                    "Session Required",
                    "Please log in again."
            );
            return;
        }

        try {
            // Find the requested screen in the app resources
            URL resource = getClass().getResource(
                    "/fxml/" + fileName
            );

            if (resource == null) {
                throw new IllegalStateException(
                        "Cannot find /fxml/" + fileName
                );
            }

            // FXMLLoader creates both the page UI and its controller
            FXMLLoader loader = new FXMLLoader(resource);
            Node page = loader.load();

            Object controller = loader.getController();

            // The account page needs the logged-in admin ID to load that account
            if (controller instanceof AdminAccountController account) {
                account.setUserId(loggedInUserId);
            }

            // For a BorderPane page, place only its center content in the dashboard
            if (page instanceof BorderPane borderPane) {
                Node content = borderPane.getCenter();

                if (content == null) {
                    throw new IllegalStateException(
                            fileName + " has no center content."
                    );
                }

                // A JavaFX node can have only one parent, so detach it first
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

    // Fetch and display the latest counts without blocking the JavaFX interface
    @FXML
    private void refreshDashboard() {
        if (loadingDashboard
                || loggedInUserId == null
                || loggedInUserId.isBlank()) {
            return;
        }

        // Mark loading and prevent another refresh until this one finishes
        loadingDashboard = true;
        btnRefresh.setDisable(true);

        clearCounts();

        lblDashboardStatus.setTextFill(
                Color.web("#526174")
        );

        lblDashboardStatus.setText(
                "Loading dashboard..."
        );

        // Run the database query in a background task; return four totals
        Task<long[]> task = new Task<>() {
            @Override
            // This method runs off the UI thread and performs the database work.
            protected long[] call() throws SQLException {
                return fetchDashboardCounts();
            }
        };

        // Update labels when the background query succeeds.
        task.setOnSucceeded(event -> {
            // Read the four totals returned by the background task
            long[] counts = task.getValue();

            lblUserCount.setText(
                    Long.toString(counts[0])
            );

            lblCourseCount.setText(
                    Long.toString(counts[1])
            );

            lblNoticeCount.setText(
                    Long.toString(counts[2])
            );

            lblTimetableCount.setText(
                    Long.toString(counts[3])
            );

            lblDashboardStatus.setTextFill(
                    Color.web("#526174")
            );

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

        // Show a useful message and restore the button if the query fails
        task.setOnFailed(event -> {
            clearCounts();

            // Get the reason the background task failed
            Throwable error = task.getException();

            lblDashboardStatus.setTextFill(
                    Color.web("#C62828")
            );

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

        // A separate daemon thread keeps the dashboard responsive during the query.
        Thread thread = new Thread(
                task,
                "admin-dashboard-loader"
        );

        thread.setDaemon(true);
        thread.start();
    }

    // Count records in the four tables shown on the admin dashboard
    private long[] fetchDashboardCounts() throws SQLException {
        // One SQL statement returns all four counts in a single result row
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM `user`) AS user_count,
                    (SELECT COUNT(*) FROM `course`) AS course_count,
                    (SELECT COUNT(*) FROM `notice`) AS notice_count,
                    (SELECT COUNT(*) FROM `timetable`) AS timetable_count
                """;

        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                throw new SQLException(
                        "Database connection is unavailable."
                );
            }

            // Set a limit so a slow database cannot leave the dashboard waiting forever
            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setQueryTimeout(15);

                try (ResultSet result = statement.executeQuery()) {
                    // The count query should return one row; report an error if it does not
                    if (!result.next()) {
                        throw new SQLException(
                                "No dashboard data was returned."
                        );
                    }

                    // Return counts in the same order used by the four dashboard labels.
                    return new long[]{
                            result.getLong("user_count"),
                            result.getLong("course_count"),
                            result.getLong("notice_count"),
                            result.getLong("timetable_count")
                    };
                }
            }
        }
    }

    // Show placeholders while counts are loading or if loading fails
    private void clearCounts() {
        lblUserCount.setText("—");
        lblCourseCount.setText("—");
        lblNoticeCount.setText("—");
        lblTimetableCount.setText("—");
    }

    // Confirm logout, then replace the admin screen with the login screen
    @FXML
    private void handleLogout() {
        // Ask before ending the current admin session.
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        alert.setTitle("Logout");

        alert.setHeaderText(
                "Are you sure you want to log out?"
        );

        alert.setContentText(
                "You will be redirected to the login page."
        );

        if (alert.showAndWait()
                .orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            // Locate the login screen that should appear after logout
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

            // Clear the saved session ID before leaving the admin screen
            loggedInUserId = null;

            stage.setScene(
                    new Scene(root, 1000, 600)
            );

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

    // Reusable helper to show errors consistently throughout this controller
    private void showError(String title, String message) {
        Alert alert = new Alert(
                Alert.AlertType.ERROR
        );

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
