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
    @FXML private ComboBox<String> academicYearComboBox;
    @FXML private ComboBox<String> semesterComboBox;
    @FXML private Label messageLabel;

    @FXML private TableView<Timetable> timetableTable;

    @FXML
    private TableColumn<Timetable, Integer> entryIdColumn;

    @FXML
    private TableColumn<Timetable, String> courseColumn;

    @FXML
    private TableColumn<Timetable, String> dayColumn;

    @FXML
    private TableColumn<Timetable, String> timeColumn;

    @FXML
    private TableColumn<Timetable, String> venueColumn;

    @FXML
    private TableColumn<Timetable, String> sessionTypeColumn;

    @FXML
    private TableColumn<Timetable, String> yearColumn;

    @FXML
    private TableColumn<Timetable, String> semesterColumn;

    private final TimetableDAO dao = new TimetableDAO();

    private final ObservableList<Timetable> timetableList =
            FXCollections.observableArrayList();

    /*
     * Compatibility default for the supplied database.
     * The dashboard can pass the logged-in administrator's
     * role-specific ID using setAdminId().
     */
    private String adminId = "A001";

    private static final double ROW_HEIGHT = 38.0;
    private static final double HEADER_SPACE = 48.0;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    public void setAdminId(String adminId) {
        if (adminId == null
                || adminId.isBlank()
                || adminId.trim().length() > 10) {
            throw new IllegalArgumentException(
                    "A valid admin role ID is required."
            );
        }

        this.adminId = adminId.trim();
    }

    @FXML
    public void initialize() {
        configureColumns();
        configureCourseDropdown();

        entryIdField.setEditable(false);

        dayComboBox.setItems(
                FXCollections.observableArrayList(
                        Timetable.DAYS
                )
        );

        sessionTypeComboBox.setItems(
                FXCollections.observableArrayList(
                        Timetable.SESSION_TYPES
                )
        );

        semesterComboBox.setItems(
                FXCollections.observableArrayList(
                        Timetable.SEMESTERS
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

        academicYearComboBox.setEditable(false);
        semesterComboBox.setEditable(false);

        loadCourses();
        loadAcademicYears();
        loadTimetables();
        refreshNextEntryId();
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

        courseComboBox.setConverter(
                new StringConverter<Course>() {
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
                        return null;
                    }
                }
        );
    }

    private void configureTableHeight() {
        timetableTable.setFixedCellSize(ROW_HEIGHT);

        timetableTable.setMinHeight(
                Region.USE_PREF_SIZE
        );

        timetableTable.setMaxHeight(
                Region.USE_PREF_SIZE
        );

        /*
         * The table expands with its rows.
         * The outer ScrollPane handles vertical scrolling.
         */
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

    private void loadAcademicYears() {
        try {
            academicYearComboBox.setItems(
                    FXCollections.observableArrayList(
                            dao.getAcademicYears()
                    )
            );

            if (academicYearComboBox.getItems().isEmpty()) {
                showError(
                        "No academic years are available. "
                                + "Add a batch with an academic year "
                                + "before creating a timetable."
                );
            }

        } catch (SQLException e) {
            showError(
                    "Unable to load academic years: "
                            + e.getMessage()
            );
        }
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
                    "Unable to load courses: "
                            + e.getMessage()
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
                    "Unable to load timetable: "
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void handleAdd() {
        try {
            if (timetableTable.getSelectionModel()
                    .getSelectedItem() != null) {
                showError(
                        "Click Clear before adding "
                                + "a new timetable entry."
                );
                return;
            }

            Timetable timetable = readForm();

            int addedId = dao.addTimetableReturningId(
                    timetable
            );

            if (addedId > 0) {
                loadTimetables();
                handleClear();

                messageLabel.setText(
                        "Timetable entry #"
                                + addedId
                                + " added successfully."
                );

            } else {
                showError(
                        "Timetable entry could not be added."
                );
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
            showError(
                    "Select a timetable entry first."
            );
            return;
        }

        try {
            Timetable form = readForm();

            Timetable timetable = new Timetable(
                    selected.getTimetableId(),
                    selected.getAdminId(),
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
                showError(
                        "No timetable entry was updated."
                );
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
            showError(
                    "Select a timetable entry first."
            );
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION
        );

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
            if (dao.deleteTimetable(
                    selected.getTimetableId()
            )) {
                loadTimetables();
                handleClear();

                messageLabel.setText(
                        "Timetable entry deleted successfully."
                );

            } else {
                showError(
                        "Timetable entry was not found."
                );
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
        timetableTable.getSelectionModel()
                .clearSelection();

        entryIdField.clear();

        courseComboBox.getSelectionModel()
                .clearSelection();
        courseComboBox.setValue(null);

        dayComboBox.getSelectionModel()
                .clearSelection();
        dayComboBox.setValue(null);

        startTimeField.clear();
        endTimeField.clear();
        venueField.clear();

        sessionTypeComboBox.getSelectionModel()
                .clearSelection();
        sessionTypeComboBox.setValue(null);

        academicYearComboBox.getSelectionModel()
                .clearSelection();
        academicYearComboBox.setValue(null);

        semesterComboBox.getSelectionModel()
                .clearSelection();
        semesterComboBox.setValue(null);

        messageLabel.setText("");

        refreshNextEntryId();
    }

    private void refreshNextEntryId() {
        try {
            entryIdField.setText(
                    String.valueOf(
                            dao.getNextTimetableId()
                    )
            );

            entryIdField.setPromptText(
                    "Next ID (assigned when saved)"
            );

        } catch (SQLException e) {
            entryIdField.clear();

            entryIdField.setPromptText(
                    "Assigned when saved"
            );

            showError(
                    "Unable to load the next entry ID: "
                            + e.getMessage()
            );
        }
    }

    private Timetable readForm() {
        Course course = courseComboBox.getValue();

        if (course == null) {
            courseComboBox.requestFocus();

            throw new IllegalArgumentException(
                    "Select a course."
            );
        }

        String start = startTimeField
                .getText()
                .trim();

        String end = endTimeField
                .getText()
                .trim();

        if (!start.matches("[0-9]{2}:[0-9]{2}")
                || !end.matches("[0-9]{2}:[0-9]{2}")) {
            throw new IllegalArgumentException(
                    "Enter times in 24-hour HH:mm format, "
                            + "for example 08:30."
            );
        }

        String year = academicYearComboBox.getValue();

        if (year == null
                || !academicYearComboBox.getItems()
                .contains(year)) {
            academicYearComboBox.requestFocus();

            throw new IllegalArgumentException(
                    "Select an academic year "
                            + "from the batch table."
            );
        }

        String semester = semesterComboBox.getValue();

        if (semester == null
                || !Timetable.SEMESTERS.contains(semester)) {
            semesterComboBox.requestFocus();

            throw new IllegalArgumentException(
                    "Select Semester 1 or Semester 2."
            );
        }

        Timetable entry = new Timetable(
                0,
                adminId,
                course.getCourseId(),
                course.getCourseCode(),
                course.getCourseName(),
                dayComboBox.getValue(),
                start,
                end,
                venueField.getText().trim(),
                sessionTypeComboBox.getValue(),
                year,
                semester
        );

        Timetable.validate(entry);

        return entry;
    }

    private void fillForm(Timetable timetable) {
        entryIdField.setText(
                String.valueOf(
                        timetable.getTimetableId()
                )
        );

        courseComboBox.getSelectionModel()
                .clearSelection();
        courseComboBox.setValue(null);

        for (Course course : courseComboBox.getItems()) {
            if (course.getCourseId()
                    == timetable.getCourseId()) {
                courseComboBox.setValue(course);
                break;
            }
        }

        dayComboBox.setValue(
                timetable.getDayOfWeek()
        );

        startTimeField.setText(
                displayTime(
                        timetable.getStartTime()
                )
        );

        endTimeField.setText(
                displayTime(
                        timetable.getEndTime()
                )
        );

        venueField.setText(
                timetable.getLectureHall()
        );

        sessionTypeComboBox.setValue(
                timetable.getSessionType()
        );

        boolean yearAvailable = academicYearComboBox
                .getItems()
                .contains(
                        timetable.getAcademicYear()
                );

        academicYearComboBox.getSelectionModel()
                .clearSelection();

        academicYearComboBox.setValue(
                yearAvailable
                        ? timetable.getAcademicYear()
                        : null
        );

        boolean semesterAvailable =
                timetable.getSemester() != null
                        && Timetable.SEMESTERS.contains(
                        timetable.getSemester()
                );

        semesterComboBox.setValue(
                semesterAvailable
                        ? timetable.getSemester()
                        : null
        );

        String message = "Selected entry ID: "
                + timetable.getTimetableId();

        if (!yearAvailable) {
            message += ". Existing academic year "
                    + timetable.getAcademicYear()
                    + " is not in the batch table. "
                    + "Select an available year to update.";
        }

        if (!semesterAvailable) {
            message += " Select Semester 1 "
                    + "or Semester 2 to update.";
        }

        messageLabel.setText(message);
    }

    private String displayTime(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        try {
            return LocalTime.parse(
                    value.trim()
            ).format(TIME_FORMAT);

        } catch (DateTimeParseException e) {
            return value;
        }
    }

    private void showError(String message) {
        messageLabel.setText(message);

        Alert alert = new Alert(
                Alert.AlertType.ERROR
        );

        alert.setTitle("Timetable Management");
        alert.setHeaderText("Operation failed");
        alert.setContentText(message);

        if (timetableTable.getScene() != null) {
            alert.initOwner(
                    timetableTable.getScene()
                            .getWindow()
            );
        }

        alert.showAndWait();
    }
}