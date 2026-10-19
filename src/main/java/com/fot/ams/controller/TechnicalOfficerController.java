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


    @FXML
    private void initialize() {
        dashboardContent = dashboardRoot.getCenter();
    }

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

                attendanceContent = FXMLLoader.load(resource);
            }

            dashboardRoot.setCenter(attendanceContent);

        } catch (IOException exception) {
            exception.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Page Loading Error");
            alert.setHeaderText("Attendance page could not be opened.");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void showDashboard() {
        dashboardRoot.setCenter(dashboardContent);
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

                medicalContent = FXMLLoader.load(resource);
            }

            dashboardRoot.setCenter(medicalContent);

        } catch (IOException exception) {
            exception.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Page Loading Error");
            alert.setHeaderText("Medical page could not be opened.");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void showNotices() {
        try {
            if (noticesContent == null) {
                URL resource = getClass().getResource(
                        "/fxml/technical-officer-notices.fxml"
                );

                if (resource == null) {
                    throw new IOException(
                            "Cannot find /fxml/technical-officer-notices.fxml"
                    );
                }

                noticesContent = FXMLLoader.load(resource);
            }

            dashboardRoot.setCenter(noticesContent);

        } catch (IOException exception) {
            exception.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Page Loading Error");
            alert.setHeaderText("Notices page could not be opened.");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }
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

                    timetableContent = FXMLLoader.load(resource);
                }

                dashboardRoot.setCenter(timetableContent);

            } catch (IOException exception) {
                exception.printStackTrace();

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Page Loading Error");
                alert.setHeaderText("Timetable page could not be opened.");
                alert.setContentText(exception.getMessage());
                alert.showAndWait();
            }
        }
    }
}