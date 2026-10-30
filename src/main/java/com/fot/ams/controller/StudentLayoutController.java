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
import com.fot.ams.dao.StudentDashboardDAO;
import java.sql.SQLException;
import com.fot.ams.dao.StudentDashboardDAO;
import com.fot.ams.model.Student;


public class StudentLayoutController {

    private static final String ACTIVE = "nav-button-active";

    @FXML private VBox navBox;
    @FXML private StackPane contentArea;
    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblAvatar;

    @FXML
    private void initialize() {

        setUser("Student", "Undergraduate");

        open((Button) navBox.getChildren().get(0));
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



    public void loadStudentName(String userId) {

        StudentDashboardDAO dao = new StudentDashboardDAO();

        try {

            // Get Student object from DAO
            Student student = dao.getStudentByUserId(userId);

            if (student != null && !student.getFullName().isBlank()) {

                // Get name and role from Student Model
                String name = student.getFullName();
                String role = student.getRole();

                // Display in Student Layout
                setUser(name, role);

            } else {

                setUser("Student", "Undergraduate");
            }

        } catch (SQLException e) {

            e.printStackTrace();
            setUser("Student", "Undergraduate");
        }
    }



    @FXML
    private void handleLogout(ActionEvent event) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to logout?", ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        if (a.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginRoot = loader.load();


            javafx.stage.Stage stage = (javafx.stage.Stage) ((Node) event.getSource()).getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(loginRoot);
            stage.setScene(scene);
            stage.setTitle("Login - AMS");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Cannot load login page!").showAndWait();
        }
    }
}
