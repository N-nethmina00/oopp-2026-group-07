package com.fot.ams.controller;

import com.fot.ams.dao.AttendanceDAO;
import com.fot.ams.dao.CourseDAO;
import com.fot.ams.model.Batch;
import com.fot.ams.model.Course;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// TG2070 - Attendance page controller
public class AttendanceController {

    @FXML
    private ComboBox<Batch> batchComboBox;

    @FXML
    private ComboBox<Course> courseComboBox;

    @FXML
    private void initialize() {
        loadBatches();
        loadCourses();
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