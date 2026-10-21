package com.fot.ams.controller;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.model.Course;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class AdminCourseManagementController {

    @FXML private TextField courseIdField;
    @FXML private TextField courseNameField;
    @FXML private TextField creditField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> departmentComboBox;
    @FXML private ComboBox<String> lecturerComboBox;
    @FXML private TableView<Course> coursesTable;
    @FXML private TableColumn<Course, String> courseIdColumn;
    @FXML private TableColumn<Course, String> courseNameColumn;
    @FXML private TableColumn<Course, String> departmentColumn;
    @FXML private TableColumn<Course, Number> creditsColumn;
    @FXML private TableColumn<Course, String> typeColumn;
    @FXML private TableColumn<Course, String> lecturerColumn;
    @FXML private Label messageLabel;

    private final CourseDAO courseDAO = new CourseDAO();
    private final ObservableList<Course> courseList =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        courseIdColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCourseId()));

        courseNameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCourseName()));

        departmentColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDepartmentName()));

        creditsColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getCredit()));

        typeColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getType()));

        lecturerColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getLecturerId()));

        typeComboBox.setItems(FXCollections.observableArrayList(
                "Theory", "Practical"
        ));

        courseIdField.textProperty().addListener((obs, oldValue, newValue) ->
                courseIdField.setText(newValue.toUpperCase().trim()));

        coursesTable.setItems(courseList);
        coursesTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        coursesTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldCourse, selectedCourse) -> {
                    if (selectedCourse != null) {
                        showCourse(selectedCourse);
                    }
                });

        loadDropdowns();
        loadCourses();
    }

    private void loadDropdowns() {
        try {
            departmentComboBox.setItems(
                    FXCollections.observableArrayList(
                            courseDAO.getDepartmentNames()
                    )
            );

            lecturerComboBox.setItems(
                    FXCollections.observableArrayList(
                            courseDAO.getLecturerIds()
                    )
            );
        } catch (SQLException e) {
            showError("Could not load departments or lecturers: " + e.getMessage());
        }
    }

    private void loadCourses() {
        try {
            courseList.setAll(courseDAO.getAllCourses());
            messageLabel.setText("Loaded " + courseList.size() + " courses.");
        } catch (SQLException e) {
            showError("Could not load courses: " + e.getMessage());
        }
    }

    private void showCourse(Course course) {
        courseIdField.setText(course.getCourseId());
        courseNameField.setText(course.getCourseName());
        creditField.setText(String.valueOf(course.getCredit()));
        typeComboBox.setValue(course.getType());
        departmentComboBox.setValue(course.getDepartmentName());
        lecturerComboBox.setValue(course.getLecturerId());
        courseIdField.setEditable(false);
        messageLabel.setText("Selected course: " + course.getCourseId());
    }

    @FXML
    private void handleAdd() {
        if (!validateForm()) {
            return;
        }

        try {
            String courseId = courseIdField.getText().trim().toUpperCase();

            if (courseDAO.courseExists(courseId)) {
                showError("This Course ID already exists.");
                return;
            }

            Course course = buildCourse();
            boolean added = courseDAO.addCourse(course);

            if (added) {
                loadCourses();
                clearForm();
                showInfo("Course added successfully.");
            } else {
                showError("Course could not be added.");
            }
        } catch (SQLException e) {
            showError("Add failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (!validateForm()) {
            return;
        }

        if (coursesTable.getSelectionModel().getSelectedItem() == null) {
            showError("Select a course from the table before updating.");
            return;
        }

        try {
            boolean updated = courseDAO.updateCourse(buildCourse());

            if (updated) {
                loadCourses();
                clearForm();
                showInfo("Course updated successfully.");
            } else {
                showError("No matching course was found.");
            }
        } catch (SQLException e) {
            showError("Update failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Course selected = coursesTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Select a course from the table before deleting.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Course");
        confirmation.setHeaderText("Delete " + selected.getCourseId() + "?");
        confirmation.setContentText(
                "This action may be blocked if other records reference this course."
        );

        if (confirmation.showAndWait().orElse(
                javafx.scene.control.ButtonType.CANCEL
        ) != javafx.scene.control.ButtonType.OK) {
            return;
        }

        try {
            boolean deleted = courseDAO.deleteCourse(selected.getCourseId());

            if (deleted) {
                loadCourses();
                clearForm();
                showInfo("Course deleted successfully.");
            } else {
                showError("Course could not be deleted.");
            }
        } catch (SQLException e) {
            showError("Delete failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
        coursesTable.getSelectionModel().clearSelection();
        messageLabel.setText("");
    }

    private Course buildCourse() throws SQLException {
        int credit = Integer.parseInt(creditField.getText().trim());
        int departmentId = courseDAO.getDepartmentId(
                departmentComboBox.getValue()
        );

        return new Course(
                courseIdField.getText().trim().toUpperCase(),
                courseNameField.getText().trim(),
                credit,
                typeComboBox.getValue(),
                lecturerComboBox.getValue(),
                departmentId,
                departmentComboBox.getValue()
        );
    }

    private boolean validateForm() {
        if (courseIdField.getText().isBlank()
                || courseNameField.getText().isBlank()
                || creditField.getText().isBlank()
                || typeComboBox.getValue() == null
                || departmentComboBox.getValue() == null
                || lecturerComboBox.getValue() == null) {
            showError("Please complete all fields.");
            return false;
        }

        if (!courseIdField.getText().trim().matches("[A-Za-z0-9-]{3,20}")) {
            showError("Course ID must contain 3–20 letters, digits or hyphens.");
            return false;
        }

        try {
            int credit = Integer.parseInt(creditField.getText().trim());

            if (credit < 1 || credit > 10) {
                showError("Credits must be between 1 and 10.");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Credits must be a whole number.");
            return false;
        }

        return true;
    }

    private void clearForm() {
        courseIdField.clear();
        courseNameField.clear();
        creditField.clear();
        typeComboBox.getSelectionModel().clearSelection();
        departmentComboBox.getSelectionModel().clearSelection();
        lecturerComboBox.getSelectionModel().clearSelection();
        courseIdField.setEditable(true);
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Course Management");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        messageLabel.setText(message);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Course Management");
        alert.setHeaderText("Operation failed");
        alert.setContentText(message);
        alert.showAndWait();
    }
}