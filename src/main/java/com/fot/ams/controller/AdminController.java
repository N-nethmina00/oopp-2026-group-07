
package com.fot.ams.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class AdminController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private VBox sidebar;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button usersButton;

    @FXML
    private Button coursesButton;

    @FXML
    private Button noticesButton;

    @FXML
    private Button timetableButton;

    @FXML
    private Button logoutButton;

    @FXML
    private void initialize() {
        setActiveButton(dashboardButton);
    }

    @FXML
    private void handleSidebarClick(javafx.event.ActionEvent event) {
        if (!(event.getSource() instanceof Button clickedButton)) {
            return;
        }

        if (clickedButton == logoutButton) {
            handleLogout();
            return;
        }

        setActiveButton(clickedButton);

        if (clickedButton == dashboardButton) {
            return;
        }

        String sectionName = clickedButton.getText();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Admin Dashboard");
        alert.setHeaderText(sectionName);
        alert.setContentText(
                sectionName + " section selected."
        );
        alert.showAndWait();
    }

    private void setActiveButton(Button activeButton) {
        if (sidebar == null) {
            return;
        }

        for (Node node : sidebar.getChildren()) {
            if (node instanceof Button button && button != logoutButton) {
                button.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 8;" +
                                "-fx-font-weight: normal;" +
                                "-fx-alignment: CENTER_LEFT;" +
                                "-fx-padding: 0 12 0 12;"
                );
            }
        }

        if (activeButton != null) {
            activeButton.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-text-fill: #0078b9;" +
                            "-fx-background-radius: 8;" +
                            "-fx-font-weight: bold;" +
                            "-fx-alignment: CENTER_LEFT;" +
                            "-fx-padding: 0 12 0 12;"
            );
        }
    }

    private void handleLogout() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Log Out");
        confirmation.setHeaderText("Confirm Logout");
        confirmation.setContentText(
                "Are you sure you want to log out?"
        );

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            Alert message = new Alert(Alert.AlertType.INFORMATION);
            message.setTitle("Log Out");
            message.setHeaderText(null);
            message.setContentText(
                    "Logout confirmed. Connect your login screen here."
            );
            message.showAndWait();
        }
    }
}
