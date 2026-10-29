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

// TG2070 - Attendance page controller
public class AttendanceController {

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

    @FXML
    private TableView<StudentAttendance> attendanceTable;

    @FXML
    private TableColumn<StudentAttendance, String> regNoColumn;

    @FXML
    private TableColumn<StudentAttendance, String> studentNameColumn;

    @FXML
    private TableColumn<StudentAttendance, String> statusColumn;

    @FXML
    private Label studentCountLabel;

    // TG2070 - START: Receive logged-in TO ID from dashboard
    private String technicalOfficerId;

    public void setTechnicalOfficerId(String technicalOfficerId) {
        this.technicalOfficerId = technicalOfficerId;
    }
    // TG2070 - END

    @FXML
    private void initialize() {
        setupTable();

        loadBatches();
        loadCourses();

        sessionTypeComboBox.getItems().add("Theory");
        sessionTypeComboBox.getItems().add("Practical");

        sessionComboBox.setDisable(true);

        // TG2070 - Reload sessions when filters change
        courseComboBox.setOnAction(event -> loadSessions());
        sessionDatePicker.setOnAction(event -> loadSessions());
        sessionTypeComboBox.setOnAction(event -> loadSessions());

        // TG2070 - Clear students when batch or session changes
        batchComboBox.setOnAction(event -> clearStudents());
        sessionComboBox.setOnAction(event -> clearStudents());
    }

    // TG2070 - Configure editable attendance table
    private void setupTable() {
        regNoColumn.setCellValueFactory(
                new PropertyValueFactory<>("regNo")
        );

        studentNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("studentName")
        );

        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        attendanceTable.setEditable(true);
        regNoColumn.setEditable(false);
        studentNameColumn.setEditable(false);
        statusColumn.setEditable(true);

        statusColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(
                        "Present",
                        "Absent",
                        "Late",
                        "Excused"
                )
        );

        statusColumn.setOnEditCommit(event -> {
            StudentAttendance student = event.getRowValue();
            student.setStatus(event.getNewValue());
        });

        clearStudents();
    }

    // TG2070 - Load batches
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

    // TG2070 - Load courses
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

    // TG2070 - Load sessions using selected filters
    private void loadSessions() {
        clearStudents();

        sessionComboBox.getSelectionModel().clearSelection();
        sessionComboBox.getItems().clear();
        sessionComboBox.setDisable(true);

        Course selectedCourse = courseComboBox.getValue();
        LocalDate selectedDate = sessionDatePicker.getValue();
        String selectedType = sessionTypeComboBox.getValue();

        if (selectedCourse == null
                || selectedDate == null
                || selectedType == null) {

            sessionComboBox.setPromptText("Select course, date and type");
            return;
        }

        AttendanceDAO attendanceDAO = new AttendanceDAO();

        try {
            ArrayList<AttendanceSession> sessions =
                    attendanceDAO.getSessions(
                            selectedCourse.getCourseCode(),
                            selectedDate,
                            selectedType
                    );

            for (AttendanceSession session : sessions) {
                sessionComboBox.getItems().add(session);
            }

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

    // TG2070 - Load students and existing attendance
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

            for (StudentAttendance student : students) {
                attendanceTable.getItems().add(student);
            }

            studentCountLabel.setText(students.size() + " students");

            if (students.isEmpty()) {
                attendanceTable.setPlaceholder(
                        new Label("No students found for this batch and session.")
                );
            }

        } catch (SQLException exception) {
            showDatabaseError("Could not load students.", exception);
        }
    }

    // TG2070 - Clear old students when selections change
    private void clearStudents() {
        attendanceTable.getItems().clear();
        studentCountLabel.setText("0 students");

        attendanceTable.setPlaceholder(
                new Label("Select a batch and session, then click Load Students.")
        );
    }

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

    // TG2070 - Mark all loaded students as Present
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

        attendanceTable.refresh();
    }

    // TG2070 - Save or update attendance
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
    // TG2070 - START: Open Add Session form
    @FXML
    private void showAddSession() {
        if (technicalOfficerId == null
                || technicalOfficerId.isBlank()) {
            showSessionMessage(
                    Alert.AlertType.ERROR,
                    "Please log in again before adding a session."
            );
            return;
        }

        Course selectedCourse = courseComboBox.getValue();
        LocalDate selectedDate = sessionDatePicker.getValue();
        String selectedType = sessionTypeComboBox.getValue();

        if (selectedCourse == null
                || selectedDate == null
                || selectedType == null) {
            showSessionMessage(
                    Alert.AlertType.WARNING,
                    "Please select course, date and session type first."
            );
            return;
        }

        javafx.scene.control.Dialog<Void> dialog =
                new javafx.scene.control.Dialog<>();

        dialog.setTitle("Add Session");
        dialog.setHeaderText(
                selectedCourse.getCourseCode()
                        + " | " + selectedDate
                        + " | " + selectedType
        );

        dialog.initOwner(courseComboBox.getScene().getWindow());

        javafx.scene.control.TextField startField =
                new javafx.scene.control.TextField();
        startField.setPromptText("09:00");

        javafx.scene.control.TextField endField =
                new javafx.scene.control.TextField();
        endField.setPromptText("11:00");

        Label errorLabel = new Label();
        errorLabel.setWrapText(true);

        javafx.scene.layout.GridPane form =
                new javafx.scene.layout.GridPane();

        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new javafx.geometry.Insets(20));

        form.add(new Label("Start Time (HH:mm)"), 0, 0);
        form.add(startField, 1, 0);

        form.add(new Label("End Time (HH:mm)"), 0, 1);
        form.add(endField, 1, 1);

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

        javafx.scene.Node saveButton =
                dialog.getDialogPane().lookupButton(saveButtonType);

        // Keep the form open if validation or saving fails
        saveButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {
                    event.consume();
                    errorLabel.setText("");

                    String startText = startField.getText().trim();
                    String endText = endField.getText().trim();

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
                        startTime = java.time.LocalTime.parse(startText);
                        endTime = java.time.LocalTime.parse(endText);

                    } catch (java.time.format.DateTimeParseException exception) {
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
                        boolean saved = attendanceDAO.addSession(
                                technicalOfficerId,
                                selectedCourse.getCourseId(),
                                selectedDate,
                                startTime,
                                endTime,
                                selectedType
                        );

                        if (!saved) {
                            errorLabel.setText(
                                    "Session was not added. Check that the course "
                                            + "belongs to your department and "
                                            + "the time does not overlap another session."
                            );
                            return;
                        }

                        dialog.close();

                        // Refresh session dropdown after saving
                        loadSessions();

                        showSessionMessage(
                                Alert.AlertType.INFORMATION,
                                "Session added successfully. "
                                        + "Select it from the Session dropdown."
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

        dialog.showAndWait();
    }
// TG2070 - END

    // TG2070 - Show session messages
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