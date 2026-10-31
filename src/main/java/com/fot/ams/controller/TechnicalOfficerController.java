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

public class TechnicalOfficerController {

    @FXML
    private BorderPane dashboardRoot;

    @FXML
    private Label studentCountLabel;

    @FXML
    private Label sessionCountLabel;

    @FXML
    private Label attendanceCountLabel;

    private Node dashboardContent;
    private Node attendanceContent;
    private Node medicalContent;
    private Node noticesContent;
    private Node timetableContent;
    private Node profileContent;

    // TG2070 - Logged-in Technical Officer ID
    private String technicalOfficerId;

    // TG2070 - Keep attendance controller to pass TO ID
    private AttendanceController attendanceController;

    private MedicalController medicalController;
    private TechnicalOfficerTimetableController timetableController;
    private TechnicalOfficerProfileController profileController;

    private final TechnicalOfficerDashboardDAO dashboardDAO =
            new TechnicalOfficerDashboardDAO();

    // TG2070 - Receive TO ID from LoginController
    public void setTechnicalOfficerId(String technicalOfficerId) {
        this.technicalOfficerId = technicalOfficerId;

        // TG2070 - Pass TO ID to attendance controller
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

        loadDashboardCounts();
    }

    @FXML
    private void initialize() {
        dashboardContent = dashboardRoot.getCenter();
        clearDashboardCounts();
    }

    @FXML
    private void showDashboard() {
        dashboardRoot.setCenter(dashboardContent);
        loadDashboardCounts();
    }

    // TG2070 - Load department counts from database
    private void loadDashboardCounts() {
        clearDashboardCounts();

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

            studentCountLabel.setText(String.valueOf(students));
            sessionCountLabel.setText(String.valueOf(sessions));
            attendanceCountLabel.setText(
                    String.valueOf(pendingAttendance)
            );

        } catch (SQLException exception) {
            exception.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Dashboard counts could not be loaded.");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }
    }

    private void clearDashboardCounts() {
        studentCountLabel.setText("—");
        sessionCountLabel.setText("—");
        attendanceCountLabel.setText("—");
    }

    // TG2070 - Load attendance page and pass logged-in TO ID
    @FXML
    private void showAttendance() {
        try {
            if (attendanceContent == null) {
                URL resource = getClass().getResource(
                        "/fxml/attendance.fxml"
                );

                if (resource == null) {
                    throw new IOException(
                            "Cannot find /fxml/attendance.fxml"
                    );
                }

                FXMLLoader loader = new FXMLLoader(resource);
                Node page = loader.load();

                AttendanceController controller =
                        loader.getController();

                if (controller == null) {
                    throw new IOException(
                            "AttendanceController is not linked "
                                    + "in attendance.fxml"
                    );
                }

                controller.setTechnicalOfficerId(technicalOfficerId);

                attendanceController = controller;
                attendanceContent = page;
            }

            dashboardRoot.setCenter(attendanceContent);

        } catch (IOException exception) {
            showPageError("Attendance", exception);
        }
    }

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

    // TG2070 - Load department timetable
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

    // TG2070 - Load profile and pass logged-in TO ID
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

    private Node loadPage(String path) throws IOException {
        URL resource = getClass().getResource(path);

        if (resource == null) {
            throw new IOException("Cannot find " + path);
        }

        return FXMLLoader.load(resource);
    }

    private void showPageError(String page, IOException exception) {
        exception.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Page Loading Error");
        alert.setHeaderText(page + " page could not be opened.");
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }

    // TG2070 - Return to login page when logging out
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

            javafx.scene.Scene loginScene =
                    new javafx.scene.Scene(loginPage, 1200, 750);

            javafx.stage.Stage stage =
                    (javafx.stage.Stage) dashboardRoot
                            .getScene().getWindow();

            stage.setTitle("Academic Management System");
            stage.setScene(loginScene);
            stage.centerOnScreen();

            technicalOfficerId = null;

            attendanceContent = null;
            medicalContent = null;
            noticesContent = null;
            timetableContent = null;
            profileContent = null;

            // TG2070 - Clear cached page controllers
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