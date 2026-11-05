package com.fot.ams.controller;

import com.fot.ams.dao.TechnicalOfficerPersonalDAO;
import com.fot.ams.dao.UserDAO;
import com.fot.ams.model.User;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

// TG2070 - Manage Technical Officer profile details and photo.
public class TechnicalOfficerProfileController {

    // Connect the FXML controls to this controller.
    @FXML
    private TextField profileUsernameField;

    @FXML
    private TextField profileDepartmentField;

    @FXML
    private TextField profileFirstNameField;

    @FXML
    private TextField profileLastNameField;

    @FXML
    private TextField profileEmailField;

    @FXML
    private ImageView profileImageView;

    @FXML
    private Label profileAvatarLabel;

    // TG2070 - NIC and birthday controls.
    @FXML
    private TextField profileNicField;

    @FXML
    private DatePicker profileBirthdayPicker;

    @FXML
    private Label birthdayCountdownLabel;

    private final UserDAO userDAO = new UserDAO();

    private final TechnicalOfficerPersonalDAO personalDAO =
            new TechnicalOfficerPersonalDAO();

    private String technicalOfficerId;
    private String savedPhotoPath;
    private File selectedPhoto;

    // Receive the logged-in officer ID and load the profile.
    public void setTechnicalOfficerId(String technicalOfficerId) {
        this.technicalOfficerId = technicalOfficerId;
        loadProfile();
    }

    // Check whether the officer ID is available.
    private boolean hasTechnicalOfficerId() {
        return technicalOfficerId != null
                && !technicalOfficerId.isBlank();
    }

    // Load saved details from the database and fill the form.
    private void loadProfile() {
        if (!hasTechnicalOfficerId()) {
            showMessage(
                    Alert.AlertType.ERROR,
                    "Profile Error",
                    "Technical Officer ID is missing."
            );
            return;
        }

        try {
            User user = userDAO.getTechnicalOfficerProfile(
                    technicalOfficerId
            );

            if (user == null) {
                showMessage(
                        Alert.AlertType.ERROR,
                        "Profile Error",
                        "Technical Officer account was not found."
                );
                return;
            }

            String department =
                    userDAO.getTechnicalOfficerDepartment(
                            technicalOfficerId
                    );

            String photoPath =
                    userDAO.getTechnicalOfficerPhoto(
                            technicalOfficerId
                    );

            profileUsernameField.setText(user.getUsername());

            profileDepartmentField.setText(
                    department == null ? "" : department
            );

            profileFirstNameField.setText(user.getFirstName());
            profileLastNameField.setText(user.getLastName());
            profileEmailField.setText(user.getEmail());

            savedPhotoPath = photoPath;
            selectedPhoto = null;

            loadSavedPhoto();
            loadPersonalDetails();

        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    // Show the saved photo or keep the placeholder.
    private void loadSavedPhoto() {
        showPhoto(null);

        if (savedPhotoPath == null || savedPhotoPath.isBlank()) {
            return;
        }

        try {
            Image image = readPhoto(new File(savedPhotoPath));
            showPhoto(image);

        } catch (IOException | IllegalArgumentException exception) {
            System.out.println(
                    "Profile photo could not be loaded: "
                            + exception.getMessage()
            );
        }
    }

    // Select and preview a photo before saving.
    @FXML
    private void changePhoto() {
        if (!hasTechnicalOfficerId()) {
            showMessage(
                    Alert.AlertType.ERROR,
                    "Profile Error",
                    "Please log in again before selecting a photo."
            );
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Image files (*.png, *.jpg, *.jpeg)",
                        "*.png", "*.jpg", "*.jpeg"
                )
        );

        File file = fileChooser.showOpenDialog(
                profileUsernameField.getScene().getWindow()
        );

        if (file == null) {
            return;
        }

        try {
            Image image = readPhoto(file);

            selectedPhoto = file;
            showPhoto(image);

        } catch (IOException | IllegalArgumentException exception) {
            showMessage(
                    Alert.AlertType.WARNING,
                    "Invalid Photo",
                    exception.getMessage()
            );
        }
    }

    // Check the photo type, file size and image data.
    private Image readPhoto(File file) throws IOException {
        String name = file.getName().toLowerCase(Locale.ROOT);

        if (!name.endsWith(".png")
                && !name.endsWith(".jpg")
                && !name.endsWith(".jpeg")) {
            throw new IOException("Please select a PNG or JPG image.");
        }

        if (!file.isFile()) {
            throw new IOException("The selected photo was not found.");
        }

        if (Files.size(file.toPath()) > 5L * 1024 * 1024) {
            throw new IOException(
                    "Please select a photo smaller than 5 MB."
            );
        }

        // Read the image and close the stream automatically.
        try (InputStream input = Files.newInputStream(file.toPath())) {
            Image image = new Image(input, 200, 200, true, true);

            if (image.isError()
                    || image.getWidth() <= 0
                    || image.getHeight() <= 0) {
                throw new IOException(
                        "The selected file could not be opened as an image."
                );
            }

            return image;
        }
    }

    // Display the photo inside a circle.
    private void showPhoto(Image image) {
        profileImageView.setImage(image);

        boolean hasPhoto = image != null;

        if (hasPhoto) {
            double width = image.getWidth();
            double height = image.getHeight();

            double size = Math.min(width, height);

            double x = (width - size) / 2;
            double y = (height - size) / 2;

            // Take a square from the centre of the image.
            profileImageView.setViewport(
                    new Rectangle2D(x, y, size, size)
            );

            profileImageView.setFitWidth(80);
            profileImageView.setFitHeight(80);
            profileImageView.setPreserveRatio(true);

            profileImageView.setClip(
                    new Circle(40, 40, 40)
            );

        } else {
            profileImageView.setViewport(null);
            profileImageView.setClip(null);
        }

        // Show the photo when available; otherwise show the placeholder.
        profileImageView.setVisible(hasPhoto);
        profileImageView.setManaged(hasPhoto);

        profileAvatarLabel.setVisible(!hasPhoto);
        profileAvatarLabel.setManaged(!hasPhoto);
    }

    // Check the entered details and save the profile.
    @FXML
    private void saveProfile() {
        if (!hasTechnicalOfficerId()) {
            showMessage(
                    Alert.AlertType.ERROR,
                    "Profile Error",
                    "Please log in again before saving."
            );
            return;
        }

        String firstName = profileFirstNameField.getText().trim();
        String lastName = profileLastNameField.getText().trim();
        String email = profileEmailField.getText().trim();

        if (firstName.isEmpty()
                || lastName.isEmpty()
                || email.isEmpty()) {
            showMessage(
                    Alert.AlertType.WARNING,
                    "Check Details",
                    "Please enter first name, last name and email."
            );
            return;
        }

        if (firstName.length() > 50 || lastName.length() > 50) {
            showMessage(
                    Alert.AlertType.WARNING,
                    "Check Details",
                    "First name and last name must each be "
                            + "50 characters or fewer."
            );
            return;
        }

        if (email.length() > 100
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            showMessage(
                    Alert.AlertType.WARNING,
                    "Check Details",
                    "Please enter a valid email address "
                            + "of 100 characters or fewer."
            );
            return;
        }

        Path copiedPhoto = null;
        boolean saved = false;

        try {
            String photoPath = savedPhotoPath;

            // Copy the new photo to the uploads folder.
            if (selectedPhoto != null) {
                readPhoto(selectedPhoto);

                Path folder = Path.of("uploads", "profile-images");
                Files.createDirectories(folder);

                String originalName = selectedPhoto.getName();

                String extension = originalName.substring(
                        originalName.lastIndexOf('.')
                ).toLowerCase(Locale.ROOT);

                // Create a unique file name.
                String fileName = UUID.randomUUID() + extension;

                copiedPhoto = folder.resolve(fileName);

                Files.copy(selectedPhoto.toPath(), copiedPhoto);

                photoPath = copiedPhoto.toString();
            }

            // Save profile details and the photo path through UserDAO.
            saved = userDAO.updateTechnicalOfficerProfile(
                    technicalOfficerId,
                    firstName,
                    lastName,
                    email,
                    photoPath
            );

            if (saved) {
                savedPhotoPath = photoPath;
                selectedPhoto = null;

            } else {
                showMessage(
                        Alert.AlertType.WARNING,
                        "Profile",
                        "No changes were saved. Reload your profile "
                                + "and try again."
                );
            }

        } catch (SQLException exception) {
            showDatabaseError(exception);

        } catch (IOException | IllegalArgumentException exception) {
            showMessage(
                    Alert.AlertType.ERROR,
                    "Photo Error",
                    "Unable to save the photo: "
                            + exception.getMessage()
            );

        } finally {
            // Remove the new photo copy if saving failed.
            if (!saved && copiedPhoto != null) {
                try {
                    Files.deleteIfExists(copiedPhoto);

                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }

        if (saved) {
            showMessage(
                    Alert.AlertType.INFORMATION,
                    "Profile",
                    "Profile details saved successfully."
            );
        }
    }

    // TG2070 - Load the saved NIC and birthday.
    private void loadPersonalDetails() throws SQLException {
        TechnicalOfficerPersonalDAO.PersonalDetails details =
                personalDAO.getPersonalDetails(technicalOfficerId);

        profileNicField.clear();
        profileBirthdayPicker.setValue(null);
        birthdayCountdownLabel.setText("Birthday is not saved.");

        if (details != null) {
            profileNicField.setText(
                    details.nic() == null ? "" : details.nic()
            );

            profileBirthdayPicker.setValue(details.birthday());
            showBirthdayCountdown(details.birthday());
        }
    }

    // TG2070 - Validate and save NIC and birthday separately.
    @FXML
    private void savePersonalDetails() {
        if (!hasTechnicalOfficerId()) {
            showMessage(
                    Alert.AlertType.ERROR,
                    "Profile Error",
                    "Please log in again."
            );
            return;
        }

        String nic = profileNicField.getText()
                .trim().toUpperCase(Locale.ROOT);

        LocalDate birthday = profileBirthdayPicker.getValue();

        // Check the old or new NIC format.
        if (!nic.matches("([0-9]{9}[VX]|[0-9]{12})")) {
            showMessage(
                    Alert.AlertType.WARNING,
                    "Invalid NIC",
                    "Enter 9 digits followed by V or X, or 12 digits."
            );
            return;
        }

        if (birthday == null
                || !birthday.isBefore(LocalDate.now())) {
            showMessage(
                    Alert.AlertType.WARNING,
                    "Invalid Birthday",
                    "Select a date of birth before today."
            );
            return;
        }

        try {
            boolean saved = personalDAO.savePersonalDetails(
                    technicalOfficerId,
                    nic,
                    birthday
            );

            if (saved) {
                profileNicField.setText(nic);
                showBirthdayCountdown(birthday);

                showMessage(
                        Alert.AlertType.INFORMATION,
                        "Personal Details",
                        "NIC and birthday saved successfully."
                );

            } else {
                showMessage(
                        Alert.AlertType.WARNING,
                        "Personal Details",
                        "Officer account was not found. "
                                + "Please reload your profile."
                );
            }

        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    // TG2070 - Count days until the next birthday.
    private void showBirthdayCountdown(LocalDate birthday) {
        if (birthday == null) {
            birthdayCountdownLabel.setText("Birthday is not saved.");
            return;
        }

        LocalDate today = LocalDate.now();
        MonthDay birthMonthDay = MonthDay.from(birthday);

        // Use February 28 for February 29 in a non-leap year.
        LocalDate nextBirthday =
                birthMonthDay.atYear(today.getYear());

        // If this year's birthday has passed, use next year.
        if (nextBirthday.isBefore(today)) {
            nextBirthday = birthMonthDay.atYear(
                    today.getYear() + 1
            );
        }

        long days = ChronoUnit.DAYS.between(today, nextBirthday);

        if (days == 0) {
            birthdayCountdownLabel.setText("Happy Birthday!");

        } else {
            birthdayCountdownLabel.setText(
                    "Your birthday is in " + days
                            + (days == 1 ? " day." : " days.")
            );
        }
    }

    // Reload saved details to cancel unsaved changes.
    @FXML
    private void cancelChanges() {
        loadProfile();
    }

    // Handle database errors, including duplicate email addresses.
    private void showDatabaseError(SQLException exception) {
        exception.printStackTrace();

        if (exception.getErrorCode() == 1062) {
            showMessage(
                    Alert.AlertType.ERROR,
                    "Profile Error",
                    "That email address is already used by another account."
            );

        } else {
            showMessage(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    "Unable to load or save profile details."
            );
        }
    }

    // Display an alert message.
    private void showMessage(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}