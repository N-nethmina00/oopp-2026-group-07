
package com.fot.ams.controller;

import com.fot.ams.dao.StudentNoticeDAO;
import com.fot.ams.model.Notice;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

public class StudentNoticesController {

    @FXML
    private VBox noticeBox;

    private final StudentNoticeDAO noticeDAO =
            new StudentNoticeDAO();

    @FXML
    private void initialize() {
        loadNotices();
    }

    // Load published notices from database
    private void loadNotices() {

        noticeBox.getChildren().clear();

        try {

            // Get published notices using DAO
            List<Notice> notices =
                    noticeDAO.getPublishedNotices();

            // If there are no published notices
            if (notices.isEmpty()) {

                Label emptyLabel =
                        new Label("No notices available.");

                emptyLabel.getStyleClass().add("notice-body");

                noticeBox.getChildren().add(emptyLabel);
                return;
            }

            // Display notices using Notice Model
            for (Notice notice : notices) {

                VBox card = new VBox(6);
                card.getStyleClass().add("section-card");

                // Published Date
                String dateText =
                        notice.getPublishedDate() == null
                                ? ""
                                : notice.getPublishedDate().toString();

                Label date = new Label(dateText);
                date.getStyleClass().add("notice-date");

                // Notice Title
                Label title =
                        new Label(notice.getTitle());

                title.getStyleClass().add("notice-title");
                title.setWrapText(true);

                // Notice Content
                Label body =
                        new Label(notice.getContent());

                body.getStyleClass().add("notice-body");
                body.setWrapText(true);
                body.setMaxWidth(Double.MAX_VALUE);

                // Add labels to card
                card.getChildren().addAll(
                        date,
                        title,
                        body
                );

                // Display card in VBox
                noticeBox.getChildren().add(card);
            }

        } catch (SQLException e) {

            e.printStackTrace();

            Label errorLabel =
                    new Label("Unable to load notices.");

            noticeBox.getChildren().add(errorLabel);

            new Alert(
                    Alert.AlertType.ERROR,
                    "Database Error: " + e.getMessage()
            ).showAndWait();
        }
    }
}
