
package com.fot.ams.controller;

import com.fot.ams.dao.StudentAttendanceDAO;
import com.fot.ams.dao.StudentCoursesDAO;
import com.fot.ams.model.Attendance;
import com.fot.ams.model.Course;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.List;

public class StudentAttendanceController {

    private static final double MIN_REQUIRED = 0.80;

    @FXML private ComboBox<Course> cmbCourse;

    @FXML private Label lblTotal;
    @FXML private Label lblPresent;
    @FXML private Label lblAbsent;


    @FXML private Label lblPercent;
    @FXML private Label lblEligibility;

    @FXML private Label lblLecture;
    @FXML private Label lblPractical;
    @FXML private Label lblLecturePercent;
    @FXML private Label lblPracticalPercent;

    @FXML private ProgressBar barAttendance;
    @FXML private ProgressBar barLecture;
    @FXML private ProgressBar barPractical;

    private String loggedInUserId;

    private final StudentCoursesDAO coursesDAO =
            new StudentCoursesDAO();

    private final StudentAttendanceDAO attendanceDAO =
            new StudentAttendanceDAO();

    @FXML
    private void initialize() {

        cmbCourse.setConverter(new StringConverter<>() {

            @Override
            public String toString(Course course) {

                if (course == null) {
                    return "";
                }

                return course.getCourseCode()
                        + " - " + course.getCourseName();
            }

            @Override
            public Course fromString(String text) {
                return null;
            }
        });

        updateSummary(List.of());
    }

    public void loadAttendance(String userId) {

        loggedInUserId = userId;

        cmbCourse.getItems().clear();
        updateSummary(List.of());

        if (userId == null || userId.isBlank()) {
            showError("Student is not logged in.");
            return;
        }

        try {

            List<Course> courses =
                    coursesDAO.getEnrolledCourses(userId);

            cmbCourse.getItems().setAll(courses);

            if (!courses.isEmpty()) {
                cmbCourse.getSelectionModel().selectFirst();
                handleSearch();
            }

        } catch (SQLException e) {

            e.printStackTrace();
            updateSummary(List.of());

            showError(
                    "Unable to load courses: "
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void handleSearch() {

        Course selectedCourse = cmbCourse.getValue();

        if (selectedCourse == null
                || loggedInUserId == null) {

            updateSummary(List.of());
            return;
        }

        try {

            List<Attendance> records =
                    attendanceDAO.getStudentAttendance(
                            loggedInUserId,
                            selectedCourse.getCourseId()
                    );

            updateSummary(records);

        } catch (SQLException e) {

            e.printStackTrace();
            updateSummary(List.of());

            showError(
                    "Unable to load attendance: "
                            + e.getMessage()
            );
        }
    }

    private void updateSummary(List<Attendance> records) {

        int total = records.size();

        int present = 0;
        int absent = 0;


        int lectureTotal = 0;
        int lecturePresent = 0;

        int practicalTotal = 0;
        int practicalPresent = 0;

        for (Attendance record : records) {

            String status = record.getStatus();
            String type = record.getSessionType();

            boolean isPresent =
                    "Present".equalsIgnoreCase(status);

            if (isPresent) {
                present++;

            } else if ("Absent".equalsIgnoreCase(status)
                    || "Late".equalsIgnoreCase(status)) {
                absent++;


            }

            if ("Theory".equalsIgnoreCase(type)) {

                lectureTotal++;

                if (isPresent) {
                    lecturePresent++;
                }

            } else if ("Practical".equalsIgnoreCase(type)) {

                practicalTotal++;

                if (isPresent) {
                    practicalPresent++;
                }
            }
        }

        double overall = total == 0
                ? 0
                : (double) present / total;

        double lecture = lectureTotal == 0
                ? 0
                : (double) lecturePresent / lectureTotal;

        double practical = practicalTotal == 0
                ? 0
                : (double) practicalPresent / practicalTotal;

        // Summary cards
        lblTotal.setText(String.valueOf(total));
        lblPresent.setText(String.valueOf(present));
        lblAbsent.setText(String.valueOf(absent));


        // Overall percentage
        lblPercent.setText(
                total == 0
                        ? "N/A"
                        : Math.round(overall * 100) + "%"
        );

        barAttendance.setProgress(overall);
        setBarColor(barAttendance, overall, total);

        if (total == 0) {

            lblEligibility.setText("No records");
            lblEligibility.getStyleClass().setAll(
                    "badge", "badge-pending"
            );

        } else if (overall >= MIN_REQUIRED) {

            lblEligibility.setText("80% or above");
            lblEligibility.getStyleClass().setAll(
                    "badge", "badge-present"
            );

        } else {

            lblEligibility.setText("Below 80%");
            lblEligibility.getStyleClass().setAll(
                    "badge", "badge-absent"
            );
        }

        // Theory / Lecture attendance
        lblLecture.setText(
                "Present " + lecturePresent
                        + " / " + lectureTotal
        );

        lblLecturePercent.setText(
                lectureTotal == 0
                        ? "N/A"
                        : Math.round(lecture * 100) + "%"
        );

        barLecture.setProgress(lecture);

        setBarColor(
                barLecture,
                lecture,
                lectureTotal
        );

        // Practical attendance
        lblPractical.setText(
                "Present " + practicalPresent
                        + " / " + practicalTotal
        );

        lblPracticalPercent.setText(
                practicalTotal == 0
                        ? "N/A"
                        : Math.round(practical * 100) + "%"
        );

        barPractical.setProgress(practical);

        setBarColor(
                barPractical,
                practical,
                practicalTotal
        );
    }

    private void setBarColor(
            ProgressBar bar,
            double percentage,
            int total) {

        bar.getStyleClass().removeAll(
                "bar-good", "bar-low"
        );

        if (total == 0) {
            return;
        }

        bar.getStyleClass().add(
                percentage >= MIN_REQUIRED
                        ? "bar-good"
                        : "bar-low"
        );
    }

    private void showError(String message) {

        new Alert(
                Alert.AlertType.ERROR,
                message
        ).showAndWait();
    }
}
