//Created by Shreya

package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import java.sql.SQLException;
import java.util.List;


/**
 * The {@code AdminRequestPage} allows users to submit and manage admin requests.
 *
 * <p>This page includes a form for submitting requests and a list view for reviewing and deleting previously submitted ones.</p>
 */

public class AdminRequestPage extends VBox {
    private DatabaseHelper databaseHelper;
    private User currentUser;
    private TextArea requestDescription;
    private Button submitButton;
    private ListView<AdminRequest> userRequests;
    private Button deleteButton;

    /**
 * Constructs the admin request page for a specific user.
 * @param databaseHelper database helper instance
 * @param currentUser currently logged-in user
 */
public AdminRequestPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
        initializeUI();
    }

    /**
 * Initializes the layout and components of the request submission and list UI.
 */
private void initializeUI() {
        requestDescription = new TextArea();
        requestDescription.setPromptText("Enter your request details...");
        
        submitButton = new Button("Submit Request");
        submitButton.setOnAction(e -> submitRequest());
        
        userRequests = new ListView<>();
        loadUserRequests();
        
        deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> deleteSelectedRequest());
        
        this.setSpacing(10);
        this.getChildren().addAll(
            new Label("New Admin Request:"),
            requestDescription,
            submitButton,
            new Label("Your Existing Requests:"),
            userRequests,
            deleteButton
        );
    }

    /**
 * Displays the request submission page on the given stage.
 * @param primaryStage the stage to show the scene
 */
public void show(Stage primaryStage) {
        Scene scene = new Scene(this, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Admin Request Page");
        primaryStage.show();
    }

    /**
 * Submits a new admin request if the description is not empty.
 */
private void submitRequest() {
        String description = requestDescription.getText().trim();
        if (!description.isEmpty()) {
            AdminRequest newRequest = new AdminRequest(currentUser, description);
            try {
                databaseHelper.saveAdminRequest(newRequest);
                userRequests.getItems().add(newRequest);
                requestDescription.clear();
            } catch (SQLException e) {
                showAlert("Error", "Failed to submit request: " + e.getMessage());
            }
        }
    }

    /**
 * Loads the user's previously submitted requests from the database.
 */
private void loadUserRequests() {
        try {
            List<AdminRequest> requests = databaseHelper.getAdminRequestsByUser(currentUser.getUserName());
            userRequests.getItems().setAll(requests);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load requests: " + e.getMessage());
        }
    }

    /**
 * Deletes the currently selected request from the list, if it's not closed.
 */
private void deleteSelectedRequest() {
        AdminRequest selected = userRequests.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.isClosed()) {
            try {
                databaseHelper.deleteAdminRequest(selected.getId());
                userRequests.getItems().remove(selected);
            } catch (SQLException e) {
                showAlert("Error", "Failed to delete request: " + e.getMessage());
            }
        }
    }

    /**
 * Shows an alert with the given title and message.
 * @param title the alert title
 * @param message the alert message
 */
private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}