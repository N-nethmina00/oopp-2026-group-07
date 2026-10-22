package com.fot.ams.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StudentMaterialsController {

    private record Material(String course, String title, String type, String uploaded) {}

    @FXML private ComboBox<String> cmbCourse;
    @FXML private TableView<Material> tblMaterials;
    @FXML private TableColumn<Material, String> colCourse;
    @FXML private TableColumn<Material, String> colTitle;
    @FXML private TableColumn<Material, String> colType;
    @FXML private TableColumn<Material, String> colUploaded;
    @FXML private TableColumn<Material, Void> colAction;

    @FXML
    private void initialize() {
        // TODO: MaterialDAO.findByStudent(...)
        ObservableList<Material> all = FXCollections.observableArrayList(
                new Material("CS101", "Week 01 - Introduction to OOP", "PDF",  "2026-09-01"),
                new Material("CS101", "Week 02 - Classes and Objects", "PPTX", "2026-09-08"),
                new Material("MA101", "Set Theory Notes",              "PDF",  "2026-09-03"),
                new Material("DB201", "ER Diagram Lab Sheet",          "PDF",  "2026-09-10"),
                new Material("NW201", "OSI Model Slides",              "PPTX", "2026-09-12"));
        FilteredList<Material> filtered = new FilteredList<>(all, m -> true);

        cmbCourse.getItems().addAll("All", "CS101", "MA101", "DB201", "NW201");
        cmbCourse.getSelectionModel().selectFirst();
        cmbCourse.valueProperty().addListener((o, a, c) ->
                filtered.setPredicate(m -> c == null || c.equals("All") || m.course().equals(c)));
        colCourse.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().course()));
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().title()));
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().type()));
        colUploaded.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().uploaded()));
        colAction.setCellFactory(c -> new TableCell<>() {
            private final Button b = new Button("Download");
            {
                b.getStyleClass().add("small-button");
                b.setOnAction(e -> download(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : b);
            }
        });

        tblMaterials.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblMaterials.setPlaceholder(new Label("No records found"));
        tblMaterials.setItems(filtered);
    }

    private void download(Material m) {
        // TODO: real download
        info("Download", "Downloading: " + m.title());
    }

    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
