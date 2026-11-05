package com.fot.ams.controller;

import com.fot.ams.dao.NoticeDAO;
import com.fot.ams.model.Notice;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;

import java.sql.SQLException;
import java.util.Optional;

public class AdminNoticeManagementController {

    // Connects the notice input fields to the FXML file.
    @FXML private TextField noticeIdField;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;

    // Date selectors for the notice.
    @FXML private DatePicker publishedDatePicker;
    @FXML private DatePicker createdDatePicker;

    // Table that displays notices.
    @FXML private TableView<Notice> noticesTable;

    // Table columns for each notice detail.
    @FXML private TableColumn<Notice, String> noticeIdColumn;
    @FXML private TableColumn<Notice, String> titleColumn;
    @FXML private TableColumn<Notice, String> contentColumn;
    @FXML private TableColumn<Notice, String> publishedDateColumn;
    @FXML private TableColumn<Notice, String> createdDateColumn;

    // Displays status and error messages.
    @FXML private Label messageLabel;

    // Handles notice-related database operations.
    private final NoticeDAO noticeDAO = new NoticeDAO();

    // Stores notices and updates the table when the list changes.
    private final ObservableList<Notice> noticeList =
            FXCollections.observableArrayList();

    // Heights used to calculate the table's size.
    private static final double TABLE_ROW_HEIGHT = 38.0;

    // Extra space for the table header and borders.
    private static final double TABLE_HEADER_SPACE = 48.0;

    // JavaFX calls this after loading the FXML controls.
    @FXML
    public void initialize() {
        // Connect the table columns to Notice properties.
        configureColumns();

        // Notice IDs come from the database, so they cannot be edited.
        noticeIdField.setEditable(false);

        // Show the notices stored in noticeList in the table.
        noticesTable.setItems(noticeList);

        // Set the table height based on the number of notices.
        configureTableHeight();

        // Show a selected notice's details in the form.
        noticesTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldNotice, selectedNotice) -> {
                    if (selectedNotice != null) {
                        fillForm(selectedNotice);
                    }
                });

        // Load the notices from the database.
        loadNotices();
    }

    // Connects each table column to the matching Notice value.
    private void configureColumns() {
        // Display the notice ID as text.
        noticeIdColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        String.valueOf(data.getValue().getNoticeId())
                )
        );

        // Display the notice title.
        titleColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getTitle()
                )
        );

        // Display the notice content.
        contentColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getContent()
                )
        );

        // Display the published date, or a blank value if it is missing.
        publishedDateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getPublishedDate() == null
                                ? ""
                                : data.getValue()
                                .getPublishedDate()
                                .toString()
                )
        );

        // Display the created date, or a blank value if it is missing.
        createdDateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCreatedDate() == null
                                ? ""
                                : data.getValue()
                                .getCreatedDate()
                                .toString()
                )
        );

        // Let the columns use the available table width.
        noticesTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        // Show this text if the table has no notices.
        noticesTable.setPlaceholder(
                new Label("No notices available.")
        );
    }

    // Makes the table tall enough to show all its rows.
    private void configureTableHeight() {
        noticesTable.setFixedCellSize(TABLE_ROW_HEIGHT);

        // Prevent the parent layout from shrinking the table.
        noticesTable.setMinHeight(Region.USE_PREF_SIZE);
        noticesTable.setMaxHeight(Region.USE_PREF_SIZE);

        // Keep the preferred height matched to the number of notices.
        noticesTable.prefHeightProperty().bind(
                Bindings.createDoubleBinding(
                        () -> {
                            // Keep one row of space for the empty-table message.
                            int visibleRows = Math.max(
                                    1,
                                    noticeList.size()
                            );

                            // Add the row heights and header space together.
                            return visibleRows
                                    * noticesTable.getFixedCellSize()
                                    + TABLE_HEADER_SPACE;
                        },
                        noticeList,
                        noticesTable.fixedCellSizeProperty()
                )
        );
    }

    // Validates the form and publishes a new notice.
    @FXML
    private void handlePublish() {
        // Stop if any required value is missing.
        if (!validateForm()) {
            return;
        }

        // Create a Notice object using the form values.
        Notice notice = new Notice();

        notice.setTitle(titleField.getText().trim());
        notice.setContent(contentArea.getText().trim());
        notice.setPublishedDate(publishedDatePicker.getValue());
        notice.setCreatedDate(createdDatePicker.getValue());

        try {
            // Save the notice and refresh the table if successful.
            if (noticeDAO.addNotice(notice)) {
                loadNotices();
                clearForm();

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Notice created successfully."
                );
            } else {
                showAlert(
                        Alert.AlertType.ERROR,
                        "Failed",
                        "Notice could not be created."
                );
            }

        } catch (SQLException e) {
            // Display a message if the database operation fails.
            handleDatabaseError(e);
        }
    }

    // Updates the notice currently selected in the table.
    @FXML
    private void handleUpdate() {
        // A notice must be selected before it can be updated.
        if (noticeIdField.getText().trim().isEmpty()) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Select Notice",
                    "Select a notice from the table first."
            );
            return;
        }

        // Make sure the required form values are present.
        if (!validateForm()) {
            return;
        }

        try {
            // Create an updated Notice object from the form.
            Notice notice = new Notice(
                    Integer.parseInt(
                            noticeIdField.getText().trim()
                    ),
                    titleField.getText().trim(),
                    contentArea.getText().trim(),
                    publishedDatePicker.getValue(),
                    createdDatePicker.getValue()
            );

            // Save the changes and refresh the table if successful.
            if (noticeDAO.updateNotice(notice)) {
                loadNotices();
                clearForm();

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Notice updated successfully."
                );
            } else {
                showAlert(
                        Alert.AlertType.WARNING,
                        "Not Found",
                        "Notice was not found."
                );
            }

        } catch (NumberFormatException e) {
            // Show an error if the notice ID is not a valid number.
            showAlert(
                    Alert.AlertType.ERROR,
                    "Invalid ID",
                    "Notice ID must be a valid number."
            );

        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    // Confirms and deletes the selected notice.
    @FXML
    private void handleDelete() {
        // A notice must be selected before it can be deleted.
        if (noticeIdField.getText().trim().isEmpty()) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Select Notice",
                    "Select a notice from the table first."
            );
            return;
        }

        // Ask the admin to confirm before deleting.
        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Notice");

        confirmation.setContentText(
                "Are you sure you want to delete this notice?"
        );

        Optional<ButtonType> result =
                confirmation.showAndWait();

        // Stop if the admin cancels or closes the confirmation.
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            int noticeId = Integer.parseInt(
                    noticeIdField.getText().trim()
            );

            // Delete the notice and refresh the screen if successful.
            if (noticeDAO.deleteNotice(noticeId)) {
                loadNotices();
                clearForm();

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Notice deleted successfully."
                );
            } else {
                showAlert(
                        Alert.AlertType.WARNING,
                        "Not Found",
                        "Notice was not found."
                );
            }

        } catch (NumberFormatException e) {
            // Show an error if the notice ID is not a valid number.
            showAlert(
                    Alert.AlertType.ERROR,
                    "Invalid ID",
                    "Notice ID must be a valid number."
            );

        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    // Clears the notice form when the Clear button is pressed.
    @FXML
    private void handleClear() {
        clearForm();
    }

    // Loads all notices from the database into the table list.
    private void loadNotices() {
        try {
            // Replace the current list with the database results.
            noticeList.setAll(
                    noticeDAO.getAllNotices()
            );

        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    // Copies a selected notice's values into the form.
    private void fillForm(Notice notice) {
        noticeIdField.setText(
                String.valueOf(notice.getNoticeId())
        );

        titleField.setText(
                notice.getTitle()
        );

        contentArea.setText(
                notice.getContent()
        );

        publishedDatePicker.setValue(
                notice.getPublishedDate()
        );

        createdDatePicker.setValue(
                notice.getCreatedDate()
        );

        // Tell the admin which notice is selected.
        messageLabel.setText(
                "Selected Notice ID: " + notice.getNoticeId()
        );
    }

    // Checks that all required notice details have been entered.
    private boolean validateForm() {
        // Require a notice title.
        if (titleField.getText() == null
                || titleField.getText().trim().isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Required",
                    "Enter the notice title."
            );

            titleField.requestFocus();
            return false;
        }

        // Require notice content.
        if (contentArea.getText() == null
                || contentArea.getText().trim().isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Required",
                    "Enter the notice content."
            );

            contentArea.requestFocus();
            return false;
        }

        // Require a published date.
        if (publishedDatePicker.getValue() == null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Required",
                    "Select the published date."
            );

            publishedDatePicker.requestFocus();
            return false;
        }

        // Require a created date.
        if (createdDatePicker.getValue() == null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Required",
                    "Select the created date."
            );

            createdDatePicker.requestFocus();
            return false;
        }

        // All required values are present.
        return true;
    }

    // Clears the form and removes the selected table row.
    private void clearForm() {
        noticesTable.getSelectionModel().clearSelection();

        noticeIdField.clear();
        titleField.clear();
        contentArea.clear();

        publishedDatePicker.setValue(null);
        createdDatePicker.setValue(null);

        messageLabel.setText("");

        // Put the cursor back in the title field.
        titleField.requestFocus();
    }

    // Logs and displays an error from a database operation.
    private void handleDatabaseError(SQLException e) {
        e.printStackTrace();

        showAlert(
                Alert.AlertType.ERROR,
                "Database Error",
                "Database operation failed: " + e.getMessage()
        );
    }

    // Displays a message in the screen label and an alert window.
    private void showAlert(
            Alert.AlertType type,
            String title,
            String message) {

        messageLabel.setText(message);

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}