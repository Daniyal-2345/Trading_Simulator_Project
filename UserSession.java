package com.mycompany.tradingsim;

public class UserSession {
    private static String loggedInUser = null;

    public static void setLoggedInUser(String username) {
        loggedInUser = username;
    }

    public static String getLoggedInUser() {
        return loggedInUser != null ? loggedInUser : "GuestUser";
    }

    public static boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public static void clearSession() {
        loggedInUser = null;
    }
}