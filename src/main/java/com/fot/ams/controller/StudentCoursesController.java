
package com.fot.ams.controller;

import com.fot.ams.dao.StudentCoursesDAO;
import com.fot.ams.model.Course;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.util.List;

public class StudentCoursesController {

    @FXML private TableView<Course> tblCourses;
    @FXML private TableColumn<Course, String> colCode;
    @FXML private TableColumn<Course, String> colName;
    @FXML private TableColumn<Course, String> colCredits;
    @FXML private TableColumn<Course, String> colLecturer;
    @FXML private TableColumn<Course, String> colType;

    private final StudentCoursesDAO dao =
            new StudentCoursesDAO();

    @FXML
    private void initialize() {

        // Course Code
        colCode.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCourseCode()
                )
        );

        // Course Name
        colName.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCourseName()
                )
        );

        // Credits
        colCredits.setCellValueFactory(data ->
                new SimpleStringProperty(
                        String.valueOf(
                                data.getValue().getCredit()
                        )
                )
        );

        // Lecturer ID
        colLecturer.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getLecturerId()
                )
        );

        // Course Type
        colType.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getType()
                )
        );

        // Type badge styling
        colType.setCellFactory(column -> badgeCell());

        // Table settings
        tblCourses.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tblCourses.setPlaceholder(
                new Label("No enrolled courses found")
        );
    }

    // Load logged-in student's enrolled courses
    public void loadCourses(String userId) {

        try {

            List<Course> courses =
                    dao.getEnrolledCourses(userId);

            tblCourses.setItems(
                    FXCollections.observableArrayList(courses)
            );

        } catch (SQLException e) {

            e.printStackTrace();

            tblCourses.getItems().clear();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to load enrolled courses."
            ).showAndWait();
        }
    }

    // Display Theory / Practical as coloured badges
    private TableCell<Course, String> badgeCell() {

        return new TableCell<>() {

            @Override
            protected void updateItem(String type, boolean empty) {

                super.updateItem(type, empty);

                if (empty || type == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label badge = new Label(type);

                badge.getStyleClass().addAll(
                        "badge",
                        "badge-" + type.toLowerCase().replace(' ', '-')
                );

                setGraphic(badge);
                setText(null);
            }
        };
    }
}
