
package com.fot.ams.controller;

import com.fot.ams.dao.StudentMaterialDAO;
import com.fot.ams.model.CourseMaterial;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StudentMaterialsController {

    @FXML private ComboBox<String> cmbCourse;
    @FXML private TableView<CourseMaterial> tblMaterials;

    @FXML private TableColumn<CourseMaterial, String> colCourse;
    @FXML private TableColumn<CourseMaterial, String> colTitle;
    @FXML private TableColumn<CourseMaterial, String> colType;
    @FXML private TableColumn<CourseMaterial, String> colUploaded;
    @FXML private TableColumn<CourseMaterial, Void> colAction;

    private final StudentMaterialDAO materialDAO =
            new StudentMaterialDAO();

    private final ObservableList<CourseMaterial> allMaterials =
            FXCollections.observableArrayList();

    private final FilteredList<CourseMaterial> filtered =
            new FilteredList<>(allMaterials, item -> true);

    private final Map<Integer, String> courses =
            new LinkedHashMap<>();

    @FXML
    private void initialize() {

        colCourse.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCourseCode()
                )
        );

        colTitle.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getMaterialTitle()
                )
        );

        colType.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getMaterialType()
                )
        );

        colUploaded.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getUploadedDate() == null
                                ? ""
                                : data.getValue()
                                .getUploadedDate().toString()
                )
        );

        colAction.setCellFactory(column ->
                new TableCell<>() {

                    private final Button button =
                            new Button("Download");

                    {
                        button.getStyleClass().add("small-button");

                        button.setOnAction(event -> {
                            CourseMaterial material =
                                    getTableRow().getItem();

                            if (material != null) {
                                downloadMaterial(material);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(
                            Void item, boolean empty) {

                        super.updateItem(item, empty);
                        setGraphic(empty ? null : button);
                    }
                }
        );

        tblMaterials.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tblMaterials.setPlaceholder(
                new Label("No course materials found")
        );

        tblMaterials.setItems(filtered);

        cmbCourse.valueProperty().addListener(
                (obs, oldValue, newValue) ->
                        filterMaterials()
        );
    }

    public void loadMaterials(String userId) {

        allMaterials.clear();
        cmbCourse.getItems().clear();
        courses.clear();

        if (userId == null || userId.isBlank()) {
            showError("Student is not logged in.");
            return;
        }

        try {

            courses.putAll(
                    materialDAO.getEnrolledCourses(userId)
            );

            cmbCourse.getItems().add("All");

            for (String courseName : courses.values()) {
                cmbCourse.getItems().add(courseName);
            }

            List<CourseMaterial> materials =
                    materialDAO.getStudentMaterials(userId);

            allMaterials.setAll(materials);

            cmbCourse.getSelectionModel().select("All");

        } catch (SQLException e) {

            e.printStackTrace();

            allMaterials.clear();
            cmbCourse.getItems().clear();
            courses.clear();

            showError(
                    "Unable to load materials: " +
                            e.getMessage()
            );
        }
    }

    private void filterMaterials() {

        String selected = cmbCourse.getValue();

        if (selected == null || selected.equals("All")) {

            filtered.setPredicate(material -> true);
            return;
        }

        Integer selectedCourseId = null;

        for (Map.Entry<Integer, String> entry
                : courses.entrySet()) {

            if (entry.getValue().equals(selected)) {
                selectedCourseId = entry.getKey();
                break;
            }
        }

        final Integer courseId = selectedCourseId;

        filtered.setPredicate(material ->
                courseId != null &&
                        material.getCourseId() == courseId
        );
    }

    private void downloadMaterial(CourseMaterial material) {

        try {
            String filePath = material.getFilePath();

            if (filePath == null || filePath.isBlank()) {
                showError("No file available for download.");
                return;
            }

            Path projectRoot = Path.of(
                    System.getProperty("user.dir")
            ).toAbsolutePath().normalize();

            Path materialsFolder =
                    projectRoot.resolve("materials").normalize();

            Path storedPath = Path.of(filePath.replace('\\', '/'));

            Path source = storedPath.isAbsolute()
                    ? storedPath.normalize()
                    : projectRoot.resolve(storedPath).normalize();

            if (!source.startsWith(materialsFolder)) {
                showError("Invalid file location.");
                return;
            }

            if (!Files.isRegularFile(source)
                    || !Files.isReadable(source)) {
                showError("File not found:\n" + source);
                return;
            }

            if (!source.toRealPath().startsWith(
                    materialsFolder.toRealPath())) {
                showError("Invalid file location.");
                return;
            }

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Download Course Material");

            String fileName = material.getFileName();

            if (fileName == null || fileName.isBlank()) {
                fileName = source.getFileName().toString();
            }

            fileName = Path.of(
                    fileName.replace('\\', '/')
            ).getFileName().toString();

            chooser.setInitialFileName(fileName);

            File destination = chooser.showSaveDialog(
                    tblMaterials.getScene().getWindow()
            );

            if (destination == null) {
                return;
            }

            Path destinationPath = destination.toPath()
                    .toAbsolutePath().normalize();

            if (Files.exists(destinationPath)
                    && Files.isSameFile(source, destinationPath)) {
                showError("Please choose a different save location.");
                return;
            }

            Files.copy(
                    source,
                    destinationPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            new Alert(
                    Alert.AlertType.INFORMATION,
                    "File downloaded successfully!\n" +
                            destinationPath
            ).showAndWait();

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            showError("Download failed: " + e.getMessage());
        }
    }



    private void showError(String message) {

        new Alert(
                Alert.AlertType.ERROR,
                message
        ).showAndWait();
    }
}
