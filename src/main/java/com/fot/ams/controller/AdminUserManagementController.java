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

    // Connects the role dropdown to this controller.
    @FXML private ComboBox<String> roleComboBox;

    // Displays the automatically generated IDs.
    @FXML private TextField userIdField;
    @FXML private TextField roleSpecificIdField;

    // Displays labels and messages on the screen.
    @FXML private Label roleSpecificIdLabel;
    @FXML private Label modeLabel;
    @FXML private Label idHintLabel;
    @FXML private Label messageLabel;

    // Groups of fields that can be shown or hidden.
    @FXML private VBox detailsBox;
    @FXML private VBox departmentBox;
    @FXML private VBox batchBox;

    // Dropdowns for selecting department and batch.
    @FXML private ComboBox<UserDAO.Option> departmentComboBox;
    @FXML private ComboBox<UserDAO.Option> batchComboBox;

    // Fields for the user's information.
    @FXML private TextField usernameField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;

    // A normal text field displays the password as it is typed.
    @FXML private TextField passwordField;

    // Buttons for user management.
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    @FXML private Button refreshButton;

    // Search field, user count and loading indicator.
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private ProgressIndicator progressIndicator;

    // Table that displays users and their details.
    @FXML private TableView<User> usersTable;

    @FXML private TableColumn<User, String> userIdColumn;
    @FXML private TableColumn<User, String> roleSpecificIdColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> departmentColumn;

    // Handles user-related database operations.
    private final UserDAO userDAO = new UserDAO();

    // Stores all users and updates the table when the list changes.
    private final ObservableList<User> userList =
            FXCollections.observableArrayList();

    // Shows users that match the search text.
    private final FilteredList<User> filteredUsers =
            new FilteredList<>(userList, user -> true);

    // Tracks the current screen state.
    private boolean busy;
    private boolean ready;
    private boolean idsReady;
    private boolean resetting;

    // Helps ignore outdated ID requests.
    private long idRequestNumber;

    // Stores the user currently being edited.
    private User editingUser;

    // Holds the data needed to display this page.
    private record PageData(
            List<User> users,
            List<UserDAO.Option> departments,
            List<UserDAO.Option> batches
    ) {
    }

    // JavaFX calls this after loading the FXML controls.
    @FXML
    private void initialize() {
        // Add the available roles to the role dropdown.
        roleComboBox.getItems().setAll(
                "Admin",
                "Lecturer",
                "Technical_Officer",
                "Undergraduate"
        );

        // Display role names in a user-friendly format.
        roleComboBox.setConverter(new StringConverter<String>() {
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

        // Set up the department and batch dropdowns.
        configureOptionBox(departmentComboBox);
        configureOptionBox(batchComboBox);

        // Users cannot manually change automatically generated IDs.
        userIdField.setEditable(false);
        roleSpecificIdField.setEditable(false);

        // Connect each table column to a User getter.
        configureColumn(userIdColumn, User::getUserId);
        configureColumn(roleSpecificIdColumn, User::getRoleSpecificId);
        configureColumn(usernameColumn, User::getUsername);
        configureColumn(fullNameColumn, User::getFullName);
        configureColumn(emailColumn, User::getEmail);

        // Display a user-friendly role name in the table.
        configureColumn(
                roleColumn,
                user -> roleName(user.getRole())
        );

        // Display the user's department in the table.
        configureColumn(
                departmentColumn,
                this::departmentName
        );

        // Allow the filtered list to be sorted by clicking table columns.
        SortedList<User> sortedUsers =
                new SortedList<>(filteredUsers);

        sortedUsers.comparatorProperty().bind(
                usersTable.comparatorProperty()
        );

        // Show the sorted and filtered users in the table.
        usersTable.setItems(sortedUsers);

        // React when the selected table row changes.
        usersTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldUser, selectedUser) -> {
                    // Ignore changes during resets or database operations.
                    if (resetting || busy) {
                        return;
                    }

                    // Clear the form or show the selected user's details.
                    if (selectedUser == null) {
                        clearFields();
                    } else {
                        showUser(selectedUser);
                    }
                });

        // React when a different role is selected.
        roleComboBox.valueProperty().addListener(
                (observable, oldRole, newRole) -> {
                    updateRoleLabel(newRole);

                    // For a new user, clear old choices and generate IDs.
                    if (!resetting && editingUser == null) {
                        departmentComboBox.setValue(null);
                        batchComboBox.setValue(null);
                        loadNextIds();
                    }

                    updateActions();
                }
        );

        // Filter the table whenever the search text changes.
        searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> filterUsers()
        );

        // Start with a blank form and load the page data.
        clearFields();
        loadPage("Select a role to create a new user.");
    }

    // Sets how items appear in the department or batch dropdown.
    private void configureOptionBox(
            ComboBox<UserDAO.Option> comboBox
    ) {
        // Users must select an existing option.
        comboBox.setEditable(false);

        // Show the option's name in the dropdown.
        comboBox.setConverter(
                new StringConverter<UserDAO.Option>() {
                    @Override
                    public String toString(UserDAO.Option option) {
                        return option == null ? "" : option.name();
                    }

                    @Override
                    public UserDAO.Option fromString(String value) {
                        return null;
                    }
                }
        );
    }

    // Connects a table column to a function that gets text from a User.
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

    // Returns the department name or a dash if it is missing.
    private String departmentName(User user) {
        String name = user.getDepartmentName();

        return name == null || name.isBlank()
                ? "—"
                : name;
    }

    // Filters the displayed users using the search text.
    private void filterUsers() {
        String query = safe(searchField.getText())
                .trim()
                .toLowerCase(Locale.ROOT);

        filteredUsers.setPredicate(user -> {
            // Combine user details into one string to search.
            String text = String.join(
                    " ",
                    safe(user.getUserId()),
                    safe(user.getRoleSpecificId()),
                    safe(user.getUsername()),
                    safe(user.getFullName()),
                    safe(user.getEmail()),
                    roleName(user.getRole()),
                    safe(user.getDepartmentName())
            ).toLowerCase(Locale.ROOT);

            return text.contains(query);
        });

        // Show how many users match the search.
        countLabel.setText(
                filteredUsers.size()
                        + " of "
                        + userList.size()
                        + " users"
        );

        // Show a message when the table has no rows.
        usersTable.setPlaceholder(new Label(
                userList.isEmpty()
                        ? "No users available."
                        : "No users match your search."
        ));
    }

    // Gets the next user IDs for the selected role.
    private void loadNextIds() {
        final long request = ++idRequestNumber;
        final String role = roleComboBox.getValue();

        // Clear old IDs while the new IDs are loading.
        idsReady = false;
        userIdField.clear();
        roleSpecificIdField.clear();

        updateActions();

        // IDs need a selected role and a ready, idle screen.
        if (role == null || !ready || busy) {
            idHintLabel.setText(
                    "Select a role. Both IDs will be generated automatically."
            );
            return;
        }

        idHintLabel.setText("Loading the next available IDs...");

        // Get the IDs from the database in the background.
        runDatabase(
                () -> userDAO.getNextIds(role),

                ids -> {
                    // Ignore results from old or changed requests.
                    if (request != idRequestNumber
                            || editingUser != null
                            || busy
                            || !role.equals(roleComboBox.getValue())) {
                        return;
                    }

                    // Display the generated IDs.
                    userIdField.setText(ids.userId());
                    roleSpecificIdField.setText(ids.roleSpecificId());

                    idsReady = true;

                    idHintLabel.setText(
                            "IDs are automatic and will be confirmed when saving."
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
                            "Could not generate IDs. "
                                    + "Select the role again or click Refresh."
                    );

                    messageLabel.setText(errorMessage(error));
                    updateActions();
                }
        );
    }

    // Sets the label for the selected role's ID.
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

    // Shows a selected user's details in the form.
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

            // Do not display the existing password.
            passwordField.clear();

            // Leave department and batch unchanged during editing.
            departmentComboBox.setValue(null);
            batchComboBox.setValue(null);

        } finally {
            resetting = false;
        }

        modeLabel.setText("Edit User: " + user.getUserId());

        idHintLabel.setText(
                "Existing role and IDs are fixed. "
                        + "Department and batch remain unchanged."
        );

        messageLabel.setText(
                "Department: " + departmentName(user)
                        + ". Leave the password empty "
                        + "to keep the current password."
        );

        updateRoleLabel(user.getRole());
        updateActions();
    }

    // Clears the form and returns to create mode.
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

            departmentComboBox.setValue(null);
            batchComboBox.setValue(null);

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

    // Validates and adds a new user.
    @FXML
    private void addUser() {
        if (busy || editingUser != null || !idsReady) {
            return;
        }

        try {
            // Read and validate the form values.
            User user = readAndValidate(true);

            // Admins do not need a department.
            boolean needsDepartment =
                    !"Admin".equals(user.getRole());

            // Only undergraduates need a batch.
            boolean needsBatch =
                    "Undergraduate".equals(user.getRole());

            UserDAO.Option department =
                    departmentComboBox.getValue();

            UserDAO.Option batch =
                    batchComboBox.getValue();

            // Require a department for non-admin users.
            if (needsDepartment && department == null) {
                departmentComboBox.requestFocus();

                throw new IllegalArgumentException(
                        "Select a department."
                );
            }

            // Require a batch for undergraduate users.
            if (needsBatch && batch == null) {
                batchComboBox.requestFocus();

                throw new IllegalArgumentException(
                        "Select an undergraduate batch."
                );
            }

            // Get the selected IDs when they are required.
            Integer departmentId =
                    needsDepartment ? department.id() : null;

            Integer batchId =
                    needsBatch ? batch.id() : null;

            ++idRequestNumber;
            setBusy(true);

            messageLabel.setText("Creating user...");

            // Save the user in the background.
            runDatabase(
                    () -> userDAO.addUser(
                            user,
                            departmentId,
                            batchId
                    ),

                    saved -> {
                        // Clear the password after saving.
                        user.setPassword(null);
                        passwordField.clear();

                        setBusy(false);

                        if (!saved) {
                            messageLabel.setText(
                                    "The user could not be created."
                            );
                            loadNextIds();
                            return;
                        }

                        // Reload the table and show the new IDs.
                        loadPage(
                                "User created: "
                                        + user.getUserId()
                                        + " | Role ID: "
                                        + user.getRoleSpecificId()
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

    // Validates and updates the selected user.
    @FXML
    private void updateUser() {
        if (busy || editingUser == null) {
            return;
        }

        try {
            User user = readAndValidate(false);

            // An empty password means keep the current password.
            boolean changePassword =
                    !passwordField.getText().isEmpty();

            setBusy(true);
            messageLabel.setText("Updating user...");

            // Update the user in the background.
            runDatabase(
                    () -> userDAO.updateUser(
                            user,
                            changePassword
                    ),

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

    // Asks for confirmation before deleting the selected user.
    @FXML
    private void deleteUser() {
        if (busy || editingUser == null) {
            return;
        }

        User selected = editingUser;

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        // Attach the dialog to the application's window.
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
                        + "\n\nThis permanently deletes the account "
                        + "and its role record."
                        + "\nLinked records may also be deleted "
                        + "by database cascade rules."
        );

        // Stop if the user does not confirm.
        if (confirmation.showAndWait()
                .orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        setBusy(true);
        messageLabel.setText("Deleting user...");

        // Delete the user in the background.
        runDatabase(
                () -> userDAO.deleteUser(selected.getUserId()),

                deleted -> {
                    setBusy(false);

                    // Reload the table after deleting.
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

    // Reload users, departments and batches.
    @FXML
    private void refreshUsers() {
        loadPage(
                "Users refreshed. Select a role to create a new user."
        );
    }

    // Loads the data needed by this screen.
    private void loadPage(String successMessage) {
        if (busy) {
            return;
        }

        ++idRequestNumber;
        setBusy(true);

        messageLabel.setText(
                "Loading users, departments and batches..."
        );

        // Load the database lists in the background.
        runDatabase(
                () -> new PageData(
                        userDAO.getAllUsers(),
                        userDAO.getDepartments(),
                        userDAO.getBatches()
                ),

                data -> {
                    resetting = true;

                    try {
                        usersTable.getSelectionModel()
                                .clearSelection();

                        // Replace the old data with the newly loaded data.
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

    // Reads the form and checks that its values are valid.
    private User readAndValidate(boolean creating) {
        // Keep the selected role when editing an existing user.
        String role = creating
                ? roleComboBox.getValue()
                : editingUser.getRole();

        if (role == null) {
            roleComboBox.requestFocus();

            throw new IllegalArgumentException(
                    "Select a role first."
            );
        }

        // Wait for generated IDs before creating a user.
        if (creating && !idsReady) {
            throw new IllegalArgumentException(
                    "Wait until both IDs have been generated."
            );
        }

        // Read required fields and check their maximum lengths.
        String username = required(
                usernameField, "Username", 50
        );

        String firstName = required(
                firstNameField, "First name", 50
        );

        String lastName = required(
                lastNameField, "Last name", 50
        );

        String email = required(
                emailField, "Email", 100
        );

        // Check the basic format of the email address.
        if (!email.matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            emailField.requestFocus();

            throw new IllegalArgumentException(
                    "Enter a valid email address."
            );
        }

        String password = passwordField.getText();

        // A new user needs a password.
        // During an update, check it only if a new password was entered.
        if (creating || !password.isEmpty()) {
            if (password.isBlank()
                    || password.length() < 8
                    || password.length() > 255) {

                passwordField.requestFocus();

                throw new IllegalArgumentException(
                        "Password must contain 8 to 255 characters."
                );
            }
        }

        // Create a User object using the validated values.
        return new User(
                // Keep the existing ID when editing.
                creating
                        ? userIdField.getText()
                        : editingUser.getUserId(),

                // Keep the existing role ID when editing.
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

    // Checks that a required text field is not empty or too long.
    private String required(
            TextField field,
            String label,
            int maxLength
    ) {
        String value = safe(field.getText()).trim();

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

    // Shows or hides fields and enables or disables buttons.
    private void updateActions() {
        boolean editing = editingUser != null;
        String role = roleComboBox.getValue();

        // Show department when creating a non-admin user.
        boolean showDepartment =
                !editing
                        && role != null
                        && !"Admin".equals(role);

        // Show batch only when creating an undergraduate.
        boolean showBatch =
                !editing
                        && "Undergraduate".equals(role);

        departmentBox.setVisible(showDepartment);
        departmentBox.setManaged(showDepartment);

        batchBox.setVisible(showBatch);
        batchBox.setManaged(showBatch);

        // Show a helpful prompt in each dropdown.
        departmentComboBox.setPromptText(
                departmentComboBox.getItems().isEmpty()
                        ? "No departments available"
                        : "Select department"
        );

        batchComboBox.setPromptText(
                batchComboBox.getItems().isEmpty()
                        ? "No batches available"
                        : "Select undergraduate batch"
        );

        // Do not allow role changes during loading or editing.
        roleComboBox.setDisable(
                busy || !ready || editing
        );

        // Enable detail fields only when they can be used.
        detailsBox.setDisable(
                busy || !ready || role == null
        );

        // Add needs generated IDs and a new user form.
        addButton.setDisable(
                busy || !ready || editing || !idsReady
        );

        // Update and Delete are available only while editing.
        updateButton.setDisable(
                busy || !ready || !editing
        );

        deleteButton.setDisable(
                busy || !ready || !editing
        );

        clearButton.setDisable(busy || !ready);
        refreshButton.setDisable(busy);

        // Prevent table and search actions during database work.
        usersTable.setDisable(busy);
        searchField.setDisable(busy);

        // Show the progress indicator while work is running.
        progressIndicator.setVisible(busy);
        progressIndicator.setManaged(busy);
    }

    // Sets the busy state and updates the controls.
    private void setBusy(boolean value) {
        busy = value;
        updateActions();
    }

    // Runs database work in the background to keep the UI responsive.
    private <T> void runDatabase(
            Callable<T> operation,
            Consumer<T> success,
            Consumer<Throwable> failure
    ) {
        // This Task runs the supplied operation on a background thread.
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return operation.call();
            }
        };

        // Handle the result if the task succeeds.
        task.setOnSucceeded(
                event -> success.accept(task.getValue())
        );

        // Handle the error if the task fails.
        task.setOnFailed(
                event -> failure.accept(task.getException())
        );

        // Start the task on a background thread.
        Thread thread = new Thread(
                task,
                "ams-user-management"
        );

        thread.setDaemon(true);
        thread.start();
    }

    // Gets the window containing the users table.
    private Window owner() {
        return usersTable.getScene() == null
                ? null
                : usersTable.getScene().getWindow();
    }

    // Displays the technical officer role with spaces.
    private String roleName(String role) {
        return "Technical_Officer".equals(role)
                ? "Technical Officer"
                : safe(role);
    }

    // Returns an empty string instead of null.
    private String safe(String value) {
        return value == null ? "" : value;
    }

    // Gets the error message or returns a general message.
    private String errorMessage(Throwable error) {
        if (error != null
                && error.getMessage() != null
                && !error.getMessage().isBlank()) {

            return error.getMessage();
        }

        return "The operation failed. Please try again.";
    }
}