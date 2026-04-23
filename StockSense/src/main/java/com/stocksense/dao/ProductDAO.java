package com.stocksense.dao;

import com.stocksense.db.DatabaseManager;
import com.stocksense.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// This DAO handles all database queries related to products.
// It stores and reads product records from SQLite.
public class ProductDAO {
    public void addProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products(user_id, name, category, batch_number, manufacture_date, expiry_date) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Saving user_id here keeps products separated for each account.
            ps.setInt(1, product.getUserId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getCategory());
            ps.setString(4, product.getBatchNumber());
            ps.setString(5, product.getManufactureDate().toString());
            ps.setString(6, product.getExpiryDate().toString());
            ps.executeUpdate();
        }
    }

    public void updateProduct(Product product) throws SQLException {
        String sql = """
                UPDATE products
                SET name = ?, category = ?, batch_number = ?, manufacture_date = ?, expiry_date = ?
                WHERE id = ? AND user_id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Update only works for the matching user, not for other users' data.
            ps.setString(1, product.getName());
            ps.setString(2, product.getCategory());
            ps.setString(3, product.getBatchNumber());
            ps.setString(4, product.getManufactureDate().toString());
            ps.setString(5, product.getExpiryDate().toString());
            ps.setInt(6, product.getId());
            ps.setInt(7, product.getUserId());
            ps.executeUpdate();
        }
    }

    public void deleteProduct(int id, int userId) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public List<Product> getAllProducts(int userId) throws SQLException {
        String sql = "SELECT * FROM products WHERE user_id = ? ORDER BY expiry_date ASC";
        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                // Reading all rows first and turning them into Product objects.
                while (rs.next()) {
                    Product product = new Product(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getString("batch_number"),
                            LocalDate.parse(rs.getString("manufacture_date")),
                            LocalDate.parse(rs.getString("expiry_date"))
                    );
                    products.add(product);
                }
            }
        }
        return products;
    }
}
