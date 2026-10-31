package com.fot.ams.controller;

import com.fot.ams.dao.UserDAO;
import com.fot.ams.model.User;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;

import java.util.Optional;

public class AdminUserManagementController {

    @FXML private TextField userIdField;
    @FXML private TextField roleSpecificIdField;
    @FXML private Label roleSpecificIdLabel;
    @FXML private TextField usernameField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> userIdColumn;
    @FXML private TableColumn<User, String> roleSpecificIdColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;

    private final UserDAO userDAO = new UserDAO();

    private final ObservableList<User> userList =
            FXCollections.observableArrayList();

    private static final double ROW_HEIGHT = 38.0;
    private static final double HEADER_SPACE = 48.0;

    @FXML
    public void initialize() {
        configureColumns();

        usersTable.setItems(userList);
        configureTableHeight();

        roleComboBox.setItems(
                FXCollections.observableArrayList(
                        "Admin",
                        "Lecturer",
                        "Technical_Officer",
                        "Undergraduate"
                )
        );

        roleComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) ->
                        updateRoleIdLabel(newValue)
        );

        usersTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldUser, selectedUser) -> {
                    if (selectedUser != null) {
                        showUserDetails(selectedUser);
                    }
                });

        updateRoleIdLabel(null);
        loadUsers();
    }

    private void configureColumns() {
        usersTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        userIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("userId")
        );

        roleSpecificIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("roleSpecificId")
        );

        usernameColumn.setCellValueFactory(
                new PropertyValueFactory<>("username")
        );

        fullNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("fullName")
        );

        emailColumn.setCellValueFactory(
                new PropertyValueFactory<>("email")
        );

        roleColumn.setCellValueFactory(
                new PropertyValueFactory<>("role")
        );

        usersTable.setPlaceholder(
                new Label("No registered users available.")
        );
    }

    private void configureTableHeight() {
        usersTable.setFixedCellSize(ROW_HEIGHT);

        usersTable.setMinHeight(Region.USE_PREF_SIZE);
        usersTable.setMaxHeight(Region.USE_PREF_SIZE);

        // Keep all rows visible; the outer ScrollPane scrolls the page.
        usersTable.prefHeightProperty().bind(
                Bindings.createDoubleBinding(
                        () -> Math.max(1, userList.size())
                                * usersTable.getFixedCellSize()
                                + HEADER_SPACE,
                        userList,
                        usersTable.fixedCellSizeProperty()
                )
        );
    }

    private void updateRoleIdLabel(String role) {
        if (role == null) {
            roleSpecificIdLabel.setText("Role-specific ID");
            roleSpecificIdField.setPromptText(
                    "Enter role-specific ID"
            );
            return;
        }

        switch (role) {
            case "Undergraduate" -> {
                roleSpecificIdLabel.setText("Registration No");
                roleSpecificIdField.setPromptText(
                        "Enter registration number"
                );
            }

            case "Technical_Officer" -> {
                roleSpecificIdLabel.setText(
                        "Technical Officer ID"
                );
                roleSpecificIdField.setPromptText(
                        "Enter technical officer ID"
                );
            }

            case "Lecturer" -> {
                roleSpecificIdLabel.setText("Lecturer ID");
                roleSpecificIdField.setPromptText(
                        "Enter lecturer ID"
                );
            }

            case "Admin" -> {
                roleSpecificIdLabel.setText("Admin ID");
                roleSpecificIdField.setPromptText(
                        "Enter admin ID"
                );
            }

            default -> {
                roleSpecificIdLabel.setText("Role-specific ID");
                roleSpecificIdField.setPromptText(
                        "Enter role-specific ID"
                );
            }
        }
    }

    private void showUserDetails(User user) {
        userIdField.setText(user.getUserId());
        roleSpecificIdField.setText(user.getRoleSpecificId());
        usernameField.setText(user.getUsername());
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        emailField.setText(user.getEmail());

        roleComboBox.setValue(user.getRole());

        // Do not display stored passwords.
        passwordField.clear();

        // Keep the ID readable and selectable, but prevent editing.
        userIdField.setEditable(false);
    }

    @FXML
    private void addUser() {
        if (usersTable.getSelectionModel().getSelectedItem() != null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Clear Selection",
                    "Click Clear before adding a new user."
            );
            return;
        }

        if (!validateFields(true)) {
            return;
        }

        try {
            boolean added = userDAO.addUser(readForm());

            if (added) {
                loadUsers();
                clearFields();

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "User added successfully."
                );
            } else {
                showAlert(
                        Alert.AlertType.WARNING,
                        "Not Added",
                        "User was not added."
                );
            }

        } catch (RuntimeException e) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void updateUser() {
        User selected = usersTable
                .getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Select User",
                    "Select a user from the table first."
            );
            return;
        }

        if (!validateFields(false)) {
            return;
        }

        if (!selected.getUserId().equals(
                userIdField.getText().trim())) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Invalid User ID",
                    "User ID cannot be changed during update."
            );
            return;
        }

        try {
            User user = readForm();

            boolean updatePassword =
                    !passwordField.getText().isBlank();

            if (userDAO.updateUser(user, updatePassword)) {
                loadUsers();
                clearFields();

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "User updated successfully."
                );
            } else {
                showAlert(
                        Alert.AlertType.WARNING,
                        "Not Updated",
                        "No matching user was found."
                );
            }

        } catch (RuntimeException e) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void deleteUser() {
        User selected = usersTable
                .getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Select User",
                    "Select a user from the table first."
            );
            return;
        }

        String userId = selected.getUserId();

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete User");

        confirmation.setContentText(
                "Delete user " + userId
                        + " and their role record?"
        );

        Optional<ButtonType> result =
                confirmation.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            if (userDAO.deleteUser(userId)) {
                loadUsers();
                clearFields();

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "User deleted successfully."
                );
            } else {
                showAlert(
                        Alert.AlertType.WARNING,
                        "Not Deleted",
                        "No matching user was found."
                );
            }

        } catch (RuntimeException e) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void clearFields() {
        usersTable.getSelectionModel().clearSelection();

        userIdField.clear();
        roleSpecificIdField.clear();
        usernameField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        passwordField.clear();

        roleComboBox.getSelectionModel().clearSelection();
        roleComboBox.setValue(null);

        userIdField.setDisable(false);
        userIdField.setEditable(true);

        updateRoleIdLabel(null);
    }

    private User readForm() {
        return new User(
                userIdField.getText().trim(),
                roleSpecificIdField.getText().trim(),
                usernameField.getText().trim(),
                passwordField.getText(),
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                roleComboBox.getValue()
        );
    }

    private boolean validateFields(boolean requirePassword) {
        if (userIdField.getText().isBlank()
                || roleSpecificIdField.getText().isBlank()
                || usernameField.getText().isBlank()
                || firstNameField.getText().isBlank()
                || lastNameField.getText().isBlank()
                || emailField.getText().isBlank()
                || roleComboBox.getValue() == null
                || (requirePassword
                && passwordField.getText().isBlank())) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    requirePassword
                            ? "Fill in all fields, including password."
                            : "Fill in all required fields."
            );

            return false;
        }

        if (!roleComboBox.getItems().contains(
                roleComboBox.getValue())) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Invalid Role",
                    "Select a valid role."
            );

            return false;
        }

        String email = emailField.getText().trim();

        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Invalid Email",
                    "Enter a valid email address."
            );

            emailField.requestFocus();
            return false;
        }

        return true;
    }

    private void loadUsers() {
        try {
            // Reuse the same list so the height binding stays active.
            userList.setAll(
                    userDAO.getAllUsers()
            );

        } catch (RuntimeException e) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    "Could not load users.\n" + e.getMessage()
            );
        }
    }

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);

        alert.setContentText(
                message == null || message.isBlank()
                        ? "The operation could not be completed."
                        : message
        );

        alert.showAndWait();
    }
}