package com.fot.ams.controller;

import com.fot.ams.dao.AttendanceDAO;
import com.fot.ams.dao.CourseDAO;
import com.fot.ams.model.AttendanceSession;
import com.fot.ams.model.Batch;
import com.fot.ams.model.Course;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// TG2070 - Attendance page controller
public class AttendanceController {

    @FXML
    private ComboBox<Batch> batchComboBox;

    @FXML
    private ComboBox<Course> courseComboBox;

    // TG2070 - Session filter controls
    @FXML
    private DatePicker sessionDatePicker;

    @FXML
    private ComboBox<String> sessionTypeComboBox;

    @FXML
    private ComboBox<AttendanceSession> sessionComboBox;

    @FXML
    private void initialize() {
        loadBatches();
        loadCourses();

        // TG2070 - Match database session type values
        sessionTypeComboBox.getItems().add("Theory");
        sessionTypeComboBox.getItems().add("Practical");

        sessionComboBox.setDisable(true);

        // TG2070 - Reload sessions when a filter changes
        courseComboBox.setOnAction(event -> loadSessions());
        sessionDatePicker.setOnAction(event -> loadSessions());
        sessionTypeComboBox.setOnAction(event -> loadSessions());
    }

    // TG2070 - START: Load batch dropdown
    private void loadBatches() {

        AttendanceDAO attendanceDAO = new AttendanceDAO();

        try {
            ArrayList<Batch> batches = attendanceDAO.getAllBatches();

            batchComboBox.getItems().clear();

            for (Batch batch : batches) {
                batchComboBox.getItems().add(batch);
            }

        } catch (SQLException exception) {
            showDatabaseError("Could not load batches.", exception);
        }
    }
    // TG2070 - END

    // TG2070 - START: Load course dropdown
    private void loadCourses() {

        CourseDAO courseDAO = new CourseDAO();

        try {
            List<Course> courses = courseDAO.getAllCourses();

            courseComboBox.getItems().clear();

            for (Course course : courses) {
                courseComboBox.getItems().add(course);
            }

        } catch (SQLException exception) {
            showDatabaseError("Could not load courses.", exception);
        }
    }
    // TG2070 - END

    // TG2070 - START: Load sessions using selected filters
    private void loadSessions() {

        // Clear the previous session selection and list
        sessionComboBox.getSelectionModel().clearSelection();
        sessionComboBox.getItems().clear();
        sessionComboBox.setDisable(true);

        Course selectedCourse = courseComboBox.getValue();
        LocalDate selectedDate = sessionDatePicker.getValue();
        String selectedType = sessionTypeComboBox.getValue();

        // All three filters are required
        if (selectedCourse == null
                || selectedDate == null
                || selectedType == null) {

            sessionComboBox.setPromptText("Select course, date and type");
            return;
        }

        AttendanceDAO attendanceDAO = new AttendanceDAO();

        try {
            ArrayList<AttendanceSession> sessions =
                    attendanceDAO.getSessions(
                            selectedCourse.getCourseId(),
                            selectedDate,
                            selectedType
                    );

            for (AttendanceSession session : sessions) {
                sessionComboBox.getItems().add(session);
            }

            if (sessions.isEmpty()) {
                sessionComboBox.setPromptText("No sessions found");
            } else {
                sessionComboBox.setPromptText("Select session");
                sessionComboBox.setDisable(false);
            }

        } catch (SQLException exception) {
            sessionComboBox.setPromptText("Could not load sessions");
            showDatabaseError("Could not load sessions.", exception);
        }
    }
    // TG2070 - END

    // TG2070 - Show database errors
    private void showDatabaseError(String message,
                                   SQLException exception) {

        exception.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(message);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }
}