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

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// TG2070 - Manage medical records, statuses and attached documents.
public class MedicalController {

    // Receive the logged-in Technical Officer ID.
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

    // Set up the table, status lists and initial data.
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

        medicalUpdateStatusComboBox.getItems().addAll(
                "Pending",
                "Approved",
                "Rejected"
        );

        // Show the selected record's status in the update dropdown.
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

    // Connect table columns to MedicalRecord getters.
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

    // Load courses through CourseDAO.
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

    // Load records using the registration search and status filter.
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

    // Validate the form and save a new Pending medical record.
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

        int courseId = selectedCourse.getCourseId();

        MedicalDAO medicalDAO = new MedicalDAO();

        try {
            // Check the student's course enrollment before saving.
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

    // Check the selection and officer ID before updating the status.
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

    // Open the selected record's attachment using the computer's viewer.
    @FXML
    private void viewMedicalDocument() {
        if (technicalOfficerId == null
                || technicalOfficerId.isBlank()) {

            showMessage(
                    Alert.AlertType.WARNING,
                    "Login Required",
                    "Please log in as a Technical Officer."
            );
            return;
        }

        MedicalRecord selectedRecord =
                medicalTable.getSelectionModel().getSelectedItem();

        if (selectedRecord == null) {
            showMessage(
                    Alert.AlertType.WARNING,
                    "No Record Selected",
                    "Please select a medical record from the table."
            );
            return;
        }

        String documentPath = selectedRecord.getDocumentPath();

        if (documentPath == null || documentPath.isBlank()) {
            showMessage(
                    Alert.AlertType.INFORMATION,
                    "No Document",
                    "No medical document is attached to this record."
            );
            return;
        }

        try {
            Path document = resolveMedicalDocument(documentPath);

            String fileName = document.getFileName()
                    .toString()
                    .toLowerCase(Locale.ROOT);

            // Allow only PDF, PNG and JPG file extensions.
            if (!fileName.endsWith(".pdf")
                    && !fileName.endsWith(".png")
                    && !fileName.endsWith(".jpg")
                    && !fileName.endsWith(".jpeg")) {

                showMessage(
                        Alert.AlertType.WARNING,
                        "Unsupported Document",
                        "Only PDF, PNG and JPG medical documents can be opened."
                );
                return;
            }

            // The actual file must exist and be readable on this computer.
            if (!Files.isRegularFile(document)
                    || !Files.isReadable(document)) {

                showMessage(
                        Alert.AlertType.WARNING,
                        "Document Not Found",
                        "The attached file is not available on this computer.\n\n"
                                + "Expected location:\n" + document
                                + "\n\nThe actual file must be available "
                                + "as well as its database path."
                );
                return;
            }

            if (!Desktop.isDesktopSupported()) {
                showMessage(
                        Alert.AlertType.WARNING,
                        "Document Viewer",
                        "Opening documents is not supported on this computer."
                );
                return;
            }

            Desktop desktop = Desktop.getDesktop();

            if (!desktop.isSupported(Desktop.Action.OPEN)) {
                showMessage(
                        Alert.AlertType.WARNING,
                        "Document Viewer",
                        "No supported document opening action is available."
                );
                return;
            }

            desktop.open(document.toFile());

        } catch (IOException
                 | IllegalArgumentException
                 | UnsupportedOperationException
                 | SecurityException exception) {

            exception.printStackTrace();

            showMessage(
                    Alert.AlertType.ERROR,
                    "Document Error",
                    "Could not open the medical document.\n"
                            + "Check the file and its default viewer.\n\n"
                            + exception.getMessage()
            );
        }
    }

    // Convert the database path into a local file path.
    private Path resolveMedicalDocument(String documentPath)
            throws IOException {

        Path projectFolder = Path.of(
                System.getProperty("user.dir")
        ).toAbsolutePath().normalize();

        Path document;

        try {
            String normalizedPath = documentPath.trim()
                    .replace('\\', '/');

            Path savedPath = Path.of(normalizedPath);

            // Use absolute paths directly; resolve relative paths from the project folder.
            document = savedPath.isAbsolute()
                    ? savedPath.normalize()
                    : projectFolder.resolve(savedPath).normalize();

        } catch (InvalidPathException exception) {
            throw new IOException(
                    "The stored document path is invalid.",
                    exception
            );
        }

        return document;
    }

    // Reset the medical entry form.
    @FXML
    private void clearForm() {
        registrationField.clear();
        medicalCourseComboBox.getSelectionModel().clearSelection();
        medicalDatePicker.setValue(null);
        medicalReasonArea.clear();
        registrationField.requestFocus();
    }

    // Display information and validation messages.
    private void showMessage(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Display database errors.
    private void showDatabaseError(
            String message,
            SQLException exception) {

        exception.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Database Error");
        alert.setHeaderText(message);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }
}