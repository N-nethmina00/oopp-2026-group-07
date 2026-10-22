package com.fot.ams.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StudentResultsController {

    private record Result(String code, String name, int credits, String grade, double gp) {}

    @FXML private ComboBox<String> cmbYear, cmbSemester;
    @FXML private TableView<Result> tblResults;
    @FXML private TableColumn<Result, String> colCode;
    @FXML private TableColumn<Result, String> colName;
    @FXML private TableColumn<Result, String> colCredits;
    @FXML private TableColumn<Result, String> colGrade;
    @FXML private TableColumn<Result, String> colGp;
    @FXML private Label lblGpa;
    private final ObservableList<Result> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        cmbYear.getItems().addAll("2024", "2025", "2026");
        cmbYear.getSelectionModel().select("2026");
        cmbSemester.getItems().addAll("Semester 1", "Semester 2");
        cmbSemester.getSelectionModel().selectFirst();
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().code()));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        colCredits.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().credits())));
        colGrade.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().grade()));
        colGp.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.1f", d.getValue().gp())));
        tblResults.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblResults.setPlaceholder(new Label("No records found"));
        tblResults.setItems(data);
        handleView();
    }

    @FXML
    private void handleView() {
        // TODO: ResultDAO.find(year, semester, studentId)
        List<Result> rows = "Semester 1".equals(cmbSemester.getValue())
                ? List.of(new Result("CS101", "Object Oriented Programming", 3, "A",  4.0),
                          new Result("MA101", "Discrete Mathematics",        3, "B+", 3.3),
                          new Result("DB201", "Database Systems",            3, "A-", 3.7),
                          new Result("NW201", "Computer Networks",           2, "B",  3.0),
                          new Result("EN101", "Technical English",           2, "B+", 3.3))
                : List.of(new Result("SE301", "Software Engineering", 3, "A", 4.0),
                          new Result("CS202", "Data Structures",      3, "B", 3.0));
        data.setAll(rows);

        double pts = 0;
        int credits = 0;
        for (Result r : data) { pts += r.credits() * r.gp(); credits += r.credits(); }
        lblGpa.setText(credits == 0 ? "-" : String.format("%.2f", pts / credits));
    }
}
