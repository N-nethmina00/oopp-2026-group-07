package com.fot.ams.controller;

import com.fot.ams.dao.UserDAO;
import com.fot.ams.model.User;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

public class AdminUserManagementController {

    @FXML private ComboBox<String> roleComboBox;

    @FXML private TextField userIdField;
    @FXML private TextField roleSpecificIdField;
    @FXML private Label roleSpecificIdLabel;
    @FXML private Label modeLabel;
    @FXML private Label idHintLabel;
    @FXML private Label messageLabel;

    @FXML private VBox detailsBox;
    @FXML private VBox departmentBox;
    @FXML private VBox batchBox;

    @FXML private ComboBox<UserDAO.Option> departmentComboBox;
    @FXML private ComboBox<UserDAO.Option> batchComboBox;

    @FXML private TextField usernameField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;

    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private CheckBox showPasswordCheckBox;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    @FXML private Button refreshButton;

    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private ProgressIndicator progressIndicator;

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

    private final FilteredList<User> filteredUsers =
            new FilteredList<>(userList, user -> true);

    private boolean busy;
    private boolean ready;
    private boolean idsReady;
    private boolean resetting;

    private long idRequestNumber;
    private User editingUser;

    private record PageData(
            List<User> users,
            List<UserDAO.Option> departments,
            List<UserDAO.Option> batches
    ) {
    }

    @FXML
    private void initialize() {
        roleComboBox.getItems().setAll(
                "Admin",
                "Lecturer",
                "Technical_Officer",
                "Undergraduate"
        );

        roleComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(String value) {
                return roleName(value);
            }

            @Override
            public String fromString(String value) {
                return "Technical Officer".equals(value)
                        ? "Technical_Officer"
                        : value;
            }
        });

        userIdField.setEditable(false);
        roleSpecificIdField.setEditable(false);

        visiblePasswordField.textProperty().bindBidirectional(
                passwordField.textProperty()
        );

        passwordField.visibleProperty().bind(
                showPasswordCheckBox.selectedProperty().not()
        );
        passwordField.managedProperty().bind(
                passwordField.visibleProperty()
        );

        visiblePasswordField.visibleProperty().bind(
                showPasswordCheckBox.selectedProperty()
        );
        visiblePasswordField.managedProperty().bind(
                visiblePasswordField.visibleProperty()
        );

        configureColumn(userIdColumn, User::getUserId);
        configureColumn(roleSpecificIdColumn, User::getRoleSpecificId);
        configureColumn(usernameColumn, User::getUsername);
        configureColumn(fullNameColumn, User::getFullName);
        configureColumn(emailColumn, User::getEmail);

        configureColumn(
                roleColumn,
                user -> roleName(user.getRole())
        );

        SortedList<User> sortedUsers = new SortedList<>(filteredUsers);
        sortedUsers.comparatorProperty().bind(
                usersTable.comparatorProperty()
        );

        usersTable.setItems(sortedUsers);

        usersTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldUser, selectedUser) -> {
                    if (resetting || busy) {
                        return;
                    }

                    if (selectedUser == null) {
                        clearFields();
                    } else {
                        showUser(selectedUser);
                    }
                });

        roleComboBox.valueProperty().addListener(
                (observable, oldRole, newRole) -> {
                    updateRoleLabel(newRole);
                    updateActions();

                    if (!resetting && editingUser == null) {
                        loadNextIds();
                    }
                });

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> filterUsers()
        );

        clearFields();
        loadPage("Select a role to create a new user.");
    }

    private void configureColumn(
            TableColumn<User, String> column,
            Function<User, String> getter
    ) {
        column.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(
                        safe(getter.apply(cell.getValue()))
                )
        );
    }

    private void filterUsers() {
        String query = safe(searchField.getText())
                .trim()
                .toLowerCase(Locale.ROOT);

        filteredUsers.setPredicate(user -> {
            String text = String.join(" ",
                    safe(user.getUserId()),
                    safe(user.getRoleSpecificId()),
                    safe(user.getUsername()),
                    safe(user.getFullName()),
                    safe(user.getEmail()),
                    roleName(user.getRole())
            ).toLowerCase(Locale.ROOT);

            return text.contains(query);
        });

        countLabel.setText(
                filteredUsers.size() + " of " + userList.size() + " users"
        );

        usersTable.setPlaceholder(new Label(
                userList.isEmpty()
                        ? "No users available."
                        : "No users match your search."
        ));
    }

    private void loadNextIds() {
        final long request = ++idRequestNumber;
        final String role = roleComboBox.getValue();

        idsReady = false;
        userIdField.clear();
        roleSpecificIdField.clear();

        updateActions();

        if (role == null || !ready || busy) {
            idHintLabel.setText(
                    "Select a role. Both IDs will be generated automatically."
            );
            return;
        }

        idHintLabel.setText("Loading the next available IDs...");

        runDatabase(
                () -> userDAO.getNextIds(role),
                ids -> {
                    // Ignore an old response if the role or selection changed.
                    if (request != idRequestNumber
                            || editingUser != null
                            || busy
                            || !role.equals(roleComboBox.getValue())) {
                        return;
                    }

                    userIdField.setText(ids.userId());
                    roleSpecificIdField.setText(ids.roleSpecificId());

                    idsReady = true;

                    idHintLabel.setText(
                            "IDs are automatic. The latest available IDs "
                                    + "will be confirmed when saving."
                    );

                    updateActions();
                },
                error -> {
                    if (request != idRequestNumber
                            || editingUser != null
                            || busy) {
                        return;
                    }

                    idsReady = false;

                    idHintLabel.setText(
                            "Could not generate IDs. Select the role again "
                                    + "or click Refresh."
                    );

                    messageLabel.setText(errorMessage(error));
                    updateActions();
                }
        );
    }

    private void updateRoleLabel(String role) {
        roleSpecificIdLabel.setText(
                switch (role == null ? "" : role) {
                    case "Admin" -> "Admin ID";
                    case "Lecturer" -> "Lecturer ID";
                    case "Technical_Officer" -> "Technical Officer ID";
                    case "Undergraduate" -> "Registration Number";
                    default -> "Role-specific ID";
                }
        );
    }

    private void showUser(User user) {
        ++idRequestNumber;

        resetting = true;
        editingUser = user;
        idsReady = false;

        try {
            roleComboBox.setValue(user.getRole());

            userIdField.setText(user.getUserId());
            roleSpecificIdField.setText(
                    safe(user.getRoleSpecificId())
            );

            usernameField.setText(user.getUsername());
            firstNameField.setText(user.getFirstName());
            lastNameField.setText(user.getLastName());
            emailField.setText(user.getEmail());

            passwordField.clear();
            showPasswordCheckBox.setSelected(false);

        } finally {
            resetting = false;
        }

        modeLabel.setText("Edit User: " + user.getUserId());

        idHintLabel.setText(
                "Existing IDs and role are fixed. "
                        + "Leave the password empty to keep the current password."
        );

        messageLabel.setText(
                "Update the details, or click New / Clear to add another user."
        );

        updateRoleLabel(user.getRole());
        updateActions();
    }

    @FXML
    private void clearFields() {
        if (busy) {
            return;
        }

        ++idRequestNumber;

        resetting = true;

        try {
            editingUser = null;
            idsReady = false;

            usersTable.getSelectionModel().clearSelection();

            roleComboBox.setValue(null);
            userIdField.clear();
            roleSpecificIdField.clear();

            usernameField.clear();
            firstNameField.clear();
            lastNameField.clear();
            emailField.clear();

            passwordField.clear();
            showPasswordCheckBox.setSelected(false);

            departmentComboBox.setValue(null);
            batchComboBox.setValue(null);

            if (departmentComboBox.getItems().size() == 1) {
                departmentComboBox.getSelectionModel().selectFirst();
            }

        } finally {
            resetting = false;
        }

        modeLabel.setText("Create User");

        idHintLabel.setText(
                "Select a role. Both IDs will be generated automatically."
        );

        messageLabel.setText("");

        updateRoleLabel(null);
        updateActions();
    }

    @FXML
    private void addUser() {
        if (busy || editingUser != null || !idsReady) {
            return;
        }

        try {
            User user = readAndValidate(true);

            Integer departmentId =
                    departmentComboBox.getValue() == null
                            ? null
                            : departmentComboBox.getValue().id();

            Integer batchId =
                    batchComboBox.getValue() == null
                            ? null
                            : batchComboBox.getValue().id();

            if (!"Admin".equals(user.getRole()) && departmentId == null) {
                departmentComboBox.requestFocus();

                throw new IllegalArgumentException(
                        "Select a department."
                );
            }

            if ("Undergraduate".equals(user.getRole()) && batchId == null) {
                batchComboBox.requestFocus();

                throw new IllegalArgumentException(
                        "Select a batch."
                );
            }

            ++idRequestNumber;
            setBusy(true);
            messageLabel.setText("Creating user...");

            runDatabase(
                    () -> userDAO.addUser(user, departmentId, batchId),
                    saved -> {
                        user.setPassword(null);
                        passwordField.clear();

                        setBusy(false);

                        loadPage(
                                "User created: " + user.getUserId()
                                        + " | Role ID: " + user.getRoleSpecificId()
                        );
                    },
                    error -> {
                        user.setPassword(null);
                        setBusy(false);

                        messageLabel.setText(errorMessage(error));
                        loadNextIds();
                    }
            );

        } catch (IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void updateUser() {
        if (busy || editingUser == null) {
            return;
        }

        try {
            User user = readAndValidate(false);

            boolean changePassword =
                    !passwordField.getText().isEmpty();

            setBusy(true);
            messageLabel.setText("Updating user...");

            runDatabase(
                    () -> userDAO.updateUser(user, changePassword),
                    updated -> {
                        user.setPassword(null);
                        passwordField.clear();

                        setBusy(false);

                        loadPage(
                                updated
                                        ? "User updated successfully."
                                        : "User no longer exists. List refreshed."
                        );
                    },
                    error -> {
                        user.setPassword(null);
                        setBusy(false);
                        messageLabel.setText(errorMessage(error));
                    }
            );

        } catch (IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void deleteUser() {
        if (busy || editingUser == null) {
            return;
        }

        User selected = editingUser;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);

        Window window = owner();
        if (window != null) {
            confirmation.initOwner(window);
        }

        confirmation.setTitle("Delete User");
        confirmation.setHeaderText(
                "Delete " + selected.getFullName() + "?"
        );

        confirmation.setContentText(
                "User ID: " + selected.getUserId()
                        + "\nRole ID: " + selected.getRoleSpecificId()
                        + "\n\nThis permanently deletes the account and role record."
                        + "\nLinked records may also be deleted by your database's "
                        + "cascade rules, including student marks and attendance."
                        + "\n\nThis action cannot be undone."
        );

        ButtonType deleteType = new ButtonType(
                "Delete User",
                ButtonBar.ButtonData.OK_DONE
        );

        confirmation.getButtonTypes().setAll(
                deleteType,
                ButtonType.CANCEL
        );

        Button deleteConfirm = (Button)
                confirmation.getDialogPane().lookupButton(deleteType);

        deleteConfirm.setDefaultButton(false);

        Button cancel = (Button)
                confirmation.getDialogPane().lookupButton(ButtonType.CANCEL);

        cancel.setDefaultButton(true);

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL)
                != deleteType) {
            return;
        }

        setBusy(true);
        messageLabel.setText("Deleting user...");

        runDatabase(
                () -> userDAO.deleteUser(selected.getUserId()),
                deleted -> {
                    setBusy(false);

                    loadPage(
                            deleted
                                    ? "User deleted successfully."
                                    : "User was already removed."
                    );
                },
                error -> {
                    setBusy(false);
                    messageLabel.setText(errorMessage(error));
                }
        );
    }

    @FXML
    private void refreshUsers() {
        loadPage("Users refreshed. Select a role to create a new user.");
    }

    private void loadPage(String successMessage) {
        if (busy) {
            return;
        }

        ++idRequestNumber;
        setBusy(true);

        messageLabel.setText("Loading users and available options...");

        runDatabase(
                () -> new PageData(
                        userDAO.getAllUsers(),
                        userDAO.getDepartments(),
                        userDAO.getBatches()
                ),
                data -> {
                    resetting = true;

                    try {
                        usersTable.getSelectionModel().clearSelection();

                        userList.setAll(data.users());

                        departmentComboBox.getItems().setAll(
                                data.departments()
                        );

                        batchComboBox.getItems().setAll(
                                data.batches()
                        );

                    } finally {
                        resetting = false;
                    }

                    ready = true;
                    setBusy(false);

                    clearFields();
                    filterUsers();

                    messageLabel.setText(successMessage);
                },
                error -> {
                    setBusy(false);
                    messageLabel.setText(errorMessage(error));
                }
        );
    }

    private User readAndValidate(boolean creating) {
        String role = roleComboBox.getValue();

        if (role == null) {
            roleComboBox.requestFocus();
            throw new IllegalArgumentException("Select a role first.");
        }

        if (creating && !idsReady) {
            throw new IllegalArgumentException(
                    "Wait until both IDs have been generated."
            );
        }

        String username = required(usernameField, "Username", 50);
        String firstName = required(firstNameField, "First name", 50);
        String lastName = required(lastNameField, "Last name", 50);
        String email = required(emailField, "Email", 100);

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            emailField.requestFocus();

            throw new IllegalArgumentException(
                    "Enter a valid email address."
            );
        }

        String password = passwordField.getText();

        if (creating || !password.isEmpty()) {
            if (password.isBlank()
                    || password.length() < 8
                    || password.length() > 255) {

                if (showPasswordCheckBox.isSelected()) {
                    visiblePasswordField.requestFocus();
                } else {
                    passwordField.requestFocus();
                }

                throw new IllegalArgumentException(
                        "Password must contain 8 to 255 characters. "
                                + "Letters, numbers, or a combination are allowed."
                );
            }
        }

        return new User(
                creating
                        ? userIdField.getText()
                        : editingUser.getUserId(),

                creating
                        ? roleSpecificIdField.getText()
                        : editingUser.getRoleSpecificId(),

                username,
                password,
                firstName,
                lastName,
                email,
                role
        );
    }

    private String required(
            TextField field,
            String label,
            int maxLength
    ) {
        String value = field.getText().trim();

        if (value.isEmpty()) {
            field.requestFocus();

            throw new IllegalArgumentException(
                    label + " is required."
            );
        }

        if (value.length() > maxLength) {
            field.requestFocus();

            throw new IllegalArgumentException(
                    label + " must contain no more than "
                            + maxLength + " characters."
            );
        }

        return value;
    }

    private void updateActions() {
        boolean editing = editingUser != null;
        String role = roleComboBox.getValue();

        boolean showDepartment =
                !editing && role != null && !"Admin".equals(role);

        boolean showBatch =
                !editing && "Undergraduate".equals(role);

        departmentBox.setVisible(showDepartment);
        departmentBox.setManaged(showDepartment);

        batchBox.setVisible(showBatch);
        batchBox.setManaged(showBatch);

        roleComboBox.setDisable(busy || !ready || editing);

        detailsBox.setDisable(
                busy || !ready || role == null
        );

        addButton.setDisable(
                busy || !ready || editing || !idsReady
        );

        updateButton.setDisable(
                busy || !ready || !editing
        );

        deleteButton.setDisable(
                busy || !ready || !editing
        );

        clearButton.setDisable(busy || !ready);
        refreshButton.setDisable(busy);

        usersTable.setDisable(busy);
        searchField.setDisable(busy);

        progressIndicator.setVisible(busy);
        progressIndicator.setManaged(busy);
    }

    private void setBusy(boolean value) {
        busy = value;
        updateActions();
    }

    private <T> void runDatabase(
            Callable<T> operation,
            Consumer<T> success,
            Consumer<Throwable> failure
    ) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return operation.call();
            }
        };

        task.setOnSucceeded(event -> success.accept(task.getValue()));
        task.setOnFailed(event -> failure.accept(task.getException()));

        Thread thread = new Thread(task, "ams-user-management");
        thread.setDaemon(true);
        thread.start();
    }

    private Window owner() {
        return usersTable.getScene() == null
                ? null
                : usersTable.getScene().getWindow();
    }

    private String roleName(String role) {
        return "Technical_Officer".equals(role)
                ? "Technical Officer"
                : safe(role);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String errorMessage(Throwable error) {
        if (error != null) {
            error.printStackTrace();

            if (error.getMessage() != null
                    && !error.getMessage().isBlank()) {

                return error.getMessage();
            }
        }

        return "The operation failed. Please try again.";
    }
}