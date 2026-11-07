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

import javafx.stage.Modality;

public class TradingSim extends Application {

    // Live market price feed variables & UI labels
    private double currentBtcPrice = 81057.0; 
    private double currentEthPrice = 3540.0;
    private double currentEurUsdPrice = 1.0845;

    private String selectedAsset = "BTC/USD"; // Active selected index

    private final Label btcIndexLabel = new Label("BTC/USD : $81,057.00");
    private final Label ethIndexLabel = new Label("ETH/USD : $3,540.00");
    private final Label eurIndexLabel = new Label("EUR/USD : 1.0845");

    // Dynamic Buy/Sell Price Buttons for Exness-style panel
    private final Button exnessBuyBtn = new Button("Buy\n81,057.00");
    private final Button exnessSellBtn = new Button("Sell\n81,051.00");
    
    // UI Controls
    private final ComboBox<String> assetComboBox = new ComboBox<>();
    private final Label activeAssetHeader = new Label("₿ BTC/USD • Regular form");
    private final TextField qtyField = new TextField("0.01");
    private final Label balanceLabel = new Label("Balance: $500.00 USD");

    // Blockchain backend instance
    private final Blockchain tradeLedger = new Blockchain();
    private final ObservableList<Block> ledgerObservableList = FXCollections.observableArrayList();
    private final ObservableList<Block> reversedLedger = FXCollections.observableArrayList();
    private final TableView<Block> historyTable = new TableView<>();

    // Admin button instance
    private final Button btnAdmin = new Button("🛡️ Admin Panel");

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.initializeDatabase();
        DatabaseManager.testConnection();
        ledgerObservableList.addAll(tradeLedger.getChain());

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1e1e1e;");

        root.setTop(createTopBar());
        root.setLeft(createLeftToolbar()); 
        root.setCenter(createCenterChart());
        root.setRight(createRightOrderPanel()); 
        root.setBottom(createHistoryTray());

        Scene scene = new Scene(root, 1280, 800);
        primaryStage.setTitle("TradeSim - Professional Trading Terminal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createTopBar() {
        HBox bar = new HBox(15);
        bar.setPadding(new Insets(8, 15, 8, 15));
        bar.setStyle("-fx-background-color: #252526; -fx-border-color: #333; -fx-border-width: 0 0 1 0;");
        bar.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("📈 TradeSim");
        logo.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Quick Index Switcher Buttons in Top Bar
        Button btnBtcTab = new Button("₿ BTC/USD");
        btnBtcTab.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-cursor: hand;");
        btnBtcTab.setOnAction(e -> assetComboBox.setValue("BTC/USD"));

        Button btnEthTab = new Button("Ξ ETH/USD");
        btnEthTab.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-cursor: hand;");
        btnEthTab.setOnAction(e -> assetComboBox.setValue("ETH/USD"));

        Button btnEurTab = new Button("€ EUR/USD");
        btnEurTab.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-cursor: hand;");
        btnEurTab.setOnAction(e -> assetComboBox.setValue("EUR/USD"));

        Button ledgerTab = new Button("⛓️ Chain Ledger");
        ledgerTab.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-cursor: hand;");
        ledgerTab.setOnAction(e -> LedgerWindow.display(ledgerObservableList));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label demoBadge = new Label(" Demo ");
        demoBadge.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 3;");
        
        balanceLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");

        Button depositBtn = new Button("Dashboard");
        depositBtn.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        depositBtn.setOnAction(e -> PortfolioWindow.display());

        Label userStatusLabel = new Label("Not Logged In");
        userStatusLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px;");

        Button loginBtn = new Button("Login");
        loginBtn.setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white; -fx-cursor: hand;");

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        logoutBtn.setVisible(false);
        logoutBtn.setManaged(false);

        btnAdmin.setStyle("-fx-background-color: #b71c1c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnAdmin.setVisible(false);
        btnAdmin.setManaged(false);
        btnAdmin.setOnAction(e -> AdminWindow.display());

        loginBtn.setOnAction(e -> {
            AuthDialog.display(() -> {
                String updatedUser = UserSession.getLoggedInUser();
                if (updatedUser != null && !updatedUser.equals("Guest")) {
                    AccountManager.reloadHoldingsFromDatabase(updatedUser);
                    updateAccountBalanceDisplay();
                    String role = DatabaseManager.getUserRole(updatedUser);
                    if ("admin".equalsIgnoreCase(role)) {
                        btnAdmin.setVisible(true);
                        btnAdmin.setManaged(true);
                    }
                    userStatusLabel.setText("👤 " + updatedUser);
                    userStatusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    loginBtn.setVisible(false);
                    loginBtn.setManaged(false);
                    logoutBtn.setVisible(true);
                    logoutBtn.setManaged(true);
                }
            });
        });

        logoutBtn.setOnAction(e -> {
            UserSession.setLoggedInUser("Guest");
            userStatusLabel.setText("Not Logged In");
            userStatusLabel.setStyle("-fx-text-fill: #aaa;");
            updateAccountBalanceDisplay();
            btnAdmin.setVisible(false);
            btnAdmin.setManaged(false);
            logoutBtn.setVisible(false);
            logoutBtn.setManaged(false);
            loginBtn.setVisible(true);
            loginBtn.setManaged(true);
        });

        bar.getChildren().addAll(logo, btnBtcTab, btnEthTab, btnEurTab, ledgerTab, depositBtn,spacer,demoBadge , balanceLabel,  userStatusLabel, loginBtn, logoutBtn, btnAdmin);
        return bar;
    }

    private void updateAccountBalanceDisplay() {
        double currentBalance = AccountManager.getCashBalance();
        balanceLabel.setText(String.format("Balance: $%,.2f USD", currentBalance));
    }

    private VBox createLeftToolbar() {
        VBox toolbar = new VBox(8);
        toolbar.setPadding(new Insets(10, 4, 10, 4));
        toolbar.setStyle("-fx-background-color: #252526; -fx-border-color: #333; -fx-border-width: 0 1 0 0;");
        toolbar.setPrefWidth(48);
        toolbar.setAlignment(Pos.TOP_CENTER);

        String[] icons = {"↖", "➕", "📈", "📏", "📐", "✏️", "🔤", "😀", "🧲", "🗑️"};
         Button btn = new Button(icons[0]);
         btn.setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white; -fx-cursor: hand;" );
           btn.setOnAction(e -> {
            AuthDialog.display(() -> {
                String updatedUser = UserSession.getLoggedInUser();
                if (updatedUser != null && !updatedUser.equals("Guest")) {
                    AccountManager.reloadHoldingsFromDatabase(updatedUser);
                    updateAccountBalanceDisplay();
                    String role = DatabaseManager.getUserRole(updatedUser);
                    if ("admin".equalsIgnoreCase(role)) {
                        btnAdmin.setVisible(true);
                        btnAdmin.setManaged(true);
                    }
               
                }
            });
        });
//        for (String icon : icons) {
//            Button btn = new Button(icon);
//            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ccc; -fx-font-size: 14px; -fx-cursor: hand;");
//            btn.setPrefWidth(38);
//            toolbar.getChildren().add(btn);
 //    }
 toolbar.getChildren().addAll(btn); 
        return toolbar;
    }

    private StackPane createCenterChart() {
        StackPane pane = new StackPane();
        pane.setPadding(new Insets(0));
        pane.setStyle("-fx-background-color: #1e1e1e;");

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        Timeline livePriceTicker = new Timeline(
            new KeyFrame(Duration.seconds(3), event -> fetchRealBinancePrices())
        );
        livePriceTicker.setCycleCount(Timeline.INDEFINITE);
        livePriceTicker.play();

        String htmlContent = "<HTML><BODY style='background-color: #1e1e1e; margin:0; padding:0; overflow:hidden;'>" +
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
            "</BODY></HTML>";

        webEngine.loadContent(htmlContent);
        pane.getChildren().add(webView);
        return pane;
    }

    private VBox createRightOrderPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #252526; -fx-border-color: #333; -fx-border-width: 0 0 0 1;");
        panel.setPrefWidth(285);

        activeAssetHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");

        // Asset selection Dropdown in the Order Form
        assetComboBox.getItems().addAll("BTC/USD", "ETH/USD", "EUR/USD");
        assetComboBox.setValue("BTC/USD");
        assetComboBox.setMaxWidth(Double.MAX_VALUE);
        assetComboBox.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white;");

        assetComboBox.setOnAction(e -> {
            if (assetComboBox.getValue() != null) {
                selectedAsset = assetComboBox.getValue();
                activeAssetHeader.setText("📊 " + selectedAsset + " • Regular form");
                updateBuySellButtonPrices();
            }
        });

        HBox buySellBox = new HBox(8);
        
        exnessSellBtn.setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #c62828; -fx-text-fill: #c62828; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;");
        exnessSellBtn.setPrefWidth(125);
        exnessSellBtn.setPrefHeight(50);

        exnessBuyBtn.setStyle("-fx-background-color: #2d2d2d; -fx-border-color: #2e7d32; -fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4;");
        exnessBuyBtn.setPrefWidth(125);
        exnessBuyBtn.setPrefHeight(50);

        buySellBox.getChildren().addAll(exnessSellBtn, exnessBuyBtn);

        ProgressBar sentimentBar = new ProgressBar(0.64);
        sentimentBar.setStyle("-fx-accent: #2ecc71;");
        sentimentBar.setMaxWidth(Double.MAX_VALUE);
        Label sentimentLabel = new Label("36% Sell          64% Buy");
        sentimentLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");

        Label volLabel = new Label("Volume (Lots)");
        volLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px;");
        
        qtyField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white; -fx-border-color: #444;");

        // Percentage Quick Buttons Bar
        HBox percentageBox = new HBox(5);
        Button p25 = setPercentageButton("25%", 0.25);
        Button p50 = setPercentageButton("50%", 0.50);
        Button p75 = setPercentageButton("75%", 0.75);
        Button p100 = setPercentageButton("100%", 1.00);
        percentageBox.getChildren().addAll(p25, p50, p75, p100);

        Label tpLabel = new Label("Take Profit");
        tpLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px;");
        TextField tpField = new TextField();
        tpField.setPromptText("Not set");
        tpField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white; -fx-border-color: #444;");

        Label slLabel = new Label("Stop Loss");
        slLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px;");
        TextField slField = new TextField();
        slField.setPromptText("Not set");
        slField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: white; -fx-border-color: #444;");

        exnessBuyBtn.setOnAction(e -> executeTrade("BUY", selectedAsset, qtyField.getText()));
        exnessSellBtn.setOnAction(e -> executeTrade("SELL", selectedAsset, qtyField.getText()));

        panel.getChildren().addAll(
            activeAssetHeader,
            assetComboBox, 
            buySellBox, 
            sentimentBar, 
            sentimentLabel, 
            new Separator(), 
            volLabel, 
            qtyField, 
            percentageBox,
            tpLabel, 
            tpField, 
            slLabel, 
            slField
        );
        return panel;
    }

    private Button setPercentageButton(String text, double ratio) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-font-size: 10px; -fx-cursor: hand;");
        btn.setPrefWidth(62);
        btn.setOnAction(e -> {
            double calculatedLots = Math.max(0.01, Math.round((AccountManager.getCashBalance() * ratio / getLivePriceForAsset(selectedAsset)) * 100.0) / 100.0);
            qtyField.setText(String.valueOf(calculatedLots));
        });
        return btn;
    }

    private VBox createHistoryTray() {
        VBox tray = new VBox(5);
        tray.setPadding(new Insets(8, 12, 8, 12));
        tray.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #333; -fx-border-width: 1 0 0 0;");
        tray.setPrefHeight(150);

        Label trayTitle = new Label("⛓️ Blockchain Transaction Ledger Feed");
        trayTitle.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");

        historyTable.setStyle("-fx-background-color: #1e1e1e;");
        VBox.setVgrow(historyTable, Priority.ALWAYS);

        TableColumn<Block, String> colIndex = new TableColumn<>("Index");
        colIndex.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(String.valueOf(cellData.getValue().getIndex())));
        colIndex.setPrefWidth(55);

        TableColumn<Block, String> colTx = new TableColumn<>("Transaction Details");
        colTx.setCellValueFactory(cellData -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cellData.getValue().getTransactionData()));
        colTx.setPrefWidth(600);

        TableColumn<Block, String> colHash = new TableColumn<>("Current Hash");
        colHash.setCellValueFactory(cellData -> {
            String hash = cellData.getValue().getHash();
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(hash.length() > 12 ? hash.substring(0, 12) + "..." : hash);
        });
        colHash.setPrefWidth(160);

        if (historyTable.getColumns().isEmpty()) {
            historyTable.getColumns().addAll(colIndex, colTx, colHash);
        }
        
        updateReversedLedger(reversedLedger);
        historyTable.setItems(reversedLedger);

        tray.getChildren().addAll(trayTitle, historyTable);
        return tray;
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

        double executionPrice = getLivePriceForAsset(asset);
        boolean success = AccountManager.processTrade(action, asset, qty);
        if (!success) {
            showAlert("Trade Failed", "Insufficient cash balance or asset quantity to complete order.");
            return;
        }

        updateAccountBalanceDisplay();

        String activeUser = UserSession.getLoggedInUser();
        String anonymousWallet = BlockchainUtils.getAnonymizedWallet(activeUser);

        String txData = String.format("Wallet [%s] -> Action: %s | Asset: %s | Qty: %.2f | Price: $%,.2f", 
            anonymousWallet, action, asset, qty, executionPrice);
        
        tradeLedger.addBlock(txData);
        
        Platform.runLater(() -> {
            ledgerObservableList.setAll(tradeLedger.getChain());
            updateReversedLedger(reversedLedger); 
            historyTable.refresh();
        });

        DatabaseManager.logTradeToDatabase(activeUser, action, asset, qty);

        showAlert("Trade Executed", String.format("Successfully executed %s for %.2f %s at live market price $%,.2f and logged to MySQL!", 
            action, qty, asset, executionPrice));
    }

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

    private void fetchRealBinancePrices() {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(3))
            .build();
        
        AccountManager.updateMarketPrice("BTC/USD", currentBtcPrice);
        AccountManager.updateMarketPrice("ETH/USD", currentEthPrice);
        
        HttpRequest btcReq = HttpRequest.newBuilder()
            .uri(URI.create("https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT"))
            .header("User-Agent", "Mozilla/5.0")
            .GET()
            .build();

        client.sendAsync(btcReq, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenAccept(responseBody -> {
                try {
                    double btcPrice = parseSingleJsonPrice(responseBody);
                    if (btcPrice > 0) {
                        Platform.runLater(() -> {
                            currentBtcPrice = btcPrice;
                            btcIndexLabel.setText(String.format("BTC/USD : $%,.2f", currentBtcPrice));
                            updateBuySellButtonPrices();
                        });
                    }
                } catch (Exception e) {
                    System.err.println("BTC Price parse error: " + e.getMessage());
                }
            });

        HttpRequest ethReq = HttpRequest.newBuilder()
            .uri(URI.create("https://api.binance.com/api/v3/ticker/price?symbol=ETHUSDT"))
            .header("User-Agent", "Mozilla/5.0")
            .GET()
            .build();

        client.sendAsync(ethReq, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenAccept(responseBody -> {
                try {
                    double ethPrice = parseSingleJsonPrice(responseBody);
                    if (ethPrice > 0) {
                        Platform.runLater(() -> {
                            currentEthPrice = ethPrice;
                            ethIndexLabel.setText(String.format("ETH/USD : $%,.2f", currentEthPrice));
                            updateBuySellButtonPrices();
                        });
                    }
                } catch (Exception e) {
                    System.err.println("ETH Price parse error: " + e.getMessage());
                }
            });
    }

    private void updateBuySellButtonPrices() {
        double activePrice = getLivePriceForAsset(selectedAsset);
        exnessBuyBtn.setText(String.format("Buy\n%,.2f", activePrice));
        exnessSellBtn.setText(String.format("Sell\n%,.2f", activePrice - 1.5));
    }

    private double parseSingleJsonPrice(String json) {
        try {
            int priceIdx = json.indexOf("\"price\":\"");
            if (priceIdx == -1) return -1;
            int start = priceIdx + 9;
            int end = json.indexOf("\"", start);
            return Double.parseDouble(json.substring(start, end));
        } catch (Exception e) {
            return -1;
        }
    }

    private void updateReversedLedger(ObservableList<Block> targetList) {
        List<Block> chainCopy = new ArrayList<>(tradeLedger.getChain());
        java.util.Collections.reverse(chainCopy);
        targetList.setAll(chainCopy);
    }

    public static class BlockchainUtils {
        public static String getAnonymizedWallet(String username) {
            if (username == null || username.equals("Guest")) {
                return "Xcb0000000";
            }
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(username.getBytes("UTF-8"));
                StringBuilder hexString = new StringBuilder("Xcb");
                for (int i = 0; i < 4; i++) {
                    hexString.append(String.format("%02x", hash[i]));
                }
                return hexString.toString();
            } catch (Exception e) {
                return "Xcb998876c";
            }
        }
    }
    
    public static class LedgerWindow {
        public static void display(ObservableList<TradingSim.Block> chainData) {
            Stage window = new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            window.setTitle("TradeSim - Immutable Blockchain Ledger");
            window.setMinWidth(800);
            window.setMinHeight(450);

            VBox root = new VBox(10);
            root.setPadding(new Insets(15));
            root.setStyle("-fx-background-color: #1e1e1e;");

            Label title = new Label("⛓️ Cryptographic Transaction Chain");
            title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

            TableView<TradingSim.Block> table = new TableView<>();
            table.setPrefHeight(350);
            table.setStyle("-fx-background-color: #2d2d2d;");

            TableColumn<TradingSim.Block, String> colIndex = new TableColumn<>("Index");
            colIndex.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getIndex())));
            
            TableColumn<TradingSim.Block, String> colTx = new TableColumn<>("Transaction Data");
            colTx.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTransactionData()));
            colTx.setPrefWidth(400);

            TableColumn<TradingSim.Block, String> colHash = new TableColumn<>("Hash");
            colHash.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHash()));
            colHash.setPrefWidth(200);

            table.getColumns().addAll(colIndex, colTx, colHash);
            table.setItems(chainData);

            root.getChildren().addAll(title, table);
            window.setScene(new Scene(root));
            window.showAndWait();
        }
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