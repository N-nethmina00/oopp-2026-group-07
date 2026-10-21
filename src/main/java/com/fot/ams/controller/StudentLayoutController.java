package com.fot.ams.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class StudentLayoutController {

    private static final String ACTIVE = "nav-button-active";

    @FXML private VBox navBox;
    @FXML private StackPane contentArea;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblAvatar;

    @FXML
    private void initialize() {
        // TODO: logged-in user eka session / UserDAO eken ganna
        setUser("Sample User", "Undergraduate");
        open((Button) navBox.getChildren().get(0));   // first page = Dashboard
    }

    /** Call this from LoginController after login success. */
    public void setUser(String name, String role) {
        lblUserName.setText("Welcome, " + name);
        lblUserRole.setText(role);
        lblAvatar.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase());
    }

    @FXML
    private void navigate(ActionEvent e) {
        open((Button) e.getSource());
    }

    @FXML
    private void openProfile(MouseEvent e) {
        for (Node n : navBox.getChildren()) {
            if ("student-profile".equals(n.getUserData())) { open((Button) n); return; }
        }
    }

    private void open(Button btn) {
        String page = (String) btn.getUserData();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/" + page + ".fxml"));
            contentArea.getChildren().setAll(root);
            navBox.getChildren().forEach(n -> n.getStyleClass().remove(ACTIVE));
            btn.getStyleClass().add(ACTIVE);
        } catch (IOException | NullPointerException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Cannot load page: " + page + ".fxml").showAndWait();
        }
    }

    @FXML
    private void handleLogout() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to logout?", ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        if (a.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        try {
            // NOTE: login fxml path eka oyaage project ekata change karanna
            Parent login = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            contentArea.getScene().setRoot(login);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
