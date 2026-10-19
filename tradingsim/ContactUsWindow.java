package com.mycompany.tradingsim;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ContactUsWindow {

    public static void display() {
        Stage contactStage = new Stage();
        contactStage.initModality(Modality.APPLICATION_MODAL);
        contactStage.setTitle("TradeSim - Contact Support");

        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white;");

        Label titleLabel = new Label("Contact TradeSim Support");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        TextField nameField = new TextField();
        nameField.setPromptText("Your Full Name");
        nameField.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-prompt-text-fill: #888888;");

        TextField emailField = new TextField();
        emailField.setPromptText("Your Email Address");
        emailField.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-prompt-text-fill: #888888;");

        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Describe your issue or inquiry here...;");
        messageArea.setPrefRowCount(5); // Fixed method name for JavaFX TextArea
        messageArea.setStyle("-fx-control-inner-background: #2d2d2d; -fx-text-fill: yellow;");

        Button submitButton = new Button("Send Message");
        submitButton.setStyle("-fx-background-color: #007acc; -fx-text-fill: white; -fx-font-weight: bold;");
        
        submitButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String msg = messageArea.getText().trim();

            if (name.isEmpty() || email.isEmpty() || msg.isEmpty()) {
                showAlert("Error", "All fields must be filled out!");
            } else {
                showAlert("Success", "Thank you, " + name + "! Your message has been sent to support.");
                contactStage.close();
            }
        });

        root.getChildren().addAll(titleLabel, nameField, emailField, messageArea, submitButton);

        Scene scene = new Scene(root, 400, 450);
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