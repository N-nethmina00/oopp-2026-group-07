package com.fot.ams.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StudentMarksController {

    /** quiz /10, assignment /15, mid /25, final /50 */
    private record Eligibility(String code, String name, String status) {}
    @FXML private TableView<Eligibility> tblMarks;

    @FXML private TableColumn<Eligibility, String> colCode;
    @FXML private TableColumn<Eligibility, String> colName;
    @FXML private TableColumn<Eligibility, String> colEligibility;
    @FXML
    private void initialize() {
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().code()));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));


        class studentp {
            private record Eligibility(String code, String name, String status) {
            }

            @FXML
            private TableView<Eligibility> tblMarks;

            @FXML
            private TableColumn<Eligibility, String> colCode;
            @FXML
            private TableColumn<Eligibility, String> colName;
            @FXML
            private TableColumn<Eligibility, String> colEligibility;

            @FXML
            private void initialize() {

            }
        }


    }
}
