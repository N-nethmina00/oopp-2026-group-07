
package com.fot.ams.controller;

import com.fot.ams.dao.StudentResultsDAO;
import com.fot.ams.model.Student;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class StudentResultsController {

    @FXML private ComboBox<String> cmbYear;
    @FXML private ComboBox<String> cmbSemester;

    @FXML private TableView<Student.CourseResult> tblResults;

    @FXML private TableColumn<Student.CourseResult, String> colCode;
    @FXML private TableColumn<Student.CourseResult, String> colName;
    @FXML private TableColumn<Student.CourseResult, String> colCredits;
    @FXML private TableColumn<Student.CourseResult, String> colGrade;
    @FXML private TableColumn<Student.CourseResult, String> colGp;

    @FXML private Label lblGpa;

    private String loggedInUserId;
    private int semesterCount;

    private final StudentResultsDAO resultsDAO =
            new StudentResultsDAO();

    @FXML
    private void initialize() {

        colCode.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getCourseCode()
                )
        );

        colName.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getCourseName()
                )
        );

        colCredits.setCellValueFactory(d ->
                new SimpleStringProperty(
                        String.valueOf(d.getValue().getCredits())
                )
        );

        colGrade.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getGrade()
                )
        );

        colGp.setCellValueFactory(d -> {

            Double gp = d.getValue().getGradePoint();

            return new SimpleStringProperty(
                    gp == null ? "N/A"
                            : String.format(Locale.US, "%.2f", gp)
            );
        });

        tblResults.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tblResults.setPlaceholder(
                new Label("No results available")
        );

        lblGpa.setText("N/A");

        // Update semesters when year changes
        cmbYear.valueProperty().addListener(
                (obs, oldYear, newYear) -> {

                    cmbSemester.getItems().clear();
                    tblResults.getItems().clear();
                    lblGpa.setText("N/A");

                    if (loggedInUserId != null
                            && newYear != null) {

                        loadSemesters(newYear);
                    }
                }
        );
    }

    // Logged-in user ID from Layout Controller
    public void loadResults(String userId) {

        loggedInUserId = userId;

        try {

            semesterCount =
                    resultsDAO.getSemesterCount(userId);

            List<String> years =
                    resultsDAO.getAcademicYears(userId);

            cmbYear.getItems().setAll(years);

            if (!years.isEmpty()) {
                cmbYear.getSelectionModel().selectFirst();
            } else {
                cmbSemester.getItems().clear();
                tblResults.getItems().clear();
                lblGpa.setText("N/A");
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void loadSemesters(String year) {

        try {

            List<String> semesters =
                    resultsDAO.getSemesters(
                            loggedInUserId, year
                    );

            cmbSemester.getItems().setAll(semesters);

            if (!semesters.isEmpty()) {
                cmbSemester.getSelectionModel().selectFirst();
                handleView();
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    @FXML
    private void handleView() {

        String year = cmbYear.getValue();
        String semester = cmbSemester.getValue();

        if (loggedInUserId == null
                || year == null
                || semester == null) {

            tblResults.getItems().clear();
            lblGpa.setText("N/A");
            return;
        }

        try {

            Student student =
                    resultsDAO.getStudentResults(
                            loggedInUserId,
                            year,
                            semester
                    );

            tblResults.setItems(
                    FXCollections.observableArrayList(
                            student.getSemesterResults()
                    )
            );

            Double gpa = student.getGpa();

            lblGpa.setText(
                    gpa == null ? "N/A"
                            : String.format(Locale.US, "%.2f", gpa)
            );

            if (semesterCount > 1) {

                tblResults.setPlaceholder(
                        new Label("No course results found for selected semester")
                );

            } else {

                tblResults.setPlaceholder(
                        new Label("No course grades available")
                );
            }

        } catch (SQLException e) {

            tblResults.getItems().clear();
            lblGpa.setText("N/A");
            showError(e);
        }
    }

    private void showError(SQLException e) {

        e.printStackTrace();

        new Alert(
                Alert.AlertType.ERROR,
                "Database Error: " + e.getMessage()
        ).showAndWait();
    }
}
