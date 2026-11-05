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

//UI content for the lecturer Eligibility page

public final class LecturerEligibilityPageController extends LecturerPageBase {

    public LecturerEligibilityPageController(LecturerPageContext context) {
        super(context);
    }

    public void showEligibility() {

        displayPage(
                "Exam Eligibility",
                "View attendance and CA eligibility records.",
                eligibilityBtn
        );

        Label heading = new Label("Student Exam Eligibility");

        heading.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        Label courseLabel = new Label("Select Course Module");

        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.setPromptText("Select Your Course");
        courseComboBox.setPrefWidth(300);

        // Exam Eligibility Table using java generics
        TableView<EligibilityRecord> eligibilityTable =
                new TableView<>();

        eligibilityTable.setPlaceholder(
                new Label("Select a course to view eligibility.")
        );

        eligibilityTable.setPrefHeight(420);

        eligibilityTable.setColumnResizePolicy(
                TableView.UNCONSTRAINED_RESIZE_POLICY
        );

        eligibilityTable.getColumns().forEach(column ->
                column.setPrefWidth(145)
        );

        // Registration Number
        TableColumn<EligibilityRecord, String> regCol =
                new TableColumn<>("Registration No");

        regCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getRegistrationNo()
                )
        );

        // Course Code
        TableColumn<EligibilityRecord, String> courseCol =
                new TableColumn<>("Course Code");

        courseCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getCourseCode()
                )
        );

        // Recorded Attendance Percentage
        TableColumn<EligibilityRecord, String> attendanceCol =
                new TableColumn<>("Present % (Recorded)");

        attendanceCol.setCellValueFactory(data -> {

            Double percentage =
                    data.getValue().getAttendancePercentage();

            String value = percentage == null
                    ? "Not Recorded"
                    : String.format(
                    java.util.Locale.US,
                    "%.2f%%",
                    percentage
            );

            return new ReadOnlyStringWrapper(value);
        });


        // Recorded sessions compared with scheduled sessions
        TableColumn<EligibilityRecord, String> sessionsCol =
                new TableColumn<>("Recorded / Sessions");

        sessionsCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getRecordedSessions()
                                + " / " +
                                data.getValue().getScheduledSessions()
                )
        );

        // Theory attendance percentage
        TableColumn<EligibilityRecord, String> theoryCol =
                new TableColumn<>("Theory Present %");

        theoryCol.setCellValueFactory(data -> {
            Double value = data.getValue().getTheoryPercentage();

            return new ReadOnlyStringWrapper(
                    value == null
                            ? "Not Recorded"
                            : String.format(
                            java.util.Locale.US, "%.2f%%", value
                    )
            );
        });

        // Practical attendance percentage
        TableColumn<EligibilityRecord, String> practicalCol =
                new TableColumn<>("Practical Present %");

        practicalCol.setCellValueFactory(data -> {
            Double value = data.getValue().getPracticalPercentage();

            return new ReadOnlyStringWrapper(
                    value == null
                            ? "Not Recorded"
                            : String.format(
                            java.util.Locale.US, "%.2f%%", value
                    )
            );
        });

        // Approved medical count
        TableColumn<EligibilityRecord, String> approvedCol =
                new TableColumn<>("Approved Medical");

        approvedCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        String.valueOf(
                                data.getValue().getApprovedMedicalCount()
                        )
                )
        );

        // Pending medical count
        TableColumn<EligibilityRecord, String> pendingCol =
                new TableColumn<>("Pending Medical");

        pendingCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        String.valueOf(
                                data.getValue().getPendingMedicalCount()
                        )
                )
        );


        // CA Status
        TableColumn<EligibilityRecord, String> caStatusCol =
                new TableColumn<>("CA Status");

        caStatusCol.setCellValueFactory(data -> {

            String status = data.getValue().getCaStatus();

            return new ReadOnlyStringWrapper(
                    status == null ? "Not Available" : status
            );
        });

        // CA Marks
        TableColumn<EligibilityRecord, String> caMarksCol =
                new TableColumn<>("Stored CA Marks");

        caMarksCol.setCellValueFactory(data -> {

            Double marks = data.getValue().getCaMarks();

            return new ReadOnlyStringWrapper(
                    marks == null
                            ? "Not Available"
                            : String.format(
                            java.util.Locale.US,
                            "%.2f",
                            marks
                    )
            );
        });

        // Database Eligibility Flag
        TableColumn<EligibilityRecord, String> eligibilityCol =
                new TableColumn<>("Eligibility DB Flag");

        eligibilityCol.setCellValueFactory(data -> {

            Integer flag =
                    data.getValue().getEligibilityFlag();

            String value = flag == null
                    ? "Not Available"
                    : String.valueOf(flag);

            return new ReadOnlyStringWrapper(value);
        });

        eligibilityTable.getColumns().addAll(
                regCol,
                courseCol,
                attendanceCol,
                sessionsCol,
                theoryCol,
                practicalCol,
                caStatusCol,
                caMarksCol,
                approvedCol,
                pendingCol,
                eligibilityCol
        );

        // Load Lecturer's Assigned Courses
        try {

            CourseDAO courseDAO = new CourseDAO();

            List<Course> courses =
                    courseDAO.getCourseModelsByLecturer(lecturerId);

            for (Course course : courses) {
                courseComboBox.getItems().add(course.getCourseCode());
            }

        } catch (SQLException e) {

            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to load assigned courses."
            ).showAndWait();
        }

        // Load Eligibility Records
        courseComboBox.setOnAction(event -> {

            String selectedCourse = courseComboBox.getValue();

            eligibilityTable.getItems().clear();

            if (selectedCourse == null) {
                return;
            }

            try {

                EligibilityDAO eligibilityDAO =
                        new EligibilityDAO();

                List<EligibilityRecord> records =
                        eligibilityDAO.getEligibilityByLecturerCourse(
                                lecturerId,
                                selectedCourse
                        );

                eligibilityTable.getItems().setAll(records);

            } catch (SQLException e) {

                e.printStackTrace();

                new Alert(
                        Alert.AlertType.ERROR,
                        "Unable to load exam eligibility records."
                ).showAndWait();
            }
        });


        contentArea.getChildren().addAll(
                heading,
                courseLabel,
                courseComboBox,
                eligibilityTable
        );
    }
}
