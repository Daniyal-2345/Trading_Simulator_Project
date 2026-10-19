package com.mycompany.tradingsim;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DashboardWindow {

    public static void display() {
        Stage dashboardStage = new Stage();
        dashboardStage.initModality(Modality.APPLICATION_MODAL);
        dashboardStage.setTitle("TradeSim - Account Dashboard & Analytics");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: #1e1e1e;");

        // Header Title
        Label titleLabel = new Label("📊 Trader Dashboard & Performance");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        // Summary Metric Cards Row
        HBox summaryCards = new HBox(12);
        summaryCards.setAlignment(Pos.CENTER);

        VBox card1 = createMetricCard("Account Status", "Active / Verified", "#2ecc71");
        VBox card2 = createMetricCard("Starting Balance", "$100,000.00", "#3498db");
        VBox card3 = createMetricCard("Ledger Integrity", "100% Secure", "#f39c12");

        summaryCards.getChildren().addAll(card1, card2, card3);

        // System & Trading Overview Box
        VBox statsBox = new VBox(10);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle(
            "-fx-background-color: #252526; " +
            "-fx-border-color: #333333; " +
            "-fx-border-radius: 6px; " +
            "-fx-background-radius: 6px;"
        );

        Label statsTitle = new Label("Platform & Security Metrics");
        statsTitle.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label stat1 = new Label("• Cryptographic Ledger: SHA-256 Chain Active");
        stat1.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");

        Label stat2 = new Label("• Supported Markets: EUR/USD, BTC/USD, ETH/USD");
        stat2.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");

        Label stat3 = new Label("• Security Validation: Strict Regex Rules Enforced");
        stat3.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");

        statsBox.getChildren().addAll(statsTitle, new Separator(), stat1, stat2, stat3);

        // Close Button
        Button closeBtn = new Button("Close Dashboard");
        closeBtn.setStyle(
            "-fx-background-color: #007acc; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13px; " +
            "-fx-padding: 8px 0; " +
            "-fx-background-radius: 4px; " +
            "-fx-cursor: hand;"
        );
        closeBtn.setMaxWidth(Double.MAX_VALUE);
        closeBtn.setOnAction(e -> dashboardStage.close());

        root.getChildren().addAll(titleLabel, summaryCards, statsBox, closeBtn);

        Scene scene = new Scene(root, 480, 420);
        dashboardStage.setScene(scene);
        dashboardStage.showAndWait();
    }

    private static VBox createMetricCard(String title, String value, String valueColor) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(145);
        card.setStyle(
            "-fx-background-color: #252526; " +
            "-fx-border-color: #333333; " +
            "-fx-border-radius: 6px; " +
            "-fx-background-radius: 6px;"
        );

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-text-fill: " + valueColor + "; -fx-font-size: 13px; -fx-font-weight: bold;");

        card.getChildren().addAll(titleLbl, valLbl);
        return card;
    }
}