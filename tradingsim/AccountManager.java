package com.mycompany.tradingsim;

import java.util.HashMap;
import java.util.Map;

public class AccountManager {
    // Initial starting balance
    private static double cashBalance = 100000.0;
    
    // Tracking quantities and average prices for assets
    private static final Map<String, Double> assetQuantities = new HashMap<>();
    private static final Map<String, Double> assetAvgPrices = new HashMap<>();

    // Current market price reference for valuation
    private static final Map<String, Double> marketPrices = Map.of(
        "EUR/USD", 1.0845,
        "BTC/USD", 67200.0,
        "ETH/USD", 3540.0
    );

    public static double getCashBalance() {
        return cashBalance;
    }

    public static Map<String, Double> getAssetQuantities() {
        return assetQuantities;
    }

    public static Map<String, Double> getAssetAvgPrices() {
        return assetAvgPrices;
    }

    // Process a trade and update numbers dynamically
    public static boolean processTrade(String action, String asset, double qty) {
        double currentPrice = marketPrices.getOrDefault(asset, 100.0);
        double totalCost = qty * currentPrice;

        if (action.equalsIgnoreCase("BUY")) {
            if (cashBalance < totalCost) {
                return false; // Insufficient funds
            }
            cashBalance -= totalCost;
            double existingQty = assetQuantities.getOrDefault(asset, 0.0);
            assetQuantities.put(asset, existingQty + qty);
            assetAvgPrices.put(asset, currentPrice);
        } else if (action.equalsIgnoreCase("SELL")) {
            double existingQty = assetQuantities.getOrDefault(asset, 0.0);
            if (existingQty < qty) {
                return false; // Not enough shares/coins to sell
            }
            cashBalance += totalCost;
            assetQuantities.put(asset, existingQty - qty);
        }
        return true;
    }

    // Calculate total market value of all held assets
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

    // Total Net Worth = Cash + Holdings Market Value
    public static double getTotalNetWorth() {
        return cashBalance + getTotalHoldingsValue();
    }
}