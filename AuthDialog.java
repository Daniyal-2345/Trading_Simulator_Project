package com.mycompany.tradingsim;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AuthDialog {

    // Displays the Login / Register Dialog Window
    public static void display(Runnable onLoginSuccess) {
        Stage authStage = new Stage();
        authStage.initModality(Modality.APPLICATION_MODAL);
        authStage.setTitle("TradeSim - Authentication");

        TabPane tabPane = new TabPane();

        // --- REGISTER TAB ---
        Tab registerTab = new Tab("Register");
        registerTab.setClosable(false);
        
        VBox regLayout = new VBox(12);
        regLayout.setPadding(new Insets(20));
        regLayout.setAlignment(Pos.CENTER);
        regLayout.setStyle("-fx-background-color: #1e1e1e;");

        TextField regFullName = new TextField();
        regFullName.setPromptText("Full Name (Letters only)");
        regFullName.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-prompt-text-fill: #888;");

        TextField regUsername = new TextField();
        regUsername.setPromptText("Username");
        regUsername.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-prompt-text-fill: #888;");

        PasswordField regPassword = new PasswordField();
        regPassword.setPromptText("Password (8+ chars, Uppercase, Number, Special)");
        regPassword.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-prompt-text-fill: #888;");
        
        PasswordField adminCodeField = new PasswordField();
        adminCodeField.setPromptText("Admin Code (Leave blank if normal user)");
        adminCodeField.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-prompt-text-fill: #888888;");

        Button regButton = new Button("Create Account");
        regButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        regButton.setOnAction(e -> {
            String name = regFullName.getText().trim();
            String user = regUsername.getText().trim();
            String pass = regPassword.getText();
            String adminCode = adminCodeField.getText().trim();

            // 1. Validate fields using your regex validator
            if (RegistrationValidator.validateAndRegister(name, user, pass)) {
                
                // 2. SAVE TO MYSQL DATABASE VIA DatabaseManager
                boolean success = DatabaseManager.registerUser(name, user, pass, adminCode);
                
                if (success) {
                    showAlert("Success", "Account created successfully! You can now log in with $100,000 balance.");
                    // Clear text fields after successful registration
                    regFullName.clear();
                    regUsername.clear();
                    regPassword.clear();
                    adminCodeField.clear();
                } else {
                    showAlert("Registration Failed", "Username may already exist in the database.");
                }
            }
        });

        // ADDED adminCodeField into the layout children list so it renders on screen
        regLayout.getChildren().addAll(new Label("Register New Account") {{
            setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        }}, regFullName, regUsername, regPassword, adminCodeField, regButton);
        registerTab.setContent(regLayout);

        // --- LOGIN TAB ---
        Tab loginTab = new Tab("Login");
        loginTab.setClosable(false);

        VBox loginLayout = new VBox(12);
        loginLayout.setPadding(new Insets(20));
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setStyle("-fx-background-color: #1e1e1e;");

        TextField loginUsername = new TextField();
        loginUsername.setPromptText("Username");
        loginUsername.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-prompt-text-fill: #888;");

        PasswordField loginPassword = new PasswordField();
        loginPassword.setPromptText("Password");
        loginPassword.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-prompt-text-fill: #888;");

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #007acc; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        loginButton.setOnAction(e -> {
            String user = loginUsername.getText().trim();
            String pass = loginPassword.getText();

            if (user.isEmpty() || pass.isEmpty()) {
                showAlert("Error", "Please fill in all fields.");
            } else {
                // Verify against real MySQL database
                boolean authenticated = DatabaseManager.loginUser(user, pass);
                if (authenticated) {
                    // Set active session
                    UserSession.setLoggedInUser(user);
                    
                    showAlert("Success", "Welcome back, " + user + "! You are now logged in.");
                    authStage.close();
                    if (onLoginSuccess != null) {
                        onLoginSuccess.run();
                    }
                } else {
                    showAlert("Login Failed", "Invalid username or password.");
                }
            }
        });

        loginLayout.getChildren().addAll(new Label("Login to TradeSim") {{
            setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        }}, loginUsername, loginPassword, loginButton);
        loginTab.setContent(loginLayout);

        tabPane.getTabs().addAll(loginTab, registerTab);

        // Slightly increased height to comfortably fit the new admin code field
        Scene scene = new Scene(tabPane, 380, 380);
        authStage.setScene(scene);
        authStage.showAndWait();
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}