package com.stocksense.view;

import com.stocksense.controller.AuthController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.Optional;

// This file builds the login screen and small auth dialogs.
// It is the first UI page the user sees before entering the main app.
public class LoginView {
    private final Stage stage;
    private final AuthController authController;

    public LoginView(Stage stage, AuthController authController) {
        this.stage = stage;
        this.authController = authController;
    }

    public void show(Runnable onLoginSuccess) {
        Label title = new Label("Stock Sense");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 30px; -fx-font-weight: 800;");
        Label tagline = new Label("Stay ahead of expiry. Stay in control.");
        tagline.setStyle("-fx-text-fill: rgba(255,255,255,0.88); -fx-font-size: 13px;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle(
                "-fx-background-color: rgba(255,255,255,0.18);"
                        + "-fx-background-radius: 10;"
                        + "-fx-text-fill: white;"
                        + "-fx-prompt-text-fill: rgba(255,255,255,0.7);"
                        + "-fx-pref-height: 40;"
        );

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle(
                "-fx-background-color: rgba(255,255,255,0.18);"
                        + "-fx-background-radius: 10;"
                        + "-fx-text-fill: white;"
                        + "-fx-prompt-text-fill: rgba(255,255,255,0.7);"
                        + "-fx-pref-height: 40;"
        );

        Label message = new Label();
        message.setStyle("-fx-text-fill: #ffd1d1; -fx-font-size: 12px;");
        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #0ea5e9, #2563eb);"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: 700;"
                        + "-fx-background-radius: 12;"
                        + "-fx-pref-height: 40;"
        );
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #38bdf8, #3b82f6);"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: 700;"
                        + "-fx-background-radius: 12;"
                        + "-fx-pref-height: 40;"
        ));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #0ea5e9, #2563eb);"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: 700;"
                        + "-fx-background-radius: 12;"
                        + "-fx-pref-height: 40;"
        ));

        loginButton.setOnAction(e -> {
            try {
                // Trying login and moving to dashboard only if credentials match.
                boolean valid = authController.login(usernameField.getText().trim(), passwordField.getText());
                if (valid) {
                    onLoginSuccess.run();
                } else {
                    message.setText("Invalid username or password.");
                }
            } catch (IllegalArgumentException ex) {
                message.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                message.setText("Login failed. Try again.");
            }
        });

        Label newUserLabel = new Label("New user?");
        newUserLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.82);");
        Button signUpButton = new Button("Sign Up");
        signUpButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #7dd3fc; -fx-font-weight: 700;");
        signUpButton.setOnAction(e -> openSignUpDialog(message));

        Button forgotPasswordButton = new Button("Forgot Password");
        forgotPasswordButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #7dd3fc; -fx-font-weight: 700;");
        forgotPasswordButton.setOnAction(e -> openForgotPasswordDialog(message));

        HBox newUserRow = new HBox(8, newUserLabel, signUpButton);
        newUserRow.setAlignment(Pos.CENTER);

        VBox card = new VBox(14, title, tagline, usernameField, passwordField, loginButton, newUserRow, forgotPasswordButton, message);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(300);
        card.setPadding(new Insets(26));
        card.setStyle(
                "-fx-background-color: rgba(15,23,42,0.55);"
                        + "-fx-background-radius: 18;"
                        + "-fx-border-color: rgba(255,255,255,0.16);"
                        + "-fx-border-radius: 18;"
                        + "-fx-border-width: 1;"
        );
        card.setEffect(new DropShadow(28, Color.rgb(0, 0, 0, 0.40)));

        Image bgImage = new Image(getClass().getResourceAsStream("/images/login-bg.png"));
        ImageView bgView = new ImageView(bgImage);
        bgView.setPreserveRatio(false);
        bgView.fitWidthProperty().bind(stage.widthProperty());
        bgView.fitHeightProperty().bind(stage.heightProperty());

        Rectangle darkOverlay = new Rectangle();
        darkOverlay.widthProperty().bind(stage.widthProperty());
        darkOverlay.heightProperty().bind(stage.heightProperty());
        darkOverlay.setFill(Color.rgb(3, 7, 18, 0.58));

        StackPane root = new StackPane(bgView, darkOverlay, card);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 1100, 700);

        stage.setTitle("Stock Sense - Inventory Expiry Tracker");
        stage.setScene(scene);
        stage.show();
    }

    private void openSignUpDialog(Label messageLabel) {
        Alert dialog = new Alert(Alert.AlertType.NONE);
        dialog.setTitle("Create New User");

        TextField nameField = new TextField();
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        PasswordField repeatPasswordField = new PasswordField();
        DatePicker dobField = new DatePicker();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Name"), nameField);
        form.addRow(1, new Label("Username"), usernameField);
        form.addRow(2, new Label("Password"), passwordField);
        form.addRow(3, new Label("Repeat Password"), repeatPasswordField);
        form.addRow(4, new Label("Date of Birth"), dobField);
        form.setPadding(new Insets(10));

        ButtonType save = new ButtonType("Register", ButtonType.OK.getButtonData());
        ButtonType cancel = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());
        dialog.getButtonTypes().setAll(save, cancel);
        dialog.getDialogPane().setContent(form);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get().getButtonData().isDefaultButton()) {
            try {
                // Sending all signup input to controller for validation and saving.
                authController.registerUser(
                        nameField.getText().trim(),
                        usernameField.getText().trim(),
                        passwordField.getText(),
                        repeatPasswordField.getText(),
                        dobField.getValue()
                );
                messageLabel.setText("Registration successful. Please login.");
            } catch (IllegalArgumentException ex) {
                messageLabel.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                messageLabel.setText("Registration failed.");
            }
        }
    }

    private void openForgotPasswordDialog(Label messageLabel) {
        Alert dialog = new Alert(Alert.AlertType.NONE);
        dialog.setTitle("Forgot Password");

        TextField usernameField = new TextField();
        DatePicker dobField = new DatePicker();
        PasswordField newPasswordField = new PasswordField();
        PasswordField repeatPasswordField = new PasswordField();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Username"), usernameField);
        form.addRow(1, new Label("Date of Birth"), dobField);
        form.addRow(2, new Label("New Password"), newPasswordField);
        form.addRow(3, new Label("Repeat Password"), repeatPasswordField);
        form.setPadding(new Insets(10));

        ButtonType save = new ButtonType("Update Password", ButtonType.OK.getButtonData());
        ButtonType cancel = new ButtonType("Cancel", ButtonType.CANCEL.getButtonData());
        dialog.getButtonTypes().setAll(save, cancel);
        dialog.getDialogPane().setContent(form);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get().getButtonData().isDefaultButton()) {
            try {
                // Reset works only when username and DOB match the saved account.
                authController.resetPassword(
                        usernameField.getText().trim(),
                        dobField.getValue(),
                        newPasswordField.getText(),
                        repeatPasswordField.getText()
                );
                messageLabel.setText("Password updated. Please login.");
            } catch (IllegalArgumentException ex) {
                messageLabel.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                messageLabel.setText("Password reset failed.");
            }
        }
    }
}
