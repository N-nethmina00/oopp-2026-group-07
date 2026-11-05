package com.fot.ams.controller;

import com.fot.ams.util.DBConnection;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;

public class AdminAccountController {

    // Labels used for the profile photo and screen messages.
    @FXML private Label lblPhoto;
    @FXML private Label lblMessage;
    @FXML private Label lblPasswordMessage;

    // Fields containing the admin's account details.
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtUserId;
    @FXML private TextField txtUsername;

    // Normal text fields show the passwords while they are being typed.
    @FXML private TextField txtCurrentPassword;
    @FXML private TextField txtNewPassword;
    @FXML private TextField txtConfirmPassword;

    // Displays the profile photo.
    @FXML private ImageView imgProfile;

    // Buttons for saving account changes and changing the photo or password.
    @FXML private Button btnSave;
    @FXML private Button btnChangePhoto;
    @FXML private Button btnChangePassword;

    // Stores the current admin's ID and profile photo information.
    private String userId;
    private String photoPath;
    private File selectedPhotoFile;

    // Tracks whether the admin account has loaded successfully.
    private boolean accountLoaded;

    // Set up the account screen after JavaFX loads its FXML file.
    @FXML
    private void initialize() {
        // These account values are displayed but cannot be edited here.
        txtUserId.setEditable(false);
        txtUsername.setEditable(false);

        // Update the profile-photo initial when the first name changes.
        txtFirstName.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updateInitial();
                    hideMessage(lblMessage);
                }
        );

        // Hide the account message when the user edits these fields.
        txtLastName.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        hideMessage(lblMessage)
        );

        txtEmail.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        hideMessage(lblMessage)
        );

        // Hide the password message when a password field changes.
        txtCurrentPassword.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        hideMessage(lblPasswordMessage)
        );

        txtNewPassword.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        hideMessage(lblPasswordMessage)
        );

        txtConfirmPassword.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        hideMessage(lblPasswordMessage)
        );

        // Pressing Enter in the confirmation field changes the password.
        txtConfirmPassword.setOnAction(
                event -> handleChangePassword()
        );
    }

    // Receives the admin ID from the dashboard controller.
    public void setUserId(String userId) {
        this.userId = userId;
        selectedPhotoFile = null;

        // Clear old password entries and messages before loading the account.
        clearPasswordFields();
        hideMessage(lblMessage);
        hideMessage(lblPasswordMessage);

        loadAccount();
    }

    // Gets a database connection or reports that one is unavailable.
    private Connection openConnection() throws SQLException {
        Connection connection = DBConnection.getConnection();

        if (connection == null) {
            throw new SQLException(
                    "Database connection is unavailable."
            );
        }

        return connection;
    }

    // Enables account actions only after the account has loaded.
    private void setAccountLoaded(boolean loaded) {
        accountLoaded = loaded;

        btnSave.setDisable(!loaded);
        btnChangePhoto.setDisable(!loaded);
        btnChangePassword.setDisable(!loaded);
    }

    // Loads the admin's account details from the database.
    private void loadAccount() {
        setAccountLoaded(false);

        // A missing ID usually means the user needs to log in again.
        if (userId == null || userId.isBlank()) {
            showMessage(
                    lblMessage,
                    "Please log in again.",
                    false
            );
            return;
        }

        // Select only the account matching this ID and the Admin role.
        String sql = """
                SELECT user_id, user_name, first_name,
                       last_name, email, profile_image
                FROM `user`
                WHERE user_id = ? AND role = ?
                """;

        try (Connection connection = openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            // Fill the query placeholders with the account ID and role.
            statement.setString(1, userId);
            statement.setString(2, "Admin");
            statement.setQueryTimeout(15);

            try (ResultSet result = statement.executeQuery()) {
                // Show a message if the admin account was not found.
                if (!result.next()) {
                    showMessage(
                            lblMessage,
                            "The admin account could not be found.",
                            false
                    );
                    return;
                }

                // Display the database values in the account fields.
                txtUserId.setText(result.getString("user_id"));
                txtUsername.setText(result.getString("user_name"));
                txtFirstName.setText(result.getString("first_name"));
                txtLastName.setText(result.getString("last_name"));
                txtEmail.setText(result.getString("email"));

                // Save and display the profile photo.
                photoPath = result.getString("profile_image");
                selectedPhotoFile = null;

                updateInitial();
                displaySavedPhoto();
                setAccountLoaded(true);
            }

        } catch (SQLException e) {
            showMessage(
                    lblMessage,
                    "Unable to load account: " + e.getMessage(),
                    false
            );
        }
    }

    // Displays the saved photo, if a valid photo file exists.
    private void displaySavedPhoto() {
        imgProfile.setImage(null);
        imgProfile.setVisible(false);
        lblPhoto.setVisible(true);

        // Keep the initial-letter placeholder if there is no saved path.
        if (photoPath == null || photoPath.isBlank()) {
            return;
        }

        try {
            Path savedPath = Path.of(photoPath);

            // Resolve a relative path from the application folder.
            if (!savedPath.isAbsolute()) {
                savedPath = Path.of(
                        System.getProperty("user.dir")
                ).resolve(savedPath);
            }

            File file = savedPath.normalize().toFile();

            // Leave the placeholder visible if the file cannot be read.
            if (!file.isFile() || !file.canRead()) {
                return;
            }

            Image image = loadImage(file);

            imgProfile.setImage(image);
            imgProfile.setVisible(true);
            lblPhoto.setVisible(false);

        } catch (Exception e) {
            showMessage(
                    lblMessage,
                    "Unable to display the saved profile image.",
                    false
            );
        }
    }

    // Loads an image and checks that JavaFX could read it.
    private Image loadImage(File file) {
        Image image = new Image(
                file.toURI().toString(),
                138,
                138,
                false,
                true,
                false
        );

        // Reject unreadable or invalid image files.
        if (image.isError()
                || image.getWidth() <= 0
                || image.getHeight() <= 0) {

            throw new IllegalArgumentException(
                    "Unable to read the selected image."
            );
        }

        return image;
    }

    // Opens a file chooser so the admin can pick a profile photo.
    @FXML
    private void handleChangePhoto() {
        if (!accountLoaded) {
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Profile Picture");

        // Only show PNG and JPG files in the file chooser.
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png",
                        "*.jpg",
                        "*.jpeg",
                        "*.PNG",
                        "*.JPG",
                        "*.JPEG"
                )
        );

        File file = chooser.showOpenDialog(
                imgProfile.getScene().getWindow()
        );

        // The user closed the chooser without selecting a file.
        if (file == null) {
            return;
        }

        // Make sure the selected file can be read.
        if (!file.isFile() || !file.canRead()) {
            showMessage(
                    lblMessage,
                    "The selected file cannot be read.",
                    false
            );
            return;
        }

        // Limit the image size to 5 MB.
        if (file.length() == 0
                || file.length() > 5L * 1024 * 1024) {
            showMessage(
                    lblMessage,
                    "Image must be between 1 byte and 5 MB.",
                    false
            );
            return;
        }

        // Accept only PNG and JPG file extensions.
        String name = file.getName().toLowerCase(Locale.ROOT);

        if (!name.endsWith(".png")
                && !name.endsWith(".jpg")
                && !name.endsWith(".jpeg")) {
            showMessage(
                    lblMessage,
                    "Choose a PNG or JPG image.",
                    false
            );
            return;
        }

        try {
            // Preview the image and remember the selected file.
            Image image = loadImage(file);

            selectedPhotoFile = file;

            imgProfile.setImage(image);
            imgProfile.setVisible(true);
            lblPhoto.setVisible(false);

            showMessage(
                    lblMessage,
                    "Photo selected. Click Save Changes to save it.",
                    true
            );

        } catch (IllegalArgumentException e) {
            showMessage(
                    lblMessage,
                    e.getMessage(),
                    false
            );
        }
    }

    // Validates and saves the account details.
    @FXML
    private void handleSave() {
        if (!accountLoaded || userId == null) {
            showMessage(
                    lblMessage,
                    "Please log in again.",
                    false
            );
            return;
        }

        // Read and clean the editable account values.
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String email = txtEmail.getText().trim();

        // Require both names.
        if (firstName.isEmpty() || lastName.isEmpty()) {
            showMessage(
                    lblMessage,
                    "Enter your first and last names.",
                    false
            );
            return;
        }

        // Limit the length of each name.
        if (firstName.length() > 50 || lastName.length() > 50) {
            showMessage(
                    lblMessage,
                    "First and last names must not exceed 50 characters.",
                    false
            );
            return;
        }

        // Check the email's basic format and maximum length.
        if (email.length() > 100
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            showMessage(
                    lblMessage,
                    "Enter a valid email address.",
                    false
            );
            return;
        }

        // Keep the old path unless a new photo is selected.
        String newPhotoPath = photoPath;
        Path newlyCopiedFile = null;

        btnSave.setDisable(true);

        try {
            // Copy a newly selected photo into the app's profile folder.
            if (selectedPhotoFile != null) {
                if (!selectedPhotoFile.isFile()
                        || !selectedPhotoFile.canRead()
                        || selectedPhotoFile.length() == 0
                        || selectedPhotoFile.length()
                        > 5L * 1024 * 1024) {
                    showMessage(
                            lblMessage,
                            "The selected image is invalid.",
                            false
                    );
                    return;
                }

                // Keep PNG files as PNG; save other accepted files as JPG.
                String originalName =
                        selectedPhotoFile.getName()
                                .toLowerCase(Locale.ROOT);

                String extension = originalName.endsWith(".png")
                        ? ".png"
                        : ".jpg";

                // Create the folder if it does not already exist.
                Path photoDirectory = Path.of(
                        System.getProperty("user.dir"),
                        "images",
                        "profiles"
                );

                Files.createDirectories(photoDirectory);

                // Use a unique filename so existing photos are not overwritten.
                String uniqueName = userId
                        + "_profile_"
                        + UUID.randomUUID()
                        + extension;

                newlyCopiedFile =
                        photoDirectory.resolve(uniqueName);

                Files.copy(
                        selectedPhotoFile.toPath(),
                        newlyCopiedFile
                );

                // Store a relative path in the database.
                newPhotoPath = Path.of(
                        "images",
                        "profiles",
                        uniqueName
                ).toString();
            }

            // Update the editable account values in the database.
            String sql = """
                    UPDATE `user`
                    SET first_name = ?, last_name = ?,
                        email = ?, profile_image = ?
                    WHERE user_id = ? AND role = ?
                    """;

            try (Connection connection = openConnection();
                 PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, firstName);
                statement.setString(2, lastName);
                statement.setString(3, email);
                statement.setString(4, newPhotoPath);
                statement.setString(5, userId);
                statement.setString(6, "Admin");
                statement.setQueryTimeout(15);

                int updated = statement.executeUpdate();

                // Check whether the admin account still exists.
                if (updated == 0 && !adminExists(connection)) {
                    deleteFileQuietly(newlyCopiedFile);
                    setAccountLoaded(false);

                    showMessage(
                            lblMessage,
                            "The admin account is no longer available.",
                            false
                    );
                    return;
                }
            }

            // Update the saved photo path and refresh the display.
            photoPath = newPhotoPath;
            selectedPhotoFile = null;

            updateInitial();
            displaySavedPhoto();

            showMessage(
                    lblMessage,
                    "Account changes saved successfully.",
                    true
            );

        } catch (SQLException | IOException e) {
            // Remove the copied image if saving the account failed.
            deleteFileQuietly(newlyCopiedFile);

            showMessage(
                    lblMessage,
                    "Unable to save account: " + e.getMessage(),
                    false
            );

        } finally {
            // Re-enable Save if the account is still available.
            btnSave.setDisable(!accountLoaded);
        }
    }

    // Validates and changes the admin's password.
    @FXML
    private void handleChangePassword() {
        if (!accountLoaded || userId == null) {
            showMessage(
                    lblPasswordMessage,
                    "Please log in again.",
                    false
            );
            return;
        }

        // Read the three passwords from the normal text fields.
        String currentPassword = txtCurrentPassword.getText();
        String newPassword = txtNewPassword.getText();
        String confirmation = txtConfirmPassword.getText();

        // Require all three values.
        if (currentPassword.isEmpty()
                || newPassword.isEmpty()
                || confirmation.isEmpty()) {
            showMessage(
                    lblPasswordMessage,
                    "Complete all password fields.",
                    false
            );
            return;
        }

        // Count characters correctly, including Unicode characters.
        int passwordLength = newPassword.codePointCount(
                0,
                newPassword.length()
        );

        // Require at least 8 characters.
        if (passwordLength < 8) {
            showMessage(
                    lblPasswordMessage,
                    "The new password must contain at least 8 characters.",
                    false
            );

            txtNewPassword.requestFocus();
            return;
        }

        // Keep the password within the database field's limit.
        if (passwordLength > 255) {
            showMessage(
                    lblPasswordMessage,
                    "The new password must not exceed 255 characters.",
                    false
            );
            return;
        }

        // Make sure the new password was entered twice correctly.
        if (!newPassword.equals(confirmation)) {
            showMessage(
                    lblPasswordMessage,
                    "The new passwords do not match.",
                    false
            );

            txtConfirmPassword.requestFocus();
            return;
        }

        // Require the new password to differ from the current one.
        if (newPassword.equals(currentPassword)) {
            showMessage(
                    lblPasswordMessage,
                    "Choose a password different from your current password.",
                    false
            );
            return;
        }

        // Change the password only if the current password matches.
        String sql = """
                UPDATE `user`
                SET `password` = ?
                WHERE user_id = ?
                  AND role = ?
                  AND BINARY `password` = BINARY ?
                """;

        btnChangePassword.setDisable(true);

        try (Connection connection = openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, newPassword);
            statement.setString(2, userId);
            statement.setString(3, "Admin");
            statement.setString(4, currentPassword);
            statement.setQueryTimeout(15);

            int updated = statement.executeUpdate();

            if (updated == 1) {
                // Clear the password fields after a successful change.
                clearPasswordFields();

                showMessage(
                        lblPasswordMessage,
                        "Password changed successfully.",
                        true
                );
            } else {
                // The current password may be incorrect.
                txtCurrentPassword.clear();

                showMessage(
                        lblPasswordMessage,
                        "The current password is incorrect "
                                + "or the admin account is unavailable.",
                        false
                );

                txtCurrentPassword.requestFocus();
            }

        } catch (SQLException e) {
            showMessage(
                    lblPasswordMessage,
                    "A database error prevented the password change. "
                            + "Check the database connection and try again.",
                    false
            );

        } finally {
            // Enable the button again if the account is available.
            btnChangePassword.setDisable(!accountLoaded);
        }
    }

    // Checks whether this admin account still exists in the database.
    private boolean adminExists(Connection connection)
            throws SQLException {

        String sql = """
                SELECT user_id
                FROM `user`
                WHERE user_id = ? AND role = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, userId);
            statement.setString(2, "Admin");
            statement.setQueryTimeout(15);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    // Tries to delete a file without interrupting the main operation.
    private void deleteFileQuietly(Path file) {
        if (file == null) {
            return;
        }

        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Clears all password fields.
    private void clearPasswordFields() {
        txtCurrentPassword.clear();
        txtNewPassword.clear();
        txtConfirmPassword.clear();
    }

    // Shows the first letter of the admin's name as the photo placeholder.
    private void updateInitial() {
        String firstName = txtFirstName.getText();

        lblPhoto.setText(
                firstName == null || firstName.isBlank()
                        ? "A"
                        : firstName.trim()
                        .substring(0, 1)
                        .toUpperCase(Locale.ROOT)
        );
    }

    // Displays a success or error message.
    private void showMessage(
            Label label,
            String message,
            boolean success) {

        label.setText(message);
        label.setStyle(
                "-fx-text-fill: "
                        + (success ? "#15803D;" : "#B42318;")
                        + "-fx-font-size: 12px;"
        );
        label.setManaged(true);
        label.setVisible(true);
    }

    // Hides a message from the screen.
    private void hideMessage(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}