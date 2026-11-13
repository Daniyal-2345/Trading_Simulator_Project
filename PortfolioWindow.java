package com.mycompany.tradingsim;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PortfolioWindow {

    public static void display() {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("TradeSim - Live Asset Portfolio & Holdings");
        window.setMinWidth(750);
        window.setMinHeight(520);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1e1e1e;");

        String currentUser = UserSession.getLoggedInUser();
        Label titleLabel = new Label("💼 Active Portfolio for: " + (currentUser != null ? currentUser : "Guest"));
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Summary metrics card bar
        double cash = AccountManager.getCashBalance();
        Label cashLabel = new Label(String.format("Available Cash Balance: $%,.2f USD", cash));
        cashLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #252526; -fx-padding: 10; -fx-background-radius: 4;");

        // Holdings Table
        TableView<HoldingItem> table = new TableView<>();
        table.setPrefHeight(340);
        table.setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #333;");

        TableColumn<HoldingItem, String> colAsset = new TableColumn<>("Asset");
        colAsset.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAsset()));
        colAsset.setPrefWidth(180);

        TableColumn<HoldingItem, String> colQty = new TableColumn<>("Total Quantity Owned");
        colQty.setCellValueFactory(cell -> new SimpleStringProperty(String.format("%.4f", cell.getValue().getQuantity())));
        colQty.setPrefWidth(220);

        TableColumn<HoldingItem, String> colVal = new TableColumn<>("Current Market Value");
        colVal.setCellValueFactory(cell -> new SimpleStringProperty(String.format("$%,.2f", cell.getValue().getTotalValue())));
        colVal.setPrefWidth(250);

        table.getColumns().addAll(colAsset, colQty, colVal);

        // Load holdings from MySQL database trades for current user
        ObservableList<HoldingItem> holdings = loadUserHoldingsFromDatabase(currentUser);
        table.setItems(holdings);

        root.getChildren().addAll(titleLabel, cashLabel, table);

        Scene scene = new Scene(root);
        window.setScene(scene);
        window.showAndWait();
    }

    private static ObservableList<HoldingItem> loadUserHoldingsFromDatabase(String username) {
        ObservableList<HoldingItem> list = FXCollections.observableArrayList();
        double btcQty = 0;
        double ethQty = 0;
        double eurQty = 0;

        String query = "SELECT action, asset, quantity FROM trades WHERE username = ?";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String action = rs.getString("action");
                String asset = rs.getString("asset").toUpperCase();
                double qty = rs.getDouble("quantity");

                double multiplier = action.equalsIgnoreCase("BUY") ? 1.0 : -1.0;
                if (asset.contains("BTC")) {
                    btcQty += (qty * multiplier);
                } else if (asset.contains("ETH")) {
                    ethQty += (qty * multiplier);
                } else if (asset.contains("EUR")) {
                    eurQty += (qty * multiplier);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading portfolio holdings: " + e.getMessage());
        }

        // Live reference prices matching TradingSim feed defaults
        double btcPrice = 81057.0; 
        double ethPrice = 3540.0;
        double eurPrice = 1.0845;

        list.add(new HoldingItem("BTC/USD", Math.max(0, btcQty), Math.max(0, btcQty) * btcPrice));
        list.add(new HoldingItem("ETH/USD", Math.max(0, ethQty), Math.max(0, ethQty) * ethPrice));
        list.add(new HoldingItem("EUR/USD", Math.max(0, eurQty), Math.max(0, eurQty) * eurPrice));

        return list;
    }

    public static class HoldingItem {
        private String asset;
        private double quantity;
        private double totalValue;

        public HoldingItem(String asset, double quantity, double totalValue) {
            this.asset = asset;
            this.quantity = quantity;
            this.totalValue = totalValue;
        }

        public String getAsset() { return asset; }
        public double getQuantity() { return quantity; }
        public double getTotalValue() { return totalValue; }
    }
}