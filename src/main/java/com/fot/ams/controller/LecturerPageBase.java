package com.fot.ams.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

//Shared view utilities used by all lecturer feature controllers
public abstract class LecturerPageBase {

    private final LecturerPageContext context;
    protected final String lecturerId;
    protected final VBox contentArea;

    protected final Button dashboardBtn;
    protected final Button profileBtn;
    protected final Button coursesBtn;
    protected final Button materialsBtn;
    protected final Button marksBtn;
    protected final Button studentsBtn;
    protected final Button eligibilityBtn;
    protected final Button attendanceBtn;
    protected final Button medicalsBtn;
    protected final Button gradesBtn;
    protected final Button noticesBtn;

    protected LecturerPageBase(LecturerPageContext context) {
        this.context = context;
        this.lecturerId = context.getLecturerId();
        this.contentArea = context.getContentArea();
        this.dashboardBtn = context.getNavButton(0);
        this.profileBtn = context.getNavButton(1);
        this.coursesBtn = context.getNavButton(2);
        this.materialsBtn = context.getNavButton(3);
        this.marksBtn = context.getNavButton(4);
        this.studentsBtn = context.getNavButton(5);
        this.eligibilityBtn = context.getNavButton(6);
        this.attendanceBtn = context.getNavButton(7);
        this.medicalsBtn = context.getNavButton(8);
        this.gradesBtn = context.getNavButton(9);
        this.noticesBtn = context.getNavButton(10);
    }

    protected final void displayPage(String title, String description, Button activeButton) {
        context.displayPage(title, description, activeButton);
    }

    protected final VBox createStatCard(
            String title,
            String value,
            String subtitle) {

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-text-fill: #526174;" +
                        "-fx-font-size: 13px;"
        );

        Label valueLabel = new Label(value);
        valueLabel.setStyle(
                "-fx-text-fill: #172B45;" +
                        "-fx-font-size: 27px;" +
                        "-fx-font-weight: bold;"
        );

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle(
                "-fx-text-fill: #7B8797;" +
                        "-fx-font-size: 11px;"
        );

        VBox card = new VBox(
                13,
                titleLabel,
                valueLabel,
                subtitleLabel
        );

        card.setPadding(new Insets(18));
        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPrefHeight(145);

        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #D5DCE5;" +
                        "-fx-border-radius: 8;"
        );

        HBox.setHgrow(card, Priority.ALWAYS);

        return card;
    }

    protected final TableView<String[]> createTable(
            String placeholder,
            String... headings) {

        TableView<String[]> table = new TableView<>();

        table.setPlaceholder(new Label(placeholder));
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        for (int index = 0;
             index < headings.length; index++) {

            final int columnIndex = index;

            TableColumn<String[], String> column =
                    new TableColumn<>(headings[index]);

            column.setCellValueFactory(cell -> {
                String[] row = cell.getValue();

                String value =
                        row != null &&
                                columnIndex < row.length
                                ? row[columnIndex]
                                : "";

                return new ReadOnlyStringWrapper(value);
            });

            table.getColumns().add(column);
        }

        table.setPrefHeight(210);
        table.setMinHeight(170);

        return table;
    }
}
