package com.fot.ams.controller;

import com.fot.ams.dao.UserDAO;
import com.fot.ams.model.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class LoginController {

    private static final String FOCUSED_STYLE = "input-box-focused";

    @FXML
    private HBox boxUsername;

    @FXML
    private HBox boxPassword;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblError;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void initialize() {
        txtUsername.clear();
        txtPassword.clear();
        hideError();

        bindFocusStyle(txtUsername, boxUsername);
        bindFocusStyle(txtPassword, boxPassword);

        txtUsername.setOnAction(
                event -> txtPassword.requestFocus()
        );

        txtUsername.textProperty().addListener(
                (observable, oldValue, newValue) -> hideError()
        );

        txtPassword.textProperty().addListener(
                (observable, oldValue, newValue) -> hideError()
        );
    }

    private void bindFocusStyle(TextField field, HBox box) {
        field.focusedProperty().addListener(
                (observable, wasFocused, isFocused) -> {
                    if (isFocused) {
                        if (!box.getStyleClass().contains(FOCUSED_STYLE)) {
                            box.getStyleClass().add(FOCUSED_STYLE);
                        }
                    } else {
                        box.getStyleClass().remove(FOCUSED_STYLE);
                    }
                }
        );
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }

        hideError();

        User user = userDAO.authenticateUser(username, password);

        if (user == null) {
            showError("Invalid username or password!");
            return;
        }

        switch (user.getRole()) {
            case "Admin" -> navigateToDashboard(
                    "/fxml/admindashboard.fxml",
                    "AMS - Admin Dashboard"
            );

            case "Lecturer" -> navigateToLecturerDashboard(user);

            case "Undergraduate" -> navigateToDashboard(
                    "/fxml/student-layout.fxml",
                    "AMS - Student Dashboard"
            );

            // TG2070 - Open TO dashboard with authenticated TO ID
            case "Technical_Officer" ->
                    navigateToTechnicalOfficerDashboard(user);

            default -> showError(
                    "Invalid user role detected: " + user.getRole()
            );
        }
    }

    // Existing develop lecturer navigation
    private void navigateToLecturerDashboard(User user) {
        try {
            String lecturerId = userDAO.getLecturerIdByUserId(
                    user.getUserId()
            );

            if (lecturerId == null || lecturerId.isBlank()) {
                showError("No lecturer record found for this account.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/lecturer-dashboard.fxml"
                    )
            );

            Parent root = loader.load();

            LecturerController lecturerController =
                    loader.getController();

            lecturerController.setLecturerId(lecturerId);

            Stage stage =
                    (Stage) txtUsername.getScene().getWindow();

            stage.setTitle("AMS - Lecturer Dashboard");
            stage.setScene(new Scene(root, 1200, 750));
            stage.centerOnScreen();

        } catch (SQLException exception) {
            exception.printStackTrace();
            showError("Unable to load lecturer account.");

        } catch (Exception exception) {
            exception.printStackTrace();
            showError("Failed to load Lecturer Dashboard!");
        }
    }

    // TG2070 - START: Pass authenticated TO ID to dashboard
    private void navigateToTechnicalOfficerDashboard(User user) {
        try {
            String technicalOfficerId =
                    userDAO.getTechnicalOfficerIdByUserId(
                            user.getUserId()
                    );

            if (technicalOfficerId == null
                    || technicalOfficerId.isBlank()) {

                showError(
                        "No Technical Officer record found for this account."
                );
                return;
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/technical-officer-dashboard.fxml"
                    )
            );

            Parent root = loader.load();

            TechnicalOfficerController controller =
                    loader.getController();

            controller.setTechnicalOfficerId(technicalOfficerId);

            Stage stage =
                    (Stage) txtUsername.getScene().getWindow();

            stage.setTitle("AMS - Technical Officer Dashboard");
            stage.setScene(new Scene(root, 1200, 750));
            stage.centerOnScreen();

        } catch (SQLException exception) {
            exception.printStackTrace();
            showError("Unable to load Technical Officer account.");

        } catch (Exception exception) {
            exception.printStackTrace();
            showError("Failed to load Technical Officer Dashboard.");
        }
    }
    // TG2070 - END

    private void navigateToDashboard(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource(fxmlPath)
            );

            Stage stage =
                    (Stage) txtUsername.getScene().getWindow();

            stage.setTitle(title);
            stage.setScene(new Scene(root, 1100, 680));
            stage.centerOnScreen();

        } catch (Exception exception) {
            exception.printStackTrace();
            showError("Failed to load dashboard interface!");
        }
    }

    @FXML
    private void handleClear() {
        txtUsername.clear();
        txtPassword.clear();
        hideError();
        txtUsername.requestFocus();
    }

    @FXML
    private void handleForgotPassword() {
        info(
                "Forgot Password",
                "Please contact the system administrator to reset your password."
        );
    }

    @FXML
    private void handleAbout() {
        info(
                "About",
                "University Management System\nLearn • Manage • Grow"
        );
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void hideError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    private void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}