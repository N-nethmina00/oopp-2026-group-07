package com.fot.ams.controller;

import com.fot.ams.dao.CourseDAO;
import com.fot.ams.dao.MedicalDAO;
import com.fot.ams.model.Course;
import com.fot.ams.model.MedicalRecord;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// TG2070 - Technical Officer medical page controller
public class MedicalController {

    // TG2070 - ID received from Technical Officer dashboard
    private String technicalOfficerId;

    public void setTechnicalOfficerId(String technicalOfficerId) {
        this.technicalOfficerId = technicalOfficerId;
    }

    @FXML
    private TextField registrationField;

    @FXML
    private ComboBox<Course> medicalCourseComboBox;

    @FXML
    private DatePicker medicalDatePicker;

    @FXML
    private TextArea medicalReasonArea;

    @FXML
    private TextField medicalSearchField;

    @FXML
    private ComboBox<String> medicalStatusFilter;

    @FXML
    private ComboBox<String> medicalUpdateStatusComboBox;

    @FXML
    private TableView<MedicalRecord> medicalTable;

    @FXML
    private TableColumn<MedicalRecord, Integer> medicalIdColumn;

    @FXML
    private TableColumn<MedicalRecord, String> registrationColumn;

    @FXML
    private TableColumn<MedicalRecord, String> courseColumn;

    @FXML
    private TableColumn<MedicalRecord, LocalDate> medicalDateColumn;

    @FXML
    private TableColumn<MedicalRecord, String> reasonColumn;

    @FXML
    private TableColumn<MedicalRecord, String> statusColumn;

    @FXML
    private void initialize() {
        setupTable();

        medicalStatusFilter.getItems().addAll(
                "All statuses",
                "Pending",
                "Approved",
                "Rejected"
        );

        medicalStatusFilter.setValue("All statuses");

        // TG2070 - Status options for the selected record
        medicalUpdateStatusComboBox.getItems().addAll(
                "Pending",
                "Approved",
                "Rejected"
        );

        // TG2070 - Display selected record's current status
        medicalTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldRecord, selectedRecord) -> {
                    if (selectedRecord == null) {
                        medicalUpdateStatusComboBox
                                .getSelectionModel().clearSelection();
                    } else {
                        medicalUpdateStatusComboBox.setValue(
                                selectedRecord.getStatus()
                        );
                    }
                });

        loadCourses();
        loadMedicalRecords();
    }

    // TG2070 - Connect table columns to model getters
    private void setupTable() {
        medicalIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("medicalId")
        );

        registrationColumn.setCellValueFactory(
                new PropertyValueFactory<>("regNo")
        );

        courseColumn.setCellValueFactory(
                new PropertyValueFactory<>("courseCode")
        );

        medicalDateColumn.setCellValueFactory(
                new PropertyValueFactory<>("medicalDate")
        );

        reasonColumn.setCellValueFactory(
                new PropertyValueFactory<>("reason")
        );

        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        medicalTable.setPlaceholder(
                new Label("No medical records found.")
        );
    }

    // TG2070 - Load courses from database
    private void loadCourses() {
        CourseDAO courseDAO = new CourseDAO();

        try {
            List<Course> courses = courseDAO.getAllCourses();

            medicalCourseComboBox.getItems().clear();

            for (Course course : courses) {
                medicalCourseComboBox.getItems().add(course);
            }

        } catch (SQLException exception) {
            showDatabaseError(
                    "Could not load courses.",
                    exception
            );
        }
    }

    // TG2070 - Load and search medical records
    @FXML
    private void loadMedicalRecords() {
        MedicalDAO medicalDAO = new MedicalDAO();

        try {
            ArrayList<MedicalRecord> records =
                    medicalDAO.getMedicalRecords(
                            medicalSearchField.getText(),
                            medicalStatusFilter.getValue()
                    );

            medicalTable.getSelectionModel().clearSelection();
            medicalTable.getItems().clear();

            medicalUpdateStatusComboBox
                    .getSelectionModel().clearSelection();

            for (MedicalRecord record : records) {
                medicalTable.getItems().add(record);
            }

        } catch (SQLException exception) {
            showDatabaseError(
                    "Could not load medical records.",
                    exception
            );
        }
    }

    // TG2070 - Save a new medical record as Pending
    @FXML
    private void saveMedical() {
        String registrationNumber =
                registrationField.getText().trim();

        Course selectedCourse =
                medicalCourseComboBox.getValue();

        LocalDate selectedDate =
                medicalDatePicker.getValue();

        String reason =
                medicalReasonArea.getText().trim();

        if (registrationNumber.isEmpty()
                || selectedCourse == null
                || selectedDate == null
                || reason.isEmpty()) {

            showMessage(
                    Alert.AlertType.WARNING,
                    "Missing Details",
                    "Please enter registration number, course, date and reason."
            );
            return;
        }

        if (selectedDate.isAfter(LocalDate.now())) {
            showMessage(
                    Alert.AlertType.WARNING,
                    "Invalid Date",
                    "Medical date cannot be in the future."
            );
            return;
        }

        // TG2070 - Use integer ID from updated Course model
        int courseId = selectedCourse.getCourseId();

        MedicalDAO medicalDAO = new MedicalDAO();

        try {
            boolean enrolled = medicalDAO.isStudentEnrolled(
                    registrationNumber,
                    courseId
            );

            if (!enrolled) {
                showMessage(
                        Alert.AlertType.WARNING,
                        "Enrollment Not Found",
                        "No eligible enrollment was found for this "
                                + "registration number and course."
                );
                return;
            }

            boolean saved = medicalDAO.addMedical(
                    registrationNumber,
                    courseId,
                    selectedDate,
                    reason
            );

            if (saved) {
                showMessage(
                        Alert.AlertType.INFORMATION,
                        "Medical Saved",
                        "Medical record saved with Pending status."
                );

                clearForm();
                loadMedicalRecords();

            } else {
                showMessage(
                        Alert.AlertType.WARNING,
                        "Not Saved",
                        "Medical record was not saved."
                );
            }

        } catch (SQLException exception) {
            showDatabaseError(
                    "Could not save medical record.",
                    exception
            );
        }
    }

    // TG2070 - Update selected medical record status
    @FXML
    private void updateMedicalStatus() {
        MedicalRecord selectedRecord =
                medicalTable.getSelectionModel().getSelectedItem();

        String selectedStatus =
                medicalUpdateStatusComboBox.getValue();

        if (selectedRecord == null) {
            showMessage(
                    Alert.AlertType.WARNING,
                    "No Record Selected",
                    "Please select a medical record from the table."
            );
            return;
        }

        if (selectedStatus == null) {
            showMessage(
                    Alert.AlertType.WARNING,
                    "No Status Selected",
                    "Please select a status."
            );
            return;
        }

        if (technicalOfficerId == null
                || technicalOfficerId.isBlank()) {

            showMessage(
                    Alert.AlertType.WARNING,
                    "Login Required",
                    "Please log in as a Technical Officer."
            );
            return;
        }

        // Avoid updating an unchanged status
        if (selectedStatus.equals(selectedRecord.getStatus())) {
            showMessage(
                    Alert.AlertType.INFORMATION,
                    "No Change",
                    "This record already has the selected status."
            );
            return;
        }

        MedicalDAO medicalDAO = new MedicalDAO();

        try {
            boolean updated = medicalDAO.updateMedicalStatus(
                    selectedRecord.getMedicalId(),
                    selectedStatus,
                    technicalOfficerId
            );

            if (updated) {
                showMessage(
                        Alert.AlertType.INFORMATION,
                        "Status Updated",
                        "Medical status updated successfully."
                );

                loadMedicalRecords();

            } else {
                showMessage(
                        Alert.AlertType.WARNING,
                        "Not Updated",
                        "No record was updated. Refresh the records "
                                + "and check your Technical Officer account."
                );
            }

        } catch (SQLException exception) {
            showDatabaseError(
                    "Could not update medical status.",
                    exception
            );
        }
    }

    // TG2070 - Clear medical entry form
    @FXML
    private void clearForm() {
        registrationField.clear();
        medicalCourseComboBox.getSelectionModel().clearSelection();
        medicalDatePicker.setValue(null);
        medicalReasonArea.clear();
        registrationField.requestFocus();
    }

    // TG2070 - Display information and validation messages
    private void showMessage(Alert.AlertType type,
                             String title,
                             String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // TG2070 - Display database errors
    private void showDatabaseError(String message,
                                   SQLException exception) {
        exception.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(message);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }
}