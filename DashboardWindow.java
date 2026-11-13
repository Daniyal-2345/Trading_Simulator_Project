package com.mycompany.tradingsim;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DashboardWindow {

    public static void display() {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("TradeSim - Executive User Dashboard");
        window.setMinWidth(750);
        window.setMinHeight(580);

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #1e1e1e;");

        // 1. Header Section
        String currentUser = UserSession.getLoggedInUser();
        Label titleLabel = new Label("📊 Account Executive Dashboard");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label userSubLabel = new Label("Authenticated User: " + (currentUser != null ? currentUser : "Guest"));
        userSubLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 14px; -fx-font-weight: bold;");

        // 2. Financial Metrics Cards (Expanded & Fixed Accent Color Hex Codes)
        HBox metricsBox = new HBox(15);
        metricsBox.setAlignment(Pos.CENTER);

        double cashBalance = AccountManager.getCashBalance();
        VBox cashCard = createMetricCard("Available Cash", String.format("$%,.2f", cashBalance), "#2ecc71");
        VBox portfolioCard = createMetricCard("Margin Level", "100%", "#007acc");
        VBox statusCard = createMetricCard("Session Status", "Secured & Live", "#2ecc71");
        VBox riskCard = createMetricCard("Risk Parameter", "Standard (1:1)", "#f1c40f");

        metricsBox.getChildren().addAll(cashCard, portfolioCard, statusCard, riskCard);

        // 3. Quick Action Navigation Bar
        HBox actionBox = new HBox(12);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        Button btnPortfolio = new Button("💼 View Portfolio Holdings");
        btnPortfolio.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
        btnPortfolio.setOnAction(e -> PortfolioWindow.display());

        Button btnSupport = new Button("🎧 Contact Support");
        btnSupport.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
        btnSupport.setOnAction(e -> ContactUsWindow.display());

        actionBox.getChildren().addAll(btnPortfolio, btnSupport);

        // 4. System Audit & Security Notes
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #333;");

        VBox infoBox = new VBox(10);
        Label infoTitle = new Label("🔒 System Audit & Blockchain Protocol Security");
        infoTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label infoBody = new Label(
            "• All order executions are cryptographically signed and immutable via SHA-256 blocks.\n" +
            "• Cash balance updates dynamically against live institutional Binance price feeds.\n" +
            "• Database persistence guarantees cross-session trade history integrity."
        );
        infoBody.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px; -fx-line-spacing: 4px;");
        infoBox.getChildren().addAll(infoTitle, infoBody);

        root.getChildren().addAll(titleLabel, userSubLabel, metricsBox, actionBox, separator, infoBox);

        Scene scene = new Scene(root);
        window.setScene(scene);
        window.showAndWait();
    }

    private static VBox createMetricCard(String title, String value, String accentColor) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setPrefWidth(160);
        card.setStyle(String.format(
            "-fx-background-color: #2d2d2d; -fx-border-color: %s; -fx-border-width: 0 0 0 4px; -fx-background-radius: 4px;",
            accentColor
        ));

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");

        Label valLbl = new Label(value);
        valLbl.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");

        card.getChildren().addAll(titleLbl, valLbl);
        return card;
    }
}