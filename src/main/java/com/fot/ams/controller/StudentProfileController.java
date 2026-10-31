
package com.fot.ams.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;

public class StudentProfileController {

    @FXML
    private Label lblPhoto, lblMessage;

    @FXML
    private TextField txtFirstName, txtLastName, txtEmail, txtRegNo;

    @FXML
    private ImageView imgProfile;

    private File selectedPhotoFile;

    @FXML
    private void initialize() {

        // TODO: Load real student data from DAO
        txtFirstName.setText("Sample");
        txtLastName.setText("User");
        txtEmail.setText("user@mail.com");
        txtRegNo.setText("TG/2023/001");

        // Only contact details and photo can be updated
        txtFirstName.setEditable(false);
        txtLastName.setEditable(false);
        txtRegNo.setEditable(false);

        lblPhoto.setText(
                txtFirstName.getText()
                        .substring(0, 1)
                        .toUpperCase()
        );

        txtEmail.textProperty().addListener(
                (obs, oldValue, newValue) -> hideMessage()
        );
    }

    @FXML
    private void handleSave() {

        String email = txtEmail.getText().trim();

        if (!email.matches(
                "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {

            showMessage(
                    "Please enter a valid email address.",
                    false
            );
            return;
        }

        // TODO: Save email and selectedPhotoFile using DAO.
        // Do not show success until database save succeeds.

        showMessage(
                "Profile changes are not saved to the database yet.",
                false
        );
    }

    @FXML
    private void handleChangePhoto() {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Select Profile Picture");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Image Files",
                        "*.png", "*.jpg", "*.jpeg"
                )
        );

        File file = fileChooser.showOpenDialog(
                imgProfile.getScene().getWindow()
        );

        if (file == null) {
            return;
        }

        try {

            // Limit selected image to 5 MB
            if (file.length() > 5 * 1024 * 1024) {
                showMessage(
                        "Image size must be less than 5 MB.",
                        false
                );
                return;
            }

            Image image = new Image(
                    file.toURI().toString(),
                    false
            );

            if (image.isError()) {
                showMessage(
                        "Unable to load selected image.",
                        false
                );
                return;
            }

            imgProfile.setImage(image);
            imgProfile.setVisible(true);

            lblPhoto.setVisible(false);

            selectedPhotoFile = file;

            showMessage(
                    "Profile picture selected. Save it after database integration.",
                    true
            );

        } catch (Exception e) {

            showMessage(
                    "Failed to load profile picture.",
                    false
            );

            e.printStackTrace();
        }
    }

    private void showMessage(String msg, boolean ok) {

        lblMessage.getStyleClass().setAll(
                ok ? "success-label" : "error-label"
        );

        lblMessage.setText(msg);
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private void hideMessage() {

        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }
}
