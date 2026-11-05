package com.fot.ams.controller;

import com.fot.ams.dao.*;
import com.fot.ams.model.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Desktop;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.*;

//UI content for the lecturer Notices page
public final class LecturerNoticesPageController extends LecturerPageBase {

    public LecturerNoticesPageController(LecturerPageContext context) {
        super(context);
    }

    public void showNotices() {

        displayPage(
                "Notices",
                "View faculty notices and announcements.",
                noticesBtn
        );

        Label heading = new Label("Faculty Notices");

        heading.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        // Notice table
        TableView<Notice> noticesTable = new TableView<>();

        noticesTable.setPlaceholder(
                new Label("No published notices available.")
        );

        noticesTable.setPrefHeight(300);

        noticesTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        // Title column
        TableColumn<Notice, String> titleCol =
                new TableColumn<>("Notice Title");

        titleCol.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getTitle() == null
                                ? ""
                                : data.getValue().getTitle()
                )
        );

        // Published date column
        TableColumn<Notice, String> dateCol =
                new TableColumn<>("Published Date");

        dateCol.setCellValueFactory(data -> {

            java.time.LocalDate date =
                    data.getValue().getPublishedDate();

            return new ReadOnlyStringWrapper(
                    date == null ? "" : date.toString()
            );
        });

        noticesTable.getColumns().addAll(
                titleCol,
                dateCol
        );

        titleCol.setPrefWidth(420);
        dateCol.setPrefWidth(160);

        // Selected notice details
        Label detailsHeading = new Label("Notice Details");

        detailsHeading.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        TextArea noticeContent = new TextArea();

        noticeContent.setEditable(false);
        noticeContent.setWrapText(true);
        noticeContent.setPrefRowCount(7);

        noticeContent.setPromptText(
                "Select a notice to view its content."
        );

        noticeContent.setStyle(
                "-fx-control-inner-background: #F8FAFC;" +
                        "-fx-font-size: 13px;"
        );

        // Show notice content on row selection
        noticesTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldNotice, selectedNotice) -> {

                    if (selectedNotice == null) {
                        noticeContent.clear();
                        return;
                    }

                    noticeContent.setText(
                            selectedNotice.getContent() == null
                                    ? ""
                                    : selectedNotice.getContent()
                    );
                });

        // Load published notices from database
        try {

            NoticeDAO noticeDAO = new NoticeDAO();

            List<Notice> notices =
                    noticeDAO.getAllNotices();

            java.time.LocalDate today =
                    java.time.LocalDate.now();

            for (Notice notice : notices) {

                java.time.LocalDate publishedDate =
                        notice.getPublishedDate();

                // Only display notices published by today
                if (publishedDate != null &&
                        !publishedDate.isAfter(today)) {

                    noticesTable.getItems().add(notice);
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to load faculty notices."
            ).showAndWait();
        }

        contentArea.getChildren().addAll(
                heading,
                noticesTable,
                detailsHeading,
                noticeContent
        );
    }
}
