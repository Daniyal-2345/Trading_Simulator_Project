package com.mycompany.tradingsim;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TradingSim extends Application {

    // Blockchain backend instance
    private final Blockchain tradeLedger = new Blockchain();
    private final ObservableList<Block> ledgerObservableList = FXCollections.observableArrayList();

@Override
    public void start(Stage primaryStage) {
        // 1. INITIALIZE MYSQL DATABASE TABLES AUTOMATICALLY ON STARTUP
        DatabaseManager.initializeDatabase();

        // 2. Load initial blocks into observable list
        ledgerObservableList.addAll(tradeLedger.getChain());

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1e1e1e;");

        root.setTop(createTopBar());
        root.setLeft(createSidebar());
        root.setCenter(createCenterChart());
        root.setRight(createRightPanel());
        root.setBottom(createHistoryTray());

        Scene scene = new Scene(root, 1280, 800);
        primaryStage.setTitle("TradeSim - Risk-Aware Trading Simulation Platform");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

 private HBox createTopBar() {
    HBox bar = new HBox(15);
    bar.setPadding(new Insets(10, 20, 10, 20));
    bar.setStyle("-fx-background-color: #252526; -fx-border-color: #333;");
    bar.setAlignment(Pos.CENTER_LEFT);

    Label logo = new Label("📈 TradeSim");
    logo.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    Button loginBtn = new Button("Login / Register");
    loginBtn.setStyle("-fx-background-color: #007acc; -fx-text-fill: white;");
    
    // Wire up the AuthDialog when clicked
    loginBtn.setOnAction(e -> {
        AuthDialog.display(() -> {
            loginBtn.setText("Logged In");
            loginBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");
        });
    });

    bar.getChildren().addAll(logo, spacer, loginBtn);
    return bar;
}

private VBox createSidebar() {
    VBox sidebar = new VBox(10);
    sidebar.setPadding(new Insets(15));
    sidebar.setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #333;");
    sidebar.setPrefWidth(160);

   Button btnDash = new Button("Dashboard");
btnDash.setOnAction(e -> DashboardWindow.display());
    Button btnPortfolio = new Button("Portfolio");
    btnPortfolio.setOnAction(e -> PortfolioWindow.display());
    Button btnLedger = new Button("Chain Ledger");
    Button btnRisk = new Button("Risk Config");
    
    // Support / Contact Us Button
    Button btnContact = new Button("Contact Us");
    btnContact.setOnAction(e -> ContactUsWindow.display());

    sidebar.getChildren().addAll(btnDash, btnPortfolio, btnLedger, btnRisk, new Separator(), btnContact);

    for (Node node : sidebar.getChildren()) {
        if (node instanceof Button) {
            ((Button) node).setMaxWidth(Double.MAX_VALUE);
            ((Button) node).setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white; -fx-alignment: CENTER_LEFT;");
        }
    }

    return sidebar;
}

    private StackPane createCenterChart() {
        StackPane pane = new StackPane();
        pane.setPadding(new Insets(10));
        
        Label chartPlaceholder = new Label("[ Live Candlestick Chart / Asset Simulation Area ]");
        chartPlaceholder.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
        
        pane.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #333;");
        pane.getChildren().add(chartPlaceholder);
        return pane;
    }
private void executeTrade(String action, String asset, String qtyStr) {
        double qty;
        try {
            qty = Double.parseDouble(qtyStr);
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Invalid Quantity", "Please enter a valid numeric quantity.");
            return;
        }

        // 1. Process numerical balance check & update via AccountManager
        boolean success = AccountManager.processTrade(action, asset, qty);
        if (!success) {
            showAlert("Trade Failed", "Insufficient cash balance or asset quantity to complete order.");
            return;
        }

        // 2. Record trade to local cryptographic blockchain ledger
        String txData = "Action: " + action + " | Asset: " + asset + " | Qty: " + qty + " | Cash Left: $" + String.format("%,.2f", AccountManager.getCashBalance());
        tradeLedger.addBlock(txData);
        ledgerObservableList.setAll(tradeLedger.getChain());
        
        // 3. PERSIST TRADE TO MYSQL DATABASE (XAMPP)
        DatabaseManager.logTradeToDatabase("DemoUser", action, asset, qty);

        showAlert("Trade Executed", "Successfully executed " + action + " for " + qty + " " + asset + " and logged to MySQL!");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #333;");
        panel.setPrefWidth(280);

        Label indicesTitle = new Label("Market Indices");
        indicesTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        ListView<String> indicesList = new ListView<>();
        indicesList.getItems().addAll("EUR/USD : 1.0845 (+0.3%)", "BTC/USD : $67,200 (-1.2%)", "ETH/USD : $3,540 (+0.8%)");
        indicesList.setPrefHeight(120);

        Label orderTitle = new Label("Place Order");
        orderTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        ComboBox<String> assetBox = new ComboBox<>();
        assetBox.getItems().addAll("EUR/USD", "BTC/USD", "ETH/USD");
        assetBox.setValue("BTC/USD");
        assetBox.setMaxWidth(Double.MAX_VALUE);

        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity");

        Button buyBtn = new Button("BUY");
        buyBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        buyBtn.setMaxWidth(Double.MAX_VALUE);

        Button sellBtn = new Button("SELL");
        sellBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        sellBtn.setMaxWidth(Double.MAX_VALUE);

        buyBtn.setOnAction(e -> executeTrade("BUY", assetBox.getValue(), qtyField.getText()));
        sellBtn.setOnAction(e -> executeTrade("SELL", assetBox.getValue(), qtyField.getText()));

        panel.getChildren().addAll(indicesTitle, indicesList, new Separator(), orderTitle, assetBox, qtyField, buyBtn, sellBtn);
        return panel;
    }

 

    private VBox createHistoryTray() {
        VBox tray = new VBox(5);
        tray.setPadding(new Insets(10));
        tray.setStyle("-fx-background-color: #252526; -fx-border-color: #333;");
        tray.setPrefHeight(160);

        Label trayTitle = new Label("Transaction History / Blockchain Ledger Feed");
        trayTitle.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");

        TableView<Block> historyTable = new TableView<>();
        historyTable.setPrefHeight(110);

        TableColumn<Block, String> colIndex = new TableColumn<>("Index");
        colIndex.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIndex())));

        TableColumn<Block, String> colTx = new TableColumn<>("Transaction Details");
        colTx.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTransactionData()));
        colTx.setPrefWidth(350);

        TableColumn<Block, String> colHash = new TableColumn<>("Current Hash");
        colHash.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHash().substring(0, 12) + "..."));
        colHash.setPrefWidth(150);

        TableColumn<Block, String> colPrevHash = new TableColumn<>("Previous Hash");
        colPrevHash.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPreviousHash().equals("0") ? "0" : cellData.getValue().getPreviousHash().substring(0, 12) + "..."));
        colPrevHash.setPrefWidth(150);

        historyTable.getColumns().addAll(colIndex, colTx, colHash, colPrevHash);
        historyTable.setItems(ledgerObservableList);

        tray.getChildren().addAll(trayTitle, historyTable);
        return tray;
    }

    public static class Block {
        private int index;
        private long timestamp;
        private String transactionData;
        private String previousHash;
        private String hash;

        public Block(int index, String transactionData, String previousHash) {
            this.index = index;
            this.timestamp = new Date().getTime();
            this.transactionData = transactionData;
            this.previousHash = previousHash;
            this.hash = calculateHash();
        }

        public String calculateHash() {
            try {
                String input = index + Long.toString(timestamp) + transactionData + (previousHash == null ? "" : previousHash);
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] encodedhash = digest.digest(input.getBytes("UTF-8"));
                StringBuilder hexString = new StringBuilder();
                for (byte b : encodedhash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                return hexString.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public int getIndex() { return index; }
        public String getHash() { return hash; }
        public String getPreviousHash() { return previousHash; }
        public String getTransactionData() { return transactionData; }
    }

    public static class Blockchain {
        private List<Block> chain;

        public Blockchain() {
            chain = new ArrayList<>();
            chain.add(new Block(0, "Genesis Block - TradeSim Initialized", "0"));
        }

        public Block getLatestBlock() {
            return chain.get(chain.size() - 1);
        }

        public void addBlock(String transactionData) {
            Block latestBlock = getLatestBlock();
            Block newBlock = new Block(latestBlock.getIndex() + 1, transactionData, latestBlock.getHash());
            chain.add(newBlock);
        }

        public List<Block> getChain() {
            return chain;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}