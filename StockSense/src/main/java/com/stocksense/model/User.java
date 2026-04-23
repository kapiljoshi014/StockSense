package com.stocksense.model;

import java.time.LocalDate;

// This model stores user information for login and profile features.
// It keeps simple data that is read from or written to the users table.
public class User {
    private int id;
    private String fullName;
    private String username;
    private String password;
    private LocalDate dateOfBirth;

    public User() {
    }

    public User(int id, String fullName, String username, String password, LocalDate dateOfBirth) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
    }

    public User(String fullName, String username, String password, LocalDate dateOfBirth) {
        this(0, fullName, username, password, dateOfBirth);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
