
package com.fot.ams.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

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
    @FXML private Button logoutBtn;

    private static final String ACTIVE_STYLE =
            "-fx-background-color: white;" +
                    "-fx-text-fill: #0078b9;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 8;";

    private static final String NORMAL_STYLE =
            "-fx-background-color: transparent;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: normal;" +
                    "-fx-background-radius: 8;";

    @FXML
    public void initialize() {
        showDashboard();
    }

    private void setActiveButton(Button activeButton) {

        Button[] buttons = {
                dashboardBtn, profileBtn, coursesBtn,
                materialsBtn, marksBtn, studentsBtn,
                eligibilityBtn, attendanceBtn, medicalsBtn,
                gradesBtn, noticesBtn
        };

        for (Button button : buttons) {
            if (button != null) {
                button.setStyle(NORMAL_STYLE);
            }
        }

        if (activeButton != null) {
            activeButton.setStyle(ACTIVE_STYLE);
        }
    }

    private void displayPage(
            String title,
            String description,
            Button activeButton) {

        pageTitle.setText(title);
        pageDescription.setText(description);
        contentArea.getChildren().clear();

        setActiveButton(activeButton);
    }

    @FXML
    private void showDashboard() {
        displayPage(
                "Lecturer Dashboard",
                "Welcome to the Lecturer Dashboard.",
                dashboardBtn
        );
    }

    @FXML
    private void showProfile() {
        displayPage(
                "My Profile",
                "Lecturer profile management.",
                profileBtn
        );
    }

    @FXML
    private void showCourses() {
        displayPage(
                "My Courses",
                "View assigned courses.",
                coursesBtn
        );
    }

    @FXML
    private void showMaterials() {
        displayPage(
                "Course Materials",
                "Manage lecture notes and documents.",
                materialsBtn
        );
    }

    @FXML
    private void showMarks() {
        displayPage(
                "Student Marks",
                "Enter and manage examination marks.",
                marksBtn
        );
    }

    @FXML
    private void showStudents() {
        displayPage(
                "Undergraduate Details",
                "View undergraduate academic information.",
                studentsBtn
        );
    }

    @FXML
    private void showEligibility() {
        displayPage(
                "Exam Eligibility",
                "View examination eligibility.",
                eligibilityBtn
        );
    }

    @FXML
    private void showAttendance() {
        displayPage(
                "Attendance",
                "View student attendance records.",
                attendanceBtn
        );
    }

    @FXML
    private void showMedicals() {
        displayPage(
                "Medical Records",
                "View student medical information.",
                medicalsBtn
        );
    }

    @FXML
    private void showGrades() {
        displayPage(
                "Grades & GPA",
                "View grades and GPA calculations.",
                gradesBtn
        );
    }

    @FXML
    private void showNotices() {
        displayPage(
                "Notices",
                "View faculty notices.",
                noticesBtn
        );
    }

    @FXML
    private void logout() {
        pageTitle.setText("Logout");
        pageDescription.setText(
                "Logout integration will be implemented later."
        );

        contentArea.getChildren().clear();
        setActiveButton(null);
    }
}
