package com.fot.ams.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class StudentProfileController {

    @FXML private Label lblPhoto, lblMessage;
    @FXML private TextField txtFirstName, txtLastName, txtEmail, txtRegNo;

    @FXML
    private void initialize() {
        // TODO: UserDAO.findById(...)
        txtFirstName.setText("Sample");
        txtLastName.setText("User");
        txtEmail.setText("user@mail.com");
        txtRegNo.setText("TG/2023/001");
        lblPhoto.setText(txtFirstName.getText().substring(0, 1).toUpperCase());

        txtFirstName.textProperty().addListener((o, a, b) -> hideMessage());
        txtLastName.textProperty().addListener((o, a, b) -> hideMessage());
        txtEmail.textProperty().addListener((o, a, b) -> hideMessage());
    }

    @FXML
    private void handleSave() {
        if (txtFirstName.getText().isBlank() || txtLastName.getText().isBlank()) {
            showMessage("First name and last name are required.", false);
            return;
        }
        if (!txtEmail.getText().matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            showMessage("Please enter a valid email address.", false);
            return;
        }
        // TODO: UserDAO.update(...)
        lblPhoto.setText(txtFirstName.getText().trim().substring(0, 1).toUpperCase());
        showMessage("Profile updated successfully.", true);
    }

    @FXML
    private void handleChangePhoto() {
        info("Change Photo", "Photo upload will be added later.");
    }

    @FXML
    private void handleChangePassword() {
        PasswordField current = new PasswordField();
        PasswordField newPw = new PasswordField();
        PasswordField confirm = new PasswordField();
        current.setPromptText("Current password");
        newPw.setPromptText("New password");
        confirm.setPromptText("Confirm new password");
        for (PasswordField p : new PasswordField[]{current, newPw, confirm}) p.getStyleClass().add("form-control");

        VBox box = new VBox(10, current, newPw, confirm);
        box.setStyle("-fx-padding: 10;");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/student-profile.css").toExternalForm());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        if (current.getText().isEmpty() || newPw.getText().length() < 6) {
            showMessage("New password must be at least 6 characters.", false);
        } else if (!newPw.getText().equals(confirm.getText())) {
            showMessage("New password and confirm password do not match.", false);
        } else {
            // TODO: UserDAO.changePassword(...)
            showMessage("Password changed successfully.", true);
        }
    }

    private void showMessage(String msg, boolean ok) {
        lblMessage.getStyleClass().setAll(ok ? "success-label" : "error-label");
        lblMessage.setText(msg);
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private void hideMessage() {
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }

    private void info(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}