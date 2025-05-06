package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;
import java.util.ArrayList;

/**
 * The UserLoginPage class provides a login interface for users to access their accounts.
 * It validates the user's credentials and navigates to the appropriate page upon successful login.
 */
/**
 * The {@code UserLoginPage} class provides the login screen interface for users.
 * It handles credential verification and routes users to the appropriate home page or password reset.
 */

public class UserLoginPage {
	
    private final DatabaseHelper databaseHelper;

    public UserLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    	// Input field for the user's userName, password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");


        Button loginButton = new Button("Login");
        Button forgotPasswordButton = new Button("Forgot Password");
        
        loginButton.setOnAction(a -> {
        	// Retrieve user inputs
            String userName = userNameField.getText();
            String password = passwordField.getText();
            try {
            	// Retrieve the user's name and email from the database using userName
            	String name = databaseHelper.getName(userName);
            	String email = databaseHelper.getEmail(userName);
            	
            	ArrayList<String> roles = databaseHelper.getUserRoles(userName);
            	User user=new User(name, email, userName, password, roles);
            	WelcomeLoginPage welcomeLoginPage = new WelcomeLoginPage(databaseHelper);
            
            	
            	if(roles!=null) {
            		user.setRoles(roles);
            		if (databaseHelper.login(user)) {
                        //***RS // Skip role selection for admin
                        if (roles.contains("admin")) {
                            welcomeLoginPage.show(primaryStage, user);
                        } else {
                            if (roles.isEmpty()) { // Check if the user has any roles assigned
                                // ***Redirect to role selection page if no roles are assigned (for non-admin users)
                                new RoleSelectionPage(databaseHelper, user).show(primaryStage);
                        } else {
                            welcomeLoginPage.show(primaryStage, user);
                        }
                    }
            		}else {
                        errorLabel.setText("Error logging in");
            		}
            	}
            	else {
            		// Display an error if the account does not exist
                    errorLabel.setText("user account doesn't exists");
            	}
            	
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            } 
        });
        
        forgotPasswordButton.setOnAction(e -> {
            new PasswordReset(databaseHelper).showPasswordResetPage(primaryStage); // Handle password reset
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(userNameField, passwordField, loginButton, forgotPasswordButton, errorLabel);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }
}