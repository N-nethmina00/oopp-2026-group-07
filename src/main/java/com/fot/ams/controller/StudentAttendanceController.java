package com.fot.ams.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StudentAttendanceController {

    private record Row(String date, String session, String status) {}

    @FXML private ComboBox<String> cmbCourse;

    @FXML
    private Label lblTotal, lblPresent, lblAbsent,
            lblMedical, lblPercent,
            lblLecture, lblPractical,
            lblLecturePercent, lblPracticalPercent;
    @FXML private ProgressBar barAttendance;
    @FXML private TableView<Row> tblAttendance;
    @FXML private TableColumn<Row, String> colDate;
    @FXML private TableColumn<Row, String> colSession;
    @FXML private TableColumn<Row, String> colStatus;
    private final ObservableList<Row> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        cmbCourse.getItems().addAll("CS101 - Object Oriented Programming", "MA101 - Discrete Mathematics",
                "DB201 - Database Systems");
        cmbCourse.getSelectionModel().selectFirst();
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().date()));
        colSession.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().session()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status()));
        colStatus.setCellFactory(c -> badgeCell());
        tblAttendance.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblAttendance.setPlaceholder(new Label("No records found"));
        tblAttendance.setItems(data);
        handleSearch();
    }

    @FXML
    private void handleSearch() {

        String selectedCourse = cmbCourse.getValue();

        data.clear();

        if (selectedCourse == null) {
            updateSummary();
            return;
        }

        // TODO: Replace demo data with DAO

        if (selectedCourse.startsWith("CS101")) {

            data.setAll(
                    new Row("2026-10-05", "Lecture", "Present"),
                    new Row("2026-10-03", "Practical", "Present"),
                    new Row("2026-09-30", "Lecture", "Absent"),
                    new Row("2026-09-28", "Lecture", "Medical"),
                    new Row("2026-09-26", "Practical", "Present"),
                    new Row("2026-09-22", "Lecture", "Present"),
                    new Row("2026-09-19", "Practical", "Absent"),
                    new Row("2026-09-15", "Lecture", "Present")
            );

        } else if (selectedCourse.startsWith("MA101")) {

            data.setAll(
                    new Row("2026-10-06", "Lecture", "Present"),
                    new Row("2026-10-01", "Lecture", "Present"),
                    new Row("2026-09-25", "Lecture", "Absent"),
                    new Row("2026-09-20", "Lecture", "Present"),
                    new Row("2026-09-15", "Lecture", "Present")
            );

        } else if (selectedCourse.startsWith("DB201")) {

            data.setAll(
                    new Row("2026-10-07", "Lecture", "Present"),
                    new Row("2026-10-04", "Practical", "Present"),
                    new Row("2026-10-01", "Lecture", "Medical"),
                    new Row("2026-09-27", "Practical", "Absent"),
                    new Row("2026-09-24", "Lecture", "Present"),
                    new Row("2026-09-21", "Practical", "Present")
            );
        }

        updateSummary();
    }

    private void updateSummary() {

        int total = data.size();

        int present = 0;
        int absent = 0;
        int medical = 0;

        int lectureTotal = 0;
        int lecturePresent = 0;

        int practicalTotal = 0;
        int practicalPresent = 0;

        for (Row row : data) {

            String status = row.status();
            String session = row.session();

            if (status.equalsIgnoreCase("Present")) {
                present++;
            } else if (status.equalsIgnoreCase("Absent")) {
                absent++;
            } else if (status.equalsIgnoreCase("Medical")) {
                medical++;
            }

            if (session.equalsIgnoreCase("Lecture")) {

                lectureTotal++;

                if (status.equalsIgnoreCase("Present")) {
                    lecturePresent++;
                }

            } else if (session.equalsIgnoreCase("Practical")) {

                practicalTotal++;

                if (status.equalsIgnoreCase("Present")) {
                    practicalPresent++;
                }
            }
        }

        double overallPct = total == 0
                ? 0 : (double) present / total;

        double lecturePct = lectureTotal == 0
                ? 0 : (double) lecturePresent / lectureTotal;

        double practicalPct = practicalTotal == 0
                ? 0 : (double) practicalPresent / practicalTotal;

        lblTotal.setText(String.valueOf(total));
        lblPresent.setText(String.valueOf(present));
        lblAbsent.setText(String.valueOf(absent));
        lblMedical.setText(String.valueOf(medical));

        lblPercent.setText(Math.round(overallPct * 100) + "%");
        barAttendance.setProgress(overallPct);

        lblLecture.setText(
                "Present " + lecturePresent + " / " + lectureTotal
        );
        lblLecturePercent.setText(
                Math.round(lecturePct * 100) + "%"
        );

        lblPractical.setText(
                "Present " + practicalPresent + " / " + practicalTotal
        );
        lblPracticalPercent.setText(
                Math.round(practicalPct * 100) + "%"
        );
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
