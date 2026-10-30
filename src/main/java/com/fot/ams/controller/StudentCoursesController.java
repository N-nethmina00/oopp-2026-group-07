package com.fot.ams.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class StudentCoursesController {

    /** type = "Theory" / "Practical" */
    private record Course(String code, String name, String credits, String lecturer, String type) {}

    @FXML private TableView<Course> tblCourses;
    @FXML private TableColumn<Course, String> colCode;
    @FXML private TableColumn<Course, String> colName;
    @FXML private TableColumn<Course, String> colCredits;
    @FXML private TableColumn<Course, String> colLecturer;
    @FXML private TableColumn<Course, String> colType;

    @FXML
    private void initialize() {
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().code()));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        colCredits.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().credits()));
        colLecturer.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().lecturer()));
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().type()));
        colType.setCellFactory(c -> badgeCell());

        tblCourses.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblCourses.setPlaceholder(new Label("No records found"));

        // TODO: CourseDAO.findByStudent(...)
        tblCourses.setItems(FXCollections.observableArrayList(
                new Course("CS101", "Object Oriented Programming", "3", "Dr. Perera",     "Theory"),
                new Course("MA101", "Discrete Mathematics",        "3", "Ms. Silva",      "Theory"),
                new Course("DB201", "Database Systems",            "3", "Mr. Fernando",   "Practical"),
                new Course("NW201", "Computer Networks",           "2", "Dr. Jayasinghe", "Theory"),
                new Course("SE301", "Software Engineering",        "3", "Ms. Dias",       "Practical"),
                new Course("EN101", "Technical English",           "2", "Mr. Kumara",     "Theory")));
    }

    /** Type eka colour badge ekak widiyata pennanna */
    private TableCell<Course, String> badgeCell() {
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