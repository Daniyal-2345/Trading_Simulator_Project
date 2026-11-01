package com.mycompany.tradingsim;

import java.sql.*;

public class DatabaseManager {
    // Database connection URL (configured for XAMPP/Local MySQL port 3307)
    private static final String URL = "jdbc:mysql://localhost:3307/tradingsim_db?useSSL=false&serverTimezone=UTC";
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
                "balance DOUBLE DEFAULT 100000.0, " +
                "role VARCHAR(20) DEFAULT 'user')";

        String createTradesTable = "CREATE TABLE IF NOT EXISTS trades (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL, " +
                "action VARCHAR(10) NOT NULL, " +
                "asset VARCHAR(20) NOT NULL, " +
                "quantity DOUBLE NOT NULL, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        String createSupportTable = "CREATE TABLE IF NOT EXISTS support_messages (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100), " +
                "email VARCHAR(100), " +
                "message TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createTradesTable);
            stmt.execute(createSupportTable);

            // Safety check: ensure 'role' column exists if table was created earlier without it
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'user'");
            } catch (SQLException ignored) {
                // Column likely already exists, safe to ignore
            }

            System.out.println("Database tables initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    // Fetch user role for admin control panel access
    public static String getUserRole(String username) {
        String sql = "SELECT role FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    return role != null ? role : "user";
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch user role: " + e.getMessage());
        }
        return "user";
    }

    // Add a new user record to the database during registration
    // Updated registerUser method with secret code check
    public static boolean registerUser(String fullname, String username, String password, String adminCode) {
        // Define your secret code required to become an admin
        String secretAdminCode = "AD"; 
        
        // Determine role based on the code entered
        String assignedRole = "user";
        if (adminCode != null && adminCode.equals(secretAdminCode)) {
            assignedRole = "admin";
        }

        String sql = "INSERT INTO users (fullname, username, password, balance, role) VALUES (?, ?, ?, 100000.0, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fullname);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.setString(4, assignedRole);
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

    // Fetch user's persistent cash balance from MySQL
    public static double getUserBalance(String username) {
        String sql = "SELECT balance FROM users WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch user balance: " + e.getMessage());
        }
        return 100000.0; // Fallback default
    }

    // Save updated balance back to MySQL
    public static void updateUserBalance(String username, double newBalance) {
        String sql = "UPDATE users SET balance = ? WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update user balance in MySQL: " + e.getMessage());
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

    public static boolean logSupportMessage(String name, String email, String message) {
        String query = "INSERT INTO support_messages (name, email, message) VALUES (?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, message);
            pstmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("Error logging support message: " + e.getMessage());
            return false;
        }
    }

    // Support Ticket data model for Admin Panel
    public static class SupportTicket {
        private int id;
        private String name, email, message, createdAt;

        public SupportTicket(int id, String name, String email, String message, String createdAt) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.message = message;
            this.createdAt = createdAt;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getMessage() { return message; }
        public String getCreatedAt() { return createdAt; }
    }

    // Fetch all support messages for Admin Window
    public static javafx.collections.ObservableList<SupportTicket> getAllSupportMessages() {
        javafx.collections.ObservableList<SupportTicket> list = javafx.collections.FXCollections.observableArrayList();
        String sql = "SELECT id, name, email, message, created_at FROM support_messages ORDER BY created_at DESC";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new SupportTicket(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("message"),
                    rs.getString("created_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch support messages: " + e.getMessage());
        }
        return list;
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