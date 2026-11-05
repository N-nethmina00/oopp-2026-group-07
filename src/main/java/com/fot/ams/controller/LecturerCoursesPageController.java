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

//UI content for the lecturer Courses page.

public final class LecturerCoursesPageController extends LecturerPageBase {

    public LecturerCoursesPageController(LecturerPageContext context) {
        super(context);
    }

    public void showCourses() {

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
            List<Course> courses =
                    courseDAO.getCourseModelsByLecturer(lecturerId);

            List<String[]> rows = new ArrayList<>();

            for (Course course : courses) {
                rows.add(new String[]{
                        course.getCourseCode(),
                        course.getCourseName(),
                        String.valueOf(course.getCredit()),
                        course.getType()
                });
            }

            coursesTable.getItems().setAll(rows);

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
}
