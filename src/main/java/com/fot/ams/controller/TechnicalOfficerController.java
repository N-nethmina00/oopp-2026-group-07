package com.fot.ams.controller;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;

public class TechnicalOfficerController {

    @FXML
    private BorderPane dashboardRoot;

    private Node dashboardContent;
    private Node attendanceContent;
    private Node medicalContent;
    private Node noticesContent;
    private Node timetableContent;
    private Node profileContent;

    // TG2070 - Logged-in Technical Officer details
    private String technicalOfficerId;
    private MedicalController medicalController;

    // TG2070 - Receive TO ID from LoginController
    public void setTechnicalOfficerId(String technicalOfficerId) {
        this.technicalOfficerId = technicalOfficerId;

        if (medicalController != null) {
            medicalController.setTechnicalOfficerId(technicalOfficerId);
        }
    }

    @FXML
    private void initialize() {
        dashboardContent = dashboardRoot.getCenter();
    }

    @FXML
    private void showDashboard() {
        dashboardRoot.setCenter(dashboardContent);
    }

    @FXML
    private void showAttendance() {
        try {
            if (attendanceContent == null) {
                attendanceContent = loadPage(
                        "/fxml/attendance.fxml"
                );
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

                // TG2070 - Load medical page and obtain its controller
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

                // TG2070 - Pass logged-in TO ID to medical page
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

    @FXML
    private void showTimetable() {
        try {
            if (timetableContent == null) {
                timetableContent = loadPage(
                        "/fxml/technical-officer-timetable.fxml"
                );
            }

            dashboardRoot.setCenter(timetableContent);

        } catch (IOException exception) {
            showPageError("Timetable", exception);
        }
    }

    @FXML
    private void showProfile() {
        try {
            if (profileContent == null) {
                profileContent = loadPage(
                        "/fxml/technical-officer-profile.fxml"
                );
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
}