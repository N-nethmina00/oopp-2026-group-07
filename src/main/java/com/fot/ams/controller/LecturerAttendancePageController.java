package com.fot.ams.controller;

import com.fot.ams.dao.*;
import com.fot.ams.model.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Desktop;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.*;

//UI content for the lecturer Attendance page

public final class LecturerAttendancePageController extends LecturerPageBase {

    public LecturerAttendancePageController(LecturerPageContext context) {
        super(context);
    }

    public void showAttendance() {
        displayPage(
                "Attendance",
                "View recorded attendance for students in your assigned courses.",
                attendanceBtn
        );

        Label heading = new Label("Student Attendance Records");

        heading.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        // Course selection
        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.setPromptText("Select Your Course");
        courseComboBox.setPrefWidth(300);

        // Attendance table
        TableView<String[]> attendanceTable = createTable(
                "Select a course to view recorded attendance.",
                "Registration No",
                "Session Date",
                "Session Type",
                "Time",
                "Status"
        );

        attendanceTable.setPrefHeight(450);


        // Load attendance when course is selected
        courseComboBox.setOnAction(event -> {

            String courseCode = courseComboBox.getValue();

            attendanceTable.getItems().clear();

            if (courseCode == null) {
                return;
            }

            try {
                LecturerRecordsDAO dao = new LecturerRecordsDAO();

                List<String[]> records =
                        dao.getAttendanceByLecturerCourse(
                                lecturerId,
                                courseCode
                        );

                attendanceTable.getItems().setAll(records);

            } catch (SQLException e) {
                e.printStackTrace();

                new Alert(
                        Alert.AlertType.ERROR,
                        "Unable to load attendance records: "
                                + e.getMessage()
                ).showAndWait();
            }
        });

        // Display page
        contentArea.getChildren().addAll(
                heading,
                new Label("Select Course Module"),
                courseComboBox,
                attendanceTable
        );

        // Load lecturer's assigned courses
        try {
            CourseDAO courseDAO = new CourseDAO();

            List<Course> courses =
                    courseDAO.getCourseModelsByLecturer(lecturerId);

            for (Course course : courses) {
                courseComboBox.getItems().add(
                        course.getCourseCode()
                );
            }

            if (!courseComboBox.getItems().isEmpty()) {
                courseComboBox.getSelectionModel().selectFirst();
            }

        } catch (SQLException e) {
            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to load assigned courses."
            ).showAndWait();
        }
    }
}
