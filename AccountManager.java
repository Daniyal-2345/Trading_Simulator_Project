package com.mycompany.tradingsim;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class AccountManager {
    
    // Tracking quantities and average prices for assets
    private static final Map<String, Double> assetQuantities = new HashMap<>();
    private static final Map<String, Double> assetAvgPrices = new HashMap<>();

    // Current live market prices (dynamically updated by Binance feed)
    private static final Map<String, Double> marketPrices = new HashMap<>(Map.of(
        "EUR/USD", 1.0845,
        "BTC/USD", 81057.0,
        "ETH/USD", 3540.0
    ));

    public static void updateMarketPrice(String asset, double price) {
        marketPrices.put(asset, price);
    }

    // Fetch the active user's balance directly from MySQL database
    public static double getCashBalance() {
        String currentUser = UserSession.getLoggedInUser();
        if (currentUser == null || currentUser.isEmpty() || currentUser.equals("Guest")) {
            return 100000.0;
        }
        return DatabaseManager.getUserBalance(currentUser);
    }

    // Update balance in MySQL database for the active user
    public static void setCashBalance(double balance) {
        String currentUser = UserSession.getLoggedInUser();
        if (currentUser != null && !currentUser.isEmpty() && !currentUser.equals("Guest")) {
            DatabaseManager.updateUserBalance(currentUser, balance);
        }
    }

    public static Map<String, Double> getAssetQuantities() {
        return assetQuantities;
    }

    public static Map<String, Double> getAssetAvgPrices() {
        return assetAvgPrices;
    }

    // Re-sync asset holdings from MySQL trades for the active user upon login
    public static void reloadHoldingsFromDatabase(String username) {
        assetQuantities.clear();
        assetAvgPrices.clear();
        if (username == null || username.equals("Guest")) return;

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
                double existing = assetQuantities.getOrDefault(asset, 0.0);
                double updatedQty = Math.max(0, existing + (qty * multiplier));
                
                assetQuantities.put(asset, updatedQty);
            }
            System.out.println("Successfully reloaded asset holdings for user: " + username);
        } catch (Exception e) {
            System.err.println("Error reloading holdings from database: " + e.getMessage());
        }
    }

    // Process a trade, update balance in MySQL, and persist
    public static boolean processTrade(String action, String asset, double qty) {
        double currentPrice = marketPrices.getOrDefault(asset, 100.0);
        double totalCost = qty * currentPrice;
        double currentBalance = getCashBalance();
        String normalizedAsset = asset.toUpperCase();

        if (action.equalsIgnoreCase("BUY")) {
            if (currentBalance < totalCost) {
                System.err.println("Trade Denied: Insufficient cash balance.");
                return false; // Insufficient funds
            }
            double newBalance = currentBalance - totalCost;
            setCashBalance(newBalance); // Saves to MySQL

            double existingQty = assetQuantities.getOrDefault(normalizedAsset, 0.0);
            assetQuantities.put(normalizedAsset, existingQty + qty);
            assetAvgPrices.put(normalizedAsset, currentPrice);
            
        } else if (action.equalsIgnoreCase("SELL")) {
            double existingQty = assetQuantities.getOrDefault(normalizedAsset, 0.0);
            if (existingQty < qty) {
                System.err.println("Trade Denied: Attempted to sell " + qty + " but only own " + existingQty);
                return false; // Not enough tokens to sell
            }
            double newBalance = currentBalance + totalCost;
            setCashBalance(newBalance); // Saves to MySQL

            assetQuantities.put(normalizedAsset, existingQty - qty);
        }
        return true;
    }

    public static double getTotalHoldingsValue() {
        double totalValue = 0.0;
        for (Map.Entry<String, Double> entry : assetQuantities.entrySet()) {
            String asset = entry.getKey();
            double qty = entry.getValue();
            double price = marketPrices.getOrDefault(asset, 0.0);
            totalValue += (qty * price);
        }
        return totalValue;
    }

    public static double getTotalNetWorth() {
        return getCashBalance() + getTotalHoldingsValue();
    }
}