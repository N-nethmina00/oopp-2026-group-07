package com.fot.ams.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StudentCoursesController {

    private record Course(String code, String name, String credits, String lecturer, String status) {}

    @FXML private TableView<Course> tblCourses;
    @FXML private TableColumn<Course, String> colCode;
    @FXML private TableColumn<Course, String> colName;
    @FXML private TableColumn<Course, String> colCredits;
    @FXML private TableColumn<Course, String> colLecturer;
    @FXML private TableColumn<Course, String> colStatus;
    @FXML private TableColumn<Course, Void> colAction;
    private final ObservableList<Course> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().code()));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        colCredits.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().credits()));
        colLecturer.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().lecturer()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status()));
        colStatus.setCellFactory(c -> badgeCell());
        colAction.setCellFactory(c -> new TableCell<>() {
            private final Button b = new Button("View");
            {
                b.getStyleClass().add("small-button");
                b.setOnAction(e -> show(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : b);
            }
        });

        tblCourses.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblCourses.setPlaceholder(new Label("No records found"));
        tblCourses.setItems(data);
        handleRefresh();
    }

    @FXML
    private void handleRefresh() {
        // TODO: CourseDAO.findByStudent(...)
        data.setAll(
                new Course("CS101", "Object Oriented Programming", "3", "Dr. Perera",    "Active"),
                new Course("MA101", "Discrete Mathematics",        "3", "Ms. Silva",     "Active"),
                new Course("DB201", "Database Systems",            "3", "Mr. Fernando",  "Active"),
                new Course("NW201", "Computer Networks",           "2", "Dr. Jayasinghe","Active"),
                new Course("SE301", "Software Engineering",        "3", "Ms. Dias",      "Active"),
                new Course("EN101", "Technical English",           "2", "Mr. Kumara",    "Active"));
    }

    @FXML
    private void handleView() {
        Course c = tblCourses.getSelectionModel().getSelectedItem();
        if (c == null) { info("My Courses", "Please select a course first."); return; }
        show(c);
    }

    private void show(Course c) {
        info("Course Details", c.code() + " - " + c.name() + "\nCredits: " + c.credits() + "\nLecturer: " + c.lecturer());
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

    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
