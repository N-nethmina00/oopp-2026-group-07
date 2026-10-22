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
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.cell.ComboBoxTableCell;

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

    // TG2070 - Student attendance table
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

        // TG2070 - Clear old students when batch or session changes
        batchComboBox.setOnAction(event -> clearStudents());
        sessionComboBox.setOnAction(event -> clearStudents());
    }

    // TG2070 - Configure table and editable attendance status
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

        // TG2070 - Status dropdown inside each table row
        statusColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(
                        "Present",
                        "Absent",
                        "Late",
                        "Excused"
                )
        );

        // TG2070 - Store the selected status in the student object
        statusColumn.setOnEditCommit(event -> {
            StudentAttendance student = event.getRowValue();
            student.setStatus(event.getNewValue());
        });

        clearStudents();
    }
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

    // TG2070 - Load course dropdown
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
                            selectedCourse.getCourseId(),
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

    // TG2070 - START: Load students for selected session and batch
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
    // TG2070 - END

    // TG2070 - Clear students when selections change
    private void clearStudents() {
        attendanceTable.getItems().clear();
        studentCountLabel.setText("0 students");

        attendanceTable.setPlaceholder(
                new Label("Select a batch and session, then click Load Students.")
        );
    }

    // TG2070 - Show database errors
    private void showDatabaseError(String message,
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
    // TG2070 - START: Save loaded student attendance
    @FXML
    private void saveAttendance() {

        AttendanceSession selectedSession =
                sessionComboBox.getValue();

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

        // Finish selecting a status before saving
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
    // TG2070 - END
}