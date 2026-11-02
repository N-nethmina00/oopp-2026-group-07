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

//UI content for the lecturer Medical page
public final class LecturerMedicalPageController extends LecturerPageBase {

    public LecturerMedicalPageController(LecturerPageContext context) {
        super(context);
    }

    public void showMedicals() {

        displayPage(
                "Medical Records",
                "View medical records for students in your assigned courses.",
                medicalsBtn
        );

        Label heading = new Label("Student Medical Records");

        heading.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        // Course selection
        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.setPromptText("Select Your Course");
        courseComboBox.setPrefWidth(300);

        // Medical status filter
        ComboBox<String> statusFilter = new ComboBox<>();

        statusFilter.getItems().addAll(
                "All Statuses",
                "Pending",
                "Approved",
                "Rejected"
        );

        statusFilter.setValue("All Statuses");
        statusFilter.setPrefWidth(180);

        HBox filters = new HBox(
                12,
                new Label("Course:"),
                courseComboBox,
                new Label("Status:"),
                statusFilter
        );

        filters.setAlignment(
                javafx.geometry.Pos.CENTER_LEFT
        );

        // Medical Records Table
        TableView<String[]> medicalTable = createTable(
                "Select a course to view student medical records.",
                "Medical ID",
                "Registration No",
                "Date",
                "Reason",
                "Status"
        );

        medicalTable.setPrefHeight(450);

        // Hide internal Medical ID
        medicalTable.getColumns().get(0).setVisible(false);

        // Load medical records and apply filters
        Runnable reload = () -> {

            medicalTable.getItems().clear();

            String courseCode = courseComboBox.getValue();

            if (courseCode == null) {
                return;
            }

            try {
                LecturerRecordsDAO dao =
                        new LecturerRecordsDAO();

                List<String[]> rows = new ArrayList<>();

                String selectedStatus = statusFilter.getValue();

                List<MedicalRecord> medicalRecords =
                        dao.getMedicalsByLecturerCourse(
                                lecturerId,
                                courseCode
                        );

                for (MedicalRecord record : medicalRecords) {

                    if (!"All Statuses".equals(selectedStatus)
                            && !Objects.equals(
                            selectedStatus,
                            record.getStatus()
                    )) {
                        continue;
                    }

                    rows.add(new String[]{
                            String.valueOf(record.getMedicalId()),
                            record.getRegNo(),
                            record.getMedicalDate() == null
                                    ? ""
                                    : record.getMedicalDate().toString(),
                            record.getReason(),
                            record.getStatus()
                    });
                }

                medicalTable.getItems().setAll(rows);

            } catch (SQLException e) {

                e.printStackTrace();

                new Alert(
                        Alert.AlertType.ERROR,
                        "Unable to load medical records: "
                                + e.getMessage()
                ).showAndWait();
            }
        };

        // Refresh when course changes
        courseComboBox.setOnAction(
                event -> reload.run()
        );

        // Refresh when status filter changes
        statusFilter.setOnAction(
                event -> reload.run()
        );

        contentArea.getChildren().addAll(
                heading,
                filters,
                medicalTable
        );

        // Load courses assigned to logged-in lecturer
        try {

            CourseDAO courseDAO = new CourseDAO();

            List<Course> courses =
                    courseDAO.getCourseModelsByLecturer(
                            lecturerId
                    );

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
