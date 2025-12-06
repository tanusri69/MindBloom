package com.mindbloom.ui;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.mindbloom.service.UserService;


public class LoginScreen {
    private UserService userService;
    private String currentUser;

    public LoginScreen(UserService userService) {
        this.userService = userService;
    }

    
    public String showLoginDialog(Stage primaryStage) {
        VBox root = new VBox(12);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1;");

        Label title = new Label("🌱 MindBloom Login");
        title.setStyle("-fx-font-size:20px; -fx-font-weight:bold;");

        TextField userField = new TextField();
        userField.setPromptText("Username");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");

        Label msgLabel = new Label();

        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register");
        javafx.scene.layout.HBox btnBox = new javafx.scene.layout.HBox(8);
        btnBox.getChildren().addAll(loginBtn, registerBtn);

        root.getChildren().addAll(
                title,
                new Label("Username:"), userField,
                new Label("Password:"), passField,
                msgLabel,
                btnBox
        );

        Stage loginStage = new Stage();
        loginStage.setTitle("MindBloom - Login");
        loginStage.setScene(new Scene(root, 400, 300));
        loginStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        javafx.application.Platform.runLater(() -> {
            loginBtn.setOnAction(evt -> {
                String user = userField.getText().trim();
                String pass = passField.getText();
                if (user.isEmpty() || pass.isEmpty()) {
                    msgLabel.setText("❌ Please enter username and password.");
                    msgLabel.setStyle("-fx-text-fill: red;");
                    return;
                }
                if (userService.authenticate(user, pass)) {
                    currentUser = user;
                    msgLabel.setText("✅ Login successful!");
                    msgLabel.setStyle("-fx-text-fill: green;");
                    loginStage.close();
                } else {
                    msgLabel.setText("❌ Invalid credentials.");
                    msgLabel.setStyle("-fx-text-fill: red;");
                }
            });

            registerBtn.setOnAction(evt -> {
                String user = userField.getText().trim();
                String pass = passField.getText();
                if (user.isEmpty() || pass.isEmpty()) {
                    msgLabel.setText("❌ Please enter username and password.");
                    msgLabel.setStyle("-fx-text-fill: red;");
                    return;
                }
                if (userService.register(user, pass)) {
                    msgLabel.setText("✅ Registration successful! You can now login.");
                    msgLabel.setStyle("-fx-text-fill: green;");
                    userField.clear();
                    passField.clear();
                } else {
                    msgLabel.setText("❌ User already exists or invalid input.");
                    msgLabel.setStyle("-fx-text-fill: red;");
                }
            });
        });

        loginStage.showAndWait();
        return currentUser;
    }
}
