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
    private String loggedInUserId;

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

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/" + page + ".fxml"
                    )
            );

            Parent root = loader.load();

            // Load dashboard data for logged-in student
            if ("student-dashboard".equals(page)
                    && loggedInUserId != null) {

                StudentDashboardController controller =
                        loader.getController();

                controller.loadDashboard(loggedInUserId);
            }
            // Load logged-in student's enrolled courses
            if ("student-courses".equals(page)
                    && loggedInUserId != null) {

                StudentCoursesController controller =
                        loader.getController();

                controller.loadCourses(loggedInUserId);
            }
            // Load profile for logged-in student
            if ("student-profile".equals(page)
                    && loggedInUserId != null) {

                StudentProfileController controller =
                        loader.getController();

                controller.loadProfile(loggedInUserId);
            }
            // Load semester results of logged-in student
            if ("student-results".equals(page)
                    && loggedInUserId != null) {

                StudentResultsController controller =
                        loader.getController();

                controller.loadResults(loggedInUserId);
            }
            if ("student-medical".equals(page)
                    && loggedInUserId != null) {

                StudentMedicalController controller =
                        loader.getController();

                controller.loadMedical(loggedInUserId);
            }
            if ("student-marks".equals(page)
                    && loggedInUserId != null) {

                StudentMarksController controller =
                        loader.getController();

                controller.loadMarks(loggedInUserId);
            }
            if ("student-materials".equals(page)
                    && loggedInUserId != null) {

                StudentMaterialsController controller =
                        loader.getController();

                controller.loadMaterials(loggedInUserId);
            }
            if ("student-attendance".equals(page)
                    && loggedInUserId != null) {

                StudentAttendanceController controller =
                        loader.getController();

                controller.loadAttendance(loggedInUserId);
            }

            contentArea.getChildren().setAll(root);

            // Update active sidebar button
            navBox.getChildren().forEach(
                    n -> n.getStyleClass().remove(ACTIVE)
            );

            btn.getStyleClass().add(ACTIVE);

        } catch (IOException | NullPointerException ex) {

            ex.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Cannot load page: " + page + ".fxml"
            ).showAndWait();
        }
    }



    public void loadStudentName(String userId) {

        // Save logged-in user ID
        this.loggedInUserId = userId;

        StudentDashboardDAO dao = new StudentDashboardDAO();

        try {

            Student student = dao.getStudentByUserId(userId);

            if (student != null
                    && !student.getFullName().isBlank()) {

                setUser(
                        student.getFullName(),
                        student.getRole()
                );

            } else {

                setUser("Student", "Undergraduate");
            }

        } catch (SQLException e) {

            e.printStackTrace();
            setUser("Student", "Undergraduate");
        }

        // Reload dashboard using actual logged-in user ID
        open((Button) navBox.getChildren().get(0));
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
