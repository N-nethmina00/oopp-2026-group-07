package com.fot.ams.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;

public class StudentMedicalController {

    /** date = "2026-10-03"  or  "2026-10-03 to 2026-10-05" */
    private record Application(String date, String course, String reason, String status) {}

    @FXML private ScrollPane scrollPage;
    @FXML private ComboBox<String> cmbCourse;
    @FXML private RadioButton rbSingle, rbMultiple;
    @FXML private Label lblFromDate;
    @FXML private DatePicker dpFrom, dpTo;
    @FXML private VBox boxToDate;
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
        // TODO: CourseDAO eken student ge courses ganna
        cmbCourse.getItems().addAll(
                "CS101 - Object Oriented Programming",
                "MA101 - Discrete Mathematics",
                "DB201 - Database Systems",
                "NW201 - Computer Networks",
                "SE301 - Software Engineering",
                "EN101 - Technical English");

        // Single day -> 1 calendar, Multiple days -> 2 calendars
        rbMultiple.selectedProperty().addListener((o, was, multiple) -> {
            boxToDate.setVisible(multiple);
            boxToDate.setManaged(multiple);
            lblFromDate.setText(multiple ? "FROM DATE" : "MEDICAL DATE");
            if (!multiple) dpTo.setValue(null);
            hideMessage();
        });

        // Future dates disable; "To" date can't be before "From" date
        dpFrom.setDayCellFactory(p -> new DateCell() {
            @Override public void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setDisable(empty || d.isAfter(LocalDate.now()));
            }
        });
        dpTo.setDayCellFactory(p -> new DateCell() {
            @Override public void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setDisable(empty || d.isAfter(LocalDate.now())
                        || (dpFrom.getValue() != null && d.isBefore(dpFrom.getValue())));
            }
        });
        dpFrom.valueProperty().addListener((o, a, from) -> {
            if (from != null && dpTo.getValue() != null && dpTo.getValue().isBefore(from)) dpTo.setValue(null);
            hideMessage();
        });

        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().date()));
        colCourse.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().course()));
        colReason.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().reason()));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status()));
        colStatus.setCellFactory(c -> badgeCell());
        tblMedical.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblMedical.setPlaceholder(new Label("No records found"));
        tblMedical.setItems(data);

        // TODO: MedicalDAO.findByStudent(...)
        data.add(new Application("2026-09-28", "MA101 - Discrete Mathematics", "Fever", "Approved"));
        data.add(new Application("2026-10-03 to 2026-10-05", "CS101 - Object Oriented Programming",
                "Dental surgery", "Pending"));

        txtReason.textProperty().addListener((o, a, b) -> hideMessage());
    }

    @FXML
    private void handleChooseFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select medical document");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF / Images", "*.pdf", "*.png", "*.jpg", "*.jpeg"));
        File f = fc.showOpenDialog(lblFile.getScene().getWindow());
        if (f != null) { selectedFile = f; lblFile.setText(f.getName()); hideMessage(); }
    }

    @FXML
    private void handleSubmit() {
        boolean multiple = rbMultiple.isSelected();
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();

        if (cmbCourse.getValue() == null || from == null || txtReason.getText().isBlank()) {
            showMessage("Please select course, date and enter a reason.", false);
            return;
        }
        if (selectedFile == null) {
            showMessage("Please upload your medical document (required).", false);
            return;
        }
        if (multiple && to == null) {
            showMessage("Please select the 'To' date.", false);
            return;
        }
        if (from.isAfter(LocalDate.now()) || (multiple && to.isAfter(LocalDate.now()))) {
            showMessage("Medical date cannot be in the future.", false);
            return;
        }
        if (multiple && to.isBefore(from)) {
            showMessage("'To' date cannot be before the 'From' date.", false);
            return;
        }

        String dateText = (multiple && !to.equals(from)) ? from + " to " + to : from.toString();

        // TODO: MedicalDAO.save(...) + selectedFile upload
        data.add(0, new Application(dateText, cmbCourse.getValue(), txtReason.getText().trim(), "Pending"));

        cmbCourse.getSelectionModel().clearSelection();
        rbSingle.setSelected(true);
        dpFrom.setValue(null);
        dpTo.setValue(null);
        txtReason.clear();
        selectedFile = null;
        lblFile.setText("No file selected");
        showMessage("Medical application submitted successfully.", true);

        // aluth application eka table eke udama pennanawa, page eka table ekata scroll wenawa
        tblMedical.getSelectionModel().selectFirst();
        Platform.runLater(() -> scrollPage.setVvalue(1.0));
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