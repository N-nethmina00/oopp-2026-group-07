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

    // Connects the entry ID field and form controls to the FXML file.
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

    // Table that displays the timetable entries.
    @FXML private TableView<Timetable> timetableTable;

    // Table columns for each timetable detail.
    @FXML private TableColumn<Timetable, Integer> entryIdColumn;
    @FXML private TableColumn<Timetable, String> courseColumn;
    @FXML private TableColumn<Timetable, String> dayColumn;
    @FXML private TableColumn<Timetable, String> timeColumn;
    @FXML private TableColumn<Timetable, String> venueColumn;
    @FXML private TableColumn<Timetable, String> sessionTypeColumn;
    @FXML private TableColumn<Timetable, String> yearColumn;
    @FXML private TableColumn<Timetable, String> semesterColumn;

    // Handles timetable-related database operations.
    private final TimetableDAO dao = new TimetableDAO();

    // Stores timetable entries and updates the table when they change.
    private final ObservableList<Timetable> timetableList =
            FXCollections.observableArrayList();

    /*
     * Default admin ID used by the supplied database.
     * The dashboard can replace it by calling setAdminId().
     */
    private String adminId = "A001";

    // Values used to calculate the table's height.
    private static final double ROW_HEIGHT = 38.0;
    private static final double HEADER_SPACE = 48.0;

    // Displays times in 24-hour HH:mm format.
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    // Receives and checks the logged-in administrator's role ID.
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

    // JavaFX calls this after loading the FXML controls.
    @FXML
    public void initialize() {
        // Connect table columns to Timetable properties.
        configureColumns();

        // Control how courses appear in the course dropdown.
        configureCourseDropdown();

        // The database assigns the entry ID, so it cannot be edited here.
        entryIdField.setEditable(false);

        // Add the allowed days to the dropdown.
        dayComboBox.setItems(
                FXCollections.observableArrayList(
                        Timetable.DAYS
                )
        );

        // Add the allowed session types to the dropdown.
        sessionTypeComboBox.setItems(
                FXCollections.observableArrayList(
                        Timetable.SESSION_TYPES
                )
        );

        // Add the supported semesters to the dropdown.
        semesterComboBox.setItems(
                FXCollections.observableArrayList(
                        Timetable.SEMESTERS
                )
        );

        // Display the timetable list in the table.
        timetableTable.setItems(timetableList);

        // Make the table height fit its rows.
        configureTableHeight();

        // Fill the form when a table row is selected.
        timetableTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, selected) -> {
                    if (selected != null) {
                        fillForm(selected);
                    }
                });

        // Require academic year and semester to be selected from their lists.
        academicYearComboBox.setEditable(false);
        semesterComboBox.setEditable(false);

        // Load dropdown options and timetable entries from the database.
        loadCourses();
        loadAcademicYears();
        loadTimetables();
        refreshNextEntryId();
    }

    // Connects each table column to the matching Timetable value.
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

        // Show the start and end times together in one column.
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

        // Let the table columns share the available width.
        timetableTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        // Show this text if the table has no entries.
        timetableTable.setPlaceholder(
                new Label("No timetable entries available.")
        );
    }

    // Makes courses display as "course code - course name".
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
                        // Users select a course; they do not type a new one.
                        return null;
                    }
                }
        );
    }

    // Sets the table height based on the number of entries.
    private void configureTableHeight() {
        timetableTable.setFixedCellSize(ROW_HEIGHT);

        // Prevent the parent layout from shrinking the table.
        timetableTable.setMinHeight(
                Region.USE_PREF_SIZE
        );

        timetableTable.setMaxHeight(
                Region.USE_PREF_SIZE
        );

        /*
         * The table grows to fit its rows.
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

    // Loads academic years for the academic-year dropdown.
    private void loadAcademicYears() {
        try {
            academicYearComboBox.setItems(
                    FXCollections.observableArrayList(
                            dao.getAcademicYears()
                    )
            );

            // Tell the user if there are no years to choose from.
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

    // Loads courses for the course dropdown.
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

    // Loads all timetable entries into the table list.
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

    // Validates the form and adds a timetable entry.
    @FXML
    private void handleAdd() {
        try {
            // Require Clear to be clicked before adding after selecting a row.
            if (timetableTable.getSelectionModel()
                    .getSelectedItem() != null) {
                showError(
                        "Click Clear before adding "
                                + "a new timetable entry."
                );
                return;
            }

            // Read the form and validate its values.
            Timetable timetable = readForm();

            // Save the entry and get the ID assigned by the database.
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

    // Updates the selected timetable entry.
    @FXML
    private void handleUpdate() {
        Timetable selected = timetableTable
                .getSelectionModel()
                .getSelectedItem();

        // A table row must be selected before updating.
        if (selected == null) {
            showError(
                    "Select a timetable entry first."
            );
            return;
        }

        try {
            // Read and validate the edited form values.
            Timetable form = readForm();

            // Keep the selected entry's ID and admin ID.
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

            // Save changes and refresh the table.
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

    // Confirms and deletes the selected timetable entry.
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

        // Ask before permanently deleting the entry.
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

        // Stop if the user cancels the confirmation.
        if (confirm.showAndWait()
                .orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            // Delete the selected entry from the database.
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

    // Clears all form values and removes the selected row.
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

        // Show the next ID after clearing the form.
        refreshNextEntryId();
    }

    // Gets the next timetable ID from the database.
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

    // Reads the form, checks its values and creates a Timetable object.
    private Timetable readForm() {
        Course course = courseComboBox.getValue();

        // Require a course to be selected.
        if (course == null) {
            courseComboBox.requestFocus();

            throw new IllegalArgumentException(
                    "Select a course."
            );
        }

        // Read the entered start and end times.
        String start = startTimeField
                .getText()
                .trim();

        String end = endTimeField
                .getText()
                .trim();

        // Require times in 24-hour HH:mm format.
        if (!start.matches("[0-9]{2}:[0-9]{2}")
                || !end.matches("[0-9]{2}:[0-9]{2}")) {
            throw new IllegalArgumentException(
                    "Enter times in 24-hour HH:mm format, "
                            + "for example 08:30."
            );
        }

        // Make sure the academic year is one of the available choices.
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

        // Make sure a supported semester is selected.
        String semester = semesterComboBox.getValue();

        if (semester == null
                || !Timetable.SEMESTERS.contains(semester)) {
            semesterComboBox.requestFocus();

            throw new IllegalArgumentException(
                    "Select Semester 1 or Semester 2."
            );
        }

        // Create a timetable object using the form values.
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

        // Run the model's timetable validation checks.
        Timetable.validate(entry);

        return entry;
    }

    // Copies the selected timetable entry into the form.
    private void fillForm(Timetable timetable) {
        entryIdField.setText(
                String.valueOf(
                        timetable.getTimetableId()
                )
        );

        // Find the matching course in the dropdown.
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

        // Display the selected entry's details in the form.
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

        // Only select the academic year if it is in the dropdown.
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

        // Only select a semester supported by the application.
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

        // Tell the user if old data needs a new academic year or semester.
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

    // Formats a time as HH:mm, while keeping unrecognized values unchanged.
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

    // Displays an error in the label and in an alert dialog.
    private void showError(String message) {
        messageLabel.setText(message);

        Alert alert = new Alert(
                Alert.AlertType.ERROR
        );

        alert.setTitle("Timetable Management");
        alert.setHeaderText("Operation failed");
        alert.setContentText(message);

        // Attach the alert to this screen's window when possible.
        if (timetableTable.getScene() != null) {
            alert.initOwner(
                    timetableTable.getScene()
                            .getWindow()
            );
        }

        alert.showAndWait();
    }
}