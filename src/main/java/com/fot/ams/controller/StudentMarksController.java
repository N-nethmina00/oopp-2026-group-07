package com.fot.ams.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StudentMarksController {

    /** quiz /10, assignment /15, mid /25, final /50 */
    private record Mark(String code, String name, int quiz, int assign, int mid, int fin) {
        int ca() { return quiz + assign + mid; }              // out of 50
        String grade() {
            int t = ca() + fin;                               // out of 100
            return t >= 80 ? "A" : t >= 70 ? "B" : t >= 60 ? "C" : t >= 50 ? "D" : "E";
        }
    }

    @FXML private TableView<Mark> tblMarks;
    @FXML private TableColumn<Mark, String> colCode;
    @FXML private TableColumn<Mark, String> colName;
    @FXML private TableColumn<Mark, String> colQuiz;
    @FXML private TableColumn<Mark, String> colAssign;
    @FXML private TableColumn<Mark, String> colMid;
    @FXML private TableColumn<Mark, String> colCa;
    @FXML private TableColumn<Mark, String> colFinal;
    @FXML private TableColumn<Mark, String> colGrade;
    @FXML private Label lblCa, lblFinal, lblGrade;

    @FXML
    private void initialize() {
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().code()));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        colQuiz.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().quiz())));
        colAssign.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().assign())));
        colMid.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().mid())));
        colCa.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().ca())));
        colFinal.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().fin())));
        colGrade.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().grade()));
        tblMarks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblMarks.setPlaceholder(new Label("No records found"));
        // TODO: MarksDAO eken
        tblMarks.setItems(FXCollections.observableArrayList(
                new Mark("CS101", "Object Oriented Programming", 9, 13, 20, 38),
                new Mark("MA101", "Discrete Mathematics",        7, 12, 18, 31),
                new Mark("DB201", "Database Systems",            8, 14, 21, 35),
                new Mark("NW201", "Computer Networks",           6, 10, 15, 27),
                new Mark("SE301", "Software Engineering",        9, 14, 22, 40)));

        tblMarks.getSelectionModel().selectedItemProperty().addListener((o, a, m) -> {
            if (m != null) showSummary(m);
        });
        tblMarks.getSelectionModel().selectFirst();
    }

    private void showSummary(Mark m) {
        lblCa.setText(m.ca() + "/50");
        lblFinal.setText(m.fin() + "/50");
        lblGrade.setText(m.grade());
    }
}
