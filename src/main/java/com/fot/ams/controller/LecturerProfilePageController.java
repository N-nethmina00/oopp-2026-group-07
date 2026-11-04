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

//UI content for the lecturer Profile page.

public final class LecturerProfilePageController extends LecturerPageBase {

    public LecturerProfilePageController(LecturerPageContext context) {
        super(context);
    }

    public void showProfile() {

        displayPage(
                "My Profile",
                "View your personal and academic information.",
                profileBtn
        );

        Label heading = new Label("Lecturer Profile");

        heading.setStyle(
                "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #17324D;"
        );

        try {

            LecturerProfileDAO dao =
                    new LecturerProfileDAO();

            Lecturer lecturer =
                    dao.getLecturerProfile(lecturerId);

            if (lecturer == null) {
                new Alert(
                        Alert.AlertType.WARNING,
                        "Lecturer profile not found."
                ).showAndWait();
                return;
            }

            User user = lecturer;

            GridPane profileGrid = new GridPane();
            profileGrid.setHgap(25);
            profileGrid.setVgap(18);
            profileGrid.setPadding(new Insets(25));

            String[][] details = {
                    {"Lecturer ID", lecturer.getLecturerId()},
                    {"Full Name", user.getFullName()},
                    {"Username", user.getUsername()},
                    {"Email Address", user.getEmail()},
                    {"Department", lecturer.getDepartmentName()},
                    {"Role", user.getRole()}
            };

            for (int i = 0; i < details.length; i++) {

                Label fieldName = new Label(details[i][0]);

                fieldName.setStyle(
                        "-fx-font-weight: bold;" +
                                "-fx-text-fill: #526174;" +
                                "-fx-font-size: 13px;"
                );

                Label fieldValue = new Label(
                        details[i][1] == null
                                ? "Not Available"
                                : details[i][1]
                );

                fieldValue.setWrapText(true);

                fieldValue.setStyle(
                        "-fx-text-fill: #17324D;" +
                                "-fx-font-size: 14px;"
                );

                profileGrid.add(fieldName, 0, i);
                profileGrid.add(fieldValue, 1, i);
            }

            VBox profileCard = new VBox(15);
            profileCard.setPadding(new Insets(20));

            profileCard.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: #D5DCE5;" +
                            "-fx-border-radius: 10;"
            );

            profileCard.getChildren().add(profileGrid);


            String profileImagePath =
                    dao.getProfileImagePath(lecturerId);

// Show profile picture above the details
            profileCard.getChildren().add(
                    0,
                    createLecturerPhoto(profileImagePath)
            );

// Edit Profile button
            Button editProfileButton =
                    new Button("Edit My Profile");

            editProfileButton.setStyle(
                    "-fx-background-color: #4c56cd;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 11 20;"
            );

            editProfileButton.setOnAction(
                    event -> showEditProfileDialog(lecturer)
            );


            contentArea.getChildren().addAll(
                    heading,
                    profileCard,
                    editProfileButton
            );

        } catch (SQLException e) {

            e.printStackTrace();

            new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to load lecturer profile."
            ).showAndWait();
        }
    }

    private javafx.scene.Node createLecturerPhoto(String storedPath) {

        final double size = 120;

        if (storedPath != null && !storedPath.isBlank()) {

            Path root = Paths.get(
                    System.getProperty("user.dir")
            ).toAbsolutePath().normalize();

            Path folder = root.resolve(
                    "uploads/profiles"
            ).normalize();

            Path imagePath = root.resolve(storedPath).normalize();

            if (imagePath.startsWith(folder) &&
                    Files.isRegularFile(imagePath)) {

                Image image = new Image(
                        imagePath.toUri().toString()
                );

                if (!image.isError()) {

                    ImageView imageView = new ImageView(image);

                    // Crop the image to a centered square
                    double imageWidth = image.getWidth();
                    double imageHeight = image.getHeight();

                    double cropSize = Math.min(
                            imageWidth, imageHeight
                    );

                    imageView.setViewport(new Rectangle2D(
                            (imageWidth - cropSize) / 2,
                            (imageHeight - cropSize) / 2,
                            cropSize,
                            cropSize
                    ));

                    imageView.setFitWidth(size);
                    imageView.setFitHeight(size);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);

                    // Make profile picture circular
                    Circle circle = new Circle(
                            size / 2,
                            size / 2,
                            size / 2
                    );

                    imageView.setClip(circle);

                    return imageView;
                }
            }
        }

        // Circular placeholder when no picture exists
        Label placeholder = new Label("👤");

        placeholder.setAlignment(
                javafx.geometry.Pos.CENTER
        );

        placeholder.setPrefSize(size, size);
        placeholder.setMinSize(size, size);
        placeholder.setMaxSize(size, size);

        placeholder.setStyle(
                "-fx-background-color: #E8EAFB;" +
                        "-fx-background-radius: 60;" +
                        "-fx-font-size: 42px;" +
                        "-fx-text-fill: #4c56cd;"
        );

        return placeholder;
    }

    private void showEditProfileDialog(Lecturer lecturer) {

        LecturerProfileDAO dao = new LecturerProfileDAO();
        final String oldPicturePath;
        try {
            oldPicturePath = dao.getProfileImagePath(lecturer.getLecturerId());
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Unable to load current profile picture.").showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit My Profile");
        dialog.setHeaderText("Update Lecturer Profile");

        User user = lecturer;
        TextField firstNameField = new TextField(user.getFirstName());
        TextField lastNameField = new TextField(user.getLastName());
        TextField emailField = new TextField(user.getEmail());
        TextField usernameField = new TextField(user.getUsername());

        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Required to save changes");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Leave blank to keep password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");

        boolean hasOldPicture = oldPicturePath != null &&
                !oldPicturePath.isBlank();

        Label pictureLabel = new Label(
                hasOldPicture ? "Current profile picture is saved" :
                        "No profile picture selected"
        );
        pictureLabel.setWrapText(true);

        final File[] selectedPicture = {null};
        final boolean[] removePicture = {false};

        Button choosePictureButton = new Button("Choose Profile Picture");
        Button removePictureButton = new Button("Remove Profile Picture");
        removePictureButton.setDisable(!hasOldPicture);
        removePictureButton.setStyle(
                "-fx-background-color: #D9534F;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;"
        );

        choosePictureButton.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Profile Picture");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File file = chooser.showOpenDialog(
                    contentArea.getScene().getWindow()
            );
            if (file != null) {
                selectedPicture[0] = file;
                removePicture[0] = false;
                pictureLabel.setText("New picture: " + file.getName());
                removePictureButton.setDisable(false);
            }
        });

        removePictureButton.setOnAction(event -> {
            selectedPicture[0] = null;
            removePicture[0] = true;
            pictureLabel.setText("Profile picture will be removed when saved");
            removePictureButton.setDisable(true);
        });

        HBox pictureButtons = new HBox(
                10, choosePictureButton, removePictureButton
        );

        VBox form = new VBox(10);
        form.setPadding(new Insets(18));
        form.getChildren().addAll(
                new Label("First Name"), firstNameField,
                new Label("Last Name"), lastNameField,
                new Label("Email Address"), emailField,
                new Label("Username"), usernameField,
                new Label("Current Password (Required)"), currentPasswordField,
                new Label("New Password (Optional)"), newPasswordField,
                new Label("Confirm New Password"), confirmPasswordField,
                new Label("Profile Picture (Optional)"),
                pictureButtons, pictureLabel
        );

        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportWidth(460);
        scrollPane.setPrefViewportHeight(470);
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(510);

        ButtonType saveType = new ButtonType(
                "Save Changes", ButtonBar.ButtonData.OK_DONE
        );
        dialog.getDialogPane().getButtonTypes().addAll(
                saveType, ButtonType.CANCEL
        );

        Button saveButton = (Button)
                dialog.getDialogPane().lookupButton(saveType);
        final boolean[] saved = {false};

        saveButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {
                    Path copiedFile = null;
                    try {
                        String firstName = firstNameField.getText().trim();
                        String lastName = lastNameField.getText().trim();
                        String email = emailField.getText().trim();
                        String username = usernameField.getText().trim();
                        String currentPassword = currentPasswordField.getText();
                        String newPassword = newPasswordField.getText();
                        String confirmPassword = confirmPasswordField.getText();

                        if (firstName.isEmpty() || firstName.length() > 80 ||
                                lastName.isEmpty() || lastName.length() > 80) {
                            throw new IllegalArgumentException(
                                    "First/Last name must be 1-80 characters."
                            );
                        }
                        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") ||
                                email.length() > 150) {
                            throw new IllegalArgumentException(
                                    "Enter a valid email address."
                            );
                        }
                        if (!username.matches("[A-Za-z0-9._-]{3,50}")) {
                            throw new IllegalArgumentException(
                                    "Invalid username (3-50 characters)."
                            );
                        }
                        if (currentPassword.isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Current password is required."
                            );
                        }
                        if (!newPassword.isEmpty()) {
                            if (newPassword.isBlank() ||
                                    newPassword.length() < 8) {
                                throw new IllegalArgumentException(
                                        "New password must be at least 8 characters."
                                );
                            }
                            if (!newPassword.equals(confirmPassword)) {
                                throw new IllegalArgumentException(
                                        "New passwords do not match."
                                );
                            }
                        } else if (!confirmPassword.isEmpty()) {
                            throw new IllegalArgumentException(
                                    "Enter a new password first."
                            );
                        }

                        String newImagePath = null;
                        if (!removePicture[0] && selectedPicture[0] != null) {
                            File source = selectedPicture[0];
                            String name = source.getName().toLowerCase(Locale.ROOT);
                            boolean png = name.endsWith(".png");
                            boolean jpg = name.endsWith(".jpg") ||
                                    name.endsWith(".jpeg");
                            if (!png && !jpg) {
                                throw new IllegalArgumentException(
                                        "Only PNG and JPG images are allowed."
                                );
                            }
                            long size = Files.size(source.toPath());
                            if (size == 0 || size > 5L * 1024 * 1024) {
                                throw new IllegalArgumentException(
                                        "Image must be between 1 byte and 5 MB."
                                );
                            }
                            BufferedImage checkedImage = ImageIO.read(source);
                            if (checkedImage == null ||
                                    checkedImage.getWidth() <= 0 ||
                                    checkedImage.getHeight() <= 0 ||
                                    checkedImage.getWidth() > 4096 ||
                                    checkedImage.getHeight() > 4096) {
                                throw new IllegalArgumentException(
                                        "Invalid or oversized image."
                                );
                            }

                            Path folder = Paths.get(
                                    System.getProperty("user.dir"),
                                    "uploads", "profiles"
                            );
                            Files.createDirectories(folder);
                            String filename = UUID.randomUUID() +
                                    (png ? ".png" : ".jpg");
                            copiedFile = folder.resolve(filename);
                            Files.copy(source.toPath(), copiedFile);
                            newImagePath = "uploads/profiles/" + filename;
                        }

                        boolean updated = dao.updateProfile(
                                lecturer.getLecturerId(),
                                username,
                                firstName,
                                lastName,
                                email,
                                currentPassword,
                                newPassword,
                                newImagePath,
                                removePicture[0]
                        );

                        if (!updated) {
                            throw new IllegalArgumentException(
                                    "Incorrect current password or lecturer not found."
                            );
                        }
                        saved[0] = true;

                        // After successful DB update, delete a replaced/removed
                        // local picture ONLY if it is a UUID-named managed upload.
                        if ((removePicture[0] || newImagePath != null) &&
                                hasOldPicture) {
                            try {
                                Path root = Paths.get(
                                        System.getProperty("user.dir")
                                ).toAbsolutePath().normalize();
                                Path managedFolder = root.resolve(
                                        "uploads/profiles"
                                ).normalize();
                                Path oldFile = root.resolve(
                                        oldPicturePath
                                ).normalize();
                                String name = oldFile.getFileName().toString();
                                boolean isGeneratedUpload = name.matches(
                                        "(?i)[0-9a-f]{8}-[0-9a-f]{4}-" +
                                                "[0-9a-f]{4}-[0-9a-f]{4}-" +
                                                "[0-9a-f]{12}\\.(png|jpg|jpeg)"
                                );
                                if (oldFile.startsWith(managedFolder) &&
                                        oldFile.getParent().equals(managedFolder) &&
                                        isGeneratedUpload) {
                                    Files.deleteIfExists(oldFile);
                                }
                            } catch (IOException cleanupError) {
                                cleanupError.printStackTrace();
                                new Alert(Alert.AlertType.WARNING,
                                        "Profile updated, but old photo file could not be deleted."
                                ).showAndWait();
                            }
                        }

                    } catch (Exception e) {
                        // Only remove a newly copied file if saving failed.
                        if (copiedFile != null && !saved[0]) {
                            try {
                                Files.deleteIfExists(copiedFile);
                            } catch (IOException cleanupError) {
                                e.addSuppressed(cleanupError);
                            }
                        }
                        e.printStackTrace();
                        String message =
                                e instanceof java.sql.SQLIntegrityConstraintViolationException
                                        ? "Username or email is already in use."
                                        : e.getMessage();
                        new Alert(Alert.AlertType.ERROR,
                                "Unable to update profile: " + message
                        ).showAndWait();
                        event.consume();
                    }
                }
        );

        dialog.showAndWait();
        if (saved[0]) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Profile updated successfully!").showAndWait();
            showProfile();
        }
    }
}
