package com.fot.ams.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class StudentMarksController {

    /** status = "Eligible" / "Not Eligible" / "Pending" */
    private record Eligibility(String code, String name, String status) {}

    @FXML private TableView<Eligibility> tblMarks;
    @FXML private TableColumn<Eligibility, String> colCode;
    @FXML private TableColumn<Eligibility, String> colName;
    @FXML private TableColumn<Eligibility, String> colEligibility;

    @FXML
    private void initialize() {
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().code()));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        colEligibility.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status()));
        colEligibility.setCellFactory(c -> badgeCell());

        tblMarks.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblMarks.setPlaceholder(new Label("No records found"));

        // TODO: MarksDAO / EligibilityDAO eken real data ganna
        tblMarks.setItems(FXCollections.observableArrayList(
                new Eligibility("CS101", "Object Oriented Programming", "Eligible"),
                new Eligibility("MA101", "Discrete Mathematics",        "Eligible"),
                new Eligibility("DB201", "Database Systems",            "Eligible"),
                new Eligibility("NW201", "Computer Networks",           "Not Eligible"),
                new Eligibility("SE301", "Software Engineering",        "Pending")));
    }

    /** Eligibility eka colour badge ekak widiyata pennanna */
    private TableCell<Eligibility, String> badgeCell() {
        return new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label l = new Label(s);
                l.getStyleClass().addAll("badge", "badge-" + s.toLowerCase().replace(' ', '-'));
                setGraphic(l);
                setText(null);
            }
        };
    }
}