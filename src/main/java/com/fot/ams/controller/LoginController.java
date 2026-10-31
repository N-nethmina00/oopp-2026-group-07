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

import java.net.URL;

public class LoginController {

    private static final String FOCUSED_STYLE =
            "input-box-focused";

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

        txtPassword.setOnAction(
                event -> handleLogin()
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
                (observable, oldValue, focused) -> {
                    if (focused) {
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

        if (username.isEmpty() || password.isBlank()) {
            showError("Please enter username and password.");
            return;
        }

        hideError();

        try {
            User user = userDAO.authenticateUser(
                    username,
                    password
            );

            if (user == null) {
                showError("Invalid username or password!");
                return;
            }

            if (user.getRole() == null) {
                showError("This account has no assigned role.");
                return;
            }

            switch (user.getRole()) {
                case "Admin" -> navigateToAdminDashboard(user);

                case "Lecturer" ->
                        navigateToLecturerDashboard(user);

                case "Undergraduate" ->
                        navigateToStudentDashboard(user);

                // TG2070 - Open dashboard with authenticated TO ID
                case "Technical_Officer" ->
                        navigateToTechnicalOfficerDashboard(user);

                default -> showError(
                        "Invalid user role detected: "
                                + user.getRole()
                );
            }

        } catch (Exception exception) {
            exception.printStackTrace();

            showError(
                    "Unable to log in: "
                            + exception.getMessage()
            );
        }
    }

    private FXMLLoader createLoader(String path) {
        URL resource = getClass().getResource(path);

        if (resource == null) {
            throw new IllegalStateException(
                    "Cannot find " + path
            );
        }

        return new FXMLLoader(resource);
    }

    private void openScene(
            Parent root,
            String title,
            double width,
            double height) {

        Stage stage = (Stage) txtUsername
                .getScene()
                .getWindow();

        txtPassword.clear();

        stage.setTitle(title);
        stage.setScene(new Scene(root, width, height));
        stage.centerOnScreen();
        stage.show();
    }

    private void navigateToAdminDashboard(User user) {
        try {
            FXMLLoader loader = createLoader(
                    "/fxml/adminDashboard.fxml"
            );

            Parent root = loader.load();

            AdminController controller = loader.getController();
            controller.setLoggedInUser(user);

            openScene(
                    root,
                    "AMS - Admin Dashboard",
                    1100,
                    680
            );

        } catch (Exception exception) {
            exception.printStackTrace();

            showError(
                    "Failed to load Admin Dashboard: "
                            + exception.getMessage()
            );
        }
    }

    private void navigateToLecturerDashboard(User user) {
        try {
            String lecturerId = userDAO.getLecturerIdByUserId(
                    user.getUserId()
            );

            if (lecturerId == null || lecturerId.isBlank()) {
                showError(
                        "No lecturer record found for this account."
                );
                return;
            }

            FXMLLoader loader = createLoader(
                    "/fxml/lecturer-dashboard.fxml"
            );

            Parent root = loader.load();

            LecturerController controller = loader.getController();
            controller.setLecturerId(lecturerId);

            openScene(
                    root,
                    "AMS - Lecturer Dashboard",
                    1200,
                    750
            );

        } catch (Exception exception) {
            exception.printStackTrace();

            showError(
                    "Failed to load Lecturer Dashboard: "
                            + exception.getMessage()
            );
        }
    }

    private void navigateToStudentDashboard(User user) {
        try {
            FXMLLoader loader = createLoader(
                    "/fxml/student-layout.fxml"
            );

            Parent root = loader.load();

            StudentLayoutController controller =
                    loader.getController();

            controller.loadStudentName(user.getUserId());

            openScene(
                    root,
                    "AMS - Student Dashboard",
                    1100,
                    680
            );

        } catch (Exception exception) {
            exception.printStackTrace();

            showError(
                    "Failed to load Student Dashboard: "
                            + exception.getMessage()
            );
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

            FXMLLoader loader = createLoader(
                    "/fxml/technical-officer-dashboard.fxml"
            );

            Parent root = loader.load();

            TechnicalOfficerController controller =
                    loader.getController();

            controller.setTechnicalOfficerId(technicalOfficerId);

            openScene(
                    root,
                    "AMS - Technical Officer Dashboard",
                    1200,
                    750
            );

        } catch (Exception exception) {
            exception.printStackTrace();

            showError(
                    "Failed to load Technical Officer Dashboard: "
                            + exception.getMessage()
            );
        }
    }
    // TG2070 - END

    private void navigateToDashboard(
            String path,
            String title) {

        try {
            Parent root = createLoader(path).load();

            openScene(root, title, 1100, 680);

        } catch (Exception exception) {
            exception.printStackTrace();

            showError(
                    "Failed to load dashboard interface: "
                            + exception.getMessage()
            );
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
                "Please contact the system administrator "
                        + "to reset your password."
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