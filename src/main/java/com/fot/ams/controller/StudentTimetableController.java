package com.fot.ams.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StudentTimetableController {

    private record Slot(String day, String time, String code, String name, String hall, String lecturer) {}

    @FXML private TableView<Slot> tblTimetable;
    @FXML private TableColumn<Slot, String> colDay;
    @FXML private TableColumn<Slot, String> colTime;
    @FXML private TableColumn<Slot, String> colCode;
    @FXML private TableColumn<Slot, String> colName;
    @FXML private TableColumn<Slot, String> colHall;
    @FXML private TableColumn<Slot, String> colLecturer;

    @FXML
    private void initialize() {
        colDay.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().day()));
        colTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().time()));
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().code()));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        colHall.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().hall()));
        colLecturer.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().lecturer()));
        tblTimetable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblTimetable.setPlaceholder(new Label("No records found"));
        // TODO: TimetableDAO.findByStudent(...)
        tblTimetable.setItems(FXCollections.observableArrayList(
                new Slot("Monday",    "08:00 - 10:00", "CS101", "Object Oriented Programming", "Hall A1", "Dr. Perera"),
                new Slot("Monday",    "10:30 - 12:30", "MA101", "Discrete Mathematics",        "Hall B2", "Ms. Silva"),
                new Slot("Tuesday",   "01:30 - 04:30", "DB201", "Database Systems (Lab)",      "Lab 03",  "Mr. Fernando"),
                new Slot("Wednesday", "09:00 - 11:00", "NW201", "Computer Networks",           "Hall A2", "Dr. Jayasinghe"),
                new Slot("Thursday",  "08:00 - 10:00", "SE301", "Software Engineering",        "Hall C1", "Ms. Dias"),
                new Slot("Friday",    "10:00 - 12:00", "EN101", "Technical English",           "Hall B1", "Mr. Kumara")));
    }
}
