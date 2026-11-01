package com.fot.ams.controller;

import com.fot.ams.dao.TimetableDAO;
import com.fot.ams.model.Timetable;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// TG2070 - Technical Officer department timetable
public class TechnicalOfficerTimetableController {

    private String technicalOfficerId;

    private List<Timetable> departmentTimetables =
            new ArrayList<>();

    @FXML
    private ComboBox<String> timetableYearComboBox;

    @FXML
    private ComboBox<String> timetableSemesterComboBox;

    @FXML
    private ComboBox<String> timetableCourseComboBox;

    @FXML
    private ComboBox<String> timetableTypeComboBox;

    @FXML
    private TableView<Timetable> timetableTable;

    @FXML
    private TableColumn<Timetable, String> timetableCourseCodeColumn;

    @FXML
    private TableColumn<Timetable, String> timetableCourseNameColumn;

    @FXML
    private TableColumn<Timetable, String> timetableDayColumn;

    @FXML
    private TableColumn<Timetable, String> timetableTypeColumn;

    @FXML
    private TableColumn<Timetable, String> timetableStartTimeColumn;

    @FXML
    private TableColumn<Timetable, String> timetableEndTimeColumn;

    @FXML
    private TableColumn<Timetable, String> timetableHallColumn;

    @FXML
    private void initialize() {
        setupTable();
        setupFilters();

        timetableTable.setPlaceholder(
                new Label("No timetable records loaded.")
        );
    }

    // TG2070 - Receive logged-in TO ID from dashboard
    public void setTechnicalOfficerId(String technicalOfficerId) {
        this.technicalOfficerId = technicalOfficerId;
        loadDepartmentTimetable();
    }

    // TG2070 - Connect table columns to model getters
    private void setupTable() {
        timetableCourseCodeColumn.setCellValueFactory(
                new PropertyValueFactory<>("courseCode")
        );

        timetableCourseNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("courseName")
        );

        timetableDayColumn.setCellValueFactory(
                new PropertyValueFactory<>("dayOfWeek")
        );

        timetableTypeColumn.setCellValueFactory(
                new PropertyValueFactory<>("sessionType")
        );

        timetableStartTimeColumn.setCellValueFactory(
                new PropertyValueFactory<>("startTime")
        );

        timetableEndTimeColumn.setCellValueFactory(
                new PropertyValueFactory<>("endTime")
        );

        timetableHallColumn.setCellValueFactory(
                new PropertyValueFactory<>("lectureHall")
        );
    }

    // TG2070 - Build dropdown options from department records
    private void setupFilters() {
        timetableYearComboBox.getItems().clear();
        timetableSemesterComboBox.getItems().clear();
        timetableCourseComboBox.getItems().clear();
        timetableTypeComboBox.getItems().clear();

        timetableYearComboBox.getItems().add("All years");
        timetableSemesterComboBox.getItems().add("All semesters");
        timetableCourseComboBox.getItems().add("All courses");

        timetableTypeComboBox.getItems().addAll(
                "All types",
                "Theory",
                "Practical"
        );

        for (Timetable entry : departmentTimetables) {

            if (!timetableYearComboBox.getItems().contains(
                    entry.getAcademicYear())) {

                timetableYearComboBox.getItems().add(
                        entry.getAcademicYear()
                );
            }

            if (!timetableSemesterComboBox.getItems().contains(
                    entry.getSemester())) {

                timetableSemesterComboBox.getItems().add(
                        entry.getSemester()
                );
            }

            if (!timetableCourseComboBox.getItems().contains(
                    entry.getCourseCode())) {

                timetableCourseComboBox.getItems().add(
                        entry.getCourseCode()
                );
            }
        }

        resetFilters();
    }

    // TG2070 - Load only the logged-in TO's department timetable
    private void loadDepartmentTimetable() {
        departmentTimetables.clear();
        timetableTable.getItems().clear();
        setupFilters();

        if (technicalOfficerId == null
                || technicalOfficerId.isBlank()) {

            timetableTable.setPlaceholder(
                    new Label("Please log in as a Technical Officer.")
            );
            return;
        }

        TimetableDAO timetableDAO = new TimetableDAO();

        try {
            departmentTimetables =
                    timetableDAO.getTimetablesForTechnicalOfficer(
                            technicalOfficerId
                    );

            timetableTable.setPlaceholder(
                    new Label("No timetable records match these filters.")
            );

            setupFilters();

        } catch (SQLException exception) {
            exception.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Could not load department timetable.");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }
    }

    // TG2070 - Apply selected filters
    @FXML
    private void viewTimetable() {
        String year = timetableYearComboBox.getValue();
        String semester = timetableSemesterComboBox.getValue();
        String course = timetableCourseComboBox.getValue();
        String type = timetableTypeComboBox.getValue();

        timetableTable.getItems().clear();

        for (Timetable entry : departmentTimetables) {

            boolean yearMatches =
                    year == null
                            || "All years".equals(year)
                            || year.equals(entry.getAcademicYear());

            boolean semesterMatches =
                    semester == null
                            || "All semesters".equals(semester)
                            || semester.equals(entry.getSemester());

            boolean courseMatches =
                    course == null
                            || "All courses".equals(course)
                            || course.equals(entry.getCourseCode());

            boolean typeMatches =
                    type == null
                            || "All types".equals(type)
                            || type.equals(entry.getSessionType());

            if (yearMatches
                    && semesterMatches
                    && courseMatches
                    && typeMatches) {

                timetableTable.getItems().add(entry);
            }
        }
    }

    // TG2070 - Clear filters and display department records
    @FXML
    private void resetFilters() {
        timetableYearComboBox.setValue("All years");
        timetableSemesterComboBox.setValue("All semesters");
        timetableCourseComboBox.setValue("All courses");
        timetableTypeComboBox.setValue("All types");

        viewTimetable();
    }
}