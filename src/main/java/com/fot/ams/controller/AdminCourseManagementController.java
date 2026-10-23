package com.fot.ams.controller;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.model.Course;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

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

    @FXML
    public void initialize() {
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

        typeComboBox.setItems(
                FXCollections.observableArrayList("Theory", "Practical")
        );

        coursesTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, selected) -> {
                    if (selected != null) {
                        fillForm(selected);
                    }
                });

        loadOptions();
        loadCourses();
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
            showError("Unable to load courses: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        try {
            Course course = readForm();

            if (dao.addCourse(course)) {
                loadCourses();
                handleClear();
                messageLabel.setText("Course added successfully.");
            }

        } catch (Exception e) {
            showError("Unable to add course: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        try {
            Course selected =
                    coursesTable.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showError("Select a course from the table first.");
                return;
            }

            Course course = readForm();

            if (!course.getCourseCode().equals(selected.getCourseCode())) {
                showError("Course ID cannot be changed during update.");
                return;
            }

            if (dao.updateCourse(course)) {
                loadCourses();
                messageLabel.setText("Course updated successfully.");
            }

        } catch (Exception e) {
            showError("Unable to update course: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Course selected =
                coursesTable.getSelectionModel().getSelectedItem();

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
                "Related records may also be deleted due to database foreign keys."
        );

        if (confirm.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return;
        }

        try {
            if (dao.deleteCourse(selected.getCourseCode())) {
                loadCourses();
                handleClear();
                messageLabel.setText("Course deleted successfully.");
            }

        } catch (SQLException e) {
            showError("Unable to delete course: " + e.getMessage());
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
        messageLabel.setText("");
    }

    private Course readForm() {
        String code = courseIdField.getText().trim();
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

        if (!code.matches("[A-Za-z0-9-]{2,20}")) {
            throw new IllegalArgumentException(
                    "Enter a valid course code."
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
        alert.showAndWait();
    }
}