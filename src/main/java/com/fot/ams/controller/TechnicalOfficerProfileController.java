package com.fot.ams.controller;

import com.fot.ams.dao.UserDAO;
import com.fot.ams.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.SQLException;

// TG2070 - Technical Officer profile controller
public class TechnicalOfficerProfileController {

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

    private final UserDAO userDAO = new UserDAO();

    private String technicalOfficerId;

    // TG2070 - Receive logged-in TO ID from dashboard
    public void setTechnicalOfficerId(String technicalOfficerId) {
        this.technicalOfficerId = technicalOfficerId;
        loadProfile();
    }

    // TG2070 - Load account and personal details
    private void loadProfile() {
        if (technicalOfficerId == null
                || technicalOfficerId.isBlank()) {
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

            profileUsernameField.setText(user.getUsername());
            profileDepartmentField.setText(
                    department == null ? "" : department
            );
            profileFirstNameField.setText(user.getFirstName());
            profileLastNameField.setText(user.getLastName());
            profileEmailField.setText(user.getEmail());

        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    // TG2070 - Validate and save personal details
    @FXML
    private void saveProfile() {
        if (technicalOfficerId == null
                || technicalOfficerId.isBlank()) {
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
                    "First name and last name must each be 50 characters or fewer."
            );
            return;
        }

        if (email.length() > 100
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            showMessage(
                    Alert.AlertType.WARNING,
                    "Check Details",
                    "Please enter a valid email address of 100 characters or fewer."
            );
            return;
        }

        try {
            boolean saved = userDAO.updateTechnicalOfficerProfile(
                    technicalOfficerId,
                    firstName,
                    lastName,
                    email
            );

            if (saved) {
                loadProfile();

                showMessage(
                        Alert.AlertType.INFORMATION,
                        "Profile",
                        "Profile details saved successfully."
                );
            } else {
                showMessage(
                        Alert.AlertType.WARNING,
                        "Profile",
                        "No changes were saved. Reload your profile and try again."
                );
            }

        } catch (SQLException exception) {
            showDatabaseError(exception);
        }
    }

    // TG2070 - Restore details currently saved in database
    @FXML
    private void cancelChanges() {
        loadProfile();
    }

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