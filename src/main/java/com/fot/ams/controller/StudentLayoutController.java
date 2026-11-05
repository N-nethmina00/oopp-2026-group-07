
package com.fot.ams.controller;

import com.fot.ams.dao.StudentDashboardDAO;
import com.fot.ams.dao.StudentProfileDAO;
import com.fot.ams.model.Student;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

// Main layout controller for the Student Portal.
public class StudentLayoutController {

    // CSS class for the selected sidebar button.
    private static final String ACTIVE = "nav-button-active";

    // Store the authenticated student's user ID.
    private String loggedInUserId;

    // Sidebar containing navigation buttons.
    @FXML private VBox navBox;

    // Main area where student pages are displayed.
    @FXML private StackPane contentArea;

    // Display the student's name in the header.
    @FXML private Label lblUserName;

    // Display the student's role in the header.
    @FXML private Label lblUserRole;

    // Display the first letter when no photo exists.
    @FXML private Label lblAvatar;

    // Display the student's saved profile picture.
    @FXML private ImageView imgHeaderAvatar;

    // Called automatically when the FXML file is loaded.
    @FXML
    private void initialize() {

        // Keep the header profile picture circular.
        imgHeaderAvatar.setClip(
                new Circle(23, 23, 23)
        );

        // Set default information before student login data loads.
        setUser("Student", "Undergraduate");

        // Initially hide the profile photo.
        imgHeaderAvatar.setVisible(false);

        // Open the Dashboard page by default.
        open((Button) navBox.getChildren().get(0));
    }

    // Update the student's name and role in the header.
    public void setUser(String name, String role) {

        // Handle a missing student name.
        if (name == null || name.isBlank()) {
            name = "Student";
        }

        // Display the student's full name.
        lblUserName.setText("Welcome, " + name);

        // Display the student's role.
        lblUserRole.setText(role);

        // Use the first letter as the default avatar.
        lblAvatar.setText(
                name.substring(0, 1).toUpperCase()
        );
    }

    // Handle sidebar navigation button clicks.
    @FXML
    private void navigate(ActionEvent e) {

        // Open the selected student page.
        open((Button) e.getSource());
    }

    // Open My Profile when the header avatar is clicked.
    @FXML
    private void openProfile(MouseEvent e) {

        // Search for the My Profile navigation button.
        for (Node n : navBox.getChildren()) {

            if ("student-profile".equals(n.getUserData())) {

                // Open the Student Profile page.
                open((Button) n);
                return;
            }
        }
    }

    // Open the selected page inside the Student Portal.
    private void open(Button btn) {

        // Get the page name from the selected button.
        String page = (String) btn.getUserData();

        try {

            // Create an FXMLLoader for the selected page.
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/" + page + ".fxml"
                    )
            );

            // Load the selected FXML interface.
            Parent root = loader.load();

            // Load dashboard data for the logged-in student.
            if ("student-dashboard".equals(page)
                    && loggedInUserId != null) {

                StudentDashboardController controller =
                        loader.getController();

                controller.loadDashboard(loggedInUserId);
            }

            // Load the student's enrolled courses.
            if ("student-courses".equals(page)
                    && loggedInUserId != null) {

                StudentCoursesController controller =
                        loader.getController();

                controller.loadCourses(loggedInUserId);
            }

            // Load the logged-in student's profile.
            if ("student-profile".equals(page)
                    && loggedInUserId != null) {

                StudentProfileController controller =
                        loader.getController();

                // Refresh the header after a successful profile save.
                controller.setOnProfileSaved(
                        this::refreshHeaderAvatar
                );

                // Load student profile information.
                controller.loadProfile(loggedInUserId);
            }

            // Load semester results.
            if ("student-results".equals(page)
                    && loggedInUserId != null) {

                StudentResultsController controller =
                        loader.getController();

                controller.loadResults(loggedInUserId);
            }

            // Load medical records.
            if ("student-medical".equals(page)
                    && loggedInUserId != null) {

                StudentMedicalController controller =
                        loader.getController();

                controller.loadMedical(loggedInUserId);
            }

            // Load student marks.
            if ("student-marks".equals(page)
                    && loggedInUserId != null) {

                StudentMarksController controller =
                        loader.getController();

                controller.loadMarks(loggedInUserId);
            }

            // Load learning materials.
            if ("student-materials".equals(page)
                    && loggedInUserId != null) {

                StudentMaterialsController controller =
                        loader.getController();

                controller.loadMaterials(loggedInUserId);
            }

            // Load attendance records.
            if ("student-attendance".equals(page)
                    && loggedInUserId != null) {

                StudentAttendanceController controller =
                        loader.getController();

                controller.loadAttendance(loggedInUserId);
            }

            // Replace current page with the selected page.
            contentArea.getChildren().setAll(root);

            // Remove active styles from all sidebar buttons.
            navBox.getChildren().forEach(
                    n -> n.getStyleClass().remove(ACTIVE)
            );

            // Highlight the selected sidebar button.
            btn.getStyleClass().add(ACTIVE);

        } catch (IOException | NullPointerException ex) {

            // Print the error for debugging.
            ex.printStackTrace();

            // Display an error when a page cannot load.
            new Alert(
                    Alert.AlertType.ERROR,
                    "Cannot load page: " + page + ".fxml"
            ).showAndWait();
        }
    }

    // Receive the logged-in user's ID from LoginController.
    public void loadStudentName(String userId) {

        // Store the authenticated user ID.
        this.loggedInUserId = userId;

        // Create DAO object to retrieve student information.
        StudentDashboardDAO dao = new StudentDashboardDAO();

        try {

            // Retrieve the student using the user ID.
            Student student = dao.getStudentByUserId(userId);

            // Check whether student information exists.
            if (student != null
                    && student.getFullName() != null
                    && !student.getFullName().isBlank()) {

                // Display actual student name and role.
                setUser(
                        student.getFullName(),
                        student.getRole()
                );

            } else {

                // Display default student details.
                setUser("Student", "Undergraduate");
            }

        } catch (SQLException e) {

            // Handle database errors.
            e.printStackTrace();

            // Display default information.
            setUser("Student", "Undergraduate");
        }

        // Load the student's saved profile photo.
        refreshHeaderAvatar();

        // Reload dashboard using the actual user ID.
        open((Button) navBox.getChildren().get(0));
    }

    // NEW METHOD:
    // Refresh the header avatar using the saved profile image.
    public void refreshHeaderAvatar() {

        // Do not load a photo without a valid user ID.
        if (loggedInUserId == null
                || loggedInUserId.isBlank()) {
            return;
        }

        try {

            // Create DAO object for profile data.
            StudentProfileDAO dao = new StudentProfileDAO();

            // Retrieve the latest saved student profile.
            Student student =
                    dao.getStudentProfile(loggedInUserId);

            if (student == null) {
                showDefaultAvatar();
                return;
            }

            // Update header name and role.
            setUser(
                    student.getFullName(),
                    student.getRole()
            );

            // Get the saved profile image path.
            String imagePath = student.getProfileImage();

            // Show the first letter if no image is saved.
            if (imagePath == null || imagePath.isBlank()) {
                showDefaultAvatar();
                return;
            }

            // Get the application working directory.
            Path projectRoot = Path.of(
                    System.getProperty("user.dir")
            ).toAbsolutePath().normalize();

            // Get the profile images folder.
            Path profilesFolder = projectRoot
                    .resolve("images")
                    .resolve("profiles")
                    .normalize();

            // Read the path stored in the database.
            Path storedPath = Path.of(
                    imagePath.replace('\\', '/')
            );

            // Support both a filename and a relative path.
            Path imageFile;

            if (storedPath.isAbsolute()) {

                // Use an absolute path directly.
                imageFile = storedPath.normalize();

            } else if (storedPath.getNameCount() == 1) {

                // Example: photo123.jpg
                imageFile = profilesFolder
                        .resolve(storedPath)
                        .normalize();

            } else {

                // Example: images/profiles/photo123.jpg
                imageFile = projectRoot
                        .resolve(storedPath)
                        .normalize();
            }

            // Only allow images from the profiles folder.
            if (!imageFile.startsWith(profilesFolder)) {
                showDefaultAvatar();
                return;
            }

            // Check whether the image file exists.
            if (!Files.isRegularFile(imageFile)
                    || !Files.isReadable(imageFile)) {

                showDefaultAvatar();
                return;
            }

            // Verify the real file stays inside the folder.
            if (!imageFile.toRealPath().startsWith(
                    profilesFolder.toRealPath())) {

                showDefaultAvatar();
                return;
            }

            // Load the image from the saved file.
            Image image = new Image(
                    imageFile.toUri().toString(),
                    false
            );

            // Check whether the image loaded successfully.
            if (image.isError()) {
                showDefaultAvatar();
                return;
            }

            // Display the profile image in the header.
            imgHeaderAvatar.setImage(image);
            imgHeaderAvatar.setVisible(true);

            // Hide the default first-letter avatar.
            lblAvatar.setVisible(false);

        } catch (SQLException | IOException
                 | RuntimeException e) {

            // Print the error for debugging.
            e.printStackTrace();

            // Use the default avatar when loading fails.
            showDefaultAvatar();
        }
    }

    // NEW METHOD:
    // Show the student's first letter when no image exists.
    private void showDefaultAvatar() {

        // Remove any previous profile picture.
        imgHeaderAvatar.setImage(null);

        // Hide the profile image view.
        imgHeaderAvatar.setVisible(false);

        // Display the student's first letter.
        lblAvatar.setVisible(true);
    }

    // Handle the Student Portal logout button.
    @FXML
    private void handleLogout(ActionEvent event) {

        // Ask the student to confirm logout.
        Alert a = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Do you want to logout?",
                ButtonType.YES,
                ButtonType.NO
        );

        // Remove the confirmation dialog header.
        a.setHeaderText(null);

        // Stop if the student selects No.
        if (a.showAndWait().orElse(ButtonType.NO)
                != ButtonType.YES) {
            return;
        }

        try {

            // Load the Login Page FXML file.
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml")
            );

            Parent loginRoot = loader.load();

            // Get the current application window.
            javafx.stage.Stage stage =
                    (javafx.stage.Stage) ((Node) event.getSource())
                            .getScene()
                            .getWindow();

            // Create a new login scene.
            javafx.scene.Scene scene =
                    new javafx.scene.Scene(loginRoot);

            // Replace the Student Portal with Login Page.
            stage.setScene(scene);

            // Update the window title.
            stage.setTitle("Login - AMS");

            // Center the login window.
            stage.centerOnScreen();

            // Display the Login Page.
            stage.show();

        } catch (IOException ex) {

            // Print the error for debugging.
            ex.printStackTrace();

            // Display an error if Login Page cannot load.
            new Alert(
                    Alert.AlertType.ERROR,
                    "Cannot load login page!"
            ).showAndWait();
        }
    }
}
