
package com.fot.ams.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class AdminController {

    @FXML
    private Label usersCount;

    @FXML
    private Label coursesCount;

    @FXML
    private Label departmentsCount;

    @FXML
    private Label medicalsCount;

    @FXML
    private TableView<Activity> activityTable;

    @FXML
    private TableColumn<Activity, String> activityColumn;

    @FXML
    private TableColumn<Activity, String> userColumn;

    @FXML
    private TableColumn<Activity, String> dateColumn;

    @FXML
    private TableColumn<Activity, String> statusColumn;

    private final ObservableList<Activity> activities =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        // These are sample values, not database records.
        usersCount.setText("120");
        coursesCount.setText("18");
        departmentsCount.setText("6");
        medicalsCount.setText("4");

        activityColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        cell.getValue().getActivity()));

        userColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        cell.getValue().getUser()));

        dateColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        cell.getValue().getDateTime()));

        statusColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(
                        cell.getValue().getStatus()));

        loadDemoActivities();
    }

    private void loadDemoActivities() {

        activities.setAll(
                new Activity(
                        "New user registered",
                        "Admin",
                        "2026-10-12 09:30",
                        "Completed"
                ),
                new Activity(
                        "Course details updated",
                        "Admin",
                        "2026-10-11 14:15",
                        "Completed"
                ),
                new Activity(
                        "Medical approval submitted",
                        "Student",
                        "2026-10-11 11:00",
                        "Pending"
                ),
                new Activity(
                        "Department information updated",
                        "Admin",
                        "2026-10-10 16:45",
                        "Completed"
                )
        );

        activityTable.setItems(activities);
    }

    @FXML
    private void handleNavigation(ActionEvent event) {

        Button clickedButton = (Button) event.getSource();
        String page = clickedButton.getText();

        if ("Logout".equals(page)) {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Logout");
            alert.setHeaderText("Logout is not configured yet");
            alert.setContentText(
                    "The logout feature will be implemented "
                            + "after connecting the login screen."
            );

            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Navigation");
        alert.setHeaderText(page);

        alert.setContentText(
                page.equals("Dashboard")
                        ? "You are already on the Admin Dashboard."
                        : "The " + page + " page has not been implemented yet."
        );

        alert.showAndWait();
    }

    @FXML
    private void refreshDashboard() {
        loadDemoActivities();
    }

    public static class Activity {

        private final String activity;
        private final String user;
        private final String dateTime;
        private final String status;

        public Activity(String activity, String user,
                        String dateTime, String status) {
            this.activity = activity;
            this.user = user;
            this.dateTime = dateTime;
            this.status = status;
        }

        public String getActivity() {
            return activity;
        }

        public String getUser() {
            return user;
        }

        public String getDateTime() {
            return dateTime;
        }

        public String getStatus() {
            return status;
        }
    }
}
