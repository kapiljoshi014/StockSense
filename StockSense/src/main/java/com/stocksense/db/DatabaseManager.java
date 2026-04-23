package com.stocksense.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// This file manages database connection and table setup.
// It also handles basic migration work when the table structure changes.
public final class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:stocksense.db";

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        String createUsers = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    date_of_birth TEXT NOT NULL
                );
                """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Creating users first because products now depend on user_id.
            stmt.execute(createUsers);
            ensureDefaultAdminUser(conn);
            ensureProductsTable(conn, stmt);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to initialize database", e);
        }
    }

    private static void ensureProductsTable(Connection conn, Statement stmt) throws SQLException {
        int fallbackUserId = getDefaultAdminUserId(conn);
        if (!tableExists(conn, "products")) {
            stmt.execute(productsTableDefinition("products"));
            return;
        }

        if (hasUniqueBatchConstraint(conn) || !hasColumn(conn, "products", "user_id")) {
            // Rebuilding the table here helps older project data match the new format.
            stmt.execute("ALTER TABLE products RENAME TO products_legacy");
            stmt.execute(productsTableDefinition("products"));
            String userIdExpression = hasColumn(conn, "products_legacy", "user_id")
                    ? "COALESCE(user_id, " + fallbackUserId + ")"
                    : Integer.toString(fallbackUserId);
            stmt.execute("""
                    INSERT INTO products(id, user_id, name, category, batch_number, manufacture_date, expiry_date)
                    SELECT id, %s, name, category, batch_number, manufacture_date, expiry_date
                    FROM products_legacy
                    """.formatted(userIdExpression));
            stmt.execute("DROP TABLE products_legacy");
        }
    }

    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?";
        try (var ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static boolean hasUniqueBatchConstraint(Connection conn) throws SQLException {
        try (var ps = conn.prepareStatement("PRAGMA index_list(products)");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                if (rs.getInt("unique") == 1) {
                    String indexName = rs.getString("name");
                    if (indexCoversBatchNumber(conn, indexName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("PRAGMA table_info(" + tableName + ")");
             ResultSet rs = ps.executeQuery()) {
            // Checking columns like this is useful during simple migration steps.
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean indexCoversBatchNumber(Connection conn, String indexName) throws SQLException {
        try (var ps = conn.prepareStatement("PRAGMA index_info(" + indexName + ")");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                if ("batch_number".equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String productsTableDefinition(String tableName) {
        return """
                CREATE TABLE IF NOT EXISTS %s (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    batch_number TEXT NOT NULL,
                    manufacture_date TEXT NOT NULL,
                    expiry_date TEXT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                );
                """.formatted(tableName);
    }

    private static void ensureDefaultAdminUser(Connection conn) throws SQLException {
        String sql = """
                INSERT INTO users(full_name, username, password, date_of_birth)
                SELECT 'System Admin', 'admin', 'admin123', '2000-01-01'
                WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private static int getDefaultAdminUserId(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE username = 'admin' LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        throw new SQLException("Default admin user not found.");
    }
}
