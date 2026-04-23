package com.stocksense.dao;

import com.stocksense.db.DatabaseManager;
import com.stocksense.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

// This DAO handles user records like login lookup, registration, and profile changes.
// It talks directly to the users table in the database.
public class UserDAO {
    public void addUser(User user) throws SQLException {
        String sql = "INSERT INTO users(full_name, username, password, date_of_birth) VALUES(?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getDateOfBirth().toString());
            ps.executeUpdate();
        }
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                // Returning Optional here makes login handling cleaner.
                if (rs.next()) {
                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getString("username"),
                            rs.getString("password"),
                            LocalDate.parse(rs.getString("date_of_birth"))
                    );
                    return Optional.of(user);
                }
                return Optional.empty();
            }
        }
    }

    public void updateUser(User user, String originalUsername) throws SQLException {
        String sql = """
                UPDATE users
                SET full_name = ?, username = ?, date_of_birth = ?
                WHERE id = ? OR username = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getDateOfBirth().toString());
            ps.setInt(4, user.getId());
            ps.setString(5, originalUsername);
            ps.executeUpdate();
        }
    }

    public boolean resetPassword(String username, LocalDate dateOfBirth, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE username = ? AND date_of_birth = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, username);
            ps.setString(3, dateOfBirth.toString());
            return ps.executeUpdate() > 0;
        }
    }

    public void ensureDefaultAdmin() throws SQLException {
        String sql = """
                INSERT INTO users(full_name, username, password, date_of_birth)
                SELECT 'System Admin', 'admin', 'admin123', '2000-01-01'
                WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // This makes sure the app always has one default account on first run.
            ps.executeUpdate();
        }
    }
}
