package com.stocksense;

import com.stocksense.controller.AuthController;
import com.stocksense.controller.ProductController;
import com.stocksense.db.DatabaseManager;
import com.stocksense.view.DashboardView;
import com.stocksense.view.LoginView;
import javafx.application.Application;
import javafx.stage.Stage;

// This is the main starting file of the project.
// It sets up the database and opens the main screens of the app.
public class MainApp extends Application {
    private final AuthController authController = new AuthController();
    private final ProductController productController = new ProductController(authController);

    @Override
    public void start(Stage primaryStage) {
        // Preparing database and default user before showing any UI.
        DatabaseManager.initializeDatabase();
        authController.initializeDefaultUser();
        showLogin(primaryStage);
    }

    private void showLogin(Stage stage) {
        LoginView loginView = new LoginView(stage, authController);
        loginView.show(() -> showDashboard(stage));
    }

    private void showDashboard(Stage stage) {
        DashboardView dashboardView = new DashboardView(stage, productController, authController);
        dashboardView.show(() -> {
            // Clearing the current session when user logs out.
            authController.logout();
            showLogin(stage);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
