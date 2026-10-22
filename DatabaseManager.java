package com.mycompany.tradingsim;

import java.sql.*;

public class DatabaseManager {
    // Database connection URL (defaults to XAMPP/Local MySQL port 3306)
    private static final String URL = "jdbc:mysql://localhost:3306/tradingsim_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Automatically create tables on application startup if they don't exist
    public static void initializeDatabase() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "fullname VARCHAR(100) NOT NULL, " +
                "username VARCHAR(50) UNIQUE NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "balance DOUBLE DEFAULT 100000.0)";

        String createTradesTable = "CREATE TABLE IF NOT EXISTS trades (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL, " +
                "action VARCHAR(10) NOT NULL, " +
                "asset VARCHAR(20) NOT NULL, " +
                "quantity DOUBLE NOT NULL, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createTradesTable);
            System.out.println("Database tables initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    // Add a new user record to the database during registration
    public static boolean registerUser(String fullname, String username, String password) {
        String sql = "INSERT INTO users (fullname, username, password, balance) VALUES (?, ?, ?, 100000.0)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fullname);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Registration failed (Username may already exist): " + e.getMessage());
            return false;
        }
    }

    // Verify user login credentials against the database
    public static boolean loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Returns true if a matching user record is found
            }
        } catch (SQLException e) {
            System.err.println("Login query failed: " + e.getMessage());
            return false;
        }
    }

    // Test connection method
    public static void testConnection() {
        try (Connection conn = connect()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println(" SUCCESS: Successfully connected to XAMPP MySQL database!");
            }
        } catch (SQLException e) {
            System.err.println(" CONNECTION FAILED: Could not connect to MySQL.");
            System.err.println("Error details: " + e.getMessage());
        }
    }
    
    // Save executed trades to the database for persistent auditing
    public static void logTradeToDatabase(String username, String action, String asset, double quantity) {
        String sql = "INSERT INTO trades (username, action, asset, quantity) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, action);
            pstmt.setString(3, asset);
            pstmt.setDouble(4, quantity);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to log trade to database: " + e.getMessage());
        }
    }
}