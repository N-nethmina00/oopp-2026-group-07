package com.fot.ams.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

public class StudentNoticesController {

    private record Notice(String title, String date, String body) {}

    @FXML private VBox noticeBox;

    @FXML
    private void initialize() {
        // TODO: NoticeDAO.findAll()
        List<Notice> notices = List.of(
                new Notice("Mid Semester Examination Schedule", "2026-10-10",
                        "Mid semester examinations will commence on 2026-10-26. Please check the timetable for your hall allocation."),
                new Notice("Library Closed on Poya Day", "2026-10-06",
                        "The main library will remain closed on Poya day. Online resources will be available as usual."),
                new Notice("Medical Submission Deadline", "2026-10-02",
                        "All medical applications must be submitted within 7 days of the missed session."));

        for (Notice n : notices) {
            VBox card = new VBox(6);
            card.getStyleClass().add("section-card");

            Label date  = new Label(n.date());  date.getStyleClass().add("notice-date");
            Label title = new Label(n.title()); title.getStyleClass().add("notice-title");
            Label body  = new Label(n.body());  body.getStyleClass().add("notice-body");
            body.setMaxWidth(Double.MAX_VALUE);

            card.getChildren().addAll(date, title, body);
            noticeBox.getChildren().add(card);
        }
    }
}
