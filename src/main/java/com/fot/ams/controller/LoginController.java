package com.fot.ams.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class LoginController {

    private static final String FOCUSED_STYLE = "input-box-focused";

    @FXML private HBox boxUsername;
    @FXML private HBox boxPassword;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    private void initialize() {
        // Input box eka focus unama border eka highlight wenna
        bindFocusStyle(txtUsername, boxUsername);
        bindFocusStyle(txtPassword, boxPassword);

        // Username eke Enter ebuwama password ekata yanna
        txtUsername.setOnAction(e -> txtPassword.requestFocus());

        // Type karanakota error eka hide wenna
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
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }

        // TODO: DBConnection eken user validate karanna, role eka anuwa dashboard eka open karanna
        // admin         -> /fxml/admin-dashboard.fxml
        // lecturer      -> /fxml/lecturer-dashboard.fxml
        // undergraduate -> /fxml/student-dashboard.fxml
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