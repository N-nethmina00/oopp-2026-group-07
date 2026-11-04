package com.fot.ams.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;


public class LecturerController {

    @FXML private Label pageTitle;
    @FXML private Label pageDescription;
    @FXML private VBox contentArea;

    @FXML private Button dashboardBtn;
    @FXML private Button profileBtn;
    @FXML private Button coursesBtn;
    @FXML private Button materialsBtn;
    @FXML private Button marksBtn;
    @FXML private Button studentsBtn;
    @FXML private Button eligibilityBtn;
    @FXML private Button attendanceBtn;
    @FXML private Button medicalsBtn;
    @FXML private Button gradesBtn;
    @FXML private Button noticesBtn;

    private String lecturerId;

    // FXML initializes before LoginController passes the authenticated lecturer ID.
    @FXML
    public void initialize() {
        createContext().displayPage(
                "Lecturer Dashboard",
                "Loading lecturer information...",
                dashboardBtn
        );
    }

    public void setLecturerId(String lecturerId) {
        if (lecturerId == null || lecturerId.isBlank()) {
            throw new IllegalArgumentException("Valid Lecturer ID is required");
        }
        this.lecturerId = lecturerId;
        showDashboard();
    }

    private LecturerPageContext createContext() {
        return new LecturerPageContext(
                lecturerId, pageTitle, pageDescription, contentArea,
                dashboardBtn, profileBtn, coursesBtn, materialsBtn, marksBtn,
                studentsBtn, eligibilityBtn, attendanceBtn, medicalsBtn,
                gradesBtn, noticesBtn
        );
    }

    // Keep these @FXML method names: lecturer-dashboard.fxml calls them directly.

    @FXML
    private void showDashboard() {
        if (lecturerId == null || lecturerId.isBlank()) return;
        new LecturerDashboardPageController(createContext()).showDashboard();
    }

    @FXML
    private void showProfile() {
        if (lecturerId == null || lecturerId.isBlank()) return;
        new LecturerProfilePageController(createContext()).showProfile();
    }

    @FXML
    private void showCourses() {
        if (lecturerId == null || lecturerId.isBlank()) return;
        new LecturerCoursesPageController(createContext()).showCourses();
    }

    @FXML
    private void showMaterials() {
        if (lecturerId == null || lecturerId.isBlank()) return;
        new LecturerMaterialsPageController(createContext()).showMaterials();
    }

    @FXML
    private void showMarks() {
        if (lecturerId == null || lecturerId.isBlank()) return;
        new LecturerMarksPageController(createContext()).showMarks();
    }

    @FXML
    private void showStudents() {
        if (lecturerId == null || lecturerId.isBlank()) return;
        new LecturerStudentsPageController(createContext()).showStudents();
    }

    @FXML
    private void showEligibility() {
        if (lecturerId == null || lecturerId.isBlank()) return;
        new LecturerEligibilityPageController(createContext()).showEligibility();
    }

    @FXML
    private void showAttendance() {
        if (lecturerId == null || lecturerId.isBlank()) return;
        new LecturerAttendancePageController(createContext()).showAttendance();
    }

    @FXML
    private void showMedicals() {
        if (lecturerId == null || lecturerId.isBlank()) return;
        new LecturerMedicalPageController(createContext()).showMedicals();
    }

    @FXML
    private void showGrades() {
        if (lecturerId == null || lecturerId.isBlank()) return;
        new LecturerGradesPageController(createContext()).showGrades();
    }

    @FXML
    private void showNotices() {
        if (lecturerId == null || lecturerId.isBlank()) return;
        new LecturerNoticesPageController(createContext()).showNotices();
    }

    // Same logout implementation as the original LecturerController.
    @FXML
    private void logout() {

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to log out?",
                ButtonType.YES,
                ButtonType.NO
        );

        confirmation.setTitle("Confirm Logout");
        confirmation.setHeaderText("Log Out");

        Optional<ButtonType> result =
                confirmation.showAndWait();

        if (result.isEmpty() ||
                result.get() != ButtonType.YES) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage)
                    contentArea.getScene().getWindow();

            stage.setTitle("University Management System - Login");
            stage.setScene(new Scene(root, 1100, 680));
            stage.centerOnScreen();

            // Clear lecturer information
            lecturerId = null;

        } catch (IOException e) {
            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to return to Login Page."
            ).showAndWait();
        }
    }
}
