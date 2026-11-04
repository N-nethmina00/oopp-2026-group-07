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

//UI content for the lecturer Dashboard page

public final class LecturerDashboardPageController extends LecturerPageBase {

    public LecturerDashboardPageController(LecturerPageContext context) {
        super(context);
    }

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

    public void showDashboard() {

        displayPage(
                "Dashboard Overview",
                "Overview of your teaching activities.",
                dashboardBtn
        );

        LecturerDashboardDAO dashboardDAO =
                new LecturerDashboardDAO();

        String studentsCount = "—";
        String pendingMarksCount = "—";
        String materialsCount = "—";

        List<String[]> teachingSessions =
                new java.util.ArrayList<>();

        List<String[]> studentRecords =
                new java.util.ArrayList<>();

        // Load real dashboard data
        try {
            studentsCount = String.valueOf(
                    dashboardDAO.countStudents(lecturerId)
            );

            pendingMarksCount = String.valueOf(
                    dashboardDAO.countPendingMarks(lecturerId)
            );

            materialsCount = String.valueOf(
                    dashboardDAO.countMaterials(lecturerId)
            );

            teachingSessions =
                    dashboardDAO.getTeachingSessions(lecturerId);

            studentRecords =
                    dashboardDAO.getStudentOverview(lecturerId);

        } catch (SQLException e) {
            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to load some lecturer dashboard data."
            ).showAndWait();
        }

        // Dashboard statistic cards
        HBox statsRow = new HBox(15);

        statsRow.getChildren().addAll(

                createStatCard(
                        "My Courses",
                        loadMyCoursesCount(),
                        "Assigned courses"
                ),

                createStatCard(
                        "Students",
                        studentsCount,
                        "Enrolled students"
                ),

                createStatCard(
                        "Pending Marks",
                        pendingMarksCount,
                        "Incomplete results"
                ),

                createStatCard(
                        "Materials",
                        materialsCount,
                        "Uploaded materials"
                )
        );

        // Teaching Schedule
        Label scheduleTitle =
                new Label("Teaching Schedule");

        scheduleTitle.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        TableView<String[]> scheduleTable = createTable(
                "No teaching sessions found.",
                "Day / Date",
                "Time",
                "Course",
                "Session Type"
        );

        scheduleTable.setPrefHeight(250);

        scheduleTable.getItems().setAll(teachingSessions);

        // Students Overview
        Label studentsTitle =
                new Label("Students Overview");

        studentsTitle.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        TableView<String[]> studentsTable = createTable(
                "No enrolled students found.",
                "Registration No",
                "Student Name",
                "Course",
                "Status"
        );

        studentsTable.setPrefHeight(350);

        studentsTable.getItems().setAll(studentRecords);

        contentArea.setSpacing(22);

        contentArea.getChildren().addAll(
                statsRow,
                scheduleTitle,
                scheduleTable,
                studentsTitle,
                studentsTable
        );
    }
}
