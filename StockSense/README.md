# Stock Sense - Inventory Expiry Tracker

Stock Sense is a Java desktop application built with JavaFX and SQLite. It helps users manage product batches, track expiry timelines, and view only their own inventory data through a clean dark-themed interface.

## Features

- User authentication with SQLite:
  - Login
  - New user registration
  - Forgot password with date-of-birth verification
  - Default seeded account: `admin` / `admin123`
- User-specific product data:
  - Each logged-in user sees only their own products
  - Products are stored with `user_id`
- Product management:
  - Add product
  - Update product
  - Delete product
  - View products by batch
- Expiry status logic:
  - `Safe` for products with more than 60 days left
  - `Need Attention` for products with 30 to 60 days left
  - `Expiring Soon` for products with 7 to 30 days left
  - `Expired / Critical` for products with 7 days or less left
- Dashboard summary cards:
  - Total Products
  - Safe
  - Need Attention
  - Expiring Soon
  - Expired
- Product Information page:
  - Batch-wise product cards
  - Filtered product table
- Batch Management page:
  - Select a batch
  - Edit products
  - Delete products
- Smart Assistant page:
  - Full-page chatbot interface
  - Answers simple inventory questions using real product data
  - Supports keywords like `expired`, `attention`, `total`, and `safe`
- About Us page:
  - Project description
  - Team contact emails
- Modern JavaFX UI:
  - Dark theme
  - Animated hover effects
  - Dashboard card icons
  - Styled tables, forms, and chat bubbles

## Tech Stack

- Java 17+
- JavaFX
- SQLite (Xerial JDBC)
- Maven Wrapper

## Project Structure

```text
StockSense/
|-- pom.xml
|-- mvnw
|-- mvnw.cmd
|-- stocksense.db
`-- src/
    `-- main/
        |-- java/
        |   `-- com/stocksense/
        |       |-- MainApp.java
        |       |-- controller/
        |       |   |-- AuthController.java
        |       |   `-- ProductController.java
        |       |-- dao/
        |       |   |-- ProductDAO.java
        |       |   `-- UserDAO.java
        |       |-- db/
        |       |   `-- DatabaseManager.java
        |       |-- model/
        |       |   |-- Product.java
        |       |   `-- User.java
        |       `-- view/
        |           |-- DashboardView.java
        |           |-- LoginView.java
        |           `-- ThemeManager.java
        `-- resources/
            |-- dark.css
            `-- images/
                `-- login-bg.png
```

## Run Instructions

1. Make sure Java 17 or above is installed.
2. Open terminal inside the project folder.
3. Run with Maven Wrapper:

- PowerShell:
  - `.\mvnw.cmd javafx:run`
- Command Prompt:
  - `mvnw.cmd javafx:run`

If you want a fresh rebuild first:

- PowerShell:
  - `.\mvnw.cmd clean javafx:run`
- Command Prompt:
  - `mvnw.cmd clean javafx:run`

## Important

- Do not run `javafx:run` by itself.
- `javafx:run` is a Maven goal, so it must be run through Maven Wrapper.
- In PowerShell, always use `.\mvnw.cmd` instead of just `mvnw.cmd`.

## Notes

- The database file is `stocksense.db`.
- Tables are created automatically on startup.
- Older product data is migrated to support `user_id`.
- The dashboard, product tables, and chatbot all use the same expiry status logic.
- The project uses pure JavaFX only and does not depend on external UI libraries.
