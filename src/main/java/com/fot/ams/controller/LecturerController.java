
package com.fot.ams.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import com.fot.ams.dao.CourseDAO;
import java.sql.SQLException;
import javafx.scene.control.Alert;
import java.util.List;

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

    // Sidebar button styles
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
    // Temporary lecturer ID for testing only
    private String lecturerId = "L001";

    private String loadMyCoursesCount() {

        CourseDAO courseDAO = new CourseDAO();

        try {
            int count =
                    courseDAO.countCoursesByLecturer(lecturerId);

            return String.valueOf(count);

        } catch (SQLException e) {
            e.printStackTrace();
            return "—";
        }
    }

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
                button.setStyle(
                        button == activeButton
                                ? ACTIVE_STYLE
                                : NORMAL_STYLE
                );
            }
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

    // Create a dashboard statistic card
    private VBox createStatCard(
            String title,
            String value,
            String subtitle) {

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-text-fill: #526174;" +
                        "-fx-font-size: 13px;"
        );

        Label valueLabel = new Label(value);
        valueLabel.setStyle(
                "-fx-text-fill: #172B45;" +
                        "-fx-font-size: 27px;" +
                        "-fx-font-weight: bold;"
        );

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle(
                "-fx-text-fill: #7B8797;" +
                        "-fx-font-size: 11px;"
        );

        VBox card = new VBox(
                13,
                titleLabel,
                valueLabel,
                subtitleLabel
        );

        card.setPadding(new Insets(18));
        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPrefHeight(145);

        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #D5DCE5;" +
                        "-fx-border-radius: 8;"
        );

        HBox.setHgrow(card, Priority.ALWAYS);

        return card;
    }

    // Create a table with column headings
    private TableView<String[]> createTable(
            String placeholder,
            String... headings) {

        TableView<String[]> table = new TableView<>();

        table.setPlaceholder(new Label(placeholder));
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        for (int index = 0;
             index < headings.length; index++) {

            final int columnIndex = index;

            TableColumn<String[], String> column =
                    new TableColumn<>(headings[index]);

            column.setCellValueFactory(cell -> {
                String[] row = cell.getValue();

                String value =
                        row != null &&
                                columnIndex < row.length
                                ? row[columnIndex]
                                : "";

                return new ReadOnlyStringWrapper(value);
            });

            table.getColumns().add(column);
        }

        table.setPrefHeight(210);
        table.setMinHeight(170);

        return table;
    }

    // Dashboard overview
    @FXML
    private void showDashboard() {

        displayPage(
                "Dashboard Overview",
                "Overview of your teaching activities.",
                dashboardBtn
        );

        // Statistic cards
        HBox statsRow = new HBox(15);

        statsRow.getChildren().addAll(
                createStatCard(
                        "My Courses",
                        loadMyCoursesCount(),
                        "Assigned courses"
                ),
                createStatCard(
                        "Students", "—", "Enrolled"
                ),
                createStatCard(
                        "Pending Marks", "—", "To complete"
                ),
                createStatCard(
                        "Materials", "—", "Uploaded"
                )
        );

        // Teaching schedule
        Label scheduleTitle =
                new Label("Teaching Schedule");

        scheduleTitle.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        TableView<String[]> scheduleTable =
                createTable(
                        "No teaching schedule loaded",
                        "Day", "Time", "Course", "Location"
                );

        // Students overview
        Label studentsTitle =
                new Label("Students Overview");

        studentsTitle.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        TableView<String[]> studentsTable =
                createTable(
                        "No student records loaded",
                        "ID", "Name", "Course",
                        "Status", "Action"
                );

        // Add dashboard elements
        contentArea.setSpacing(22);

        contentArea.getChildren().addAll(
                statsRow,
                scheduleTitle,
                scheduleTable,
                studentsTitle,
                studentsTable
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
                "Courses assigned to Lecturer " + lecturerId,
                coursesBtn
        );

        Label heading = new Label("My Assigned Courses");

        heading.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        TableView<String[]> coursesTable = createTable(
                "No courses assigned to this lecturer.",
                "Course Code",
                "Course Name",
                "Credits",
                "Type"
        );

        coursesTable.setPrefHeight(350);

        CourseDAO courseDAO = new CourseDAO();

        try {
            List<String[]> courses =
                    courseDAO.getCoursesByLecturer(lecturerId);

            coursesTable.getItems().setAll(courses);

        } catch (SQLException e) {
            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to load courses from database."
            ).showAndWait();
        }

        contentArea.getChildren().addAll(
                heading,
                coursesTable
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
        displayPage(
                "Logout",
                "Logout integration will be implemented later.",
                null
        );
    }
}
