//Created by Shreya

package application;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.text.SimpleDateFormat;
import java.util.List;
import databasePart1.DatabaseHelper;


/**
 * The {@code AdminRequestViewPage} class provides an admin-facing dashboard to view,
 * close, and reopen user-submitted admin requests.
 *
 * <p>Displays requests in two categorized tabs (Open and Closed),
 * and supports managing responses and request statuses.</p>
 */

public class AdminRequestViewPage extends BorderPane {
    private DatabaseHelper databaseHelper;
    private ListView<AdminRequest> openRequests;
    private ListView<AdminRequest> closedRequests;
    private Button closeRequestButton;
    private Button reopenRequestButton;
    private TextArea adminResponseArea;
    private TabPane tabPane;

    /**
 * Constructs the request view interface.
 * @param databaseHelper reference to the database
 */
public AdminRequestViewPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        
        // Initialize UI components
        initializeUI();
        
        // Load data
        loadOpenRequests();
        loadClosedRequests();
    }
    
    /**
 * Initializes the interface layout and components.
 */
private void initializeUI() {
        // Create tabs
        tabPane = new TabPane();
        Tab openTab = new Tab("Open Requests", openRequests = new ListView<>());
        Tab closedTab = new Tab("Closed Requests", closedRequests = new ListView<>());
        
        // Set custom cell factories
        setCellFactories();
        
        // Set placeholders
        openRequests.setPlaceholder(new Label("No open requests available"));
        closedRequests.setPlaceholder(new Label("No closed requests available"));
        
        // Response area and buttons
        adminResponseArea = new TextArea();
        adminResponseArea.setPromptText("Enter your response here...");
        adminResponseArea.setWrapText(true);
        
        closeRequestButton = new Button("Close Request");
        closeRequestButton.setStyle("-fx-base: #c62828; -fx-text-fill: white;");
        closeRequestButton.setOnAction(e -> closeSelectedRequest());
        
        reopenRequestButton = new Button("Reopen Request");
        reopenRequestButton.setStyle("-fx-base: #2e7d32; -fx-text-fill: white;");
        reopenRequestButton.setOnAction(e -> reopenSelectedRequest());
        
        // Initially disable buttons
        closeRequestButton.setDisable(true);
        reopenRequestButton.setDisable(true);
        
        // Control panel
        VBox controlPanel = new VBox(15);
        controlPanel.setPadding(new Insets(15));
        controlPanel.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-width: 1;");
        
        Label panelTitle = new Label("Request Management");
        panelTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        
        controlPanel.getChildren().addAll(
            panelTitle,
            new Label("Admin Response:"),
            adminResponseArea,
            closeRequestButton,
            reopenRequestButton
        );
        
        // Selection listeners
        openRequests.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                adminResponseArea.setText(newVal.getAdminResponse() != null ? newVal.getAdminResponse() : "");
                closeRequestButton.setDisable(false);
                reopenRequestButton.setDisable(true);
                tabPane.getSelectionModel().select(0); // Keep on Open Requests tab
            }
        });
        
        closedRequests.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                adminResponseArea.setText(newVal.getAdminResponse() != null ? newVal.getAdminResponse() : "");
                closeRequestButton.setDisable(true);
                reopenRequestButton.setDisable(false);
                tabPane.getSelectionModel().select(1); // Keep on Closed Requests tab
            }
        });
        
        tabPane.getTabs().addAll(openTab, closedTab);
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
        // Main layout
        this.setCenter(tabPane);
        this.setRight(controlPanel);
        this.setPadding(new Insets(15));
        BorderPane.setMargin(controlPanel, new Insets(0, 0, 0, 10));
    }
    
    /**
 * Sets custom render styles for open and closed request lists.
 */
private void setCellFactories() {
        // Custom cell factory for open requests
        openRequests.setCellFactory(new Callback<ListView<AdminRequest>, ListCell<AdminRequest>>() {
            @Override
            public ListCell<AdminRequest> call(ListView<AdminRequest> param) {
                return new ListCell<AdminRequest>() {
                    @Override
                    protected void updateItem(AdminRequest item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                            setTooltip(null);
                        } else {
                            setText(formatRequestText(item));
                            setTooltip(createTooltip(item));
                            setStyle("-fx-text-fill: darkgreen; -fx-font-weight: bold;");
                        }
                    }
                };
            }
        });
        
        // Custom cell factory for closed requests
        closedRequests.setCellFactory(new Callback<ListView<AdminRequest>, ListCell<AdminRequest>>() {
            @Override
            public ListCell<AdminRequest> call(ListView<AdminRequest> param) {
                return new ListCell<AdminRequest>() {
                    @Override
                    protected void updateItem(AdminRequest item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                            setTooltip(null);
                        } else {
                            setText(formatRequestText(item));
                            setTooltip(createTooltip(item));
                            setStyle("-fx-text-fill: gray;");
                        }
                    }
                };
            }
        });
    }
    
    /**
 * Formats a request for display in the list.
 * @param request the admin request object
 * @return formatted string
 */
private String formatRequestText(AdminRequest request) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, hh:mm a");
        return String.format("%s\nRequested by: %s on %s",
                request.getDescription(),
                request.getRequester().getUserName(),
                dateFormat.format(request.getRequestDate()));
    }
    
    /**
 * Creates a tooltip showing request metadata.
 * @param request the admin request
 * @return a tooltip with details
 */
private Tooltip createTooltip(AdminRequest request) {
        String responseText = request.getAdminResponse() != null ? 
                request.getAdminResponse() : "No response yet";
        String resolutionDate = request.getResolutionDate() != null ?
                new SimpleDateFormat("MMM dd, hh:mm a").format(request.getResolutionDate()) : "Not resolved";
        
        return new Tooltip(String.format(
                "Status: %s\nResponse: %s\nResolution Date: %s",
                request.isClosed() ? "CLOSED" : "OPEN",
                responseText,
                resolutionDate));
    }
    
    /**
 * Displays the admin requests dashboard UI.
 * @param primaryStage the window stage
 */
public void show(Stage primaryStage) {
        Scene scene = new Scene(this, 1000, 650);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Admin Requests Dashboard");
        primaryStage.show();
    }

    /**
 * Loads all open (unresolved) admin requests.
 */
private void loadOpenRequests() {
        try {
            List<AdminRequest> requests = databaseHelper.getAdminRequestsByStatus(false);
            openRequests.getItems().setAll(requests);
        } catch (Exception e) {
            showAlert("Error", "Failed to load open requests: " + e.getMessage());
        }
    }

    /**
 * Loads all closed (resolved) admin requests.
 */
private void loadClosedRequests() {
        try {
            List<AdminRequest> requests = databaseHelper.getAdminRequestsByStatus(true);
            closedRequests.getItems().setAll(requests);
        } catch (Exception e) {
            showAlert("Error", "Failed to load closed requests: " + e.getMessage());
        }
    }

    /**
 * Closes the selected open request and records an admin response.
 */
private void closeSelectedRequest() {
        AdminRequest selected = openRequests.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (adminResponseArea.getText().trim().isEmpty()) {
                showAlert("Warning", "Please enter a response before closing the request");
                return;
            }
            
            selected.setAdminResponse(adminResponseArea.getText().trim());
            selected.setClosed(true);
            try {
                databaseHelper.updateAdminRequest(selected);
                loadOpenRequests();
                loadClosedRequests();
                adminResponseArea.clear();
                openRequests.getSelectionModel().clearSelection();
            } catch (Exception e) {
                showAlert("Error", "Failed to close request: " + e.getMessage());
            }
        } else {
            showAlert("Warning", "Please select a request to close");
        }
    }

    /**
 * Reopens a previously closed request.
 */
private void reopenSelectedRequest() {
        AdminRequest selected = closedRequests.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setClosed(false);
            selected.setResolutionDate(null); // Clear resolution date when reopening
            selected.setAdminResponse(null); // Optional: clear response
            
            try {
                databaseHelper.updateAdminRequest(selected);
                loadOpenRequests();
                loadClosedRequests();
                closedRequests.getSelectionModel().clearSelection();
            } catch (Exception e) {
                showAlert("Error", "Failed to reopen request: " + e.getMessage());
                e.printStackTrace(); // Add this for debugging
            }
        } else {
            showAlert("Warning", "Please select a request to reopen");
        }
    }

    /**
 * Shows an alert dialog.
 * @param title the alert title
 * @param message the alert content
 */
private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}