package com.stocksense.view;

import com.stocksense.controller.AuthController;
import com.stocksense.controller.ProductController;
import com.stocksense.model.Product;
import com.stocksense.model.User;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.Desktop;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

// This is the main dashboard and feature page file of the project.
// It controls the sidebar, cards, tables, chatbot page, and main screens after login.
public class DashboardView {
    private static final String APP_BG = "#06111f";
    private static final String SIDEBAR_BG = "#091427";
    private static final String PANEL_BG = "#0d1a31";
    private static final String PANEL_BG_ALT = "#12223d";
    private static final String PANEL_BG_HOVER = "#182b4d";
    private static final String TABLE_HEADER_BG = "#172948";
    private static final String INPUT_FOCUS = "#60a5fa";
    private static final String BORDER = "rgba(148,163,184,0.18)";
    private static final String TEXT_PRIMARY = "#f8fafc";
    private static final String TEXT_SECONDARY = "#b7c7dd";
    private static final String TEXT_MUTED = "rgba(226,232,240,0.78)";
    private static final String ACCENT_BLUE = "#38bdf8";
    private static final String ACCENT_PURPLE = "#7c3aed";
    private static final String ACCENT_BLUE_SOFT = "#2563eb";
    private static final String DANGER = "#ef4444";
    private static final String WARNING = "#facc15";
    private static final String WARNING_ORANGE = "#fb923c";
    private static final String SUCCESS = "#22c55e";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final String STATUS_SAFE = "Safe";
    private static final String STATUS_NEED_ATTENTION = "Need Attention";
    private static final String STATUS_EXPIRING_SOON = "Expiring Soon";
    private static final String STATUS_CRITICAL = "Expired / Critical";

    private final Stage stage;
    private final ProductController productController;
    private final AuthController authController;

    private final ObservableList<Product> allProducts = FXCollections.observableArrayList();
    private final ObservableList<Product> filteredProducts = FXCollections.observableArrayList();
    private final ObservableList<Product> batchProducts = FXCollections.observableArrayList();
    private final ObservableList<Product> managedProducts = FXCollections.observableArrayList();
    private final ObservableList<String> batchItems = FXCollections.observableArrayList();

    private final TableView<Product> dashboardTable = createProductTable(true);
    private final TableView<Product> productInfoTable = createProductTable(false);
    private final TableView<Product> batchManagementTable = createProductTable(false);
    private final FlowPane batchCardsPane = new FlowPane();
    private final ComboBox<String> batchSelector = new ComboBox<>();
    private final StackPane contentPane = new StackPane();
    private final VBox chatMessagesBox = new VBox(14);
    private final ScrollPane chatScrollPane = new ScrollPane();
    private final TextArea chatInputArea = new TextArea();

    private final Label totalLabel = createMetricValue();
    private final Label safeLabel = createMetricValue();
    private final Label needAttentionLabel = createMetricValue();
    private final Label expiringSoonLabel = createMetricValue();
    private final Label expiredLabel = createMetricValue();
    private final Label pageTitle = new Label("Dashboard");
    private final Label pageSubtitle = new Label("Monitor stock health with a clear overview.");
    private final Label userBadge = new Label("Not logged in");
    private final Label productInfoFilterLabel = new Label("Showing all products");

    private final TextField settingsNameField = new TextField();
    private final TextField settingsUsernameField = new TextField();
    private final DatePicker settingsDobPicker = new DatePicker();

    private final Map<String, Button> navigationButtons = new LinkedHashMap<>();

    private Runnable onLogout;
    private String currentSection = "Dashboard";
    private String selectedBatch;
    private String selectedStatusFilter;

    public DashboardView(Stage stage, ProductController productController, AuthController authController) {
        this.stage = stage;
        this.productController = productController;
        this.authController = authController;
    }

    public void show(Runnable onLogout) {
        this.onLogout = onLogout;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + APP_BG + "; -fx-font-family: 'Segoe UI';");
        root.setLeft(buildSidebar());
        root.setCenter(buildMainArea());

        Scene scene = new Scene(root, 1420, 860);
        stage.setTitle("Stock Sense - Inventory Expiry Tracker");
        stage.setScene(scene);
        stage.show();

        // Loading fresh data before opening the first page.
        refreshData();
        showSection("Dashboard");
    }

    private VBox buildSidebar() {
        Label appTitle = new Label("Stock Sense");
        appTitle.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 24px; -fx-font-weight: 900;");

        Label subtitle = new Label("Smart inventory lifecycle tracking");
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px; -fx-line-spacing: 3px;");

        VBox navBox = new VBox(14,
                createNavButton("Dashboard"),
                createNavButton("Product Information"),
                createNavButton("Batch Management"),
                createNavButton("Settings"),
                createNavButton("About Us"),
                createNavButton("Smart Assistant")
        );
        navBox.setFillWidth(true);

        Button logoutButton = createActionButton("Logout", "rgba(239,68,68,0.16)", "#fecaca", false);
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setPrefHeight(48);
        logoutButton.setOnAction(e -> onLogout.run());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox sidebar = new VBox(20, appTitle, subtitle, navBox, spacer, logoutButton);
        sidebar.setPadding(new Insets(28, 22, 28, 22));
        sidebar.setPrefWidth(250);
        sidebar.setStyle(
                "-fx-background-color: " + SIDEBAR_BG + ";"
                        + "-fx-border-color: rgba(148,163,184,0.10);"
                        + "-fx-border-width: 0 1 0 0;"
        );
        return sidebar;
    }

    private BorderPane buildMainArea() {
        pageTitle.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 30px; -fx-font-weight: 900;");
        pageSubtitle.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 13px;");

        userBadge.setStyle(
                "-fx-text-fill: " + TEXT_PRIMARY + ";"
                        + "-fx-font-size: 12px;"
                        + "-fx-font-weight: 800;"
                        + "-fx-padding: 11 16 11 16;"
                        + "-fx-background-color: rgba(124,58,237,0.18);"
                        + "-fx-background-radius: 999;"
                        + "-fx-border-color: rgba(56,189,248,0.24);"
                        + "-fx-border-radius: 999;"
        );

        VBox titleBox = new VBox(5, pageTitle, pageSubtitle);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(16, titleBox, spacer, userBadge);
        topBar.setAlignment(Pos.CENTER_LEFT);

        BorderPane container = new BorderPane();
        container.setTop(topBar);
        container.setCenter(contentPane);
        contentPane.setStyle("-fx-background-color: transparent;");
        BorderPane.setMargin(topBar, new Insets(24, 28, 14, 28));
        BorderPane.setMargin(contentPane, new Insets(0, 28, 24, 28));
        return container;
    }

    private void showSection(String section) {
        currentSection = section;
        navigationButtons.forEach((name, button) -> applyNavButtonStyle(button, name.equals(section), false));

        // Switching the main area based on the selected sidebar page.
        switch (section) {
            case "Dashboard" -> {
                pageTitle.setText("Dashboard");
                pageSubtitle.setText("Track inventory health and jump straight to the products that need action.");
                contentPane.getChildren().setAll(buildDashboardSection());
            }
            case "Product Information" -> {
                pageTitle.setText("Product Information");
                pageSubtitle.setText("Explore batches as cards and inspect filtered product details in a readable table.");
                contentPane.getChildren().setAll(buildProductInfoSection());
            }
            case "Batch Management" -> {
                pageTitle.setText("Batch Management");
                pageSubtitle.setText("Select a batch, then edit or delete products while keeping the layout clean.");
                contentPane.getChildren().setAll(buildBatchManagementSection());
            }
            case "Settings" -> {
                pageTitle.setText("Settings");
                pageSubtitle.setText("Manage profile details with a clean, high-contrast settings experience.");
                contentPane.getChildren().setAll(buildSettingsSection());
            }
            case "About Us" -> {
                pageTitle.setText("About Us");
                pageSubtitle.setText("Learn about Stock Sense, our mission, and how to reach the team.");
                contentPane.getChildren().setAll(buildAboutUsSection());
            }
            case "Smart Assistant" -> {
                pageTitle.setText("Smart Assistant");
                pageSubtitle.setText("Ask simple inventory questions in natural language and get live answers from your product data.");
                contentPane.getChildren().setAll(buildSmartAssistantSection());
            }
            default -> throw new IllegalStateException("Unknown section: " + section);
        }
    }

    private VBox buildDashboardSection() {
        dashboardTable.setItems(filteredProducts);
        filteredProducts.setAll(allProducts);

        HBox metricRow = new HBox(16,
                createMetricCard("Total Products", totalLabel, "All inventory items currently monitored.", "linear-gradient(to bottom right, rgba(56,189,248,0.24), rgba(124,58,237,0.18))", ACCENT_BLUE, null),
                createMetricCard("Safe", safeLabel, "Products with more than 60 days remaining.", "linear-gradient(to bottom right, rgba(34,197,94,0.28), rgba(22,163,74,0.16))", SUCCESS, STATUS_SAFE),
                createMetricCard("Need Attention", needAttentionLabel, "Products between 30 and 60 days left.", "linear-gradient(to bottom right, rgba(251,146,60,0.28), rgba(249,115,22,0.16))", WARNING_ORANGE, STATUS_NEED_ATTENTION),
                createMetricCard("Expiring Soon", expiringSoonLabel, "Products between 7 and 30 days left.", "linear-gradient(to bottom right, rgba(250,204,21,0.30), rgba(234,179,8,0.16))", WARNING, STATUS_EXPIRING_SOON),
                createMetricCard("Expired", expiredLabel, "Products with 7 days or less remaining.", "linear-gradient(to bottom right, rgba(239,68,68,0.30), rgba(185,28,28,0.16))", DANGER, STATUS_CRITICAL)
        );
        metricRow.setAlignment(Pos.CENTER_LEFT);

        Button addButton = createActionButton("Add Product", ACCENT_BLUE, TEXT_PRIMARY, true);
        addButton.setOnAction(e -> openProductDialog(null));

        Button refreshButton = createActionButton("Refresh", PANEL_BG_ALT, TEXT_PRIMARY, false);
        refreshButton.setOnAction(e -> refreshData());

        HBox actions = new HBox(12, addButton, refreshButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox section = new VBox(18, metricRow, createPanel("Inventory Snapshot", "All products with the latest shelf-life classification and readable table styling.", dashboardTable), actions);
        VBox.setVgrow(section.getChildren().get(1), Priority.ALWAYS);
        return section;
    }

    private HBox buildProductInfoSection() {
        batchCardsPane.setHgap(16);
        batchCardsPane.setVgap(16);
        batchCardsPane.setPrefWrapLength(370);
        batchCardsPane.setPadding(new Insets(4));

        productInfoFilterLabel.setStyle(
                "-fx-text-fill: " + TEXT_PRIMARY + ";"
                        + "-fx-font-size: 12px;"
                        + "-fx-font-weight: 800;"
                        + "-fx-padding: 8 12 8 12;"
                        + "-fx-background-color: rgba(56,189,248,0.12);"
                        + "-fx-background-radius: 999;"
                        + "-fx-border-color: rgba(56,189,248,0.22);"
                        + "-fx-border-radius: 999;"
        );

        ScrollPane batchScroller = new ScrollPane(batchCardsPane);
        batchScroller.setFitToWidth(true);
        batchScroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        batchScroller.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        batchScroller.setPrefViewportHeight(530);

        VBox batchCardContent = new VBox(12, productInfoFilterLabel, batchScroller);
        VBox.setVgrow(batchScroller, Priority.ALWAYS);

        productInfoTable.setItems(batchProducts);

        VBox batchPanel = createPanel("Batches", "Select a batch card to see only the products within that group.", batchCardContent);
        batchPanel.setPrefWidth(420);

        VBox productPanel = createPanel("Products In Selected Batch", "Readable table with status-aware highlighting and days-left visibility.", productInfoTable);
        HBox.setHgrow(productPanel, Priority.ALWAYS);

        return new HBox(18, batchPanel, productPanel);
    }

    private VBox buildBatchManagementSection() {
        Label selectorLabel = createFormLabel("Select Batch");

        styleInput(batchSelector);
        batchSelector.setItems(batchItems);
        batchSelector.setPromptText("Choose a batch");
        batchSelector.setOnAction(e -> {
            selectedBatch = batchSelector.getValue();
            updateManagedProducts(selectedBatch);
            rebuildBatchCards();
        });

        Button editButton = createActionButton("Edit", ACCENT_PURPLE, TEXT_PRIMARY, true);
        editButton.setOnAction(e -> {
            Product selected = batchManagementTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Select a product from the table to edit.");
                return;
            }
            openProductDialog(selected);
        });

        Button deleteButton = createActionButton("Delete", "#7f1d1d", "#fee2e2", false);
        deleteButton.setOnAction(e -> deleteSelectedProduct(batchManagementTable.getSelectionModel().getSelectedItem()));

        Button addButton = createActionButton("Add Product", ACCENT_BLUE, TEXT_PRIMARY, true);
        addButton.setOnAction(e -> openProductDialog(prefillBatch(batchSelector.getValue())));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox controls = new HBox(12, selectorLabel, batchSelector, spacer, addButton, editButton, deleteButton);
        controls.setAlignment(Pos.CENTER_LEFT);

        batchManagementTable.setItems(managedProducts);

        VBox section = new VBox(18, controls, createPanel("Batch Product Management", "Select a batch and manage the products inside it with a clean, consistent layout.", batchManagementTable));
        VBox.setVgrow(section.getChildren().get(1), Priority.ALWAYS);
        return section;
    }

    private VBox buildSettingsSection() {
        styleInput(settingsNameField);
        styleInput(settingsUsernameField);
        styleInput(settingsDobPicker);

        settingsNameField.setPromptText("Full Name");
        settingsUsernameField.setPromptText("Username");
        settingsDobPicker.setPromptText("Date of Birth");

        Label profileTitle = createSectionTitle("Profile");
        Label profileCopy = createSecondaryBody("Manage the account details used to sign in and identify the current Stock Sense user.");

        GridPane profileGrid = new GridPane();
        profileGrid.setHgap(14);
        profileGrid.setVgap(12);
        profileGrid.add(createFormLabel("Name"), 0, 0);
        profileGrid.add(settingsNameField, 0, 1);
        profileGrid.add(createFormLabel("Username"), 1, 0);
        profileGrid.add(settingsUsernameField, 1, 1);
        profileGrid.add(createFormLabel("Date of Birth"), 0, 2);
        profileGrid.add(settingsDobPicker, 0, 3);

        Button saveSettingsButton = createActionButton("Save Settings", ACCENT_BLUE, TEXT_PRIMARY, true);
        saveSettingsButton.setOnAction(e -> saveSettings());

        VBox tipsCard = createInfoCard(
                createSectionTitle("Display & Visibility"),
                createBody("The dark theme now uses higher-contrast text, bold table headers, clearer placeholders, and stronger focus borders so content stays readable across every page."),
                createBody("Batch cards, buttons, summary cards, and emails also respond visually on hover and click for a cleaner, more modern experience.")
        );

        VBox profileCard = createInfoCard(profileTitle, profileCopy, profileGrid, saveSettingsButton);

        GridPane settingsGrid = new GridPane();
        settingsGrid.setHgap(18);
        settingsGrid.setVgap(18);
        settingsGrid.add(profileCard, 0, 0);
        settingsGrid.add(tipsCard, 1, 0);
        GridPane.setHgrow(profileCard, Priority.ALWAYS);
        GridPane.setHgrow(tipsCard, Priority.ALWAYS);

        populateSettings();

        VBox wrapper = new VBox(settingsGrid);
        VBox.setVgrow(settingsGrid, Priority.ALWAYS);
        return wrapper;
    }

    private VBox buildAboutUsSection() {
        VBox aboutStockSenseCard = createInfoCard(
                createSectionTitle("About Stock Sense"),
                createBody("Stock Sense is an intelligent inventory management system designed to minimize losses caused by product expiration and inefficient tracking. It provides real-time visibility into stock health, categorizes items based on urgency, and enables smarter decision-making through structured batch management. By combining simplicity with intelligent insights, Stock Sense helps businesses maintain control, reduce waste, and improve operational efficiency.")
        );

        VBox aboutUsCard = createInfoCard(
                createSectionTitle("About Us"),
                createBody("We are a team of developers focused on building practical and efficient software solutions. Stock Sense is designed to solve real-world inventory challenges by combining simplicity with smart tracking features.")
        );

        VBox contactCard = createInfoCard(
                createSectionTitle("Contact"),
                createSecondaryBody("Every team email below is highlighted equally and remains fully interactive."),
                createEmailLink("kapildev75001@gmail.com"),
                createEmailLink("adityarao8707@gmail.com"),
                createEmailLink("Aryanthakur66556@gmail.com"),
                createEmailLink("manishkumar99210@gmail.com"),
                createEmailLink("abhishek.s.builds@gmail.com")
        );

        GridPane aboutGrid = new GridPane();
        aboutGrid.setHgap(18);
        aboutGrid.setVgap(18);
        aboutGrid.add(aboutStockSenseCard, 0, 0);
        aboutGrid.add(aboutUsCard, 1, 0);
        aboutGrid.add(contactCard, 0, 1, 2, 1);
        GridPane.setHgrow(aboutStockSenseCard, Priority.ALWAYS);
        GridPane.setHgrow(aboutUsCard, Priority.ALWAYS);
        GridPane.setHgrow(contactCard, Priority.ALWAYS);

        VBox wrapper = new VBox(aboutGrid);
        VBox.setVgrow(aboutGrid, Priority.ALWAYS);
        return wrapper;
    }

    private VBox buildSmartAssistantSection() {
        chatMessagesBox.setPadding(new Insets(6));
        chatMessagesBox.setFillWidth(true);

        chatScrollPane.setContent(chatMessagesBox);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        if (chatMessagesBox.getChildren().isEmpty()) {
            // One starter line makes the assistant page feel ready to use.
            addBotMessage("Hello. Ask me about: expired, attention, total, safe.");
        }

        chatInputArea.setPromptText("Ask about inventory health, totals, expired products, safe products...");
        chatInputArea.setWrapText(true);
        chatInputArea.setPrefRowCount(2);
        chatInputArea.setMinHeight(52);
        chatInputArea.setMaxHeight(90);
        styleChatInput(chatInputArea);

        Button sendButton = createActionButton("Send", ACCENT_BLUE, TEXT_PRIMARY, true);
        sendButton.setPrefHeight(52);
        sendButton.setOnAction(e -> sendChatMessage());

        HBox composer = new HBox(12, chatInputArea, sendButton);
        composer.setAlignment(Pos.BOTTOM_CENTER);
        HBox.setHgrow(chatInputArea, Priority.ALWAYS);

        VBox chatPage = new VBox(18,
                createPanel("Conversation", "Use natural keywords like expired, attention, total, or safe to query your inventory.", chatScrollPane),
                composer
        );
        VBox.setVgrow(chatPage.getChildren().get(0), Priority.ALWAYS);
        return chatPage;
    }

    private VBox createMetricCard(String title, Label valueLabel, String copy, String backgroundStyle, String glowColor, String statusFilter) {
        SVGPath icon = createDashboardIcon(title, glowColor);
        StackPane iconWrap = new StackPane(icon);
        iconWrap.setMinSize(44, 44);
        iconWrap.setPrefSize(44, 44);
        iconWrap.setMaxSize(44, 44);
        iconWrap.setStyle(
                "-fx-background-color: rgba(6,17,31,0.22);"
                        + "-fx-background-radius: 14;"
                        + "-fx-border-color: rgba(255,255,255,0.10);"
                        + "-fx-border-radius: 14;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: rgba(248,250,252,0.95); -fx-font-size: 12px; -fx-font-weight: 900;");

        Label copyLabel = new Label(copy);
        copyLabel.setWrapText(true);
        copyLabel.setStyle("-fx-text-fill: rgba(248,250,252,0.82); -fx-font-size: 11px; -fx-line-spacing: 2px;");

        VBox textBlock = new VBox(4, titleLabel, valueLabel);
        HBox headerRow = new HBox(12, iconWrap, textBlock);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(12, headerRow, copyLabel);
        card.setPadding(new Insets(18));
        card.setPrefWidth(188);
        card.setMinHeight(142);
        card.setCursor(Cursor.HAND);
        card.setStyle(
                "-fx-background-color: " + backgroundStyle + ";"
                        + "-fx-background-radius: 24;"
                        + "-fx-border-color: rgba(255,255,255,0.10);"
                        + "-fx-border-radius: 24;"
        );
        card.setEffect(new DropShadow(20, Color.web(glowColor, 0.22)));
        installHoverCard(card, glowColor);
        card.setOnMouseClicked(e -> openProductInformationWithFilter(statusFilter));
        return card;
    }

    private SVGPath createDashboardIcon(String title, String color) {
        SVGPath icon = new SVGPath();
        switch (title) {
            case "Total Products" ->
                    icon.setContent("M4 7 L12 3 L20 7 L20 17 L12 21 L4 17 Z M4 7 L12 11 L20 7 M12 11 L12 21");
            case "Safe" ->
                    icon.setContent("M12 3 L20 6 V11 C20 16 16.8 20.2 12 21 C7.2 20.2 4 16 4 11 V6 Z M8 11.5 L11 14.5 L16 9.5");
            case "Need Attention" ->
                    icon.setContent("M12 4 L20 19 H4 Z M12 9 V13 M12 16 H12.01");
            case "Expiring Soon" ->
                    icon.setContent("M12 4 A8 8 0 1 1 11.99 4 M12 8 V12 L15 14");
            case "Expired" ->
                    icon.setContent("M12 4 A8 8 0 1 1 11.99 4 M9 9 L15 15 M15 9 L9 15");
            default ->
                    icon.setContent("M12 4 A8 8 0 1 1 11.99 4");
        }
        icon.setFill(null);
        icon.setStroke(Color.web(color));
        icon.setStrokeWidth(1.8);
        icon.setScaleX(1.15);
        icon.setScaleY(1.15);
        return icon;
    }

    private VBox createPanel(String title, String subtitle, Node content) {
        Label heading = createSectionTitle(title);
        Label copy = createSecondaryBody(subtitle);

        VBox wrapper = new VBox(14, heading, copy, content);
        wrapper.setPadding(new Insets(22));
        wrapper.setStyle(panelStyle(PANEL_BG));
        VBox.setVgrow(content, Priority.ALWAYS);
        if (content instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        return wrapper;
    }

    private VBox createInfoCard(Node... content) {
        VBox card = new VBox(14, content);
        card.setPadding(new Insets(22));
        card.setStyle(panelStyle(PANEL_BG));
        card.setMinWidth(0);
        return card;
    }

    private void sendChatMessage() {
        String userText = chatInputArea.getText() == null ? "" : chatInputArea.getText().trim();
        if (userText.isEmpty()) {
            return;
        }

        // Show user text first, then delay the bot reply for a simple typing feel.
        addUserMessage(userText);
        chatInputArea.clear();

        HBox typingRow = addBotMessage("Bot is typing...");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> replaceTypingMessage(typingRow, buildAssistantResponse(userText)));
        pause.play();
    }

    private String buildAssistantResponse(String input) {
        String normalized = input.toLowerCase();
        // Handling chatbot input with simple keyword matching.
        if (normalized.contains("expire") || normalized.contains("expired") || normalized.contains("expiring")) {
            List<Product> expiringProducts = allProducts.stream()
                    .filter(product -> STATUS_CRITICAL.equals(product.getStatus()) || STATUS_EXPIRING_SOON.equals(product.getStatus()))
                    .sorted(Comparator.comparing(Product::getDaysLeft).thenComparing(Product::getName))
                    .toList();
            if (expiringProducts.isEmpty()) {
                return "There are no expired or expiring-soon products right now.";
            }
            String productList = expiringProducts.stream()
                    .limit(6)
                    .map(product -> product.getName() + " (" + product.getBatchNumber() + ", " + product.getDaysLeft() + " days left)")
                    .collect(Collectors.joining(", "));
            return "You have " + expiringProducts.size() + " expired or expiring-soon product(s): " + productList + ".";
        }
        if (normalized.contains("attention")) {
            long count = allProducts.stream().filter(product -> STATUS_NEED_ATTENTION.equals(product.getStatus())).count();
            return "Products needing attention (30–60 days left): " + count + ".";
        }
        if (normalized.contains("total")) {
            return "Total products in your inventory: " + allProducts.size() + ".";
        }
        if (normalized.contains("safe")) {
            long count = allProducts.stream().filter(product -> STATUS_SAFE.equals(product.getStatus())).count();
            return "Safe products with more than 60 days left: " + count + ".";
        }
        return "Sorry, I didn't understand. Try: expired, attention, total, safe.";
    }

    private HBox addUserMessage(String text) {
        return addChatMessage(text, true);
    }

    private HBox addBotMessage(String text) {
        return addChatMessage(text, false);
    }

    private HBox addChatMessage(String text, boolean isUser) {
        Label message = new Label(text);
        message.setWrapText(true);
        message.setMaxWidth(480);
        message.setStyle(
                "-fx-text-fill: " + TEXT_PRIMARY + ";"
                        + "-fx-font-size: 13px;"
                        + "-fx-font-weight: 700;"
                        + "-fx-padding: 12 16 12 16;"
                        + "-fx-background-color: " + (isUser
                        ? "linear-gradient(to right, rgba(56,189,248,0.90), rgba(124,58,237,0.86))"
                        : "rgba(18,34,61,0.96)") + ";"
                        + "-fx-background-radius: 18;"
                        + "-fx-border-color: " + (isUser ? "rgba(56,189,248,0.25)" : "rgba(148,163,184,0.18)") + ";"
                        + "-fx-border-radius: 18;"
        );

        HBox row = new HBox(message);
        row.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        row.setPadding(new Insets(0, 4, 0, 4));
        chatMessagesBox.getChildren().add(row);

        FadeTransition fade = new FadeTransition(Duration.millis(180), row);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        smoothScrollToLatest();
        return row;
    }

    private void replaceTypingMessage(HBox typingRow, String response) {
        chatMessagesBox.getChildren().remove(typingRow);
        addBotMessage(response);
    }

    private void smoothScrollToLatest() {
        PauseTransition pause = new PauseTransition(Duration.millis(60));
        pause.setOnFinished(e -> chatScrollPane.setVvalue(1.0));
        pause.play();
    }

    private TableView<Product> createProductTable(boolean includeBatchColumn) {
        TableView<Product> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(createSecondaryBody("No products to show yet."));
        table.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-control-inner-background: " + PANEL_BG_ALT + ";"
                        + "-fx-table-cell-border-color: rgba(148,163,184,0.10);"
                        + "-fx-text-fill: " + TEXT_PRIMARY + ";"
                        + "-fx-selection-bar: rgba(56,189,248,0.32);"
                        + "-fx-selection-bar-non-focused: rgba(56,189,248,0.18);"
        );

        TableColumn<Product, String> nameCol = createColumn("Product Name", Product::getName);
        TableColumn<Product, String> categoryCol = createColumn("Category", Product::getCategory);
        TableColumn<Product, String> batchCol = createColumn("Batch Number", Product::getBatchNumber);
        TableColumn<Product, String> mfgCol = createColumn("Manufacture Date", product -> formatDate(product.getManufactureDate()));
        TableColumn<Product, String> expCol = createColumn("Expiry Date", product -> formatDate(product.getExpiryDate()));
        TableColumn<Product, String> daysLeftCol = createColumn("Days Left", product -> String.valueOf(product.getDaysLeft()));

        styleTableColumn(nameCol);
        styleTableColumn(categoryCol);
        styleTableColumn(batchCol);
        styleTableColumn(mfgCol);
        styleTableColumn(expCol);
        styleTableColumn(daysLeftCol);

        table.getColumns().add(nameCol);
        table.getColumns().add(categoryCol);
        if (includeBatchColumn) {
            table.getColumns().add(batchCol);
        }
        table.getColumns().add(mfgCol);
        table.getColumns().add(expCol);
        table.getColumns().add(daysLeftCol);
        table.setRowFactory(tv -> createStatusAwareRow());
        return table;
    }

    private TableColumn<Product, String> createColumn(String title, Function<Product, String> mapper) {
        TableColumn<Product, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new SimpleStringProperty(mapper.apply(cell.getValue())));
        return column;
    }

    private void styleTableColumn(TableColumn<Product, String> column) {
        column.setStyle(
                "-fx-alignment: CENTER_LEFT;"
                        + "-fx-font-weight: 800;"
                        + "-fx-background-color: " + TABLE_HEADER_BG + ";"
                        + "-fx-text-fill: " + TEXT_PRIMARY + ";"
        );
    }

    private TableRow<Product> createStatusAwareRow() {
        return new TableRow<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: transparent;");
                    return;
                }

                String status = item.getStatus();
                if (STATUS_CRITICAL.equals(status)) {
                    setStyle("-fx-background-color: rgba(239,68,68,0.22); -fx-text-fill: " + TEXT_PRIMARY + ";");
                } else if (STATUS_NEED_ATTENTION.equals(status)) {
                    setStyle("-fx-background-color: rgba(251,146,60,0.22); -fx-text-fill: " + TEXT_PRIMARY + ";");
                } else if (STATUS_EXPIRING_SOON.equals(status)) {
                    setStyle("-fx-background-color: rgba(250,204,21,0.22); -fx-text-fill: " + TEXT_PRIMARY + ";");
                } else {
                    setStyle("-fx-background-color: rgba(34,197,94,0.12); -fx-text-fill: " + TEXT_PRIMARY + ";");
                }
            }
        };
    }

    private void refreshData() {
        try {
            // Reloading products so cards, tables, and chatbot all use the latest data.
            List<Product> loadedProducts = productController.getAllProducts().stream()
                    .sorted(Comparator.comparing(Product::getExpiryDate).thenComparing(Product::getBatchNumber).thenComparing(Product::getName))
                    .toList();

            allProducts.setAll(loadedProducts);
            totalLabel.setText(String.valueOf(loadedProducts.size()));
            safeLabel.setText(String.valueOf(loadedProducts.stream().filter(product -> STATUS_SAFE.equals(product.getStatus())).count()));
            needAttentionLabel.setText(String.valueOf(loadedProducts.stream().filter(product -> STATUS_NEED_ATTENTION.equals(product.getStatus())).count()));
            expiringSoonLabel.setText(String.valueOf(loadedProducts.stream().filter(product -> STATUS_EXPIRING_SOON.equals(product.getStatus())).count()));
            expiredLabel.setText(String.valueOf(loadedProducts.stream().filter(product -> STATUS_CRITICAL.equals(product.getStatus())).count()));

            applyProductFilter();
            updateBatchCollections();

            User currentUser = authController.getCurrentUser();
            if (currentUser != null) {
                userBadge.setText(currentUser.getFullName() + "  |  @" + currentUser.getUsername());
            }
            if ("Settings".equals(currentSection)) {
                populateSettings();
            }
        } catch (SQLException e) {
            showError("Could not load data: " + e.getMessage());
        }
    }

    private void openProductInformationWithFilter(String statusFilter) {
        selectedStatusFilter = statusFilter;
        applyProductFilter();
        updateBatchCollections();
        showSection("Product Information");
    }

    private void applyProductFilter() {
        // Filtering products based on the selected dashboard card.
        List<Product> products = selectedStatusFilter == null
                ? allProducts
                : allProducts.stream().filter(product -> selectedStatusFilter.equals(product.getStatus())).toList();
        filteredProducts.setAll(products);
        dashboardTable.setItems(allProducts);
        updateProductInfoFilterLabel();
    }

    private void updateProductInfoFilterLabel() {
        if (selectedStatusFilter == null) {
            productInfoFilterLabel.setText("Showing all products");
        } else {
            productInfoFilterLabel.setText("Filter: " + selectedStatusFilter);
        }
    }

    private void updateBatchCollections() {
        // Rebuilding batch groups after filters or data changes.
        List<Product> sourceProducts = selectedStatusFilter == null
                ? allProducts
                : allProducts.stream().filter(product -> selectedStatusFilter.equals(product.getStatus())).toList();

        batchItems.setAll(sourceProducts.stream()
                .map(Product::getBatchNumber)
                .distinct()
                .sorted()
                .collect(Collectors.toList()));

        if ((selectedBatch == null || !batchItems.contains(selectedBatch)) && !batchItems.isEmpty()) {
            selectedBatch = batchItems.get(0);
        }
        if (batchItems.isEmpty()) {
            selectedBatch = null;
        }

        batchSelector.setItems(batchItems);
        batchSelector.setValue(selectedBatch);
        rebuildBatchCards();
        updateBatchProducts(selectedBatch);
        updateManagedProducts(selectedBatch);
    }

    private void rebuildBatchCards() {
        batchCardsPane.getChildren().clear();
        for (String batch : batchItems) {
            batchCardsPane.getChildren().add(createBatchCard(batch));
        }
    }

    private VBox createBatchCard(String batchNumber) {
        // Collecting a small summary so each batch card shows useful info quickly.
        List<Product> products = filterProductsByBatch(batchNumber);
        long criticalCount = products.stream().filter(product -> STATUS_CRITICAL.equals(product.getStatus())).count();
        long attentionCount = products.stream().filter(product -> STATUS_NEED_ATTENTION.equals(product.getStatus())).count();
        long soonCount = products.stream().filter(product -> STATUS_EXPIRING_SOON.equals(product.getStatus())).count();
        Product earliest = products.stream().min(Comparator.comparing(Product::getExpiryDate)).orElse(null);

        Label batchLabel = new Label(batchNumber);
        batchLabel.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 17px; -fx-font-weight: 900;");

        Label countLabel = new Label(products.size() + (products.size() == 1 ? " product" : " products"));
        countLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px; -fx-font-weight: 800;");

        Label expiryLabel = new Label(earliest == null ? "No expiry data" : "Nearest expiry: " + formatDate(earliest.getExpiryDate()));
        expiryLabel.setWrapText(true);
        expiryLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");

        Label statusLine = new Label(
                criticalCount > 0 ? criticalCount + " critical item(s)"
                        : attentionCount > 0 ? attentionCount + " need attention"
                        : soonCount > 0 ? soonCount + " expiring soon"
                        : "Inventory status looks healthy"
        );
        statusLine.setStyle("-fx-text-fill: " + (criticalCount > 0 ? "#fecaca" : attentionCount > 0 ? "#fdba74" : soonCount > 0 ? "#fde68a" : "#bbf7d0")
                + "; -fx-font-size: 12px; -fx-font-weight: 800;");

        VBox card = new VBox(8, batchLabel, countLabel, expiryLabel, statusLine);
        card.setPrefWidth(170);
        card.setPadding(new Insets(18));
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> {
            selectedBatch = batchNumber;
            batchSelector.setValue(batchNumber);
            updateBatchProducts(batchNumber);
            updateManagedProducts(batchNumber);
            rebuildBatchCards();
        });
        installHoverCard(card, ACCENT_PURPLE);
        applyBatchCardStyle(card, batchNumber.equals(selectedBatch));
        return card;
    }

    private void updateBatchProducts(String batchNumber) {
        batchProducts.setAll(filterProductsByBatch(batchNumber));
    }

    private void updateManagedProducts(String batchNumber) {
        managedProducts.setAll(filterProductsByBatch(batchNumber));
    }

    private List<Product> filterProductsByBatch(String batchNumber) {
        if (batchNumber == null || batchNumber.isBlank()) {
            return List.of();
        }
        // Filtering products based on selected batch and current status filter.
        List<Product> sourceProducts = selectedStatusFilter == null
                ? allProducts
                : allProducts.stream().filter(product -> selectedStatusFilter.equals(product.getStatus())).toList();
        return sourceProducts.stream()
                .filter(product -> batchNumber.equals(product.getBatchNumber()))
                .sorted(Comparator.comparing(Product::getExpiryDate).thenComparing(Product::getName))
                .toList();
    }

    private void openProductDialog(Product productToEdit) {
        boolean isUpdate = productToEdit != null && productToEdit.getId() > 0;

        Alert dialog = new Alert(Alert.AlertType.NONE);
        dialog.setTitle(isUpdate ? "Edit Product" : "Add Product");
        dialog.getDialogPane().setStyle("-fx-background-color: " + PANEL_BG + "; -fx-font-family: 'Segoe UI';");

        TextField nameField = new TextField();
        TextField categoryField = new TextField();
        TextField batchField = new TextField();
        DatePicker mfgDate = new DatePicker();
        DatePicker expDate = new DatePicker();

        styleInput(nameField);
        styleInput(categoryField);
        styleInput(batchField);
        styleInput(mfgDate);
        styleInput(expDate);

        if (productToEdit != null) {
            nameField.setText(productToEdit.getName());
            categoryField.setText(productToEdit.getCategory());
            batchField.setText(productToEdit.getBatchNumber());
            mfgDate.setValue(productToEdit.getManufactureDate());
            expDate.setValue(productToEdit.getExpiryDate());
        }

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(8));
        form.add(createFormLabel("Product Name"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(createFormLabel("Category"), 0, 1);
        form.add(categoryField, 1, 1);
        form.add(createFormLabel("Batch Number"), 0, 2);
        form.add(batchField, 1, 2);
        form.add(createFormLabel("Manufacture Date"), 0, 3);
        form.add(mfgDate, 1, 3);
        form.add(createFormLabel("Expiry Date"), 0, 4);
        form.add(expDate, 1, 4);

        ButtonType save = new ButtonType(isUpdate ? "Save Changes" : "Add Product", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getButtonTypes().setAll(save, cancel);
        dialog.getDialogPane().setContent(form);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != save) {
            return;
        }

        try {
            // Creating one product object from form values before add or update.
            Product payload = new Product(
                    isUpdate ? productToEdit.getId() : 0,
                    nameField.getText().trim(),
                    categoryField.getText().trim(),
                    batchField.getText().trim(),
                    mfgDate.getValue(),
                    expDate.getValue()
            );
            if (isUpdate) {
                productController.updateProduct(payload);
            } else {
                productController.addProduct(payload);
            }
            refreshData();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void deleteSelectedProduct(Product selected) {
        if (selected == null) {
            showError("Select a product to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete \"" + selected.getName() + "\" from batch " + selected.getBatchNumber() + "?", ButtonType.OK, ButtonType.CANCEL);
        confirm.getDialogPane().setStyle("-fx-background-color: " + PANEL_BG + ";");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                productController.deleteProduct(selected);
                refreshData();
            } catch (SQLException ex) {
                showError("Unable to delete the selected product.");
            }
        }
    }

    private void populateSettings() {
        User currentUser = authController.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        settingsNameField.setText(currentUser.getFullName());
        settingsUsernameField.setText(currentUser.getUsername());
        settingsDobPicker.setValue(currentUser.getDateOfBirth());
    }

    private void saveSettings() {
        try {
            authController.updateCurrentUser(
                    settingsNameField.getText().trim(),
                    settingsUsernameField.getText().trim(),
                    settingsDobPicker.getValue()
            );
            refreshData();
            showInfo("Settings updated successfully.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        } catch (RuntimeException ex) {
            showError("Unable to save settings.");
        }
    }

    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(48);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setCursor(Cursor.HAND);
        button.setOnAction(e -> showSection(text));
        navigationButtons.put(text, button);
        installButtonAnimation(button);
        applyNavButtonStyle(button, false, false);
        return button;
    }

    private void applyNavButtonStyle(Button button, boolean selected, boolean hovered) {
        if (selected) {
            button.setStyle(
                    "-fx-background-color: linear-gradient(to right, " + ACCENT_PURPLE + ", " + ACCENT_BLUE_SOFT + ");"
                            + "-fx-text-fill: white;"
                            + "-fx-font-weight: 900;"
                            + "-fx-background-radius: 16;"
                            + "-fx-padding: 0 18 0 18;"
                            + "-fx-border-color: rgba(56,189,248,0.28);"
                            + "-fx-border-radius: 16;"
            );
            button.setEffect(new DropShadow(18, Color.web(ACCENT_PURPLE, hovered ? 0.45 : 0.30)));
        } else if (hovered) {
            button.setStyle(
                    "-fx-background-color: rgba(124,58,237,0.16);"
                            + "-fx-text-fill: white;"
                            + "-fx-font-weight: 800;"
                            + "-fx-background-radius: 16;"
                            + "-fx-padding: 0 18 0 18;"
                            + "-fx-border-color: rgba(124,58,237,0.22);"
                            + "-fx-border-radius: 16;"
            );
            button.setEffect(new DropShadow(14, Color.web(ACCENT_PURPLE, 0.20)));
        } else {
            button.setStyle(
                    "-fx-background-color: transparent;"
                            + "-fx-text-fill: " + TEXT_SECONDARY + ";"
                            + "-fx-font-weight: 800;"
                            + "-fx-background-radius: 16;"
                            + "-fx-padding: 0 18 0 18;"
                            + "-fx-border-color: rgba(148,163,184,0.10);"
                            + "-fx-border-radius: 16;"
            );
            button.setEffect(null);
        }
    }

    private Button createActionButton(String text, String background, String textColor, boolean strongGlow) {
        Button button = new Button(text);
        button.setPrefHeight(40);
        button.setCursor(Cursor.HAND);
        button.setStyle(buttonStyle(background, textColor));
        installButtonAnimation(button);

        Color glowColor = Color.web(background.startsWith("rgba") ? ACCENT_BLUE : background, strongGlow ? 0.30 : 0.18);
        DropShadow normal = new DropShadow(strongGlow ? 16 : 10, glowColor);
        DropShadow hover = new DropShadow(strongGlow ? 24 : 16, glowColor);
        button.setEffect(normal);
        button.setOnMouseEntered(e -> button.setEffect(hover));
        button.setOnMouseExited(e -> button.setEffect(normal));
        return button;
    }

    private String buttonStyle(String background, String textColor) {
        return "-fx-background-color: " + background + ";"
                + "-fx-text-fill: " + textColor + ";"
                + "-fx-font-weight: 900;"
                + "-fx-background-radius: 13;"
                + "-fx-padding: 0 18 0 18;";
    }

    private void styleInput(Region control) {
        String baseStyle =
                "-fx-background-color: " + PANEL_BG_ALT + ";"
                        + "-fx-text-fill: " + TEXT_PRIMARY + ";"
                        + "-fx-prompt-text-fill: rgba(183,199,221,0.80);"
                        + "-fx-background-radius: 14;"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-radius: 14;";
        control.setStyle(baseStyle);

        if (control instanceof TextField textField) {
            textField.setPrefHeight(42);
            textField.focusedProperty().addListener((obs, oldValue, focused) ->
                    textField.setStyle(baseStyle + (focused ? "-fx-border-color: " + INPUT_FOCUS + ";" : "")));
        } else if (control instanceof DatePicker datePicker) {
            datePicker.setPrefHeight(42);
            datePicker.focusedProperty().addListener((obs, oldValue, focused) ->
                    datePicker.setStyle(baseStyle + (focused ? "-fx-border-color: " + INPUT_FOCUS + ";" : "")));
        } else if (control instanceof ComboBox<?> comboBox) {
            comboBox.setPrefHeight(42);
            comboBox.setCursor(Cursor.HAND);
            comboBox.focusedProperty().addListener((obs, oldValue, focused) ->
                    comboBox.setStyle(baseStyle + (focused ? "-fx-border-color: " + INPUT_FOCUS + ";" : "")));
        }
    }

    private void styleChatInput(TextArea input) {
        String baseStyle =
                "-fx-background-color: " + PANEL_BG_ALT + ";"
                        + "-fx-control-inner-background: " + PANEL_BG_ALT + ";"
                        + "-fx-text-fill: " + TEXT_PRIMARY + ";"
                        + "-fx-prompt-text-fill: rgba(183,199,221,0.80);"
                        + "-fx-background-radius: 16;"
                        + "-fx-border-color: " + BORDER + ";"
                        + "-fx-border-radius: 16;"
                        + "-fx-padding: 10 12 10 12;";
        input.setStyle(baseStyle);
        input.focusedProperty().addListener((obs, oldValue, focused) ->
                input.setStyle(baseStyle + (focused ? "-fx-border-color: " + INPUT_FOCUS + ";" : "")));
    }

    private void installHoverCard(Region card, String glowColor) {
        card.setOnMouseEntered(e -> {
            card.setScaleX(1.02);
            card.setScaleY(1.02);
            card.setEffect(new DropShadow(22, Color.web(glowColor, 0.24)));
        });
        card.setOnMouseExited(e -> {
            card.setScaleX(1.0);
            card.setScaleY(1.0);
            card.setEffect(new DropShadow(14, Color.web(glowColor, 0.12)));
        });
        card.setEffect(new DropShadow(14, Color.web(glowColor, 0.12)));
    }

    private void applyBatchCardStyle(VBox card, boolean selected) {
        if (selected) {
            card.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, rgba(124,58,237,0.30), rgba(37,99,235,0.22));"
                            + "-fx-background-radius: 20;"
                            + "-fx-border-color: rgba(56,189,248,0.34);"
                            + "-fx-border-radius: 20;"
            );
            card.setEffect(new DropShadow(24, Color.web(ACCENT_PURPLE, 0.28)));
            card.setScaleX(1.02);
            card.setScaleY(1.02);
        } else {
            card.setStyle(panelStyle(PANEL_BG_ALT));
        }
    }

    private void installButtonAnimation(Button button) {
        button.setOnMouseEntered(e -> {
            if (navigationButtons.containsValue(button)) {
                applyNavButtonStyle(button, button == navigationButtons.get(currentSection), true);
            }
            animateScale(button, 1.03);
        });
        button.setOnMouseExited(e -> {
            if (navigationButtons.containsValue(button)) {
                applyNavButtonStyle(button, button == navigationButtons.get(currentSection), false);
            }
            animateScale(button, 1.0);
        });
        button.setOnMousePressed(e -> animateScale(button, 0.98));
        button.setOnMouseReleased(e -> animateScale(button, button.isHover() ? 1.03 : 1.0));
    }

    private void animateScale(Node node, double scale) {
        ScaleTransition transition = new ScaleTransition(Duration.millis(140), node);
        transition.setToX(scale);
        transition.setToY(scale);
        transition.play();
    }

    private String panelStyle(String background) {
        return "-fx-background-color: " + background + ";"
                + "-fx-background-radius: 24;"
                + "-fx-border-color: " + BORDER + ";"
                + "-fx-border-radius: 24;";
    }

    private Label createMetricValue() {
        Label label = new Label("0");
        label.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 30px; -fx-font-weight: 900;");
        return label;
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 12px; -fx-font-weight: 900;");
        return label;
    }

    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 18px; -fx-font-weight: 900;");
        return label;
    }

    private Label createSecondaryBody(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 12px; -fx-line-spacing: 3px;");
        return label;
    }

    private Label createBody(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px; -fx-line-spacing: 4px;");
        return label;
    }

    private Hyperlink createEmailLink(String email) {
        Hyperlink link = new Hyperlink(email);
        link.setCursor(Cursor.HAND);
        link.setStyle(
                "-fx-text-fill: #a5b4fc;"
                        + "-fx-font-size: 14px;"
                        + "-fx-font-weight: 800;"
                        + "-fx-underline: false;"
        );
        link.setOnAction(e -> openMailClient(email));
        link.setOnMouseEntered(e -> {
            link.setUnderline(true);
            link.setEffect(new DropShadow(12, Color.web(ACCENT_BLUE, 0.28)));
            animateScale(link, 1.02);
        });
        link.setOnMouseExited(e -> {
            link.setUnderline(false);
            link.setEffect(null);
            animateScale(link, 1.0);
        });
        return link;
    }

    private Product prefillBatch(String batchNumber) {
        if (batchNumber == null || batchNumber.isBlank()) {
            return null;
        }
        return new Product(0, "", "", batchNumber, LocalDate.now(), LocalDate.now().plusDays(30));
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : DATE_FORMATTER.format(date);
    }

    private void openMailClient(String email) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().mail(new URI("mailto:" + email));
            }
        } catch (Exception e) {
            showError("Unable to open the mail client.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.getDialogPane().setStyle("-fx-background-color: " + PANEL_BG + ";");
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.getDialogPane().setStyle("-fx-background-color: " + PANEL_BG + ";");
        alert.showAndWait();
    }
}
