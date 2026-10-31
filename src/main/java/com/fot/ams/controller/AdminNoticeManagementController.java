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

    @FXML private TextField noticeIdField;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;

    @FXML private DatePicker publishedDatePicker;
    @FXML private DatePicker createdDatePicker;

    @FXML private TableView<Notice> noticesTable;

    @FXML private TableColumn<Notice, String> noticeIdColumn;
    @FXML private TableColumn<Notice, String> titleColumn;
    @FXML private TableColumn<Notice, String> contentColumn;
    @FXML private TableColumn<Notice, String> publishedDateColumn;
    @FXML private TableColumn<Notice, String> createdDateColumn;

    @FXML private Label messageLabel;

    private final NoticeDAO noticeDAO = new NoticeDAO();

    private final ObservableList<Notice> noticeList =
            FXCollections.observableArrayList();

    private static final double TABLE_ROW_HEIGHT = 38.0;

    // Allows space for the header and table borders.
    private static final double TABLE_HEADER_SPACE = 48.0;

    @FXML
    public void initialize() {
        configureColumns();

        noticeIdField.setEditable(false);

        noticesTable.setItems(noticeList);

        configureTableHeight();

        noticesTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldNotice, selectedNotice) -> {
                    if (selectedNotice != null) {
                        fillForm(selectedNotice);
                    }
                });

        loadNotices();
    }

    private void configureColumns() {
        noticeIdColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        String.valueOf(data.getValue().getNoticeId())
                )
        );

        titleColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getTitle()
                )
        );

        contentColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getContent()
                )
        );

        publishedDateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getPublishedDate() == null
                                ? ""
                                : data.getValue()
                                .getPublishedDate()
                                .toString()
                )
        );

        createdDateColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCreatedDate() == null
                                ? ""
                                : data.getValue()
                                .getCreatedDate()
                                .toString()
                )
        );

        noticesTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        noticesTable.setPlaceholder(
                new Label("No notices available.")
        );
    }

    private void configureTableHeight() {
        noticesTable.setFixedCellSize(TABLE_ROW_HEIGHT);

        // Prevent the parent layout from shrinking the table.
        noticesTable.setMinHeight(Region.USE_PREF_SIZE);
        noticesTable.setMaxHeight(Region.USE_PREF_SIZE);

        // noticeList is reused by loadNotices(), so this binding
        // updates after loading, adding, updating or deleting rows.
        noticesTable.prefHeightProperty().bind(
                Bindings.createDoubleBinding(
                        () -> {
                            int visibleRows = Math.max(
                                    1,
                                    noticeList.size()
                            );

                            return visibleRows
                                    * noticesTable.getFixedCellSize()
                                    + TABLE_HEADER_SPACE;
                        },
                        noticeList,
                        noticesTable.fixedCellSizeProperty()
                )
        );
    }

    @FXML
    private void handlePublish() {
        if (!validateForm()) {
            return;
        }

        Notice notice = new Notice();

        notice.setTitle(titleField.getText().trim());
        notice.setContent(contentArea.getText().trim());
        notice.setPublishedDate(publishedDatePicker.getValue());
        notice.setCreatedDate(createdDatePicker.getValue());

        try {
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
            handleDatabaseError(e);
        }
    }

    @FXML
    private void handleUpdate() {
        if (noticeIdField.getText().trim().isEmpty()) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Select Notice",
                    "Select a notice from the table first."
            );
            return;
        }

        if (!validateForm()) {
            return;
        }

        try {
            Notice notice = new Notice(
                    Integer.parseInt(
                            noticeIdField.getText().trim()
                    ),
                    titleField.getText().trim(),
                    contentArea.getText().trim(),
                    publishedDatePicker.getValue(),
                    createdDatePicker.getValue()
            );

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
            showAlert(
                    Alert.AlertType.ERROR,
                    "Invalid ID",
                    "Notice ID must be a valid number."
            );

        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    @FXML
    private void handleDelete() {
        if (noticeIdField.getText().trim().isEmpty()) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Select Notice",
                    "Select a notice from the table first."
            );
            return;
        }

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

        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            int noticeId = Integer.parseInt(
                    noticeIdField.getText().trim()
            );

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
            showAlert(
                    Alert.AlertType.ERROR,
                    "Invalid ID",
                    "Notice ID must be a valid number."
            );

        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    private void loadNotices() {
        try {
            noticeList.setAll(
                    noticeDAO.getAllNotices()
            );

        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

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

        messageLabel.setText(
                "Selected Notice ID: " + notice.getNoticeId()
        );
    }

    private boolean validateForm() {
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

        if (publishedDatePicker.getValue() == null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Required",
                    "Select the published date."
            );

            publishedDatePicker.requestFocus();
            return false;
        }

        if (createdDatePicker.getValue() == null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Required",
                    "Select the created date."
            );

            createdDatePicker.requestFocus();
            return false;
        }

        return true;
    }

    private void clearForm() {
        noticesTable.getSelectionModel().clearSelection();

        noticeIdField.clear();
        titleField.clear();
        contentArea.clear();

        publishedDatePicker.setValue(null);
        createdDatePicker.setValue(null);

        messageLabel.setText("");

        titleField.requestFocus();
    }

    private void handleDatabaseError(SQLException e) {
        e.printStackTrace();

        showAlert(
                Alert.AlertType.ERROR,
                "Database Error",
                "Database operation failed: " + e.getMessage()
        );
    }

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