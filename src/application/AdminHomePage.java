package application;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TabPane; //Shreya

/**
 * The AdminHomePage class represents the administrative dashboard in the application.
 * It allows an admin to manage users by deleting accounts and roles.
 * 
 * <p>Admins can:
 * <ul>
 *   <li>View all users (excluding themselves)</li>
 *   <li>Delete a user from the system</li>
 *   <li>Remove specific roles from a user</li>
 * </ul>
 */

public class AdminHomePage {
    private final DatabaseHelper databaseHelper;

    public AdminHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
 * Displays the main admin dashboard with user management controls.
 * @param primaryStage the primary stage to display the UI
 * @param currentUser the current admin user
 */
public void show(Stage primaryStage, User currentUser) {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label adminLabel = new Label("Hello, Admin!");
        adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Dropdowns for user selection and role selection
        ComboBox<String> userDropdown = new ComboBox<>();
        userDropdown.setPromptText("Select user");
        loadUsernames(userDropdown, currentUser);

        ComboBox<String> roleDropdown = new ComboBox<>();
        roleDropdown.setPromptText("Select role");
        userDropdown.setOnAction(e -> loadUserRoles(roleDropdown, userDropdown.getValue()));

        // Delete User Button
        Button deleteUserButton = new Button("Delete User");
        deleteUserButton.setOnAction(button -> handleDeleteUser(userDropdown, currentUser));

        // Delete User Role Button
        Button deleteUserRoleButton = new Button("Delete User Role");
        deleteUserRoleButton.setOnAction(button -> handleDeleteUserRole(userDropdown, roleDropdown, currentUser));
        
        Button viewRequestsButton = new Button("View Admin Requests");  //Added by Shreya
        viewRequestsButton.setOnAction(e -> {
        	Stage requestViewStage = new Stage();
        	new AdminRequestViewPage(databaseHelper).show(requestViewStage);
        });
        
        Button returnButton = new Button("Return");
        returnButton.setOnAction(a -> {
            new WelcomeLoginPage(databaseHelper).show(primaryStage, currentUser); 
        });

        layout.getChildren().addAll(adminLabel, userDropdown, deleteUserButton, roleDropdown, deleteUserRoleButton, viewRequestsButton, returnButton); //Shreya
        Scene scene = new Scene(layout, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Admin Panel");
    }

    /**
 * Loads all usernames into the dropdown excluding the current admin.
 * @param userDropdown the combo box for usernames
 * @param currentUser the admin to exclude from the list
 */
private void loadUsernames(ComboBox<String> userDropdown, User currentUser) {
        userDropdown.getItems().clear();
        try {
            ResultSet rs = databaseHelper.getAllUsers();
            List<String> usernames = new ArrayList<>();
            while (rs.next()) {
                String username = rs.getString("userName");
                if (!username.equalsIgnoreCase(currentUser.getUserName())) {
                    usernames.add(username);
                }
            }
            userDropdown.getItems().addAll(usernames);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load users: " + e.getMessage());
        }
    }

    /**
 * Loads roles associated with the selected user.
 * @param roleDropdown the combo box for roles
 * @param userName the selected user's username
 */
private void loadUserRoles(ComboBox<String> roleDropdown, String userName) {
        roleDropdown.getItems().clear();
        if (userName == null) return;

        try {
            List<String> roles = databaseHelper.getUserRoles(userName);
            roleDropdown.getItems().addAll(roles);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load roles: " + e.getMessage());
        }
    }

    /**
 * Handles deletion of a user from the system.
 * @param userDropdown the dropdown containing users
 * @param currentUser the current admin
 */
private void handleDeleteUser(ComboBox<String> userDropdown, User currentUser) {
        String selectedUser = userDropdown.getValue();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a user to delete.");
            return;
        }
        if (selectedUser.equalsIgnoreCase(currentUser.getUserName())) {
            showAlert(Alert.AlertType.ERROR, "Error", "You cannot delete your own account.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Are you sure you want to delete user: " + selectedUser + "?");
        confirmation.setContentText("This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        databaseHelper.deleteUser(selectedUser);
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.INFORMATION, "Success", "User '" + selectedUser + "' deleted successfully.");
                            loadUsernames(userDropdown, currentUser);
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> 
                            showAlert(Alert.AlertType.ERROR, "Error", "There was an error! " + e.getMessage())
                        );
                    }
                }).start();
            }
        });
    }

    /**
 * Handles deletion of a specific role from a user.
 * @param userDropdown dropdown containing usernames
 * @param roleDropdown dropdown containing roles
 * @param currentUser the admin performing the action
 */
private void handleDeleteUserRole(ComboBox<String> userDropdown, ComboBox<String> roleDropdown, User currentUser) {
        String selectedUser = userDropdown.getValue();
        String selectedRole = roleDropdown.getValue();

        if (selectedUser == null || selectedRole == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select both a user and a role to delete.");
            return;
        }
        if (selectedUser.equalsIgnoreCase(currentUser.getUserName()) && selectedRole.equalsIgnoreCase("admin")) {
            showAlert(Alert.AlertType.ERROR, "Error", "You cannot remove your own admin role.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Role Deletion");
        confirmation.setHeaderText("Are you sure you want to remove role: " + selectedRole + " from user: " + selectedUser + "?");
        confirmation.setContentText("This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        databaseHelper.deleteUserRole(selectedUser, selectedRole);
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.INFORMATION, "Success", "Role '" + selectedRole + "' removed from user '" + selectedUser + "'.");
                            loadUserRoles(roleDropdown, selectedUser);
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> 
                            showAlert(Alert.AlertType.ERROR, "Error", "There was an error! " + e.getMessage())
                        );
                    }
                }).start();
            }
        });
    }

    /**
 * Displays an alert box.
 * @param alertType the type of alert
 * @param title the alert title
 * @param message the alert content
 */
private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}