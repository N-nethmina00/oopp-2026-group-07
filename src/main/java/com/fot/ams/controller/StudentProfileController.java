
package com.fot.ams.controller;

import com.fot.ams.dao.StudentProfileDAO;
import com.fot.ams.model.Student;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.UUID;

public class StudentProfileController {

    @FXML
    private Label lblPhoto, lblMessage;

    @FXML
    private TextField txtFirstName, txtLastName,
            txtEmail, txtRegNo;

    @FXML
    private ImageView imgProfile;

    private String loggedInUserId;
    private String currentPhotoPath;
    private File selectedPhotoFile;

    private final StudentProfileDAO profileDAO =
            new StudentProfileDAO();

    @FXML
    private void initialize() {

        // Only email and profile picture can be edited
        txtFirstName.setEditable(false);
        txtLastName.setEditable(false);
        txtRegNo.setEditable(false);

        txtEmail.textProperty().addListener(
                (obs, oldValue, newValue) -> hideMessage()
        );
    }

    // Called from StudentLayoutController
    public void loadProfile(String userId) {

        this.loggedInUserId = userId;

        try {

            Student student =
                    profileDAO.getStudentProfile(userId);

            if (student == null) {
                showMessage("Student profile not found.", false);
                return;
            }

            // Load student information from Model
            txtFirstName.setText(student.getFirstName());
            txtLastName.setText(student.getLastName());
            txtEmail.setText(student.getEmail());
            txtRegNo.setText(student.getRegNo());

            currentPhotoPath = student.getProfileImage();
            selectedPhotoFile = null;

            // Set avatar letter
            String firstName = student.getFirstName();

            lblPhoto.setText(
                    firstName == null || firstName.isBlank()
                            ? "?"
                            : firstName.substring(0, 1).toUpperCase()
            );

            // Load saved profile picture
            displayPhoto(currentPhotoPath);

        } catch (SQLException e) {

            e.printStackTrace();

            showMessage(
                    "Unable to load profile: " + e.getMessage(),
                    false
            );
        }
    }

    // Display image or default avatar
    private void displayPhoto(String path) {

        imgProfile.setImage(null);
        imgProfile.setVisible(false);
        lblPhoto.setVisible(true);

        if (path == null || path.isBlank()) {
            return;
        }

        try {

            File file = Path.of(
                    System.getProperty("user.dir"),
                    path
            ).toFile();

            if (!file.isFile()) {
                return;
            }

            Image image = new Image(
                    file.toURI().toString(),
                    false
            );

            if (!image.isError()) {

                imgProfile.setImage(image);
                imgProfile.setVisible(true);
                lblPhoto.setVisible(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Select new profile picture
    @FXML
    private void handleChangePhoto() {

        FileChooser chooser = new FileChooser();

        chooser.setTitle("Select Profile Picture");

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png", "*.jpg", "*.jpeg"
                )
        );

        File file = chooser.showOpenDialog(
                imgProfile.getScene().getWindow()
        );

        if (file == null) {
            return;
        }

        try {

            // Maximum 5 MB, non-empty
            if (file.length() == 0 ||
                    file.length() > 5 * 1024 * 1024) {

                showMessage(
                        "Image must be between 1 byte and 5 MB.",
                        false
                );
                return;
            }

            String name = file.getName().toLowerCase();

            if (!(name.endsWith(".png") ||
                    name.endsWith(".jpg") ||
                    name.endsWith(".jpeg"))) {

                showMessage("Select JPG or PNG image.", false);
                return;
            }

            Image image = new Image(
                    file.toURI().toString(),
                    false
            );

            if (image.isError() ||
                    image.getWidth() <= 0 ||
                    image.getHeight() <= 0) {

                showMessage("Invalid image file.", false);
                return;
            }

            selectedPhotoFile = file;

            // Preview only, not saved yet
            imgProfile.setImage(image);
            imgProfile.setVisible(true);
            lblPhoto.setVisible(false);

            showMessage(
                    "Photo selected. Click Save Changes.",
                    true
            );

        } catch (Exception e) {

            e.printStackTrace();
            showMessage("Unable to open photo.", false);
        }
    }

    // Save email and picture
    @FXML
    private void handleSave() {

        if (loggedInUserId == null) {
            showMessage("Student is not logged in.", false);
            return;
        }

        String email = txtEmail.getText().trim();

        if (!email.matches(
                "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {

            showMessage(
                    "Please enter a valid email address.",
                    false
            );
            return;
        }

        String newPhotoPath = currentPhotoPath;
        Path newlyCopiedFile = null;

        try {

            if (selectedPhotoFile != null) {

                // Save images in application data folder
                Path photoDirectory = Path.of(
                        System.getProperty("user.dir"),
                        "images",
                        "profiles"
                );

                Files.createDirectories(photoDirectory);

                String lowerName =
                        selectedPhotoFile.getName().toLowerCase();

                String extension = lowerName.endsWith(".png")
                        ? ".png" : ".jpg";

                String uniqueName =
                        loggedInUserId + "_profile_" +
                                UUID.randomUUID() + extension;

                Path destination =
                        photoDirectory.resolve(uniqueName);

                Files.copy(
                        selectedPhotoFile.toPath(),
                        destination,
                        StandardCopyOption.REPLACE_EXISTING
                );

                newlyCopiedFile = destination;
                newPhotoPath = Path.of(
                        "images",
                        "profiles",
                        uniqueName
                ).toString();
            }

            // Update database only after photo copy succeeds
            boolean updated =
                    profileDAO.updateStudentProfile(
                            loggedInUserId,
                            email,
                            newPhotoPath
                    );

            if (updated) {

                currentPhotoPath = newPhotoPath;
                selectedPhotoFile = null;

                displayPhoto(currentPhotoPath);

                showMessage(
                        "Profile updated successfully!",
                        true
                );

            } else {

                // Remove unsaved copy
                if (newlyCopiedFile != null) {
                    Files.deleteIfExists(newlyCopiedFile);
                }

                showMessage("Profile update failed.", false);
            }

        } catch (SQLException | IOException e) {

            e.printStackTrace();

            if (newlyCopiedFile != null) {
                try {
                    Files.deleteIfExists(newlyCopiedFile);
                } catch (IOException cleanupError) {
                    cleanupError.printStackTrace();
                }
            }

            showMessage(
                    "Unable to save profile: " + e.getMessage(),
                    false
            );
        }
    }

    private void showMessage(String message, boolean success) {

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
