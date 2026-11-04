package com.fot.ams.controller;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.model.Course;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;

import java.sql.SQLException;

public class AdminCourseManagementController {

    // Input fields for course details.
    @FXML private TextField courseIdField;
    @FXML private TextField courseNameField;
    @FXML private TextField creditField;

    // Dropdowns for choosing course type, department and lecturer.
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> departmentComboBox;
    @FXML private ComboBox<String> lecturerComboBox;

    // Displays status and error messages.
    @FXML private Label messageLabel;

    // Displays the courses loaded from the database.
    @FXML private TableView<Course> coursesTable;

    // Columns that display each course detail.
    @FXML private TableColumn<Course, String> courseIdColumn;
    @FXML private TableColumn<Course, String> courseNameColumn;
    @FXML private TableColumn<Course, String> departmentColumn;
    @FXML private TableColumn<Course, Integer> creditsColumn;
    @FXML private TableColumn<Course, String> typeColumn;
    @FXML private TableColumn<Course, String> lecturerColumn;

    // Handles course-related database operations.
    private final CourseDAO dao = new CourseDAO();

    // Heights used to calculate the table's total height.
    private static final double TABLE_ROW_HEIGHT = 38.0;
    private static final double TABLE_HEADER_HEIGHT = 40.0;

    // Updates the table height whenever its rows change.
    private final ListChangeListener<Course> tableRowsListener =
            change -> updateTableHeight();

    // JavaFX calls this after loading the FXML controls.
    @FXML
    public void initialize() {
        // Connect each table column to a Course property.
        configureTableColumns();

        // Set the table height based on the number of courses.
        configureTableHeight();

        // Show an example format in the course-code field.
        courseIdField.setPromptText("Example: ICT1124");

        // Clear the red error style when the course code becomes valid.
        courseIdField.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == null || newValue.isEmpty()
                            || Course.isValidCourseCode(newValue)) {
                        courseIdField.setStyle("");
                    }
                }
        );

        // Add the available course types to the dropdown.
        typeComboBox.setItems(
                FXCollections.observableArrayList(
                        "Theory",
                        "Practical"
                )
        );

        // Show the selected course's details in the form.
        coursesTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, selected) -> {
                    if (selected != null) {
                        fillForm(selected);
                    }
                });

        // Load dropdown options and course records from the database.
        loadOptions();
        loadCourses();
    }

    // Connects each table column to the matching Course getter.
    private void configureTableColumns() {
        courseIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("courseCode")
        );

        courseNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("courseName")
        );

        departmentColumn.setCellValueFactory(
                new PropertyValueFactory<>("departmentName")
        );

        creditsColumn.setCellValueFactory(
                new PropertyValueFactory<>("credit")
        );

        typeColumn.setCellValueFactory(
                new PropertyValueFactory<>("type")
        );

        lecturerColumn.setCellValueFactory(
                new PropertyValueFactory<>("lecturerId")
        );

        // Let the table columns share the available width.
        coursesTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
    }

    // Sets up the table so its height fits its rows.
    private void configureTableHeight() {
        coursesTable.setFixedCellSize(TABLE_ROW_HEIGHT);

        // The outer ScrollPane handles vertical scrolling.
        coursesTable.setMinHeight(Region.USE_PREF_SIZE);
        coursesTable.setMaxHeight(Region.USE_PREF_SIZE);

        // Listen for changes to the table rows.
        if (coursesTable.getItems() != null) {
            coursesTable.getItems().addListener(tableRowsListener);
        }

        // Listen when the table receives a different list of courses.
        coursesTable.itemsProperty().addListener(
                (observable, oldItems, newItems) -> {
                    // Stop listening to the old list.
                    if (oldItems != null) {
                        oldItems.removeListener(tableRowsListener);
                    }

                    // Listen to changes in the new list.
                    if (newItems != null) {
                        newItems.addListener(tableRowsListener);
                    }

                    updateTableHeight();
                }
        );

        updateTableHeight();
    }

    // Calculates the table height from the number of rows.
    private void updateTableHeight() {
        int rowCount = coursesTable.getItems() == null
                ? 0
                : coursesTable.getItems().size();

        // Keep enough space to display the empty-table message.
        int visibleRows = Math.max(rowCount, 1);

        double tableHeight =
                visibleRows * TABLE_ROW_HEIGHT
                        + TABLE_HEADER_HEIGHT;

        coursesTable.setPrefHeight(tableHeight);
    }

    // Loads department and lecturer choices for the dropdowns.
    private void loadOptions() {
        try {
            departmentComboBox.setItems(
                    FXCollections.observableArrayList(
                            dao.getDepartments()
                    )
            );

            lecturerComboBox.setItems(
                    FXCollections.observableArrayList(
                            dao.getLecturers()
                    )
            );

        } catch (SQLException e) {
            showError(
                    "Unable to load departments or lecturers: "
                            + e.getMessage()
            );
        }
    }

    // Loads all courses from the database into the table.
    private void loadCourses() {
        try {
            coursesTable.setItems(
                    FXCollections.observableArrayList(
                            dao.getAllCourses()
                    )
            );

            messageLabel.setText("Courses loaded successfully.");

        } catch (SQLException e) {
            showError(
                    "Unable to load courses: " + e.getMessage()
            );
        }
    }

    // Validates the form and adds a new course.
    @FXML
    private void handleAdd() {
        try {
            // Require the user to clear the selection before adding.
            if (coursesTable.getSelectionModel().getSelectedItem() != null) {
                showError("Click Clear before adding a new course.");
                return;
            }

            // Read and validate the values entered in the form.
            Course course = readForm();

            // Save the course and refresh the table if successful.
            if (dao.addCourse(course)) {
                loadCourses();
                handleClear();

                messageLabel.setText("Course added successfully.");
            }

        } catch (Exception e) {
            showError(
                    "Unable to add course: " + e.getMessage()
            );
        }
    }

    // Validates the form and updates the selected course.
    @FXML
    private void handleUpdate() {
        try {
            Course selected = coursesTable
                    .getSelectionModel()
                    .getSelectedItem();

            // A course must be selected before updating.
            if (selected == null) {
                showError("Select a course from the table first.");
                return;
            }

            Course course = readForm();

            // Keep the course code unchanged during an update.
            if (!course.getCourseCode()
                    .equals(selected.getCourseCode())) {

                showError("Course code cannot be changed during update.");
                return;
            }

            // Update the course and reload the table if successful.
            if (dao.updateCourse(course)) {
                loadCourses();

                messageLabel.setText("Course updated successfully.");
            }

        } catch (Exception e) {
            showError(
                    "Unable to update course: " + e.getMessage()
            );
        }
    }

    // Confirms and deletes the selected course.
    @FXML
    private void handleDelete() {
        Course selected = coursesTable
                .getSelectionModel()
                .getSelectedItem();

        // A course must be selected before deleting.
        if (selected == null) {
            showError("Select a course from the table first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Delete Course");
        confirm.setHeaderText(
                "Delete " + selected.getCourseCode() + "?"
        );

        // Warn that related records may also be deleted.
        confirm.setContentText(
                "Related records may also be deleted "
                        + "due to database foreign keys."
        );

        // Stop if the user cancels the confirmation.
        if (confirm.showAndWait()
                .orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            // Delete the course and refresh the screen if successful.
            if (dao.deleteCourse(selected.getCourseCode())) {
                loadCourses();
                handleClear();

                messageLabel.setText("Course deleted successfully.");
            }

        } catch (SQLException e) {
            showError(
                    "Unable to delete course: " + e.getMessage()
            );
        }
    }

    // Clears the form and removes the selected table row.
    @FXML
    private void handleClear() {
        courseIdField.clear();
        courseNameField.clear();
        creditField.clear();

        typeComboBox.getSelectionModel().clearSelection();
        departmentComboBox.getSelectionModel().clearSelection();
        lecturerComboBox.getSelectionModel().clearSelection();

        coursesTable.getSelectionModel().clearSelection();

        // Allow a new course code to be entered.
        courseIdField.setEditable(true);
        courseIdField.setStyle("");

        messageLabel.setText("");
    }

    // Reads the form and checks that all course details are valid.
    private Course readForm() {
        // Check the exact course-code text without changing its case or spaces.
        String code = courseIdField.getText();

        if (!Course.isValidCourseCode(code)) {
            // Highlight the invalid code and move the cursor to that field.
            courseIdField.setStyle(
                    "-fx-border-color: #B42318; -fx-border-radius: 4;"
            );
            courseIdField.requestFocus();
            courseIdField.selectAll();

            throw new IllegalArgumentException(Course.COURSE_CODE_ERROR);
        }

        // Read the other form values.
        String name = courseNameField.getText().trim();
        String creditText = creditField.getText().trim();

        String type = typeComboBox.getValue();
        String department = departmentComboBox.getValue();
        String lecturer = lecturerComboBox.getValue();

        // Require every field to have a value.
        if (code.isEmpty()
                || name.isEmpty()
                || creditText.isEmpty()
                || type == null
                || department == null
                || lecturer == null) {

            throw new IllegalArgumentException(
                    "Please complete all fields."
            );
        }

        int credits;

        try {
            // Convert the credit text into a whole number.
            credits = Integer.parseInt(creditText);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Credits must be a whole number."
            );
        }

        // Allow credit values from 1 to 10.
        if (credits < 1 || credits > 10) {
            throw new IllegalArgumentException(
                    "Credits must be between 1 and 10."
            );
        }

        // Create a Course object from the form values.
        // The DAO handles the database operation for this object.
        return new Course(
                0,
                code,
                name,
                credits,
                type,
                lecturer,
                0,
                department
        );
    }

    // Copies the selected course's details into the form.
    private void fillForm(Course course) {
        courseIdField.setText(course.getCourseCode());
        courseNameField.setText(course.getCourseName());
        creditField.setText(String.valueOf(course.getCredit()));

        typeComboBox.setValue(course.getType());
        departmentComboBox.setValue(course.getDepartmentName());
        lecturerComboBox.setValue(course.getLecturerId());

        // Keep the course code fixed while this course is being edited.
        courseIdField.setEditable(false);
    }

    // Displays an error message on the screen and in a dialog.
    private void showError(String message) {
        messageLabel.setText(message);

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Course Management");
        alert.setHeaderText("Operation failed");
        alert.setContentText(message);

        // Attach the dialog to this screen's window if available.
        if (courseIdField.getScene() != null) {
            alert.initOwner(courseIdField.getScene().getWindow());
        }

        alert.showAndWait();

        // Focus the course-code field if its current value is invalid.
        if (!Course.isValidCourseCode(courseIdField.getText())) {
            courseIdField.requestFocus();
        }
    }
}