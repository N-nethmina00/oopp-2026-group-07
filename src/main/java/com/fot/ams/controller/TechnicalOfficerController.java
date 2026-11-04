package com.fot.ams.controller;

import com.fot.ams.dao.TechnicalOfficerDashboardDAO;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

// TG2070 - Manage dashboard navigation, counts and logout.
public class TechnicalOfficerController {

    // @FXML connects these fields to controls in the FXML file.
    @FXML
    private BorderPane dashboardRoot;

    @FXML
    private Label studentCountLabel;

    @FXML
    private Label sessionCountLabel;

    @FXML
    private Label attendanceCountLabel;

    // Keep loaded pages so they can be used again without loading again.
    // A Node is a JavaFX interface element, such as a page layout.
    private Node dashboardContent;
    private Node attendanceContent;
    private Node medicalContent;
    private Node noticesContent;
    private Node timetableContent;
    private Node profileContent;

    // Store the logged-in Technical Officer ID.
    private String technicalOfficerId;

    // Keep page controllers so the officer ID can be passed to them.
    private AttendanceController attendanceController;

    private MedicalController medicalController;
    private TechnicalOfficerTimetableController timetableController;
    private TechnicalOfficerProfileController profileController;

    // Use the DAO to get dashboard data from the database.
    private final TechnicalOfficerDashboardDAO dashboardDAO =
            new TechnicalOfficerDashboardDAO();

    // Receive the logged-in officer ID from LoginController.
    public void setTechnicalOfficerId(String technicalOfficerId) {
        // "this" refers to the field in this controller.
        this.technicalOfficerId = technicalOfficerId;

        // Update the ID only if the page controller is already loaded.
        if (attendanceController != null) {
            attendanceController.setTechnicalOfficerId(technicalOfficerId);
        }

        if (medicalController != null) {
            medicalController.setTechnicalOfficerId(technicalOfficerId);
        }

        if (timetableController != null) {
            timetableController.setTechnicalOfficerId(technicalOfficerId);
        }

        if (profileController != null) {
            profileController.setTechnicalOfficerId(technicalOfficerId);
        }

        // Load counts after the officer ID is available.
        loadDashboardCounts();
    }

    // JavaFX calls this method after loading the FXML controls.
    @FXML
    private void initialize() {
        // Keep the original dashboard page from the centre area.
        dashboardContent = dashboardRoot.getCenter();
        clearDashboardCounts();
    }

    // Show the original dashboard and refresh its counts.
    @FXML
    private void showDashboard() {
        dashboardRoot.setCenter(dashboardContent);
        loadDashboardCounts();
    }

    // Get dashboard counts for the logged-in officer's department.
    private void loadDashboardCounts() {
        clearDashboardCounts();

        // Stop if the officer ID is missing or contains only spaces.
        if (technicalOfficerId == null
                || technicalOfficerId.isBlank()) {
            return;
        }

        try {
            int students = dashboardDAO.getStudentCount(
                    technicalOfficerId
            );

            int sessions = dashboardDAO.getWeeklySessionCount(
                    technicalOfficerId
            );

            int pendingAttendance =
                    dashboardDAO.getPendingAttendanceSessionCount(
                            technicalOfficerId
                    );

            // Convert numbers to text before displaying them in labels.
            studentCountLabel.setText(String.valueOf(students));
            sessionCountLabel.setText(String.valueOf(sessions));
            attendanceCountLabel.setText(
                    String.valueOf(pendingAttendance)
            );

        } catch (SQLException exception) {
            // Print error details in the console and show an alert.
            exception.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Dashboard counts could not be loaded.");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }
    }

    // Show a dash until actual counts are loaded.
    private void clearDashboardCounts() {
        studentCountLabel.setText("—");
        sessionCountLabel.setText("—");
        attendanceCountLabel.setText("—");
    }

    // Open the attendance page.
    @FXML
    private void showAttendance() {
        try {
            // Load the page only when it is opened for the first time.
            if (attendanceContent == null) {
                URL resource = getClass().getResource(
                        "/fxml/attendance.fxml"
                );

                // Stop and report an error if the FXML file is missing.
                if (resource == null) {
                    throw new IOException(
                            "Cannot find /fxml/attendance.fxml"
                    );
                }

                // Create the interface from the FXML file.
                FXMLLoader loader = new FXMLLoader(resource);
                Node page = loader.load();

                // Get the controller linked to the loaded FXML file.
                AttendanceController controller =
                        loader.getController();

                if (controller == null) {
                    throw new IOException(
                            "AttendanceController is not linked "
                                    + "in attendance.fxml"
                    );
                }

                // Give the attendance page the logged-in officer ID.
                controller.setTechnicalOfficerId(technicalOfficerId);

                // Keep the controller and page for later use.
                attendanceController = controller;
                attendanceContent = page;
            }

            // Replace only the centre area with the attendance page.
            dashboardRoot.setCenter(attendanceContent);

        } catch (IOException exception) {
            showPageError("Attendance", exception);
        }
    }

    // Open the medical page and pass the officer ID.
    @FXML
    private void showMedical() {
        try {
            if (medicalContent == null) {
                URL resource = getClass().getResource(
                        "/fxml/medical.fxml"
                );

                if (resource == null) {
                    throw new IOException(
                            "Cannot find /fxml/medical.fxml"
                    );
                }

                FXMLLoader loader = new FXMLLoader(resource);
                Node page = loader.load();

                MedicalController controller = loader.getController();

                if (controller == null) {
                    throw new IOException(
                            "MedicalController is not linked in medical.fxml"
                    );
                }

                controller.setTechnicalOfficerId(technicalOfficerId);

                medicalController = controller;
                medicalContent = page;
            }

            dashboardRoot.setCenter(medicalContent);

        } catch (IOException exception) {
            showPageError("Medical", exception);
        }
    }

    // Open the notices page using the shared page-loading method.
    @FXML
    private void showNotices() {
        try {
            if (noticesContent == null) {
                noticesContent = loadPage(
                        "/fxml/technical-officer-notices.fxml"
                );
            }

            dashboardRoot.setCenter(noticesContent);

        } catch (IOException exception) {
            showPageError("Notices", exception);
        }
    }

    // Open the timetable page and pass the officer ID.
    @FXML
    private void showTimetable() {
        try {
            if (timetableContent == null) {
                URL resource = getClass().getResource(
                        "/fxml/technical-officer-timetable.fxml"
                );

                if (resource == null) {
                    throw new IOException(
                            "Cannot find /fxml/technical-officer-timetable.fxml"
                    );
                }

                FXMLLoader loader = new FXMLLoader(resource);
                Node page = loader.load();

                TechnicalOfficerTimetableController controller =
                        loader.getController();

                if (controller == null) {
                    throw new IOException(
                            "TechnicalOfficerTimetableController is not "
                                    + "linked in technical-officer-timetable.fxml"
                    );
                }

                controller.setTechnicalOfficerId(technicalOfficerId);

                timetableController = controller;
                timetableContent = page;
            }

            dashboardRoot.setCenter(timetableContent);

        } catch (IOException exception) {
            showPageError("Timetable", exception);
        }
    }

    // Open the profile page for the logged-in officer.
    @FXML
    private void showProfile() {
        try {
            if (profileContent == null) {
                URL resource = getClass().getResource(
                        "/fxml/technical-officer-profile.fxml"
                );

                if (resource == null) {
                    throw new IOException(
                            "Cannot find /fxml/technical-officer-profile.fxml"
                    );
                }

                FXMLLoader loader = new FXMLLoader(resource);
                Node page = loader.load();

                TechnicalOfficerProfileController controller =
                        loader.getController();

                if (controller == null) {
                    throw new IOException(
                            "TechnicalOfficerProfileController is not "
                                    + "linked in technical-officer-profile.fxml"
                    );
                }

                controller.setTechnicalOfficerId(technicalOfficerId);

                profileController = controller;
                profileContent = page;
            }

            dashboardRoot.setCenter(profileContent);

        } catch (IOException exception) {
            showPageError("Profile", exception);
        }
    }

    // Find an FXML file and return its loaded page.
    // "throws" lets the calling method handle loading errors.
    private Node loadPage(String path) throws IOException {
        URL resource = getClass().getResource(path);

        if (resource == null) {
            throw new IOException("Cannot find " + path);
        }

        return FXMLLoader.load(resource);
    }

    // Show a common error alert when a page cannot be loaded.
    private void showPageError(String page, IOException exception) {
        exception.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Page Loading Error");
        alert.setHeaderText(page + " page could not be opened.");
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }

    // Return to the login page and clear this controller's user data.
    @FXML
    private void handleLogout() {
        try {
            URL resource = getClass().getResource(
                    "/fxml/login.fxml"
            );

            if (resource == null) {
                throw new IOException(
                        "Cannot find /fxml/login.fxml"
                );
            }

            FXMLLoader loader = new FXMLLoader(resource);
            javafx.scene.Parent loginPage = loader.load();

            // Create a scene containing the login page.
            javafx.scene.Scene loginScene =
                    new javafx.scene.Scene(loginPage, 1200, 750);

            // Get the current application window.
            // The cast treats the returned Window as a Stage.
            javafx.stage.Stage stage =
                    (javafx.stage.Stage) dashboardRoot
                            .getScene().getWindow();

            // Show the login scene in the same window.
            stage.setTitle("Academic Management System");
            stage.setScene(loginScene);
            stage.centerOnScreen();

            technicalOfficerId = null;

            // Remove references to the previously loaded pages.
            attendanceContent = null;
            medicalContent = null;
            noticesContent = null;
            timetableContent = null;
            profileContent = null;

            // Remove references to the previous page controllers.
            attendanceController = null;
            medicalController = null;
            timetableController = null;
            profileController = null;

            clearDashboardCounts();

        } catch (IOException exception) {
            showPageError("Login", exception);
        }
    }
}