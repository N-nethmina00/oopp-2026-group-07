package com.fot.ams.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import com.fot.ams.dao.StudentDashboardDAO;
import java.sql.SQLException;
import com.fot.ams.model.Student;
import com.fot.ams.model.Timetable;
import java.util.List;


public class StudentDashboardController {

    @FXML private Label lblCourses;
    @FXML private Label lblGpa;
    @FXML private TableView<Timetable> tblToday;
    @FXML private TableColumn<Timetable, String> colTime;
    @FXML private TableColumn<Timetable, String> colCode;
    @FXML private TableColumn<Timetable, String> colCourse;
    @FXML private TableColumn<Timetable, String> colHall;
    @FXML private TableColumn<Timetable, String> colStatus;
    @FXML
    private void initialize() {

        colTime.setCellValueFactory(data -> {

            Timetable t = data.getValue();

            String time =
                    t.getStartTime() + " - " + t.getEndTime();

            return new SimpleStringProperty(time);
        });

        colCode.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCourseCode()
                )
        );

        colCourse.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCourseName()
                )
        );

        colHall.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getLectureHall()
                )
        );

        colStatus.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getSessionType()
                )
        );

        tblToday.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tblToday.setPlaceholder(
                new Label("No timetable records found")
        );
    }

    public void loadDashboard(String userId) {

        StudentDashboardDAO dao = new StudentDashboardDAO();

        try {

            Student student =
                    dao.getStudentDashboardData(userId);

            if (student == null) {
                lblCourses.setText("N/A");
                lblGpa.setText("N/A");
                tblToday.getItems().clear();
                return;
            }

            // Enrolled Course Count
            lblCourses.setText(
                    String.valueOf(student.getEnrolledCourses())
            );

            // GPA
            Double gpa = student.getGpa();

            if (gpa != null) {
                lblGpa.setText(
                        String.format(
                                java.util.Locale.US,
                                "%.2f",
                                gpa
                        )
                );
            } else {
                lblGpa.setText("N/A");
            }


            // ===== LOAD TIMETABLE FROM DATABASE =====

            // Get logged-in student's timetable
            List<Timetable> timetableList =
                    dao.getStudentTimetable(userId);

            // Display timetable in TableView
            tblToday.setItems(
                    FXCollections.observableArrayList(timetableList)
            );

            // ========================================


        } catch (SQLException e) {

            e.printStackTrace();

            lblCourses.setText("N/A");
            lblGpa.setText("N/A");
            tblToday.getItems().clear();
        }
    }

    /** Status column eka colour badge ekak widiyata pennanna */
    private <T> TableCell<T, String> badgeCell() {
        return new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label l = new Label(s);
                l.getStyleClass().addAll("badge", "badge-" + s.toLowerCase().replace(' ', '-'));
                setGraphic(l);
                setText(null);
            }
        };
    }





}