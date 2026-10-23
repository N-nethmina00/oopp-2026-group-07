
package com.fot.ams.controller;

import com.fot.ams.dao.CourseMaterialDAO;
import com.fot.ams.dao.StudentDAO;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import com.fot.ams.dao.CourseDAO;
import java.sql.SQLException;
import javafx.scene.control.Alert;
import java.util.List;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;




public class LecturerController {

    @FXML private Label pageTitle;
    @FXML private Label pageDescription;
    @FXML private VBox contentArea;

    @FXML private Button dashboardBtn;
    @FXML private Button profileBtn;
    @FXML private Button coursesBtn;
    @FXML private Button materialsBtn;
    @FXML private Button marksBtn;
    @FXML private Button studentsBtn;
    @FXML private Button eligibilityBtn;
    @FXML private Button attendanceBtn;
    @FXML private Button medicalsBtn;
    @FXML private Button gradesBtn;
    @FXML private Button noticesBtn;

    // Sidebar button styles
    private static final String ACTIVE_STYLE =
            "-fx-background-color: white;" +
                    "-fx-text-fill: #0078b9;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 8;";

    private static final String NORMAL_STYLE =
            "-fx-background-color: transparent;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: normal;" +
                    "-fx-background-radius: 8;";

    @FXML
    private String lecturerId;

    public void setLecturerId(String lecturerId) {

        if (lecturerId == null || lecturerId.isBlank()) {
            throw new IllegalArgumentException(
                    "Valid Lecturer ID is required"
            );
        }

        this.lecturerId = lecturerId;
        showDashboard();
    }

    private String loadMyCoursesCount() {

        CourseDAO courseDAO = new CourseDAO();

        try {
            int count =
                    courseDAO.countCoursesByLecturer(lecturerId);

            return String.valueOf(count);

        } catch (SQLException e) {
            e.printStackTrace();
            return "—";
        }
    }

    @FXML
    public void initialize() {

        displayPage(
                "Lecturer Dashboard",
                "Loading lecturer information...",
                dashboardBtn
        );
    }

    private void setActiveButton(Button activeButton) {

        Button[] buttons = {
                dashboardBtn, profileBtn, coursesBtn,
                materialsBtn, marksBtn, studentsBtn,
                eligibilityBtn, attendanceBtn, medicalsBtn,
                gradesBtn, noticesBtn
        };

        for (Button button : buttons) {
            if (button != null) {
                button.setStyle(
                        button == activeButton
                                ? ACTIVE_STYLE
                                : NORMAL_STYLE
                );
            }
        }
    }

    private void displayPage(
            String title,
            String description,
            Button activeButton) {

        pageTitle.setText(title);
        pageDescription.setText(description);
        contentArea.getChildren().clear();
        setActiveButton(activeButton);
    }

    // Create a dashboard statistic card
    private VBox createStatCard(
            String title,
            String value,
            String subtitle) {

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-text-fill: #526174;" +
                        "-fx-font-size: 13px;"
        );

        Label valueLabel = new Label(value);
        valueLabel.setStyle(
                "-fx-text-fill: #172B45;" +
                        "-fx-font-size: 27px;" +
                        "-fx-font-weight: bold;"
        );

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle(
                "-fx-text-fill: #7B8797;" +
                        "-fx-font-size: 11px;"
        );

        VBox card = new VBox(
                13,
                titleLabel,
                valueLabel,
                subtitleLabel
        );

        card.setPadding(new Insets(18));
        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPrefHeight(145);

        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #D5DCE5;" +
                        "-fx-border-radius: 8;"
        );

        HBox.setHgrow(card, Priority.ALWAYS);

        return card;
    }

    // Create a table with column headings
    private TableView<String[]> createTable(
            String placeholder,
            String... headings) {

        TableView<String[]> table = new TableView<>();

        table.setPlaceholder(new Label(placeholder));
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        for (int index = 0;
             index < headings.length; index++) {

            final int columnIndex = index;

            TableColumn<String[], String> column =
                    new TableColumn<>(headings[index]);

            column.setCellValueFactory(cell -> {
                String[] row = cell.getValue();

                String value =
                        row != null &&
                                columnIndex < row.length
                                ? row[columnIndex]
                                : "";

                return new ReadOnlyStringWrapper(value);
            });

            table.getColumns().add(column);
        }

        table.setPrefHeight(210);
        table.setMinHeight(170);

        return table;
    }

    // Dashboard overview
    @FXML
    private void showDashboard() {

        displayPage(
                "Dashboard Overview",
                "Overview of your teaching activities.",
                dashboardBtn
        );

        // Statistic cards
        HBox statsRow = new HBox(15);

        statsRow.getChildren().addAll(
                createStatCard(
                        "My Courses",
                        loadMyCoursesCount(),
                        "Assigned courses"
                ),
                createStatCard(
                        "Students", "—", "Enrolled"
                ),
                createStatCard(
                        "Pending Marks", "—", "To complete"
                ),
                createStatCard(
                        "Materials", "—", "Uploaded"
                )
        );

        // Teaching schedule
        Label scheduleTitle =
                new Label("Teaching Schedule");

        scheduleTitle.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        TableView<String[]> scheduleTable =
                createTable(
                        "No teaching schedule loaded",
                        "Day", "Time", "Course", "Location"
                );

        // Students overview
        Label studentsTitle =
                new Label("Students Overview");

        studentsTitle.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        TableView<String[]> studentsTable =
                createTable(
                        "No student records loaded",
                        "ID", "Name", "Course",
                        "Status", "Action"
                );

        // Add dashboard elements
        contentArea.setSpacing(22);

        contentArea.getChildren().addAll(
                statsRow,
                scheduleTitle,
                scheduleTable,
                studentsTitle,
                studentsTable
        );
    }

    @FXML
    private void showProfile() {
        displayPage(
                "My Profile",
                "Lecturer profile management.",
                profileBtn
        );
    }

    @FXML

    private void showCourses() {

        displayPage(
                "My Courses",
                "Courses assigned to Lecturer " + lecturerId,
                coursesBtn
        );

        Label heading = new Label("My Assigned Courses");

        heading.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        TableView<String[]> coursesTable = createTable(
                "No courses assigned to this lecturer.",
                "Course Code",
                "Course Name",
                "Credits",
                "Type"
        );

        coursesTable.setPrefHeight(350);

        CourseDAO courseDAO = new CourseDAO();

        try {
            List<String[]> courses =
                    courseDAO.getCoursesByLecturer(lecturerId);

            coursesTable.getItems().setAll(courses);

        } catch (SQLException e) {
            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to load courses from database."
            ).showAndWait();
        }

        contentArea.getChildren().addAll(
                heading,
                coursesTable
        );
    }



    @FXML
    private void showMaterials() {

        displayPage(
                "Course Materials",
                "Materials uploaded for your assigned courses.",
                materialsBtn
        );

        Label heading = new Label("My Course Materials");

        Button uploadButton = new Button("+ Upload Material");

        uploadButton.setStyle(
                "-fx-background-color: #4c56cd;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 11 20;"
        );

        uploadButton.setOnAction(event -> uploadMaterial());

        heading.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        TableView<String[]> materialsTable = createTable(
                "No course materials found.",
                "Course Code",
                "Material Title",
                "Type",
                "File Name",
                "Uploaded Date"
        );

        materialsTable.setPrefHeight(400);

        CourseMaterialDAO materialDAO = new CourseMaterialDAO();

        try {
            List<String[]> materials =
                    materialDAO.getMaterialsByLecturer(lecturerId);

            materialsTable.getItems().setAll(materials);

        } catch (SQLException e) {
            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to load course materials."
            ).showAndWait();
        }

        contentArea.getChildren().addAll(
                heading,
                uploadButton,
                materialsTable
        );
    }

        private void uploadMaterial() {

            // Get assigned courses
            ComboBox<String> courseComboBox = new ComboBox<>();
            courseComboBox.setMaxWidth(Double.MAX_VALUE);
            courseComboBox.setPromptText("Select Course");

            try {
                CourseDAO courseDAO = new CourseDAO();

                List<String[]> courses =
                        courseDAO.getCoursesByLecturer(lecturerId);

                for (String[] course : courses) {
                    courseComboBox.getItems().add(course[0]);
                }

            } catch (SQLException e) {
                e.printStackTrace();

                new Alert(
                        Alert.AlertType.ERROR,
                        "Unable to load assigned courses."
                ).showAndWait();
                return;
            }

            if (courseComboBox.getItems().isEmpty()) {
                new Alert(
                        Alert.AlertType.INFORMATION,
                        "No courses are assigned to this lecturer."
                ).showAndWait();
                return;
            }

            // Material title
            TextField titleField = new TextField();
            titleField.setPromptText("Enter material title");

            // Selected PDF
            final File[] selectedPdf = {null};

            Label fileLabel = new Label("No PDF selected");
            fileLabel.setWrapText(true);

            Button chooseFileButton = new Button("Choose PDF");

            chooseFileButton.setOnAction(event -> {

                FileChooser chooser = new FileChooser();
                chooser.setTitle("Select Lecture PDF");

                chooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter(
                                "PDF Files", "*.pdf"
                        )
                );

                File file = chooser.showOpenDialog(
                        contentArea.getScene().getWindow()
                );

                if (file != null) {
                    selectedPdf[0] = file;
                    fileLabel.setText(file.getName());
                }
            });

            // Upload dialog layout
            VBox form = new VBox(12);
            form.setPadding(new Insets(15));

            form.getChildren().addAll(
                    new Label("Select Course"),
                    courseComboBox,
                    new Label("Material Title"),
                    titleField,
                    chooseFileButton,
                    fileLabel
            );

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Upload Course Material");
            dialog.setHeaderText("Add New PDF Material");

            dialog.getDialogPane().setContent(form);

            ButtonType uploadType = new ButtonType(
                    "Upload",
                    ButtonBar.ButtonData.OK_DONE
            );

            dialog.getDialogPane()
                    .getButtonTypes()
                    .addAll(uploadType, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isEmpty() ||
                    result.get() != uploadType) {
                return;
            }

            String courseCode = courseComboBox.getValue();
            String materialTitle = titleField.getText().trim();

            // Validate input
            if (courseCode == null ||
                    materialTitle.isEmpty() ||
                    selectedPdf[0] == null) {

                new Alert(
                        Alert.AlertType.WARNING,
                        "Please select a course, enter a title, " +
                                "and choose a PDF file."
                ).showAndWait();
                return;
            }

            if (materialTitle.length() > 150) {
                new Alert(
                        Alert.AlertType.WARNING,
                        "Material title must be 150 characters or less."
                ).showAndWait();
                return;
            }

            File sourceFile = selectedPdf[0];

            if (!sourceFile.getName().toLowerCase()
                    .endsWith(".pdf")) {
                new Alert(
                        Alert.AlertType.WARNING,
                        "Only PDF files are allowed."
                ).showAndWait();
                return;
            }

            Path targetPath = null;

            try {
                // Maximum file size: 20 MB
                long maxSize = 20L * 1024 * 1024;

                long fileSize = Files.size(sourceFile.toPath());

                if (fileSize == 0 || fileSize > maxSize) {
                    new Alert(
                            Alert.AlertType.WARNING,
                            "PDF must be between 1 byte and 20 MB."
                    ).showAndWait();
                    return;
                }

                // Basic PDF signature validation
                try (InputStream input =
                             Files.newInputStream(sourceFile.toPath())) {

                    String signature = new String(
                            input.readNBytes(5),
                            StandardCharsets.US_ASCII
                    );

                    if (!"%PDF-".equals(signature)) {
                        new Alert(
                                Alert.AlertType.WARNING,
                                "Selected file is not a valid PDF header."
                        ).showAndWait();
                        return;
                    }
                }

                // Create upload directory
                Path uploadDirectory = Paths.get(
                        System.getProperty("user.dir"),
                        "uploads",
                        "materials"
                );

                Files.createDirectories(uploadDirectory);

                // Unique filename prevents overwriting
                String storedFileName =
                        UUID.randomUUID() + ".pdf";

                targetPath =
                        uploadDirectory.resolve(storedFileName);

                // Save PDF to local storage
                Files.copy(
                        sourceFile.toPath(),
                        targetPath,
                        StandardCopyOption.COPY_ATTRIBUTES
                );

                // Relative path stored in MySQL
                String databasePath =
                        "uploads/materials/" + storedFileName;

                CourseMaterialDAO materialDAO = new CourseMaterialDAO();

                materialDAO.addMaterial(
                        lecturerId,
                        courseCode,
                        materialTitle,
                        sourceFile.getName(),
                        databasePath
                );

                new Alert(
                        Alert.AlertType.INFORMATION,
                        "PDF uploaded successfully!"
                ).showAndWait();

                // Refresh Course Materials table
                showMaterials();

            } catch (IOException | SQLException e) {

                // Delete the copied file if database saving fails
                if (targetPath != null) {
                    try {
                        Files.deleteIfExists(targetPath);
                    } catch (IOException cleanupError) {
                        e.addSuppressed(cleanupError);
                    }
                }

                e.printStackTrace();

                new Alert(
                        Alert.AlertType.ERROR,
                        "Unable to upload PDF: " + e.getMessage()
                ).showAndWait();
            }
        }


    @FXML
    private void showMarks() {
        displayPage(
                "Student Marks",
                "Enter and manage examination marks.",
                marksBtn
        );
    }

    @FXML
    private void showStudents() {

        displayPage(
                "Undergraduate Details",
                "Students enrolled in your assigned courses.",
                studentsBtn
        );

        Label heading = new Label("Enrolled Students");

        heading.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        TableView<String[]> studentsTable = createTable(
                "No student enrollments found.",
                "Registration No",
                "Course Code",
                "Course Name",
                "Batch ID",
                "Status"
        );

        studentsTable.setPrefHeight(400);

        StudentDAO studentDAO = new StudentDAO();

        try {
            List<String[]> students =
                    studentDAO.getStudentsByLecturer(lecturerId);

            studentsTable.getItems().setAll(students);

        } catch (SQLException e) {

            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to load student records from database."
            ).showAndWait();
        }

        contentArea.getChildren().addAll(
                heading,
                studentsTable
        );
    }

    @FXML
    private void showEligibility() {
        displayPage(
                "Exam Eligibility",
                "View examination eligibility.",
                eligibilityBtn
        );
    }

    @FXML
    private void showAttendance() {
        displayPage(
                "Attendance",
                "View student attendance records.",
                attendanceBtn
        );
    }

    @FXML
    private void showMedicals() {
        displayPage(
                "Medical Records",
                "View student medical information.",
                medicalsBtn
        );
    }

    @FXML
    private void showGrades() {
        displayPage(
                "Grades & GPA",
                "View grades and GPA calculations.",
                gradesBtn
        );
    }

    @FXML
    private void showNotices() {
        displayPage(
                "Notices",
                "View faculty notices.",
                noticesBtn
        );
    }


    @FXML
    private void logout() {

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to log out?",
                ButtonType.YES,
                ButtonType.NO
        );

        confirmation.setTitle("Confirm Logout");
        confirmation.setHeaderText("Log Out");

        Optional<ButtonType> result =
                confirmation.showAndWait();

        if (result.isEmpty() ||
                result.get() != ButtonType.YES) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml")
            );

            Parent root = loader.load();

            Stage stage = (Stage)
                    contentArea.getScene().getWindow();

            stage.setTitle("University Management System - Login");
            stage.setScene(new Scene(root, 1100, 680));
            stage.centerOnScreen();

            // Clear lecturer information
            lecturerId = null;

        } catch (IOException e) {
            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to return to Login Page."
            ).showAndWait();
        }
    }

}
