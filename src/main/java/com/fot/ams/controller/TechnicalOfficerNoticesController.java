package com.fot.ams.controller;

import com.fot.ams.dao.NoticeDAO;
import com.fot.ams.model.Notice;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

// TG2070 - View published notices
public class TechnicalOfficerNoticesController {

    @FXML
    private TextField noticeSearchField;

    @FXML
    private TableView<Notice> noticesTable;

    @FXML
    private TableColumn<Notice, Integer> noticeIdColumn;

    @FXML
    private TableColumn<Notice, String> noticeTitleColumn;

    @FXML
    private TableColumn<Notice, LocalDate> noticePublishedDateColumn;

    @FXML
    private Label selectedNoticeTitleLabel;

    @FXML
    private Label selectedNoticeDateLabel;

    @FXML
    private TextArea noticeContentArea;

    @FXML
    private void initialize() {
        noticeIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("noticeId")
        );

        noticeTitleColumn.setCellValueFactory(
                new PropertyValueFactory<>("title")
        );

        noticePublishedDateColumn.setCellValueFactory(
                new PropertyValueFactory<>("publishedDate")
        );

        noticesTable.setPlaceholder(
                new Label("No published notices found.")
        );

        // TG2070 - Show details when a notice is selected
        noticesTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldNotice, selectedNotice) -> {
                    showNoticeDetails(selectedNotice);
                });

        // TG2070 - Search when Enter is pressed
        noticeSearchField.setOnAction(event -> searchNotices());

        searchNotices();
    }

    // TG2070 - Load published notices using title search
    @FXML
    private void searchNotices() {
        NoticeDAO noticeDAO = new NoticeDAO();

        try {
            List<Notice> notices = noticeDAO.getPublishedNotices(
                    noticeSearchField.getText()
            );

            noticesTable.getSelectionModel().clearSelection();
            noticesTable.getItems().clear();
            showNoticeDetails(null);

            for (Notice notice : notices) {
                noticesTable.getItems().add(notice);
            }

        } catch (SQLException exception) {
            exception.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Could not load notices.");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }
    }

    // TG2070 - Clear search and reload published notices
    @FXML
    private void refreshNotices() {
        noticeSearchField.clear();
        searchNotices();
    }

    // TG2070 - Display full notice content
    private void showNoticeDetails(Notice notice) {
        if (notice == null) {
            selectedNoticeTitleLabel.setText(
                    "Select a notice to view its details."
            );
            selectedNoticeDateLabel.setText("Published date: —");
            noticeContentArea.clear();
            return;
        }

        selectedNoticeTitleLabel.setText(notice.getTitle());

        selectedNoticeDateLabel.setText(
                "Published date: " + notice.getPublishedDate()
        );

        noticeContentArea.setText(notice.getContent());
        noticeContentArea.positionCaret(0);
    }
}