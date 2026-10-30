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

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javafx.application.Platform;

import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TradingSim extends Application {

    // Live market price feed variables & UI labels
    private double currentBtcPrice = 81057.0; // Initialized close to live Binance price
    private double currentEthPrice = 3540.0;
    private double currentEurUsdPrice = 1.0845;

    private final Label btcIndexLabel = new Label("BTC/USD : $81,057.00");
    private final Label ethIndexLabel = new Label("ETH/USD : $3,540.00");
    private final Label eurIndexLabel = new Label("EUR/USD : 1.0845");

    // Blockchain backend instance
    private final Blockchain tradeLedger = new Blockchain();
    private final ObservableList<Block> ledgerObservableList = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        // 1. INITIALIZE MYSQL DATABASE TABLES AUTOMATICALLY ON STARTUP
        DatabaseManager.initializeDatabase();
        
        // Database connection checkup
        DatabaseManager.testConnection();
        
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
        
        // Wire up the AuthDialog when clicked & update button text on success
        loginBtn.setOnAction(e -> {
            AuthDialog.display(() -> {
                String activeUser = UserSession.getLoggedInUser();
                loginBtn.setText("👤 " + activeUser);
                loginBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
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

    private VBox createRightPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #333;");
        panel.setPrefWidth(280);

        Label indicesTitle = new Label("Market Indices (Live Binance)");
        indicesTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        ListView<Node> indicesList = new ListView<>();
        indicesList.setPrefHeight(120);
        indicesList.setStyle("-fx-background-color: #1e1e1e;");

        btcIndexLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        ethIndexLabel.setStyle("-fx-text-fill: #2ecc71;");
        eurIndexLabel.setStyle("-fx-text-fill: #2ecc71;");

        indicesList.getItems().addAll(eurIndexLabel, btcIndexLabel, ethIndexLabel);

        Label orderTitle = new Label("Place Order");
        orderTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        ComboBox<String> assetBox = new ComboBox<>();
        assetBox.getItems().addAll("EUR/USD", "BTC/USD", "ETH/USD");
        assetBox.setValue("BTC/USD");
        assetBox.setMaxWidth(Double.MAX_VALUE);

        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity");
        qtyField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white; -fx-prompt-text-fill: #888;");

        Button buyBtn = new Button("BUY");
        buyBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        buyBtn.setMaxWidth(Double.MAX_VALUE);

        Button sellBtn = new Button("SELL");
        sellBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        sellBtn.setMaxWidth(Double.MAX_VALUE);

        buyBtn.setOnAction(e -> executeTrade("BUY", assetBox.getValue(), qtyField.getText()));
        sellBtn.setOnAction(e -> executeTrade("SELL", assetBox.getValue(), qtyField.getText()));

        panel.getChildren().addAll(indicesTitle, indicesList, new Separator(), orderTitle, assetBox, qtyField, buyBtn, sellBtn);
        return panel;
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

        // 1. Fetch the exact live market price from Binance feed
        double executionPrice = getLivePriceForAsset(asset);

        // 2. Process numerical balance check & update via AccountManager
        boolean success = AccountManager.processTrade(action, asset, qty);
        if (!success) {
            showAlert("Trade Failed", "Insufficient cash balance or asset quantity to complete order.");
            return;
        }

        // 3. Record trade with the live execution price to cryptographic blockchain ledger
        String txData = String.format("Action: %s | Asset: %s | Qty: %.2f | Price: $%,.2f | Cash Left: $%,.2f", 
            action, asset, qty, executionPrice, AccountManager.getCashBalance());
        
        tradeLedger.addBlock(txData);
        ledgerObservableList.setAll(tradeLedger.getChain());
        
        // 4. PERSIST TRADE TO MYSQL DATABASE USING THE LOGGED-IN USER
        String activeUser = UserSession.getLoggedInUser();
        DatabaseManager.logTradeToDatabase(activeUser, action, asset, qty);
        
        showAlert("Trade Executed", String.format("Successfully executed %s for %.2f %s at live market price $%,.2f and logged to MySQL!", 
            action, qty, asset, executionPrice));
    }

    // Helper method to retrieve the live price matching the selected asset
    private double getLivePriceForAsset(String asset) {
        switch (asset) {
            case "BTC/USD": return currentBtcPrice;
            case "ETH/USD": return currentEthPrice;
            case "EUR/USD": return currentEurUsdPrice;
            default: return 100.0;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private StackPane createCenterChart() {
        StackPane pane = new StackPane();
        pane.setPadding(new Insets(0));
        pane.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #333;");

        // Create a JavaFX WebView to render the live TradingView widget
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        // Real Binance API background poller for 100% chart & trade synchronization
        Timeline livePriceTicker = new Timeline(
            new KeyFrame(Duration.seconds(3), event -> {
                fetchRealBinancePrices();
            })
        );
        livePriceTicker.setCycleCount(Timeline.INDEFINITE);
        livePriceTicker.play();

        // Official TradingView Advanced Chart HTML Widget (Dark theme, Binance BTCUSDT feed)
        String htmlContent = "<HTML><BODY style='background-color: #1e1e1e; margin:0; padding:0; overflow:hidden;'>" +
            "<!-- TradingView Widget BEGIN -->" +
            "<div class=\"tradingview-widget-container\" style=\"height:100vh;width:100vw\">" +
            "  <div class=\"tradingview-widget-container__widget\" style=\"height:100%;width:100%\"></div>" +
            "  <script type=\"text/javascript\" src=\"https://s3.tradingview.com/external-embedding/embed-widget-advanced-chart.js\" async>" +
            "  {" +
            "    \"autosize\": true," +
            "    \"symbol\": \"BINANCE:BTCUSDT\"," +
            "    \"interval\": \"D\"," +
            "    \"timezone\": \"Etc/UTC\"," +
            "    \"theme\": \"dark\"," +
            "    \"style\": \"1\"," +
            "    \"locale\": \"en\"," +
            "    \"allow_symbol_change\": true," +
            "    \"calendar\": false," +
            "    \"support_host\": \"https://www.tradingview.com\"" +
            "  }" +
            "  </script>" +
            "</div>" +
            "<!-- TradingView Widget END -->" +
            "</BODY></HTML>";

        webEngine.loadContent(htmlContent);

        pane.getChildren().add(webView);
        return pane;
    }
    
    private void fetchRealBinancePrices() {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(2))
            .build();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.binance.com/api/v3/ticker/price?symbols=[\"BTCUSDT\",\"ETHUSDT\"]"))
            .GET()
            .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenAccept(responseBody -> {
                try {
                    if (responseBody.contains("BTCUSDT")) {
                        double btcPrice = parseJsonPrice(responseBody, "BTCUSDT");
                        double ethPrice = parseJsonPrice(responseBody, "ETHUSDT");

                        Platform.runLater(() -> {
                            if (btcPrice > 0) currentBtcPrice = btcPrice;
                            if (ethPrice > 0) currentEthPrice = ethPrice;
                            
                            // Update UI Market Indices live in sync with Binance & TradingView
                            btcIndexLabel.setText(String.format("BTC/USD : $%,.2f", currentBtcPrice));
                            ethIndexLabel.setText(String.format("ETH/USD : $%,.2f", currentEthPrice));
                        });
                    }
                } catch (Exception ignored) {
                    // Fallback silently if offline/rate-limited
                }
            });
    }

    // Lightweight JSON string parser to avoid heavy external libraries like Gson/Jackson
    private double parseJsonPrice(String json, String symbol) {
        try {
            int symbolIdx = json.indexOf(symbol);
            int priceKeyIdx = json.indexOf("price", symbolIdx);
            int colonIdx = json.indexOf(":", priceKeyIdx);
            int quote1 = json.indexOf("\"", colonIdx);
            int quote2 = json.indexOf("\"", quote1 + 1);
            String priceStr = json.substring(quote1 + 1, quote2);
            return Double.parseDouble(priceStr);
        } catch (Exception e) {
            return -1;
        }
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
        historyTable.setStyle("-fx-background-color: #1e1e1e;");

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