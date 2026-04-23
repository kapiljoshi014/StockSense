package com.stocksense.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// This model stores product data used across the app.
// It also calculates expiry-related values like days left and status.
public class Product {
    private int id;
    private int userId;
    private String name;
    private String category;
    private String batchNumber;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;

    public Product() {
    }

    public Product(int id, int userId, String name, String category, String batchNumber, LocalDate manufactureDate, LocalDate expiryDate) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.category = category;
        this.batchNumber = batchNumber;
        this.manufactureDate = manufactureDate;
        this.expiryDate = expiryDate;
    }

    public Product(int id, String name, String category, String batchNumber, LocalDate manufactureDate, LocalDate expiryDate) {
        this(id, 0, name, category, batchNumber, manufactureDate, expiryDate);
    }

    public Product(String name, String category, String batchNumber, LocalDate manufactureDate, LocalDate expiryDate) {
        this(0, 0, name, category, batchNumber, manufactureDate, expiryDate);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDate getManufactureDate() {
        return manufactureDate;
    }

    public void setManufactureDate(LocalDate manufactureDate) {
        this.manufactureDate = manufactureDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public long getDaysLeft() {
        // This method calculates days left before expiry.
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    public String getStatus() {
        // Status is calculated here so the dashboard and assistant use the same logic.
        long daysLeft = getDaysLeft();
        if (daysLeft <= 7) {
            return "Expired / Critical";
        }
        if (daysLeft <= 30) {
            return "Expiring Soon";
        }
        if (daysLeft <= 60) {
            return "Need Attention";
        }
        return "Safe";
    }
}
