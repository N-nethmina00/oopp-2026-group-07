package com.fot.ams.controller;

import com.fot.ams.dao.TimetableDAO;
import com.fot.ams.model.Course;
import com.fot.ams.model.Timetable;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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

    private final ObservableList<Timetable> timetableList =
            FXCollections.observableArrayList();

    // Retained from your original controller.
    private static final String ADMIN_ID = "A001";

    private static final double ROW_HEIGHT = 38.0;
    private static final double HEADER_SPACE = 48.0;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        configureColumns();
        configureCourseDropdown();

        entryIdField.setEditable(false);

        dayComboBox.setItems(FXCollections.observableArrayList(
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday",
                "Saturday",
                "Sunday"
        ));

        sessionTypeComboBox.setItems(
                FXCollections.observableArrayList(
                        "Theory",
                        "Practical"
                )
        );

        semesterComboBox.setItems(
                FXCollections.observableArrayList(
                        "Semester 1",
                        "Semester 2",
                        "Semester 3"
                )
        );

        timetableTable.setItems(timetableList);
        configureTableHeight();

        timetableTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, selected) -> {
                    if (selected != null) {
                        fillForm(selected);
                    }
                });

        loadCourses();
        loadTimetables();
    }

    private void configureColumns() {
        entryIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("timetableId")
        );

        courseColumn.setCellValueFactory(
                new PropertyValueFactory<>("courseCode")
        );

        dayColumn.setCellValueFactory(
                new PropertyValueFactory<>("dayOfWeek")
        );

        timeColumn.setCellValueFactory(cell -> {
            Timetable entry = cell.getValue();

            return new SimpleStringProperty(
                    displayTime(entry.getStartTime())
                            + " - "
                            + displayTime(entry.getEndTime())
            );
        });

        venueColumn.setCellValueFactory(
                new PropertyValueFactory<>("lectureHall")
        );

        sessionTypeColumn.setCellValueFactory(
                new PropertyValueFactory<>("sessionType")
        );

        yearColumn.setCellValueFactory(
                new PropertyValueFactory<>("academicYear")
        );

        semesterColumn.setCellValueFactory(
                new PropertyValueFactory<>("semester")
        );

        timetableTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        timetableTable.setPlaceholder(
                new Label("No timetable entries available.")
        );
    }

    private void configureCourseDropdown() {
        courseComboBox.setEditable(false);

        courseComboBox.setConverter(new StringConverter<Course>() {
            @Override
            public String toString(Course course) {
                if (course == null) {
                    return "";
                }

                return course.getCourseCode()
                        + " - "
                        + course.getCourseName();
            }

            @Override
            public Course fromString(String value) {
                // The dropdown is not editable.
                return null;
            }
        });
    }

    private void configureTableHeight() {
        timetableTable.setFixedCellSize(ROW_HEIGHT);

        timetableTable.setMinHeight(Region.USE_PREF_SIZE);
        timetableTable.setMaxHeight(Region.USE_PREF_SIZE);

        // The table expands with its rows.
        // Vertical scrolling belongs to the outer ScrollPane.
        timetableTable.prefHeightProperty().bind(
                Bindings.createDoubleBinding(
                        () -> Math.max(1, timetableList.size())
                                * timetableTable.getFixedCellSize()
                                + HEADER_SPACE,
                        timetableList,
                        timetableTable.fixedCellSizeProperty()
                )
        );
    }

    private void loadCourses() {
        try {
            courseComboBox.setItems(
                    FXCollections.observableArrayList(
                            dao.getCourses()
                    )
            );

        } catch (SQLException e) {
            showError(
                    "Unable to load courses: " + e.getMessage()
            );
        }
    }

    private void loadTimetables() {
        try {
            timetableList.setAll(
                    dao.getAllTimetables()
            );

        } catch (SQLException e) {
            showError(
                    "Unable to load timetable: " + e.getMessage()
            );
        }
    }

    @FXML
    private void handleAdd() {
        try {
            Timetable timetable = readForm();

            if (dao.addTimetable(timetable)) {
                loadTimetables();
                handleClear();

                messageLabel.setText(
                        "Timetable entry added successfully."
                );
            } else {
                showError("Timetable entry could not be added.");
            }

        } catch (Exception e) {
            showError(
                    "Unable to add timetable entry: "
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void handleUpdate() {
        Timetable selected = timetableTable
                .getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showError("Select a timetable entry first.");
            return;
        }

        try {
            Timetable form = readForm();

            Timetable timetable = new Timetable(
                    selected.getTimetableId(),
                    ADMIN_ID,
                    form.getCourseId(),
                    form.getCourseCode(),
                    form.getCourseName(),
                    form.getDayOfWeek(),
                    form.getStartTime(),
                    form.getEndTime(),
                    form.getLectureHall(),
                    form.getSessionType(),
                    form.getAcademicYear(),
                    form.getSemester()
            );

            if (dao.updateTimetable(timetable)) {
                loadTimetables();
                handleClear();

                messageLabel.setText(
                        "Timetable entry updated successfully."
                );
            } else {
                showError("No timetable entry was updated.");
            }

        } catch (Exception e) {
            showError(
                    "Unable to update timetable entry: "
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void handleDelete() {
        Timetable selected = timetableTable
                .getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showError("Select a timetable entry first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Delete Timetable Entry");
        confirm.setHeaderText("Confirm deletion");
        confirm.setContentText(
                "Delete timetable entry "
                        + selected.getTimetableId()
                        + "?"
        );

        if (confirm.showAndWait()
                .orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            if (dao.deleteTimetable(selected.getTimetableId())) {
                loadTimetables();
                handleClear();

                messageLabel.setText(
                        "Timetable entry deleted successfully."
                );
            } else {
                showError("Timetable entry was not found.");
            }

        } catch (SQLException e) {
            showError(
                    "Unable to delete timetable entry: "
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void handleClear() {
        timetableTable.getSelectionModel().clearSelection();

        entryIdField.clear();

        courseComboBox.getSelectionModel().clearSelection();
        courseComboBox.setValue(null);

        dayComboBox.getSelectionModel().clearSelection();
        dayComboBox.setValue(null);

        startTimeField.clear();
        endTimeField.clear();
        venueField.clear();

        sessionTypeComboBox.getSelectionModel().clearSelection();
        sessionTypeComboBox.setValue(null);

        academicYearField.clear();

        semesterComboBox.getSelectionModel().clearSelection();
        semesterComboBox.setValue(null);

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

        if (course == null
                || day == null
                || start.isEmpty()
                || end.isEmpty()
                || venue.isEmpty()
                || type == null
                || year.isEmpty()
                || semester == null) {

            throw new IllegalArgumentException(
                    "Please complete all fields."
            );
        }

        if (!start.matches("\\d{2}:\\d{2}")
                || !end.matches("\\d{2}:\\d{2}")) {

            throw new IllegalArgumentException(
                    "Enter time in 24-hour HH:mm format, e.g. 08:30."
            );
        }

        LocalTime startTime;
        LocalTime endTime;

        try {
            startTime = LocalTime.parse(start);
            endTime = LocalTime.parse(end);

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Enter a valid time between 00:00 and 23:59."
            );
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "End time must be later than start time."
            );
        }

        if (!year.matches("\\d{4}/\\d{4}")) {
            throw new IllegalArgumentException(
                    "Academic year format must be YYYY/YYYY."
            );
        }

        return new Timetable(
                0,
                ADMIN_ID,
                course.getCourseId(),
                course.getCourseCode(),
                course.getCourseName(),
                day,
                startTime.format(TIME_FORMAT),
                endTime.format(TIME_FORMAT),
                venue,
                type,
                year,
                semester
        );
    }

    private void fillForm(Timetable timetable) {
        entryIdField.setText(
                String.valueOf(timetable.getTimetableId())
        );

        // Clear the previous course before finding the matching one.
        courseComboBox.getSelectionModel().clearSelection();
        courseComboBox.setValue(null);

        for (Course course : courseComboBox.getItems()) {
            if (course.getCourseId() == timetable.getCourseId()) {
                courseComboBox.setValue(course);
                break;
            }
        }

        dayComboBox.setValue(
                timetable.getDayOfWeek()
        );

        startTimeField.setText(
                displayTime(timetable.getStartTime())
        );

        endTimeField.setText(
                displayTime(timetable.getEndTime())
        );

        venueField.setText(
                timetable.getLectureHall()
        );

        sessionTypeComboBox.setValue(
                timetable.getSessionType()
        );

        academicYearField.setText(
                timetable.getAcademicYear()
        );

        semesterComboBox.setValue(
                timetable.getSemester()
        );

        messageLabel.setText(
                "Selected entry ID: " + timetable.getTimetableId()
        );
    }

    private String displayTime(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        try {
            // Accept database values such as 08:30:00,
            // while displaying the HH:mm format used by the form.
            return LocalTime.parse(value.trim()).format(TIME_FORMAT);

        } catch (DateTimeParseException e) {
            return value;
        }
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