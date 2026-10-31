package com.fot.ams.controller;

import com.fot.ams.util.DBConnection;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class AdminAccountController {

    @FXML private Label lblPhoto;
    @FXML private Label lblMessage;
    @FXML private Label lblPasswordMessage;

    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtUserId;
    @FXML private TextField txtUsername;

    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;

    @FXML private TextField txtCurrentPasswordVisible;
    @FXML private TextField txtNewPasswordVisible;
    @FXML private TextField txtConfirmPasswordVisible;

    @FXML private CheckBox chkShowPasswords;

    @FXML private ImageView imgProfile;

    @FXML private Button btnSave;
    @FXML private Button btnChangePhoto;
    @FXML private Button btnChangePassword;

    private String userId;
    private String photoPath;
    private boolean accountLoaded;

    @FXML
    private void initialize() {
        txtUserId.setEditable(false);
        txtUsername.setEditable(false);

        configurePasswordVisibility(
                txtCurrentPassword,
                txtCurrentPasswordVisible
        );

        configurePasswordVisibility(
                txtNewPassword,
                txtNewPasswordVisible
        );

        configurePasswordVisibility(
                txtConfirmPassword,
                txtConfirmPasswordVisible
        );

        txtFirstName.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updateInitial();
                    hideMessage(lblMessage);
                }
        );

        txtLastName.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        hideMessage(lblMessage)
        );

        txtEmail.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        hideMessage(lblMessage)
        );

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

        txtConfirmPassword.setOnAction(
                event -> handleChangePassword()
        );

        txtConfirmPasswordVisible.setOnAction(
                event -> handleChangePassword()
        );
    }

    private void configurePasswordVisibility(
            PasswordField hiddenField,
            TextField visibleField) {

        visibleField.textProperty().bindBidirectional(
                hiddenField.textProperty()
        );

        hiddenField.visibleProperty().bind(
                chkShowPasswords.selectedProperty().not()
        );

        hiddenField.managedProperty().bind(
                hiddenField.visibleProperty()
        );

        visibleField.visibleProperty().bind(
                chkShowPasswords.selectedProperty()
        );

        visibleField.managedProperty().bind(
                visibleField.visibleProperty()
        );
    }

    public void setUserId(String userId) {
        this.userId = userId;

        clearPasswordFields();
        hideMessage(lblMessage);
        hideMessage(lblPasswordMessage);

        loadAccount();
    }

    private Connection openConnection() throws SQLException {
        Connection connection = DBConnection.getConnection();

        if (connection == null) {
            throw new SQLException(
                    "Database connection is unavailable."
            );
        }

        return connection;
    }

    private void setAccountLoaded(boolean loaded) {
        accountLoaded = loaded;

        btnSave.setDisable(!loaded);
        btnChangePhoto.setDisable(!loaded);
        btnChangePassword.setDisable(!loaded);
    }

    private void loadAccount() {
        setAccountLoaded(false);

        if (userId == null || userId.isBlank()) {
            showMessage(
                    lblMessage,
                    "Please log in again.",
                    false
            );
            return;
        }

        String sql = """
                SELECT user_id, user_name, first_name,
                       last_name, email, profile_image
                FROM `user`
                WHERE user_id = ? AND role = ?
                """;

        try (Connection connection = openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, userId);
            statement.setString(2, "Admin");
            statement.setQueryTimeout(15);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    showMessage(
                            lblMessage,
                            "The admin account could not be found.",
                            false
                    );
                    return;
                }

                txtUserId.setText(result.getString("user_id"));
                txtUsername.setText(result.getString("user_name"));
                txtFirstName.setText(result.getString("first_name"));
                txtLastName.setText(result.getString("last_name"));
                txtEmail.setText(result.getString("email"));

                photoPath = result.getString("profile_image");

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

    private void displaySavedPhoto() {
        imgProfile.setImage(null);
        imgProfile.setVisible(false);
        lblPhoto.setVisible(true);

        if (photoPath == null || photoPath.isBlank()) {
            return;
        }

        File file = new File(photoPath);

        if (!file.isFile() || !file.canRead()) {
            showMessage(
                    lblMessage,
                    "The saved profile image is unavailable. "
                            + "Choose another photo.",
                    false
            );
            return;
        }

        try {
            Image image = loadImage(file);

            imgProfile.setImage(image);
            imgProfile.setVisible(true);
            lblPhoto.setVisible(false);

        } catch (IllegalArgumentException e) {
            showMessage(
                    lblMessage,
                    "Unable to display the saved profile image.",
                    false
            );
        }
    }

    private Image loadImage(File file) {
        Image image = new Image(
                file.toURI().toString(),
                138,
                138,
                false,
                true,
                false
        );

        if (image.isError()
                || image.getWidth() <= 0
                || image.getHeight() <= 0) {

            throw new IllegalArgumentException(
                    "Unable to read the selected image."
            );
        }

        return image;
    }

    @FXML
    private void handleChangePhoto() {
        if (!accountLoaded) {
            return;
        }

        FileChooser chooser = new FileChooser();

        chooser.setTitle("Select Profile Picture");

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

        if (file == null) {
            return;
        }

        if (!file.isFile() || !file.canRead()) {
            showMessage(
                    lblMessage,
                    "The selected file cannot be read.",
                    false
            );
            return;
        }

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

        if (file.length() > 5L * 1024 * 1024) {
            showMessage(
                    lblMessage,
                    "Image size must not exceed 5 MB.",
                    false
            );
            return;
        }

        String path = file.getAbsolutePath();

        if (path.length() > 255) {
            showMessage(
                    lblMessage,
                    "The image path is too long. "
                            + "Choose an image from a shorter folder path.",
                    false
            );
            return;
        }

        try {
            Image image = loadImage(file);

            photoPath = path;

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

        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String email = txtEmail.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            showMessage(
                    lblMessage,
                    "Enter your first and last names.",
                    false
            );
            return;
        }

        if (firstName.length() > 50 || lastName.length() > 50) {
            showMessage(
                    lblMessage,
                    "First and last names must not exceed 50 characters.",
                    false
            );
            return;
        }

        if (email.length() > 100
                || !email.matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            showMessage(
                    lblMessage,
                    "Enter a valid email address.",
                    false
            );
            return;
        }

        String sql = """
                UPDATE `user`
                SET first_name = ?, last_name = ?,
                    email = ?, profile_image = ?
                WHERE user_id = ? AND role = ?
                """;

        btnSave.setDisable(true);

        try (Connection connection = openConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, email);
            statement.setString(4, photoPath);
            statement.setString(5, userId);
            statement.setString(6, "Admin");
            statement.setQueryTimeout(15);

            int updated = statement.executeUpdate();

            if (updated == 0 && !adminExists(connection)) {
                setAccountLoaded(false);

                showMessage(
                        lblMessage,
                        "The admin account is no longer available.",
                        false
                );
                return;
            }

            updateInitial();

            showMessage(
                    lblMessage,
                    "Account changes saved successfully.",
                    true
            );

        } catch (SQLException e) {
            showMessage(
                    lblMessage,
                    "Unable to save account: " + e.getMessage(),
                    false
            );

        } finally {
            btnSave.setDisable(!accountLoaded);
        }
    }

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

        String currentPassword = txtCurrentPassword.getText();
        String newPassword = txtNewPassword.getText();
        String confirmation = txtConfirmPassword.getText();

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

        int passwordLength = newPassword.codePointCount(
                0,
                newPassword.length()
        );

        if (passwordLength < 8) {
            showMessage(
                    lblPasswordMessage,
                    "The new password must contain at least 8 characters.",
                    false
            );

            focusPassword(
                    txtNewPassword,
                    txtNewPasswordVisible
            );
            return;
        }

        if (passwordLength > 255) {
            showMessage(
                    lblPasswordMessage,
                    "The new password must not exceed 255 characters.",
                    false
            );
            return;
        }

        if (!newPassword.equals(confirmation)) {
            showMessage(
                    lblPasswordMessage,
                    "The new passwords do not match.",
                    false
            );

            focusPassword(
                    txtConfirmPassword,
                    txtConfirmPasswordVisible
            );
            return;
        }

        if (newPassword.equals(currentPassword)) {
            showMessage(
                    lblPasswordMessage,
                    "Choose a password different from your current password.",
                    false
            );
            return;
        }

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
                clearPasswordFields();

                showMessage(
                        lblPasswordMessage,
                        "Password changed successfully.",
                        true
                );

            } else {
                txtCurrentPassword.clear();

                showMessage(
                        lblPasswordMessage,
                        "The current password is incorrect "
                                + "or the admin account is unavailable.",
                        false
                );

                focusPassword(
                        txtCurrentPassword,
                        txtCurrentPasswordVisible
                );
            }

        } catch (SQLException e) {
            showMessage(
                    lblPasswordMessage,
                    "A database error prevented the password change. "
                            + "Check the database connection and try again.",
                    false
            );

        } finally {
            btnChangePassword.setDisable(!accountLoaded);
        }
    }

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

    private void focusPassword(
            PasswordField hiddenField,
            TextField visibleField) {

        if (chkShowPasswords.isSelected()) {
            visibleField.requestFocus();
        } else {
            hiddenField.requestFocus();
        }
    }

    private void clearPasswordFields() {
        txtCurrentPassword.clear();
        txtNewPassword.clear();
        txtConfirmPassword.clear();

        chkShowPasswords.setSelected(false);
    }

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

    private void hideMessage(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }
}