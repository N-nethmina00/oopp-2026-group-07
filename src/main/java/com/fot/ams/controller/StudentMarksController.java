
package com.fot.ams.controller;

import com.fot.ams.dao.StudentMarksDAO;
import com.fot.ams.model.CAEvaluation;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class StudentMarksController {

    @FXML
    private TableView<CAEvaluation> tblMarks;

    @FXML
    private TableColumn<CAEvaluation, String> colCode;

    @FXML
    private TableColumn<CAEvaluation, String> colName;

    @FXML
    private TableColumn<CAEvaluation, String> colEligibility;

    private final StudentMarksDAO marksDAO =
            new StudentMarksDAO();

    @FXML
    private void initialize() {

        colCode.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCourseCode()
                )
        );

        colName.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCourseName()
                )
        );

        colEligibility.setCellValueFactory(data ->
                new SimpleStringProperty(
                        getEligibilityText(data.getValue())
                )
        );

        colEligibility.setCellFactory(
                column -> badgeCell()
        );

        tblMarks.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tblMarks.setPlaceholder(
                new Label("No CA eligibility records found")
        );
    }

    // Get CA status from database model
    private String getEligibilityText(CAEvaluation evaluation) {

        String status = evaluation.getCaStatus();
        Integer eligibility = evaluation.getEligibility();

        if (status != null &&
                status.equalsIgnoreCase("Pending Medical")) {
            return "Pending";
        }

        if (eligibility == null) {
            return "Pending";
        }

        if (eligibility == 1) {
            return "Eligible";
        }

        return "Not Eligible";
    }

    // Load logged-in student's CA eligibility
    public void loadMarks(String userId) {

        try {

            List<CAEvaluation> results =
                    marksDAO.getStudentCAEligibility(userId);

            tblMarks.setItems(
                    FXCollections.observableArrayList(results)
            );

        } catch (SQLException e) {

            e.printStackTrace();

            tblMarks.getItems().clear();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to load CA eligibility: "
                            + e.getMessage()
            ).showAndWait();
        }
    }

    // Colour badges
    private TableCell<CAEvaluation, String> badgeCell() {

        return new TableCell<>() {

            @Override
            protected void updateItem(
                    String status, boolean empty) {

                super.updateItem(status, empty);

                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label label = new Label(status);

                label.getStyleClass().addAll(
                        "badge",
                        "badge-" +
                                status.toLowerCase(Locale.ROOT)
                                        .replace(' ', '-')
                );

                setGraphic(label);
                setText(null);
            }
        };
    }
}
