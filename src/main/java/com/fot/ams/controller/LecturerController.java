
package com.fot.ams.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class LecturerController {

    @FXML
    private Label pageTitle;

    @FXML
    private Label pageDescription;

    @FXML
    private VBox contentArea;

    @FXML
    public void initialize() {
        showDashboard();
    }

    private void displayPage(String title, String description) {
        pageTitle.setText(title);
        pageDescription.setText(description);
        contentArea.getChildren().clear();
    }

    @FXML
    private void showDashboard() {
        displayPage("Welcome,Lecturer",
                "Welcome to the Lecturer Dashboard.");
    }

    @FXML
    private void showProfile() {
        displayPage("My Profile",
                "Lecturer profile management.");
    }

    @FXML
    private void showCourses() {
        displayPage("My Courses",
                "View assigned courses.");
    }

    @FXML
    private void showMaterials() {
        displayPage("Course Materials",
                "Manage lecture notes and documents.");
    }

    @FXML
    private void showMarks() {
        displayPage("Student Marks",
                "Enter and manage examination marks.");
    }

    @FXML
    private void showStudents() {
        displayPage("Undergraduate Details",
                "View undergraduate academic information.");
    }

    @FXML
    private void showEligibility() {
        displayPage("Exam Eligibility",
                "View examination eligibility.");
    }

    @FXML
    private void showAttendance() {
        displayPage("Attendance",
                "View student attendance records.");
    }

    @FXML
    private void showMedicals() {
        displayPage("Medical Records",
                "View student medical information.");
    }

    @FXML
    private void showGrades() {
        displayPage("Grades & GPA",
                "View grades and GPA calculations.");
    }

    @FXML
    private void showNotices() {
        displayPage("Notices",
                "View faculty notices.");
    }

    @FXML
    private void logout() {
        displayPage("Logout",
                "Logout integration will be implemented later.");
    }
}
