
package com.fot.ams.controller;

import com.fot.ams.dao.StudentCoursesDAO;
import com.fot.ams.dao.StudentMedicalDAO;
import com.fot.ams.model.Course;
import com.fot.ams.model.Medical;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class StudentMedicalController {

    @FXML private ScrollPane scrollPage;

    @FXML private ComboBox<Course> cmbCourse;

    @FXML private RadioButton rbSingle;
    @FXML private RadioButton rbMultiple;

    @FXML private Label lblFromDate;
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;

    @FXML private javafx.scene.layout.VBox boxToDate;

    @FXML private TextArea txtReason;
    @FXML private Label lblFile;
    @FXML private Label lblMessage;

    @FXML private TableView<Medical> tblMedical;
    @FXML private TableColumn<Medical, String> colDate;
    @FXML private TableColumn<Medical, String> colCourse;
    @FXML private TableColumn<Medical, String> colReason;
    @FXML private TableColumn<Medical, String> colStatus;

    private String loggedInUserId;
    private File selectedFile;

    private final StudentCoursesDAO coursesDAO =
            new StudentCoursesDAO();

    private final StudentMedicalDAO medicalDAO =
            new StudentMedicalDAO();

    @FXML
    private void initialize() {

        cmbCourse.setConverter(new StringConverter<>() {

            @Override
            public String toString(Course course) {
                if (course == null) {
                    return "";
                }

                return course.getCourseCode() +
                        " - " + course.getCourseName();
            }

            @Override
            public Course fromString(String text) {
                return null;
            }
        });

        rbMultiple.selectedProperty().addListener(
                (obs, oldValue, multiple) -> {

                    boxToDate.setVisible(multiple);
                    boxToDate.setManaged(multiple);

                    lblFromDate.setText(
                            multiple ? "FROM DATE" : "MEDICAL DATE"
                    );

                    if (!multiple) {
                        dpTo.setValue(null);
                    }

                    hideMessage();
                }
        );

        dpFrom.setDayCellFactory(p -> new DateCell() {

            @Override
            public void updateItem(
                    LocalDate date, boolean empty) {

                super.updateItem(date, empty);

                setDisable(empty ||
                        date.isAfter(LocalDate.now()));
            }
        });

        dpTo.setDayCellFactory(p -> new DateCell() {

            @Override
            public void updateItem(
                    LocalDate date, boolean empty) {

                super.updateItem(date, empty);

                setDisable(
                        empty ||
                                date.isAfter(LocalDate.now()) ||
                                (dpFrom.getValue() != null &&
                                        date.isBefore(dpFrom.getValue()))
                );
            }
        });

        colDate.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getDateDisplay()
                )
        );

        colCourse.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCourseName()
                )
        );

        colReason.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getReason()
                )
        );

        colStatus.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getStatus()
                )
        );

        colStatus.setCellFactory(c -> badgeCell());

        tblMedical.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tblMedical.setPlaceholder(
                new Label("No medical applications found")
        );

        txtReason.textProperty().addListener(
                (obs, oldValue, newValue) -> hideMessage()
        );
    }

    public void loadMedical(String userId) {

        loggedInUserId = userId;

        cmbCourse.getItems().clear();
        tblMedical.getItems().clear();

        try {

            List<Course> courses =
                    coursesDAO.getEnrolledCourses(userId);

            cmbCourse.getItems().setAll(courses);

            loadHistory();

        } catch (SQLException e) {

            e.printStackTrace();

            showMessage(
                    "Unable to load medical data: " +
                            e.getMessage(),
                    false
            );
        }
    }

    private void loadHistory() throws SQLException {

        List<Medical> medicals =
                medicalDAO.getStudentMedicalHistory(
                        loggedInUserId
                );

        tblMedical.setItems(
                FXCollections.observableArrayList(medicals)
        );
    }

    @FXML
    private void handleChooseFile() {

        FileChooser chooser = new FileChooser();

        chooser.setTitle("Choose Medical Document");

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Medical Documents",
                        "*.pdf", "*.png", "*.jpg", "*.jpeg"
                )
        );

        File file = chooser.showOpenDialog(
                lblFile.getScene().getWindow()
        );

        if (file == null) {
            return;
        }

        if (!file.isFile() || !file.canRead()
                || file.length() == 0
                || file.length() > 5 * 1024 * 1024) {

            showMessage(
                    "Select a readable file up to 5 MB.",
                    false
            );
            return;
        }

        String name = file.getName().toLowerCase(Locale.ROOT);

        if (!(name.endsWith(".pdf")
                || name.endsWith(".jpg")
                || name.endsWith(".jpeg")
                || name.endsWith(".png"))) {

            showMessage(
                    "Only PDF, JPG and PNG are allowed.",
                    false
            );
            return;
        }

        selectedFile = file;

        lblFile.setText(file.getName());

        hideMessage();
    }

    @FXML
    private void handleSubmit() {

        Course course = cmbCourse.getValue();

        LocalDate fromDate = dpFrom.getValue();

        LocalDate toDate = rbMultiple.isSelected()
                ? dpTo.getValue()
                : null;

        String reason = txtReason.getText().trim();

        if (loggedInUserId == null
                || course == null
                || fromDate == null
                || reason.isBlank()) {

            showMessage(
                    "Please select course, date and reason.",
                    false
            );
            return;
        }

        if (rbMultiple.isSelected() && toDate == null) {

            showMessage("Please select To Date.", false);
            return;
        }

        if (fromDate.isAfter(LocalDate.now())
                || (toDate != null &&
                toDate.isAfter(LocalDate.now()))) {

            showMessage(
                    "Medical dates cannot be in the future.",
                    false
            );
            return;
        }

        if (toDate != null && toDate.isBefore(fromDate)) {

            showMessage("Invalid medical date range.", false);
            return;
        }

        if (reason.length() > 2000) {

            showMessage("Reason is too long.", false);
            return;
        }

        if (selectedFile == null) {

            showMessage(
                    "Please upload your medical document.",
                    false
            );
            return;
        }

        Path savedFile = null;

        try {

            if (!selectedFile.isFile()
                    || !selectedFile.canRead()
                    || selectedFile.length() == 0
                    || selectedFile.length() > 5 * 1024 * 1024) {

                showMessage(
                        "Selected document is invalid.",
                        false
                );
                return;
            }

            String originalName =
                    selectedFile.getName().toLowerCase(Locale.ROOT);

            String ext;

            if (originalName.endsWith(".pdf")) {
                ext = ".pdf";
            } else if (originalName.endsWith(".png")) {
                ext = ".png";
            } else if (originalName.endsWith(".jpg")
                    || originalName.endsWith(".jpeg")) {
                ext = ".jpg";
            } else {
                showMessage("Invalid file type.", false);
                return;
            }

            Path folder = Path.of(
                    System.getProperty("user.dir"),
                    "medical-documents"
            );
            Files.createDirectories(folder);

            String fileName =
                    UUID.randomUUID() + ext;

            savedFile = folder.resolve(fileName);

            Files.copy(
                    selectedFile.toPath(),
                    savedFile
            );

            String relativePath = Path.of(
                    "medical-documents",
                    fileName
            ).toString();

            boolean success = medicalDAO.submitMedical(
                    loggedInUserId,
                    course.getCourseId(),
                    fromDate,
                    toDate,
                    reason,
                    relativePath
            );

            if (!success) {

                Files.deleteIfExists(savedFile);

                showMessage(
                        "Unable to submit. Check your enrollment.",
                        false
                );
                return;
            }

            cmbCourse.getSelectionModel().clearSelection();

            rbSingle.setSelected(true);

            dpFrom.setValue(null);
            dpTo.setValue(null);
            txtReason.clear();

            selectedFile = null;
            lblFile.setText("No file selected");

            try {
                loadHistory();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            showMessage(
                    "Medical application submitted successfully.",
                    true
            );

            Platform.runLater(
                    () -> scrollPage.setVvalue(1.0)
            );

        } catch (SQLException | IOException e) {

            e.printStackTrace();

            if (savedFile != null) {
                try {
                    Files.deleteIfExists(savedFile);
                } catch (IOException cleanupError) {
                    cleanupError.printStackTrace();
                }
            }

            showMessage(
                    "Unable to submit medical: " +
                            e.getMessage(),
                    false
            );
        }
    }

    private TableCell<Medical, String> badgeCell() {

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
                        "badge-" + status.toLowerCase(Locale.ROOT)
                );

                setGraphic(label);
                setText(null);
            }
        };
    }

    private void showMessage(
            String message, boolean success) {

        lblMessage.getStyleClass().setAll(
                success ? "success-label" : "error-label"
        );

        lblMessage.setText(message);
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private void hideMessage() {

        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }
}
