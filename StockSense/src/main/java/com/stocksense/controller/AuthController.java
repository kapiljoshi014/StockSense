package com.stocksense.controller;

import com.stocksense.dao.UserDAO;
import com.stocksense.model.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

// This controller handles login, signup, password reset, and current user data.
// It works like the bridge between auth screens and the users table.
public class AuthController {
    private final UserDAO userDAO;
    private User currentUser;

    public AuthController() {
        this.userDAO = new UserDAO();
    }

    public void initializeDefaultUser() {
        try {
            userDAO.ensureDefaultAdmin();
        } catch (SQLException e) {
            throw new RuntimeException("Unable to initialize default user", e);
        }
    }

    public boolean login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Username and password are required.");
        }
        try {
            Optional<User> user = userDAO.findByUsername(username.trim());
            // Saving the logged-in user here helps other parts use the same session data.
            boolean authenticated = user.isPresent() && user.get().getPassword().equals(password);
            currentUser = authenticated ? user.get() : null;
            return authenticated;
        } catch (SQLException e) {
            throw new RuntimeException("Login failed due to database error.", e);
        }
    }

    public void registerUser(String fullName, String username, String password, String repeatPassword, LocalDate dateOfBirth) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (!password.equals(repeatPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth is required.");
        }

        try {
            userDAO.addUser(new User(fullName.trim(), username.trim(), password, dateOfBirth));
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new IllegalArgumentException("Username already exists.");
            }
            throw new RuntimeException("Unable to register user.", e);
        }
    }

    public void resetPassword(String username, LocalDate dateOfBirth, String newPassword, String repeatPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth is required.");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required.");
        }
        if (!newPassword.equals(repeatPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        try {
            boolean updated = userDAO.resetPassword(username.trim(), dateOfBirth, newPassword);
            if (!updated) {
                throw new IllegalArgumentException("Username and date of birth do not match.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to reset password.", e);
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    public void updateCurrentUser(String fullName, String username, LocalDate dateOfBirth) {
        if (currentUser == null) {
            throw new IllegalStateException("No active user session.");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth is required.");
        }

        String originalUsername = currentUser.getUsername();
        // Updating the in-memory user too so the UI shows the latest details immediately.
        currentUser.setFullName(fullName.trim());
        currentUser.setUsername(username.trim());
        currentUser.setDateOfBirth(dateOfBirth);
        try {
            userDAO.updateUser(currentUser, originalUsername);
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new IllegalArgumentException("Username already exists.");
            }
            throw new RuntimeException("Unable to update profile.", e);
        }
    }
}
