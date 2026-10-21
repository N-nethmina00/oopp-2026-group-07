package com.fot.ams.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StudentDashboardController {

    private record Slot(String time, String code, String name, String hall, String status) {}

    @FXML private Label lblCourses, lblAttendance, lblGpa, lblPending;
    @FXML private TableView<Slot> tblToday;
    @FXML private TableColumn<Slot, String> colTime;
    @FXML private TableColumn<Slot, String> colCode;
    @FXML private TableColumn<Slot, String> colCourse;
    @FXML private TableColumn<Slot, String> colHall;
    @FXML private TableColumn<Slot, String> colStatus;

    @FXML
    private void initialize() {
        // TODO: DAO eken real data ganna
        colTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().time()));
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().code()));
        colCourse.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        colHall.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().hall()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status()));
        colStatus.setCellFactory(c -> badgeCell());
        tblToday.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblToday.setPlaceholder(new Label("No records found"));
        tblToday.setItems(FXCollections.observableArrayList(
                new Slot("08:00 - 10:00", "CS101", "Object Oriented Programming", "Hall A1", "Completed"),
                new Slot("10:30 - 12:30", "MA101", "Discrete Mathematics",        "Hall B2", "Active"),
                new Slot("01:30 - 03:30", "DB201", "Database Systems",            "Lab 03",  "Upcoming")));
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
