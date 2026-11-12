package com.mycompany.tradingsim;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AuthDialog {

    public static void display(Runnable onLoginSuccess) {
        Stage authStage = new Stage();
        authStage.initModality(Modality.APPLICATION_MODAL);
        authStage.setTitle("TradeSim - Authentication Gateway");
        authStage.setMinWidth(440);
        authStage.setMinHeight(560);

        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #1e1e1e;");
        root.setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label("🔐 Welcome to TradeSim Terminal");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #2d2d2d;");

        // ==========================================
        // 1. LOGIN TAB SETUP
        // ==========================================
        Tab loginTab = new Tab("Login");
        loginTab.setClosable(false);
        VBox loginBox = new VBox(10);
        loginBox.setPadding(new Insets(20));
        loginBox.setStyle("-fx-background-color: #2d2d2d;");

        Label loginUserLabel = new Label("Username:");
        loginUserLabel.setStyle("-fx-text-fill: #ccc; -fx-font-weight: bold;");
        TextField loginUserField = new TextField();
        loginUserField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white; -fx-prompt-text-fill: #777;");
        loginUserField.setPromptText("Enter your account username");
        
        Label loginUserRule = new Label("• Must match an existing registered account username.");
        loginUserRule.setStyle("-fx-text-fill: #777; -fx-font-size: 10px;");

        Label loginPassLabel = new Label("Password:");
        loginPassLabel.setStyle("-fx-text-fill: #ccc; -fx-font-weight: bold;");
        PasswordField loginPassField = new PasswordField();
        loginPassField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white; -fx-prompt-text-fill: #777;");
        loginPassField.setPromptText("Enter your security password");
        
        Label loginPassRule = new Label("• Case-sensitive password verification.");
        loginPassRule.setStyle("-fx-text-fill: #777; -fx-font-size: 10px;");

        Button loginSubmitBtn = new Button("Login to Terminal");
        loginSubmitBtn.setStyle("-fx-background-color: #007acc; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        loginSubmitBtn.setMaxWidth(Double.MAX_VALUE);

        loginSubmitBtn.setOnAction(e -> {
            String username = loginUserField.getText().trim();
            String password = loginPassField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Error", "Please fill in all login fields.");
                return;
            }

            // FIXED: Calls correct DatabaseManager.loginUser method
            boolean authenticated = DatabaseManager.loginUser(username, password);
            if (authenticated) {
                UserSession.setLoggedInUser(username);
                if (onLoginSuccess != null) {
                    onLoginSuccess.run();
                }
                authStage.close();
            } else {
                showAlert("Login Failed", "Invalid username or password.");
            }
        });

        loginBox.getChildren().addAll(
            loginUserLabel, loginUserField, loginUserRule,
            new Separator(),
            loginPassLabel, loginPassField, loginPassRule,
            new Region(), loginSubmitBtn
        );
        loginTab.setContent(loginBox);

        // ==========================================
        // 2. REGISTER TAB SETUP (Using RegistrationValidator)
        // ==========================================
        Tab registerTab = new Tab("Register");
        registerTab.setClosable(false);
        VBox registerBox = new VBox(10);
        registerBox.setPadding(new Insets(20));
        registerBox.setStyle("-fx-background-color: #2d2d2d;");

        Label regNameLabel = new Label("Full Name:");
        regNameLabel.setStyle("-fx-text-fill: #ccc; -fx-font-weight: bold;");
        TextField regNameField = new TextField();
        regNameField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white; -fx-prompt-text-fill: #777;");
        regNameField.setPromptText("e.g., John Doe");
        
        Label regNameRule = new Label("• Only letters and spaces allowed (no numbers).");
        regNameRule.setStyle("-fx-text-fill: #777; -fx-font-size: 10px;");

        Label regUserLabel = new Label("Choose Username:");
        regUserLabel.setStyle("-fx-text-fill: #ccc; -fx-font-weight: bold;");
        TextField regUserField = new TextField();
        regUserField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white; -fx-prompt-text-fill: #777;");
        regUserField.setPromptText("Min 3 characters");
        
        Label regUserRule = new Label("• Must be unique and at least 3 characters long.");
        regUserRule.setStyle("-fx-text-fill: #777; -fx-font-size: 10px;");

        Label regPassLabel = new Label("Choose Password:");
        regPassLabel.setStyle("-fx-text-fill: #ccc; -fx-font-weight: bold;");
        PasswordField regPassField = new PasswordField();
        regPassField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white; -fx-prompt-text-fill: #777;");
        regPassField.setPromptText("Min 8 chars, uppercase, lowercase, number, symbol");
        
        Label regPassRule = new Label("• Min 8 chars, 1 uppercase, 1 lowercase, 1 number, 1 special char.");
        regPassRule.setStyle("-fx-text-fill: #777; -fx-font-size: 10px;");

        // Optional Admin Code field
        Label adminCodeLabel = new Label("Admin Code (Optional):");
        adminCodeLabel.setStyle("-fx-text-fill: #ccc; -fx-font-weight: bold;");
        PasswordField adminCodeField = new PasswordField();
        adminCodeField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white; -fx-prompt-text-fill: #777;");
        adminCodeField.setPromptText("Enter secret code if admin");

        Button regSubmitBtn = new Button("Create Secure Account");
        regSubmitBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        regSubmitBtn.setMaxWidth(Double.MAX_VALUE);

        regSubmitBtn.setOnAction(e -> {
            String fullName = regNameField.getText().trim();
            String username = regUserField.getText().trim();
            String password = regPassField.getText();
            String adminCode = adminCodeField.getText();

            // Delegate validation to RegistrationValidator
            if (!RegistrationValidator.validateAndRegister(fullName, username, password)) {
                return; 
            }

            // FIXED: Passes all 4 parameters to match DatabaseManager.registerUser(...)
            boolean registered = DatabaseManager.registerUser(fullName, username, password, adminCode);
            if (registered) {
                showAlert("Success", "Account created successfully! You can now log in.");
                tabPane.getSelectionModel().select(loginTab); 
            } else {
                showAlert("Registration Failed", "Username may already be taken in the database.");
            }
        });

        registerBox.getChildren().addAll(
            regNameLabel, regNameField, regNameRule,
            regUserLabel, regUserField, regUserRule,
            regPassLabel, regPassField, regPassRule,
            adminCodeLabel, adminCodeField,
            new Region(), regSubmitBtn
        );
        registerTab.setContent(registerBox);

        tabPane.getTabs().addAll(loginTab, registerTab);
        root.getChildren().addAll(titleLabel, tabPane);

        Scene scene = new Scene(root);
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