package com.fot.ams.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StudentAttendanceController {

    private record Row(String date, String session, String status) {}

    @FXML private ComboBox<String> cmbCourse;
    @FXML private Label lblPercent, lblCount;
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
        // TODO: AttendanceDAO.findByStudentAndCourse(...)
        data.setAll(
                new Row("2026-10-05", "Lecture",   "Present"),
                new Row("2026-10-03", "Practical", "Present"),
                new Row("2026-09-30", "Lecture",   "Absent"),
                new Row("2026-09-28", "Lecture",   "Medical"),
                new Row("2026-09-26", "Practical", "Present"));

        int present = 24, total = 28;   // demo values
        double pct = (double) present / total;
        lblPercent.setText(Math.round(pct * 100) + "%");
        lblCount.setText("Present " + present + " / " + total);
        barAttendance.setProgress(pct);
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
