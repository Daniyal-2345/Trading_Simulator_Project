package com.mycompany.tradingsim;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AdminWindow {

    public static void display() {
        Stage adminStage = new Stage();
        adminStage.initModality(Modality.APPLICATION_MODAL);
        adminStage.setTitle("TradeSim - Admin Control Panel");
        adminStage.setMinWidth(900);
        adminStage.setMinHeight(500);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1e1e1e;");

        Label title = new Label("🛡️ Administrator Dashboard - Support Tickets");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        TableView<DatabaseManager.SupportTicket> table = new TableView<>();
        table.setStyle("-fx-background-color: #2d2d2d;");
        table.setPrefHeight(400);

        TableColumn<DatabaseManager.SupportTicket, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colId.setPrefWidth(50);

        TableColumn<DatabaseManager.SupportTicket, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        colName.setPrefWidth(150);

        TableColumn<DatabaseManager.SupportTicket, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));
        colEmail.setPrefWidth(200);

        TableColumn<DatabaseManager.SupportTicket, String> colMsg = new TableColumn<>("Message");
        colMsg.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMessage()));
        colMsg.setPrefWidth(350);

        TableColumn<DatabaseManager.SupportTicket, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCreatedAt()));
        colDate.setPrefWidth(120);

        table.getColumns().addAll(colId, colName, colEmail, colMsg, colDate);
        table.setItems(DatabaseManager.getAllSupportMessages());

        root.getChildren().addAll(title, table);
        adminStage.setScene(new Scene(root));
        adminStage.showAndWait();
    }
}