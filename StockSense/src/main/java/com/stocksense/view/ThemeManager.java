package com.stocksense.view;

import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

// This class keeps theme-related styling in one place.
// It switches simple colors for login and dashboard screens.
public class ThemeManager {
    private boolean darkMode;

    public void applyLoginTheme(VBox wrapper, VBox card, Button primaryButton, Label messageLabel) {
        if (darkMode) {
            wrapper.setStyle("-fx-font-family: 'Segoe UI'; -fx-background-color: #0f172a;");
            card.setStyle("-fx-background-color: #1e293b; -fx-padding: 30; -fx-background-radius: 10;");
            primaryButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 8;");
            messageLabel.setStyle("-fx-text-fill: #fca5a5;");
        } else {
            wrapper.setStyle("-fx-font-family: 'Segoe UI'; -fx-background-color: #f4f6fb;");
            card.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 10;");
            primaryButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 8;");
            messageLabel.setStyle("-fx-text-fill: #dc2626;");
        }
    }

    public void applyDashboardTheme(
            Region root,
            VBox sidebar,
            Region[] cards,
            Label[] labels,
            Button[] primaryButtons,
            Button[] dangerButtons,
            Control[] inputControls,
            TableView<?> tableView
    ) {
        String bg = darkMode ? "#0f172a" : "#f4f6fb";
        String text = darkMode ? "#e2e8f0" : "#1f2937";
        String sidebarBg = darkMode ? "#111827" : "#ffffff";
        String cardBg = darkMode ? "#1e293b" : "white";
        String inputBg = darkMode ? "#334155" : "white";
        String inputBorder = darkMode ? "#475569" : "#cbd5e1";

        root.setStyle("-fx-font-family: 'Segoe UI'; -fx-background-color: " + bg + ";");
        sidebar.setStyle("-fx-background-color: " + sidebarBg + "; -fx-padding: 16; -fx-spacing: 12;");

        for (Region card : cards) {
            card.setStyle("-fx-background-color: " + cardBg + "; -fx-padding: 16; -fx-background-radius: 10;");
        }
        for (Label label : labels) {
            label.setStyle("-fx-text-fill: " + text + ";");
        }
        for (Button button : primaryButtons) {
            button.setStyle("-fx-background-color: " + (darkMode ? "#3b82f6" : "#2563eb")
                    + "; -fx-text-fill: white; -fx-background-radius: 8;");
        }
        for (Button button : dangerButtons) {
            button.setStyle("-fx-background-color: " + (darkMode ? "#ef4444" : "#dc2626")
                    + "; -fx-text-fill: white; -fx-background-radius: 8;");
        }
        for (Control control : inputControls) {
            control.setStyle("-fx-background-color: " + inputBg + "; -fx-text-fill: " + text
                    + "; -fx-border-color: " + inputBorder + ";");
        }

        tableView.setStyle("-fx-control-inner-background: " + inputBg + "; -fx-background-color: " + cardBg
                + "; -fx-text-fill: " + text + ";");
    }

    public void toggleTheme() {
        darkMode = !darkMode;
    }

    public boolean isDarkMode() {
        return darkMode;
    }
}
