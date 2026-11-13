package com.mycompany.tradingsim;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ContactUsWindow {

    public static void display() {
        Stage contactStage = new Stage();
        contactStage.initModality(Modality.APPLICATION_MODAL);
        contactStage.setTitle("TradeSim - Contact Support");
        contactStage.setMinWidth(440);
        contactStage.setMinHeight(500);

        VBox root = new VBox(12);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: #1e1e1e;");

        // Header Title
        Label titleLabel = new Label("🎧 Contact TradeSim Support");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitleLabel = new Label("Submit a support ticket and our team will get back to you.");
        subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #aaa;");

        // Full Name Field
        Label nameLabel = new Label("Full Name");
        nameLabel.setStyle("-fx-text-fill: #ccc; -fx-font-weight: bold; -fx-font-size: 12px;");
        TextField nameField = new TextField();
        nameField.setPromptText("e.g., John Doe");
        nameField.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-prompt-text-fill: #777; -fx-background-radius: 4; -fx-border-color: #333; -fx-border-radius: 4;");

        // Email Field
        Label emailLabel = new Label("Email Address");
        emailLabel.setStyle("-fx-text-fill: #ccc; -fx-font-weight: bold; -fx-font-size: 12px;");
        TextField emailField = new TextField();
        emailField.setPromptText("e.g., john@example.com");
        emailField.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-prompt-text-fill: #777; -fx-background-radius: 4; -fx-border-color: #333; -fx-border-radius: 4;");

        // Message Area
        Label messageLabel = new Label("Message / Inquiry");
messageLabel.setStyle("-fx-text-fill: #ccc; -fx-font-weight: bold; -fx-font-size: 12px;");

TextArea messageArea = new TextArea();
messageArea.setPromptText("Describe your issue or inquiry here...");
messageArea.setPrefRowCount(4);
// Enhanced styling to ensure both the inner background and outer container match your dark theme perfectly
messageArea.setStyle(
    "-fx-control-inner-background: #2d2d2d; " +
    "-fx-background-color: #2d2d2d; " +
    "-fx-text-fill: white; " +
    "-fx-prompt-text-fill: #777; " +
    "-fx-border-color: #333; " +
    "-fx-border-radius: 4; " +
    "-fx-background-radius: 4;"
);

        // Submit Button
        Button submitButton = new Button("Send Support Ticket");
        submitButton.setStyle("-fx-background-color: #007acc; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;");
        submitButton.setMaxWidth(Double.MAX_VALUE);
        submitButton.setPrefHeight(35);
        
        submitButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String msg = messageArea.getText().trim();

            if (name.isEmpty() || email.isEmpty() || msg.isEmpty()) {
                showAlert("Error", "All fields must be filled out!");
            } else {
                // Persist support ticket to MySQL database
                boolean saved = DatabaseManager.logSupportMessage(name, email, msg);
                if (saved) {
                    showAlert("Success", "Thank you, " + name + "! Your support message has been sent successfully.");
                    contactStage.close();
                } else {
                    showAlert("Database Error", "Failed to save message. Please check your database connection.");
                }
            }
        });

        root.getChildren().addAll(
            titleLabel, 
            subtitleLabel, 
            new Separator(), 
            nameLabel, 
            nameField, 
            emailLabel, 
            emailField, 
            messageLabel, 
            messageArea, 
            new Region(), 
            submitButton
        );

        Scene scene = new Scene(root);
        contactStage.setScene(scene);
        contactStage.showAndWait();
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}