package com.fot.ams.controller;

import com.fot.ams.dao.TimetableDAO;
import com.fot.ams.model.Course;
import com.fot.ams.model.Timetable;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class AdminTimetableManagementController {

    @FXML private TextField entryIdField;
    @FXML private ComboBox<Course> courseComboBox;
    @FXML private ComboBox<String> dayComboBox;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private TextField venueField;
    @FXML private ComboBox<String> sessionTypeComboBox;
    @FXML private TextField academicYearField;
    @FXML private ComboBox<String> semesterComboBox;
    @FXML private Label messageLabel;

    @FXML private TableView<Timetable> timetableTable;
    @FXML private TableColumn<Timetable, Integer> entryIdColumn;
    @FXML private TableColumn<Timetable, String> courseColumn;
    @FXML private TableColumn<Timetable, String> dayColumn;
    @FXML private TableColumn<Timetable, String> timeColumn;
    @FXML private TableColumn<Timetable, String> venueColumn;
    @FXML private TableColumn<Timetable, String> sessionTypeColumn;
    @FXML private TableColumn<Timetable, String> yearColumn;
    @FXML private TableColumn<Timetable, String> semesterColumn;

    private final TimetableDAO dao = new TimetableDAO();

    private static final String ADMIN_ID = "A001";

    @FXML
    public void initialize() {
        entryIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("timetableId"));

        courseColumn.setCellValueFactory(
                new PropertyValueFactory<>("courseCode"));

        dayColumn.setCellValueFactory(
                new PropertyValueFactory<>("dayOfWeek"));

        timeColumn.setCellValueFactory(cell -> {
            Timetable t = cell.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    t.getStartTime() + " - " + t.getEndTime());
        });

        venueColumn.setCellValueFactory(
                new PropertyValueFactory<>("lectureHall"));

        sessionTypeColumn.setCellValueFactory(
                new PropertyValueFactory<>("sessionType"));

        yearColumn.setCellValueFactory(
                new PropertyValueFactory<>("academicYear"));

        semesterColumn.setCellValueFactory(
                new PropertyValueFactory<>("semester"));

        entryIdField.setEditable(false);

        dayComboBox.setItems(FXCollections.observableArrayList(
                "Monday", "Tuesday", "Wednesday", "Thursday",
                "Friday", "Saturday", "Sunday"));

        sessionTypeComboBox.setItems(FXCollections.observableArrayList(
                "Theory", "Practical"));

        semesterComboBox.setItems(FXCollections.observableArrayList(
                "Semester 1", "Semester 2", "Semester 3"));

        timetableTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, selected) -> {
                    if (selected != null) {
                        fillForm(selected);
                    }
                });

        loadCourses();
        loadTimetables();
    }

    private void loadCourses() {
        try {
            courseComboBox.setItems(
                    FXCollections.observableArrayList(dao.getCourses()));
        } catch (SQLException e) {
            showError("Unable to load courses: " + e.getMessage());
        }
    }

    private void loadTimetables() {
        try {
            timetableTable.setItems(
                    FXCollections.observableArrayList(dao.getAllTimetables()));
        } catch (SQLException e) {
            showError("Unable to load timetable: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        try {
            Timetable timetable = readForm();
            if (dao.addTimetable(timetable)) {
                loadTimetables();
                handleClear();
                messageLabel.setText("Timetable entry added successfully.");
            }
        } catch (Exception e) {
            showError("Unable to add timetable entry: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        try {
            if (entryIdField.getText().trim().isEmpty()) {
                showError("Select a timetable entry first.");
                return;
            }

            Timetable timetable = readForm();
            timetable = new Timetable(
                    Integer.parseInt(entryIdField.getText().trim()),
                    ADMIN_ID,
                    timetable.getCourseId(),
                    timetable.getCourseCode(),
                    timetable.getCourseName(),
                    timetable.getDayOfWeek(),
                    timetable.getStartTime(),
                    timetable.getEndTime(),
                    timetable.getLectureHall(),
                    timetable.getSessionType(),
                    timetable.getAcademicYear(),
                    timetable.getSemester()
            );

            if (dao.updateTimetable(timetable)) {
                loadTimetables();
                messageLabel.setText("Timetable entry updated successfully.");
            } else {
                showError("No timetable entry was updated.");
            }
        } catch (Exception e) {
            showError("Unable to update timetable entry: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (entryIdField.getText().trim().isEmpty()) {
            showError("Select a timetable entry first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Timetable Entry");
        confirm.setHeaderText("Confirm deletion");
        confirm.setContentText("Delete this timetable entry?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            int id = Integer.parseInt(entryIdField.getText().trim());

            if (dao.deleteTimetable(id)) {
                loadTimetables();
                handleClear();
                messageLabel.setText("Timetable entry deleted successfully.");
            }
        } catch (Exception e) {
            showError("Unable to delete timetable entry: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        entryIdField.clear();
        courseComboBox.getSelectionModel().clearSelection();
        dayComboBox.getSelectionModel().clearSelection();
        startTimeField.clear();
        endTimeField.clear();
        venueField.clear();
        sessionTypeComboBox.getSelectionModel().clearSelection();
        academicYearField.clear();
        semesterComboBox.getSelectionModel().clearSelection();
        timetableTable.getSelectionModel().clearSelection();
        messageLabel.setText("");
    }

    private Timetable readForm() {
        Course course = courseComboBox.getValue();
        String day = dayComboBox.getValue();
        String start = startTimeField.getText().trim();
        String end = endTimeField.getText().trim();
        String venue = venueField.getText().trim();
        String type = sessionTypeComboBox.getValue();
        String year = academicYearField.getText().trim();
        String semester = semesterComboBox.getValue();

        if (course == null || day == null || start.isEmpty()
                || end.isEmpty() || venue.isEmpty() || type == null
                || year.isEmpty() || semester == null) {
            throw new IllegalArgumentException("Please complete all fields.");
        }

        if (!start.matches("\\d{2}:\\d{2}") ||
                !end.matches("\\d{2}:\\d{2}")) {
            throw new IllegalArgumentException(
                    "Enter time in 24-hour HH:mm format, e.g. 08:30.");
        }

        java.time.LocalTime startTime;
        java.time.LocalTime endTime;

        try {
            startTime = java.time.LocalTime.parse(start);
            endTime = java.time.LocalTime.parse(end);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid time.");
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "End time must be later than start time.");
        }

        if (!year.matches("\\d{4}/\\d{4}")) {
            throw new IllegalArgumentException(
                    "Academic year format must be YYYY/YYYY.");
        }

        return new Timetable(
                0, ADMIN_ID,
                course.getCourseId(),
                course.getCourseCode(),
                course.getCourseName(),
                day, start, end, venue, type, year, semester
        );
    }

    private void fillForm(Timetable t) {
        entryIdField.setText(String.valueOf(t.getTimetableId()));

        for (Course course : courseComboBox.getItems()) {
            if (course.getCourseId() == t.getCourseId()) {
                courseComboBox.setValue(course);
                break;
            }
        }

        dayComboBox.setValue(t.getDayOfWeek());
        startTimeField.setText(t.getStartTime());
        endTimeField.setText(t.getEndTime());
        venueField.setText(t.getLectureHall());
        sessionTypeComboBox.setValue(t.getSessionType());
        academicYearField.setText(t.getAcademicYear());
        semesterComboBox.setValue(t.getSemester());
    }

    private void showError(String message) {
        messageLabel.setText(message);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Timetable Management");
        alert.setHeaderText("Operation failed");
        alert.setContentText(message);
        alert.showAndWait();
    }
}