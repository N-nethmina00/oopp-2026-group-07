package com.fot.ams.controller;

import com.fot.ams.dao.AttendanceDAO;
import com.fot.ams.dao.CourseDAO;
import com.fot.ams.model.AttendanceSession;
import com.fot.ams.model.Batch;
import com.fot.ams.model.Course;
import com.fot.ams.model.StudentAttendance;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// TG2070 - Manage attendance and session creation.
public class AttendanceController {

    // Connect these controls to the FXML file.
    @FXML
    private ComboBox<Batch> batchComboBox;

    @FXML
    private ComboBox<Course> courseComboBox;

    @FXML
    private DatePicker sessionDatePicker;

    @FXML
    private ComboBox<String> sessionTypeComboBox;

    @FXML
    private ComboBox<AttendanceSession> sessionComboBox;

    // Each table row contains one StudentAttendance object.
    @FXML
    private TableView<StudentAttendance> attendanceTable;

    // Each column displays a String value from the student object.
    @FXML
    private TableColumn<StudentAttendance, String> regNoColumn;

    @FXML
    private TableColumn<StudentAttendance, String> studentNameColumn;

    @FXML
    private TableColumn<StudentAttendance, String> statusColumn;

    @FXML
    private Label studentCountLabel;

    private String technicalOfficerId;

    // Receive the logged-in Technical Officer ID.
    public void setTechnicalOfficerId(String technicalOfficerId) {
        this.technicalOfficerId = technicalOfficerId;
    }

    // JavaFX calls this method after loading the FXML controls.
    @FXML
    private void initialize() {
        setupTable();

        loadBatches();
        loadCourses();

        sessionTypeComboBox.getItems().add("Theory");
        sessionTypeComboBox.getItems().add("Practical");

        // Enable this dropdown only when matching sessions are available.
        sessionComboBox.setDisable(true);

        // These event handlers run when the user changes a selection.
        courseComboBox.setOnAction(event -> loadSessions());
        batchComboBox.setOnAction(event -> loadSessions());
        sessionDatePicker.setOnAction(event -> loadSessions());
        sessionTypeComboBox.setOnAction(event -> loadSessions());

        // Remove old students when a different session is selected.
        sessionComboBox.setOnAction(event -> clearStudents());
    }

    // Set up table columns and the editable attendance status.
    private void setupTable() {
        // Property names match getters in StudentAttendance.
        // For example, "regNo" uses getRegNo().
        regNoColumn.setCellValueFactory(
                new PropertyValueFactory<>("regNo")
        );

        studentNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("studentName")
        );

        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        // Allow editing only in the status column.
        attendanceTable.setEditable(true);
        regNoColumn.setEditable(false);
        studentNameColumn.setEditable(false);
        statusColumn.setEditable(true);

        // Show a dropdown inside each status cell.
        statusColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(
                        "Present",
                        "Absent",
                        "Late",
                        "Excused"
                )
        );

        // Update the model when the user finishes editing a status.
        // This does not save the change to the database yet.
        statusColumn.setOnEditCommit(event -> {
            StudentAttendance student = event.getRowValue();
            student.setStatus(event.getNewValue());
        });

        clearStudents();
    }

    // Get batches through the DAO and add them to the dropdown.
    private void loadBatches() {
        AttendanceDAO attendanceDAO = new AttendanceDAO();

        try {
            ArrayList<Batch> batches = attendanceDAO.getAllBatches();

            batchComboBox.getItems().clear();

            for (Batch batch : batches) {
                batchComboBox.getItems().add(batch);
            }

        } catch (SQLException exception) {
            showDatabaseError("Could not load batches.", exception);
        }
    }

    // Get courses through CourseDAO and add them to the dropdown.
    private void loadCourses() {
        CourseDAO courseDAO = new CourseDAO();

        try {
            List<Course> courses = courseDAO.getAllCourses();

            courseComboBox.getItems().clear();

            for (Course course : courses) {
                courseComboBox.getItems().add(course);
            }

        } catch (SQLException exception) {
            showDatabaseError("Could not load courses.", exception);
        }
    }

    // Load sessions for the selected course, batch, date and type.
    private void loadSessions() {
        clearStudents();

        // Remove the previous session selection and list.
        sessionComboBox.getSelectionModel().clearSelection();
        sessionComboBox.getItems().clear();
        sessionComboBox.setDisable(true);

        Course selectedCourse = courseComboBox.getValue();
        Batch selectedBatch = batchComboBox.getValue();
        LocalDate selectedDate = sessionDatePicker.getValue();
        String selectedType = sessionTypeComboBox.getValue();

        // Stop until all required filters are selected.
        if (selectedCourse == null
                || selectedBatch == null
                || selectedDate == null
                || selectedType == null) {

            sessionComboBox.setPromptText(
                    "Select course, batch, date and type"
            );
            return;
        }

        AttendanceDAO attendanceDAO = new AttendanceDAO();

        try {
            ArrayList<AttendanceSession> sessions =
                    attendanceDAO.getSessions(
                            selectedCourse.getCourseCode(),
                            selectedDate,
                            selectedType,
                            selectedBatch.getBatchId()
                    );

            // Replace the dropdown items with the returned sessions.
            sessionComboBox.getItems().setAll(sessions);

            if (sessions.isEmpty()) {
                sessionComboBox.setPromptText("No sessions found");
            } else {
                sessionComboBox.setPromptText("Select session");
                sessionComboBox.setDisable(false);
            }

        } catch (SQLException exception) {
            sessionComboBox.setPromptText("Could not load sessions");
            showDatabaseError("Could not load sessions.", exception);
        }
    }

    // Load students and existing attendance for the selected session.
    @FXML
    private void loadStudents() {
        Batch selectedBatch = batchComboBox.getValue();
        AttendanceSession selectedSession = sessionComboBox.getValue();

        clearStudents();

        if (selectedBatch == null || selectedSession == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Missing Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select a batch and session.");
            alert.showAndWait();
            return;
        }

        AttendanceDAO attendanceDAO = new AttendanceDAO();

        try {
            ArrayList<StudentAttendance> students =
                    attendanceDAO.getStudentsForSession(
                            selectedSession.getSessionId(),
                            selectedBatch.getBatchId()
                    );

            attendanceTable.getItems().setAll(students);

            // Use "student" for one person and "students" for other counts.
            studentCountLabel.setText(
                    students.size()
                            + (students.size() == 1 ? " student" : " students")
            );

            if (students.isEmpty()) {
                attendanceTable.setPlaceholder(
                        new Label(
                                "No students found for this batch and session."
                        )
                );
            }

        } catch (SQLException exception) {
            showDatabaseError("Could not load students.", exception);
        }
    }

    // Clear table data when the selected filters or session change.
    private void clearStudents() {
        // Stop editing the current table cell.
        attendanceTable.edit(-1, null);
        attendanceTable.getItems().clear();
        studentCountLabel.setText("0 students");

        // This message appears when the table has no rows.
        attendanceTable.setPlaceholder(
                new Label(
                        "Select a batch and session, then click Load Students."
                )
        );
    }

    // Print database error details and display an alert.
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

    // Set every loaded student's status to Present.
    @FXML
    private void markAllPresent() {
        if (attendanceTable.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Students");
            alert.setHeaderText(null);
            alert.setContentText("Please load students first.");
            alert.showAndWait();
            return;
        }

        attendanceTable.edit(-1, null);

        for (StudentAttendance student : attendanceTable.getItems()) {
            student.setStatus("Present");
        }

        // Redraw the table to show the changed model values.
        // The user still needs to click Save Attendance.
        attendanceTable.refresh();
    }

    // Send the table's attendance data to the DAO for saving.
    @FXML
    private void saveAttendance() {
        AttendanceSession selectedSession = sessionComboBox.getValue();

        if (selectedSession == null
                || attendanceTable.getItems().isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Attendance");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Please select a session and load students first."
            );
            alert.showAndWait();
            return;
        }

        // Do not save while a status cell is still being edited.
        if (attendanceTable.getEditingCell() != null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Finish Editing");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Please finish selecting the attendance status "
                            + "before saving."
            );
            alert.showAndWait();
            return;
        }

        AttendanceDAO attendanceDAO = new AttendanceDAO();

        try {
            attendanceDAO.saveAttendance(
                    selectedSession.getSessionId(),
                    attendanceTable.getItems()
            );

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Attendance Saved");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Attendance saved successfully for "
                            + attendanceTable.getItems().size()
                            + " student(s)."
            );
            alert.showAndWait();

        } catch (SQLException exception) {
            showDatabaseError(
                    "Could not save attendance.",
                    exception
            );
        }
    }

    // Create a session using the selected course, batch, date and type.
    @FXML
    private void showAddSession() {
        // Check that the logged-in officer ID is available.
        if (technicalOfficerId == null
                || technicalOfficerId.isBlank()) {

            showSessionMessage(
                    Alert.AlertType.ERROR,
                    "Please log in again before adding a session."
            );
            return;
        }

        Course selectedCourse = courseComboBox.getValue();
        Batch selectedBatch = batchComboBox.getValue();
        LocalDate selectedDate = sessionDatePicker.getValue();
        String selectedType = sessionTypeComboBox.getValue();

        if (selectedCourse == null
                || selectedBatch == null
                || selectedDate == null
                || selectedType == null) {

            showSessionMessage(
                    Alert.AlertType.WARNING,
                    "Please select course, batch, date and session type first."
            );
            return;
        }

        // Create the popup form. Void means no result value is returned.
        javafx.scene.control.Dialog<Void> dialog =
                new javafx.scene.control.Dialog<>();

        dialog.setTitle("Add Session");

        dialog.setHeaderText(
                selectedCourse.getCourseCode()
                        + " | " + selectedBatch
                        + "\n" + selectedDate
                        + " | " + selectedType
        );

        // Set the current application window as the popup's owner.
        dialog.initOwner(courseComboBox.getScene().getWindow());

        javafx.scene.control.TextField startField =
                new javafx.scene.control.TextField();
        startField.setPromptText("09:00");

        javafx.scene.control.TextField endField =
                new javafx.scene.control.TextField();
        endField.setPromptText("11:00");

        Label errorLabel = new Label();
        errorLabel.setWrapText(true);

        // Arrange form controls in rows and columns.
        javafx.scene.layout.GridPane form =
                new javafx.scene.layout.GridPane();

        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new javafx.geometry.Insets(20));

        // The numbers represent the column and row positions.
        form.add(new Label("Start Time (HH:mm)"), 0, 0);
        form.add(startField, 1, 0);

        form.add(new Label("End Time (HH:mm)"), 0, 1);
        form.add(endField, 1, 1);

        // Make the error label span two columns.
        form.add(errorLabel, 0, 2, 2, 1);

        dialog.getDialogPane().setContent(form);

        javafx.scene.control.ButtonType saveButtonType =
                new javafx.scene.control.ButtonType(
                        "Save Session",
                        javafx.scene.control.ButtonBar.ButtonData.OK_DONE
                );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButtonType,
                javafx.scene.control.ButtonType.CANCEL
        );

        // Get the actual Save button created by the dialog.
        javafx.scene.Node saveButton =
                dialog.getDialogPane().lookupButton(saveButtonType);

        // Handle Save clicks before the dialog closes automatically.
        saveButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {
                    // Keep the form open until saving succeeds.
                    event.consume();
                    errorLabel.setText("");

                    String startText = startField.getText().trim();
                    String endText = endField.getText().trim();

                    // Require two digits, a colon and two digits.
                    if (!startText.matches("\\d{2}:\\d{2}")
                            || !endText.matches("\\d{2}:\\d{2}")) {

                        errorLabel.setText(
                                "Enter times as HH:mm, for example 09:00."
                        );
                        return;
                    }

                    java.time.LocalTime startTime;
                    java.time.LocalTime endTime;

                    try {
                        // Convert the text into actual time values.
                        startTime = java.time.LocalTime.parse(startText);
                        endTime = java.time.LocalTime.parse(endText);

                    } catch (
                            java.time.format.DateTimeParseException exception
                    ) {
                        errorLabel.setText(
                                "Enter valid times between 00:00 and 23:59."
                        );
                        return;
                    }

                    if (!endTime.isAfter(startTime)) {
                        errorLabel.setText(
                                "End time must be after start time."
                        );
                        return;
                    }

                    AttendanceDAO attendanceDAO = new AttendanceDAO();

                    try {
                        // Pass the officer ID and session details to the DAO.
                        boolean saved = attendanceDAO.addSession(
                                technicalOfficerId,
                                selectedCourse.getCourseId(),
                                selectedDate,
                                startTime,
                                endTime,
                                selectedType,
                                selectedBatch.getBatchId()
                        );

                        // Show a message if the DAO did not add the session.
                        if (!saved) {
                            errorLabel.setText(
                                    "Session was not added. Check the batch, "
                                            + "course department and overlapping "
                                            + "sessions for this batch or an "
                                            + "existing shared session."
                            );
                            return;
                        }

                        // Close the form only after a successful save.
                        dialog.close();

                        // Reload sessions so the new session is available.
                        loadSessions();

                        showSessionMessage(
                                Alert.AlertType.INFORMATION,
                                "Session added successfully for "
                                        + selectedBatch
                                        + ". Select it from the Session dropdown."
                        );

                    } catch (SQLException exception) {
                        exception.printStackTrace();

                        errorLabel.setText(
                                "Could not save session: "
                                        + exception.getMessage()
                        );
                    }
                }
        );

        // Show the popup and wait until it is closed.
        dialog.showAndWait();
    }

    // Display a session-related alert.
    private void showSessionMessage(
            Alert.AlertType type,
            String message) {

        Alert alert = new Alert(type);
        alert.setTitle("Session");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}