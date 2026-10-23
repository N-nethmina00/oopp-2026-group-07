package com.fot.ams.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDate;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StudentMedicalController {

    private record Application(String date, String course, String reason, String status) {}

    @FXML private ComboBox<String> cmbCourse;
    @FXML private DatePicker dpDate;
    @FXML private TextArea txtReason;
    @FXML private Label lblFile, lblMessage;
    @FXML private TableView<Application> tblMedical;
    @FXML private TableColumn<Application, String> colDate;
    @FXML private TableColumn<Application, String> colCourse;
    @FXML private TableColumn<Application, String> colReason;
    @FXML private TableColumn<Application, String> colStatus;

    private final ObservableList<Application> data = FXCollections.observableArrayList();
    private File selectedFile;

    @FXML
    private void initialize() {
        cmbCourse.getItems().addAll("CS101", "MA101", "DB201", "NW201", "SE301", "EN101");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().date()));
        colCourse.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().course()));
        colReason.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().reason()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status()));
        colStatus.setCellFactory(c -> badgeCell());
        tblMedical.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblMedical.setPlaceholder(new Label("No records found"));
        tblMedical.setItems(data);

        // TODO: MedicalDAO.findByStudent(...)
        data.add(new Application("2026-09-28", "MA101", "Fever", "Approved"));
        data.add(new Application("2026-10-03", "CS101", "Dental surgery", "Pending"));

        txtReason.textProperty().addListener((o, a, b) -> hideMessage());
    }

    @FXML
    private void handleChooseFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select medical document");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF / Images", "*.pdf", "*.png", "*.jpg", "*.jpeg"));
        File f = fc.showOpenDialog(lblFile.getScene().getWindow());
        if (f != null) { selectedFile = f; lblFile.setText(f.getName()); }
    }

    @FXML
    private void handleSubmit() {
        if (cmbCourse.getValue() == null || dpDate.getValue() == null || txtReason.getText().isBlank()) {
            showMessage("Please select course, date and enter a reason.", false);
            return;
        }
        if (dpDate.getValue().isAfter(LocalDate.now())) {
            showMessage("Medical date cannot be in the future.", false);
            return;
        }
        // TODO: MedicalDAO.save(...) + selectedFile upload
        data.add(0, new Application(dpDate.getValue().toString(), cmbCourse.getValue(),
                txtReason.getText().trim(), "Pending"));
        cmbCourse.getSelectionModel().clearSelection();
        dpDate.setValue(null);
        txtReason.clear();
        selectedFile = null;
        lblFile.setText("No file selected");
        showMessage("Medical application submitted successfully.", true);
    }

    private void showMessage(String msg, boolean ok) {
        lblMessage.getStyleClass().setAll(ok ? "success-label" : "error-label");
        lblMessage.setText(msg);
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private void hideMessage() {
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }

    /** Status column eka colour badge ekak widiyata pennanna */
    private <T> TableCell<T, String> badgeCell() {
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
