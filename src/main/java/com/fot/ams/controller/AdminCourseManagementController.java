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

    @FXML private TextField courseIdField;
    @FXML private TextField courseNameField;
    @FXML private TextField creditField;

    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> departmentComboBox;
    @FXML private ComboBox<String> lecturerComboBox;

    @FXML private Label messageLabel;

    @FXML private TableView<Course> coursesTable;

    @FXML private TableColumn<Course, String> courseIdColumn;
    @FXML private TableColumn<Course, String> courseNameColumn;
    @FXML private TableColumn<Course, String> departmentColumn;
    @FXML private TableColumn<Course, Integer> creditsColumn;
    @FXML private TableColumn<Course, String> typeColumn;
    @FXML private TableColumn<Course, String> lecturerColumn;

    private final CourseDAO dao = new CourseDAO();

    private static final double TABLE_ROW_HEIGHT = 38.0;
    private static final double TABLE_HEADER_HEIGHT = 40.0;

    private final ListChangeListener<Course> tableRowsListener =
            change -> updateTableHeight();

    @FXML
    public void initialize() {
        configureTableColumns();
        configureTableHeight();

        courseIdField.setPromptText("Example: ICT1124");
        courseIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()
                    || Course.isValidCourseCode(newValue)) {
                courseIdField.setStyle("");
            }
        });

        typeComboBox.setItems(
                FXCollections.observableArrayList(
                        "Theory",
                        "Practical"
                )
        );

        coursesTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, selected) -> {
                    if (selected != null) {
                        fillForm(selected);
                    }
                });

        loadOptions();
        loadCourses();
    }

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

        coursesTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
    }

    private void configureTableHeight() {
        coursesTable.setFixedCellSize(TABLE_ROW_HEIGHT);

        // The outer ScrollPane handles vertical scrolling.
        coursesTable.setMinHeight(Region.USE_PREF_SIZE);
        coursesTable.setMaxHeight(Region.USE_PREF_SIZE);

        if (coursesTable.getItems() != null) {
            coursesTable.getItems().addListener(tableRowsListener);
        }

        // Observe the new list when loadCourses replaces table items.
        coursesTable.itemsProperty().addListener(
                (observable, oldItems, newItems) -> {
                    if (oldItems != null) {
                        oldItems.removeListener(tableRowsListener);
                    }

                    if (newItems != null) {
                        newItems.addListener(tableRowsListener);
                    }

                    updateTableHeight();
                }
        );

        updateTableHeight();
    }

    private void updateTableHeight() {
        int rowCount = coursesTable.getItems() == null
                ? 0
                : coursesTable.getItems().size();

        // Leave space for the placeholder when there are no courses.
        int visibleRows = Math.max(rowCount, 1);

        double tableHeight =
                visibleRows * TABLE_ROW_HEIGHT
                        + TABLE_HEADER_HEIGHT;

        coursesTable.setPrefHeight(tableHeight);
    }

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

    @FXML
    private void handleAdd() {
        try {
            if (coursesTable.getSelectionModel().getSelectedItem() != null) {
                showError("Click Clear before adding a new course.");
                return;
            }

            Course course = readForm();

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

    @FXML
    private void handleUpdate() {
        try {
            Course selected = coursesTable
                    .getSelectionModel()
                    .getSelectedItem();

            if (selected == null) {
                showError("Select a course from the table first.");
                return;
            }

            Course course = readForm();

            if (!course.getCourseCode()
                    .equals(selected.getCourseCode())) {

                showError("Course code cannot be changed during update.");
                return;
            }

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

    @FXML
    private void handleDelete() {
        Course selected = coursesTable
                .getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showError("Select a course from the table first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Delete Course");
        confirm.setHeaderText(
                "Delete " + selected.getCourseCode() + "?"
        );

        confirm.setContentText(
                "Related records may also be deleted "
                        + "due to database foreign keys."
        );

        if (confirm.showAndWait()
                .orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
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

    @FXML
    private void handleClear() {
        courseIdField.clear();
        courseNameField.clear();
        creditField.clear();

        typeComboBox.getSelectionModel().clearSelection();
        departmentComboBox.getSelectionModel().clearSelection();
        lecturerComboBox.getSelectionModel().clearSelection();

        coursesTable.getSelectionModel().clearSelection();

        courseIdField.setEditable(true);
        courseIdField.setStyle("");
        messageLabel.setText("");
    }

    private Course readForm() {
        // Validate the exact input; do not silently trim or uppercase it.
        String code = courseIdField.getText();

        if (!Course.isValidCourseCode(code)) {
            courseIdField.setStyle(
                    "-fx-border-color: #B42318; -fx-border-radius: 4;"
            );
            courseIdField.requestFocus();
            courseIdField.selectAll();
            throw new IllegalArgumentException(Course.COURSE_CODE_ERROR);
        }
        String name = courseNameField.getText().trim();
        String creditText = creditField.getText().trim();

        String type = typeComboBox.getValue();
        String department = departmentComboBox.getValue();
        String lecturer = lecturerComboBox.getValue();

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
            credits = Integer.parseInt(creditText);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Credits must be a whole number."
            );
        }

        if (credits < 1 || credits > 10) {
            throw new IllegalArgumentException(
                    "Credits must be between 1 and 10."
            );
        }

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

    private void fillForm(Course course) {
        courseIdField.setText(course.getCourseCode());
        courseNameField.setText(course.getCourseName());
        creditField.setText(String.valueOf(course.getCredit()));

        typeComboBox.setValue(course.getType());
        departmentComboBox.setValue(course.getDepartmentName());
        lecturerComboBox.setValue(course.getLecturerId());

        courseIdField.setEditable(false);
    }

    private void showError(String message) {
        messageLabel.setText(message);

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Course Management");
        alert.setHeaderText("Operation failed");
        alert.setContentText(message);
        if (courseIdField.getScene() != null) {
            alert.initOwner(courseIdField.getScene().getWindow());
        }
        alert.showAndWait();

        if (!Course.isValidCourseCode(courseIdField.getText())) {
            courseIdField.requestFocus();
        }
    }
}
