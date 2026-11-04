package com.fot.ams.controller;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

//Common lecturer ID, page layout and sidebar navigation, supplied by FXML controller
public final class LecturerPageContext {

    private static final String ACTIVE_STYLE =
            "-fx-background-color: white;" +
            "-fx-text-fill: #0078b9;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;";

    private static final String NORMAL_STYLE =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: normal;" +
            "-fx-background-radius: 8;";

    private final String lecturerId;
    private final Label pageTitle;
    private final Label pageDescription;
    private final VBox contentArea;
    private final Button[] navButtons;

    public LecturerPageContext(String lecturerId, Label pageTitle,
                               Label pageDescription, VBox contentArea,
                               Button... navButtons) {
        if (navButtons == null || navButtons.length != 11) {
            throw new IllegalArgumentException("Expected eleven lecturer navigation buttons.");
        }
        this.lecturerId = lecturerId;
        this.pageTitle = pageTitle;
        this.pageDescription = pageDescription;
        this.contentArea = contentArea;
        this.navButtons = navButtons.clone();
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public VBox getContentArea() {
        return contentArea;
    }

    public Button getNavButton(int index) {
        return navButtons[index];
    }

    public void displayPage(String title, String description, Button activeButton) {
        pageTitle.setText(title);
        pageDescription.setText(description);
        contentArea.getChildren().clear();
        for (Button button : navButtons) {
            if (button != null) {
                button.setStyle(button == activeButton ? ACTIVE_STYLE : NORMAL_STYLE);
            }
        }
    }
}
