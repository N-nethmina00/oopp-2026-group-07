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

//UI content for the lecturer Grades page
public final class LecturerGradesPageController extends LecturerPageBase {

    public LecturerGradesPageController(LecturerPageContext context) {
        super(context);
    }

    public void showGrades() {

        displayPage(
                "Grades & GPA",
                "View student grades and grade points.",
                gradesBtn
        );

        Label heading = new Label("Student Grades & Results");

        heading.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        Label courseLabel = new Label("Select Course Module");

        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.setPromptText("Select Your Course");
        courseComboBox.setPrefWidth(300);

        TableView<String[]> gradesTable = createTable(
                "Select a course to view student grades.",
                "Registration No",
                "Raw CA (/70)",
                "End Exam (/100)",
                "Final Total (/100)",
                "Grade",
                "Grade Point"
        );

        gradesTable.setPrefHeight(400);


        // Load assigned courses
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

        // Load grades when a course is selected
        courseComboBox.setOnAction(event -> {

            String courseCode = courseComboBox.getValue();

            gradesTable.getItems().clear();

            if (courseCode == null) {
                return;
            }

            try {
                MarksDAO marksDAO = new MarksDAO();

                List<Grade> grades =
                        marksDAO.getGradeModelsByLecturerCourse(
                                lecturerId, courseCode
                        );

                List<Mark> markModels =
                        marksDAO.getMarkModelsByLecturerCourse(
                                lecturerId, courseCode
                        );

                List<CAEvaluation> caModels =
                        marksDAO.getCAEvaluationsByLecturerCourse(
                                lecturerId, courseCode
                        );

                Map<Integer, Mark> markByEnrollment = new HashMap<>();
                for (Mark mark : markModels) {
                    markByEnrollment.put(mark.getEnrollmentId(), mark);
                }

                Map<Integer, CAEvaluation> caByEnrollment = new HashMap<>();
                for (CAEvaluation ca : caModels) {
                    caByEnrollment.put(ca.getEnrollmentId(), ca);
                }

                List<String[]> results = new ArrayList<>();

                for (Grade grade : grades) {
                    // Only show saved final results
                    if (grade.getTotalMarks() == null || grade.getGrade() == null) {
                        continue;
                    }

                    CAEvaluation ca = caByEnrollment.get(grade.getEnrollmentId());
                    Mark mark = markByEnrollment.get(grade.getEnrollmentId());

                    results.add(new String[]{
                            grade.getRegistrationNo(),
                            formatMark(ca == null ? null : ca.getTotalCAMarks()),
                            formatMark(mark == null ? null : mark.getEndExamMarks()),
                            formatMark(grade.getTotalMarks()),
                            grade.getGrade(),
                            formatMark(grade.getGradePoint())
                    });
                }

                gradesTable.getItems().setAll(results);

            } catch (SQLException e) {
                e.printStackTrace();

                new Alert(
                        Alert.AlertType.ERROR,
                        "Unable to load student grades."
                ).showAndWait();
            }
        });

        contentArea.getChildren().addAll(
                heading,
                courseLabel,
                courseComboBox,
                gradesTable
        );
    }

    private static String formatMark(Double value) {
        return value == null ? "" : String.format(Locale.US, "%.2f", value);
    }
}
