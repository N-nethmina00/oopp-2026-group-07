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

//UI content for the lecturer Students page.

public final class LecturerStudentsPageController extends LecturerPageBase {

    public LecturerStudentsPageController(LecturerPageContext context) {
        super(context);
    }

    public void showStudents() {

        displayPage(
                "Undergraduate Details",
                "Students enrolled in your assigned courses.",
                studentsBtn
        );

        Label heading = new Label("Enrolled Students");

        heading.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        TableView<String[]> studentsTable = createTable(
                "No student enrollments found.",
                "Registration No",
                "Course Code",
                "Course Name",
                "Batch ID",
                "Status"
        );

        studentsTable.setPrefHeight(400);

        StudentDAO studentDAO = new StudentDAO();

        try {
            List<String[]> students =
                    studentDAO.getStudentsByLecturer(lecturerId);

            studentsTable.getItems().setAll(students);

        } catch (SQLException e) {

            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to load student records from database."
            ).showAndWait();
        }

        contentArea.getChildren().addAll(
                heading,
                studentsTable
        );
    }
}
