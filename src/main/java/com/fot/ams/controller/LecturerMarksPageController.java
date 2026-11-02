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

//UI content for the lecturer Marks page.

public final class LecturerMarksPageController extends LecturerPageBase {

    public LecturerMarksPageController(LecturerPageContext context) {
        super(context);
    }

    public void showMarks() {

        displayPage(
                "Student Marks",
                "View and manage marks for your assigned courses.",
                marksBtn
        );

        Label heading = new Label("Student Marks Management");

        heading.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        // Course selection
        Label courseLabel = new Label("Select Course Module");

        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.setPromptText("Select Your Course");
        courseComboBox.setPrefWidth(300);

        // Student marks table
        TableView<String[]> marksTable = createTable(
                "Please select a course to view student marks.",
                "Enrollment ID",
                "Registration No",
                "Quiz 1",
                "Quiz 2",
                "Quiz 3",
                "Assignment 1",
                "Assignment 2",
                "Mid Exam",
                "Quiz Total",
                "CA Total",
                "End Exam",
                "Final Total",
                "Grade",
                "Grade Point"
        );

        marksTable.setPrefHeight(450);

        // Hide internal Enrollment ID from UI
        marksTable.getColumns().get(0).setVisible(false);

        // Allow horizontal scrolling for marks columns
        marksTable.setColumnResizePolicy(
                TableView.UNCONSTRAINED_RESIZE_POLICY
        );

        marksTable.getColumns().forEach(column ->
                column.setPrefWidth(115)
        );

        // Load courses assigned to logged-in lecturer
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
                    "Unable to load lecturer courses."
            ).showAndWait();
        }

        // Load student marks after selecting a course
        courseComboBox.setOnAction(event -> {

            String selectedCourse = courseComboBox.getValue();

            marksTable.getItems().clear();

            if (selectedCourse == null) {
                return;
            }

            try {
                MarksDAO marksDAO = new MarksDAO();

                List<String[]> marks =
                        marksDAO.getMarksByLecturerCourse(
                                lecturerId,
                                selectedCourse
                        );

                marksTable.getItems().setAll(marks);

            } catch (SQLException e) {
                e.printStackTrace();

                new Alert(
                        Alert.AlertType.ERROR,
                        "Unable to load student marks."
                ).showAndWait();
            }
        });


        Button editMarksButton = new Button("Update Selected Marks");

        editMarksButton.setStyle(
                "-fx-background-color: #4c56cd;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 11 20;"
        );

        editMarksButton.disableProperty().bind(
                marksTable.getSelectionModel()
                        .selectedItemProperty().isNull()
        );

        editMarksButton.setOnAction(event -> {

            String[] selected =
                    marksTable.getSelectionModel().getSelectedItem();

            String selectedCourse = courseComboBox.getValue();

            if (selected == null || selectedCourse == null) {
                return;
            }

            boolean saved = showMarkEditorDialog(
                    selectedCourse, selected
            );

            if (saved) {
                try {
                    List<String[]> updatedMarks =
                            new MarksDAO().getMarksByLecturerCourse(
                                    lecturerId, selectedCourse
                            );

                    marksTable.getItems().setAll(updatedMarks);

                    new Alert(
                            Alert.AlertType.INFORMATION,
                            "Student marks saved successfully!"
                    ).showAndWait();

                } catch (SQLException e) {
                    e.printStackTrace();

                    new Alert(
                            Alert.AlertType.ERROR,
                            "Marks saved, but table refresh failed."
                    ).showAndWait();
                }
            }
        });


        Button addMarksButton = new Button("+ Add New Marks");

        addMarksButton.setStyle(
                "-fx-background-color: #198754;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 11 20;"
        );

        addMarksButton.setOnAction(event -> {

            String courseCode = courseComboBox.getValue();

            if (courseCode == null) {
                new Alert(
                        Alert.AlertType.WARNING,
                        "Please select a course first."
                ).showAndWait();
                return;
            }

            // Find enrolled students without existing marks
            List<String[]> newStudents =
                    marksTable.getItems().stream()
                            .filter(row ->
                                    row[2] == null &&
                                            row[10] == null
                            )
                            .toList();

            if (newStudents.isEmpty()) {
                new Alert(
                        Alert.AlertType.INFORMATION,
                        "No enrolled students without marks found."
                ).showAndWait();
                return;
            }

            // Student selection dialog
            List<String> studentNumbers =
                    newStudents.stream()
                            .map(row -> row[1])
                            .toList();

            ChoiceDialog<String> studentDialog =
                    new ChoiceDialog<>(
                            studentNumbers.get(0),
                            studentNumbers
                    );

            studentDialog.setTitle("Add New Marks");
            studentDialog.setHeaderText(
                    "Select a student for course " + courseCode
            );

            studentDialog.setContentText("Student Registration No:");

            Optional<String> result = studentDialog.showAndWait();

            if (result.isEmpty()) {
                return;
            }

            String selectedRegNo = result.get();

            String[] student = newStudents.stream()
                    .filter(row -> row[1].equals(selectedRegNo))
                    .findFirst()
                    .orElse(null);

            if (student == null) {
                return;
            }

            // Open existing marks entry dialog
            boolean saved = showMarkEditorDialog(
                    courseCode,
                    student
            );

            if (saved) {
                try {
                    List<String[]> updatedMarks =
                            new MarksDAO().getMarksByLecturerCourse(
                                    lecturerId,
                                    courseCode
                            );

                    marksTable.getItems().setAll(updatedMarks);

                    new Alert(
                            Alert.AlertType.INFORMATION,
                            "New student marks added successfully!"
                    ).showAndWait();

                } catch (SQLException e) {
                    e.printStackTrace();

                    new Alert(
                            Alert.AlertType.ERROR,
                            "Marks saved, but table refresh failed."
                    ).showAndWait();
                }
            }
        });

// Display action buttons horizontally
        HBox marksActionButtons = new HBox(10);

        marksActionButtons.getChildren().addAll(
                addMarksButton,
                editMarksButton
        );



        VBox courseSection = new VBox(10);
        courseSection.getChildren().addAll(
                courseLabel,
                courseComboBox
        );

        contentArea.getChildren().addAll(
                heading,
                courseSection,
                marksActionButtons,
                marksTable
        );
    }

    private boolean showMarkEditorDialog(
            String courseCode, String[] student) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add / Update Student Marks");
        dialog.setHeaderText(
                "Student: " + student[1] +
                        " | Course: " + courseCode
        );

        String[] labels = {
                "Quiz 1 (10)",
                "Quiz 2 (10)",
                "Quiz 3 (10)",
                "Assignment 1 (20)",
                "Assignment 2 (20)",
                "Mid Exam (30)",
                "End Exam (100)"
        };

        // Column positions from MarksDAO result
        int[] indices = {2, 3, 4, 5, 6, 7, 10};

        TextField[] fields = new TextField[7];

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        for (int i = 0; i < fields.length; i++) {

            String existingValue = student[indices[i]];

            fields[i] = new TextField(
                    existingValue == null ? "" : existingValue
            );

            fields[i].setPromptText("Enter marks");
            fields[i].setPrefWidth(180);

            grid.add(new Label(labels[i]), 0, i);
            grid.add(fields[i], 1, i);
        }

        Label previewLabel = new Label();
        previewLabel.setWrapText(true);

        previewLabel.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #17324D;" +
                        "-fx-background-color: #F0F3FA;" +
                        "-fx-padding: 12;"
        );

        MarksDAO dao = new MarksDAO();

        // Update calculated results while typing
        Runnable updatePreview = () -> {
            try {
                double[] v = readMarkInputs(fields, labels);

                previewLabel.setText(
                        dao.getMarksPreview(
                                v[0], v[1], v[2],
                                v[3], v[4], v[5], v[6]
                        )
                );

            } catch (IllegalArgumentException e) {
                previewLabel.setText(
                        "Enter valid marks in all seven fields " +
                                "to see the calculation."
                );
            }
        };

        for (TextField field : fields) {
            field.textProperty().addListener(
                    (observable, oldValue, newValue) ->
                            updatePreview.run()
            );
        }

        updatePreview.run();

        VBox form = new VBox(15);
        form.setPadding(new Insets(15));
        form.getChildren().addAll(grid, previewLabel);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(480);

        ButtonType saveType = new ButtonType(
                "Save Marks", ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveType, ButtonType.CANCEL
        );

        final boolean[] saved = {false};

        Button saveButton = (Button)
                dialog.getDialogPane().lookupButton(saveType);

        // Validate and save before allowing dialog to close
        saveButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {

                    try {
                        double[] v = readMarkInputs(fields, labels);

                        int enrollmentId =
                                Integer.parseInt(student[0]);

                        dao.saveStudentMarks(
                                lecturerId,
                                courseCode,
                                enrollmentId,
                                v[0], v[1], v[2],
                                v[3], v[4], v[5], v[6]
                        );

                        saved[0] = true;

                    } catch (SQLException |
                             IllegalArgumentException e) {

                        e.printStackTrace();

                        new Alert(
                                Alert.AlertType.ERROR,
                                "Unable to save marks: " +
                                        e.getMessage()
                        ).showAndWait();

                        // Keep dialog open on failure
                        event.consume();
                    }
                }
        );

        dialog.showAndWait();

        return saved[0];
    }

    private double[] readMarkInputs(
            TextField[] fields, String[] labels) {

        double[] values = new double[fields.length];

        for (int i = 0; i < fields.length; i++) {

            String text = fields[i].getText().trim();

            if (text.isEmpty()) {
                throw new IllegalArgumentException(
                        labels[i] + " is required."
                );
            }

            try {
                values[i] = Double.parseDouble(text);

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        labels[i] + " must be a valid number."
                );
            }
        }

        return values;
    }
}
