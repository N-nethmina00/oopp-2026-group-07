
package com.fot.ams.controller;


import com.fot.ams.dao.*;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.*;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.awt.Desktop;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.*;





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
                "Material ID",
                "Course Code",
                "Material Title",
                "Type",
                "File Name",
                "Uploaded Date"
        );

        materialsTable.setPrefHeight(400);


        Button openButton = new Button("Open PDF");
        Button deleteButton = new Button("Delete");

        openButton.setStyle(
                "-fx-background-color: #0877B8;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;"
        );

        deleteButton.setStyle(
                "-fx-background-color: #D9534F;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;"
        );

// Open selected PDF
        openButton.setOnAction(event -> {

            String[] selected =
                    materialsTable.getSelectionModel().getSelectedItem();

            if (selected == null) {
                new Alert(
                        Alert.AlertType.WARNING,
                        "Please select a material first."
                ).showAndWait();
                return;
            }

            try {
                int materialId = Integer.parseInt(selected[0]);

                CourseMaterialDAO dao = new CourseMaterialDAO();

                String filePath = dao.getMaterialPath(
                        materialId,
                        lecturerId
                );

                if (filePath == null || filePath.isBlank()) {
                    throw new IOException(
                            "Material not found or access denied."
                    );
                }

                Path projectRoot = Paths.get(
                        System.getProperty("user.dir")
                ).toAbsolutePath().normalize();

                Path pdfPath = projectRoot
                        .resolve(filePath)
                        .normalize();

                if (!pdfPath.startsWith(projectRoot)) {
                    throw new IOException("Invalid file path.");
                }

                if (!Files.isRegularFile(pdfPath)) {
                    throw new IOException(
                            "PDF file not found: " + filePath
                    );
                }

                if (!Desktop.isDesktopSupported() ||
                        !Desktop.getDesktop().isSupported(
                                Desktop.Action.OPEN)) {
                    throw new IOException(
                            "PDF opening is not supported."
                    );
                }

                Desktop.getDesktop().open(pdfPath.toFile());

            } catch (Exception e) {
                e.printStackTrace();

                new Alert(
                        Alert.AlertType.ERROR,
                        "Unable to open PDF: " + e.getMessage()
                ).showAndWait();
            }
        });

// Delete selected material record

        deleteButton.setOnAction(event -> {

            String[] selected =
                    materialsTable.getSelectionModel().getSelectedItem();

            if (selected == null) {
                new Alert(
                        Alert.AlertType.WARNING,
                        "Please select a material first."
                ).showAndWait();
                return;
            }

            Alert confirmation = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to delete this material?",
                    ButtonType.YES,
                    ButtonType.NO
            );

            confirmation.setTitle("Confirm Delete");
            confirmation.setHeaderText("Delete Course Material");

            if (confirmation.showAndWait().orElse(ButtonType.NO)
                    != ButtonType.YES) {
                return;
            }

            try {
                int materialId = Integer.parseInt(selected[0]);

                CourseMaterialDAO dao = new CourseMaterialDAO();

                // Get authorized material path before deleting record
                String filePath = dao.getMaterialPath(
                        materialId,
                        lecturerId
                );

                if (filePath == null || filePath.isBlank()) {
                    new Alert(
                            Alert.AlertType.WARNING,
                            "Material not found or access denied."
                    ).showAndWait();
                    return;
                }

                // Identify local uploads directory
                Path projectRoot = Paths.get(
                        System.getProperty("user.dir")
                ).toAbsolutePath().normalize();

                Path uploadDirectory = projectRoot.resolve(
                        "uploads/materials"
                ).normalize();

                Path pdfPath = projectRoot
                        .resolve(filePath)
                        .normalize();

                // Only files inside uploads/materials can be deleted
                boolean isManagedUpload =
                        pdfPath.startsWith(uploadDirectory)
                                && pdfPath.getFileName().toString()
                                .toLowerCase().endsWith(".pdf");

                // Delete database record
                boolean deleted = dao.deleteMaterial(
                        materialId,
                        lecturerId
                );

                if (!deleted) {
                    new Alert(
                            Alert.AlertType.WARNING,
                            "Material not found or access denied."
                    ).showAndWait();
                    return;
                }

                // Delete the physical file only for managed uploads
                if (isManagedUpload) {
                    try {
                        Files.deleteIfExists(pdfPath);
                    } catch (IOException fileError) {
                        fileError.printStackTrace();

                        new Alert(
                                Alert.AlertType.WARNING,
                                "Database record deleted, but the PDF " +
                                        "could not be removed from local storage."
                        ).showAndWait();

                        showMaterials();
                        return;
                    }
                }

                new Alert(
                        Alert.AlertType.INFORMATION,
                        isManagedUpload
                                ? "Material and uploaded PDF deleted successfully."
                                : "Material record deleted successfully."
                ).showAndWait();

                // Refresh table
                showMaterials();

            } catch (Exception e) {
                e.printStackTrace();

                new Alert(
                        Alert.AlertType.ERROR,
                        "Unable to delete material: " + e.getMessage()
                ).showAndWait();
            }
        });


// Arrange buttons horizontally
        HBox actionButtons = new HBox(10);
        actionButtons.getChildren().addAll(
                openButton,
                deleteButton
        );


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
                actionButtons,
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
                "View and manage marks for your assigned courses.",
                marksBtn
        );

        Label heading = new Label("Student Marks Management");

        heading.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        // Course selection
        Label courseLabel = new Label("Select Course Module");

        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.setPromptText("Select Your Course");
        courseComboBox.setPrefWidth(300);

        // Student marks table
        TableView<String[]> marksTable = createTable(
                "Please select a course to view student marks.",
                "Enrollment ID",
                "Registration No",
                "Quiz 1",
                "Quiz 2",
                "Quiz 3",
                "Assignment 1",
                "Assignment 2",
                "Mid Exam",
                "Quiz Total",
                "CA Total",
                "End Exam",
                "Final Total",
                "Grade",
                "Grade Point"
        );

        marksTable.setPrefHeight(450);

        // Hide internal Enrollment ID from UI
        marksTable.getColumns().get(0).setVisible(false);

        // Allow horizontal scrolling for marks columns
        marksTable.setColumnResizePolicy(
                TableView.UNCONSTRAINED_RESIZE_POLICY
        );

        marksTable.getColumns().forEach(column ->
                column.setPrefWidth(115)
        );

        // Load courses assigned to logged-in lecturer
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
                    "Unable to load lecturer courses."
            ).showAndWait();
        }

        // Load student marks after selecting a course
        courseComboBox.setOnAction(event -> {

            String selectedCourse = courseComboBox.getValue();

            marksTable.getItems().clear();

            if (selectedCourse == null) {
                return;
            }

            try {
                MarksDAO marksDAO = new MarksDAO();

                List<String[]> marks =
                        marksDAO.getMarksByLecturerCourse(
                                lecturerId,
                                selectedCourse
                        );

                marksTable.getItems().setAll(marks);

            } catch (SQLException e) {
                e.printStackTrace();

                new Alert(
                        Alert.AlertType.ERROR,
                        "Unable to load student marks."
                ).showAndWait();
            }
        });


        Button editMarksButton = new Button("Update Selected Marks");

        editMarksButton.setStyle(
                "-fx-background-color: #4c56cd;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 11 20;"
        );

        editMarksButton.disableProperty().bind(
                marksTable.getSelectionModel()
                        .selectedItemProperty().isNull()
        );

        editMarksButton.setOnAction(event -> {

            String[] selected =
                    marksTable.getSelectionModel().getSelectedItem();

            String selectedCourse = courseComboBox.getValue();

            if (selected == null || selectedCourse == null) {
                return;
            }

            boolean saved = showMarkEditorDialog(
                    selectedCourse, selected
            );

            if (saved) {
                try {
                    List<String[]> updatedMarks =
                            new MarksDAO().getMarksByLecturerCourse(
                                    lecturerId, selectedCourse
                            );

                    marksTable.getItems().setAll(updatedMarks);

                    new Alert(
                            Alert.AlertType.INFORMATION,
                            "Student marks saved successfully!"
                    ).showAndWait();

                } catch (SQLException e) {
                    e.printStackTrace();

                    new Alert(
                            Alert.AlertType.ERROR,
                            "Marks saved, but table refresh failed."
                    ).showAndWait();
                }
            }
        });


        Button addMarksButton = new Button("+ Add New Marks");

        addMarksButton.setStyle(
                "-fx-background-color: #198754;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 11 20;"
        );

        addMarksButton.setOnAction(event -> {

            String courseCode = courseComboBox.getValue();

            if (courseCode == null) {
                new Alert(
                        Alert.AlertType.WARNING,
                        "Please select a course first."
                ).showAndWait();
                return;
            }

            // Find enrolled students without existing marks
            List<String[]> newStudents =
                    marksTable.getItems().stream()
                            .filter(row ->
                                    row[2] == null &&
                                            row[10] == null
                            )
                            .toList();

            if (newStudents.isEmpty()) {
                new Alert(
                        Alert.AlertType.INFORMATION,
                        "No enrolled students without marks found."
                ).showAndWait();
                return;
            }

            // Student selection dialog
            List<String> studentNumbers =
                    newStudents.stream()
                            .map(row -> row[1])
                            .toList();

            ChoiceDialog<String> studentDialog =
                    new ChoiceDialog<>(
                            studentNumbers.get(0),
                            studentNumbers
                    );

            studentDialog.setTitle("Add New Marks");
            studentDialog.setHeaderText(
                    "Select a student for course " + courseCode
            );

            studentDialog.setContentText("Student Registration No:");

            Optional<String> result = studentDialog.showAndWait();

            if (result.isEmpty()) {
                return;
            }

            String selectedRegNo = result.get();

            String[] student = newStudents.stream()
                    .filter(row -> row[1].equals(selectedRegNo))
                    .findFirst()
                    .orElse(null);

            if (student == null) {
                return;
            }

            // Open existing marks entry dialog
            boolean saved = showMarkEditorDialog(
                    courseCode,
                    student
            );

            if (saved) {
                try {
                    List<String[]> updatedMarks =
                            new MarksDAO().getMarksByLecturerCourse(
                                    lecturerId,
                                    courseCode
                            );

                    marksTable.getItems().setAll(updatedMarks);

                    new Alert(
                            Alert.AlertType.INFORMATION,
                            "New student marks added successfully!"
                    ).showAndWait();

                } catch (SQLException e) {
                    e.printStackTrace();

                    new Alert(
                            Alert.AlertType.ERROR,
                            "Marks saved, but table refresh failed."
                    ).showAndWait();
                }
            }
        });

// Display action buttons horizontally
        HBox marksActionButtons = new HBox(10);

        marksActionButtons.getChildren().addAll(
                addMarksButton,
                editMarksButton
        );



        VBox courseSection = new VBox(10);
        courseSection.getChildren().addAll(
                courseLabel,
                courseComboBox
        );

        contentArea.getChildren().addAll(
                heading,
                courseSection,
                marksActionButtons,
                marksTable
        );
    }


    private boolean showMarkEditorDialog(
            String courseCode, String[] student) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add / Update Student Marks");
        dialog.setHeaderText(
                "Student: " + student[1] +
                        " | Course: " + courseCode
        );

        String[] labels = {
                "Quiz 1 (10)",
                "Quiz 2 (10)",
                "Quiz 3 (10)",
                "Assignment 1 (20)",
                "Assignment 2 (20)",
                "Mid Exam (30)",
                "End Exam (100)"
        };

        // Column positions from MarksDAO result
        int[] indices = {2, 3, 4, 5, 6, 7, 10};

        TextField[] fields = new TextField[7];

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        for (int i = 0; i < fields.length; i++) {

            String existingValue = student[indices[i]];

            fields[i] = new TextField(
                    existingValue == null ? "" : existingValue
            );

            fields[i].setPromptText("Enter marks");
            fields[i].setPrefWidth(180);

            grid.add(new Label(labels[i]), 0, i);
            grid.add(fields[i], 1, i);
        }

        Label previewLabel = new Label();
        previewLabel.setWrapText(true);

        previewLabel.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #17324D;" +
                        "-fx-background-color: #F0F3FA;" +
                        "-fx-padding: 12;"
        );

        MarksDAO dao = new MarksDAO();

        // Update calculated results while typing
        Runnable updatePreview = () -> {
            try {
                double[] v = readMarkInputs(fields, labels);

                previewLabel.setText(
                        dao.getMarksPreview(
                                v[0], v[1], v[2],
                                v[3], v[4], v[5], v[6]
                        )
                );

            } catch (IllegalArgumentException e) {
                previewLabel.setText(
                        "Enter valid marks in all seven fields " +
                                "to see the calculation."
                );
            }
        };

        for (TextField field : fields) {
            field.textProperty().addListener(
                    (observable, oldValue, newValue) ->
                            updatePreview.run()
            );
        }

        updatePreview.run();

        VBox form = new VBox(15);
        form.setPadding(new Insets(15));
        form.getChildren().addAll(grid, previewLabel);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(480);

        ButtonType saveType = new ButtonType(
                "Save Marks", ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveType, ButtonType.CANCEL
        );

        final boolean[] saved = {false};

        Button saveButton = (Button)
                dialog.getDialogPane().lookupButton(saveType);

        // Validate and save before allowing dialog to close
        saveButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {

                    try {
                        double[] v = readMarkInputs(fields, labels);

                        int enrollmentId =
                                Integer.parseInt(student[0]);

                        dao.saveStudentMarks(
                                lecturerId,
                                courseCode,
                                enrollmentId,
                                v[0], v[1], v[2],
                                v[3], v[4], v[5], v[6]
                        );

                        saved[0] = true;

                    } catch (SQLException |
                             IllegalArgumentException e) {

                        e.printStackTrace();

                        new Alert(
                                Alert.AlertType.ERROR,
                                "Unable to save marks: " +
                                        e.getMessage()
                        ).showAndWait();

                        // Keep dialog open on failure
                        event.consume();
                    }
                }
        );

        dialog.showAndWait();

        return saved[0];
    }

    // Read the seven input fields
    private double[] readMarkInputs(
            TextField[] fields, String[] labels) {

        double[] values = new double[fields.length];

        for (int i = 0; i < fields.length; i++) {

            String text = fields[i].getText().trim();

            if (text.isEmpty()) {
                throw new IllegalArgumentException(
                        labels[i] + " is required."
                );
            }

            try {
                values[i] = Double.parseDouble(text);

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        labels[i] + " must be a valid number."
                );
            }
        }

        return values;
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
                "View student grades and grade points.",
                gradesBtn
        );

        Label heading = new Label("Student Grades & Results");

        heading.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        Label courseLabel = new Label("Select Course Module");

        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.setPromptText("Select Your Course");
        courseComboBox.setPrefWidth(300);

        TableView<String[]> gradesTable = createTable(
                "Select a course to view student grades.",
                "Registration No",
                "Raw CA (/70)",
                "End Exam (/100)",
                "Final Total (/100)",
                "Grade",
                "Grade Point"
        );

        gradesTable.setPrefHeight(400);

        Label note = new Label(
                "Grade Point is the result for one course. " +
                        "Overall GPA is calculated using course credits."
        );

        note.setWrapText(true);
        note.setStyle(
                "-fx-text-fill: #526174;" +
                        "-fx-font-size: 12px;"
        );

        // Load assigned courses
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
        }

        // Load grades when a course is selected
        courseComboBox.setOnAction(event -> {

            String courseCode = courseComboBox.getValue();

            gradesTable.getItems().clear();

            if (courseCode == null) {
                return;
            }

            try {
                MarksDAO marksDAO = new MarksDAO();

                List<String[]> marks =
                        marksDAO.getMarksByLecturerCourse(
                                lecturerId,
                                courseCode
                        );

                List<String[]> results = new java.util.ArrayList<>();

                for (String[] row : marks) {

                    // Only display students with saved results
                    if (row[11] == null || row[12] == null) {
                        continue;
                    }

                    results.add(new String[]{
                            row[1],   // Registration No
                            row[9],   // Raw CA
                            row[10],  // End Exam
                            row[11],  // Final Total
                            row[12],  // Grade
                            row[13]   // Grade Point
                    });
                }

                gradesTable.getItems().setAll(results);

            } catch (SQLException e) {
                e.printStackTrace();

                new Alert(
                        Alert.AlertType.ERROR,
                        "Unable to load student grades."
                ).showAndWait();
            }
        });

        contentArea.getChildren().addAll(
                heading,
                courseLabel,
                courseComboBox,
                gradesTable,
                note
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
