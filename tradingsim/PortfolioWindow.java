package com.mycompany.tradingsim;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Map;

public class PortfolioWindow {

    public static class HoldingRecord {
        private final String asset;
        private final double quantity;
        private final double avgPrice;

        public HoldingRecord(String asset, double quantity, double avgPrice) {
            this.asset = asset;
            this.quantity = quantity;
            this.avgPrice = avgPrice;
        }

        public String getAsset() { return asset; }
        public double getQuantity() { return quantity; }
        public double getAvgPrice() { return avgPrice; }
    }

    public static void display() {
        Stage portfolioStage = new Stage();
        portfolioStage.initModality(Modality.APPLICATION_MODAL);
        portfolioStage.setTitle("TradeSim - Live User Portfolio");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1e1e1e;");

        Label titleLabel = new Label("📊 Live Portfolio & Balances");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        // Live Summary Cards
        HBox summaryCards = new HBox(12);
        summaryCards.setAlignment(Pos.CENTER);

        VBox cardCash = createMetricCard("Cash Balance", String.format("$%,.2f", AccountManager.getCashBalance()), "#2ecc71");
        VBox cardInvested = createMetricCard("Holdings Value", String.format("$%,.2f", AccountManager.getTotalHoldingsValue()), "#3498db");
        VBox cardNetWorth = createMetricCard("Total Net Worth", String.format("$%,.2f", AccountManager.getTotalNetWorth()), "#ffffff");

        summaryCards.getChildren().addAll(cardCash, cardInvested, cardNetWorth);

        Label tableTitle = new Label("Active Positions");
        tableTitle.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14px; -fx-font-weight: bold;");

        TableView<HoldingRecord> holdingsTable = new TableView<>();
        holdingsTable.setPrefHeight(220);
        holdingsTable.setStyle(
            "-fx-background-color: #252526; " +
            "-fx-control-inner-background: #252526; " +
            "-fx-table-cell-border-color: #333333; " +
            "-fx-background-radius: 6px;"
        );

        TableColumn<HoldingRecord, String> colAsset = new TableColumn<>("Asset");
        colAsset.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAsset()));
        colAsset.setPrefWidth(125);

        TableColumn<HoldingRecord, String> colQty = new TableColumn<>("Quantity Held");
        colQty.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getQuantity())));
        colQty.setPrefWidth(125);

        TableColumn<HoldingRecord, String> colPrice = new TableColumn<>("Avg Buy Price");
        colPrice.setCellValueFactory(cell -> new SimpleStringProperty(String.format("$%,.2f", cell.getValue().getAvgPrice())));
        colPrice.setPrefWidth(130);

        holdingsTable.getColumns().addAll(colAsset, colQty, colPrice);

        // Populate from live AccountManager holdings (only show assets with qty > 0)
        ObservableList<HoldingRecord> liveHoldings = FXCollections.observableArrayList();
        Map<String, Double> quantities = AccountManager.getAssetQuantities();
        Map<String, Double> avgPrices = AccountManager.getAssetAvgPrices();

        for (Map.Entry<String, Double> entry : quantities.entrySet()) {
            if (entry.getValue() > 0.0) {
                liveHoldings.add(new HoldingRecord(entry.getKey(), entry.getValue(), avgPrices.getOrDefault(entry.getKey(), 0.0)));
            }
        }
        holdingsTable.setItems(liveHoldings);

        Button closeBtn = new Button("Close Portfolio");
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
        closeBtn.setOnAction(e -> portfolioStage.close());

        root.getChildren().addAll(titleLabel, summaryCards, tableTitle, holdingsTable, closeBtn);

        Scene scene = new Scene(root, 440, 480);
        portfolioStage.setScene(scene);
        portfolioStage.showAndWait();
    }

    private static VBox createMetricCard(String title, String value, String valueColor) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(135);
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