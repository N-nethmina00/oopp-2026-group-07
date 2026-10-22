package com.fot.ams.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

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

    @FXML
    private void initialize() {
        bindFocusStyle(txtUsername, boxUsername);
        bindFocusStyle(txtPassword, boxPassword);

        txtUsername.setOnAction(e -> txtPassword.requestFocus());

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

        // TODO: Validate credentials using the database,
        // then open the dashboard for the user's role.
        hideError();
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