package com.mycompany.tradingsim;

import java.util.regex.Pattern;
import javafx.scene.control.Alert;

public class RegistrationValidator {

    // Regex: Only letters and spaces allowed (no numbers)
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$");

    // Regex: At least 8 chars, 1 uppercase, 1 lowercase, 1 number, 1 special character
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_]).{8,}$"
    );

    public static boolean validateAndRegister(String fullName, String username, String password) {
        // 1. Validate Full Name (No numbers)
        if (fullName == null || fullName.trim().isEmpty() || !NAME_PATTERN.matcher(fullName).matches()) {
            showAlert("Invalid Name", "Full name cannot contain numbers or special characters.");
            return false;
        }

        // 2. Validate Username
        if (username == null || username.trim().length() < 3) {
            showAlert("Invalid Username", "Username must be at least 3 characters long.");
            return false;
        }

        // 3. Validate Password Rules
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            showAlert("Weak Password", 
                "Password must meet the following rules:\n" +
                "• At least 8 characters long\n" +
                "• At least one uppercase letter (A-Z)\n" +
                "• At least one lowercase letter (a-z)\n" +
                "• At least one number (0-9)\n" +
                "• At least one special character (@#$%^&+=!_)");
            return false;
        }

        // Validation passed! Proceed with account creation / database insertion
        return true;
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}