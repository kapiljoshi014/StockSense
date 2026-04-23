package com.stocksense.controller;

import com.stocksense.dao.ProductDAO;
import com.stocksense.model.Product;
import com.stocksense.model.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

// This controller manages product-related work for the current logged-in user.
// It validates product input and sends safe requests to the product DAO.
public class ProductController {
    private final ProductDAO productDAO;
    private final AuthController authController;

    public ProductController(AuthController authController) {
        this.productDAO = new ProductDAO();
        this.authController = authController;
    }

    public void addProduct(Product product) throws SQLException {
        validateProduct(product);
        // Product is always linked to the current user before saving.
        product.setUserId(getCurrentUserId());
        productDAO.addProduct(product);
    }

    public void updateProduct(Product product) throws SQLException {
        validateProduct(product);
        product.setUserId(getCurrentUserId());
        productDAO.updateProduct(product);
    }

    public void deleteProduct(Product product) throws SQLException {
        if (product == null) {
            throw new IllegalArgumentException("Select a product first.");
        }
        productDAO.deleteProduct(product.getId(), getCurrentUserId());
    }

    public List<Product> getAllProducts() throws SQLException {
        // Every product fetch is user-based, so users only see their own data.
        return productDAO.getAllProducts(getCurrentUserId());
    }

    public long getTotalProducts() throws SQLException {
        return getAllProducts().size();
    }

    public long getExpiredCount() throws SQLException {
        return getAllProducts().stream().filter(p -> "Expired / Critical".equals(p.getStatus())).count();
    }

    public long getNeedAttentionCount() throws SQLException {
        return getAllProducts().stream().filter(p -> "Need Attention".equals(p.getStatus())).count();
    }

    public long getExpiringSoonCount() throws SQLException {
        return getAllProducts().stream().filter(p -> "Expiring Soon".equals(p.getStatus())).count();
    }

    private void validateProduct(Product product) {
        // Keeping basic checks here makes the UI code simpler.
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required.");
        }
        if (product.getCategory() == null || product.getCategory().isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (product.getBatchNumber() == null || product.getBatchNumber().isBlank()) {
            throw new IllegalArgumentException("Batch number is required.");
        }
        if (product.getManufactureDate() == null || product.getExpiryDate() == null) {
            throw new IllegalArgumentException("Manufacture and expiry dates are required.");
        }
        LocalDate mfg = product.getManufactureDate();
        LocalDate exp = product.getExpiryDate();
        if (exp.isBefore(mfg)) {
            throw new IllegalArgumentException("Expiry date cannot be before manufacture date.");
        }
    }

    private int getCurrentUserId() {
        User currentUser = authController.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("No active user session.");
        }
        // This id is used in every product query to keep data separate user by user.
        return currentUser.getId();
    }
}
