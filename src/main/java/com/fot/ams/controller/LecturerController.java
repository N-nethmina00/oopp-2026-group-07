package com.fot.ams.controller;


import com.fot.ams.dao.*;
import com.fot.ams.model.*;

import com.fot.ams.model.EligibilityRecord;
import com.fot.ams.model.Notice;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.*;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.shape.Circle;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

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

        LecturerDashboardDAO dashboardDAO =
                new LecturerDashboardDAO();

        String studentsCount = "—";
        String pendingMarksCount = "—";
        String materialsCount = "—";

        List<String[]> teachingSessions =
                new java.util.ArrayList<>();

        List<String[]> studentRecords =
                new java.util.ArrayList<>();

        // Load real dashboard data
        try {
            studentsCount = String.valueOf(
                    dashboardDAO.countStudents(lecturerId)
            );

            pendingMarksCount = String.valueOf(
                    dashboardDAO.countPendingMarks(lecturerId)
            );

            materialsCount = String.valueOf(
                    dashboardDAO.countMaterials(lecturerId)
            );

            teachingSessions =
                    dashboardDAO.getTeachingSessions(lecturerId);

            studentRecords =
                    dashboardDAO.getStudentOverview(lecturerId);

        } catch (SQLException e) {
            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to load some lecturer dashboard data."
            ).showAndWait();
        }

        // Dashboard statistic cards
        HBox statsRow = new HBox(15);

        statsRow.getChildren().addAll(

                createStatCard(
                        "My Courses",
                        loadMyCoursesCount(),
                        "Assigned courses"
                ),

                createStatCard(
                        "Students",
                        studentsCount,
                        "Enrolled students"
                ),

                createStatCard(
                        "Pending Marks",
                        pendingMarksCount,
                        "Incomplete results"
                ),

                createStatCard(
                        "Materials",
                        materialsCount,
                        "Uploaded materials"
                )
        );

        // Teaching Schedule
        Label scheduleTitle =
                new Label("Teaching Schedule");

        scheduleTitle.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        TableView<String[]> scheduleTable = createTable(
                "No teaching sessions found.",
                "Day / Date",
                "Time",
                "Course",
                "Session Type"
        );

        scheduleTable.setPrefHeight(250);

        scheduleTable.getItems().setAll(teachingSessions);

        // Students Overview
        Label studentsTitle =
                new Label("Students Overview");

        studentsTitle.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        TableView<String[]> studentsTable = createTable(
                "No enrolled students found.",
                "Registration No",
                "Student Name",
                "Course",
                "Status"
        );

        studentsTable.setPrefHeight(350);

        studentsTable.getItems().setAll(studentRecords);

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
                "View your personal and academic information.",
                profileBtn
        );

        Label heading = new Label("Lecturer Profile");

        heading.setStyle(
                "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        try {

            LecturerProfileDAO dao =
                    new LecturerProfileDAO();

            Lecturer lecturer =
                    dao.getLecturerProfile(lecturerId);

            if (lecturer == null) {
                new Alert(
                        Alert.AlertType.WARNING,
                        "Lecturer profile not found."
                ).showAndWait();
                return;
            }

            User user = lecturer.getUser();

            GridPane profileGrid = new GridPane();
            profileGrid.setHgap(25);
            profileGrid.setVgap(18);
            profileGrid.setPadding(new Insets(25));

            String[][] details = {
                    {"Lecturer ID", lecturer.getLecturerId()},
                    {"Full Name", user.getFullName()},
                    {"Username", user.getUsername()},
                    {"Email Address", user.getEmail()},
                    {"Department", lecturer.getDepartmentName()},
                    {"Role", user.getRole()}
            };

            for (int i = 0; i < details.length; i++) {

                Label fieldName = new Label(details[i][0]);

                fieldName.setStyle(
                        "-fx-font-weight: bold;" +
                                "-fx-text-fill: #526174;" +
                                "-fx-font-size: 13px;"
                );

                Label fieldValue = new Label(
                        details[i][1] == null
                                ? "Not Available"
                                : details[i][1]
                );

                fieldValue.setWrapText(true);

                fieldValue.setStyle(
                        "-fx-text-fill: #17324D;" +
                                "-fx-font-size: 14px;"
                );

                profileGrid.add(fieldName, 0, i);
                profileGrid.add(fieldValue, 1, i);
            }

            VBox profileCard = new VBox(15);
            profileCard.setPadding(new Insets(20));

            profileCard.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: #D5DCE5;" +
                            "-fx-border-radius: 10;"
            );

            profileCard.getChildren().add(profileGrid);


            String profileImagePath =
                    dao.getProfileImagePath(lecturerId);

// Show profile picture above the details
            profileCard.getChildren().add(
                    0,
                    createLecturerPhoto(profileImagePath)
            );

// Edit Profile button
            Button editProfileButton =
                    new Button("Edit My Profile");

            editProfileButton.setStyle(
                    "-fx-background-color: #4c56cd;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 11 20;"
            );

            editProfileButton.setOnAction(
                    event -> showEditProfileDialog(lecturer)
            );


            contentArea.getChildren().addAll(
                    heading,
                    profileCard,
                    editProfileButton
            );

        } catch (SQLException e) {

            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to load lecturer profile."
            ).showAndWait();
        }
    }

    private javafx.scene.Node createLecturerPhoto(String storedPath) {

        final double size = 120;

        if (storedPath != null && !storedPath.isBlank()) {

            Path root = Paths.get(
                    System.getProperty("user.dir")
            ).toAbsolutePath().normalize();

            Path folder = root.resolve(
                    "uploads/profiles"
            ).normalize();

            Path imagePath = root.resolve(storedPath).normalize();

            if (imagePath.startsWith(folder) &&
                    Files.isRegularFile(imagePath)) {

                Image image = new Image(
                        imagePath.toUri().toString()
                );

                if (!image.isError()) {

                    ImageView imageView = new ImageView(image);

                    // Crop the image to a centered square
                    double imageWidth = image.getWidth();
                    double imageHeight = image.getHeight();

                    double cropSize = Math.min(
                            imageWidth, imageHeight
                    );

                    imageView.setViewport(new Rectangle2D(
                            (imageWidth - cropSize) / 2,
                            (imageHeight - cropSize) / 2,
                            cropSize,
                            cropSize
                    ));

                    imageView.setFitWidth(size);
                    imageView.setFitHeight(size);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);

                    // Make profile picture circular
                    Circle circle = new Circle(
                            size / 2,
                            size / 2,
                            size / 2
                    );

                    imageView.setClip(circle);

                    return imageView;
                }
            }
        }

        // Circular placeholder when no picture exists
        Label placeholder = new Label("👤");

        placeholder.setAlignment(
                javafx.geometry.Pos.CENTER
        );

        placeholder.setPrefSize(size, size);
        placeholder.setMinSize(size, size);
        placeholder.setMaxSize(size, size);

        placeholder.setStyle(
                "-fx-background-color: #E8EAFB;" +
                        "-fx-background-radius: 60;" +
                        "-fx-font-size: 42px;" +
                        "-fx-text-fill: #4c56cd;"
        );

        return placeholder;
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
            List<Course> courses =
                    courseDAO.getCourseModelsByLecturer(lecturerId);

            List<String[]> rows = new ArrayList<>();

            for (Course course : courses) {
                rows.add(new String[]{
                        course.getCourseCode(),
                        course.getCourseName(),
                        String.valueOf(course.getCredit()),
                        course.getType()
                });
            }

            coursesTable.getItems().setAll(rows);

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
            List<CourseMaterial> materials =
                    materialDAO.getMaterialModelsByLecturer(lecturerId);

            List<String[]> rows = new ArrayList<>();

            for (CourseMaterial material : materials) {
                rows.add(new String[]{
                        String.valueOf(material.getMaterialId()),
                        material.getCourseCode(),
                        material.getMaterialTitle(),
                        material.getMaterialType(),
                        material.getFileName(),
                        material.getUploadedDate() == null
                                ? "" : material.getUploadedDate().toString()
                });
            }

            materialsTable.getItems().setAll(rows);

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

            List<Course> courses =
                    courseDAO.getCourseModelsByLecturer(lecturerId);

            for (Course course : courses) {
                courseComboBox.getItems().add(course.getCourseCode());
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

            List<Course> courses =
                    courseDAO.getCourseModelsByLecturer(lecturerId);

            for (Course course : courses) {
                courseComboBox.getItems().add(course.getCourseCode());
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
                "View attendance and CA eligibility records.",
                eligibilityBtn
        );

        Label heading = new Label("Student Exam Eligibility");

        heading.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        Label courseLabel = new Label("Select Course Module");

        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.setPromptText("Select Your Course");
        courseComboBox.setPrefWidth(300);

        // Exam Eligibility Table
        TableView<EligibilityRecord> eligibilityTable =
                new TableView<>();

        eligibilityTable.setPlaceholder(
                new Label("Select a course to view eligibility.")
        );

        eligibilityTable.setPrefHeight(420);

        eligibilityTable.setColumnResizePolicy(
                TableView.UNCONSTRAINED_RESIZE_POLICY
        );

        eligibilityTable.getColumns().forEach(column ->
                column.setPrefWidth(145)
        );

        // Registration Number
        TableColumn<EligibilityRecord, String> regCol =
                new TableColumn<>("Registration No");

        regCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getRegistrationNo()
                )
        );

        // Course Code
        TableColumn<EligibilityRecord, String> courseCol =
                new TableColumn<>("Course Code");

        courseCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getCourseCode()
                )
        );

        // Recorded Attendance Percentage
        TableColumn<EligibilityRecord, String> attendanceCol =
                new TableColumn<>("Present % (Recorded)");

        attendanceCol.setCellValueFactory(data -> {

            Double percentage =
                    data.getValue().getAttendancePercentage();

            String value = percentage == null
                    ? "Not Recorded"
                    : String.format(
                    java.util.Locale.US,
                    "%.2f%%",
                    percentage
            );

            return new ReadOnlyStringWrapper(value);
        });


        // Recorded sessions compared with scheduled sessions
        TableColumn<EligibilityRecord, String> sessionsCol =
                new TableColumn<>("Recorded / Sessions");

        sessionsCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getRecordedSessions()
                                + " / " +
                                data.getValue().getScheduledSessions()
                )
        );

        // Theory attendance percentage
        TableColumn<EligibilityRecord, String> theoryCol =
                new TableColumn<>("Theory Present %");

        theoryCol.setCellValueFactory(data -> {
            Double value = data.getValue().getTheoryPercentage();

            return new ReadOnlyStringWrapper(
                    value == null
                            ? "Not Recorded"
                            : String.format(
                            java.util.Locale.US, "%.2f%%", value
                    )
            );
        });

        // Practical attendance percentage
        TableColumn<EligibilityRecord, String> practicalCol =
                new TableColumn<>("Practical Present %");

        practicalCol.setCellValueFactory(data -> {
            Double value = data.getValue().getPracticalPercentage();

            return new ReadOnlyStringWrapper(
                    value == null
                            ? "Not Recorded"
                            : String.format(
                            java.util.Locale.US, "%.2f%%", value
                    )
            );
        });

        // Approved medical count
        TableColumn<EligibilityRecord, String> approvedCol =
                new TableColumn<>("Approved Medical");

        approvedCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        String.valueOf(
                                data.getValue().getApprovedMedicalCount()
                        )
                )
        );

        // Pending medical count
        TableColumn<EligibilityRecord, String> pendingCol =
                new TableColumn<>("Pending Medical");

        pendingCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        String.valueOf(
                                data.getValue().getPendingMedicalCount()
                        )
                )
        );


        // CA Status
        TableColumn<EligibilityRecord, String> caStatusCol =
                new TableColumn<>("CA Status");

        caStatusCol.setCellValueFactory(data -> {

            String status = data.getValue().getCaStatus();

            return new ReadOnlyStringWrapper(
                    status == null ? "Not Available" : status
            );
        });

        // CA Marks
        TableColumn<EligibilityRecord, String> caMarksCol =
                new TableColumn<>("Stored CA Marks");

        caMarksCol.setCellValueFactory(data -> {

            Double marks = data.getValue().getCaMarks();

            return new ReadOnlyStringWrapper(
                    marks == null
                            ? "Not Available"
                            : String.format(
                            java.util.Locale.US,
                            "%.2f",
                            marks
                    )
            );
        });

        // Database Eligibility Flag
        TableColumn<EligibilityRecord, String> eligibilityCol =
                new TableColumn<>("Eligibility DB Flag");

        eligibilityCol.setCellValueFactory(data -> {

            Integer flag =
                    data.getValue().getEligibilityFlag();

            String value = flag == null
                    ? "Not Available"
                    : String.valueOf(flag);

            return new ReadOnlyStringWrapper(value);
        });

        eligibilityTable.getColumns().addAll(
                regCol,
                courseCol,
                attendanceCol,
                sessionsCol,
                theoryCol,
                practicalCol,
                caStatusCol,
                caMarksCol,
                approvedCol,
                pendingCol,
                eligibilityCol
        );

        // Load Lecturer's Assigned Courses
        try {

            CourseDAO courseDAO = new CourseDAO();

            List<Course> courses =
                    courseDAO.getCourseModelsByLecturer(lecturerId);

            for (Course course : courses) {
                courseComboBox.getItems().add(course.getCourseCode());
            }

        } catch (SQLException e) {

            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to load assigned courses."
            ).showAndWait();
        }

        // Load Eligibility Records
        courseComboBox.setOnAction(event -> {

            String selectedCourse = courseComboBox.getValue();

            eligibilityTable.getItems().clear();

            if (selectedCourse == null) {
                return;
            }

            try {

                EligibilityDAO eligibilityDAO =
                        new EligibilityDAO();

                List<EligibilityRecord> records =
                        eligibilityDAO.getEligibilityByLecturerCourse(
                                lecturerId,
                                selectedCourse
                        );

                eligibilityTable.getItems().setAll(records);

            } catch (SQLException e) {

                e.printStackTrace();

                new Alert(
                        Alert.AlertType.ERROR,
                        "Unable to load exam eligibility records."
                ).showAndWait();
            }
        });


        contentArea.getChildren().addAll(
                heading,
                courseLabel,
                courseComboBox,
                eligibilityTable
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


        // Load assigned courses
        try {
            CourseDAO courseDAO = new CourseDAO();

            List<Course> courses =
                    courseDAO.getCourseModelsByLecturer(lecturerId);

            for (Course course : courses) {
                courseComboBox.getItems().add(course.getCourseCode());
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

                List<Grade> grades =
                        marksDAO.getGradeModelsByLecturerCourse(
                                lecturerId, courseCode
                        );

                List<Mark> markModels =
                        marksDAO.getMarkModelsByLecturerCourse(
                                lecturerId, courseCode
                        );

                List<CAEvaluation> caModels =
                        marksDAO.getCAEvaluationsByLecturerCourse(
                                lecturerId, courseCode
                        );

                Map<Integer, Mark> markByEnrollment = new HashMap<>();
                for (Mark mark : markModels) {
                    markByEnrollment.put(mark.getEnrollmentId(), mark);
                }

                Map<Integer, CAEvaluation> caByEnrollment = new HashMap<>();
                for (CAEvaluation ca : caModels) {
                    caByEnrollment.put(ca.getEnrollmentId(), ca);
                }

                List<String[]> results = new ArrayList<>();

                for (Grade grade : grades) {
                    // Only show saved final results
                    if (grade.getTotalMarks() == null || grade.getGrade() == null) {
                        continue;
                    }

                    CAEvaluation ca = caByEnrollment.get(grade.getEnrollmentId());
                    Mark mark = markByEnrollment.get(grade.getEnrollmentId());

                    results.add(new String[]{
                            grade.getRegistrationNo(),
                            formatMark(ca == null ? null : ca.getTotalCAMarks()),
                            formatMark(mark == null ? null : mark.getEndExamMarks()),
                            formatMark(grade.getTotalMarks()),
                            grade.getGrade(),
                            formatMark(grade.getGradePoint())
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
                gradesTable
        );
    }



    @FXML
    private void showNotices() {

        displayPage(
                "Notices",
                "View faculty notices and announcements.",
                noticesBtn
        );

        Label heading = new Label("Faculty Notices");

        heading.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        // Notice table
        TableView<Notice> noticesTable = new TableView<>();

        noticesTable.setPlaceholder(
                new Label("No published notices available.")
        );

        noticesTable.setPrefHeight(300);

        noticesTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        // Title column
        TableColumn<Notice, String> titleCol =
                new TableColumn<>("Notice Title");

        titleCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getTitle() == null
                                ? ""
                                : data.getValue().getTitle()
                )
        );

        // Published date column
        TableColumn<Notice, String> dateCol =
                new TableColumn<>("Published Date");

        dateCol.setCellValueFactory(data -> {

            java.time.LocalDate date =
                    data.getValue().getPublishedDate();

            return new ReadOnlyStringWrapper(
                    date == null ? "" : date.toString()
            );
        });

        noticesTable.getColumns().addAll(
                titleCol,
                dateCol
        );

        titleCol.setPrefWidth(420);
        dateCol.setPrefWidth(160);

        // Selected notice details
        Label detailsHeading = new Label("Notice Details");

        detailsHeading.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        TextArea noticeContent = new TextArea();

        noticeContent.setEditable(false);
        noticeContent.setWrapText(true);
        noticeContent.setPrefRowCount(7);

        noticeContent.setPromptText(
                "Select a notice to view its content."
        );

        noticeContent.setStyle(
                "-fx-control-inner-background: #F8FAFC;" +
                        "-fx-font-size: 13px;"
        );

        // Show notice content on row selection
        noticesTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldNotice, selectedNotice) -> {

                    if (selectedNotice == null) {
                        noticeContent.clear();
                        return;
                    }

                    noticeContent.setText(
                            selectedNotice.getContent() == null
                                    ? ""
                                    : selectedNotice.getContent()
                    );
                });

        // Load published notices from database
        try {

            NoticeDAO noticeDAO = new NoticeDAO();

            List<Notice> notices =
                    noticeDAO.getAllNotices();

            java.time.LocalDate today =
                    java.time.LocalDate.now();

            for (Notice notice : notices) {

                java.time.LocalDate publishedDate =
                        notice.getPublishedDate();

                // Only display notices published by today
                if (publishedDate != null &&
                        !publishedDate.isAfter(today)) {

                    noticesTable.getItems().add(notice);
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to load faculty notices."
            ).showAndWait();
        }

        contentArea.getChildren().addAll(
                heading,
                noticesTable,
                detailsHeading,
                noticeContent
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


    private void showEditProfileDialog(Lecturer lecturer) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit My Profile");
        dialog.setHeaderText("Update Lecturer Profile");

        TextField usernameField =
                new TextField(lecturer.getUser().getUsername());

        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Enter current password");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Leave blank to keep password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");

        Label pictureLabel = new Label("No new picture selected");

        final File[] selectedPicture = {null};

        Button choosePictureButton =
                new Button("Choose Profile Picture");

        choosePictureButton.setOnAction(event -> {

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Profile Picture");

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "Image Files",
                            "*.png", "*.jpg", "*.jpeg"
                    )
            );

            File file = chooser.showOpenDialog(
                    contentArea.getScene().getWindow()
            );

            if (file != null) {
                selectedPicture[0] = file;
                pictureLabel.setText(file.getName());
            }
        });

        VBox form = new VBox(10);
        form.setPadding(new Insets(18));

        form.getChildren().addAll(
                new Label("Username"),
                usernameField,
                new Label("Current Password"),
                currentPasswordField,
                new Label("New Password (Optional)"),
                newPasswordField,
                new Label("Confirm New Password"),
                confirmPasswordField,
                new Label("Profile Picture (Optional)"),
                choosePictureButton,
                pictureLabel
        );

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(450);

        ButtonType saveType = new ButtonType(
                "Save Changes",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveType,
                ButtonType.CANCEL
        );

        Button saveButton = (Button)
                dialog.getDialogPane().lookupButton(saveType);

        final boolean[] saved = {false};

        saveButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {

                    Path copiedFile = null;

                    try {

                        String username =
                                usernameField.getText().trim();

                        String currentPassword =
                                currentPasswordField.getText();

                        String newPassword =
                                newPasswordField.getText();

                        String confirmPassword =
                                confirmPasswordField.getText();

                        if (!username.matches("[A-Za-z0-9._-]{3,50}")) {
                            throw new IllegalArgumentException(
                                    "Invalid username. Use 3-50 characters."
                            );
                        }

                        if (currentPassword.isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Current password is required."
                            );
                        }

                        if (!newPassword.isEmpty()) {

                            if (newPassword.isBlank() ||
                                    newPassword.length() < 8) {

                                throw new IllegalArgumentException(
                                        "New password must have at least 8 characters."
                                );
                            }

                            if (!newPassword.equals(confirmPassword)) {

                                throw new IllegalArgumentException(
                                        "Passwords do not match."
                                );
                            }

                        } else if (!confirmPassword.isEmpty()) {

                            throw new IllegalArgumentException(
                                    "Enter a new password first."
                            );
                        }

                        String newImagePath = null;

                        if (selectedPicture[0] != null) {

                            File source = selectedPicture[0];

                            String name = source.getName()
                                    .toLowerCase(java.util.Locale.ROOT);

                            boolean png = name.endsWith(".png");
                            boolean jpg = name.endsWith(".jpg")
                                    || name.endsWith(".jpeg");

                            if (!png && !jpg) {
                                throw new IllegalArgumentException(
                                        "Only PNG and JPG images allowed."
                                );
                            }

                            long fileSize =
                                    Files.size(source.toPath());

                            if (fileSize == 0 ||
                                    fileSize > 5L * 1024 * 1024) {

                                throw new IllegalArgumentException(
                                        "Image must be 5 MB or less."
                                );
                            }

                            BufferedImage checkedImage =
                                    ImageIO.read(source);

                            if (checkedImage == null ||
                                    checkedImage.getWidth() > 4096 ||
                                    checkedImage.getHeight() > 4096) {

                                throw new IllegalArgumentException(
                                        "Invalid or oversized image."
                                );
                            }

                            Path folder = Paths.get(
                                    System.getProperty("user.dir"),
                                    "uploads",
                                    "profiles"
                            );

                            Files.createDirectories(folder);

                            String filename = UUID.randomUUID() +
                                    (png ? ".png" : ".jpg");

                            copiedFile = folder.resolve(filename);

                            Files.copy(
                                    source.toPath(),
                                    copiedFile
                            );

                            newImagePath =
                                    "uploads/profiles/" + filename;
                        }

                        LecturerProfileDAO dao =
                                new LecturerProfileDAO();

                        boolean updated = dao.updateProfile(
                                lecturer.getLecturerId(),
                                username,
                                currentPassword,
                                newPassword,
                                newImagePath
                        );

                        if (!updated) {
                            throw new IllegalArgumentException(
                                    "Current password is incorrect."
                            );
                        }

                        saved[0] = true;

                    } catch (Exception e) {

                        if (copiedFile != null) {
                            try {
                                Files.deleteIfExists(copiedFile);
                            } catch (IOException cleanupError) {
                                e.addSuppressed(cleanupError);
                            }
                        }

                        e.printStackTrace();

                        String message =
                                e instanceof java.sql.SQLIntegrityConstraintViolationException
                                        ? "Username is already in use."
                                        : e.getMessage();

                        new Alert(
                                Alert.AlertType.ERROR,
                                "Unable to update profile: " + message
                        ).showAndWait();

                        event.consume();
                    }
                }
        );

        dialog.showAndWait();

        if (saved[0]) {

            new Alert(
                    Alert.AlertType.INFORMATION,
                    "Profile updated successfully!"
            ).showAndWait();

            showProfile();
        }
    }


    private static String formatMark(Double value) {
        return value == null ? "" : String.format(Locale.US, "%.2f", value);
    }
}
