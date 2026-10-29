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

    @FXML private HBox boxUsername;
    @FXML private HBox boxPassword;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;


    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void initialize() {

        txtUsername.clear();
        txtPassword.clear();
        hideError();


        bindFocusStyle(txtUsername, boxUsername);
        bindFocusStyle(txtPassword, boxPassword);

        txtUsername.setOnAction(e -> txtPassword.requestFocus());

        txtUsername.textProperty().addListener((o, a, b) -> hideError());
        txtPassword.textProperty().addListener((o, a, b) -> hideError());
    }

    private void bindFocusStyle(TextField field, HBox box) {
        field.focusedProperty().addListener((obs, was, isNow) -> {
            if (isNow) {
                if (!box.getStyleClass().contains(FOCUSED_STYLE)) box.getStyleClass().add(FOCUSED_STYLE);
            } else {
                box.getStyleClass().remove(FOCUSED_STYLE);
            }
        });
    }


    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }

        hideError();

        // check the user
        User user = userDAO.authenticateUser(username, password);

        if (user != null) {
            // considering database role and redirecting dashboard
            switch (user.getRole()) {
                case "Admin" -> navigateToDashboard("/fxml/admindashboard.fxml", "AMS - Admin Dashboard");
                case "Lecturer" -> navigateToLecturerDashboard(user);
                case "Undergraduate" -> navigateToDashboard("/fxml/student-layout.fxml", "AMS - Student Dashboard");
                case "Technical_Officer" -> navigateToDashboard("/fxml/technical-officer-dashboard.fxml", "AMS - TO Dashboard");
                default -> showError("Invalid user role detected: " + user.getRole());
            }
        } else {
            showError("Invalid username or password!");
        }
    }


    private void navigateToLecturerDashboard(User user) {

        try {
            // Find Lecturer ID using authenticated User ID
            String lecturerId =
                    userDAO.getLecturerIdByUserId(
                            user.getUserId()
                    );

            if (lecturerId == null || lecturerId.isBlank()) {
                showError(
                        "No lecturer record found for this account."
                );
                return;
            }

            // Load Lecturer Dashboard FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/lecturer-dashboard.fxml"
                    )
            );

            Parent root = loader.load();

            // Pass Lecturer ID to controller
            LecturerController lecturerController =
                    loader.getController();

            lecturerController.setLecturerId(lecturerId);

            // Open dashboard after ID is set
            Stage stage = (Stage)
                    txtUsername.getScene().getWindow();

            stage.setTitle("AMS - Lecturer Dashboard");
            stage.setScene(new Scene(root, 1200, 750));
            stage.centerOnScreen();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Unable to load lecturer account.");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load Lecturer Dashboard!");
        }
    }


    // to change the page
    private void navigateToDashboard(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root, 1100, 680));
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
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
        info("Forgot Password", "Please contact the system administrator to reset your password.");
    }

    @FXML
    private void handleAbout() {
        info("About", "University Management System\nLearn • Manage • Grow");
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void hideError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}