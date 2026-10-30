package com.fot.ams.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.util.List;

public class StudentAttendanceController {

    /** Minimum attendance needed (80%) */
    private static final double MIN_REQUIRED = 0.80;

    private record Row(String date, String session, String status) {}

    @FXML private ComboBox<String> cmbCourse;

    @FXML private Label lblTotal, lblPresent, lblAbsent, lblMedical;
    @FXML private Label lblPercent, lblEligibility;
    @FXML private Label lblLecture, lblPractical, lblLecturePercent, lblPracticalPercent;
    @FXML private ProgressBar barAttendance, barLecture, barPractical;

    private List<Row> data = List.of();

    @FXML
    private void initialize() {
        cmbCourse.getItems().addAll("CS101 - Object Oriented Programming", "MA101 - Discrete Mathematics",
                "DB201 - Database Systems");
        cmbCourse.getSelectionModel().selectFirst();
        handleSearch();
    }

    @FXML
    private void handleSearch() {
        String selectedCourse = cmbCourse.getValue();
        data = List.of();

        // TODO: Replace demo data with DAO (AttendanceDAO.findByStudentAndCourse)
        if (selectedCourse != null) {
            if (selectedCourse.startsWith("CS101")) {
                data = List.of(
                        new Row("2026-10-05", "Lecture", "Present"),
                        new Row("2026-10-03", "Practical", "Present"),
                        new Row("2026-09-30", "Lecture", "Absent"),
                        new Row("2026-09-28", "Lecture", "Medical"),
                        new Row("2026-09-26", "Practical", "Present"),
                        new Row("2026-09-22", "Lecture", "Present"),
                        new Row("2026-09-19", "Practical", "Absent"),
                        new Row("2026-09-15", "Lecture", "Present"));
            } else if (selectedCourse.startsWith("MA101")) {
                data = List.of(
                        new Row("2026-10-06", "Lecture", "Present"),
                        new Row("2026-10-01", "Lecture", "Present"),
                        new Row("2026-09-25", "Lecture", "Absent"),
                        new Row("2026-09-20", "Lecture", "Present"),
                        new Row("2026-09-15", "Lecture", "Present"));
            } else if (selectedCourse.startsWith("DB201")) {
                data = List.of(
                        new Row("2026-10-07", "Lecture", "Present"),
                        new Row("2026-10-04", "Practical", "Present"),
                        new Row("2026-10-01", "Lecture", "Medical"),
                        new Row("2026-09-27", "Practical", "Absent"),
                        new Row("2026-09-24", "Lecture", "Present"),
                        new Row("2026-09-21", "Practical", "Present"));
            }
        }
        updateSummary();
    }

    private void updateSummary() {
        int total = data.size();
        int present = 0, absent = 0, medical = 0;
        int lectureTotal = 0, lecturePresent = 0;
        int practicalTotal = 0, practicalPresent = 0;

        for (Row row : data) {
            boolean isPresent = row.status().equalsIgnoreCase("Present");

            if (isPresent) present++;
            else if (row.status().equalsIgnoreCase("Absent")) absent++;
            else if (row.status().equalsIgnoreCase("Medical")) medical++;

            if (row.session().equalsIgnoreCase("Lecture")) {
                lectureTotal++;
                if (isPresent) lecturePresent++;
            } else if (row.session().equalsIgnoreCase("Practical")) {
                practicalTotal++;
                if (isPresent) practicalPresent++;
            }
        }

        double overall   = total == 0 ? 0 : (double) present / total;
        double lecture   = lectureTotal == 0 ? 0 : (double) lecturePresent / lectureTotal;
        double practical = practicalTotal == 0 ? 0 : (double) practicalPresent / practicalTotal;

        // summary cards
        lblTotal.setText(String.valueOf(total));
        lblPresent.setText(String.valueOf(present));
        lblAbsent.setText(String.valueOf(absent));
        lblMedical.setText(String.valueOf(medical));

        // overall
        lblPercent.setText(Math.round(overall * 100) + "%");
        barAttendance.setProgress(overall);
        setBarColor(barAttendance, overall, total);

        if (total == 0) {
            lblEligibility.setText("No records");
            lblEligibility.getStyleClass().setAll("badge", "badge-pending");
        } else if (overall >= MIN_REQUIRED) {
            lblEligibility.setText("Eligible");
            lblEligibility.getStyleClass().setAll("badge", "badge-present");
        } else {
            lblEligibility.setText("Below 80%");
            lblEligibility.getStyleClass().setAll("badge", "badge-absent");
        }

        // by session type
        lblLecture.setText("Present " + lecturePresent + " / " + lectureTotal);
        lblLecturePercent.setText(Math.round(lecture * 100) + "%");
        barLecture.setProgress(lecture);
        setBarColor(barLecture, lecture, lectureTotal);

        lblPractical.setText("Present " + practicalPresent + " / " + practicalTotal);
        lblPracticalPercent.setText(Math.round(practical * 100) + "%");
        barPractical.setProgress(practical);
        setBarColor(barPractical, practical, practicalTotal);
    }

    /** green if >= 80%, red if below */
    private void setBarColor(ProgressBar bar, double pct, int total) {
        bar.getStyleClass().removeAll("bar-good", "bar-low");
        if (total == 0) return;
        bar.getStyleClass().add(pct >= MIN_REQUIRED ? "bar-good" : "bar-low");
    }
}