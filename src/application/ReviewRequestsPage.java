package application;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button; //***RS
import javafx.scene.layout.HBox;
import java.sql.PreparedStatement;



//New file - added by Raya
/**
 * This page displays a list of users who want to be a reviewer and allows the instructor to approve or deny.
 */

/**
 * The {@code ReviewRequestsPage} class provides a page for instructors to view and respond
 * to reviewer role requests submitted by students.
 */

/**
 * The {@code ReviewRequestsPage} class allows instructors to view and respond to reviewer role requests.
 */
public class ReviewRequestsPage { 
	private final DatabaseHelper databaseHelper;
	/**
 * Constructs the review request page.
 * @param databaseHelper the shared DB helper
 */
public ReviewRequestsPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
	 /**
 * Shows the interface listing all pending reviewer role requests.
 * @param primaryStage JavaFX stage
 * @param user current instructor user
 */
public void show(Stage primaryStage, User user) {
    	VBox layout = new VBox(10);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display title -- 'Requests'
	    Label requestsLabel = new Label("Reviewer Requests");
	    requestsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    // Add the title first
	    layout.getChildren().add(requestsLabel);
	    
	    // Load and display requests
	    loadRequests(layout, primaryStage, user);

	    // returns to Instructor Home Page
	    Button returnButton = new Button("Return");
        returnButton.setOnAction(a -> {
            new InstructorHomePage(databaseHelper).show(primaryStage, user); 
        });
        
        // Add the return button last
        layout.getChildren().add(returnButton);
        
        Scene instructorScene = new Scene(layout, 800, 400);

        // Set the scene to primary stage
        primaryStage.setScene(instructorScene);
        primaryStage.setTitle("Reviewer Requests");
    }

    /**
 * Loads pending reviewer requests from the DB and displays them.
 */
private void loadRequests(VBox layout, Stage primaryStage, User user) {
        try {
            String query = "SELECT * FROM reviewer_requests WHERE status = 'Pending'";
            try (Statement stmt = databaseHelper.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                
                boolean hasRequests = false;
                while (rs.next()) {
                    hasRequests = true;
                    int requestId = rs.getInt("id");
                    String studentName = rs.getString("studentName");
                    String username = rs.getString("username");
                    
                    // Create a box for each request
                    HBox requestBox = new HBox(10);
                    requestBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");
                    
                    // Display student info
                    Label studentLabel = new Label("Student: " + studentName + " (" + username + ")");
                    
                    // Create approve and deny buttons
                    Button approveButton = new Button("Approve");
                    approveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    approveButton.setOnAction(e -> handleRequest(requestId, username, "Approved", primaryStage, user));
                    
                    Button denyButton = new Button("Deny");
                    denyButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                    denyButton.setOnAction(e -> handleRequest(requestId, username, "Denied", primaryStage, user));
                    
                    requestBox.getChildren().addAll(studentLabel, approveButton, denyButton);
                    layout.getChildren().add(requestBox);
                }
                
                if (!hasRequests) {
                    Label noRequestsLabel = new Label("No pending reviewer requests.");
                    layout.getChildren().add(noRequestsLabel);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while loading reviewer requests.");
            alert.showAndWait();
        }
    }

    /**
 * Handles approving or denying a request and updates the DB.
 */
private void handleRequest(int requestId, String username, String status, Stage primaryStage, User user) {
        try {
            // Update request status
            String updateQuery = "UPDATE reviewer_requests SET status = ? WHERE id = ?";
            try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(updateQuery)) {
                pstmt.setString(1, status);
                pstmt.setInt(2, requestId);
                pstmt.executeUpdate();
            }
            
            // If approved, add reviewer role to user
            if (status.equals("Approved")) {
                // First check if the role already exists
                String checkRoleQuery = "SELECT COUNT(*) FROM UserRoles WHERE userId = (SELECT id FROM cse360users WHERE userName = ?) AND role = 'reviewer'";
                try (PreparedStatement checkStmt = databaseHelper.getConnection().prepareStatement(checkRoleQuery)) {
                    checkStmt.setString(1, username);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        // Role doesn't exist, so add it
                        String addRoleQuery = "INSERT INTO UserRoles (userId, role) VALUES ((SELECT id FROM cse360users WHERE userName = ?), 'reviewer')";
                        try (PreparedStatement addStmt = databaseHelper.getConnection().prepareStatement(addRoleQuery)) {
                            addStmt.setString(1, username);
                            addStmt.executeUpdate();
                        }
                    }
                }
            }
            
            // Show success message
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Request has been " + status.toLowerCase() + ".");
            alert.showAndWait();
            
            // Refresh the page
            show(primaryStage, user);
            
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while processing the request: " + e.getMessage());
            alert.showAndWait();
        }
    }
}